package ambilight

import ambilight.serial.Connection
import jssc.SerialPortException

/**
 * Created by David Khol [david@khol.me] on 20. 7. 2017.
 */
open class ConnectionAdapter : Connection, PortListener {

	companion object {

		private val dummyListener: Connection = object : Connection {
			override fun open(portName: String) {}
			override fun sendColors(segmentColors: Array<ByteArray>) {}
			override fun close() {}
		}
	}

	private var connection: Connection = dummyListener
	private var port: String = ""

	fun setSerialConnection(connection: Connection) {
		this.connection = connection
	}

	override fun setPortName(portName: String?) {
		if (this.port != portName && portName != null) {
			close()
			open(portName)
		}
	}

	override fun open(portName: String) {
		try {
			connection.open(portName)
			port = portName
		} catch (e: SerialPortException) {
			println("Exception port " + e.portName +
					": " + e.exceptionType +
					" (" + e.methodName + ")")
		}
	}

	override fun close() {
		try {
			connection.close()
			port = ""
		} catch (e: SerialPortException) {
			println("Exception port " + e.portName +
					": " + e.exceptionType +
					" (" + e.methodName + ")")
		}
	}

	override fun sendColors(segmentColors: Array<ByteArray>) {
		if (port != "") {
			connection.sendColors(segmentColors)
		}
	}
}