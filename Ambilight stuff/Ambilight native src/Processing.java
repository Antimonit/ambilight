import sun.awt.ComponentFactory;
import sun.awt.image.SunWritableRaster;

import java.awt.*;
import java.awt.image.*;


public class Processing {

	static final int LED_COUNT_X = 10;
	static final int LED_COUNT_Y = 6;
	static final int leds[][] = new int[][] {
											{4,5}, {3,5}, {2,5}, {1,5}, {0,5}, // Bottom edge, left half
											{0,4}, {0,3}, {0,2}, {0,1}, // Left edge
											{0,0}, {1,0}, {2,0}, {3,0}, {4,0}, {5,0}, {6,0}, {7,0}, {8,0}, {9,0}, // Top edge
											{9,1}, {9,2}, {9,3}, {9,4}, // Right edge
											{9,5}, {8,5}, {7,5}, {6,5}, {5,5}  // Bottom edge, right half
	};

	static final int SEGMENT_SAMPLE_COUNT = 256;

	static final short FADE = 70;


	Rectangle[] ledBounds = new Rectangle[leds.length];

	int[][] pixelOffset = new int[leds.length][256];

	short[][] ledColor    = new short[leds.length][3];
	short[][] prevColor   = new short[leds.length][3];

	byte[][]  gamma       = new byte[256][3];
	byte[]    serialData  = new byte[leds.length * 3 + 2];
	int data_index = 0;

	Robot robot;


	public static void main(String[] args) {
		new Processing();
	}


	int frameRate;
	long lastSecondTime;

	public Processing() {
		setup();



		lastSecondTime = System.currentTimeMillis();
		while (true) {
			long time = System.currentTimeMillis();
			if (time - lastSecondTime > 1000) {
				lastSecondTime += 1000;
				frameRate = 1;
			} else {
				frameRate += 1;
			}
			loop();
		}
	}

	private void setup() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getScreenDevices()[0];
		DisplayMode mode = gd.getDisplayMode();

		int screenWidth = mode.getWidth();
		int screenHeight = mode.getHeight();

		int ledBoundWidth = screenWidth / LED_COUNT_X;
		int ledBoundHeight = screenHeight / LED_COUNT_Y;

		for (int i = 0; i < leds.length; i++) {
			int x = leds[i][0];
			int y = leds[i][1];
			ledBounds[i] = new Rectangle(x, y, ledBoundWidth, ledBoundHeight);
		}

		try {
			robot = new Robot(gd);
		} catch (AWTException e) {
			System.out.println("Robot is not available on the system.");
			System.exit(-1);
			return;
		}
	}

	Rectangle bounds = new Rectangle(0, 0, 1920, 1080);

	Rectangle leftBounds = new Rectangle(0, 0, 240, 1080);
	Rectangle rightBounds = new Rectangle(1680, 0, 240, 1080);
	Rectangle topBounds = new Rectangle(240, 0, 1440, 216);
	Rectangle bottomBounds = new Rectangle(240, 0, 1440, 216);

	private void loop() {
//		for (int i = 0; i < leds.length; i++) {
//			Rectangle bounds = ledBounds[i];
//			BufferedImage shot = robot.createScreenCapture(bounds);
//			System.out.println("Segment: index=" + i + " | x=" + bounds.x + " | y=" + bounds.y + " | w=" + bounds.width + " | h=" + bounds.height);
//		}

//		BufferedImage fullScreenshot = robot.createScreenCapture(bounds);


//		BufferedImage leftScreenshot = robot.createScreenCapture(leftBounds);
//		BufferedImage rightScreenshot = robot.createScreenCapture(rightBounds);
//		BufferedImage topScreenshot = robot.createScreenCapture(topBounds);
//		BufferedImage bottomScreenshot = robot.createScreenCapture(bottomBounds);

		System.out.println(frameRate);
		System.arraycopy(ledColor, 0, prevColor, 0, ledColor.length);
	}


}
