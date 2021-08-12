package io.github.kyle1elyk.tank_control.testing;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import io.github.kyle1elyk.tank_control.Response;


public class TCPTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		connect();
	}
	public static boolean connect() {
		final String connectionMessage = "{\"command\":\"connect\"}";
		JsonParser jsonParser = new JsonParser();
		
		try {
			Socket tcpSocket = new Socket("localhost", 33002);
			BufferedInputStream inputStream = new BufferedInputStream(tcpSocket.getInputStream());
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			
			DataOutputStream outputStream = new DataOutputStream(tcpSocket.getOutputStream());
			
			
			outputStream.write(connectionMessage.getBytes());
			final String inputResponse = inputReader.readLine();
			JsonElement jsonResponse = jsonParser.parse(inputResponse);

			switch (jsonResponse.getAsJsonObject().get("response").getAsString()) {
				case Response.CONNECTED:
					System.out.println("Connected!");
					break;
				case Response.ALREADY_CONNECTED:
				case Response.UNAUTHORIZED:
				case Response.UNKNOWN_COMMAND:
					System.out.println("Something went wrong...");
					break;
				case Response.DISCONNECTED:
					System.out.println("Disconnected...");
					break;
				default:
					System.out.println("Something really went wrong...");
					break;
						
			}
			tcpSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return false;
		
	}
}
