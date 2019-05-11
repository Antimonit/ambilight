package me.khol.ambilight.serial

import jssc.*
import me.khol.ambilight.LedColor

class SerialConnection(private val ledCount: Int) : Connection, AutoCloseable {

	private val serialData = ByteArray(ledCount * 3 + 2).also {
		it[0] = 'o'.toByte()
		it[1] = 'z'.toByte()
	}

	private var port: SerialPort? = null
	private var arduinoReadyToRead: Boolean = false

	@Throws(SerialPortException::class)
	override fun open(portName: String) {
		port = SerialPort(portName).apply {
			openPort()
			setParams(
				SerialPort.BAUDRATE_115200,
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE
			)
			addEventListener { event ->
				if (event.isRXCHAR) {
					if (event.eventValue > 0) {
						try {
							val buffer = readBytes(1)
							if (buffer[0] == 'y'.toByte()) {
								arduinoReadyToRead = true
							}
						} catch (ex: SerialPortException) {
							ex.printStackTrace()
						}
//					} else if (event.isTXEMPTY) {
//						println("Serial TXEMPTY " + event.eventValue)
//					} else {
//						println("Serial event code" + event.eventType)
					}
				}
			}
		}
		println("Opened port $portName")
	}

	override fun sendColors(segmentColors: Array<LedColor>) {
		val port = port ?: return
		if (!arduinoReadyToRead || !port.isOpened) {
			return
		}

		// Skip first two bytes which are always the same
		var dataIndex = 2
		for (i in 0 until ledCount) {
			val bytes = segmentColors[i]
			serialData[dataIndex++] = bytes.r.toByte()
			serialData[dataIndex++] = bytes.g.toByte()
			serialData[dataIndex++] = bytes.b.toByte()
		}

		arduinoReadyToRead = false

		try {
			port.writeBytes(serialData) // Issue data to Arduino
		} catch (e: SerialPortException) {
			e.printStackTrace()
		}
	}

	@Throws(SerialPortException::class)
	override fun close() {
		port?.run {
			if (isOpened) {
				println("Closing: Port is opened.")
				closePort()
			}
			println("Closed port $portName")
			port = null
		}
	}
}
