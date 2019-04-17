package ambilight;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

public class AmbilightPreview extends Ambilight {

	private final LedConfig config;
	private final byte[][] colors;
	private final List<Light> lights;

	private static final int lightCount = 5;
	private static final double dim = 1.5;

	public AmbilightPreview(LedConfig config) {
		this.config = config;

		colors = new byte[config.getLedCount()][];
		for (int i = 0; i < config.getLedCount(); i++) {
			colors[i] = new byte[3];
		}

		lights = new ArrayList<>();
		for (int i = 0; i < lightCount; i++) {
			lights.add(new Light(config.getLedsWidth(), config.getLedsHeight()));
		}
	}

	@NotNull
	@Override
	public byte[][] getScreenSegmentsColors() {
		for (int i = 0; i < config.getLedCount(); i++) {
			int[] led = config.getLed(i);
			int ledX = led[0];
			int ledY = led[1];

			int r = 0, g = 0, b = 0;
			for (Light light : lights) {
				double distX = light.x - ledX;
				double distY = light.y - ledY;
				double distance = sqrt(distX*distX + distY*distY);
				double distanceMulti = max(0, (light.size-distance) / light.size);
				r += light.r * distanceMulti;
				g += light.g * distanceMulti;
				b += light.b * distanceMulti;
			}

			colors[i][0] = (byte) min(255, r / dim);
			colors[i][1] = (byte) min(255, g / dim);
			colors[i][2] = (byte) min(255, b / dim);
		}

		return colors;
	}

	private class Light {

		double x, y;
		double velX, velY;
		double size;
		int r, g, b;

		Light(int ledWidth, int ledHeight) {
			r = (int) (256 * random());
			g = (int) (256 * random());
			b = (int) (256 * random());
			x = random() * ledWidth;
			y = random() * ledHeight;
			velX = random();
			velY = random();
			size = (0.5 + random()) * min(ledWidth, ledHeight);

			System.out.println("Color" +
							   " | x:" + (int) x +
							   " | y:" + (int) y +
							   " | size:" + (int) size);
		}
	}
}
