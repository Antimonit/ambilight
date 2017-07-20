package ambilight.serial;

import org.jetbrains.annotations.NotNull;

import ambilight.LedConfig;
import jssc.*;

public class SerialConnection implements Connection, java.lang.AutoCloseable {

	private byte[] serialData;

	private SerialPort port;
	private LedConfig ledConfig;

	private boolean arduinoReadyToRead;

	public SerialConnection(LedConfig config) {
		this.ledConfig = config;

		serialData = new byte[ledConfig.getLedCount() * 3 + 2];
		serialData[0] = 'o';
		serialData[1] = 'z';
	}

	@Override
	public void open(@NotNull String portName) throws SerialPortException {
		port = new SerialPort(portName);
		port.openPort();
		port.setParams(	SerialPort.BAUDRATE_115200,
						SerialPort.DATABITS_8,
						SerialPort.STOPBITS_1,
						SerialPort.PARITY_NONE);

		port.addEventListener(event -> {
			if (event.isRXCHAR()) {
				if (event.getEventValue() > 0) {
					try {
						byte buffer[] = port.readBytes(1);
						if (buffer[0] == 'y'){
							arduinoReadyToRead = true;
						}
					} catch (SerialPortException ex) {
						ex.printStackTrace();
					}
//				} else if (event.isTXEMPTY()) {
//					System.out.println("Serial TXEMPTY " + event.getEventValue());
//				} else {
//					System.out.println("Serial event code" + event.getEventType());
				}
			}
		});

		System.out.println("Opened port " + portName);
	}

	@Override
	public void sendColors(@NotNull byte[][] segmentColors) {

		if (!arduinoReadyToRead || !port.isOpened()) {
			return;
		}

		int dataIndex = 2;
		for (int i = 0; i < ledConfig.getLedCount(); i++) {  // For each LED...
			serialData[dataIndex++] = segmentColors[i][0];
			serialData[dataIndex++] = segmentColors[i][1];
			serialData[dataIndex++] = segmentColors[i][2];
		}

		arduinoReadyToRead = false;

		try {
			port.writeBytes(serialData); // Issue data to Arduino
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws SerialPortException {
		String portName = "";
		if (port != null) {
			portName = port.getPortName();
			if (port.isOpened()) {
				System.out.println("Closing: Port is opened.");
				port.closePort();
			}
		}

		System.out.println("Closed port " + portName);
	}

}
