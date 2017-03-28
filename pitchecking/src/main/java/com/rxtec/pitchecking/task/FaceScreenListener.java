package com.rxtec.pitchecking.task;

import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jacob.com.Variant;
import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.FaceTrackingScreen;
import com.rxtec.pitchecking.SingleFaceTrackingScreen;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.device.LightLevelControl;
import com.rxtec.pitchecking.mbean.ProcessUtil;
import com.rxtec.pitchecking.mqtt.GatCtrlSenderBroker;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.FaceImageLog;
import com.rxtec.pitchecking.utils.CalUtils;
import com.rxtec.pitchecking.utils.ImageLuminanceUtil;

public class FaceScreenListener implements Runnable {
	private Logger log = LoggerFactory.getLogger("FaceScreenListener");
	private Logger mainlog = LoggerFactory.getLogger("DeviceEventListener");
	private Logger luminanceLog = LoggerFactory.getLogger("ImageLuminanceUtil");
	private String pidStr = null;
	private static FaceScreenListener _instance;

	private BufferedImage frameImage = null;

	public static synchronized FaceScreenListener getInstance() {
		if (_instance == null) {
			_instance = new FaceScreenListener();
		}
		return _instance;
	}

	private FaceScreenListener() {
		mainlog.debug("初始化人脸检测屏位置监控");
	}

	public BufferedImage getFrameImage() {
		return frameImage;
	}

	public void setFrameImage(BufferedImage frameImage) {
		this.frameImage = frameImage;
	}

	public String getPidStr() {
		return pidStr;
	}

	public void setPidStr(String pidStr) {
		this.pidStr = pidStr;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if (pidStr == null) {
			log.debug("检脸线程正在启动中,还未开始写心跳,pidStr==" + pidStr + "#");
		} else {
			if (!pidStr.equals("")) {
				if (!FaceCheckingService.getInstance().isFrontCamera()) { // 后置摄像头
					// ProcessUtil.writeHeartbeat(pidStr,
					// Config.getInstance().getHeartBeatLogFile()); //
					// 后置摄像头写心跳日志
					GatCtrlSenderBroker
							.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum())
							.sendMessage(DeviceConfig.EventTopic, DeviceConfig.getInstance().getHeartStr(pidStr, "B"));
				} else {
					// ProcessUtil.writeHeartbeat(pidStr,
					// Config.getInstance().getFrontHeartBeatLogFile()); //
					// 前置摄像头写心跳日志
					GatCtrlSenderBroker
							.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum())
							.sendMessage(DeviceConfig.EventTopic, DeviceConfig.getInstance().getHeartStr(pidStr, "F"));
				}
				this.setPidStr("");
				// log.debug("写检脸心跳日志,pidStr==" + pidStr);
			} else {
				log.debug("检脸线程故障，停止写心跳日志,pidStr==" + pidStr + "#");
			}
		}

		// if (Config.getInstance().getIsUseLuminanceListener() == 1) {
		// if (this.frameImage != null) {
		// float luminanceResult =
		// ImageLuminanceUtil.getInstance().getLuminanceResult(frameImage).getFloat();
		// luminanceLog.debug("Luminance Result = " + luminanceResult);
		//
		//
		// if (Config.getInstance().getIsSaveLuminanceImage() == 1) {
		// int nowTime = Integer.parseInt(CalUtils.getStringTime()); // HHmm
		// if (nowTime >= Integer.parseInt("0700") && nowTime <
		// Integer.parseInt("1000")) { // 08:00<=now<18:00
		// String dirName = Config.getInstance().getImagesLogDir();
		// SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		// dirName += formatter.format(new Date());
		// String luminanceDir = dirName + "/Luminance";
		// FaceImageLog.saveImageFromFrame(luminanceDir, frameImage,
		// luminanceResult);
		// luminanceLog.debug("当前图片保存至" + luminanceDir);
		// }
		// }
		// }
		// }

		int frameX = 0;
		if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {
			frameX = FaceTrackingScreen.getInstance().getFaceFrame().getX();
		} else {
			frameX = SingleFaceTrackingScreen.getInstance().getFaceFrame().getX();
		}

		int screenNo = DeviceConfig.getInstance().getFaceScreen();
		if (FaceCheckingService.getInstance().isFrontCamera()) {
			screenNo = DeviceConfig.getInstance().getGuideScreen();
		}
		if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR && frameX == 0) {
			for (int i = 0; i < 3; i++) {
				try {
					// log.debug("Start 重置人脸检测屏的位置，恢复至第二块屏");
					if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {
						FaceTrackingScreen.getInstance().initUI(screenNo);
						FaceTrackingScreen.getInstance().repainFaceFrame();
					} else {
						SingleFaceTrackingScreen.getInstance().initUI(screenNo);
						SingleFaceTrackingScreen.getInstance().repainSingleVerifyFrame();
					}
				} catch (Exception ex) {
					log.error("重置人脸检测屏的位置失败!再次重置...");
					continue;
				}
				// log.debug("重置人脸检测屏的位置成功!");
				break;
			}
		}

	}

}
