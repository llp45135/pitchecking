package com.rxtec.pitchecking.device;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rxtec.pitchecking.device.event.IDeviceEvent;

public class Customer implements Runnable {
	private Log log = LogFactory.getLog("DeviceEventListener");

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			// 如果没有新事件将一直阻塞等待到新设备事件
			try {
				DeviceEventListener.getInstance().takeDeviceEvent();
//				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}
