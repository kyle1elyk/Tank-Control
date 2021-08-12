package io.github.kyle1elyk.tank_control.testing;

import java.util.ArrayList;

import net.java.games.input.Component;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;


public class ControllerTest {
	public static void main(String[] args) {


		Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
		final ArrayList<Controller> gamepads = new ArrayList<Controller>();

		for (Controller controller : controllers) {

			if (controller.getType() == Controller.Type.GAMEPAD || controller.getType() == Controller.Type.STICK) {

				gamepads.add(controller);

			} //System.out.println(controller.);
		}
		

		for (Controller controller: gamepads) {

			System.out.printf("%-10s: %s\r\n", controller.getType(), controller.getName());
			
			for (Component component : controller.getComponents()) {
				System.out.printf("\t%s (%s)\r\n", component.getName(), component.getIdentifier().toString());
				
			}

		}
		
		if (gamepads.isEmpty()) {
			System.out.println("No gamepads attached!");
			return;
		}
		boolean running = true;
		Runnable controllerThread = new Runnable() {

			@Override
			public void run() {
				boolean notDone = true;
				System.out.println("X,Y,Z,RZ");
				while (!Thread.interrupted() && running && notDone) {
					Controller controller = gamepads.get(0);
					controller.poll();
					float lx = controller.getComponent(Identifier.Axis.X).getPollData(),
							ly = controller.getComponent(Identifier.Axis.Y).getPollData(),
							rx = controller.getComponent(Identifier.Axis.RX).getPollData(),
							ry = controller.getComponent(Identifier.Axis.RZ).getPollData();
					//System.out.printf("%03.2f,%03.2f\r\n", ly, ry);
					ly = -scaleBetween(ly, -127, 127, -1f, 1f);
					ry = -scaleBetween(ry, -127, 127, -1f, 1f);
					System.out.printf("%d,%d\r\n", (int)ly, (int)ry);
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (lx < -0.45 && ly < -0.45 && rx < -0.45 && ry < -0.45) {
						notDone = false;
					}
				}
			}
			
		};
		Thread t = new Thread(controllerThread);
		t.start();
	}
	public static float scaleBetween(float unscaledNum, float minAllowed, float maxAllowed, float min, float max) {
		  return (maxAllowed - minAllowed) * (unscaledNum - min) / (max - min) + minAllowed;
		}
}
