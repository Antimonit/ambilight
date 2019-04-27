package ambilight

import ambilight.gui.ConfigFrame
import ambilight.gui.LoopingRunnable
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

	// Bridge between Ambilight implementation and GUI frame
	private val guiListenerAdapter = GuiListenerAdapter()

	// Bridge between Ambilight implementation and SerialConnection
	private val connectionAdapter = ConnectionAdapter()

	// GUI frame
	private val window = ConfigFrame(config, guiListenerAdapter, connectionAdapter)

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
		val renderRate = 10L
		val updateRate = 30L
		val smoothness = 100
		val saturation = 1.8
		val brightness = 256
		val cutOff = 30
		val temperature = 4000

		val colorUpdateListener = LoopingRunnable.SegmentColorsUpdateListener { segmentColors ->
			window.updatedSegmentColors(segmentColors)
			connection.sendColors(segmentColors)
		}

		val currentRunnable = LoopingRunnable(ambilight, config, colorUpdateListener, renderRate, updateRate, smoothness, saturation, brightness, cutOff, temperature)
		val currentThread = Thread(currentRunnable)
		currentThread.start()

		// connect looping runnable and GUI
		guiListenerAdapter.setGUIListener(currentRunnable, currentThread::isAlive)
	}
}
