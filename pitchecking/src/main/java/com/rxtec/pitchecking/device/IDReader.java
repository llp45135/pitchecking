package com.rxtec.pitchecking.device;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.event.IDCardReaderEvent;
import com.rxtec.pitchecking.device.event.IDeviceEvent;

public class IDReader implements Runnable {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");

	@Override
	public void run() {

		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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

		}

	}

	private IDeviceEvent readCard() {
		IDeviceEvent event = new IDCardReaderEvent();
		/*
		 * 读二代证数据,填充event 读不到数据返回null
		 */
		log.debug("IDCard");
		return event;
	}

}
