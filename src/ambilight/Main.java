package ambilight;

import ambilight.gui.AmbilightFrame;

import javax.swing.*;

public class Main {

	static public void main(String[] args) {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) {
		}

		SwingUtilities.invokeLater(AmbilightFrame::new);

	}

}
