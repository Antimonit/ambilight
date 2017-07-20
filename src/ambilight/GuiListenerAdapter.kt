package ambilight

/**
 * Created by David Khol [david@khol.me] on 20. 7. 2017.
 */
open class GuiListenerAdapter : GUIListener {

	companion object {
		private val dummyListener: GUIListener = object : GUIListener {
			override fun setLivePreview(selected: Boolean) {}
			override fun setRenderRate(renderRate: Long) {}
			override fun setUpdateRate(updateRate: Long) {}
			override fun setSmoothness(smoothness: Int) {}
			override fun setSaturation(saturation: Double) {}
			override fun setBrightness(brightness: Int) {}
			override fun setCutOff(cutOff: Int) {}
		}

		private val dummyPredicate: () -> Boolean = {
			false
		}
	}


	private var listener = dummyListener
	private var isRunning: () -> Boolean = dummyPredicate


	fun setGUIListener(listener: GUIListener?, runningPredicate: (() -> Boolean)?) {
		this.isRunning = runningPredicate ?: dummyPredicate
		this.listener = listener ?: dummyListener
	}

	override fun setLivePreview(selected: Boolean) {
		if (isRunning()) {
			listener.setLivePreview(selected)
		}
	}

	override fun setRenderRate(renderRate: Long) {
		if (isRunning()) {
			listener.setRenderRate(renderRate)
		}
	}

	override fun setUpdateRate(updateRate: Long) {
		if (isRunning()) {
			listener.setUpdateRate(updateRate)
		}
	}

	override fun setSmoothness(smoothness: Int) {
		if (isRunning()) {
			listener.setSmoothness(smoothness)
		}
	}

	override fun setSaturation(saturation: Double) {
		if (isRunning()) {
			listener.setSaturation(saturation)
		}
	}

	override fun setBrightness(brightness: Int) {
		if (isRunning()) {
			listener.setBrightness(brightness)
		}
	}

	override fun setCutOff(cutOff: Int) {
		if (isRunning()) {
			listener.setCutOff(cutOff)
		}
	}

}
