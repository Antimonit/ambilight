package ambilight

import ambilight.gui.SegmentColorsUpdateListener
import ambilight.mods.*

/**
 * Created by David Khol [david@khol.me] on 20. 7. 2017.
 */
@ExperimentalUnsignedTypes
class LoopingRunnable(
	config: LedConfig,
	private val ambilight: Ambilight,
	private val listener: SegmentColorsUpdateListener
) : Runnable, GUIListener {

	private var renderRate: Long = 10L
	private var updateRate: Long = 30L
	private var isLivePreview: Boolean = true
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

	override fun setLivePreview(isLivePreview: Boolean) {
		this.isLivePreview = isLivePreview
	}

	override fun setRenderRate(renderRate: Long) {
		this.renderRate = renderRate
	}

	override fun setUpdateRate(updateRate: Long) {
		this.updateRate = updateRate
	}

	override fun setSmoothness(smoothness: Int) {
		smoothnessMod.smoothness = smoothness
	}

	override fun setSaturation(saturation: Float) {
		saturationMod.saturation = saturation
	}

	override fun setBrightness(brightness: Float) {
		brightnessMod.brightness = brightness
	}

	override fun setCutOff(cutOff: Int) {
		cutOffMod.cutOff = cutOff
	}

	override fun setTemperature(temperature: Int) {
		temperatureMod.temperature = temperature
	}
}
