package com.rxtec.pitchecking;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.gui.ticketgui.TicketVerifyFrame;
import com.rxtec.pitchecking.mqtt.GatCtrlReceiverBroker;
import com.rxtec.pitchecking.mqtt.MqttReceiverBroker;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.FaceDetectionService;
import com.rxtec.pitchecking.picheckingservice.realsense.RSFaceDetectionService;
import com.rxtec.pitchecking.utils.CommUtil;

/**
 * 单票证核验版本主入口，从该入口启动主程序，该进程不用管理第二块屏、不做人脸检测
 * 
 * @author ZhaoLin
 *
 */
public class PITVerifyApp {
	static Logger log = LoggerFactory.getLogger("DeviceEventListener");

	public static void main(String[] args) {
		try {

			GatCtrlReceiverBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT); // 启动PITEventTopic本地监听

			TicketVerifyScreen ticketVerifyScreen = TicketVerifyScreen.getInstance();
			TicketVerifyFrame tickFrame = new TicketVerifyFrame();
			ticketVerifyScreen.setTicketFrame(tickFrame);
			try {
				ticketVerifyScreen.initUI(DeviceConfig.getInstance().getTicketScreen());
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				log.error("PITUserGuideApp:", ex);
			}

			DeviceEventListener eventListener = DeviceEventListener.getInstance();
			// Thread.sleep(3000);
			// MqttReceiverBroker mqtt = MqttReceiverBroker.getInstance();
			// TODO Auto-generated method stub
			// 启动事件监听

			// eventListener.setPitStatus(PITStatusEnum.DefaultStatus.getValue());
			// log.debug("准备初始化界面");
			// CommUtil.sleep(10*1000);

			// ExecutorService executorService =
			// Executors.newCachedThreadPool();
			// executorService.execute(eventListener);

			if (eventListener.isStartThread()) {
				ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
				scheduler.scheduleWithFixedDelay(eventListener, 0, 200, TimeUnit.MILLISECONDS);
			}
		} catch (Exception ex) {
			log.error("", ex);
		}
	}
}
