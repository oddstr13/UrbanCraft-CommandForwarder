package no.urbancraft.mod.website.commandforwarder;

import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TickHandler implements ITickHandler {

	private EntityPlayer getPlayerFromString(String username) {
		for (WorldServer ws : MinecraftServer.getServer().worldServers) {
			for (Object o : ws.playerEntities) {
				if (o instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) o;
					if (player.username == username) {
						return player;
					}
				}
			}
		}
		return null;
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		Object e = CommandForwarder.msgqueue.poll();
		if (e != null) {
			if (e instanceof PlayerMessage) {
				PlayerMessage msg = (PlayerMessage) e;
				EntityPlayer player = getPlayerFromString(msg.username);
				player.addChatMessage(msg.message);
			}
		}

	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {

	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.SERVER);
	}

	@Override
	public String getLabel() {
		return CommandForwarder.ID;
	}

}
