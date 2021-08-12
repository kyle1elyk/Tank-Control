package io.github.kyle1elyk.tank_control;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class TankRemote implements Closeable {
	private DatagramSocket udpSocket;

	private InetAddress addr;
	private static final int TCP_PORT = 33002, UDP_PORT = 33003;

	private byte[] buf;
	private JsonParser jsonParser;

	protected ControlPacket pack;

	public TankRemote(InetAddress addr) throws SocketException {
		this.addr = addr;
		this.udpSocket = new DatagramSocket();
		this.jsonParser = new JsonParser();
		this.pack = new ControlPacket();
	}

	public void sendControlPacket() throws IOException {
		buf = this.pack.getBytes();

		DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, UDP_PORT);

		this.udpSocket.send(packet);

	}

	public boolean connect() {
		final String connectionMessage = this.pack.getConnectionMessage();
		

		try {
			Socket tcpSocket = new Socket(this.addr, TCP_PORT);
			BufferedInputStream inputStream = new BufferedInputStream(tcpSocket.getInputStream());
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

			DataOutputStream outputStream = new DataOutputStream(tcpSocket.getOutputStream());

			outputStream.write(connectionMessage.getBytes());
			final String inputResponse = inputReader.readLine();
			tcpSocket.close();

			JsonElement jsonResponse = jsonParser.parse(inputResponse);

			switch (jsonResponse.getAsJsonObject().get("response").getAsString()) {
			case Response.CONNECTED:
			case Response.ALREADY_CONNECTED:
				System.out.println("Connected!");
				return true;
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

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return false;

	}
	public boolean disconnect() {
		final String connectionMessage = this.pack.getDisconnectionMessage();
		try {
			Socket tcpSocket = new Socket(this.addr, TCP_PORT);
			BufferedInputStream inputStream = new BufferedInputStream(tcpSocket.getInputStream());
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

			DataOutputStream outputStream = new DataOutputStream(tcpSocket.getOutputStream());

			outputStream.write(connectionMessage.getBytes());
			final String inputResponse = inputReader.readLine();
			tcpSocket.close();

			JsonElement jsonResponse = jsonParser.parse(inputResponse);

			switch (jsonResponse.getAsJsonObject().get("response").getAsString()) {
			case Response.CONNECTED:
			case Response.ALREADY_CONNECTED:
				System.out.println("Connected!");
				break;
			
			case Response.UNAUTHORIZED:
			case Response.UNKNOWN_COMMAND:
				System.out.println("Something went wrong...");
				break;
			case Response.DISCONNECTED:
				System.out.println("Disconnected...");
				return true;
			default:
				System.out.println("Something really went wrong...");
				break;

			}

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}
	public void asyncFire() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				final String fireMessage = pack.getFireMessage();
				try {
					Socket tcpSocket = new Socket(addr, TCP_PORT);
					//BufferedInputStream inputStream = new BufferedInputStream(tcpSocket.getInputStream());
					//BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		
					DataOutputStream outputStream = new DataOutputStream(tcpSocket.getOutputStream());
		
					outputStream.write(fireMessage.getBytes());
					//final String inputResponse = inputReader.readLine();
					tcpSocket.close();
					/*
					JsonElement jsonResponse = jsonParser.parse(inputResponse);
		
					switch (jsonResponse.getAsJsonObject().get("response").getAsString()) {
					default:
						System.out.println("No response  yet...");
						break;
		
					}*/
					System.out.printf("Sending: \"%s\"", fireMessage);
				} catch (IOException e) {
					e.printStackTrace();
					
				}
				
			}
			
		}).start();
	}
	public void exit() {
		final String exitMessage = this.pack.getExitMessage();
		try {

			Socket tcpSocket = new Socket(this.addr, 33002);

			DataOutputStream outputStream = new DataOutputStream(tcpSocket.getOutputStream());
			outputStream.write(exitMessage.getBytes());
			
			tcpSocket.close();
		} catch  (IOException e) {
			e.printStackTrace();
		}
	}

	public void test() {
		Gson builder = new Gson();

		System.out.println(builder.toJson(pack));
		System.out.println(Arrays.toString(pack.getBytes()));

	}

	@Override
	public void close() {
		this.udpSocket.close();

	}

	protected static byte[] calcThrottle(int rad, int power) {
		/**
		 * rad is radians times 100 * pi^(-1)
		 */
		power = Math.max(power, -127);

		power = Math.min(power, 127);

		byte[] throttles = new byte[2];

		int negativeModifier = 1;
		rad %= 200;
		if (rad >= 100) {
			negativeModifier = -1;

		}
		rad %= 100;
		double percentLeft, percentRight;
		if (rad < 50) {
			percentLeft = 1;
			percentRight = (1.0 / 25) * rad - 1;
		} else {
			percentLeft = (-1.0 / 25) * (rad) + 3;
			percentRight = 1;
		}

		throttles[0] = (byte) (negativeModifier * percentLeft * power);
		throttles[1] = (byte) (negativeModifier * percentRight * power);

		return throttles;
	}
/*
	public static void main(String[] args) throws SocketException, UnknownHostException, InterruptedException {
		TankRemote tr = new TankRemote(InetAddress.getByName("localhost"));
		for (int r = -128, l = 127; r < 128; r++, l--) {
			System.out.println(l);
			tr.pack.setThrottles((byte) l, (byte) r);
			try {
				tr.sendControlPacket();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Thread.sleep(10);
		}
		tr.close();

	}*/

}
