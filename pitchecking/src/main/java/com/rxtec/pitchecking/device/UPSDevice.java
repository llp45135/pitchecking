package com.rxtec.pitchecking.device;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.DeviceEventListener;
import com.rxtec.pitchecking.mqtt.ClosePCEventSenderBroker;
import com.rxtec.pitchecking.utils.CommUtil;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/**
 * UPS控制
 * 
 * @author lenovo
 *
 */
public class UPSDevice implements SerialPortEventListener {
	private Log log = LogFactory.getLog("UPSDevice");
	private static UPSDevice _instance = new UPSDevice();;
	private SerialPort serialPort;
	private InputStream in;
	private OutputStream out;
	private byte[] buffer = new byte[1024];
	private StringBuffer readBuffer = new StringBuffer();
	private static int retryTimes = 4;
	private static int delayTime = 30;
	private static int delayCount = 1000;
	private boolean isClosePC = false;
	private boolean isCloseUPS = false;

	public static UPSDevice getInstance() {
		return _instance;
	}

	private UPSDevice() {
		try {
			this.connect(Config.getInstance().getUPSPort());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
	}

	public boolean isCloseUPS() {
		return isCloseUPS;
	}

	public void setCloseUPS(boolean isCloseUPS) {
		this.isCloseUPS = isCloseUPS;
	}

	public boolean isClosePC() {
		return isClosePC;
	}

	public void setClosePC(boolean isClosePC) {
		this.isClosePC = isClosePC;
	}

	/**
	 * 打开串口
	 * 
	 * @param portName
	 * @throws Exception
	 */
	private void connect(String portName) throws Exception {
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		if (portIdentifier.isCurrentlyOwned()) {
			System.err.println("Error: Port is currently in use");
		} else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

			if (commPort instanceof SerialPort) {
				serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(2400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
						SerialPort.PARITY_NONE);
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

	/**
	 * 
	 * @return
	 */
	public int queryUPSStatus() {
		try {
			String cmd = "51310D";
			log.debug("queryUPSStatus cmd==" + cmd);
			readBuffer.delete(0, readBuffer.length());
			byte[] cmdData = CommUtil.hexStringToBytes(cmd);
			for (int i = 0; i < cmdData.length; i++) {
				this.out.write(cmdData[i]);
			}
			this.out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 倒计时关机命令
	 * 
	 * @return
	 */
	public int closeUPS() {
		try {
			String cmd = "5330310D";
			log.debug("closeUPS cmd==" + cmd);
			readBuffer.delete(0, readBuffer.length());
			byte[] cmdData = CommUtil.hexStringToBytes(cmd);
			for (int i = 0; i < cmdData.length; i++) {
				this.out.write(cmdData[i]);
			}
			this.out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
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
					int b = this.in.read();
					String data = Integer.toHexString(b).toUpperCase();
					if (data.length() < 2) {
						data = "0" + data;
					}
					readBuffer.append(data);
				}

				for (int i = 0; i < retryTimes; i++) {
					CommUtil.sleep(delayTime);
					if (readBuffer.length() == 94) {
						// log.debug("#########");
						break;
					}
				}

				String ss = readBuffer.toString();
				// log.debug("retData==" + ss + ",retData.length==" +
				// ss.length());

				if (ss.length() >= 94) {
					log.debug("query cmd received all");
					if (ss.startsWith("283030") && !isClosePC) {
						log.debug("工控机空开被打下!");
						Runtime.getRuntime()
								.exec("shutdown -s -t " + String.valueOf(Config.getInstance().getClosePCDelay()));
						this.isClosePC = true;
						log.debug("已发出" + Config.getInstance().getClosePCDelay() + "秒后关机命令!");
						// 给同组的其他闸机发送关闭PC消息命令
						if (Config.getInstance().getIsSendClosePCCmd() == 1) {
							log.debug("已经向" + Config.getInstance().getClosePCCmdUrl() + "发出关机命令!");
							ClosePCEventSenderBroker.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT)
									.sendDoorCmd(DeviceConfig.Event_ClosePC);
						}
					}

					readBuffer.delete(0, readBuffer.length());
				}

			} catch (IOException ex) {
				log.error(ex);
			}
			break;
		}
	}

	public static void main(String[] args) {
		UPSDevice upsDevice = UPSDevice.getInstance();
		try {
			while (true) {

				upsDevice.queryUPSStatus();
				if (upsDevice.isClosePC() && !upsDevice.isCloseUPS) {
					upsDevice.closeUPS();
					upsDevice.setCloseUPS(true);
				}
				CommUtil.sleep(10 * 1000);

			}

			// gateDevice.close();
			// System.exit(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
