package com.rxtec.pitchecking;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.device.LightControlBoard;
import com.rxtec.pitchecking.device.smartmonitor.MonitorXMLUtil;
import com.rxtec.pitchecking.gui.FaceCheckFrame;
import com.rxtec.pitchecking.gui.HighFaceCheckFrame;
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
import com.rxtec.pitchecking.socket.pitevent.FaceReceiver;
import com.rxtec.pitchecking.socket.pitevent.FaceResultReceiver;
import com.rxtec.pitchecking.socket.pitevent.FaceSendThread;
import com.rxtec.pitchecking.socket.pitevent.FaceSender;
import com.rxtec.pitchecking.task.FaceScreenListener;
import com.rxtec.pitchecking.task.ManualCheckingTask;

/**
 * 人脸检测独立进程入口
 * 
 * @author lenovo
 *
 */
public class PITFrontTrackingApp {

	static Logger log = LoggerFactory.getLogger("DeviceEventListener");

	// static FaceTrackingScreen faceTrackingScreen =
	// FaceTrackingScreen.getInstance();

	public static void main(String[] args) throws InterruptedException {
		log.info("/*************前置摄像头检脸进程启动主入口****************/");
		log.info("当前软件版本号:" + DeviceConfig.softVersion);
		Config appConfig = Config.getInstance();
		appConfig.setCameraNum(1);
		FaceCheckingService.getInstance().setFrontCamera(true);
		DeviceConfig.getInstance().setCameraDirection("前置");
		log.debug("getCameraNum==" + appConfig.getCameraNum());

		FaceCheckingService.getInstance().noticeCameraStatus();

		VideoPanel videoPanel = null;

		if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {
			if (Config.getInstance().getFrontScreenMode() == 1) {
				FaceCheckFrame faceCheckFrame = new FaceCheckFrame();
				FaceTrackingScreen.getInstance().setFaceFrame(faceCheckFrame);
				boolean initUIFlag = false;
				try {
					FaceTrackingScreen.getInstance().initUI(DeviceConfig.getInstance().getGuideScreen());
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
			} else {
				HighFaceCheckFrame faceCheckFrame = new HighFaceCheckFrame();
				HighFaceTrackingScreen.getInstance().setFaceFrame(faceCheckFrame);
				boolean initUIFlag = false;
				try {
					HighFaceTrackingScreen.getInstance().initUI(DeviceConfig.getInstance().getGuideScreen());
					initUIFlag = true;
				} catch (Exception ex) {
					log.error("PITrackingApp:", ex);
				}
				if (!initUIFlag) {
					try {
						HighFaceTrackingScreen.getInstance().initUI(0);
						initUIFlag = true;
					} catch (Exception ex) {
						// TODO Auto-generated catch block
						log.error("PITrackingApp:", ex);
					}
				}
				if (initUIFlag) {
					HighFaceTrackingScreen.getInstance().getFaceFrame().showDefaultContent();
				} else {
					return;
				}

				videoPanel = HighFaceTrackingScreen.getInstance().getVideoPanel();
			}
		} else { // 单门模式
			SingleVerifyFrame singleVerifyFrame = new SingleVerifyFrame();
			SingleFaceTrackingScreen.getInstance().setFaceFrame(singleVerifyFrame);
			boolean initUIFlag = false;
			try {
				SingleFaceTrackingScreen.getInstance().initUI(DeviceConfig.getInstance().getGuideScreen());
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

		GatCtrlReceiverBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum());// 启动PITEventTopic本地监听

		FaceCheckingService.getInstance().addQuartzJobs();

		FaceCheckingService.getInstance().setSubscribeVerifyResult(false); // 前置摄像头不订阅验证结果，全部放在后置摄像头进程来处理

		if (Config.getInstance().getTransferFaceMode() == Config.TransferFaceByAeron) { // aeron
			FaceCheckingService.getInstance().beginFaceCheckerTask(); // 启动人脸发布和比对结果订阅
																		// ByAeron
		}
		// mq方式发布待验证人脸
		if (Config.getInstance().getTransferFaceMode() == Config.TransferFaceByMqtt) { // mqtt
			PTVerifyThread ptVerifyThread = new PTVerifyThread(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum());
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(ptVerifyThread); // 启动人脸发布ByMqtt
		}
		if (Config.getInstance().getTransferFaceMode() == Config.TransferFaceBySocket) { // socket
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(new FaceSendThread()); // 启动人脸发布BySocket
		}

		// 跳屏恢复调用线程
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleWithFixedDelay(FaceScreenListener.getInstance(), 0, 2000, TimeUnit.MILLISECONDS);

		
		
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
