package ambilight

import java.util.prefs.BackingStoreException
import java.util.prefs.Preferences

/**
 * Created by David Khol [david@khol.me] on 1. 1. 2018.
 */
object Preferences {

	private const val PORT = "port"
	private const val PORT_DEFAULT = "COM1"

	private val preferences = Preferences.userNodeForPackage(this::class.java)

	var port: String
		set(value) {
			preferences.put(PORT, value)
			preferences.save()
		}
		get() {
			return preferences.get(PORT, PORT_DEFAULT)
		}


	private fun Preferences.save() {
		try {
			flush()
		} catch (e: BackingStoreException) {
			e.printStackTrace()
		}
	}
}