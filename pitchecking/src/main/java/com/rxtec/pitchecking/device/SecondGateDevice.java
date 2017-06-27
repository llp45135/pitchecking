package com.rxtec.pitchecking.device;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.DeviceEventListener;
import com.rxtec.pitchecking.EmerButtonTask;
import com.rxtec.pitchecking.ScreenCmdEnum;
import com.rxtec.pitchecking.TicketCheckScreen;
import com.rxtec.pitchecking.TicketVerifyScreen;
import com.rxtec.pitchecking.device.smartmonitor.MonitorXMLUtil;
import com.rxtec.pitchecking.domain.EmerButtonEvent;
import com.rxtec.pitchecking.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.mqtt.GatCtrlSenderBroker;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.CalUtils;

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
public class SecondGateDevice implements SerialPortEventListener {
	private Log log = LogFactory.getLog("DeviceEventListener");
	private static SecondGateDevice instance;
	private SerialPort serialPort;
	private InputStream in;
	private OutputStream out;
	private byte[] buffer = new byte[1024];
	private StringBuffer readBuffer = new StringBuffer();
	private static int retryTimes = 20;
	private static int delayTime = 5;
	private static int delayCount = 1000;

	private String errcode = "00";

	public String getErrcode() {
		return errcode;
	}

	public void setErrcode(String errcode) {
		this.errcode = errcode;
	}

	public static synchronized SecondGateDevice getInstance() {
		if (instance == null) {
			instance = new SecondGateDevice();
		}
		return instance;
	}

