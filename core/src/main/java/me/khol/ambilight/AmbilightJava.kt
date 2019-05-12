package me.khol.ambilight

import java.awt.*
import java.awt.image.*

/**
 * This implementation performs all the computation in Java. It works, but the performance is rather
 * bad. Better use [AmbilightGdi] implementation that performs the computation in C language
 * instead.
 *
 * Full screen bitmap retrieval can take less than 30ms running at 30fps (because of syncing) but
 * this duration increases over time and performance drops to 20 fps (because of syncing). Reason
 * unknown.
 */
class AmbilightJava(private val config: LedConfig) : Ambilight() {

	companion object {

		private const val ENABLE_FADE = false
		private const val MAX_FADE: Short = 256
		private const val FADE: Short = 64    // Smaller = faster
		private const val FADE_INV = (MAX_FADE - FADE).toShort()

		private const val SAMPLE_COUNT_X = 16
		private const val SAMPLE_COUNT_Y = 16
		private const val SAMPLE_COUNT = SAMPLE_COUNT_X * SAMPLE_COUNT_Y    // shouldn't be over 256
	}

	private val ledColor = Array(config.ledCount) { LedColor() }
	private val ledColorOld = Array(config.ledCount) { LedColor() }
	private val pixelOffset = Array(config.ledCount) { IntArray(SAMPLE_COUNT) }

	private var robot: Robot
	private val dispBounds: Rectangle

	init {
		val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
		val gd = ge.screenDevices[0]
		val mode = gd.displayMode
		dispBounds = Rectangle(0, 0, mode.width, mode.height)

		robot = Robot(gd)

		// reused by each led
		val x = IntArray(SAMPLE_COUNT_X)
		val y = IntArray(SAMPLE_COUNT_Y)

		config.forEachIndexed { i, led ->
			run { // for columns
				val range = dispBounds.width / config.ledsWidth.toFloat()
				val step = range / SAMPLE_COUNT_X
				val start = range * led.x + step * 0.5f

				for (col in 0 until SAMPLE_COUNT_X) {
					x[col] = (start + step * col).toInt()
				}
			}

			run { // for rows
				val range = dispBounds.height / config.ledsHeight.toFloat()
				val step = range / SAMPLE_COUNT_Y
				val start = range * led.y + step * 0.5f

				for (row in 0 until SAMPLE_COUNT_Y) {
					y[row] = (start + step * row).toInt()
				}
			}

			// Get offset to each pixel within full screen capture
			for (row in 0 until SAMPLE_COUNT_Y) {
				for (col in 0 until SAMPLE_COUNT_X) {
					pixelOffset[i][row * SAMPLE_COUNT_X + col] = y[row] * dispBounds.width + x[col]
				}
			}
		}
	}

	override fun getScreenSegmentsColors(): Array<LedColor> {
		/**
		 * We could possibly get better performance by retrieving four smaller screenshots instead
		 * of retrieving a screenshot of the whole screen.
		 * But it seems like each call to [Robot.createScreenCapture] and its underlying call to
		 * [java.awt.peer.RobotPeer.getRGBPixels] is tied to rendering pipeline and it is not
		 * possible to call the method more than once for a single frame, even with just 1px area.
		 * Reimplementing [Robot.createScreenCapture] without call to [Toolkit.sync] didn't make
		 * things any faster.
		 */
		val screenshot = robot.createScreenCapture(dispBounds)
		val data = (screenshot.raster.dataBuffer as DataBufferInt).data

		for (ledNum in 0 until config.ledCount) {
			var r = 0
			var g = 0
			var b = 0

			for (sampleNum in 0 until SAMPLE_COUNT) {
				val pixel = data[pixelOffset[ledNum][sampleNum]]
				r += pixel and 0x00ff0000
				g += pixel and 0x0000ff00
				b += pixel and 0x000000ff
			}

			if (ENABLE_FADE) {
				ledColor[ledNum].r = ((r / SAMPLE_COUNT shr 16 and 0xFF) * FADE_INV + ledColorOld[ledNum].r * FADE) / MAX_FADE
				ledColor[ledNum].g = ((g / SAMPLE_COUNT shr 8 and 0xFF) * FADE_INV + ledColorOld[ledNum].g * FADE) / MAX_FADE
				ledColor[ledNum].b = ((b / SAMPLE_COUNT and 0xFF) * FADE_INV + ledColorOld[ledNum].b * FADE) / MAX_FADE
			} else {
				ledColor[ledNum].r = (r / SAMPLE_COUNT shr 16 and 0xFF).toFloat()
				ledColor[ledNum].g = (g / SAMPLE_COUNT shr 8 and 0xFF).toFloat()
				ledColor[ledNum].b = (b / SAMPLE_COUNT and 0xFF).toFloat()
			}
		}

		if (ENABLE_FADE) {
			System.arraycopy(ledColor, 0, ledColorOld, 0, ledColor.size)
		}

		return ledColor
	}
}