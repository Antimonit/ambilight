package ambilight

import ambilight.gui.ConfigFrame
import ambilight.gui.LoopingRunnable
import ambilight.serial.Connection
import ambilight.serial.SerialConnection
import javax.swing.SwingUtilities
import javax.swing.UIManager


class Main private constructor() {

	companion object {

		@JvmStatic fun main(args: Array<String>) {

			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
			} catch (ignored: Exception) {
			}

			SwingUtilities.invokeLater { Main() }

		}
	}

	// Configuration of our LED system
	private val config: LedConfig = LedConfig()
	// Bridge between Ambilight implementation and GUI frame
	private val guiListenerAdapter: GuiListenerAdapter = GuiListenerAdapter()
	// Bridge between Ambilight implementation and SerialConnection
	private val connectionAdapter: ConnectionAdapter = ConnectionAdapter()

	private val ambilight: Ambilight

	private val window: ConfigFrame

	private val connection: Connection

	private val currentRunnable: LoopingRunnable?
	private val currentThread: Thread?

	init {
		// Setup GUI frame
		window = ConfigFrame(config, guiListenerAdapter, connectionAdapter)

		// Setup ambilight
		ambilight = AmbilightGdi()
		ambilight.init(config.ledsWidth,
				config.ledsHeight,
				config.leds)

		// Setup serial connection
		connection = SerialConnection(config)

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

		currentRunnable = LoopingRunnable(ambilight, config, colorUpdateListener, renderRate, updateRate, smoothness, saturation, brightness, cutOff, temperature)
		currentThread = Thread(currentRunnable)
		currentThread.start()

		// TODO: close connection

		// connect looping runnable and GUI
		guiListenerAdapter.setGUIListener(currentRunnable, this::isRunnableRunning)
	}

	private fun isRunnableRunning(): Boolean {
		return currentThread != null && currentThread.isAlive && currentRunnable != null
	}

}
