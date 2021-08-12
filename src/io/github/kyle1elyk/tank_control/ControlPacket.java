package io.github.kyle1elyk.tank_control;

import com.google.gson.annotations.Expose;

public class ControlPacket {
	@Expose(serialize = false, deserialize = false)
	private static byte CONTROL_BYTE = 0x3D;
	
	private static String PROT_CONNECT = "connect",
			PROT_DISCONNECT = "disconnect",
			PROT_EXIT = "exit",
			PROT_FIRE = "fire";

	@Expose(serialize = true)
	private byte throttleLeft, throttleRight;

	@Expose(serialize = true)
	private boolean leftBrake, rightBrake;

	public ControlPacket() {
		this.throttleLeft = 0;
		this.throttleRight = 0;

		this.leftBrake = false;
		this.rightBrake = false;
	}
	public byte[] setThrottles(final byte[] throttles) {
		return this.setThrottles(throttles[0], throttles[1]);
	}
	public byte[] setThrottles(final byte left, final byte right) {
		this.throttleLeft = left;
		this.throttleRight = right;
		return new byte[] {this.throttleLeft, this.throttleRight};
	}
	public byte[] getThrottles() {
		return new byte[] {this.throttleLeft, this.throttleRight};
	}
	public boolean getLeftBrake() {
		return this.leftBrake;
	}
	public boolean getRightBrake() {
		return this.rightBrake;
	}

	public void setLeftBrake(final boolean leftBrake) {
		this.leftBrake = leftBrake;
	}
	public void setRightBrake(final boolean rightBrake) {
		this.rightBrake = rightBrake;
	}
	public byte[] getBytes() {
		byte[] returnData = new byte[5];
		returnData[0] = CONTROL_BYTE;
		returnData[1] = throttleLeft;
		returnData[2] = throttleRight;
		returnData[3] = (byte) (leftBrake ? 1 : 0);
		returnData[4] = (byte) (rightBrake ? 1 : 0);
		return returnData;
	}
	
	private String commandBuilder(final String command) {
		return  String.format("{\"command\":\"%s\"}", command);
	}
	public String getConnectionMessage() {
		return commandBuilder(PROT_CONNECT);
	}
	public String getDisconnectionMessage() {
		return commandBuilder(PROT_DISCONNECT);
	}
	public String getExitMessage() {
		return commandBuilder(PROT_EXIT);
	}
	public String getFireMessage() {
		return commandBuilder(PROT_FIRE);
	}
	
	
}
