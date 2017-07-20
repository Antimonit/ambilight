package ambilight

/**
 * Created by David Khol [david@khol.me] on 20. 7. 2017.
 */
interface GUIListener {

	fun setLivePreview(selected: Boolean)
	fun setRenderRate(renderRate: Long)
	fun setUpdateRate(updateRate: Long)
	fun setSmoothness(smoothness: Int)
	fun setSaturation(saturation: Double)
	fun setBrightness(brightness: Int)
	fun setCutOff(cutOff: Int)

}
