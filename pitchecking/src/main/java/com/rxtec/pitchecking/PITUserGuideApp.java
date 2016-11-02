package com.rxtec.pitchecking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.gui.ticketgui.TicketVerifyFrame;

public class PITUserGuideApp {
	static Logger log = LoggerFactory.getLogger("DeviceEventListener");

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TicketVerifyScreen ticketVerifyScreen = TicketVerifyScreen.getInstance();
		TicketVerifyFrame tickFrame = new TicketVerifyFrame();
		ticketVerifyScreen.setTicketFrame(tickFrame);
		try {
			ticketVerifyScreen.initUI(DeviceConfig.getInstance().getTicketScreen());
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			log.error("PITUserGuideApp:", ex);
		}
	}

}
