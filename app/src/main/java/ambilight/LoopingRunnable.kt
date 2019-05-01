package ambilight

import ambilight.gui.SegmentColorsUpdateListener
import ambilight.mods.*

/**
 * Created by David Khol [david@khol.me] on 20. 7. 2017.
 */
@ExperimentalUnsignedTypes
class LoopingRunnable(
	private val ambilight: Ambilight,
	private val config: LedConfig,
	private val listener: SegmentColorsUpdateListener
) : Runnable, GUIListener {

	private var currentTime: Long = System.currentTimeMillis()
	private val renderTime: Long get() = 1000 / renderRate
	private val updateTime: Long get() = 1000 / updateRate
	private var lastUpdateTime: Long = currentTime
	private var lastRenderTime: Long = currentTime

	private val smoothnessMod = SmoothnessMod(config.ledCount, 100)
	private val saturationMod = SaturationMod(config.ledCount, 1.8f)
	private val brightnessMod = BrightnessMod(config.ledCount, 1f)
	private val cutOffMod = CutOffMod(config.ledCount, 30)
	private val temperatureMod = TemperatureMod(config.ledCount, 4000)

	private val renderMods: List<Mod> = listOf(
		saturationMod,
		cutOffMod,
		brightnessMod,
		temperatureMod
	)
	private val updateMods: List<Mod> = listOf(
		smoothnessMod
	)

	override var renderRate: Long = 10L
	override var updateRate: Long = 30L
	override var smoothness: Int by smoothnessMod
	override var saturation: Float by saturationMod
	override var brightness: Float by brightnessMod
	override var cutOff: Int by cutOffMod
	override var temperature: Int by temperatureMod

	override var isLivePreview: Boolean = true

	private var targetSegmentColors: Array<LedColor> = Array(config.ledCount) { LedColor(0f, 0f, 0f) }

	private fun retrieveColors(): Array<LedColor> {
		return ambilight.getScreenSegmentsColors().map {
			LedColor(it[0].toUByte().toFloat(), it[1].toUByte().toFloat(), it[2].toUByte().toFloat())
		}.toTypedArray()
	}

	private fun sendColors(colors: Array<LedColor>) {
		listener.updatedSegmentColors(colors.map {
			byteArrayOf(it.r.toByte(), it.g.toByte(), it.b.toByte())
		}.toTypedArray())
	}

	override fun run() {
		while (true) {
			if (currentTime - lastRenderTime >= renderTime) {
				if (isLivePreview) {
					render()
				}
				lastRenderTime = System.currentTimeMillis()
			}

			if (currentTime - lastUpdateTime >= updateTime) {
				update()
				lastUpdateTime = System.currentTimeMillis()
			}

			currentTime = System.currentTimeMillis()
			while (currentTime - lastRenderTime < renderTime && currentTime - lastUpdateTime < updateTime) {
				try {
					Thread.sleep(1)
				} catch (ignored: InterruptedException) {
				}

				currentTime = System.currentTimeMillis()
			}
		}
	}

	private fun render() {
		targetSegmentColors = renderMods.fold(retrieveColors()) { colors, mod ->
			mod.update(colors)
		}
	}

	private fun update() {
		sendColors(updateMods.fold(targetSegmentColors) { colors, mod ->
			mod.update(colors)
		})
	}
}
