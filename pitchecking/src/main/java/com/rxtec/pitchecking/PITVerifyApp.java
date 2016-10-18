package com.rxtec.pitchecking;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.mqtt.GatCtrlReceiverBroker;
import com.rxtec.pitchecking.mqtt.MqttReceiverBroker;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.FaceDetectionService;
import com.rxtec.pitchecking.picheckingservice.realsense.RSFaceDetectionService;

/**
 * 单票证核验版本主入口，从该入口启动主程序，该进程不用管理第二块屏、不做人脸检测
 * @author ZhaoLin
 *
 */
public class PITVerifyApp {
	static DeviceEventListener eventListener = DeviceEventListener.getInstance();
	static TicketVerifyScreen ticketVerifyScreen = TicketVerifyScreen.getInstance();

	public static void main(String[] args) throws InterruptedException {
		ticketVerifyScreen.initUI();

		Thread.sleep(1000);
//		MqttReceiverBroker mqtt = MqttReceiverBroker.getInstance();
		// TODO Auto-generated method stub
		// 启动事件监听
		GatCtrlReceiverBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT);  //启动PITEventTopic本地监听

		
		eventListener.setPitStatus(PITStatusEnum.DefaultStatus.getValue());

//		ExecutorService executorService = Executors.newCachedThreadPool();
//		executorService.execute(eventListener);

		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleWithFixedDelay(eventListener, 0, 200, TimeUnit.MILLISECONDS);

	}
}
