package io.github.kyle1elyk.tank_control;

import com.google.gson.annotations.Expose;

public class ControlPacket {
	@Expose(serialize = false, deserialize = false)
	private static byte CONTROL_BYTE = 0x3D;

	@Expose(serialize = true)
	private byte throttleLeft, throttleRight;

	@Expose(serialize = true)
	private boolean lightEnable, cameraEnable;

	public ControlPacket() {
		this.throttleLeft = 0;
		this.throttleRight = 0;

		this.lightEnable = false;
		this.cameraEnable = false;
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
	public boolean getLights() {
		return this.lightEnable;
	}
	public void toggleLights() {
		this.lightEnable = !this.lightEnable;
	}

	public void setLights(final boolean lights) {
		this.lightEnable = lights;
	}
	public void setCamera(final boolean camera) {
		this.cameraEnable = camera;
	}
	public byte[] getBytes() {
		byte[] returnData = new byte[5];
		returnData[0] = CONTROL_BYTE;
		returnData[1] = throttleLeft;
		returnData[2] = throttleRight;
		returnData[3] = (byte) (lightEnable ? 1 : 0);
		returnData[4] = (byte) (cameraEnable ? 1 : 0);
		return returnData;
	}

}
