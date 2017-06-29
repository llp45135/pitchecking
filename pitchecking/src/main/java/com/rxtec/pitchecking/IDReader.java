package com.rxtec.pitchecking;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.device.HXGCDevice;
import com.rxtec.pitchecking.device.RSIVIDcardDevice;
import com.rxtec.pitchecking.device.TKIDCDevice;
import com.rxtec.pitchecking.device.XZXCDevice;
import com.rxtec.pitchecking.device.smartmonitor.MonitorXMLUtil;
import com.rxtec.pitchecking.event.IDCardReaderEvent;
import com.rxtec.pitchecking.event.IDeviceEvent;
import com.rxtec.pitchecking.gui.TicketCheckFrame;
import com.rxtec.pitchecking.gui.ticketgui.TicketVerifyFrame;
import com.rxtec.pitchecking.utils.CommUtil;

public class IDReader implements Runnable {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");
	private static IDReader instance;
	private XZXCDevice xzxcDevice = null;
	private HXGCDevice hxgcDevice = null;
	private TKIDCDevice tkIDCDevice = null;
	private RSIVIDcardDevice rsivIDCDevice = null;
	private int deviceStatus = Config.StartStatus;
	// 以下ticketFrame仅供测试用
	// private TicketCheckFrame ticketFrame;
	private TicketVerifyFrame ticketFrame;

	public TicketVerifyFrame getTicketFrame() {
		return ticketFrame;
	}

	public void setTicketFrame(TicketVerifyFrame ticketFrame) {
		this.ticketFrame = ticketFrame;
	}

	private int lastStatusCode = -1;
	private int disconnectCount = 0;

	public static synchronized IDReader getInstance() {
		if (instance == null) {
			instance = new IDReader();
		}
		return instance;
	}

	private IDReader() {
		String openVal = "-1";
		if (DeviceConfig.getInstance().getIdDeviceType().equals("X")) {
			xzxcDevice = XZXCDevice.getInstance();
			// openVal = xzxcDevice.Syn_OpenPort();
		} else if (DeviceConfig.getInstance().getIdDeviceType().equals("H")) {
			hxgcDevice = HXGCDevice.getInstance();
			// openVal = hxgcDevice.Syn_OpenPort();
		} else if (DeviceConfig.getInstance().getIdDeviceType().equals("R")) {
			rsivIDCDevice = RSIVIDcardDevice.getInstance();
		} else {
			tkIDCDevice = TKIDCDevice.getInstance();
			openVal = tkIDCDevice.IDC_Init();
		}

		if (DeviceConfig.getInstance().getIdDeviceType().equals("R")) {
			int fetchCode = rsivIDCDevice.IDC_FetchCard(3000);
			this.lastStatusCode = fetchCode;
			if (fetchCode == 36881) {
				DeviceConfig.getInstance().setIdDeviceStatus(0);
				log.info("二代证读卡器" + DeviceConfig.getInstance().getIdDeviceType() + "SIVIDC 初始化失败!");
			} else {
				DeviceConfig.getInstance().setIdDeviceStatus(DeviceConfig.idDeviceSucc);
				log.info("二代证读卡器" + DeviceConfig.getInstance().getIdDeviceType() + "SIVIDC 初始化成功!");
			}
		} else {
			if (openVal.equals("0")) {
				DeviceConfig.getInstance().setIdDeviceStatus(DeviceConfig.idDeviceSucc);
				log.info("二代证读卡器" + DeviceConfig.getInstance().getIdDeviceType() + "KIDC 初始化成功!");
			} else {
				DeviceConfig.getInstance().setIdDeviceStatus(Integer.parseInt(openVal));
				log.info("二代证读卡器" + DeviceConfig.getInstance().getIdDeviceType() + "KIDC 初始化失败!");
			}
		}
	}

	@Override
	public void run() {
		readCard();
	}

	/*
	 * 读二代证数据,填充event 读不到数据返回null
	 */
	private void readCard() {
		if (deviceStatus == Config.StartStatus) {
			if (DeviceConfig.getInstance().getIdDeviceType().equals("X")) {
				xzxcDevice.Syn_OpenPort();
				this.readUseXZXCDevice(xzxcDevice);
				xzxcDevice.Syn_ClosePort();
			} else if (DeviceConfig.getInstance().getIdDeviceType().equals("H")) {
				hxgcDevice.Syn_OpenPort();
				this.readUseHXGCDevice(hxgcDevice);
				hxgcDevice.Syn_ClosePort();
			} else if (DeviceConfig.getInstance().getIdDeviceType().equals("R")) {
				this.readTkIDCDevice(rsivIDCDevice);
			} else {
				this.readTkIDCDevice(tkIDCDevice);
			}
		}
	}

	/**
	 * 新中新读卡
	 * 
	 * @param idDeviceType
	 * @param device
	 */
	private void readUseXZXCDevice(XZXCDevice device) {
		String findval = device.Syn_StartFindIDCard();
		if (findval.equals("0")) {
			String selectval = device.Syn_SelectIDCard();
			if (selectval.equals("0")) {
				IDCard idCard = device.Syn_ReadBaseMsg();
				if (idCard != null && idCard.getIdNo() != null && idCard.getCardImage() != null && idCard.getCardImageBytes() != null) {
					if (DeviceConfig.getInstance().getVersionFlag() == 1) {// 以下为正式代码
						// if
						// (DeviceEventListener.getInstance().isDealDeviceEvent())
						// {
						IDCardReaderEvent readCardEvent = new IDCardReaderEvent();
						readCardEvent.setIdCard(idCard);
						DeviceEventListener.getInstance().offerDeviceEvent(readCardEvent);
						// }
					} else {
						ticketFrame.showWaitInputContent(null, idCard, 1, 0);// 测试代码
					}
				}
			}
		}
	}

