package com.rxtec.pitchecking.device;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.event.IDCardReaderEvent;
import com.rxtec.pitchecking.device.event.IDeviceEvent;

public class IDReaderTask implements Runnable {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");
	private IDCardDevice device = IDCardDevice.getInstance(1001);
	

	public IDReaderTask(){
		device.Syn_OpenPort(1001);
	}
	@Override
	public void run() {

		while (true) {

			IDeviceEvent e = findCard();

			try {
				if (e != null) {
					DeviceEventListener.getInstance().addDeviceEvent(e);
					
					device.Syn_SelectIDCard(1001, 0);
					DeviceEventListener.getInstance().addDeviceEvent(e);
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}

	}

	private IDeviceEvent readCard() {
		IDeviceEvent event = null;
		/*
		 * 读二代证数据,填充event 读不到数据返回null
		 */
		log.debug("IDCard");
		return event;
	}
	
	private IDeviceEvent findCard(){
		IDeviceEvent event = null;
		String findval = device.Syn_StartFindIDCard(1001, 0);
		if(findval.equals("0")){
			event = new IDCardReaderEvent(1);
		}
		return event;
	}

}
