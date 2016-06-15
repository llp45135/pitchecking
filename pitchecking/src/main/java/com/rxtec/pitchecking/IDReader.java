package com.rxtec.pitchecking;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.IDCardDevice;
import com.rxtec.pitchecking.event.IDCardReaderEvent;
import com.rxtec.pitchecking.event.IDeviceEvent;

public class IDReader implements Runnable {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");
	IDCardDevice device = IDCardDevice.getInstance();
	private int deviceStatus = Config.StartStatus;


	private static IDReader instance;
	public static synchronized IDReader getInstance() {
		if (instance == null) {
			instance = new IDReader();
		}
		return instance;
	}
	
	
	@Override
	public void run() {
		readCard();
	}

	private void readCard() {

		if(deviceStatus == Config.StopStatus) return;
		
		/*
		 * 读二代证数据,填充event 读不到数据返回null
		 */
		// TicketCheckScreen.getInstance().repainFaceFrame();

		// log.debug("开始寻卡...");
		String openPortResult = device.Syn_OpenPort();
		if (openPortResult.equals("0")) {
			String findval = device.Syn_StartFindIDCard();
			if (findval.equals("0")) {

				IDeviceEvent findedCardEvent = new IDCardReaderEvent();
				DeviceEventListener.getInstance().offerDeviceEvent(findedCardEvent);
				String selectval = device.Syn_SelectIDCard();
				if (selectval.equals("0")) {
					IDCard idCard = device.Syn_ReadBaseMsg();
					if (idCard != null) {
						IDCardReaderEvent readCardEvent = new IDCardReaderEvent();
						readCardEvent.setIdCard(idCard);
						DeviceEventListener.getInstance().offerDeviceEvent(readCardEvent);
					}
				}
			} else {
				// log.debug("没有找到身份证");
			}

			device.Syn_ClosePort();
		}
	}
	
	
	public void start(){
		deviceStatus = Config.StartStatus;
	}
	
	public void stop(){
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
	
	
	

}
