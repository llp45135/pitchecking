package com.rxtec.pitchecking;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.device.LightControlBoard;
import com.rxtec.pitchecking.gui.FaceCheckFrame;
import com.rxtec.pitchecking.mq.RemoteMonitorPublisher;
import com.rxtec.pitchecking.mqtt.MqttReceiverBroker;
import com.rxtec.pitchecking.net.PIVerifyEventSubscriber;
import com.rxtec.pitchecking.net.PIVerifyResultSubscriber;
import com.rxtec.pitchecking.net.PTVerifyPublisher;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.FaceDetectionService;
import com.rxtec.pitchecking.picheckingservice.realsense.RSFaceDetectionService;
import com.rxtec.pitchecking.task.FaceScreenListener;

/**
 * 人脸检测独立进程入口
 * 
 * @author lenovo
 *
 */
public class PITrackingApp {

	static Logger log = LoggerFactory.getLogger("DeviceEventListener");

	static FaceTrackingScreen faceTrackingScreen = FaceTrackingScreen.getInstance();

	public static void main(String[] args) throws InterruptedException {
		log.info("/*************检脸进程启动主入口****************/");
		FaceCheckFrame faceCheckFrame = new FaceCheckFrame();
		faceTrackingScreen.setFaceFrame(faceCheckFrame);
		boolean initUIFlag = false;
		try {
			faceTrackingScreen.initUI(DeviceConfig.getInstance().getFaceScreen());
			initUIFlag = true;
		} catch (Exception ex) {
			log.error("PITrackingApp:", ex);
		}
		if (!initUIFlag) {
			try {
				faceTrackingScreen.initUI(0);
				initUIFlag = true;
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				log.error("PITrackingApp:", ex);
			}
		}
		if (initUIFlag) {
			faceTrackingScreen.getFaceFrame().showDefaultContent();
		} else {
			return;
		}

		int lightLedRet = -1;
		log.info("准备点亮补光灯");
		try {
			lightLedRet = LightControlBoard.getInstance().startLED(); // 点亮补光灯
		} catch (Exception ex) {
			log.error("PITrackingApp:", ex);
		}
		log.info("点亮补光灯,lightLedRet==" + lightLedRet);

		// PIVerifyEventSubscriber.getInstance().startSubscribing(); //启动
		// 闸机主控程序发送事件的订阅者
		// PTVerifyPublisher.getInstance();

		MqttReceiverBroker mqtt = MqttReceiverBroker.getInstance();

		FaceCheckingService.getInstance().beginFaceCheckerTask(); // 启动人脸发布和比对结果订阅

		// 语音调用线程
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
		scheduler.scheduleWithFixedDelay(AudioPlayTask.getInstance(), 0, 100, TimeUnit.MILLISECONDS);
		scheduler.scheduleWithFixedDelay(FaceScreenListener.getInstance(), 0, 1500, TimeUnit.MILLISECONDS);

		if (Config.getInstance().getIsUseManualMQ() == 1) {
			RemoteMonitorPublisher.getInstance().startService(1);
		}

		Thread.sleep(1000);
		if (Config.getInstance().getVideoType() == Config.RealSenseVideo) {
			RSFaceDetectionService.getInstance().setVideoPanel(faceTrackingScreen.getVideoPanel());
			RSFaceDetectionService.getInstance().beginVideoCaptureAndTracking(); // 启动人脸检测线程
		} else {
			FaceDetectionService.getInstance().setVideoPanel(faceTrackingScreen.getVideoPanel());
			FaceDetectionService.getInstance().beginVideoCaptureAndTracking();
		}
	}
}
