package com.rxtec.pitchecking;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.device.DeviceException;
import com.rxtec.pitchecking.gui.ticketgui.TicketVerifyFrame;
import com.rxtec.pitchecking.mqtt.GatCtrlReceiverBroker;
import com.rxtec.pitchecking.mqtt.GatCtrlSenderBroker;
import com.rxtec.pitchecking.mqtt.MqttCamRequestSender;
import com.rxtec.pitchecking.mqtt.MqttCamResponseReceiver;
import com.rxtec.pitchecking.task.UpdateFlapCountJob;
import com.rxtec.pitchecking.task.VerifyScreenListener;

public class TicketCheckApp {
	static Logger log = LoggerFactory.getLogger("DeviceEventListener");

	public TicketCheckApp() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			if (Config.getInstance().getIsOutGateForCSQ() == 1) {
				TicketVerifyScreen ticketVerifyScreen = TicketVerifyScreen.getInstance();
				TicketVerifyFrame tickFrame = new TicketVerifyFrame();
				ticketVerifyScreen.setTicketFrame(tickFrame);

				tickFrame.showSuccWait("", "系统启动中...");

				try {
					ticketVerifyScreen.initUI(DeviceConfig.getInstance().getTicketScreen());
				} catch (Exception ex) {
					// TODO Auto-generated catch block
					log.error("PITVerifyApp:", ex);
				}
			}

			if (Config.getInstance().getIsStartMainListener() == 1) {
//				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT);
//				GatCtrlReceiverBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT); // 启动PITEventTopic本地监听

				DeviceEventListener eventListener = DeviceEventListener.getInstance();
				try {
					if (Config.getInstance().getPitVerifyMode() == 1) {
						// 初始化闸机部件
						eventListener.startDevice();
					}
					// eventListener.offerDeviceEventTest(); //仅供测试用
				} catch (DeviceException e1) {
					// TODO Auto-generated catch block
					log.error("startDevice:", e1);
				}

				if (eventListener.isStartThread()) {
					ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
					scheduler.scheduleWithFixedDelay(eventListener, 0, 200, TimeUnit.MILLISECONDS);
				}
			}
		} catch (Exception ex) {
			log.error("", ex);
		}
	}

}
