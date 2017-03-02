package com.rxtec.pitchecking;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.CAMDevice;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.gui.ticketgui.TicketVerifyFrame;
import com.rxtec.pitchecking.mqtt.GatCtrlReceiverBroker;
import com.rxtec.pitchecking.mqtt.GatCtrlSenderBroker;
import com.rxtec.pitchecking.mqtt.MqttReceiverBroker;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.FaceDetectionService;
import com.rxtec.pitchecking.picheckingservice.realsense.RSFaceDetectionService;
import com.rxtec.pitchecking.task.FaceScreenListener;
import com.rxtec.pitchecking.task.GuideScreenListener;
import com.rxtec.pitchecking.task.VerifyScreenListener;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.IDCardUtil;

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
			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {// 双门模式
				TicketVerifyScreen ticketVerifyScreen = TicketVerifyScreen.getInstance();
				TicketVerifyFrame tickFrame = new TicketVerifyFrame();
				ticketVerifyScreen.setTicketFrame(tickFrame);

				tickFrame.showSuccWait("", "系统启动中...");

				try {
					ticketVerifyScreen.initUI(DeviceConfig.getInstance().getTicketScreen());
				} catch (Exception ex) {
					// TODO Auto-generated catch block
					log.error("PITVerifyApp:", ex);
				}

				if (Config.getInstance().getIsResetVerifyScreen() == 1) {
					ScheduledExecutorService screenScheduler = Executors.newScheduledThreadPool(1);
					VerifyScreenListener verifyScreenListener = VerifyScreenListener.getInstance();
					verifyScreenListener.setScreenNo(DeviceConfig.getInstance().getTicketScreen());
					screenScheduler.scheduleWithFixedDelay(verifyScreenListener, 0, 1000, TimeUnit.MILLISECONDS);
				}
			}

			if (Config.getInstance().getIsStartMainListener() == 1) {
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT);
				GatCtrlReceiverBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT); // 启动PITEventTopic本地监听

				DeviceEventListener eventListener = DeviceEventListener.getInstance();

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
			}
		} catch (Exception ex) {
			log.error("", ex);
		}
		
		
		/**
		 * 一下代码为测试代码
		 */
//		int retVal = -1;
//		int x = 0; // 窗口左边界
//		int y = 0; // 窗口顶边界。
//		int cx = 640; // 以像素指定窗口的新的宽度。
//		int cy = 480; // 以像素指定窗口的新的高度。
//		int checkThreshold = 65; // 人脸比对阀值，取值0-100
//		int iCollect = 1; // 摄像头采集图片方式 0:固采 1：预采
//		int faceTimeout = Config.getInstance().getFaceCheckDelayTime(); // 人脸识别算法的超时时间，单位秒
//
//		int[] region = { x, y, cx, cy, checkThreshold, iCollect, faceTimeout };
//		log.debug("开始调用CAM_Open");
//		retVal = CAMDevice.getInstance().CAM_Open(region);
//		log.debug("调用CAM_Open收到返回值");
//		
//		while (true) {
//
//			CommUtil.sleep(3*1000);
//			IDCard idCard = IDCardUtil.createIDCard("zp.jpg");
//			String uuidStr = idCard.getIdNo();
//			String IDPhoto_str = "zp.jpg";
//			log.debug("身份证路径==" + IDPhoto_str);
//			log.debug("CAM_Notify begin");
//			int notifyRet = CAMDevice.getInstance().CAM_Notify(1, uuidStr, IDPhoto_str);
//			int getPhotoRet = -1;
//			if (notifyRet == 0) {
//				int delaySeconds = 10;
//				getPhotoRet = CAMDevice.getInstance().CAM_GetPhotoInfo(uuidStr, delaySeconds); // 此处传入的delaySeconds是为了控制检脸通过速率而设置
//			}
//			log.debug("getphotoRet==" + getPhotoRet);
//
//			if (getPhotoRet == 0) {
//				int displayTimeOut = 3 - 1;
//				CAMDevice.getInstance().CAM_ScreenDisplay("succed#Pass!", displayTimeOut);
//			} else {
//				int displayTimeOut = 5 - 1;
//				CAMDevice.getInstance().CAM_ScreenDisplay("failed#Leave!", displayTimeOut);
//			}
//			CommUtil.sleep(7*1000);
//		}
		
	}
}
