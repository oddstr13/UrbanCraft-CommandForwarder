package no.urbancraft.mod.website.commandforwarder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

import net.minecraft.server.MinecraftServer;

public class PosterThread extends Thread {
	private String post_url;
	private String identifier;
	private String username;
	private String command;
	private String[] arguments;
	private boolean debug = false;

	public PosterThread(String post_url, String identifier, String command, String[] arguments, String username) {
		this.post_url = post_url;
		this.identifier = identifier;
		this.command = command;
		this.arguments = arguments;
		this.username = username;
	}

	public PosterThread(String post_url, String identifier, String command, String[] arguments, String username, boolean debug) {
		this.post_url = post_url;
		this.identifier = identifier;
		this.command = command;
		this.arguments = arguments;
		this.username = username;
		this.debug = debug;
	}

	public String getArgumentString() {
		String result = "";
		for (String s : this.arguments) {
			result += s + ";";
		}
		if (this.arguments.length == 0) {
			return "";
		}
		return result.substring(0, result.length() - 1);
	};

	public void run() {

		try {
			String query = "";
			query += "identifier=" + URLEncoder.encode(this.identifier, "UTF-8") + "&";
			query += "command=" + URLEncoder.encode(this.command, "UTF-8") + "&";
			query += "arguments=" + URLEncoder.encode(this.getArgumentString(), "UTF-8") + "&";
			query += "username=" + URLEncoder.encode(this.username, "UTF-8");

			URL myurl = new URL(this.post_url);

			HttpURLConnection con;
			con = (HttpURLConnection) myurl.openConnection();
			con.setRequestMethod("POST");

			con.setRequestProperty("Content-length", String.valueOf(query.length()));
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			con.setRequestProperty("User-Agent", "Minecraft Forge; UrbanCraft KnownPlayers");
			con.setDoOutput(true);
			con.setDoInput(true);

			DataOutputStream output = new DataOutputStream(con.getOutputStream());

			output.writeBytes(query);

			output.close();

			DataInputStream input = new DataInputStream(con.getInputStream());

			String return_string = "";
			for (int c = input.read(); c != -1; c = input.read()) {
				return_string += (char) c;
			}
			input.close();

			if (debug) {
				CommandForwarder.logger.info("Resp Code:" + con.getResponseCode() + " " + con.getResponseMessage());
				CommandForwarder.logger.info("Resp Body:" + return_string);
			}
			CommandForwarder.msgqueue.put(new PlayerMessage(this.username, return_string));

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
