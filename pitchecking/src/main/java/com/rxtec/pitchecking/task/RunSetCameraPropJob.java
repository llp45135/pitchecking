package com.rxtec.pitchecking.task;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.picheckingservice.realsense.RSFaceDetectionService;
import com.rxtec.pitchecking.picheckingservice.realsense.RealsenseDeviceProperties;
import com.rxtec.pitchecking.utils.CalUtils;

public class RunSetCameraPropJob implements Job {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");

	public RunSetCameraPropJob() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(JobExecutionContext jobContext) throws JobExecutionException {
		// TODO Auto-generated method stub
		log.debug("定时设置摄像头参数任务,开始执行！ By " + jobContext.getJobDetail().getJobClass());
		int dayCameraTime = Integer.parseInt(Config.getInstance().getDayCameraTime());
		int nightCameraTime = Integer.parseInt(Config.getInstance().getNightCameraTime());
		int nowTime = Integer.parseInt(CalUtils.getStringTime()); // HHmm

		if (nowTime >= dayCameraTime && nowTime < nightCameraTime) { // 08:00<=now<18:00
			log.debug("当前设置为白天模式");
			RealsenseDeviceProperties rdp = new RealsenseDeviceProperties();
//			rdp.setColorAutoExposure(cameraColorBean.getColorautoexposure());
//			rdp.setColorAutoWhiteBalance(cameraColorBean.getColorautowhitebalance());
//			rdp.setColorBackLightCompensation(cameraColorBean.getColorbacklightcompensation());
			rdp.setColorBrightness(Config.getInstance().getInitColorBrightness());
			rdp.setColorExposure(Config.getInstance().getInitColorExposure());
//			rdp.setSR300_HDR(cameraColorBean.getHdr());
			rdp.setContrast(Config.getInstance().getInitContrast());
//			rdp.setGamma(cameraColorBean.getGamma());
			rdp.setGain(Config.getInstance().getInitGain());
			RSFaceDetectionService.getInstance().setDeviceProperties(rdp);
			log.debug("InitColorExposure=="+Config.getInstance().getInitColorExposure()+",InitColorBrightness=="+Config.getInstance().getInitColorBrightness());
			log.debug("InitContrast=="+Config.getInstance().getInitContrast()+",InitGain=="+Config.getInstance().getInitGain());
		} else {
			log.debug("当前设置为夜晚模式");
			RealsenseDeviceProperties rdp = new RealsenseDeviceProperties();
//			rdp.setColorAutoExposure(cameraColorBean.getColorautoexposure());
//			rdp.setColorAutoWhiteBalance(cameraColorBean.getColorautowhitebalance());
//			rdp.setColorBackLightCompensation(cameraColorBean.getColorbacklightcompensation());
			rdp.setColorBrightness(Config.getInstance().getNightColorBrightness());
			rdp.setColorExposure(Config.getInstance().getNightColorExposure());
//			rdp.setSR300_HDR(cameraColorBean.getHdr());
			rdp.setContrast(Config.getInstance().getNightContrast());
//			rdp.setGamma(cameraColorBean.getGamma());
			rdp.setGain(Config.getInstance().getNightGain());
			RSFaceDetectionService.getInstance().setDeviceProperties(rdp);
			log.debug("NightColorExposure=="+Config.getInstance().getNightColorExposure()+",NightColorBrightness=="+Config.getInstance().getNightColorBrightness());
			log.debug("NightContrast=="+Config.getInstance().getNightContrast()+",NightGain=="+Config.getInstance().getNightGain());
		}
	}

}
