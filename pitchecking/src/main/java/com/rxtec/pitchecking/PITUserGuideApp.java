package com.rxtec.pitchecking;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.gui.ticketgui.TicketVerifyFrame;
import com.rxtec.pitchecking.mqtt.GatCtrlReceiverBroker;
import com.rxtec.pitchecking.task.GuideScreenListener;

public class PITUserGuideApp {
	static Logger log = LoggerFactory.getLogger("DeviceEventListener");

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TicketVerifyScreen ticketVerifyScreen = TicketVerifyScreen.getInstance();
		TicketVerifyFrame tickFrame = new TicketVerifyFrame();
		ticketVerifyScreen.setTicketFrame(tickFrame);
		
		
		try {
			ticketVerifyScreen.initUI(DeviceConfig.getInstance().getGuideScreen());
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			log.error("PITUserGuideApp:", ex);
		}
		
		ScheduledExecutorService screenScheduler = Executors.newScheduledThreadPool(1);
		GuideScreenListener guideScreenListener = GuideScreenListener.getInstance();
		guideScreenListener.setScreenNo(DeviceConfig.getInstance().getGuideScreen());
		screenScheduler.scheduleWithFixedDelay(guideScreenListener, 0, 1500, TimeUnit.MILLISECONDS);
		
		GatCtrlReceiverBroker.getInstance(DeviceConfig.GAT_MQ_Guide_CLIENT); // 启动PITEventTopic本地监听
	}

}
