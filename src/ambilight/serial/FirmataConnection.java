package ambilight.serial;

import org.firmata4j.IODevice;
import org.firmata4j.firmata.FirmataDevice;

import java.io.IOException;

public class FirmataConnection {


	private IODevice device;

	public FirmataConnection() {

		device = new FirmataDevice("COM4"); // construct the Firmata device instance using the name of a port

		try {

			device.start();
			System.out.println("Device started");

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Waited for 5 seconds");

			try {
				device.ensureInitializationIsDone(); // wait for initialization is done
			} catch (InterruptedException e) {
				System.out.println("Device initialization interrupted");
				e.printStackTrace();
			}
			System.out.println("Device initialized");

			doStuff();

		} catch (IOException e) {
			System.out.println("Some IO exception");
			e.printStackTrace();
		} finally {
			try {
				device.stop();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	private void doStuff() throws IOException {
		device.sendMessage("hehe");
//		device.getPin(12).setValue(0L);
	}

}
