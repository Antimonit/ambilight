package ambilight;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class AmbilightJava implements Ambilight {

	private static final boolean ENABLE_FADE = false;
	private static final short MAX_FADE = 256;
	private static final short FADE = 64;	// Smaller = faster
	private static final short FADE_INV = MAX_FADE - FADE;

	private static final int SAMPLE_COUNT_X = 16;
	private static final int SAMPLE_COUNT_Y = 16;
	private static final int SAMPLE_COUNT = SAMPLE_COUNT_X * SAMPLE_COUNT_Y;	// shouldn't be over 256


	private int[][] leds;
	private byte[][] ledColor;
	private byte[][] ledColorOld;

	private int[][] pixelOffset;

	private Robot robot;

	private Rectangle dispBounds;


	@Override
	public void init(int ledsWidth, int ledsHeight, int leds[][]) {
		this.leds = leds;

		pixelOffset = new int[leds.length][SAMPLE_COUNT];
		ledColor    = new byte[leds.length][3];
		ledColorOld = new byte[leds.length][3];

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

	@Override
	public byte[][] getScreenSegmentsColors() {
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
				ledColor[ledNum][0] = (byte) (((((r / SAMPLE_COUNT) >> 16) & 0xFF) * FADE_INV + (ledColorOld[ledNum][0] & 0xFF) * FADE) / MAX_FADE);
				ledColor[ledNum][1] = (byte) (((((g / SAMPLE_COUNT) >>  8) & 0xFF) * FADE_INV + (ledColorOld[ledNum][1] & 0xFF) * FADE) / MAX_FADE);
				ledColor[ledNum][2] = (byte) (((((b / SAMPLE_COUNT)      ) & 0xFF) * FADE_INV + (ledColorOld[ledNum][2] & 0xFF) * FADE) / MAX_FADE);
			} else {
				ledColor[ledNum][0] = (byte) (((r / SAMPLE_COUNT) >> 16) & 0xFF);
				ledColor[ledNum][1] = (byte) (((g / SAMPLE_COUNT) >>  8) & 0xFF);
				ledColor[ledNum][2] = (byte) (((b / SAMPLE_COUNT)      ) & 0xFF);
			}
		}

		if (ENABLE_FADE) {
			System.arraycopy(ledColor, 0, ledColorOld, 0, ledColor.length);
		}

		return ledColor;
	}

}