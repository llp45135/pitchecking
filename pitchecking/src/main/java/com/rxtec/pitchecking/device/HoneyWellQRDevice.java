package com.rxtec.pitchecking.device;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rxtec.pitchecking.QRReader;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.DateUtils;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/**
 * 闸门控制及指示灯控制
 * 
 * @author lenovo
 *
 */
public class HoneyWellQRDevice implements SerialPortEventListener {
	private Log log = LogFactory.getLog("HoneyWellQRDevice");
	private static HoneyWellQRDevice instance;
	private SerialPort serialPort;
	private InputStream in;
	private OutputStream out;
	private byte[] bufferBytes = new byte[1024];
	private String bufferData = "";
	private static int retryTimes = 3;
	private static int delayTime = 30;
	private static int delayCount = 1000;

	public static synchronized HoneyWellQRDevice getInstance() {
		if (instance == null) {
			instance = new HoneyWellQRDevice();
		}
		return instance;
	}

	private HoneyWellQRDevice() {
	}

	/**
	 * 打开串口
	 * 
	 * @param portName
	 * @throws Exception
	 */
	public void connect(String portName) throws Exception {
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		if (portIdentifier.isCurrentlyOwned()) {
			System.err.println("Error: Port is currently in use");
		} else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

			if (commPort instanceof SerialPort) {
				serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(DeviceConfig.getInstance().getGateCrtlRate(), SerialPort.DATABITS_8,
						SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

				this.in = serialPort.getInputStream();
				this.out = serialPort.getOutputStream();

				serialPort.addEventListener(this);
				serialPort.notifyOnDataAvailable(true);
				serialPort.notifyOnBreakInterrupt(true);
				log.debug("串口已经打开:" + portName);

			} else {
				System.err.println("Error: Only serial ports are handled by this example.");
			}
		}
	}

	/**
	 * 关闭串口
	 */
	public void close() {
		this.serialPort.close();
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		// TODO Auto-generated method stub
		switch (event.getEventType()) {
		case SerialPortEvent.BI:
		case SerialPortEvent.OE:
		case SerialPortEvent.FE:
		case SerialPortEvent.PE:
		case SerialPortEvent.CD:
		case SerialPortEvent.CTS:
		case SerialPortEvent.DSR:
		case SerialPortEvent.RI:
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			break;
		case SerialPortEvent.DATA_AVAILABLE:
			try {
				while (this.in.available() > 0) {
					int numByte = this.in.read(bufferBytes);
				}

				for (int i = 1; i < retryTimes; i++) {
					CommUtil.sleep(delayTime);
					if ((new String(bufferBytes)).trim().length() == 144) {
						break;
					}
				}
				this.bufferData = new String(bufferBytes).trim();
				log.debug("retData==" + bufferData + "##");
				String year = DateUtils.getStringDateShort2().substring(0, 4);
				QRReader.getInstance().performDeviceCallback(bufferData, year);

			} catch (IOException ex) {
				log.error(ex);
			}
			break;
		}
	}

	public static void main(String[] args) {
		HoneyWellQRDevice gateDevice = HoneyWellQRDevice.getInstance();
		try {
			// gateDevice.connect(DeviceConfig.getInstance().getFirstGateCrtlPort());
			gateDevice.connect(DeviceConfig.getInstance().getHoneywellQRPort());
			// gateDevice.openFirstDoor();
			// CommUtil.sleep(1000);
			// gateDevice.LightEntryArrow();
			// CommUtil.sleep(1000);
			// gateDevice.LightEntryCross();
			// gateDevice.close();
			// System.exit(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
