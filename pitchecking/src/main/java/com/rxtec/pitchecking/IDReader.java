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
import com.rxtec.pitchecking.device.XZXCDevice;
import com.rxtec.pitchecking.event.IDCardReaderEvent;
import com.rxtec.pitchecking.event.IDeviceEvent;
import com.rxtec.pitchecking.gui.TicketCheckFrame;
import com.rxtec.pitchecking.utils.CommUtil;

public class IDReader implements Runnable {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");
	private static IDReader instance;
	private XZXCDevice xzxcDevice = null;
	private HXGCDevice hxgcDevice = null;
	private int deviceStatus = Config.StartStatus;
	// 以下ticketFrame仅供测试用
	private TicketCheckFrame ticketFrame;

	public TicketCheckFrame getTicketFrame() {
		return ticketFrame;
	}

	public void setTicketFrame(TicketCheckFrame ticketFrame) {
		this.ticketFrame = ticketFrame;
	}

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
			openVal = xzxcDevice.Syn_OpenPort();
		} else {
			hxgcDevice = HXGCDevice.getInstance();
			openVal = hxgcDevice.Syn_OpenPort();
		}
		if (openVal.equals("0")) {
			DeviceConfig.getInstance().setIdDeviceStatus(DeviceConfig.idDeviceSucc);
		} else {
			DeviceConfig.getInstance().setIdDeviceStatus(Integer.parseInt(openVal));
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
				this.readUseXZXCDevice(xzxcDevice);
			} else {
				this.readUseHXGCDevice(hxgcDevice);
			}
		}
	}

	/**
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
				if (idCard != null && idCard.getIdNo() != null && idCard.getCardImage() != null
						&& idCard.getCardImageBytes() != null) {
					if (DeviceConfig.getInstance().getVersionFlag() == 1) {// 以下为正式代码
						if (DeviceEventListener.getInstance().isDealDeviceEvent()) {
							IDCardReaderEvent readCardEvent = new IDCardReaderEvent();
							readCardEvent.setIdCard(idCard);
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
				if (idCard != null && idCard.getIdNo() != null && idCard.getCardImage() != null
						&& idCard.getCardImageBytes() != null) {
					if (DeviceConfig.getInstance().getVersionFlag() == 1) {// 以下为正式代码
						if (DeviceEventListener.getInstance().isDealDeviceEvent()) {
							IDCardReaderEvent readCardEvent = new IDCardReaderEvent();
							readCardEvent.setIdCard(idCard);
							DeviceEventListener.getInstance().offerDeviceEvent(readCardEvent);
						}
					} else {
						ticketFrame.showWaitInputContent(null, idCard, 1, 0);// 测试代码
					}
				}
			}
		}
	}

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
		TicketCheckFrame ticketFrame = new TicketCheckFrame();
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
