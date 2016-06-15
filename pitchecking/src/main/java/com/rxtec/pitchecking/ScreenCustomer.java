package com.rxtec.pitchecking;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ScreenCustomer implements Runnable {
	private Log log = LogFactory.getLog("DeviceEventListener");

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			try {
				TicketCheckScreen.getInstance().startShow();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
