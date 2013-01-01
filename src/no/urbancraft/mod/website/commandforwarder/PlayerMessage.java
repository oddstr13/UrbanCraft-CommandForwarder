package no.urbancraft.mod.website.commandforwarder;

public class PlayerMessage {
	public String username;
	public String message;

	public PlayerMessage() {
	}

	public PlayerMessage(String username, String message) {
		this.username = username;
		this.message = message;
	}
}
