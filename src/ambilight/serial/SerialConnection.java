package ambilight.serial;

import ambilight.LedConfig;
import jssc.*;

public class SerialConnection implements java.lang.AutoCloseable {

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

	public void reopen(String portName) throws SerialPortException {
		close();
		open(portName);
	}

	public void open(String portName) throws SerialPortException {
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

	public void sendColors(byte[][] segmentColors) {

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

	public void close() {
		String portName = "";
		try {
			if (port != null) {
				portName = port.getPortName();
				if (port.isOpened()) {
					System.out.println("Closing: Port is opened.");
					port.closePort();
				}
			}
		} catch (SerialPortException e) {
			e.printStackTrace();
		}

		System.out.println("Closed port " + portName);
	}


}
