package com.rxtec.pitchecking.device;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
public class GateDeviceManager implements SerialPortEventListener {
	private Log log = LogFactory.getLog("GateDeviceManager");
	private static GateDeviceManager instance;
	private SerialPort serialPort;
	private InputStream in;
	private OutputStream out;
	private byte[] buffer = new byte[1024];
	private StringBuffer readBuffer = new StringBuffer();
	private static int delayTime = 20;
	private static int delayCount = 2000;
	private boolean status = false;
	private String sendCmdStr;
	private String firstDoorCloseStr = "";
	private String secondDoorCloseStr = "";
	private String thirdDoorCloseStr = "";

	public String getThirdDoorCloseStr() {
		return thirdDoorCloseStr;
	}

	public void setThirdDoorCloseStr(String thirdDoorCloseStr) {
		this.thirdDoorCloseStr = thirdDoorCloseStr;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getSendCmdStr() {
		return sendCmdStr;
	}

	public void setSendCmdStr(String sendCmdStr) {
		this.sendCmdStr = sendCmdStr;
	}

	public String getFirstDoorCloseStr() {
		return firstDoorCloseStr;
	}

	public void setFirstDoorCloseStr(String firstDoorCloseStr) {
		this.firstDoorCloseStr = firstDoorCloseStr;
	}

	public String getSecondDoorCloseStr() {
		return secondDoorCloseStr;
	}

	public void setSecondDoorCloseStr(String secondDoorCloseStr) {
		this.secondDoorCloseStr = secondDoorCloseStr;
	}

	public static synchronized GateDeviceManager getInstance() {
		if (instance == null) {
			instance = new GateDeviceManager();
		}
		return instance;
	}

	/**
	 * 开第一道门
	 * 
	 * @return
	 */
	public int openFirstDoor() {
		this.setStatus(false);
		this.setFirstDoorCloseStr("");
		try {
			String cmd = "1002";
			String xh = "00";
			cmd += xh;
			cmd += "0101" + "1003";
			cmd += CommUtil.tail(xh, "01", "01");
			log.debug("openFirstDoor cmd==" + cmd);
			byte[] cmdData = CommUtil.hexStringToBytes(cmd);
			for (int i = 0; i < cmdData.length; i++) {
				this.out.write(cmdData[i]);
			}
			this.out.flush();
			for (int i = 1; i < delayCount; i++) {
				CommUtil.sleep(delayTime);
				if (this.isStatus()) {
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	// 电磁开关侧门
	public int openSecondDoor() {
		this.setStatus(false);
		this.setSecondDoorCloseStr("");
		try {
			String cmd = "1002";
			String xh = "00";
			cmd += xh;
			cmd += "0101" + "1003";
			cmd += CommUtil.tail(xh, "01", "01");
			log.debug("openSecondDoor cmd==" + cmd);
			byte[] cmdData = CommUtil.hexStringToBytes(cmd);
			for (int i = 0; i < cmdData.length; i++) {
				this.out.write(cmdData[i]);
			}
			this.out.flush();
			for (int i = 1; i < delayCount; i++) {
				CommUtil.sleep(delayTime);
				if (this.isStatus()) {
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 开第三道门
	 * @return
	 */
	public int openThirdDoor() {
		this.setStatus(false);
		this.setThirdDoorCloseStr("");
		try {
			String cmd = "1002";
			String xh = "00";
			cmd += xh;
			cmd += "0101" + "1003";
			cmd += CommUtil.tail(xh, "01", "01");
			log.debug("openThirdDoor cmd==" + cmd);
			byte[] cmdData = CommUtil.hexStringToBytes(cmd);
			for (int i = 0; i < cmdData.length; i++) {
				this.out.write(cmdData[i]);
			}
			this.out.flush();
			for (int i = 1; i < delayCount; i++) {
				CommUtil.sleep(delayTime);
				if (this.isStatus()) {
					break;
				}
			}
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

                String ss = readBuffer.toString();

                log.debug("retData==" + ss);
                this.setSendCmdStr(ss);
                // if (ss.startsWith("303141")) {
				// System.out.println("01站台正在上水");
				// this.setWaterMsg(ss);
				// this.getJframe().setLibel2("");
				// this.getJframe().setLibel1("1站台正在上水!");
				// this.getJframe().setLibel1Color(Color.RED);
				// }
				// if (ss.startsWith("303142")) {
				// System.out.println("01站台结束上水");
				// this.setWaterMsg(ss);
				// String tt = ss.substring(7, 8) + ss.substring(9, 10) +
				// ss.substring(11, 12) + ss.substring(13, 14);
				// int tts = Integer.parseInt(tt) * 16;
				// BigDecimal bd = new BigDecimal(tts * 0.1);
				// StringBuilder dds = new StringBuilder();
				// dds.append("<html>");
				// dds.append("1站台上水作业结束!");
				// dds.append("<br>");
				// dds.append("作业持续时间" + tts + "s");
				// dds.append("<br>");
				// dds.append("上水量" + (bd.setScale(2, BigDecimal.ROUND_HALF_UP))
				// + "升");
				// dds.append("</html>");
				// this.getJframe().setLibel1(dds.toString());
				// this.getJframe().setLibel1Color(Color.BLACK);
				// }
				// if (ss.startsWith("303241")) {
				// System.out.println("02站台正在上水");
				// this.setWaterMsg(ss);
				// this.getJframe().setLibel1("");
				// this.getJframe().setLibel2("2站台正在上水!");
				// this.getJframe().setLibel2Color(Color.RED);
				// }
				// if (ss.startsWith("303242")) {
				// System.out.println("02站台结束上水");
				// this.setWaterMsg(ss);
				// String tt = ss.substring(7, 8) + ss.substring(9, 10) +
				// ss.substring(11, 12) + ss.substring(13, 14);
				// int tts = Integer.parseInt(tt) * 16;
				// BigDecimal bd = new BigDecimal(tts * 0.1);
				// StringBuilder dds = new StringBuilder();
				// dds.append("<html>");
				// dds.append("2站台上水作业结束!");
				// dds.append("<br>");
				// dds.append("作业持续时间" + tts + "s");
				// dds.append("<br>");
				// dds.append("上水量" + (bd.setScale(2, BigDecimal.ROUND_HALF_UP))
				// + "升");
				// dds.append("</html>");
				// this.getJframe().setLibel2(dds.toString());
				// this.getJframe().setLibel2Color(Color.BLACK);
				// }
			} catch (IOException ex) {
				log.error(ex);
			}
			break;
		}
	}

	/**
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
				serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
						SerialPort.PARITY_NONE);
				serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

				this.in = serialPort.getInputStream();
				this.out = serialPort.getOutputStream();

				serialPort.addEventListener(this);
				serialPort.notifyOnDataAvailable(true);
				serialPort.notifyOnBreakInterrupt(true);
				// (new Thread(new SerialWriter(out))).start();

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

}
