package ambilight.gui;

import ambilight.Ambilight;
import ambilight.AmbilightGdi;
import ambilight.LedConfig;
import ambilight.serial.SerialConnection;
import jssc.SerialPortException;
import jssc.SerialPortList;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Hashtable;

public class AmbilightFrame extends HideableFrame {

	private static final String EMPTY_PORT_NAME = "- no port available -";

	private JPanel rootPanel;
	private JComboBox<String> portNamesComboBox;
	private PreviewPanel previewPanel;
	private JButton portRefreshButton;
	private JSlider renderRateSlider;
	private JSlider updateRateSlider;
	private JPanel contentPanel;
	private JButton closeButton;
	private JButton minimizeButton;
	private JButton pinButton;
	private JPanel toolbarPanel;
	private JPanel updatePanel;
	private JSlider saturationSlider;
	private JSlider smoothnessSlider;
	private JButton playStopButton;
	private JSlider cutOffSlider;
	private JSlider brightnessSlider;

	private LedConfig config;
	private Ambilight ambilight;

	private LoopingRunnable currentRunnable;
	private Thread currentThread;

	private void createUIComponents() {
		config = new LedConfig();

		previewPanel = new PreviewPanel(config);
		previewPanel.setPreferredSize(new Dimension(320, 180));
	}

	public AmbilightFrame() {
		super("Ambilight", "/assets/icon-inner_border.png");

		// setup components
		setupCloseButton();
		setupMinimizeButton();
		setupPinButton();

		setupPortComboBox();
		setupPortRefreshButton();
		setupRenderRateSlider();
		setupUpdateRateSlider();
		setupSmoothnessSlider();
		setupSaturationSlider();
		setupBrightnessSlider();
		setupCutOffSlider();

		setupPortComboBox();
		setupSaturationSlider();

		setupPlayStopButton();

		rootPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

//		toolbarPanel.setBackground(new Color(110, 152, 198));

		// setup window
		setUndecorated(true);
		setType(Window.Type.UTILITY);
		setAlwaysOnTop(true);
		setContentPane(getRootPanel());
		pack();
		resetLocation();
		setResizable(false);
		setVisible(true);

		// setup ambilight
		ambilight = new AmbilightGdi();
		ambilight.init(	config.getLedsWidth(),
						config.getLedsHeight(),
						config.getLeds());

//		DynamicLedPreview preview = new DynamicLedPreview(config);
//		previewPanel.setColors(preview.getSegmentColors());

		startLoop("COM3", 10, 30, 100, 1.8, 256, 51);
	}

	private void setupPlayStopButton() {

		String playIconPath = "/assets/ic_play_32.png";
		String pauseIconPath = "/assets/ic_pause_32.png";

		ImageIcon playIcon = new ImageIcon(this.getClass().getResource(playIconPath));
		ImageIcon pauseIcon = new ImageIcon(this.getClass().getResource(pauseIconPath));

		setupBorderlessButton(playStopButton, pauseIcon);

		playStopButton.addActionListener(e -> {
			//noinspection Duplicates
			if (playStopButton.isSelected()) {
				playStopButton.setSelected(false);
				playStopButton.setIcon(playIcon);
			} else {
				playStopButton.setSelected(true);
				playStopButton.setIcon(pauseIcon);
			}
		});

	}

	// TODO: update arduino to turn off leds when there is no input signal
	// TODO: add option to turn off live mode and display solid color/spectrum

