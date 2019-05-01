package ambilight

import ambilight.gui.ConfigFrame
import ambilight.gui.SegmentColorsUpdateListener
import ambilight.serial.Connection
import ambilight.serial.SerialConnection
import javax.swing.SwingUtilities
import javax.swing.UIManager

class Main private constructor() {

	companion object {

		@JvmStatic
		fun main(args: Array<String>) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
			} catch (ignored: Exception) {
			}
			SwingUtilities.invokeLater { Main() }
		}
	}

	// Configuration of our LED system
	private val config = LedConfig()

	// Bridge between Ambilight implementation and SerialConnection
	private val connectionAdapter = ConnectionAdapter()

	// GUI frame
	private val window = ConfigFrame(config, connectionAdapter)

	// Setup ambilight
	private val ambilight: Ambilight = AmbilightGdi(
		config.ledsWidth,
		config.ledsHeight,
		config.leds
	)

	// Serial connection that communicates with Arduino
	private val connection: Connection = SerialConnection(config.ledCount)

	init {
		connectionAdapter.setSerialConnection(connection)
		connectionAdapter.open(Preferences.port)

		// create a thread that repeatedly takes screenshots and returns colors back via a listener
		val currentRunnable = LoopingRunnable(config, ambilight, SegmentColorsUpdateListener { colors ->
			window.updatedSegmentColors(colors)
			connection.sendColors(colors)
		})
		val currentThread = Thread(currentRunnable)
		currentThread.start()

		// connect looping runnable and GUI
		window.setGuiListener(currentRunnable)
	}
}
