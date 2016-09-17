package com.rxtec.pitchecking;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.rxtec.pitchecking.mqtt.MqttServerPaho;
import com.rxtec.pitchecking.net.PIVerifyEventSubscriber;
import com.rxtec.pitchecking.net.PIVerifyResultSubscriber;
import com.rxtec.pitchecking.net.PTVerifyPublisher;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.FaceDetectionService;
import com.rxtec.pitchecking.picheckingservice.realsense.RSFaceDetectionService;

/**
 * 人脸检测独立进程入口
 * @author lenovo
 *
 */
public class PITrackingApp {

	static FaceTrackingScreen screen = FaceTrackingScreen.getInstance();

	public static void main(String[] args) throws InterruptedException {
		screen.initUI();
		
		PIVerifyEventSubscriber.getInstance().startSubscribing();	//启动 闸机主控程序发送事件的订阅者
		PTVerifyPublisher.getInstance();


//		FaceCheckingService.getInstance().beginFaceCheckerTask();  	//启动人脸比对
//		MqttServerPaho.getInstance();  //启动mqtt服务端

		Thread.sleep(1000);
		if (Config.getInstance().getVideoType() == Config.RealSenseVideo) {
			RSFaceDetectionService.getInstance().setVideoPanel(screen.getVideoPanel());
			RSFaceDetectionService.getInstance().beginVideoCaptureAndTracking();  //启动人脸检测线程
		} else {
			 FaceDetectionService.getInstance().setVideoPanel(screen.getVideoPanel());
			 FaceDetectionService.getInstance().beginVideoCaptureAndTracking();
		}
	}
}