	/**
	 * 华旭金卡读卡
	 * 
	 * @param idDeviceType
	 * @param device
	 */
	private void readUseHXGCDevice(HXGCDevice device) {
		String findval = device.Syn_StartFindIDCard();
		if (findval.equals("0")) {
			String selectval = device.Syn_SelectIDCard();
			if (selectval.equals("0")) {
				IDCard idCard = device.Syn_ReadBaseMsg();
				if (idCard != null && idCard.getIdNo() != null && idCard.getCardImage() != null && idCard.getCardImageBytes() != null) {
					if (DeviceConfig.getInstance().getVersionFlag() == 1) {// 以下为正式代码
						if (DeviceEventListener.getInstance().isDealDeviceEvent()) {
							IDCardReaderEvent readCardEvent = new IDCardReaderEvent();
							readCardEvent.setIdCard(idCard);
							log.debug("offerDeviceEvent idCard");
							DeviceEventListener.getInstance().offerDeviceEvent(readCardEvent);
						}
					} else {
						ticketFrame.showWaitInputContent(null, idCard, 1, 0);// 测试代码
					}
				}
			}
		}
	}

	/**
	 * 铁科版本读卡器
	 * 
	 * @param device
	 */
	private void readTkIDCDevice(TKIDCDevice device) {
		// device.IDC_GetSerial();
		String findval = device.IDC_FetchCard(3000);
		if (findval.equals("0")) {
			IDCard idCard = device.IDC_ReadIDCardInfo(2);
			if (idCard != null && idCard.getIdNo() != null && idCard.getCardImage() != null && idCard.getCardImageBytes() != null) {
				if (DeviceConfig.getInstance().getVersionFlag() == 1) {// 以下为正式代码
					// if
					// (DeviceEventListener.getInstance().isDealDeviceEvent()) {
					IDCardReaderEvent readCardEvent = new IDCardReaderEvent();
					readCardEvent.setIdCard(idCard);
					log.info("offerDeviceEvent idCard");
					DeviceEventListener.getInstance().getTicketVerify().setIdCard(idCard);
					DeviceEventListener.getInstance().offerDeviceEvent(readCardEvent);
					// }
				} else {
					ticketFrame.showWaitInputContent(null, idCard, 1, 0);// 测试代码
				}
			}
		}
	}

	/**
	 * 铁科新版读卡器
	 * 
	 * @param device
	 */
	private void readTkIDCDevice(RSIVIDcardDevice device) {
		// device.IDC_GetSerial();
		log.debug("准备开始寻二代证");
		int findval = device.IDC_FetchCard(3000);
		if (findval == 36864) {
			IDCard idCard = device.IDC_ReadIDCardInfo(2);
			if (idCard != null && idCard.getIdNo() != null && idCard.getCardImage() != null && idCard.getCardImageBytes() != null) {
				if (DeviceConfig.getInstance().getVersionFlag() == 1) {// 以下为正式代码
					// if
					// (DeviceEventListener.getInstance().isDealDeviceEvent()) {
					IDCardReaderEvent readCardEvent = new IDCardReaderEvent();
					readCardEvent.setIdCard(idCard);
					log.info("offerDeviceEvent idCard");
					DeviceEventListener.getInstance().getTicketVerify().setIdCard(idCard);
					DeviceEventListener.getInstance().offerDeviceEvent(readCardEvent);
					// }
				} else {
					ticketFrame.showWaitInputContent(null, idCard, 1, 0);// 测试代码
				}
			}
		}
		if (findval == 36881) {
			disconnectCount = disconnectCount + 1;
			if (this.lastStatusCode != findval && disconnectCount > 3) {
				log.info("二代证读卡连接失败");
				MonitorXMLUtil.updateBaseInfoFonMonitor(Config.getInstance().getBaseInfoXMLPath(), "004", 1);
				MonitorXMLUtil.updateDoorStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), "001", "001", "004", "006");
				MonitorXMLUtil.updateEntirStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), 1);
				this.lastStatusCode = findval;
			}
		}
		if (findval == 36880) {
			disconnectCount = 0;
			if (this.lastStatusCode != findval) {
				log.info("二代证读卡已经恢复连接");
				MonitorXMLUtil.updateBaseInfoFonMonitor(Config.getInstance().getBaseInfoXMLPath(), "004", 0);
				MonitorXMLUtil.updateDoorStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), "001", "001", "004", "000");
				MonitorXMLUtil.updateEntirStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), 0);
				this.lastStatusCode = findval;
			}
		}
	}

	/**
	 * 
	 * @param device
	 * @return
	 */
	// public boolean getFetchCardStatus(TKIDCDevice device) {
	// boolean flag = false;
	// if (device.IDC_FetchCard(300).equals("0")) {
	// flag = true;
	// } else {
	// flag = false;
	// }
	// return flag;
	// }

	public void start() {
		deviceStatus = Config.StartStatus;
	}

	public void stop() {
		deviceStatus = Config.StopStatus;
	}

	/**
	 * 为测试用
	 * 
	 * @return
	 */
	private IDCard mockIDCard() {
		IDCard card = new IDCard();
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(new File("C:/DCZ/20160412/llp.jpg"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		card.setCardImage(bi);
		return card;

	}

	public static void main(String[] args) {
		TicketVerifyFrame ticketFrame = new TicketVerifyFrame();
		// ticketFrame.setVisible(true);
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		gs[0].setFullScreenWindow(ticketFrame);
		IDReader idReader = IDReader.getInstance();
		idReader.setTicketFrame(ticketFrame);
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleWithFixedDelay(idReader, 0, 150, TimeUnit.MILLISECONDS);
	}
}
