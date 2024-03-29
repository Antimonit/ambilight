package me.khol.ambilight

import me.khol.ambilight.serial.Connection
import jssc.SerialPortException

/**
 * Created by David Khol [david@khol.me] on 20. 7. 2017.
 */
open class ConnectionAdapter : Connection, PortListener {

	private var connection: Connection? = null
	private var port: String? = null

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
			connection?.open(portName)
			port = portName
		} catch (e: SerialPortException) {
			println("Exception port " + e.portName +
					": " + e.exceptionType +
					" (" + e.methodName + ")")
		}
	}

	override fun close() {
		try {
			connection?.close()
			port = null
		} catch (e: SerialPortException) {
			println("Exception port " + e.portName +
					": " + e.exceptionType +
					" (" + e.methodName + ")")
		}
	}

	override fun sendColors(segmentColors: Array<LedColor>) {
		if (port != null) {
			connection?.sendColors(segmentColors)
		}
	}
}