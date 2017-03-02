package com.rxtec.pitchecking.task;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.device.LightLevelControl;
import com.rxtec.pitchecking.picheckingservice.realsense.RSFaceDetectionService;
import com.rxtec.pitchecking.picheckingservice.realsense.RealsenseDeviceProperties;

public class ReadLightLevelTask implements Job {
	private Logger log = LoggerFactory.getLogger("ReadLightLevelTask");
	private LightLevelControl lightLevelControl;
	private RSFaceDetectionService rsFaceDetectionService;

	public ReadLightLevelTask() {
		// TODO Auto-generated constructor stub
		lightLevelControl = LightLevelControl.getInstance();
		rsFaceDetectionService = RSFaceDetectionService.getInstance();
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		if (Config.getInstance().getIsReadLightLevel() == 1) {
			String lightLevelStr = lightLevelControl.GetGZD();
			log.debug("当前光照度数值==" + lightLevelStr + ",cameraMode==" + rsFaceDetectionService.getCameraMode());
			int lightLevel = Integer.parseInt(lightLevelStr);
			if (lightLevel > 0) {
				if (lightLevel >= 2400 && rsFaceDetectionService.getCameraMode() != 3) {
					rsFaceDetectionService.setCameraMode(3); // 强背光模式
					RealsenseDeviceProperties rdp = new RealsenseDeviceProperties();
					rdp.setColorAutoExposure(true);
					rdp.setColorAutoWhiteBalance(true);
					rdp.setColorBackLightCompensation(true);					
					rdp.setColorExposure(Config.getInstance().getBackLightColorExposure());
					rdp.setColorBrightness(Config.getInstance().getBackLightColorBrightness());
					rdp.setContrast(Config.getInstance().getBackLightContrast());
					rdp.setGain(Config.getInstance().getBackLightGain());
					
					rsFaceDetectionService.setDeviceProperties(rdp);
					log.debug("光照度大于2400，将摄像头设置为强背光模式");
				}
				if (lightLevel < 300 && rsFaceDetectionService.getCameraMode() != 2) {
					rsFaceDetectionService.setCameraMode(2);
					RealsenseDeviceProperties rdp = new RealsenseDeviceProperties();
					rdp.setColorAutoExposure(true);
					rdp.setColorAutoWhiteBalance(true);
					rdp.setColorBackLightCompensation(true);
					rdp.setColorExposure(Config.getInstance().getNightColorExposure());
					rdp.setColorBrightness(Config.getInstance().getNightColorBrightness());
					rdp.setContrast(Config.getInstance().getNightContrast());
					rdp.setGain(Config.getInstance().getNightGain());
					
					rsFaceDetectionService.setDeviceProperties(rdp);
					log.debug("光照度小于300，将摄像头设置为夜晚模式");
				}
				if (lightLevel >= 300 && lightLevel < 2400 && rsFaceDetectionService.getCameraMode() != 1) {
					rsFaceDetectionService.setCameraMode(1);
					RealsenseDeviceProperties rdp = new RealsenseDeviceProperties();
					rdp.setColorAutoExposure(true);
					rdp.setColorAutoWhiteBalance(true);
					rdp.setColorBackLightCompensation(true);
					rdp.setColorExposure(Config.getInstance().getInitColorExposure());
					rdp.setColorBrightness(Config.getInstance().getInitColorBrightness());
					rdp.setContrast(Config.getInstance().getInitContrast());
					rdp.setGain(Config.getInstance().getInitGain());
					
					rsFaceDetectionService.setDeviceProperties(rdp);
					log.debug("光照度在300~2400之间，将摄像头设置为普通光照模式");
				}
			}
		}

	}

}
