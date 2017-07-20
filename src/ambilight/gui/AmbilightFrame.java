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

	public static final String EMPTY_PORT_NAME = "- no port available -";

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
	private JButton livePreviewButton;
	private JPanel toolbarPanel;
	private JPanel updatePanel;
	private JSlider saturationSlider;
	private JSlider smoothnessSlider;
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
		setupLivePreviewButton();
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

		//noinspection Duplicates
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
	private void setupLivePreviewButton() {
		String playIconPath = "/assets/ic_play_32.png";
		String pauseIconPath = "/assets/ic_pause_32.png";

		ImageIcon playIcon = new ImageIcon(this.getClass().getResource(playIconPath));
		ImageIcon pauseIcon = new ImageIcon(this.getClass().getResource(pauseIconPath));

		setupBorderlessButton(livePreviewButton, pauseIcon);

		livePreviewButton.addActionListener(e -> {
			if (isRunnableRunning()) {
				currentRunnable.setLivePreview(livePreviewButton.isSelected());
			}

			//noinspection Duplicates
			if (livePreviewButton.isSelected()) {
				livePreviewButton.setSelected(false);
				livePreviewButton.setIcon(pauseIcon);
			} else {
				livePreviewButton.setSelected(true);
				livePreviewButton.setIcon(playIcon);
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
		currentRunnable = new LoopingRunnable(ambilight, config, previewPanel, portName, renderRate, updateRate, smoothness, saturation, brightness, cutOff);

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

}






