package ambilight.gui;

import ambilight.LedConfig;

import javax.swing.*;
import java.awt.*;

public class PreviewPanel extends JPanel {

	private LedConfig config;
	private Color[] colors;

	public PreviewPanel(LedConfig config) {
		this.config = config;
		this.colors = new Color[config.getLedCount()];
	}

	public void setColors(byte[][] segmentColors) {
		for (int i = 0; i < colors.length; i++) {
			byte[] segmentColor = segmentColors[i];
			short red   = (short) ((segmentColor[0] + 256) % 256);
			short green = (short) ((segmentColor[1] + 256) % 256);
			short blue  = (short) ((segmentColor[2] + 256) % 256);
			colors[i] = new Color(red, green, blue);
		}

		repaint();
	}


	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;

		for (int i = 0; i < colors.length; i++) {
			drawRect(g2d, colors[i], i);
		}
	}

	void drawRect(Graphics2D g2d, Color rgb, int index) {
		int posX = config.getLed(index)[0];
		int posY = config.getLed(index)[1];
		drawRect(g2d, rgb, posX, posY);
	}

	void drawRect(Graphics2D g2d, Color rgb, int posX, int posY) {
		int previewWidth = getWidth() + 1;
		int previewHeight = getHeight();
		int left = previewWidth * posX / config.getLedsWidth() - 1;
		int top = previewHeight * posY / config.getLedsHeight();
		int width = (previewWidth * (posX+1) / config.getLedsWidth()) - (previewWidth * (posX) / config.getLedsWidth());
		int height = (previewHeight * (posY+1) / config.getLedsHeight()) - (previewHeight * (posY) / config.getLedsHeight());

		g2d.setColor(rgb);
		g2d.fillRect(left, top, width, height);
		g2d.setColor(Color.BLACK);
		g2d.drawRect(left, top, width, height);
	}

}
