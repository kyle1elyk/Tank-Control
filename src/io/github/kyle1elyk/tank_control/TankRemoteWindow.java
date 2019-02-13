package io.github.kyle1elyk.tank_control;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.util.Timer;
import java.util.TimerTask;

public class TankRemoteWindow{

	private static final int FORWARD = 50;
	private static final double DECAY_RATE = 0.9;
	private static final int DECAY_INTERVAL = 100;
	private static final long DECAY_WAIT = 500;
	
	private JFrame frame;
	private JSlider slider_throttleLeft, slider_throttleRight;
	private JLabel label_throttleLeft, label_throttleRight;
	private boolean running = true, transmitting = true;
	private TankRemote tr;
	private long lastInput;
	
	private static String address = "localhost";
	
	private static int dir, power;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		if (args.length < 1) {
			
			address = "localhost";
		} else {
			address = args[0];
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TankRemoteWindow window = new TankRemoteWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	/**
	 * Create the application.
	 */
	public TankRemoteWindow() {
		dir = FORWARD;
		power = 0;
		lastInput = System.currentTimeMillis();
		initialize();
		
		Timer decayTimer = new Timer();
		decayTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				decay();
			}
			
		}, 0, DECAY_INTERVAL);
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (running) {
					if (transmitting) {
						try {
							tr.sendControlPacket();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					try {
						Thread.sleep(25);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		try {
			tr = new TankRemote(InetAddress.getByName(address));
			tr.pack.setThrottles(TankRemote.calcThrottle(dir, power));
		} catch (SocketException | UnknownHostException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmStatistics = new JMenuItem("Statistics");
		mnFile.add(mntmStatistics);

		JSeparator separator = new JSeparator();
		mnFile.add(separator);

		JMenuItem mntmClose = new JMenuItem("Close");
		mntmClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				running = false;
				tr.pack.setThrottles((byte) 128, (byte) 128);
				try {
					tr.sendControlPacket();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.exit(0);
			}
		});
		mnFile.add(mntmClose);

		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);

		JMenuItem mntmSettings = new JMenuItem("Settings");
		mnEdit.add(mntmSettings);

		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		JMenuItem mntmAbout = new JMenuItem("About");
		mnHelp.add(mntmAbout);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));

		JLabel lblStatus = new JLabel("Sending at " + address);
		frame.getContentPane().add(lblStatus, BorderLayout.SOUTH);

		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.WEST);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 30, 0, 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 75, 75, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JLabel lblThrottle = new JLabel("Throttle");
		lblThrottle.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblThrottle = new GridBagConstraints();
		gbc_lblThrottle.insets = new Insets(0, 0, 5, 5);
		gbc_lblThrottle.gridwidth = 2;
		gbc_lblThrottle.gridx = 1;
		gbc_lblThrottle.gridy = 0;
		panel.add(lblThrottle, gbc_lblThrottle);

		byte[] throttles = tr.pack.getThrottles();

		slider_throttleLeft = new JSlider();
		slider_throttleLeft.setMinimum(-127);
		slider_throttleLeft.setMaximum(127);
		slider_throttleLeft.setValue(throttles[0]);
		slider_throttleLeft.setForeground(Color.BLACK);
		slider_throttleLeft.setEnabled(false);
		slider_throttleLeft.setOrientation(SwingConstants.VERTICAL);
		GridBagConstraints gbc_slider_throttleLeft = new GridBagConstraints();
		gbc_slider_throttleLeft.insets = new Insets(0, 0, 5, 5);
		gbc_slider_throttleLeft.ipadx = 25;
		gbc_slider_throttleLeft.fill = GridBagConstraints.BOTH;
		gbc_slider_throttleLeft.gridheight = 2;
		gbc_slider_throttleLeft.gridx = 1;
		gbc_slider_throttleLeft.gridy = 1;
		panel.add(slider_throttleLeft, gbc_slider_throttleLeft);

		slider_throttleRight = new JSlider();
		slider_throttleRight.setMinimum(-127);
		slider_throttleRight.setMaximum(127);
		slider_throttleRight.setValue(throttles[1]);
		slider_throttleRight.setForeground(Color.BLACK);
		slider_throttleRight.setEnabled(false);
		slider_throttleRight.setOrientation(SwingConstants.VERTICAL);
		GridBagConstraints gbc_slider_throttleRight = new GridBagConstraints();
		gbc_slider_throttleRight.fill = GridBagConstraints.BOTH;
		gbc_slider_throttleRight.insets = new Insets(0, 0, 5, 5);
		gbc_slider_throttleRight.ipadx = 25;
		gbc_slider_throttleRight.gridheight = 2;
		gbc_slider_throttleRight.gridx = 2;
		gbc_slider_throttleRight.gridy = 1;
		panel.add(slider_throttleRight, gbc_slider_throttleRight);

		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 5, 5);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 3;
		gbc_panel_1.gridy = 1;
		panel.add(panel_1, gbc_panel_1);

		JCheckBox chckbxTransmitting = new JCheckBox("Transmitting");
		chckbxTransmitting.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				transmitting = chckbxTransmitting.isSelected();
			}
		});
		chckbxTransmitting.setSelected(false);

		JCheckBox checkBox_lights = new JCheckBox("Lights");
		checkBox_lights.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				tr.pack.setLights(checkBox_lights.isSelected());
			}
		});
		
		JCheckBox chckbxCamera = new JCheckBox("Camera");
		chckbxCamera.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				tr.pack.setCamera(chckbxCamera.isSelected());
			}
		});
		
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
						.addComponent(chckbxTransmitting)
						.addComponent(checkBox_lights)
						.addComponent(chckbxCamera))
					.addGap(62))
		);
		gl_panel_1.setVerticalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addComponent(chckbxTransmitting)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(checkBox_lights)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chckbxCamera)
					.addContainerGap(9, Short.MAX_VALUE))
		);
		panel_1.setLayout(gl_panel_1);

		label_throttleLeft = new JLabel("0%");
		GridBagConstraints gbc_label_throttleLeft = new GridBagConstraints();
		gbc_label_throttleLeft.insets = new Insets(0, 0, 5, 5);
		gbc_label_throttleLeft.gridx = 1;
		gbc_label_throttleLeft.gridy = 3;
		panel.add(label_throttleLeft, gbc_label_throttleLeft);

		label_throttleRight = new JLabel("0%");
		GridBagConstraints gbc_label_throttleRight = new GridBagConstraints();
		gbc_label_throttleRight.insets = new Insets(0, 0, 5, 5);
		gbc_label_throttleRight.gridx = 2;
		gbc_label_throttleRight.gridy = 3;
		panel.add(label_throttleRight, gbc_label_throttleRight);
		

		setSliders(throttles);

		JLabel lblNewLabel = new JLabel("Toggles");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel.gridx = 3;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);

		lblThrottle.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("UP"), "LR_UP");
		lblThrottle.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DOWN"), "LR_DOWN");
		lblThrottle.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("LEFT"), "TURN_LEFT");
		lblThrottle.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RIGHT"), "TURN_RIGHT");
		lblThrottle.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("0"), "THROTTLE_ZERO");
		
		lblThrottle.getActionMap().put("LR_UP", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				byte[] throttles = tr.pack.getThrottles();
				
				power = Math.min(127, power + 1);
				throttles = tr.pack.setThrottles(TankRemote.calcThrottle(dir, power));
				
				setSliders(throttles);

				lastInput = System.currentTimeMillis();
				
			}
			
		});
		lblThrottle.getActionMap().put("LR_DOWN", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				byte[] throttles = tr.pack.getThrottles();

				power = Math.max(-127, power - 1);
				throttles = tr.pack.setThrottles(TankRemote.calcThrottle(dir, power));
				
				setSliders(throttles);

				lastInput = System.currentTimeMillis();
				
			}
			
		});
		lblThrottle.getActionMap().put("THROTTLE_ZERO", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				byte[] throttles = tr.pack.getThrottles();
				dir = FORWARD;
				power = 0;
				throttles = tr.pack.setThrottles(TankRemote.calcThrottle(dir, power));
				
				setSliders(throttles);

				lastInput = System.currentTimeMillis();
			}
			
		});
		lblThrottle.getActionMap().put("TURN_LEFT", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				byte[] throttles = tr.pack.getThrottles();
				dir ++;
				dir %= 200;
				
				throttles = tr.pack.setThrottles(TankRemote.calcThrottle(dir, power));
				
				setSliders(throttles);

				lastInput = System.currentTimeMillis();
			}
			
		});
		lblThrottle.getActionMap().put("TURN_RIGHT", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				byte[] throttles = tr.pack.getThrottles();
				dir --;
				dir %= 200;
				if (dir < 0) {
					dir += 200;
				}
				throttles = tr.pack.setThrottles(TankRemote.calcThrottle(dir, power));
				
				setSliders(throttles);

				lastInput = System.currentTimeMillis();
			}
			
		});
		

	}

	private void decay() {
		if ((System.currentTimeMillis() < lastInput + DECAY_WAIT) || (dir == FORWARD && power == 0)) {
			return;
		}
		
		final int dirNegativeModifier;

		if (dir == FORWARD) {
			dirNegativeModifier = 0;
		} else if (dir < FORWARD + 100 && dir > FORWARD) {
			dirNegativeModifier = -1;
		} else {
			dirNegativeModifier = 1;
		}
		
		dir += dirNegativeModifier * (int) Math.ceil(Math.abs((1.0 - DECAY_RATE) * (dir - FORWARD)));
		dir %= 200;
		if (dir < 0) {
			dir += 200;
		}
		
		
		final int powNegativeModifier;
		if (power == 0) {
			powNegativeModifier = 0;
		} else if (power > 0) {
			powNegativeModifier = -1;
		} else {
			powNegativeModifier = 1;
		}
		
		power += powNegativeModifier * (int) Math.ceil(Math.abs((1.0 - DECAY_RATE) * power));
		

		setSliders(tr.pack.setThrottles(TankRemote.calcThrottle(dir, power)));
		
	}
	private void setSliders(final byte[] throttles) {
		slider_throttleLeft.setValue(throttles[0]);
		slider_throttleRight.setValue(throttles[1]);
		
		label_throttleLeft.setText((int) (throttles[0] / 1.27) + "%");
		label_throttleRight.setText((int) (throttles[1] / 1.27) + "%");
	}
}
