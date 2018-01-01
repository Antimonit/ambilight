package ambilight.gui;

import ambilight.GUIListener;
import ambilight.Ambilight;
import ambilight.LedConfig;


/**
 * Created by David Khol [david@khol.me] on 20. 7. 2017.
 */
public class LoopingRunnable implements Runnable, GUIListener {

	private static final short MAX_FADE = 256;
	private static final float Pr = 0.299f;
	private static final float Pg = 0.587f;
	private static final float Pb = 0.114f;

	private final Ambilight ambilight;
	private final LedConfig config;
	private final SegmentColorsUpdateListener listener;

	private long currentTime;
	private long renderTime;
	private long updateTime;
	private long lastUpdateTime;
	private long lastRenderTime;
	private int smoothness;
	private double saturation;
	private int cutOff;
	private int brightness;
	private boolean isLivePreview;

	private byte[][] targetSegmentColors;
	private byte[][] segmentColors;


	public LoopingRunnable(Ambilight ambilight, LedConfig config, SegmentColorsUpdateListener listener, long renderRate, long updateRate, int smoothness, double saturation, int brightness, int cutOff) {
		this.ambilight = ambilight;
		this.config = config;
		this.listener = listener;
		this.renderTime = 1000 / renderRate;
		this.updateTime = 1000 / updateRate;
		this.smoothness = smoothness;
		this.saturation = saturation;
		this.brightness = brightness;
		this.cutOff = cutOff;

		currentTime = System.currentTimeMillis();
		lastRenderTime = currentTime;
		lastUpdateTime = currentTime;
		isLivePreview = true;
		segmentColors = new byte[config.getLedCount()][3];
		targetSegmentColors = segmentColors;

		System.out.println("Created new looping Runnable");
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
	public void setLivePreview(boolean isLivePreview) { this.isLivePreview = isLivePreview; }

	@Override
	public void run() {
		//noinspection InfiniteLoopStatement
		while (true) {
			if ((currentTime - lastRenderTime) >= renderTime) {
				if (isLivePreview) {
					render();
				}
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
			listener.updatedSegmentColors(targetSegmentColors);
		} else {
			smoothSegmentColors();
			listener.updatedSegmentColors(segmentColors);
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


	public interface SegmentColorsUpdateListener {

		void updatedSegmentColors(byte[][] segmentColors);

	}

}
