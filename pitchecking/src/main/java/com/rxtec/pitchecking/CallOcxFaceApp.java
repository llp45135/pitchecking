package com.rxtec.pitchecking;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.gui.faceocx.CallOcxFaceFrame;
import com.rxtec.pitchecking.gui.faceocx.OcxFaceFrameTwo;
import com.rxtec.pitchecking.mqtt.SingleOcxMQReceiver;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.task.ManualCheckingTask;

/**
 * 人脸检测独立进程入口
 * 
 * @author lenovo
 *
 */
public class CallOcxFaceApp {

	static Logger log = LoggerFactory.getLogger("DeviceEventListener");


	public static void main(String[] args) throws InterruptedException {
		log.debug("/*************USB后置摄像头检脸进程启动主入口****************/");
		log.info("当前软件版本号:" + DeviceConfig.softVersion);
		Config appConfig = Config.getInstance();
		appConfig.setCameraNum(2);
		log.debug("getCameraNum==" + appConfig.getCameraNum());

		FaceCheckingService.getInstance().noticeCameraStatus();

		/**
		 * 铁路版本
		 */
		CallOcxFaceFrame callOcxFaceFrame = new CallOcxFaceFrame();
		callOcxFaceFrame.setCameraNo(appConfig.getOcxUsbCameraNo());		
		SingleOcxFaceScreen.getInstance().setOcxFaceFrame(callOcxFaceFrame);
		
		/**
		 * 桂林全屏版本
		 */
//		appConfig.setUseFullScreenCamera(true);
//		OcxFaceFrameTwo callOcxFaceFrame = new OcxFaceFrameTwo();
//		callOcxFaceFrame.setCameraNo(appConfig.getOcxUsbCameraNo());		
//		SingleOcxFaceScreen.getInstance().setOcxFaceFrame(callOcxFaceFrame);
		
		boolean initUIFlag = false;
		try {
			SingleOcxFaceScreen.getInstance().initUI(DeviceConfig.getInstance().getFaceScreen());
			initUIFlag = true;
		} catch (Exception ex) {
			log.error("CallOcxFaceApp:", ex);
		}
		if (!initUIFlag) {
			try {
				SingleFaceTrackingScreen.getInstance().initUI(0);
				initUIFlag = true;
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				log.error("CallOcxFaceApp:", ex);
			}
		}
		if (!initUIFlag) {
			return;
		}
		
		

		SingleOcxMQReceiver.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum());// 启动PITEventTopic本地监听

	}
}
