package me.khol.ambilight

import me.khol.ambilight.mods.*
import kotlin.math.min

/**
 * Created by David Khol [david@khol.me] on 20. 7. 2017.
 */
@ExperimentalUnsignedTypes
class LoopingRunnable(
	config: LedConfig,
	private val ambilight: Ambilight,
	private val listener: (Array<ByteArray>) -> Unit
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
	private val saturationMod = SaturationMod(1.8f)
	private val brightnessMod = BrightnessMod(1f)
	private val cutOffMod = CutOffMod(30)
	private val temperatureMod = TemperatureMod(4000)

	private val renderMods: List<Mod> = listOf(
		saturationMod,
		cutOffMod,
		brightnessMod,
		temperatureMod
	)
	private val updateMods: List<Mod> = listOf(
		smoothnessMod
	)

	private var targetSegmentColors: Array<LedColor> = Array(config.ledCount) { LedColor() }

	private fun retrieveColors(): Array<LedColor> {
		return ambilight.getScreenSegmentsColors().map {
			LedColor(it[0].toUByte().toFloat(), it[1].toUByte().toFloat(), it[2].toUByte().toFloat())
		}.toTypedArray()
	}

	private fun sendColors(colors: Array<LedColor>) {
		listener(colors.map {
			byteArrayOf(it.r.toByte(), it.g.toByte(), it.b.toByte())
		}.toTypedArray())
	}

	override fun run(): Nothing {
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

			val nextRender = renderTime + lastRenderTime - currentTime
			val nextUpdate = updateTime + lastUpdateTime - currentTime

			Thread.sleep(if (renderTime < updateTime) {
				nextUpdate
			} else {
				min(nextRender, nextUpdate)
			}.coerceAtLeast(0))
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
