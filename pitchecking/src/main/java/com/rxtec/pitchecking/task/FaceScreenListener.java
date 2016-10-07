package com.rxtec.pitchecking.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.FaceTrackingScreen;
import com.rxtec.pitchecking.device.DeviceConfig;

public class FaceScreenListener implements Runnable {
	private Logger log = LoggerFactory.getLogger("FaceScreenListener");
	private Logger mainlog = LoggerFactory.getLogger("DeviceEventListener");
	private static FaceScreenListener _instance;

	public static synchronized FaceScreenListener getInstance() {
		if (_instance == null) {
			_instance = new FaceScreenListener();
		}
		return _instance;
	}

	private FaceScreenListener() {
		mainlog.info("初始化人脸检测屏位置监控");
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			int frameX = FaceTrackingScreen.getInstance().getFaceFrame().getX();
			// int frameY =
			// FaceTrackingScreen.getInstance().getFaceFrame().getY();
			// log.debug("faceFrame x==" + frameX + ",y==" + frameY);
			if (frameX == 0) {
				log.debug("重置人脸检测屏的位置，恢复至第二块屏");
				FaceTrackingScreen.getInstance().initUI(DeviceConfig.getInstance().getFaceScreen());
				FaceTrackingScreen.getInstance().repainFaceFrame();
			}
		} catch (Exception ex) {
			log.error("repainFaceFrame:", ex);
		}
	}

}
