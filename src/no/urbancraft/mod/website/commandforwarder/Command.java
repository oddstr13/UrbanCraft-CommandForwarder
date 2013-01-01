package no.urbancraft.mod.website.commandforwarder;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class Command extends CommandBase {
	public String command;
	public String post_url;

	public Command(String command, String post_url) {
		this.command = command;
		this.post_url = post_url;
	}

	@Override
	public String getCommandName() {
		return command;
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		if (sender instanceof EntityPlayer) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments) {
		CommandForwarder.logger.info("command: " + this.command);
		CommandForwarder.logger.info("post_url: " + this.post_url);
		CommandForwarder.logger.info("commandSender: " + sender.getCommandSenderName());
		if (sender instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) sender;
			new PosterThread(post_url, CommandForwarder.instance.identifier, command, arguments, player.username, CommandForwarder.instance.debug).run();
		} else {
			CommandForwarder.logger.warning("This command is only for players.");
		}
	}
}
