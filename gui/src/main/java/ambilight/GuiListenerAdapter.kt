package ambilight

import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

/**
 * Created by David Khol [david@khol.me] on 20. 7. 2017.
 */
open class GuiListenerAdapter(
	listener: GUIListener,
	private var isRunning: () -> Boolean
) : GUIListener {

	inner class Delegate<T>(private val prop: KMutableProperty0<T>) {

		operator fun getValue(thisRef: Any, property: KProperty<*>): T {
			return prop.get()
		}

		operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
			if (isRunning()) {
				prop.set(value)
			}
		}
	}

	override var isLivePreview by Delegate(listener::isLivePreview)
	override var renderRate by Delegate(listener::renderRate)
	override var updateRate by Delegate(listener::updateRate)
	override var smoothness by Delegate(listener::smoothness)
	override var saturation by Delegate(listener::saturation)
	override var brightness by Delegate(listener::brightness)
	override var cutOff by Delegate(listener::cutOff)
	override var temperature by Delegate(listener::temperature)
}
