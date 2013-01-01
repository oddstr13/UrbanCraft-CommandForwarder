#!/bin/bash
source VARIABLES

echo $VERSION
echo $MC

if [[ $BUILD_NUMBER ]]; then
  VERSION=$VERSION.$BUILD_NUMBER
fi

if [[ ! $WORKSPACE ]]; then
  WORKSPACE=`pwd`
fi

if [[ $NAME ]]; then
  MOD=$NAME
elif [[ $JOB_NAME ]]; then
  MOD=$JOB_NAME
else
  MOD="mod"
fi

# Get python!
if [[ ! $PYTHON ]]; then
  PYTHON=`which python2 python | head -n 1`
fi

# Cleanup functon in case of errors.
cleanup() {
  rm -rf tmp/
  rm -f minecraftforge.zip
  rm -rf forge/
  rm -rf output/
}
cleanup # In case of unclean exit last run. (Try to avoid this)

# Work on copies of files.
mkdir -p tmp
cp -rv src tmp/
cp -rv res tmp/

# Clean up .git_dummy files
find tmp/ -type f -name '.git_dummy' -delete

# Download MinecraftForge
echo "Downloading Forge..."
wget http://ken.wingedboot.com/forgemirror/files.minecraftforge.net/minecraftforge/minecraftforge-src-latest.zip -Ominecraftforge.zip || rm -f minecraftforge.zip
if [ ! -f minecraftforge.zip ]; then
  echo Failed to download MinecraftForge
  cleanup
  exit 1
fi
echo "Unziping Forge..."
unzip minecraftforge.zip
rm -f minecraftforge.zip

# Install MinecraftForge
cd forge
echo "Installing Forge..."
$PYTHON ./install.py

cd mcp

# Attempt to extract Minecraft version from MCP
if [[ -f conf/version.cfg ]]; then
  MCP_MC=`grep ServerVersion conf/version.cfg | awk -F'= ' '{ print $2 }'`
  if [[ $MCP_MC ]]; then
    MC=$MCP_MC
  fi
fi

# Replace @MACRO@'s with $VARIABLEs
echo "Applying macros..."
for startdir in "tmp/src" "tmp/res"; do
  startdir=$WORKSPACE/$startdir
  for fn in '*.java' 'mcmod.info'; do
    find $startdir -type f -name $fn -exec sed -i 's/@VERSION@/'${VERSION}'/g' '{}' ';'
    find $startdir -type f -name $fn -exec sed -i 's/@MC@/'${MC}'/g' '{}' ';'
  done
done
pwd
# Install mod files into MCP
echo "Copying ${MOD} source files into MCP..."
echo $WORKSPACE
cp -rf $WORKSPACE/tmp/src/* ./src/minecraft/

pwd
BUILD_CWDSAVE=`pwd`
if [[ -f $WORKSPACE/fetch_libs.sh ]]; then
  echo "Executing fetch_libs.sh"
  cd $WORKSPACE
  bash ./fetch_libs.sh
  cd $BUILD_CWDSAVE
fi

if [[ -d $WORKSPACE/lib ]]; then
  echo "Copying libraries into MCP..."
  cp -rvf $WORKSPACE/lib/* ./lib/
fi

pwd
echo "Recompiling..."
$PYTHON ./runtime/recompile.py
pwd
echo "Reobfuscating..."
$PYTHON ./runtime/reobfuscate.py
pwd

echo "Copying in resources..."
cp -rf $WORKSPACE/tmp/res/* ./reobf/minecraft/
pwd
mkdir -p $WORKSPACE/output
echo "Building JAR..."
cd reobf/minecraft/
jar cvf "${WORKSPACE}/output/${MOD}-core-${MC}-${VERSION}.jar" *


# Cleanup
cd $WORKSPACE
rm -rf tmp/
rm -rf forge/
