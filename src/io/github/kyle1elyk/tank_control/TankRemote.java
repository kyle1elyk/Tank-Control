package io.github.kyle1elyk.tank_control;
import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

import com.google.gson.Gson;

public class TankRemote implements Closeable {
	private DatagramSocket sock;
	private InetAddress addr;
	private final int PORT = 33003;

	private byte[] buf;

	protected ControlPacket pack;

	public TankRemote(InetAddress addr) throws SocketException {
		this.sock = new DatagramSocket();
		this.addr = addr;

		this.pack = new ControlPacket();
	}
	

	public void sendControlPacket() throws IOException {
		buf = this.pack.getBytes();

		DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, PORT);
		
		this.sock.send(packet);
		

	}

	public void test() {
		Gson builder = new Gson();

		System.out.println(builder.toJson(pack));
		System.out.println(Arrays.toString(pack.getBytes()));

	}

	@Override
	public void close() {
		this.sock.close();

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
			percentRight = (1.0/25) * rad - 1;
		} else {
			percentLeft = (-1.0/25) * (rad) + 3;
			percentRight = 1;
		}

		throttles[0] = (byte) (negativeModifier * percentLeft * power);
		throttles[1] = (byte) (negativeModifier * percentRight * power);

		return throttles;
	}
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

	}

}
