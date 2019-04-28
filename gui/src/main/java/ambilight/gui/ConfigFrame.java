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
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;


public class ConfigFrame extends HideableFrame implements LoopingRunnable.SegmentColorsUpdateListener {

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
		super("Ambilight", "/icon-inner_border.png");

		this.config = config;
		this.guiListener = guiListener;
		this.portListener = portListener;

		// setup components
		$$$setupUI$$$();
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
		String minimizeIconPath = "/ic_minimize_32.png";
		ImageIcon minimizeIcon = new ImageIcon(this.getClass().getResource(minimizeIconPath));

		setupBorderlessButton(minimizeButton, minimizeIcon);

		minimizeButton.addActionListener(e -> hideFrame());
	}

	private void setupCloseButton() {
		String closeIconPath = "/ic_close_32.png";
		ImageIcon closeIcon = new ImageIcon(this.getClass().getResource(closeIconPath));

		setupBorderlessButton(closeButton, closeIcon);

		closeButton.addActionListener(e -> exit());
	}

	private void setupPinButton() {
		String pinEnabledIconPath = "/ic_pin_enabled_32.png";
		String pinDisabledIconPath = "/ic_pin_disabled_32.png";

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
		String playIconPath = "/ic_play_32.png";
		String pauseIconPath = "/ic_pause_32.png";

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
					if (ConfigFrame.EMPTY_PORT_NAME.equals(port)) {
						portListener.setPortName(null);
					} else {
						portListener.setPortName(port);
					}
					Preferences.INSTANCE.setPort(port);
				} else {
					System.out.println("Port is null.");
				}
			}
		});
	}

	private void setupPortRefreshButton() {
		String refreshIconPath = "/ic_refresh_24.png";
		ImageIcon refreshIcon = new ImageIcon(this.getClass().getResource(refreshIconPath));

		portRefreshButton.setIcon(refreshIcon);
		portRefreshButton.setText(null);
		portRefreshButton.addActionListener(e -> refreshPortNames());
	}

	private void setupRenderRateSlider() {
		renderRateSlider.addChangeListener(e->{
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

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		createUIComponents();
		rootPanel = new JPanel();
		rootPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
		toolbarPanel = new JPanel();
		toolbarPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), 0, 0));
		rootPanel.add(toolbarPanel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		closeButton = new JButton();
		this.$$$loadButtonText$$$(closeButton, ResourceBundle.getBundle("strings").getString("close"));
		toolbarPanel.add(closeButton, new com.intellij.uiDesigner.core.GridConstraints(0, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(0, 0), new Dimension(32, 32), null, 0, false));
		final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
		toolbarPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		minimizeButton = new JButton();
		this.$$$loadButtonText$$$(minimizeButton, ResourceBundle.getBundle("strings").getString("minimize"));
		toolbarPanel.add(minimizeButton, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(0, 0), new Dimension(32, 32), null, 0, false));
		pinButton = new JButton();
		this.$$$loadButtonText$$$(pinButton, ResourceBundle.getBundle("strings").getString("pin"));
		toolbarPanel.add(pinButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(0, 0), new Dimension(32, 32), null, 0, false));
		livePreviewButton = new JButton();
		this.$$$loadButtonText$$$(livePreviewButton, ResourceBundle.getBundle("strings").getString("live_preview"));
		toolbarPanel.add(livePreviewButton, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(0, 0), new Dimension(32, 32), null, 0, false));
		contentPanel = new JPanel();
		contentPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(8, 3, new Insets(8, 16, 8, 16), -1, -1));
		rootPanel.add(contentPanel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final JLabel label1 = new JLabel();
		this.$$$loadLabelText$$$(label1, ResourceBundle.getBundle("strings").getString("label_port"));
		label1.setVerifyInputWhenFocusTarget(false);
		contentPanel.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label2 = new JLabel();
		this.$$$loadLabelText$$$(label2, ResourceBundle.getBundle("strings").getString("label_screen.capture.rate"));
		contentPanel.add(label2, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		portNamesComboBox = new JComboBox();
		final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
		portNamesComboBox.setModel(defaultComboBoxModel1);
		contentPanel.add(portNamesComboBox, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 22), null, 0, false));
		final JLabel label3 = new JLabel();
		this.$$$loadLabelText$$$(label3, ResourceBundle.getBundle("strings").getString("label_led.refresh.rate"));
		contentPanel.add(label3, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		portRefreshButton = new JButton();
		this.$$$loadButtonText$$$(portRefreshButton, ResourceBundle.getBundle("strings").getString("refresh"));
		contentPanel.add(portRefreshButton, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(0, 0), new Dimension(24, 24), null, 0, false));
		renderRateSlider = new JSlider();
		renderRateSlider.setMajorTickSpacing(10);
		renderRateSlider.setMaximum(30);
		renderRateSlider.setMinimum(0);
		renderRateSlider.setMinorTickSpacing(1);
		renderRateSlider.setPaintLabels(true);
		renderRateSlider.setPaintTicks(false);
		renderRateSlider.setSnapToTicks(true);
		renderRateSlider.setValue(10);
		contentPanel.add(renderRateSlider, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		updateRateSlider = new JSlider();
		updateRateSlider.setMajorTickSpacing(15);
		updateRateSlider.setMaximum(60);
		updateRateSlider.setMinimum(0);
		updateRateSlider.setMinorTickSpacing(5);
		updateRateSlider.setPaintLabels(true);
		updateRateSlider.setPaintTicks(false);
		updateRateSlider.setSnapToTicks(true);
		updateRateSlider.setValue(30);
		contentPanel.add(updateRateSlider, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label4 = new JLabel();
		this.$$$loadLabelText$$$(label4, ResourceBundle.getBundle("strings").getString("label_saturation"));
		contentPanel.add(label4, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		saturationSlider = new JSlider();
		saturationSlider.setMajorTickSpacing(10);
		saturationSlider.setMaximum(50);
		saturationSlider.setMinimum(0);
		saturationSlider.setMinorTickSpacing(2);
		saturationSlider.setPaintLabels(true);
		saturationSlider.setPaintTicks(false);
		saturationSlider.setSnapToTicks(false);
		saturationSlider.setValue(18);
		contentPanel.add(saturationSlider, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label5 = new JLabel();
		this.$$$loadLabelText$$$(label5, ResourceBundle.getBundle("strings").getString("label_brightness"));
		contentPanel.add(label5, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		brightnessSlider = new JSlider();
		brightnessSlider.setMajorTickSpacing(64);
		brightnessSlider.setMaximum(256);
		brightnessSlider.setMinimum(0);
		brightnessSlider.setMinorTickSpacing(10);
		brightnessSlider.setPaintLabels(true);
		brightnessSlider.setPaintTicks(false);
		brightnessSlider.setSnapToTicks(false);
		brightnessSlider.setValue(256);
		contentPanel.add(brightnessSlider, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label6 = new JLabel();
		this.$$$loadLabelText$$$(label6, ResourceBundle.getBundle("strings").getString("label_cut.off"));
		contentPanel.add(label6, new com.intellij.uiDesigner.core.GridConstraints(6, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		cutOffSlider = new JSlider();
		cutOffSlider.setMajorTickSpacing(51);
		cutOffSlider.setMaximum(255);
		cutOffSlider.setMinimum(0);
		cutOffSlider.setMinorTickSpacing(10);
		cutOffSlider.setPaintLabels(true);
		cutOffSlider.setPaintTicks(false);
		cutOffSlider.setSnapToTicks(false);
		cutOffSlider.setValue(30);
		contentPanel.add(cutOffSlider, new com.intellij.uiDesigner.core.GridConstraints(6, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label7 = new JLabel();
		this.$$$loadLabelText$$$(label7, ResourceBundle.getBundle("strings").getString("label_smoothness"));
		contentPanel.add(label7, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		smoothnessSlider = new JSlider();
		smoothnessSlider.setMajorTickSpacing(50);
		smoothnessSlider.setMaximum(256);
		smoothnessSlider.setMinimum(0);
		smoothnessSlider.setMinorTickSpacing(10);
		smoothnessSlider.setPaintLabels(true);
		smoothnessSlider.setPaintTicks(false);
		smoothnessSlider.setSnapToTicks(false);
		smoothnessSlider.setValue(100);
		contentPanel.add(smoothnessSlider, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label8 = new JLabel();
		this.$$$loadLabelText$$$(label8, ResourceBundle.getBundle("strings").getString("label_temperature"));
		contentPanel.add(label8, new com.intellij.uiDesigner.core.GridConstraints(7, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		temperatureSlider = new JSlider();
		temperatureSlider.setMajorTickSpacing(1000);
		temperatureSlider.setMaximum(16000);
		temperatureSlider.setMinimum(1000);
		temperatureSlider.setMinorTickSpacing(0);
		temperatureSlider.setPaintLabels(true);
		temperatureSlider.setPaintTicks(false);
		temperatureSlider.setSnapToTicks(false);
		temperatureSlider.setValue(4000);
		contentPanel.add(temperatureSlider, new com.intellij.uiDesigner.core.GridConstraints(7, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		rootPanel.add(previewPanel, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		label1.setLabelFor(portNamesComboBox);
	}

	/**
	 * @noinspection ALL
	 */
	private void $$$loadLabelText$$$(JLabel component, String text) {
		StringBuffer result = new StringBuffer();
		boolean haveMnemonic = false;
		char mnemonic = '\0';
		int mnemonicIndex = -1;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '&') {
				i++;
				if (i == text.length()) break;
				if (!haveMnemonic && text.charAt(i) != '&') {
					haveMnemonic = true;
					mnemonic = text.charAt(i);
					mnemonicIndex = result.length();
				}
			}
			result.append(text.charAt(i));
		}
		component.setText(result.toString());
		if (haveMnemonic) {
			component.setDisplayedMnemonic(mnemonic);
			component.setDisplayedMnemonicIndex(mnemonicIndex);
		}
	}

	/**
	 * @noinspection ALL
	 */
	private void $$$loadButtonText$$$(AbstractButton component, String text) {
		StringBuffer result = new StringBuffer();
		boolean haveMnemonic = false;
		char mnemonic = '\0';
		int mnemonicIndex = -1;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '&') {
				i++;
				if (i == text.length()) break;
				if (!haveMnemonic && text.charAt(i) != '&') {
					haveMnemonic = true;
					mnemonic = text.charAt(i);
					mnemonicIndex = result.length();
				}
			}
			result.append(text.charAt(i));
		}
		component.setText(result.toString());
		if (haveMnemonic) {
			component.setMnemonic(mnemonic);
			component.setDisplayedMnemonicIndex(mnemonicIndex);
		}
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return rootPanel;
	}
}






