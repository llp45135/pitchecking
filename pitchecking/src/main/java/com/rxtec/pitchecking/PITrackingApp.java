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
import com.rxtec.pitchecking.gui.VideoPanel;
import com.rxtec.pitchecking.gui.singledoor.SingleVerifyFrame;
import com.rxtec.pitchecking.mq.RemoteMonitorPublisher;
import com.rxtec.pitchecking.mqtt.GatCtrlReceiverBroker;
import com.rxtec.pitchecking.mqtt.MqttReceiverBroker;
import com.rxtec.pitchecking.mqtt.pitevent.PTVerifyResultReceiver;
import com.rxtec.pitchecking.mqtt.pitevent.PTVerifyThread;
import com.rxtec.pitchecking.net.PIVerifyEventSubscriber;
import com.rxtec.pitchecking.net.PIVerifyResultSubscriber;
import com.rxtec.pitchecking.net.PTVerifyPublisher;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.FaceDetectionService;
import com.rxtec.pitchecking.picheckingservice.realsense.RSFaceDetectionService;
import com.rxtec.pitchecking.task.FaceScreenListener;
import com.rxtec.pitchecking.task.ManualCheckingTask;

/**
 * 人脸检测独立进程入口
 * 
 * @author lenovo
 *
 */
public class PITrackingApp {

	static Logger log = LoggerFactory.getLogger("DeviceEventListener");

	// static FaceTrackingScreen faceTrackingScreen =
	// FaceTrackingScreen.getInstance();

	public static void main(String[] args) throws InterruptedException {
		log.debug("/*************后置摄像头检脸进程启动主入口****************/");
		Config appConfig = Config.getInstance();
		Config.getInstance().setCameraNum(appConfig.getBehindCameraNo()); // 设置摄像头序号
		log.debug("getCameraNum==" + appConfig.getCameraNum());

		FaceCheckingService.getInstance().noticeCameraStatus();

		VideoPanel videoPanel = null;

		if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {
			FaceCheckFrame faceCheckFrame = new FaceCheckFrame();
			FaceTrackingScreen.getInstance().setFaceFrame(faceCheckFrame);
			boolean initUIFlag = false;
			try {
				FaceTrackingScreen.getInstance().initUI(DeviceConfig.getInstance().getFaceScreen());
				initUIFlag = true;
			} catch (Exception ex) {
				log.error("PITrackingApp:", ex);
			}
			if (!initUIFlag) {
				try {
					FaceTrackingScreen.getInstance().initUI(0);
					initUIFlag = true;
				} catch (Exception ex) {
					// TODO Auto-generated catch block
					log.error("PITrackingApp:", ex);
				}
			}
			if (initUIFlag) {
				FaceTrackingScreen.getInstance().getFaceFrame().showDefaultContent();
			} else {
				return;
			}

			videoPanel = FaceTrackingScreen.getInstance().getVideoPanel();
		} else { // 单门模式
			SingleVerifyFrame singleVerifyFrame = new SingleVerifyFrame();
			SingleFaceTrackingScreen.getInstance().setFaceFrame(singleVerifyFrame);
			boolean initUIFlag = false;
			try {
				SingleFaceTrackingScreen.getInstance().initUI(DeviceConfig.getInstance().getFaceScreen());
				initUIFlag = true;
			} catch (Exception ex) {
				log.error("PITrackingApp:", ex);
			}
			if (!initUIFlag) {
				try {
					SingleFaceTrackingScreen.getInstance().initUI(0);
					initUIFlag = true;
				} catch (Exception ex) {
					// TODO Auto-generated catch block
					log.error("PITrackingApp:", ex);
				}
			}
			if (!initUIFlag) {
				return;
			}

			videoPanel = SingleFaceTrackingScreen.getInstance().getVideoPanel();
		}

		MqttReceiverBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum()); // 同CAM_RXTa.dll通信

		GatCtrlReceiverBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum());// 启动PITEventTopic本地监听

		FaceCheckingService.getInstance().addQuartzJobs();

		if (Config.getInstance().getTransferFaceMode() == 1) {  //aeron
			FaceCheckingService.getInstance().beginFaceCheckerTask(); // 启动人脸发布和比对结果订阅
		}
		// mq方式发布待验证人脸
		if (Config.getInstance().getTransferFaceMode() == 2) {   //mqtt
			PTVerifyThread ptVerifyThread = new PTVerifyThread(
					DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum());
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(ptVerifyThread);
			PTVerifyResultReceiver.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum()); // mq方式比对结果订阅
		}

		// 跳屏恢复调用线程
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleWithFixedDelay(FaceScreenListener.getInstance(), 0, 2000, TimeUnit.MILLISECONDS);

		if (Config.getInstance().getIsUseManualMQ() == 1) {// 是否连人工窗控制台
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			ManualCheckingTask manualCheckingTask = new ManualCheckingTask(
					DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum());
			executorService.execute(manualCheckingTask);
		}

		Thread.sleep(1000);

		if (Config.getInstance().getVideoType() == Config.RealSenseVideo) {
			RSFaceDetectionService.getInstance().setVideoPanel(videoPanel);
			RSFaceDetectionService.getInstance().beginVideoCaptureAndTracking(); // 启动人脸检测线程
		} else {
			FaceDetectionService.getInstance().setVideoPanel(videoPanel);
			FaceDetectionService.getInstance().beginVideoCaptureAndTracking();
		}
	}
}
