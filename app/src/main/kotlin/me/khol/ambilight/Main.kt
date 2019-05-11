@file:JvmName("Main")
package me.khol.ambilight

import me.khol.ambilight.gui.ConfigFrame
import me.khol.ambilight.serial.Connection
import me.khol.ambilight.serial.SerialConnection
import javax.swing.SwingUtilities
import javax.swing.UIManager

fun main() {
	try {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
	} catch (ignored: Exception) {
	}
	SwingUtilities.invokeLater { start() }
}

fun start() {
	// Configuration of our LED system
	val config = LedConfig()

	// Bridge between Ambilight implementation and SerialConnection
	val connectionAdapter = ConnectionAdapter()

	// GUI frame
	val window = ConfigFrame(config, connectionAdapter)

	// Setup ambilight
	val ambilight: Ambilight = AmbilightGdi(config)

	// Serial connection that communicates with Arduino
	val connection: Connection = SerialConnection(config.ledCount)

	connectionAdapter.setSerialConnection(connection)
	connectionAdapter.open(Preferences.port)

	// create a thread that repeatedly takes screenshots and returns colors back via a listener
	val currentRunnable = LoopingRunnable(config, ambilight::getScreenSegmentsColors) { colors ->
		window.updatedSegmentColors(colors)
		connection.sendColors(colors)
	}
	val currentThread = Thread(currentRunnable)
	currentThread.start()

	// connect looping runnable and GUI
	window.setGuiListener(currentRunnable)
}