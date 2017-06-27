package com.rxtec.pitchecking.task;

import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import org.jfree.util.Log;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.FaceImageLog;
import com.rxtec.pitchecking.picheckingservice.realsense.RSFaceDetectionService;
import com.rxtec.pitchecking.picheckingservice.realsense.RealsenseDeviceProperties;
import com.rxtec.pitchecking.utils.CalUtils;
import com.rxtec.pitchecking.utils.ImageLuminanceUtil;
import com.rxtec.pitchecking.utils.ImageToolkit;

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
		// && !FaceCheckingService.getInstance().isFrontCamera()
		if ((Config.getInstance().getIsUseLuminanceListener() == 1
				&& !FaceCheckingService.getInstance().isFrontCamera())
				|| (FaceCheckingService.getInstance().isFrontCamera()
						&& Config.getInstance().getIsUseFrontLuminanceListener() == 1)) {

			BufferedImage frameImage = FaceScreenListener.getInstance().getFrameImage();

			String paras = "0";
			if (FaceCheckingService.getInstance().isFrontCamera()) { // 前置
				paras = Config.getInstance().getFrontLuminancePara();
			} else { // 后置
				paras = Config.getInstance().getBackLuminancePara();
			}

			if (frameImage != null) {
				float luminanceRet = (float) 0.45;
				if (paras.equals("0")) { // 全幅图像
					luminanceRet = ImageLuminanceUtil.getInstance().getLuminanceResult(frameImage).getFloat();
				} else { // 把全幅图像分为九宫格,任意选取其中几张，取平均数
					StringTokenizer st = new StringTokenizer(paras, ",");
					float tempScore = 0;
					int i = 0;
					while (st.hasMoreTokens()) {
						String k = st.nextToken();
						BufferedImage tempImage = ImageToolkit.getImageByNine(frameImage, Integer.parseInt(k));
						tempScore = tempScore
								+ ImageLuminanceUtil.getInstance().getLuminanceResult(tempImage).getFloat();
						i++;
					}

					luminanceRet = tempScore / i;
				}

				luminanceLog.debug("Luminance Result = " + luminanceRet + ",CameraMode = "
						+ rsFaceDetectionService.getCameraMode());

				/**
				 * nowCameraMode -1:默认值,1:普通光照,2:夜晚光照;3:强背光;4:超强背光
				 */

				if (FaceCheckingService.getInstance().isFrontCamera()) { // 前置摄像头
					String nowTime = CalUtils.getStringTime(); // HHmm
					if (Integer.parseInt(nowTime) >= Integer.parseInt(Config.getInstance().getResetCameraLightTime()) && nowTime.substring(2, 4).equals("00")) { // 08:00<=now<18:00
						rsFaceDetectionService.setCameraMode(1);
						RealsenseDeviceProperties rdp = new RealsenseDeviceProperties();
						rdp.setColorAutoExposure(true);
						rdp.setColorAutoWhiteBalance(true);
						rdp.setColorBackLightCompensation(true);
						rdp.setColorExposure(Config.getInstance().getFrontInitColorExposure());
						rdp.setColorBrightness(Config.getInstance().getFrontInitColorBrightness());
						rdp.setContrast(Config.getInstance().getFrontInitContrast());
						rdp.setGain(Config.getInstance().getFrontInitGain());
						rdp.setGamma(Config.getInstance().getFrontInitGamma());
						rsFaceDetectionService.setDeviceProperties(rdp);
						luminanceLog.debug("到了设定时间首先将摄像头设置为普通光模式");
						luminanceLog.debug(Config.getInstance().getInitColorExposure() + "," + Config.getInstance().getInitColorBrightness());
						return;
					}
					
					if (luminanceRet > 0) {						
						if (luminanceRet >= Config.getInstance().getMaxLuminance()) { // 光线高于上限值
							int nowCameraMode = rsFaceDetectionService.getCameraMode();
							if (nowCameraMode == -1 || nowCameraMode == 2) {
								rsFaceDetectionService.setCameraMode(1);
								RealsenseDeviceProperties rdp = new RealsenseDeviceProperties();
								rdp.setColorAutoExposure(true);
								rdp.setColorAutoWhiteBalance(true);
								rdp.setColorBackLightCompensation(true);
								rdp.setColorExposure(Config.getInstance().getFrontInitColorExposure());
								rdp.setColorBrightness(Config.getInstance().getFrontInitColorBrightness());
								rdp.setContrast(Config.getInstance().getFrontInitContrast());
								rdp.setGain(Config.getInstance().getFrontInitGain());
								rdp.setGamma(Config.getInstance().getFrontInitGamma());
								rsFaceDetectionService.setDeviceProperties(rdp);
								luminanceLog.debug("光照度大于" + Config.getInstance().getMaxLuminance() + "，首先将摄像头设置为普通光模式");
								luminanceLog.debug(Config.getInstance().getFrontInitColorExposure() + "," + Config.getInstance().getInitColorBrightness());
								return;
							}
							if (nowCameraMode == 1) {
								rsFaceDetectionService.setCameraMode(3); // 强背光模式
								RealsenseDeviceProperties rdp = new RealsenseDeviceProperties();
								rdp.setColorAutoExposure(true);
								rdp.setColorAutoWhiteBalance(true);
								rdp.setColorBackLightCompensation(true);
								rdp.setColorExposure(Config.getInstance().getFrontBackLightColorExposure());
								rdp.setColorBrightness(Config.getInstance().getFrontBackLightColorBrightness());
								rdp.setContrast(Config.getInstance().getFrontBackLightContrast());
								rdp.setGain(Config.getInstance().getFrontBackLightGain());
								rdp.setGamma(Config.getInstance().getFrontBackLightGamma());
								rsFaceDetectionService.setDeviceProperties(rdp);
								luminanceLog.debug("光照度大于" + Config.getInstance().getMaxLuminance() + "，其次将摄像头设置为强背光模式");
								luminanceLog.debug(Config.getInstance().getFrontBackLightColorExposure() + "," + Config.getInstance().getFrontBackLightColorBrightness());
								return;
							}
							if (nowCameraMode == 3) {
								rsFaceDetectionService.setCameraMode(4); // 超强背光模式
								RealsenseDeviceProperties rdp = new RealsenseDeviceProperties();
								rdp.setColorAutoExposure(true);
								rdp.setColorAutoWhiteBalance(true);
								rdp.setColorBackLightCompensation(true);
								rdp.setColorExposure(Config.getInstance().getFrontSuperBackLightColorExposure());
								rdp.setColorBrightness(Config.getInstance().getFrontSuperBackLightColorBrightness());
								rdp.setContrast(Config.getInstance().getFrontSuperBackLightContrast());
								rdp.setGain(Config.getInstance().getFrontSuperBackLightGain());
								rdp.setGamma(Config.getInstance().getFrontSuperBackLightGamma());
								rsFaceDetectionService.setDeviceProperties(rdp);
								luminanceLog.debug("光照度大于" + Config.getInstance().getMaxLuminance() + "，其次将摄像头设置为超强背光模式");
								luminanceLog.debug(Config.getInstance().getFrontSuperBackLightColorExposure() + "," + Config.getInstance().getFrontSuperBackLightColorBrightness());
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
								rdp.setColorExposure(Config.getInstance().getFrontInitColorExposure());
								rdp.setColorBrightness(Config.getInstance().getFrontInitColorBrightness());
								rdp.setContrast(Config.getInstance().getFrontInitContrast());
								rdp.setGain(Config.getInstance().getFrontInitGain());
								rdp.setGamma(Config.getInstance().getFrontInitGamma());
								rsFaceDetectionService.setDeviceProperties(rdp);
								luminanceLog.debug("光照度小于" + Config.getInstance().getMinLuminance() + "，首先将摄像头设置为普通光模式");
								luminanceLog.debug(Config.getInstance().getFrontInitColorExposure() + "," + Config.getInstance().getFrontInitColorBrightness());
								return;
							}
							if (nowCameraMode != 2) {
								rsFaceDetectionService.setCameraMode(2);
								RealsenseDeviceProperties rdp = new RealsenseDeviceProperties();
								rdp.setColorAutoExposure(true);
								rdp.setColorAutoWhiteBalance(true);
								rdp.setColorBackLightCompensation(true);
								rdp.setColorExposure(Config.getInstance().getFrontNightColorExposure());
								rdp.setColorBrightness(Config.getInstance().getFrontNightColorBrightness());
								rdp.setContrast(Config.getInstance().getFrontNightContrast());
								rdp.setGain(Config.getInstance().getFrontNightGain());
								rdp.setGamma(Config.getInstance().getFrontNightGamma());
								rsFaceDetectionService.setDeviceProperties(rdp);
								luminanceLog.debug("光照度小于" + Config.getInstance().getMinLuminance() + "，其次将摄像头设置为夜晚模式");
								luminanceLog.debug(Config.getInstance().getFrontNightColorExposure() + "," + Config.getInstance().getFrontNightColorBrightness());
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
							// luminanceLog.debug("光照度在" +
							// Config.getInstance().getMinLuminance() + "~~" +
							// Config.getInstance().getMaxLuminance() +
							// "之间，不需要改变模式");
						}
					}
				} else { // 后置摄像头
					String nowTime = CalUtils.getStringTime(); // HHmm
					if (Integer.parseInt(nowTime) >= Integer.parseInt(Config.getInstance().getResetCameraLightTime()) && nowTime.substring(2, 4).equals("00")) { // 08:00<=now<18:00
						rsFaceDetectionService.setCameraMode(1);
						RealsenseDeviceProperties rdp = new RealsenseDeviceProperties();
						rdp.setColorAutoExposure(true);
						rdp.setColorAutoWhiteBalance(true);
						rdp.setColorBackLightCompensation(true);
						rdp.setColorExposure(Config.getInstance().getInitColorExposure());
						rdp.setColorBrightness(Config.getInstance().getInitColorBrightness());
						rdp.setContrast(Config.getInstance().getInitContrast());
						rdp.setGain(Config.getInstance().getInitGain());
						rdp.setGamma(Config.getInstance().getInitGamma());
						rsFaceDetectionService.setDeviceProperties(rdp);
						luminanceLog.debug("到了设定时间首先将摄像头设置为普通光模式");
						luminanceLog.debug(Config.getInstance().getInitColorExposure() + "," + Config.getInstance().getInitColorBrightness());
						return;
					}
					
					
					if (luminanceRet > 0) {						
						if (luminanceRet >= Config.getInstance().getMaxLuminance()) { // 光线高于上限值
							int nowCameraMode = rsFaceDetectionService.getCameraMode();
							if (nowCameraMode == -1 || nowCameraMode == 2) {
								rsFaceDetectionService.setCameraMode(1);
								RealsenseDeviceProperties rdp = new RealsenseDeviceProperties();
								rdp.setColorAutoExposure(true);
								rdp.setColorAutoWhiteBalance(true);
								rdp.setColorBackLightCompensation(true);
								rdp.setColorExposure(Config.getInstance().getInitColorExposure());
								rdp.setColorBrightness(Config.getInstance().getInitColorBrightness());
								rdp.setContrast(Config.getInstance().getInitContrast());
								rdp.setGain(Config.getInstance().getInitGain());
								rdp.setGamma(Config.getInstance().getInitGamma());
								rsFaceDetectionService.setDeviceProperties(rdp);
								luminanceLog.debug("光照度大于" + Config.getInstance().getMaxLuminance() + "，首先将摄像头设置为普通光模式");
								luminanceLog.debug(Config.getInstance().getInitColorExposure() + "," + Config.getInstance().getInitColorBrightness());
								return;
							}
							if (nowCameraMode == 1) {
								rsFaceDetectionService.setCameraMode(3); // 强背光模式
								RealsenseDeviceProperties rdp = new RealsenseDeviceProperties();
								rdp.setColorAutoExposure(true);
								rdp.setColorAutoWhiteBalance(true);
								rdp.setColorBackLightCompensation(true);
								rdp.setColorExposure(Config.getInstance().getBackLightColorExposure());
								rdp.setColorBrightness(Config.getInstance().getBackLightColorBrightness());
								rdp.setContrast(Config.getInstance().getBackLightContrast());
								rdp.setGain(Config.getInstance().getBackLightGain());
								rdp.setGamma(Config.getInstance().getBackLightGamma());
								rsFaceDetectionService.setDeviceProperties(rdp);
								luminanceLog.debug("光照度大于" + Config.getInstance().getMaxLuminance() + "，其次将摄像头设置为强背光模式");
								luminanceLog.debug(Config.getInstance().getBackLightColorExposure() + "," + Config.getInstance().getBackLightColorBrightness());
								return;
							}
							if (nowCameraMode == 3) {
								rsFaceDetectionService.setCameraMode(4); // 超级强背光模式
								RealsenseDeviceProperties rdp = new RealsenseDeviceProperties();
								rdp.setColorAutoExposure(true);
								rdp.setColorAutoWhiteBalance(true);
								rdp.setColorBackLightCompensation(true);
								rdp.setColorExposure(Config.getInstance().getSuperBackLightColorExposure());
								rdp.setColorBrightness(Config.getInstance().getSuperBackLightColorBrightness());
								rdp.setContrast(Config.getInstance().getSuperBackLightContrast());
								rdp.setGain(Config.getInstance().getSuperBackLightGain());
								rdp.setGamma(Config.getInstance().getSuperBackLightGamma());
								rsFaceDetectionService.setDeviceProperties(rdp);
								luminanceLog.debug("光照度大于" + Config.getInstance().getMaxLuminance() + "，其次将摄像头设置为超强背光模式");
								luminanceLog.debug(Config.getInstance().getSuperBackLightColorExposure() + "," + Config.getInstance().getSuperBackLightColorBrightness());
								return;
							}
							if (nowCameraMode == 4) {
								rsFaceDetectionService.setCameraMode(5); // 超级强背光模式 +1级
								RealsenseDeviceProperties rdp = new RealsenseDeviceProperties();
								rdp.setColorAutoExposure(true);
								rdp.setColorAutoWhiteBalance(true);
								rdp.setColorBackLightCompensation(true);
								rdp.setColorExposure(Config.getInstance().getSuperAdd1BackLightColorExposure());
								rdp.setColorBrightness(Config.getInstance().getSuperAdd1BackLightColorBrightness() - 20);
								rdp.setContrast(Config.getInstance().getSuperAdd1BackLightContrast());
								rdp.setGain(Config.getInstance().getSuperAdd1BackLightGain());
								rdp.setGamma(Config.getInstance().getSuperAdd1BackLightGamma() - 50);
								rsFaceDetectionService.setDeviceProperties(rdp);
								luminanceLog.debug("光照度大于" + Config.getInstance().getMaxLuminance() + "，其次将摄像头设置为+1级超强背光模式");
								luminanceLog.debug(Config.getInstance().getSuperAdd1BackLightColorExposure() + "," + Config.getInstance().getSuperAdd1BackLightColorBrightness());
								return;
							}
							if (nowCameraMode == 5) {
								rsFaceDetectionService.setCameraMode(6); // 超级强背光模式 +2级
								RealsenseDeviceProperties rdp = new RealsenseDeviceProperties();
								rdp.setColorAutoExposure(true);
								rdp.setColorAutoWhiteBalance(true);
								rdp.setColorBackLightCompensation(true);
								rdp.setColorExposure(Config.getInstance().getSuperAdd2BackLightColorExposure());
								rdp.setColorBrightness(Config.getInstance().getSuperAdd2BackLightColorBrightness() - 10);
								rdp.setContrast(Config.getInstance().getSuperAdd2BackLightContrast());
								rdp.setGain(Config.getInstance().getSuperAdd2BackLightGain());
								rdp.setGamma(Config.getInstance().getSuperAdd2BackLightGamma() - 50);
								rsFaceDetectionService.setDeviceProperties(rdp);
								luminanceLog.debug("光照度大于" + Config.getInstance().getMaxLuminance() + "，其次将摄像头设置为+2级超强背光模式");
								luminanceLog.debug(Config.getInstance().getSuperAdd2BackLightColorExposure() + "," + Config.getInstance().getSuperAdd2BackLightColorBrightness());
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
								rdp.setGamma(Config.getInstance().getInitGamma());
								rsFaceDetectionService.setDeviceProperties(rdp);
								luminanceLog.debug("光照度小于" + Config.getInstance().getMinLuminance() + "，首先将摄像头设置为普通光模式");
								luminanceLog.debug(Config.getInstance().getInitColorExposure() + "," + Config.getInstance().getInitColorBrightness());
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
								rdp.setGamma(Config.getInstance().getNightGamma());
								rsFaceDetectionService.setDeviceProperties(rdp);
								luminanceLog.debug("光照度小于" + Config.getInstance().getMinLuminance() + "，其次将摄像头设置为夜晚模式");
								luminanceLog.debug(Config.getInstance().getNightColorExposure() + "," + Config.getInstance().getNightColorBrightness());
								return;
							}
						}
						if (luminanceRet >= Config.getInstance().getMinLuminance() && luminanceRet < Config.getInstance().getMaxLuminance()) {

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
							// luminanceLog.debug("光照度在" +
							// Config.getInstance().getMinLuminance() + "~~" +
							// Config.getInstance().getMaxLuminance() +
							// "之间，不需要改变模式");
						}
					}
				}
				
				
				

				/**
				 * 
				 */
				if (Config.getInstance().getIsSaveLuminanceImage() == 1) {
					int nowTime = Integer.parseInt(CalUtils.getStringTime()); // HHmm
					if (nowTime >= Integer.parseInt("0700") && nowTime < Integer.parseInt("2300")) { // 08:00<=now<18:00
						String dirName = Config.getInstance().getImagesLogDir();
						SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
						dirName += formatter.format(new Date());
						String luminanceDir = dirName + "/Luminance";
						FaceImageLog.saveImageFromFrame(luminanceDir, frameImage, luminanceRet);
						// luminanceLog.debug("当前图片保存至" + luminanceDir);
					}
				}
			}
		}
	}

}
