package me.khol.ambilight.gui

import java.net.URL

enum class Icon(private val path: String) {
	MINIMIZE("/ic_minimize_32.png"),
	CLOSE("/ic_close_32.png"),
	PIN_ENABLED("/ic_pin_enabled_32.png"),
	PIN_DISABLED("/ic_pin_disabled_32.png"),
	PLAY("/ic_play_32.png"),
	PAUSE("/ic_pause_32.png"),
	REFRESH("/ic_refresh_24.png");

	val url: URL
		get() = Icon::class.java.getResource(path)
}
