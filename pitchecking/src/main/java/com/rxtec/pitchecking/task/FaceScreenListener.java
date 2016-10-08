package com.rxtec.pitchecking.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.FaceTrackingScreen;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.mbean.ProcessUtil;

public class FaceScreenListener implements Runnable {
	private Logger log = LoggerFactory.getLogger("FaceScreenListener");
	private Logger mainlog = LoggerFactory.getLogger("DeviceEventListener");
	private String pidStr = null;
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
				ProcessUtil.writeHeartbeat(pidStr); // 写心跳日志
				this.setPidStr("");
				// log.debug("写检脸心跳日志,pidStr==" + pidStr);
			} else {
				log.debug("检脸线程故障，停止写心跳日志,pidStr==" + pidStr + "#");
			}
		}

		int frameX = FaceTrackingScreen.getInstance().getFaceFrame().getX();
		if (frameX == 0) {
			for (int i = 0; i < 3; i++) {
				try {
					log.debug("Start 重置人脸检测屏的位置，恢复至第二块屏");
					FaceTrackingScreen.getInstance().initUI(DeviceConfig.getInstance().getFaceScreen());
					FaceTrackingScreen.getInstance().repainFaceFrame();
				} catch (Exception ex) {
					log.error("重置人脸检测屏的位置失败!再次重置...");
					continue;
				}
				log.debug("重置人脸检测屏的位置成功!");
				break;
			}
		}

	}

}
