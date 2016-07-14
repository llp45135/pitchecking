package com.rxtec.pitchecking;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.openimaj.video.capture.VideoCaptureException;

import com.rxtec.pitchecking.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.PITData;
import com.rxtec.pitchecking.picheckingservice.realsense.RSFaceDetectionService;
import com.rxtec.pitchecking.picheckingservice.FaceDetectionService;
import com.rxtec.pitchecking.picheckingservice.FaceImageLog;
import com.rxtec.pitchecking.task.RunningStatus;

/**
 * 人证识别自动验票软件主入口
 * 
 * @author ZhaoLin
 *
 */
public class PitCheckingApp {

	static DeviceEventListener eventListener = DeviceEventListener.getInstance();
	static TicketCheckScreen ticketCheckScreen = TicketCheckScreen.getInstance();

	public static void main(String[] args) throws InterruptedException {
		FaceImageLog.clearFaceLogs();
		ticketCheckScreen.initUI();

		Thread.sleep(1000);

		// TODO Auto-generated method stub
		// 启动事件监听

		FaceCheckingService.getInstance().beginFaceCheckerTask();  //启动人脸比对线程

		if (Config.getInstance().getVideoType() == Config.RealSenseVideo) {
			RSFaceDetectionService.getInstance().setVideoPanel(ticketCheckScreen.getVideoPanel());
			RSFaceDetectionService.getInstance().beginVideoCaptureAndTracking();  //启动人脸检测线程
		} else {
			 FaceDetectionService.getInstance().setVideoPanel(ticketCheckScreen.getVideoPanel());
			 FaceDetectionService.getInstance().beginVideoCaptureAndTracking();
		}



		eventListener.setPitStatus(PITStatusEnum.DefaultStatus.getValue());

		// eventListener.startListenEvent();

		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleWithFixedDelay(eventListener, 0, 100, TimeUnit.MILLISECONDS);

	}

}
