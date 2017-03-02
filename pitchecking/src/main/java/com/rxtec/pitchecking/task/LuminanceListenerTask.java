package com.rxtec.pitchecking.task;

import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.picheckingservice.FaceImageLog;
import com.rxtec.pitchecking.picheckingservice.realsense.RSFaceDetectionService;
import com.rxtec.pitchecking.picheckingservice.realsense.RealsenseDeviceProperties;
import com.rxtec.pitchecking.utils.CalUtils;
import com.rxtec.pitchecking.utils.ImageLuminanceUtil;

public class LuminanceListenerTask implements Job {
	private Logger mainlog = LoggerFactory.getLogger("DeviceEventListener");
	private Logger luminanceLog = LoggerFactory.getLogger("ImageLuminanceUtil");
	private RSFaceDetectionService rsFaceDetectionService;

	public LuminanceListenerTask() {
		rsFaceDetectionService = RSFaceDetectionService.getInstance();
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		if (Config.getInstance().getIsUseLuminanceListener() == 1) {
			BufferedImage frameImage = FaceScreenListener.getInstance().getFrameImage();
			if (frameImage != null) {
				float luminanceRet = ImageLuminanceUtil.getInstance().getLuminanceResult(frameImage).getFloat();
//				luminanceLog.debug("Luminance Result = " + luminanceRet + ",getCameraMode==" + rsFaceDetectionService.getCameraMode());
				if (luminanceRet > 0) {
					if (luminanceRet >= Config.getInstance().getMaxLuminance()) { // 光线高于上限值
						int nowCameraMode = rsFaceDetectionService.getCameraMode();
						if (nowCameraMode != 1) {
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
//							luminanceLog.debug("光照度大于" + Config.getInstance().getMaxLuminance() + "，首先将摄像头设置为普通光模式");
							return;
						}
						if (nowCameraMode != 3) {
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
//							luminanceLog.debug("光照度大于" + Config.getInstance().getMaxLuminance() + "，其次将摄像头设置为强背光模式");
							return;
						}
					}
					if (luminanceRet < Config.getInstance().getMinLuminance()) { // 光线低于下限值
						int nowCameraMode = rsFaceDetectionService.getCameraMode();
						if (nowCameraMode != 1) {
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
//							luminanceLog.debug("光照度小于" + Config.getInstance().getMinLuminance() + "，首先将摄像头设置为普通光模式");
							return;
						}
						if (nowCameraMode != 2) {
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
//							luminanceLog.debug("光照度小于" + Config.getInstance().getMinLuminance() + "，其次将摄像头设置为夜晚模式");
							return;
						}
					}
					if (luminanceRet >= Config.getInstance().getMinLuminance()
							&& luminanceRet < Config.getInstance().getMaxLuminance()) {

						// rsFaceDetectionService.setCameraMode(1);
						// RealsenseDeviceProperties rdp = new
						// RealsenseDeviceProperties();
						// rdp.setColorAutoExposure(true);
						// rdp.setColorAutoWhiteBalance(true);
						// rdp.setColorBackLightCompensation(true);
						// rdp.setColorExposure(Config.getInstance().getInitColorExposure());
						// rdp.setColorBrightness(Config.getInstance().getInitColorBrightness());
						// rdp.setContrast(Config.getInstance().getInitContrast());
						// rdp.setGain(Config.getInstance().getInitGain());
						// rsFaceDetectionService.setDeviceProperties(rdp);
//						luminanceLog.debug("光照度在" + Config.getInstance().getMinLuminance() + "~~"	+ Config.getInstance().getMaxLuminance() + "之间，不需要改变模式");
					}
				}

				/**
				 * 
				 */
				if (Config.getInstance().getIsSaveLuminanceImage() == 1) {
					int nowTime = Integer.parseInt(CalUtils.getStringTime()); // HHmm
					if (nowTime >= Integer.parseInt("0700") && nowTime < Integer.parseInt("1000")) { // 08:00<=now<18:00
						String dirName = Config.getInstance().getImagesLogDir();
						SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
						dirName += formatter.format(new Date());
						String luminanceDir = dirName + "/Luminance";
						FaceImageLog.saveImageFromFrame(luminanceDir, frameImage, luminanceRet);
//						luminanceLog.debug("当前图片保存至" + luminanceDir);
					}
				}
			}
		}
	}

}