	private SecondGateDevice() {
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
				serialPort.setSerialPortParams(DeviceConfig.getInstance().getGateCrtlRate(), SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

				this.in = serialPort.getInputStream();
				this.out = serialPort.getOutputStream();

				serialPort.addEventListener(this);
				serialPort.notifyOnDataAvailable(true);
				serialPort.notifyOnBreakInterrupt(true);
				log.info("串口已经打开:" + portName);

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
	 * 闸门自检
	 * 
	 * @return
	 */
	public int selfCheck() {
		try {
			String cmd = "2A600023";
			log.info("selfCheck cmd==" + cmd);
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
	 * 电磁开关侧门
	 * 
	 * @return
	 */
	public int openEmerDoor() {
		try {
			String cmd = "2A500123";
			log.info("openEmerDoor cmd==" + cmd);
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
	 * 开第二道门
	 * 
	 * @return
	 */
	public int openTheSecondDoor() {
		try {
			String cmd = "2A040123";
			log.info("openTheSecondDoor cmd==" + cmd);
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
	 * 主机---->闸机 (入口箭头指示灯为 箭头)
	 * 
	 * @return
	 */
	public int LightEntryArrow() {
		try {
			String cmd = "2A510123";
			log.info("LightEntryArrow cmd==" + cmd);
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
	 * 主机---->闸机 (入口箭头指示灯为 红色‘X’)
	 * 
	 * @return
	 */
	public int LightEntryCross() {
		try {
			String cmd = "2A510023";
			log.info("LightEntryCross cmd==" + cmd);
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
	 * 主机---->闸机 (出口箭头指示灯为 箭头)
	 * 
	 * @return
	 */
	public int LightExitArrow() {
		try {
			String cmd = "2A520123";
			log.info("LightExitArrow cmd==" + cmd);
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
	 * 主机---->闸机 (出口箭头指示灯为红色‘X’)
	 * 
	 * @return
	 */
	public int LightExitCross() {
		try {
			String cmd = "2A520023";
			log.info("LightExitCross cmd==" + cmd);
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
	 * 主机---->闸机 (玻璃门提示指示灯收到指令后红绿闪烁)
	 */
	public int LightGlassDoorLED() {
		try {
			String cmd = "2A530123";
			log.info("LightGlassDoorLED cmd==" + cmd);
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
	 * 主机---->闸机 (顶盖指示灯变绿色)
	 */
	public int LightGreenLED() {
		try {
			String cmd = "2A540123";
			log.info("LightGreenLED cmd==" + cmd);
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
	 * 主机---->闸机 (顶盖指示灯变为红色)
	 */
	public int LightRedLED() {
		try {
			String cmd = "2A540023";
			log.info("LightRedLED cmd==" + cmd);
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
	 * 获取通道状态函数，此函数功能是检查通道内是否有人闯闸的 第一道门检测有没没刷卡进入通道的反馈 主机---->闸机
	 * (控制板收到此命令时查一下光电感应器的状态。) 2a 58 01 23 主机收到指令后返回 闸机--->主机 2a 58 xx 23 其中XX
	 * 0x00代表正常，没有闯入，0x01代表异常，有未刷卡闯入
	 */
	public int getGateStatus() {
		try {
			String cmd = "2A580123";
			log.info("getGateStatus cmd==" + cmd);
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

				String ss = "";
				for (int i = 1; i < retryTimes; i++) {
					CommUtil.sleep(delayTime);
					ss = readBuffer.toString();
					if (ss.length() >= 8) {
						break;
					}
				}

				log.info("Second Gate retData==" + ss);

				readBuffer.delete(0, ss.length());

				if (ss.length() >= 8) {
					if (ss.indexOf("2A040123") != -1) {
						DeviceEventListener.getInstance().setReceiveOpenSecondDoorCmd(true);
						log.info("控制板已收到开二门指令");

						if (Config.getInstance().getRebackReadCardMode() == 3) { // 旧板
							DeviceConfig.getInstance().setAllowOpenSecondDoor(false);
							DeviceConfig.getInstance().setSecondGateOpenCount(1);
							DeviceConfig.getInstance().setSecondGateOpened(true); // 第2门打开

							log.info("isFirstGateClosed==" + DeviceConfig.getInstance().isFirstGateClosed());
							log.info("isSecondGateOpened==" + DeviceConfig.getInstance().isSecondGateOpened());
							log.info("getSecondGateOpenCount==" + DeviceConfig.getInstance().getSecondGateOpenCount());

							if (DeviceEventListener.getInstance().isStartThread()) {
								if (DeviceConfig.getInstance().isFirstGateClosed() && DeviceConfig.getInstance().isSecondGateOpened()) {
									if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR || Config.getInstance().getIsOutGateForCSQ() == 1) {
										TicketVerifyScreen.getInstance()
												.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketDefault.getValue(), null, null, null)); // 恢复初始界面
									}
									DeviceEventListener.getInstance().setDeviceReader(true); // 允许寻卡
									DeviceEventListener.getInstance().setDealDeviceEvent(true); // 允许处理新的事件
									log.info("人证比对完成，第1门已关闭、第2门已打开，可以重新寻卡");
								}
							}
						} else if (Config.getInstance().getRebackReadCardMode() == 4) { // 旧板
							DeviceConfig.getInstance().setAllowOpenSecondDoor(false);
							DeviceConfig.getInstance().setSecondGateOpenCount(1);
							DeviceConfig.getInstance().setSecondGateOpened(true); // 第2门打开

							log.info("isFirstGateClosed==" + DeviceConfig.getInstance().isFirstGateClosed());
							log.info("isSecondGateOpened==" + DeviceConfig.getInstance().isSecondGateOpened());
							log.info("getSecondGateOpenCount==" + DeviceConfig.getInstance().getSecondGateOpenCount());
						}
					}
					if (ss.indexOf("2A040023") != -1) {
						if (Config.getInstance().getRebackReadCardMode() == 3) { // 旧板
							log.info("第二道门正常关闭");
							DeviceConfig.getInstance().setSecondGateOpenCount(2);
							DeviceConfig.getInstance().setSecondGateOpened(false); // 2门关闭
							log.info("isFirstGateClosed==" + DeviceConfig.getInstance().isFirstGateClosed());
							log.info("isSecondGateOpened==" + DeviceConfig.getInstance().isSecondGateOpened());
							log.info("getSecondGateOpenCount==" + DeviceConfig.getInstance().getSecondGateOpenCount());

							if (!DeviceConfig.getInstance().isAllowOpenSecondDoor()) {
								GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd("PITEventTopic",
										DeviceConfig.Event_SecondDoor_NormalClosed);
								DeviceConfig.getInstance().setAllowOpenSecondDoor(true);
							}
						} else if (Config.getInstance().getRebackReadCardMode() == 4) { // 旧板
							if (DeviceEventListener.getInstance().isStartThread()) {
								if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR || Config.getInstance().getIsOutGateForCSQ() == 1) {
									TicketVerifyScreen.getInstance()
											.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketDefault.getValue(), null, null, null)); // 恢复初始界面
								}
								DeviceEventListener.getInstance().setDeviceReader(true);
								// 允许寻卡
								DeviceEventListener.getInstance().setDealDeviceEvent(true);
								// 允许处理新的事件
								if (!DeviceConfig.getInstance().isAllowOpenSecondDoor()) {
									GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd("PITEventTopic",
											DeviceConfig.Event_SecondDoor_NormalClosed);
									DeviceConfig.getInstance().setAllowOpenSecondDoor(true);
								}

								log.debug("人证比对完成，第二道闸门已经关闭，重新寻卡");
							}
						} else { // 新板
							log.info("第二道门已经打开");
							DeviceConfig.getInstance().setAllowOpenSecondDoor(false);
							DeviceConfig.getInstance().setSecondGateOpenCount(1);
							DeviceConfig.getInstance().setSecondGateOpened(true); // 第2门打开

							log.info("isFirstGateClosed==" + DeviceConfig.getInstance().isFirstGateClosed());
							log.info("isSecondGateOpened==" + DeviceConfig.getInstance().isSecondGateOpened());
							log.info("getSecondGateOpenCount==" + DeviceConfig.getInstance().getSecondGateOpenCount());
							/**
							 * 通行逻辑改为 1门关同时2门已开,随即返回重新寻卡
							 */
							if (Config.getInstance().getRebackReadCardMode() == 1) {
								if (DeviceEventListener.getInstance().isStartThread()) {
									if (DeviceConfig.getInstance().isFirstGateClosed() && DeviceConfig.getInstance().isSecondGateOpened()) {
										if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR || Config.getInstance().getIsOutGateForCSQ() == 1) {
											TicketVerifyScreen.getInstance()
													.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketDefault.getValue(), null, null, null)); // 恢复初始界面
										}
										DeviceEventListener.getInstance().setDeviceReader(true); // 允许寻卡
										DeviceEventListener.getInstance().setDealDeviceEvent(true); // 允许处理新的事件
										log.info("人证比对完成，第1门已关闭、第2门已打开，可以重新寻卡");
									}
								}
							}
						}
					}
					if (ss.indexOf("2A040223") != -1) {
						log.info("第二道门正常关闭");
						DeviceConfig.getInstance().setSecondGateOpenCount(2);
						DeviceConfig.getInstance().setSecondGateOpened(false); // 2门关闭
						log.info("isFirstGateClosed==" + DeviceConfig.getInstance().isFirstGateClosed());
						log.info("isSecondGateOpened==" + DeviceConfig.getInstance().isSecondGateOpened());
						log.info("getSecondGateOpenCount==" + DeviceConfig.getInstance().getSecondGateOpenCount());

						if (Config.getInstance().getRebackReadCardMode() == 1) {
							if (!DeviceConfig.getInstance().isAllowOpenSecondDoor()) {
								GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd("PITEventTopic",
										DeviceConfig.Event_SecondDoor_NormalClosed);
								DeviceConfig.getInstance().setAllowOpenSecondDoor(true);
							}
						}

						/**
						 * 
						 */
						if (Config.getInstance().getRebackReadCardMode() == 2) {
							if (DeviceEventListener.getInstance().isStartThread()) {
								if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {
									TicketVerifyScreen.getInstance()
											.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketDefault.getValue(), null, null, null)); // 恢复初始界面
								}
								if (Config.getInstance().getIsOutGateForCSQ()==1) {
									TicketVerifyScreen.getInstance()
											.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketDefaultCSQ.getValue(), null, null, null)); // 恢复初始界面
								}
								DeviceEventListener.getInstance().setDeviceReader(true); // 允许寻卡
								DeviceEventListener.getInstance().setDealDeviceEvent(true); // 允许处理新的事件
								if (!DeviceConfig.getInstance().isAllowOpenSecondDoor()) {
									GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd("PITEventTopic",
											DeviceConfig.Event_SecondDoor_NormalClosed);
									DeviceConfig.getInstance().setAllowOpenSecondDoor(true);
								}

								log.info("人证比对完成，第二道闸门已经关闭，重新寻卡");
							}
						}
					}
					if (ss.indexOf("2A040F23") != -1) {
						log.info("第二道门超时关闭,getRebackReadCardMode = " + Config.getInstance().getRebackReadCardMode());
						DeviceConfig.getInstance().setSecondGateOpenCount(3);
						DeviceConfig.getInstance().setSecondGateOpened(false); // 2门关闭

						if (Config.getInstance().getRebackReadCardMode() == 1) {
							if (DeviceEventListener.getInstance().isStartThread()) {
								if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {
									TicketVerifyScreen.getInstance()
											.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketDefault.getValue(), null, null, null)); // 恢复初始界面
								}
								if (Config.getInstance().getIsOutGateForCSQ()==1) {
									TicketVerifyScreen.getInstance()
											.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketDefaultCSQ.getValue(), null, null, null)); // 恢复初始界面
								}
								DeviceEventListener.getInstance().setDeviceReader(true); // 允许寻卡
								DeviceEventListener.getInstance().setDealDeviceEvent(true); // 允许处理新的事件

								if (!DeviceConfig.getInstance().isAllowOpenSecondDoor()) {
									GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd("PITEventTopic",
											DeviceConfig.Event_SecondDoor_OvertimeClosed);
									DeviceConfig.getInstance().setAllowOpenSecondDoor(true);
								}
								log.info("人证比对完成，第二道闸门超时关闭，重新寻卡");
							}
						}

						/**
						 * 
						 */
						if (Config.getInstance().getRebackReadCardMode() == 2) {
							if (DeviceEventListener.getInstance().isStartThread()) {
								// log.debug("第二道闸门超时关闭");
								if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {
									TicketVerifyScreen.getInstance()
											.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketDefault.getValue(), null, null, null)); // 恢复初始界面
								}
								if (Config.getInstance().getIsOutGateForCSQ()==1) {
									TicketVerifyScreen.getInstance()
											.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketDefaultCSQ.getValue(), null, null, null)); // 恢复初始界面
								}
								DeviceEventListener.getInstance().setDeviceReader(true); // 允许寻卡
								DeviceEventListener.getInstance().setDealDeviceEvent(true); // 允许处理新的事件

								if (!DeviceConfig.getInstance().isAllowOpenSecondDoor()) {
									GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd("PITEventTopic",
											DeviceConfig.Event_SecondDoor_OvertimeClosed);
									DeviceConfig.getInstance().setAllowOpenSecondDoor(true);
								}
								log.info("人证比对完成，第二道闸门超时关闭，重新寻卡");
							}
						}

						if (Config.getInstance().getRebackReadCardMode() == 3) { // 旧板
							if (!DeviceConfig.getInstance().isAllowOpenSecondDoor()) {
								GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd("PITEventTopic",
										DeviceConfig.Event_SecondDoor_OvertimeClosed);
								DeviceConfig.getInstance().setAllowOpenSecondDoor(true);
							}
						}

						if (Config.getInstance().getRebackReadCardMode() == 4) { // 旧板，后门关才寻卡
							if (DeviceEventListener.getInstance().isStartThread()) {
								// log.debug("第二道闸门超时关闭");
								if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR || Config.getInstance().getIsOutGateForCSQ() == 1) {
									TicketVerifyScreen.getInstance()
											.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketDefault.getValue(), null, null, null)); // 恢复初始界面
								}
								DeviceEventListener.getInstance().setDeviceReader(true); // 允许寻卡
								DeviceEventListener.getInstance().setDealDeviceEvent(true); // 允许处理新的事件

								if (!DeviceConfig.getInstance().isAllowOpenSecondDoor()) {
									GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd("PITEventTopic",
											DeviceConfig.Event_SecondDoor_OvertimeClosed);
									DeviceConfig.getInstance().setAllowOpenSecondDoor(true);
								}
								log.debug("人证比对完成，第二道闸门超时关闭，重新寻卡");
							}
						}
					}
					if (ss.indexOf("2A510123") != -1) {
						log.info("点亮入口绿色箭头");
					}
					if (ss.indexOf("2A510023") != -1) {
						log.info("点亮入口红色叉叉");
					}
					if (ss.indexOf("2A550123") != -1) {
						if (DeviceEventListener.getInstance().isStartThread()) {
							log.info("开侧门按钮被按下");
							// EmerButtonEvent embEvent = new EmerButtonEvent();
							// embEvent.setEventNo("01");
							// embEvent.setEventTime(CalUtils.getStringDateHaomiao());
							// EmerButtonTask.getInstance().offerEmerButtonEvent(embEvent);

						}
					}
					if (ss.indexOf("2A720123") != -1) {
						if (DeviceEventListener.getInstance().isStartThread()) {
							log.info("开后门按钮被按下");
							// EmerButtonEvent embEvent = new EmerButtonEvent();
							// embEvent.setEventNo("02");
							// embEvent.setEventTime(CalUtils.getStringDateHaomiao());
							// EmerButtonTask.getInstance().offerEmerButtonEvent(embEvent);

							GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd("PITEventTopic", DeviceConfig.Event_PressOpenSecondDoorButton);

							if (DeviceEventListener.getInstance().isStartThread()) {

								TicketVerifyScreen.getInstance()
										.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketDefault.getValue(), null, null, null)); // 恢复初始界面
								DeviceEventListener.getInstance().setDeviceReader(true);
								// 允许寻卡
								DeviceEventListener.getInstance().setDealDeviceEvent(true);
								// 允许处理新的事件

								// if
								// (!DeviceConfig.getInstance().isAllowOpenSecondDoor())
								// {
								// GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT)
								// .sendDoorCmd("PITEventTopic",
								// DeviceConfig.Close_SECONDDOOR_Jms);
								// DeviceConfig.getInstance().setAllowOpenSecondDoor(true);
								// }
								log.debug("开后门按钮被按下，重新寻卡");
							}
						}
					}
					if (ss.indexOf("2A750023") != -1) { // 中央区域两人尾随
						log.info("中间区域全部无遮挡:" + ss);
						DeviceEventListener.getInstance().setTrailingStatus(0);
					}
					if (ss.indexOf("2A750123") != -1) { // 中央区域两人尾随
						log.info("中央区域两人尾随:" + ss);
						DeviceEventListener.getInstance().setTrailingStatus(1);
						GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd("PITEventTopic", DeviceConfig.Event_Trailling);
					}
					if (ss.indexOf("2A750223") != -1) { // 中间区域四组对射全部遮挡
						log.info("中间区域四组对射全部遮挡:" + ss);
						DeviceEventListener.getInstance().setTrailingStatus(2);
						GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd("PITEventTopic", DeviceConfig.Event_Trailling);
					}
					if (ss.indexOf("2A750323") != -1) { // 第一个人在后门正通过、第二个人已经走到中间位置
						log.info("第一个人在后门正通过、第二个人已经走到中间位置:" + ss);
						DeviceEventListener.getInstance().setTrailingStatus(3);
					}
					if (ss.indexOf("2A750423") != -1) { // 第一个人在后门正通过、第二个人已经走到中间位置
						log.info("有1个人在通道里:" + ss);
						DeviceEventListener.getInstance().setTrailingStatus(4);
					}
					if (ss.indexOf("2A750523") != -1) { // 挡住了3、4、5
						log.info("挡住了3、4、5对射:" + ss);
						DeviceEventListener.getInstance().setTrailingStatus(5);
						GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd("PITEventTopic", DeviceConfig.Event_Trailling);
					}
					if (ss.indexOf("2A910123") != -1) {
						log.debug("UPS已被断电!");
						if (Config.getInstance().getShutdownPCMode() == 3) {
							Runtime.getRuntime().exec("shutdown -s -t " + String.valueOf(Config.getInstance().getClosePCDelay()));
							log.debug("已发出" + Config.getInstance().getClosePCDelay() + "秒后关机命令!");
						}
						if (Config.getInstance().getShutdownPCMode() == 2) {
							Runtime.getRuntime().exec("shutdown -r -t " + String.valueOf(Config.getInstance().getClosePCDelay()));
							log.debug("已发出" + Config.getInstance().getClosePCDelay() + "秒后重启命令!");
						}
						if (Config.getInstance().getShutdownPCMode() == 1) {
							Runtime.getRuntime().exec("shutdown -l");
							log.debug("已发出注销命令!");
						}
					}
					// if (ss.indexOf("2A930123") != -1) { // 主机电机通信失败
					// MonitorXMLUtil.updateBaseInfoFonMonitor(Config.getInstance().getBaseInfoXMLPath(),
					// "001", 1);
					// MonitorXMLUtil.updateDoorStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(),
					// "001", "001", "001", "006");
					// MonitorXMLUtil.updateEntirStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(),
					// 1);
					// }
					// if (ss.indexOf("2A930023") != -1) { // 主机电机通信恢复
					// MonitorXMLUtil.updateBaseInfoFonMonitor(Config.getInstance().getBaseInfoXMLPath(),
					// "001", 0);
					// MonitorXMLUtil.updateDoorStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(),
					// "001", "001", "001", "000");
					// MonitorXMLUtil.updateEntirStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(),
					// 0);
					// }
					// if (ss.indexOf("2A940123") != -1) { // 副机电机通信失败
					// MonitorXMLUtil.updateBaseInfoFonMonitor(Config.getInstance().getBaseInfoXMLPath(),
					// "001", 1);
					// MonitorXMLUtil.updateDoorStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(),
					// "001", "001", "001", "006");
					// MonitorXMLUtil.updateEntirStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(),
					// 1);
					// }
					// if (ss.indexOf("2A940023") != -1) { // 副机电机通信恢复
					// MonitorXMLUtil.updateBaseInfoFonMonitor(Config.getInstance().getBaseInfoXMLPath(),
					// "001", 0);
					// MonitorXMLUtil.updateDoorStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(),
					// "001", "001", "001", "000");
					// MonitorXMLUtil.updateEntirStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(),
					// 0);
					// }
					// if (ss.indexOf("2A960123") != -1) { // 副机电机通信失败
					// MonitorXMLUtil.updateBaseInfoFonMonitor(Config.getInstance().getBaseInfoXMLPath(),
					// "001", 1);
					// MonitorXMLUtil.updateDoorStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(),
					// "001", "001", "001", "006");
					// MonitorXMLUtil.updateEntirStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(),
					// 1);
					// }
					// if (ss.indexOf("2A960023") != -1) { // 副机电机通信恢复
					// MonitorXMLUtil.updateBaseInfoFonMonitor(Config.getInstance().getBaseInfoXMLPath(),
					// "001", 0);
					// MonitorXMLUtil.updateDoorStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(),
					// "001", "001", "001", "000");
					// MonitorXMLUtil.updateEntirStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(),
					// 0);
					// }
					// if (ss.indexOf("2A970123") != -1) { // 副机电机通信失败
					// MonitorXMLUtil.updateBaseInfoFonMonitor(Config.getInstance().getBaseInfoXMLPath(),
					// "001", 1);
					// MonitorXMLUtil.updateDoorStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(),
					// "001", "001", "001", "006");
					// MonitorXMLUtil.updateEntirStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(),
					// 1);
					// }
					// if (ss.indexOf("2A970023") != -1) { // 副机电机通信恢复
					// MonitorXMLUtil.updateBaseInfoFonMonitor(Config.getInstance().getBaseInfoXMLPath(),
					// "001", 0);
					// MonitorXMLUtil.updateDoorStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(),
					// "001", "001", "001", "000");
					// MonitorXMLUtil.updateEntirStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(),
					// 0);
					// }
					if (ss.indexOf("2A600123") != -1) {
						log.info("通道当前状态为：" + ss);
						this.errcode = "01";
					}
					if (ss.indexOf("2A600223") != -1) {
						log.info("通道当前状态为：" + ss);
						this.errcode = "02";
					}
					if (ss.indexOf("2A600323") != -1) {
						log.info("通道当前状态为：" + ss);
						this.errcode = "03";
					}
					if (ss.indexOf("2A600423") != -1) {
						log.info("通道当前状态为：" + ss);
						this.errcode = "04";
					}
					if (ss.indexOf("2A600523") != -1) {
						log.info("通道当前状态为：" + ss);
						this.errcode = "05";
					}
					if (ss.indexOf("2A600623") != -1) {
						log.info("通道当前状态为：" + ss);
						this.errcode = "06";
					}

				}

			} catch (IOException ex) {
				log.error(ex);
			}
			break;
		}
	}

	public static void main(String[] args) {
		SecondGateDevice gateDevice = SecondGateDevice.getInstance();
		try {
			gateDevice.connect(DeviceConfig.getInstance().getSecondGateCrtlPort());
			gateDevice.openEmerDoor();
			CommUtil.sleep(1000);
			gateDevice.openTheSecondDoor();
			// gateDevice.close();
			// System.exit(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
