package me.khol.ambilight

/**
 * Created by David Khol [david@khol.me] on 20. 7. 2017.
 */
interface GUIListener {

	fun setLivePreview(isLivePreview: Boolean)
	fun setRenderRate(renderRate: Long)
	fun setUpdateRate(updateRate: Long)
	fun setSmoothness(smoothness: Int)
	fun setSaturation(saturation: Float)
	fun setBrightness(brightness: Float)
	fun setCutOff(cutOff: Int)
	fun setTemperature(temperature: Int)
}
