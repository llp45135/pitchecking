package com.rxtec.pitchecking.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.event.IDeviceEvent;
import com.rxtec.pitchecking.device.event.QRCodeReaderEvent;

public class QRReader implements Runnable {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");

	@Override
	public void run() {

		while (true) {
			try {
				Thread.sleep(1000 * 60 * 30);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			IDeviceEvent e = readQRData();
			try {
				if (e != null) {
					log.debug("QRReader e==" + e);
					DeviceEventListener.getInstance().offerDeviceEvent(e);
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}

	private IDeviceEvent readQRData() {
		IDeviceEvent event = new QRCodeReaderEvent();

		/*
		 * 读二维码数据,填充event 读不到数据返回null
		 */
		log.debug("QRReader");
		return event;
	}

}
