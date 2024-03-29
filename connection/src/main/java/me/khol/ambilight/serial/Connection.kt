package me.khol.ambilight.serial

import jssc.SerialPortException
import me.khol.ambilight.LedColor

/**
 * Created by David Khol [david@khol.me] on 20. 7. 2017.
 */
interface Connection  {

	@Throws(SerialPortException::class)
	fun open(portName: String)

	@Throws(SerialPortException::class)
	fun close()

	fun sendColors(segmentColors: Array<LedColor>)
}
