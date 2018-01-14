package ambilight.gui;

import org.jetbrains.annotations.NotNull;

import ambilight.GUIListener;
import ambilight.LedConfig;
import ambilight.PortListener;
import ambilight.Preferences;
import jssc.SerialPortList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Hashtable;

public class ConfigFrame extends HideableFrame implements LoopingRunnable.SegmentColorsUpdateListener {

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
	private JSlider saturationSlider;
	private JSlider smoothnessSlider;
	private JSlider cutOffSlider;
	private JSlider brightnessSlider;
	private JSlider temperatureSlider;

	private final LedConfig config;
	private final GUIListener guiListener;
	private final PortListener portListener;


	private void createUIComponents() {
		previewPanel = new PreviewPanel(config);
		previewPanel.setPreferredSize(new Dimension(320, 180));
	}

	public ConfigFrame(LedConfig config, GUIListener guiListener, PortListener portListener) {
		super("Ambilight", "/assets/icon-inner_border.png");

		this.config = config;
		this.guiListener = guiListener;
		this.portListener = portListener;

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
		setupTemperatureSlider();

		setupPortComboBox();
		setupSaturationSlider();

		rootPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

//		toolbarPanel.setBackground(new Color(110, 152, 198));

		// setup window
		setUndecorated(true);
		setType(Window.Type.UTILITY);
		setAlwaysOnTop(true);
		setContentPane(rootPanel);
		pack();
		resetLocation();
		setResizable(false);
		setVisible(true);
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
			guiListener.setLivePreview(livePreviewButton.isSelected());

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

		button.addChangeListener(e -> {
			if (button.getModel().isPressed()) {
				button.setBackground(new Color(143, 177, 204));
			} else if (button.getModel().isRollover()) {
				button.setBackground(new Color(161, 199, 230));
			} else {
				button.setBackground(null);
			}
		});
	}

	private void setupPortComboBox() {
		refreshPortNames();

		portNamesComboBox.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				String port = (String) portNamesComboBox.getSelectedItem();
				if (port != null) {
					portListener.setPortName(port);
					Preferences.INSTANCE.setPort(port);
				} else {
					System.out.println("Port is null.");
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
			int renderRate = renderRateSlider.getValue();
			if (renderRate == 0)
				renderRate = 1;
			guiListener.setRenderRate(renderRate);
		});
	}
	private void setupUpdateRateSlider() {
		updateRateSlider.addChangeListener(e -> {
			int updateRate = updateRateSlider.getValue();
			if (updateRate == 0)
				updateRate = 1;
			guiListener.setUpdateRate(updateRate);
		});
	}
	private void setupSmoothnessSlider() {
		Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
		labelTable.put(0, new JLabel("0"));
		labelTable.put(64, new JLabel(".25"));
		labelTable.put(128, new JLabel(".5"));
		labelTable.put(192, new JLabel(".75"));
		labelTable.put(256, new JLabel("1"));
		smoothnessSlider.setLabelTable(labelTable);

		smoothnessSlider.addChangeListener(e -> {
			int smoothness = smoothnessSlider.getValue();
			guiListener.setSmoothness(smoothness);
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
			double saturation = (double) saturationSlider.getValue() / 10.0;
			guiListener.setSaturation(saturation);
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
			int brightness = brightnessSlider.getValue();
			guiListener.setBrightness(brightness);
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
			int cutOff = cutOffSlider.getValue();
			guiListener.setCutOff(cutOff);
		});
	}
	private void setupTemperatureSlider() {
		Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
		labelTable.put(1000, new JLabel("1K"));
		labelTable.put(4000, new JLabel("4K"));
		labelTable.put(8000, new JLabel("8K"));
		labelTable.put(16000, new JLabel("16K"));
		temperatureSlider.setLabelTable(labelTable);

		temperatureSlider.addChangeListener(e -> {
			int temperature = temperatureSlider.getValue();
			guiListener.setTemperature(temperature);
		});
	}

	private void refreshPortNames() {
		portNamesComboBox.removeAllItems();

		String[] portNames = SerialPortList.getPortNames();
		if (portNames.length == 0) {
			portNamesComboBox.addItem(EMPTY_PORT_NAME);
		}
		System.out.println("Available ports:");
		for (int i = 0; i < portNames.length; i++) {
			String portName = portNames[i];
			portNamesComboBox.insertItemAt(portName, i);
			System.out.println(portName);
		}
		portNamesComboBox.setSelectedItem(Preferences.INSTANCE.getPort());
	}

	@Override
	public void updatedSegmentColors(@NotNull byte[][] segmentColors) {
		previewPanel.setColors(segmentColors);
	}

}