	private void setupMinimizeButton() {
		String minimizeIconPath = "/assets/ic_minimize_32.png";
		ImageIcon minimizeIcon = new ImageIcon(this.getClass().getResource(minimizeIconPath));

		setupBorderlessButton(minimizeButton, minimizeIcon);

		minimizeButton.addActionListener(e -> hideFrame());
	}
	private void setupCloseButton() {
		String closeIconPath = "/assets/ic_close_32.png";
		ImageIcon closeIcon = new ImageIcon(this.getClass().getResource(closeIconPath));

		setupBorderlessButton(closeButton, closeIcon);

		closeButton.addActionListener(e -> exit());
	}
	private void setupPinButton() {
		String pinEnabledIconPath = "/assets/ic_pin_enabled_32.png";
		String pinDisabledIconPath = "/assets/ic_pin_disabled_32.png";

		ImageIcon pinEnabledIcon = new ImageIcon(this.getClass().getResource(pinEnabledIconPath));
		ImageIcon pinDisabledIcon = new ImageIcon(this.getClass().getResource(pinDisabledIconPath));

		setupBorderlessButton(pinButton, pinDisabledIcon);

		pinButton.addActionListener(e -> {
			setMinimizeAutomatically(pinButton.isSelected());

			//noinspection Duplicates
			if (pinButton.isSelected()) {
				pinButton.setSelected(false);
				pinButton.setIcon(pinDisabledIcon);
			} else {
				pinButton.setSelected(true);
				pinButton.setIcon(pinEnabledIcon);
			}
		});
	}
	private void setupBorderlessButton(JButton button, ImageIcon icon) {
		button.setIcon(icon);
		button.setText(null);

		button.setContentAreaFilled(false);
		button.setOpaque(true);
		button.setFocusPainted(false);
//		button.setBorder(null);
//		button.setBorder(BorderFactory.createEmptyBorder(-2, -1, -1, -2));
//		button.setBorderPainted(false);

		button.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (button.getModel().isPressed()) {
					button.setBackground(new Color(143, 177, 204));
				} else if (button.getModel().isRollover()) {
					button.setBackground(new Color(161, 199, 230));
				} else {
					button.setBackground(null);
				}
			}
		});
	}

	private void setupPortComboBox() {
		refreshPortNames();

		portNamesComboBox.addItemListener(e -> {
			if (isRunnableRunning()) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String port = (String) portNamesComboBox.getSelectedItem();
					currentRunnable.setPortName(port);
				}
			}
		});
	}
	private void setupPortRefreshButton() {
		String refreshIconPath = "/assets/ic_refresh_24.png";
		ImageIcon refreshIcon = new ImageIcon(this.getClass().getResource(refreshIconPath));

		portRefreshButton.setIcon(refreshIcon);
		portRefreshButton.setText(null);
		portRefreshButton.addActionListener(e -> refreshPortNames());
	}
	private void setupRenderRateSlider() {
		renderRateSlider.addChangeListener(e -> {
			if (isRunnableRunning()) {
				int renderRate = renderRateSlider.getValue();
				if (renderRate == 0)
					renderRate = 1;
				currentRunnable.setRenderRate(renderRate);
			}
		});
	}
	private void setupUpdateRateSlider() {
		updateRateSlider.addChangeListener(e -> {
			if (isRunnableRunning()) {
				int updateRate = updateRateSlider.getValue();
				if (updateRate == 0)
					updateRate = 1;
				currentRunnable.setUpdateRate(updateRate);
			}
		});
	}
	private void setupSmoothnessSlider() {
		smoothnessSlider.addChangeListener(e -> {
			if (isRunnableRunning()) {
				int smoothness = smoothnessSlider.getValue();
				currentRunnable.setSmoothness(smoothness);
			}
		});
	}
	private void setupSaturationSlider() {
		Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
		labelTable.put(0, new JLabel("0"));
		labelTable.put(10, new JLabel("1"));
		labelTable.put(20, new JLabel("2"));
		labelTable.put(30, new JLabel("3"));
		labelTable.put(40, new JLabel("4"));
		labelTable.put(50, new JLabel("5 "));
		saturationSlider.setLabelTable(labelTable);

		saturationSlider.addChangeListener(e -> {
			if (isRunnableRunning()) {
				double saturation = (double) saturationSlider.getValue() / 10.0;
				currentRunnable.setSaturation(saturation);
			}
		});
	}
	private void setupBrightnessSlider() {
		Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
		labelTable.put(0, new JLabel("0"));
		labelTable.put(64, new JLabel(".25"));
		labelTable.put(128, new JLabel(".5"));
		labelTable.put(192, new JLabel(".75"));
		labelTable.put(256, new JLabel("1"));
		brightnessSlider.setLabelTable(labelTable);

		brightnessSlider.addChangeListener(e -> {
			if (isRunnableRunning()) {
				int brightness = brightnessSlider.getValue();
				currentRunnable.setBrightness(brightness);
			}
		});
	}
	private void setupCutOffSlider() {
		Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
		labelTable.put(0, new JLabel("0"));
		labelTable.put(51, new JLabel(".2"));
		labelTable.put(102, new JLabel(".4"));
		labelTable.put(153, new JLabel(".6"));
		labelTable.put(204, new JLabel(".8"));
		labelTable.put(255, new JLabel("1"));
		cutOffSlider.setLabelTable(labelTable);

		cutOffSlider.addChangeListener(e -> {
			if (isRunnableRunning()) {
				int cutOff = cutOffSlider.getValue();
				currentRunnable.setCutOff(cutOff);
			}
		});
	}

	private void startLoop(String portName, int renderRate, int updateRate, int smoothness, double saturation, int brightness, int cutOff) {
		currentRunnable = new LoopingRunnable(portName, renderRate, updateRate, smoothness, saturation, brightness, cutOff);

		currentThread = new Thread(currentRunnable);
		currentThread.start();
	}

	private boolean isRunnableRunning() {
		return currentThread != null && currentThread.isAlive() && currentRunnable != null;
	}

	private void refreshPortNames() {
		portNamesComboBox.removeAllItems();

		String[] portNames = SerialPortList.getPortNames();
		if (portNames.length == 0) {
			portNamesComboBox.addItem(EMPTY_PORT_NAME);
		}
		for (String portName : portNames) {
			portNamesComboBox.addItem(portName);
			System.out.println(portName);
		}

	}


	public JPanel getRootPanel() {
		return rootPanel;
	}


	private class LoopingRunnable implements Runnable {

		private static final short MAX_FADE = 256;
		private static final float Pr = 0.299f;
		private static final float Pg = 0.587f;
		private static final float Pb = 0.114f;

		private long currentTime;
		private long renderTime;
		private long updateTime;
		private long lastUpdateTime;
		private long lastRenderTime;
		private int smoothness;
		private double saturation;
		private int cutOff;
		private int brightness;
		private boolean running;

		private String portName;

		private final SerialConnection connection;

		private byte[][] targetSegmentColors;
		private byte[][] segmentColors;


		public LoopingRunnable(String portName, long renderRate, long updateRate, int smoothness, double saturation, int brightness, int cutOff) {
			this.portName = portName;
			this.renderTime = 1000 / renderRate;
			this.updateTime = 1000 / updateRate;
			this.smoothness = smoothness;
			this.saturation = saturation;
			this.brightness = brightness;
			this.cutOff = cutOff;

			currentTime = System.currentTimeMillis();
			lastRenderTime = currentTime;
			lastUpdateTime = currentTime;
			running = true;
			segmentColors = new byte[config.getLedCount()][3];
			targetSegmentColors = segmentColors;

			connection = new SerialConnection(config);

			System.out.println("Created new looping Runnable");
		}

		public void setPortName(String portName) {
			if (!this.portName.equals(portName) && !portName.equals(EMPTY_PORT_NAME)) {
				try {
					connection.reopen(portName);
				} catch (SerialPortException e) {
					System.out.println("Exception port " + e.getPortName() +
									   ": " + e.getExceptionType() +
									   " (" + e.getMethodName() + ")");
				}
			}
			this.portName = portName;
		}
		public void setRenderRate(long renderRate) {
			this.renderTime = 1000 / renderRate;
		}
		public void setUpdateRate(long updateRate) {
			this.updateTime = 1000 / updateRate;
		}
		public void setSmoothness(int smoothness) {
			this.smoothness = smoothness;
		}
		public void setSaturation(double saturation) {
			this.saturation = saturation;
		}
		public void setCutOff(int cutOff) {
			this.cutOff = cutOff;
		}
		public void setBrightness(int brightness) {
			this.brightness = brightness;
		}

		@Override
		public void run() {
			try {
				connection.open(portName);
			} catch (SerialPortException e) {
				System.out.println("Exception port " + e.getPortName() +
								   ": " + e.getExceptionType() +
								   " (" + e.getMethodName() + ")");
			}

			while (running) {
				if ((currentTime - lastRenderTime) >= renderTime) {
					render();
					lastRenderTime = System.currentTimeMillis();
				}

				if ((currentTime - lastUpdateTime) >= updateTime) {
					update();
					lastUpdateTime = System.currentTimeMillis();
				}

				currentTime = System.currentTimeMillis();
				while ((currentTime - lastRenderTime) < renderTime &&
					   (currentTime - lastUpdateTime) < updateTime) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException ignored) {
					}
					currentTime = System.currentTimeMillis();
				}
			}

			connection.close();
		}

		private void render() {
			targetSegmentColors = ambilight.getScreenSegmentsColors();
			if (saturation != 1.0)
				updateSaturation();
			if (cutOff != 0)
				updateCutOff();
			if (brightness != 256)
				updateBrightness();
		}

		private void update() {
			if (smoothness == 0) {
				previewPanel.setColors(targetSegmentColors);
				connection.sendColors(targetSegmentColors);
			} else {
				smoothSegmentColors();
				previewPanel.setColors(segmentColors);
				connection.sendColors(segmentColors);
			}
		}

		private void updateSaturation() {
			for (int i = 0; i < config.getLedCount(); i++) {
				int R = targetSegmentColors[i][0] & 0xFF;
				int G = targetSegmentColors[i][1] & 0xFF;
				int B = targetSegmentColors[i][2] & 0xFF;

				double P = Math.sqrt(R*R*Pr + G*G*Pg + B*B*Pb);

				targetSegmentColors[i][0] = (byte) Math.min(Math.max(0, (P + (R-P) * saturation)), 255);
				targetSegmentColors[i][1] = (byte) Math.min(Math.max(0, (P + (G-P) * saturation)), 255);
				targetSegmentColors[i][2] = (byte) Math.min(Math.max(0, (P + (B-P) * saturation)), 255);
			}
		}

		private void updateBrightness() {
			for (int i = 0; i < config.getLedCount(); i++) {
				targetSegmentColors[i][0] = (byte) ((targetSegmentColors[i][0] & 0xFF) * brightness / 256);
				targetSegmentColors[i][1] = (byte) ((targetSegmentColors[i][1] & 0xFF) * brightness / 256);
				targetSegmentColors[i][2] = (byte) ((targetSegmentColors[i][2] & 0xFF) * brightness / 256);
			}
		}

		private void updateCutOff() {
			if (cutOff == 255) {
				for (int i = 0; i < config.getLedCount(); i++) {
					targetSegmentColors[i][0] = 0;
					targetSegmentColors[i][1] = 0;
					targetSegmentColors[i][2] = 0;
				}
			} else {
				for (int i = 0; i < config.getLedCount(); i++) {
					int R = targetSegmentColors[i][0] & 0xFF;
					int G = targetSegmentColors[i][1] & 0xFF;
					int B = targetSegmentColors[i][2] & 0xFF;

					double HSVValue = (0.2126 * R + 0.7152 * G + 0.0722 * B);
					double multi = Math.max(0, 255 * (HSVValue - cutOff)/(255 - cutOff));

					targetSegmentColors[i][0] = (byte) (R * multi / HSVValue);
					targetSegmentColors[i][1] = (byte) (G * multi / HSVValue);
					targetSegmentColors[i][2] = (byte) (B * multi / HSVValue);
//					targetSegmentColors[i][0] = (byte) (Math.max(0, (targetSegmentColors[i][0] & 0xFF) - cutOff) * 255 / (255 - cutOff));
//					targetSegmentColors[i][1] = (byte) (Math.max(0, (targetSegmentColors[i][1] & 0xFF) - cutOff) * 255 / (255 - cutOff));
//					targetSegmentColors[i][2] = (byte) (Math.max(0, (targetSegmentColors[i][2] & 0xFF) - cutOff) * 255 / (255 - cutOff));
				}
			}
		}

		private void smoothSegmentColors() {
			final short fade = (short) (smoothness);
			final short fadeInv = (short) (MAX_FADE - fade);

			for (int ledNum = 0; ledNum < config.getLedCount(); ledNum++) {
				segmentColors[ledNum][0] = (byte) (((segmentColors[ledNum][0] & 0xFF) * fade + (targetSegmentColors[ledNum][0] & 0xFF) * fadeInv) / MAX_FADE);
				segmentColors[ledNum][1] = (byte) (((segmentColors[ledNum][1] & 0xFF) * fade + (targetSegmentColors[ledNum][1] & 0xFF) * fadeInv) / MAX_FADE);
				segmentColors[ledNum][2] = (byte) (((segmentColors[ledNum][2] & 0xFF) * fade + (targetSegmentColors[ledNum][2] & 0xFF) * fadeInv) / MAX_FADE);
			}
		}


	}
}






