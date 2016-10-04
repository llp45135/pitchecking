package com.rxtec.pitchecking;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.rxtec.pitchecking.device.LightControlBoard;
import com.rxtec.pitchecking.gui.FaceCheckFrame;
import com.rxtec.pitchecking.mqtt.MqttReceiverBroker;
import com.rxtec.pitchecking.net.PIVerifyEventSubscriber;
import com.rxtec.pitchecking.net.PIVerifyResultSubscriber;
import com.rxtec.pitchecking.net.PTVerifyPublisher;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.FaceDetectionService;
import com.rxtec.pitchecking.picheckingservice.realsense.RSFaceDetectionService;

/**
 * 人脸检测独立进程入口
 * 
 * @author lenovo
 *
 */
public class PITrackingApp {

	static FaceTrackingScreen faceTrackingScreen = FaceTrackingScreen.getInstance();

	public static void main(String[] args) throws InterruptedException {
//		LightControlBoard.getInstance().startLED();

		FaceCheckFrame faceCheckFrame = new FaceCheckFrame();
		faceTrackingScreen.setFaceFrame(faceCheckFrame);
		faceTrackingScreen.initUI();
		faceTrackingScreen.getFaceFrame().showDefaultContent();

		// PIVerifyEventSubscriber.getInstance().startSubscribing(); //启动
		// 闸机主控程序发送事件的订阅者
		// PTVerifyPublisher.getInstance();

		MqttReceiverBroker mqtt = MqttReceiverBroker.getInstance();

		FaceCheckingService.getInstance().beginFaceCheckerTask(); // 启动人脸比对

		// 语音调用线程
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleWithFixedDelay(AudioPlayTask.getInstance(), 0, 100, TimeUnit.MILLISECONDS);

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
