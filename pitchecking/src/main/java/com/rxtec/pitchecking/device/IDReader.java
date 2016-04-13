package com.rxtec.pitchecking.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.event.IDCardReaderEvent;
import com.rxtec.pitchecking.device.event.IDeviceEvent;
import com.rxtec.pitchecking.picheckingservice.IDCard;

public class IDReader implements Runnable {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");
	IDCardDevice device = IDCardDevice.getInstance();

	@Override
	public void run() {

		while (true) {
			
			log.debug("准备开始readCard");
			IDeviceEvent e = readCard();

			try {
				if (e != null) {
					log.debug("IDReader e==" + e);
					DeviceEventListener.getInstance().addDeviceEvent(e);
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}

	}

	private IDeviceEvent readCard() {

		IDeviceEvent event = new IDCardReaderEvent();
		IDCardReaderEvent idcardEvent = (IDCardReaderEvent) event;
		/*
		 * 读二代证数据,填充event 读不到数据返回null
		 */
		TicketCheckScreen.getInstance().repainFaceFrame();
		
		log.debug("开始寻卡...");
		device.Syn_OpenPort();
		String findval = device.Syn_StartFindIDCard();
		if (findval.equals("0")) {
			String selectval = device.Syn_SelectIDCard();
			if (selectval.equals("0")) {
				IDCard idCard = device.Syn_ReadBaseMsg();
				if (idCard != null) {
					idcardEvent.setIdCard(idCard);
				}
			}
		} else {
			log.debug("没有找到身份证");
			idcardEvent = null;
		}
		device.Syn_ClosePort();
		return idcardEvent;
	}

}
