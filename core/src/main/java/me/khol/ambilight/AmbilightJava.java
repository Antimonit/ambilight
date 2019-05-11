package me.khol.ambilight;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;


/**
 * This implementation performs all the computation in Java. It works, but the performance is rather
 * bad. Better use {@link AmbilightGdi} implementation that performs the computation in C language
 * instead.
 */
public class AmbilightJava extends Ambilight {

	private static final boolean ENABLE_FADE = false;
	private static final short MAX_FADE = 256;
	private static final short FADE = 64;	// Smaller = faster
	private static final short FADE_INV = MAX_FADE - FADE;

	private static final int SAMPLE_COUNT_X = 16;
	private static final int SAMPLE_COUNT_Y = 16;
	private static final int SAMPLE_COUNT = SAMPLE_COUNT_X * SAMPLE_COUNT_Y;	// shouldn't be over 256

	private int[][] leds;
	private LedColor[] ledColor;
	private LedColor[] ledColorOld;

	private int[][] pixelOffset;

	private Robot robot;

	private Rectangle dispBounds;

	public AmbilightJava(int ledsWidth, int ledsHeight, @NotNull int[][] leds) {
		this.leds = leds;

		pixelOffset = new int[leds.length][SAMPLE_COUNT];
		ledColor    = new LedColor[leds.length];
		ledColorOld = new LedColor[leds.length];
		Arrays.setAll(ledColor, value -> new LedColor());
		Arrays.setAll(ledColorOld, value -> new LedColor());

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getScreenDevices()[0];
		DisplayMode mode = gd.getDisplayMode();
		dispBounds = new Rectangle(0, 0, mode.getWidth(), mode.getHeight());

		try {
			robot = new Robot(gd);
		} catch (AWTException e) {
			System.out.println("Robot class not supported by your system!");
			System.exit(-1);
		}


		float range, step, start;
		int[] x = new int[SAMPLE_COUNT_X];
		int[] y = new int[SAMPLE_COUNT_Y];

		for (int i = 0; i < leds.length; i++) {
			// --- for columns -----
			range = (float) dispBounds.width / ledsWidth;
			step = range / SAMPLE_COUNT_X;
			start = range * (float) leds[i][0] + step * 0.5f;

			for (int col = 0; col < SAMPLE_COUNT_X; col++) {
				x[col] = (int) (start + step * (float) col);
			}

			// ----- for rows -----
			range = (float) dispBounds.height / ledsHeight;
			step = range / SAMPLE_COUNT_Y;
			start = range * (float) leds[i][1] + step * 0.5f;

			for (int row = 0; row < SAMPLE_COUNT_Y; row++) {
				y[row] = (int) (start + step * (float) row);
			}

			// Get offset to each pixel within full screen capture
			for (int row = 0; row < SAMPLE_COUNT_Y; row++) {
				for (int col = 0; col < SAMPLE_COUNT_X; col++) {
					pixelOffset[i][row * SAMPLE_COUNT_X + col] = y[row] * dispBounds.width + x[col];
				}
			}
		}
	}

	@NotNull
	@Override
	public LedColor[] getScreenSegmentsColors() {
		BufferedImage screenshot = robot.createScreenCapture(dispBounds);

		int[] data = ((DataBufferInt) screenshot.getRaster().getDataBuffer()).getData();

		for (int ledNum = 0; ledNum < leds.length; ledNum++) {
			int r = 0;
			int g = 0;
			int b = 0;

			for (int sampleNum = 0; sampleNum < SAMPLE_COUNT; sampleNum++) {
				int pixel = data[pixelOffset[ledNum][sampleNum]];
				r += pixel & 0x00ff0000;
				g += pixel & 0x0000ff00;
				b += pixel & 0x000000ff;
			}

			if (ENABLE_FADE) {
				ledColor[ledNum].setR(((((r / SAMPLE_COUNT) >> 16) & 0xFF) * FADE_INV + ledColorOld[ledNum].getR() * FADE) / MAX_FADE);
				ledColor[ledNum].setG(((((g / SAMPLE_COUNT) >>  8) & 0xFF) * FADE_INV + ledColorOld[ledNum].getG() * FADE) / MAX_FADE);
				ledColor[ledNum].setB(((((b / SAMPLE_COUNT)      ) & 0xFF) * FADE_INV + ledColorOld[ledNum].getB() * FADE) / MAX_FADE);
			} else {
				ledColor[ledNum].setR(((r / SAMPLE_COUNT) >> 16) & 0xFF);
				ledColor[ledNum].setG(((g / SAMPLE_COUNT) >>  8) & 0xFF);
				ledColor[ledNum].setB(((b / SAMPLE_COUNT)      ) & 0xFF);
			}
		}

		if (ENABLE_FADE) {
			System.arraycopy(ledColor, 0, ledColorOld, 0, ledColor.length);
		}

		return ledColor;
	}
}