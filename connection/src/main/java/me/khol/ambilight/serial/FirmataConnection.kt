package me.khol.ambilight.serial

import org.firmata4j.firmata.FirmataDevice

import java.io.IOException

class FirmataConnection {

	// construct the Firmata device instance using the name of a port
	private val device = FirmataDevice("COM4")

	init {
		try {
			device.start()
			println("Device started")

			try {
				Thread.sleep(5000)
			} catch (e: InterruptedException) {
				e.printStackTrace()
			}

			println("Waited for 5 seconds")

			try {
				device.ensureInitializationIsDone()
			} catch (e: InterruptedException) {
				println("Device initialization interrupted")
				e.printStackTrace()
			}

			println("Device initialized")

			doStuff()
		} catch (e: IOException) {
			println("Some IO exception")
			e.printStackTrace()
		} finally {
			try {
				device.stop()
			} catch (e: IOException) {
				e.printStackTrace()
			}
		}
	}

	@Throws(IOException::class)
	private fun doStuff() {
		device.sendMessage("hehe")
//		device.getPin(12).setValue(0L);
	}
}
