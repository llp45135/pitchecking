package com.rxtec.pitchecking;

import com.rxtec.pitchecking.net.PIVerifyEventSubscriber;
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

		Thread.sleep(1000);

		FaceCheckingService.getInstance().beginFaceCheckerTask();  //启动人脸比对
		PIVerifyEventSubscriber EventSubscriber = new PIVerifyEventSubscriber();	//启动 闸机主控程序发送事件的订阅者
		if (Config.getInstance().getVideoType() == Config.RealSenseVideo) {
			RSFaceDetectionService.getInstance().setVideoPanel(screen.getVideoPanel());
			RSFaceDetectionService.getInstance().beginVideoCaptureAndTracking();  //启动人脸检测线程
		} else {
			 FaceDetectionService.getInstance().setVideoPanel(screen.getVideoPanel());
			 FaceDetectionService.getInstance().beginVideoCaptureAndTracking();
		}
	}
}
