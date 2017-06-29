package com.rxtec.pitchecking.picheckingservice.realsense;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.device.smartmonitor.MonitorXMLUtil;
import com.rxtec.pitchecking.gui.VideoPanel;
import com.rxtec.pitchecking.mbean.ProcessUtil;
import com.rxtec.pitchecking.mq.RemoteMonitorPublisher;
import com.rxtec.pitchecking.mqtt.GatCtrlSenderBroker;
import com.rxtec.pitchecking.mqtt.pitevent.PTVerifySender;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.PITData;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
import com.rxtec.pitchecking.socket.pitevent.FaceSender;
import com.rxtec.pitchecking.task.FaceScreenListener;
import com.rxtec.pitchecking.utils.CalUtils;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.IDCardUtil;
import com.rxtec.pitchecking.utils.ImageToolkit;

import intel.rssdk.PXCMBase;
import intel.rssdk.PXCMCapture;
import intel.rssdk.PXCMFaceConfiguration;
import intel.rssdk.PXCMFaceData;
import intel.rssdk.PXCMFaceData.Face;
import intel.rssdk.PXCMFaceData.LandmarkPoint;
import intel.rssdk.PXCMFaceData.LandmarkType;
import intel.rssdk.PXCMFaceData.LandmarksData;
import intel.rssdk.PXCMFaceData.LandmarksGroupType;
import intel.rssdk.PXCMFaceModule;
import intel.rssdk.PXCMImage;
import intel.rssdk.PXCMImage.Option;
import intel.rssdk.PXCMPowerState;
import intel.rssdk.PXCMRectI32;
import intel.rssdk.PXCMSenseManager;
import intel.rssdk.PXCMSession;
import intel.rssdk.PXCMVideoModule;
import intel.rssdk.pxcmStatus;
import intel.rssdk.PXCMCapture.Device;
import intel.rssdk.PXCMCapture.Device.PropertyInfo;
import intel.rssdk.PXCMCapture.Sample;
import intel.rssdk.PXCMCaptureManager;

/**
 * 后置摄像头检脸线程
 * 
 * @author ZhaoLin
 *
 */
public class RSFaceTrackTask implements Runnable {

	static int cWidth = Config.FrameWidth;
	static int cHeight = Config.FrameHeigh;
	static int frameRate = Config.frameRate;
	static int dWidth, dHeight;
	static boolean exit = false;
	private Logger log = LoggerFactory.getLogger("RSFaceTrackTask");
	private Logger cameraLog = LoggerFactory.getLogger("CameraDevInfo");
	private VideoPanel videoPanel = null;
	private boolean exist = false;
	private boolean startCapture = true;
	private boolean startTrackFace = true;
	private boolean enableExpression = true;
	private boolean enableFaceLandmark = true;
	private IDCard currentIDCard = null;
	private Ticket currentTicket = null;
	private String pid = "";
	private PXCMCapture.Device dev;
	private static int LandmarkAlignment = 10;

	// private RSModuleStatus rsStatus;

	public VideoPanel getVideoPanel() {
		return videoPanel;
	}

	public void setVideoPanel(JPanel vp) {
		this.videoPanel = (VideoPanel) vp;
	}

	public boolean isStartCapture() {
		return startCapture;
	}

	public void setStartCapture(boolean startCapture) {
		this.startCapture = startCapture;
	}

	public boolean isEnableExpression() {
		return enableExpression;
	}

	public void setEnableExpression(boolean enableExpression) {
		this.enableExpression = enableExpression;
	}

	public boolean isEnableFaceLandmark() {
		return enableFaceLandmark;
	}

	public void setEnableFaceLandmark(boolean enableFaceLandmark) {
		this.enableFaceLandmark = enableFaceLandmark;
	}

	public boolean isStartTrackFace() {
		return startTrackFace;
	}

	public void setStartTrackFace(boolean startTrackFace) {
		this.startTrackFace = startTrackFace;
	}

	public boolean isExist() {
		return exist;
	}

	public void setExist(boolean exist) {
		this.exist = exist;
	}

	public RSFaceTrackTask(VideoPanel vp) {
		videoPanel = vp;
	}

	@Override
	public void run() {
		try {
			doTracking();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("RSFaceTrackTask doTracking:", e);
		}

	}

	/**
	 * 
	 * @param frame
	 * @param detection
	 * @return
	 */
	private PITData createFaceData(BufferedImage frame, PXCMFaceData.DetectionData detection) {
		if (frame == null)
			return null;
		PXCMRectI32 rect = new PXCMRectI32();
		PITData fd = new PITData(frame);
		boolean ret = detection.QueryBoundingRect(rect);
		// log.debug("detection.QueryBoundingRect(rect)==" + ret + ",rect==" +
		// rect);
		if (ret) {
			int x, y, w, h;
			float xa = (rect.w * Config.getInstance().getFaceDetectionScale() - rect.w) / 2;
			x = rect.x - (int) xa;
			float ya = (rect.h * Config.getInstance().getFaceDetectionScale() - rect.h) / 2;
			y = rect.y - (int) ya;

			w = (int) (rect.w * Config.getInstance().getFaceDetectionScale());
			h = (int) (rect.h * Config.getInstance().getFaceDetectionScale());

			if (x < 0)
				x = 0;
			if (y < 0)
				y = 0;
			if ((x + w) > Config.FrameWidth)
				x = Config.FrameWidth - w;
			if ((y + h) > Config.FrameHeigh)
				y = Config.FrameHeigh - h;

			if (Config.getInstance().getFaceTrackMode() == Config.FACE_TRACK_IR) {
				x = x * Config.FrameWidth / Config.IRFrameWidth;
				y = y * Config.FrameHeigh / Config.IRFrameHeigh;
				w = w * Config.FrameWidth / Config.IRFrameWidth;
				h = h * Config.FrameHeigh / Config.IRFrameHeigh;
				ya = (float) ((h * 1.3 - h) / 2);
				y = (int) (y - ya);
				h = (int) (h * 1.3);
			}

			if (frame.getWidth() < (x + w) || frame.getHeight() < (y + h)) {
				if (frame.getWidth() < (x + w)) {
					w = frame.getWidth() - x;
				}
				if (frame.getHeight() < (y + h)) {
					h = frame.getHeight() - y;
				}
				try {
					BufferedImage faceImage = frame.getSubimage(x, y, w, h);
					fd.setFaceImage(faceImage);
					fd.getFaceLocation().setLocation(rect.x, rect.y, rect.w, rect.h);
					fd.setDetectedFace(true);
				} catch (Exception e) {
					log.error("createFaceData", e);
					fd.setDetectedFace(false);

				}
			} else {
				try {
					BufferedImage faceImage = frame.getSubimage(x, y, w, h);
					fd.setFaceImage(faceImage);
					fd.getFaceLocation().setLocation(rect.x, rect.y, rect.w, rect.h);
					fd.setDetectedFace(true);
				} catch (Exception e) {
					log.error("createFaceData", e);
					fd.setDetectedFace(false);
				}
			}
		}
		return fd;
	}

	/**
	 * 画脸部框
	 * 
	 * @param detection
	 */
	private void drawLocation(PXCMFaceData.DetectionData detection) {
		if (detection == null)
			return;
		PXCMRectI32 rect = new PXCMRectI32();
		boolean ret = detection.QueryBoundingRect(rect);
		if (ret) {
			Graphics2D g = (Graphics2D) videoPanel.getGraphics();
			float thick = 2.0f;
			g.setColor(Color.GREEN);
			g.setStroke(new BasicStroke(thick, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));

			if (Config.getInstance().getFaceTrackMode() == Config.FACE_TRACK_IR) {
				g.drawRect(rect.x * Config.FrameWidth / Config.IRFrameWidth, rect.y * Config.FrameHeigh / Config.IRFrameHeigh,
						rect.w * Config.FrameWidth / Config.IRFrameWidth, rect.h * Config.FrameHeigh / Config.IRFrameHeigh);
			} else {
				g.drawRect(rect.x, rect.y, rect.w, rect.h);
			}
			g.dispose();
		}
	}

	/**
	 * 画脸部特征点
	 * 
	 * @param face
	 */
	private void drawLandmark(PXCMFaceData.Face face) {
		if (face == null)
			return;
		PXCMFaceData.LandmarksData landmarks = face.QueryLandmarks();
		if (landmarks == null)
			return;
		int npoints = landmarks.QueryNumPoints();
		PXCMFaceData.LandmarkPoint[] points = new PXCMFaceData.LandmarkPoint[npoints];
		for (int i = 0; i < npoints; i++) {
			points[i] = new LandmarkPoint();
		}
		landmarks.QueryPoints(points);

		Point point = new Point();
		Graphics2D graphics = (Graphics2D) videoPanel.getGraphics();
		for (PXCMFaceData.LandmarkPoint landmark : points) {
			if (landmark == null)
				continue;
			point.x = (int) (landmark.image.x + LandmarkAlignment);
			point.y = ((int) (landmark.image.y + LandmarkAlignment)) - 10;

			// log.debug("landmark.confidenceImage=" + landmark.confidenceImage
			// +" landmark.confidenceWorld=" + landmark.confidenceWorld );
			if (landmark.confidenceWorld == 0) {
				graphics.setColor(Color.RED);
				if (Config.getInstance().getFaceTrackMode() == Config.FACE_TRACK_IR)
					graphics.drawString("x", point.x * Config.FrameWidth / Config.IRFrameWidth, point.y * Config.FrameHeigh / Config.IRFrameHeigh);
				else {
					graphics.drawString("x", point.x, point.y);

				}
			} else {
				graphics.setColor(Color.YELLOW);
				if (Config.getInstance().getFaceTrackMode() == Config.FACE_TRACK_IR)
					graphics.drawString("x", point.x * Config.FrameWidth / Config.IRFrameWidth, point.y * Config.FrameHeigh / Config.IRFrameHeigh);
				else
					graphics.drawString("x", point.x, point.y);

			}

			// log.debug("landmark :" + landmark.source.alias +" z=" +
			// landmark.world.z);
		}

		graphics.dispose();
		landmarks = null;
	}

	/**
	 * 
	 * @param landmarks
	 */
	private void printFaceLandmarkZ(LandmarksData landmarks) {
		int noseTipPointIdx = landmarks.QueryPointIndex(LandmarkType.LANDMARK_NOSE_TIP);
		int noseBottomPointIdx = landmarks.QueryPointIndex(LandmarkType.LANDMARK_NOSE_BOTTOM);
		int noseLeftPointIdx = landmarks.QueryPointIndex(LandmarkType.LANDMARK_NOSE_LEFT);
		int noseRightPointIdx = landmarks.QueryPointIndex(LandmarkType.LANDMARK_NOSE_RIGHT);
		int eyeRightPointIdx = landmarks.QueryPointIndex(LandmarkType.LANDMARK_EYE_RIGHT_CENTER);
		int eyeLeftPointIdx = landmarks.QueryPointIndex(LandmarkType.LANDMARK_EYE_LEFT_CENTER);

		int faceRightPointIdx = landmarks.QueryPointIndex(LandmarkType.LANDMARK_FACE_BORDER_TOP_RIGHT);
		int faceLeftPointIdx = landmarks.QueryPointIndex(LandmarkType.LANDMARK_FACE_BORDER_TOP_LEFT);
		int chinPointIdx = landmarks.QueryPointIndex(LandmarkType.LANDMARK_CHIN);

		// LandmarkPoint noseTipPoint = new LandmarkPoint();
		// float noseTipPointZ = 0f;
		// if(landmarks.QueryPoint(noseTipPointIdx, noseTipPoint)){
		// noseTipPointZ = noseTipPoint.world.z*1000;
		// log.debug("noseTipPoint confidenceWorld=" +
		// noseTipPoint.confidenceWorld + " Z="+noseTipPointZ );
		// }
		//
		// float noseBottomPointZ = 0f;
		// LandmarkPoint noseBottomPoint = new LandmarkPoint();
		// if(landmarks.QueryPoint(noseBottomPointIdx, noseBottomPoint)){
		// noseBottomPointZ = noseBottomPoint.world.z*1000;
		// log.debug("noseBottomPoint confidenceWorld=" +
		// noseBottomPoint.confidenceWorld + " Z="+noseBottomPointZ );
		// }
		//
		// float noseRightPointZ = 0f;
		// LandmarkPoint noseRightPoint = new LandmarkPoint();
		// if(landmarks.QueryPoint(noseRightPointIdx, noseRightPoint)){
		// noseBottomPointZ = noseRightPoint.world.z*1000;
		// log.debug("noseRightPoint confidenceWorld=" +
		// noseRightPoint.confidenceWorld + " Z="+noseRightPointZ );
		// }
		//
		// float noseLeftPointZ = 0f;
		// LandmarkPoint noseLeftPoint = new LandmarkPoint();
		// if(landmarks.QueryPoint(noseLeftPointIdx, noseLeftPoint)){
		// noseLeftPointZ = noseLeftPoint.world.z*1000;
		// log.debug("noseLeftPoint confidenceWorld=" +
		// noseLeftPoint.confidenceWorld + " Z="+noseLeftPointZ );
		// }
		//
		// float eyeLeftPointZ = 0f;
		// LandmarkPoint eyeLeftPoint = new LandmarkPoint();
		// if(landmarks.QueryPoint(eyeLeftPointIdx, eyeLeftPoint)){
		// eyeLeftPointZ = eyeLeftPoint.world.z*1000;
		// log.debug("eyeLeftPoint confidenceWorld=" +
		// eyeLeftPoint.confidenceWorld + " Z="+eyeLeftPointZ );
		// }
		//
		// float eyeRightPointZ = 0f;
		// LandmarkPoint eyeRightPoint = new LandmarkPoint();
		// if(landmarks.QueryPoint(eyeRightPointIdx, eyeRightPoint)){
		// eyeRightPointZ = eyeRightPoint.world.z*1000;
		// log.debug("eyeRightPoint confidenceWorld=" +
		// eyeRightPoint.confidenceWorld + " Z="+eyeRightPointZ );
		// }

		float faceLeftPointX = 0f;
		float faceLeftPointY = 0f;

		LandmarkPoint faceLeftPoint = new LandmarkPoint();
		if (landmarks.QueryPoint(faceLeftPointIdx, faceLeftPoint)) {
			faceLeftPointX = faceLeftPoint.world.x;
			faceLeftPointY = faceLeftPoint.world.y;
			log.debug("faceLeftPoint confidenceWorld=" + faceLeftPoint.confidenceWorld + " X=" + faceLeftPointX + " Y=" + faceLeftPointY);
		}

		float faceRightPointX = 0f;
		float faceRightPointY = 0f;

		LandmarkPoint faceRightPoint = new LandmarkPoint();
		if (landmarks.QueryPoint(faceRightPointIdx, faceRightPoint)) {
			faceRightPointX = faceRightPoint.world.z;
			faceRightPointY = faceRightPoint.world.y;
			log.debug("faceRightPoint confidenceWorld=" + faceRightPoint.confidenceWorld + " X=" + faceRightPointX + " Y=" + faceRightPointY);
		}

		float chinPointX = 0f;
		float chinPointY = 0f;

		LandmarkPoint chinPoint = new LandmarkPoint();
		if (landmarks.QueryPoint(chinPointIdx, chinPoint)) {
			chinPointX = chinPoint.world.z;
			chinPointY = chinPoint.world.y;
			log.debug("chinPoint confidenceWorld=" + chinPoint.confidenceWorld + " Y=" + chinPointY);
		}

		// log.debug("zDIF = " + Math.abs(eyeRightPointZ - noseTipPointZ));

		float faceHight = Math.abs(chinPointY) + Math.abs(faceRightPointY);
		float faceWidth = Math.abs(faceRightPointX) + Math.abs(faceLeftPointX);
		float faceArea = faceHight * faceWidth * 1000;
		log.debug("faceArea = " + faceArea);

	}

	/**
	 * 
	 * @param sample
	 * @return
	 */
	private BufferedImage drawFrameImage(PXCMCapture.Sample sample) {

		PXCMImage.ImageData cData = new PXCMImage.ImageData();

		pxcmStatus sts = sample.color.AcquireAccess(PXCMImage.Access.ACCESS_READ, PXCMImage.PixelFormat.PIXEL_FORMAT_RGB32,
				// PXCMImage.Rotation.ROTATION_90_DEGREE,
				cData);

		if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) < 0) {
			log.error("Failed to AcquireAccess of color image data");
			return null;
		}
		int cBuff[] = new int[cData.pitches[0] / 4 * cHeight];
		cData.ToIntArray(0, cBuff);
		videoPanel.image.setRGB(0, 0, cWidth, cHeight, cBuff, 0, cData.pitches[0] / 4);

		videoPanel.paintImg();
		sts = sample.color.ReleaseAccess(cData);
		if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) < 0) {
			log.error("Failed to ReleaseAccess of color image data");
		}

		return videoPanel.image;
	}

	/**
	 * 
	 * @param poseData
	 * @return
	 */
	private PXCMFaceData.PoseEulerAngles checkFacePose(PXCMFaceData.PoseData poseData) {
		PXCMFaceData.PoseEulerAngles pea = new PXCMFaceData.PoseEulerAngles();
		poseData.QueryPoseAngles(pea);
		// log.debug("Confidence = " + poseData.QueryConfidence());
		// log.debug("Roll=" + Math.abs(pea.roll) + " Pitch=" +
		// Math.abs(pea.pitch) + " Yaw" + Math.abs(pea.yaw));
		if (poseData.QueryConfidence() == 0)
			return null;
		if (Math.abs(pea.yaw) > Config.FACE_POSE_YAW || Math.abs(pea.pitch) > Config.FACE_POSE_PITCH || Math.abs(pea.roll) > Config.FACE_POSE_ROLL)
			return null;
		else
			return pea;
	}

	private boolean checkFaceExpression() {
		return true;
	}

	public void beginCheckingFace(IDCard idCard, Ticket ticket) {
		currentIDCard = idCard;
		currentTicket = ticket;
		log.info("/********beginCheckingFace*********/");
	}

	public void stopCheckingFace() {
		currentIDCard = null;
		currentTicket = null;
		log.info("#######stopCheckingFace########");
	}

	public IDCard getCurrentIDCard() {
		return currentIDCard;
	}

	/**
	 * 
	 * @param currentIDCard
	 */
	public void setCurrentIDCard(IDCard currentIDCard) {
		if (currentIDCard.getCardImageBytes() != null && currentIDCard.getCardImageBytes().length > 1024) {
			this.currentIDCard = currentIDCard;
		} else {
			log.debug("IDCard 数据不完整！ currentIDCard.getCardImageBytes=" + currentIDCard.getCardImageBytes());
			currentIDCard = null;
		}
	}

	/**
	 * 设置摄像头参数 SetColorExposure 设置曝光值 范围 -8--0 step 1.0 ,系统缺省值 -6
	 * 。值越小曝光值越小,可设为自动曝光 ColorBrightness 设置亮度 范围 -64--64 step 1.0， 0 设置画面亮度 Gain
	 * 图像增益 0--128,64，1,false Gamma 灰度 100--500,300,1，false HUE 色相
	 * -180--180,0,1，false Saturation 色彩饱和度 0--100,64,1,false Sharpness 清晰度
	 * 0--100,50,1，false WhitenBalance 白平衡 2800--5600,4600,10，true
	 * DepthConfidenceTHRESHOLD 深度信息可信度阈值 0--15,3,1，flse
	 * 
	 * PROPERTY_IVCAM_FILTER_OPTION SR300摄像头平滑系数 0-9 缺省值5 代表中等距离
	 * 
	 * PROPERTY_COLOR_FOCAL_LENGTH 镜头聚焦点 【X,Y】坐标，与分辨率有关
	 * PROPERTY_COLOR_FOCAL_LENGTH_MM 镜头聚焦点距离，单位毫米
	 * 
	 * @param RealsenseDeviceProperties
	 *            设备参数
	 */
	public void setupColorCameraDevice(RealsenseDeviceProperties properties) {
		// log.debug("Camera Device==" + dev);
		if (dev == null)
			return;
		PXCMCapture.DeviceInfo info = new PXCMCapture.DeviceInfo();
		dev.QueryDeviceInfo(info);
		// log.debug("Using Camera: " + info.name);

		dev.SetColorAutoExposure(properties.isColorAutoExposure());
		dev.SetColorAutoWhiteBalance(properties.isColorAutoWhiteBalance());
		dev.SetColorBackLightCompensation(properties.isColorBackLightCompensation());

		dev.SetColorBrightness(properties.getColorBrightness());
		dev.SetColorExposure(properties.getColorExposure());

		dev.SetIVCAMFilterOption(properties.getIVCAM_Option());

		dev.SetSR300_HDR_MODE(properties.getSR300_HDR());
		dev.SetColorContrast(properties.getContrast());
		dev.SetColorGamma(properties.getGamma());
		dev.SetFocalLengthMM(properties.getFocalLength());
		dev.SetColorGain(properties.getGain());
		dev.SetColorHue(properties.getHue());
		dev.SetColorSaturation(properties.getSaturation());
		dev.SetColorSharpness(properties.getSharpness());
		// dev.SetColorWhiteBalance(properties.getWhitebalance());

		// log.debug("ColorBrightness=" + dev.QueryColorBrightness());
		// log.debug("ColorExposure=" + dev.QueryColorExposure());
		// //log.debug("isSR300_HDR=" + d);
		// log.debug("ColorContrast=" + dev.QueryColorContrast());
		// log.debug("ColorFocalLengthMM=" + dev.QueryColorFocalLengthMM());
		// log.debug("Gamma=" + dev.QueryColorGamma());
		// log.debug("HUE=" + dev.QueryColorHue());
		// log.debug("Gain=" + dev.QueryColorGain());
		// log.debug("DepthFocalLengthMM=" + dev.QueryDepthFocalLengthMM());
		// log.debug("DepthConfidenceThreshold=" +
		// dev.QueryDepthConfidenceThreshold());
		// log.debug("IVCAMFilterOption=" + dev.QueryIVCAMFilterOption());
		// log.debug("QueryIVCAMLaserPower=" + dev.QueryIVCAMLaserPower());
		// log.debug("IVCAMAccuracy=" + dev.QueryIVCAMAccuracy());
		// log.debug("ColorGainInfo=" + dev.QueryColorGainInfo());
		// PropertyInfo pinfo = dev.QueryColorGainInfo();
		// log.debug("GainInfo: defaultValue=" + pinfo.defaultValue + " Range "+
		// pinfo.range.min + "--" + pinfo.range.max + " step:" + pinfo.step +"
		// isAuto:" + pinfo.automatic);
		//
		// pinfo = dev.QueryColorBrightnessInfo();
		// log.debug("BrightnessInfo: defaultValue=" + pinfo.defaultValue + "
		// Range "+ pinfo.range.min + "--" + pinfo.range.max + " step:" +
		// pinfo.step +" isAuto:" + pinfo.automatic);
		//
		// pinfo = dev.QueryColorContrastInfo();
		// log.debug("ContrastInfo: defaultValue=" + pinfo.defaultValue + "
		// Range
		// "+ pinfo.range.min + "--" + pinfo.range.max + " step:" + pinfo.step
		// +" isAuto:" + pinfo.automatic);
		//
		// pinfo = dev.QueryColorExposureInfo();
		// log.debug("ColorExposureInfo: defaultValue=" + pinfo.defaultValue + "
		// Range "+ pinfo.range.min + "--" + pinfo.range.max + " step:" +
		// pinfo.step +" isAuto:" + pinfo.automatic);
		//
		// pinfo = dev.QueryColorGammaInfo();
		// log.debug("ColorGammaInfo: defaultValue=" + pinfo.defaultValue + "
		// Range "+ pinfo.range.min + "--" + pinfo.range.max + " step:" +
		// pinfo.step +" isAuto:" + pinfo.automatic);
		//
		// pinfo = dev.QueryColorHueInfo();
		// log.debug("ColorHueInfo(): defaultValue=" + pinfo.defaultValue + "
		// Range "+ pinfo.range.min + "--" + pinfo.range.max + " step:" +
		// pinfo.step +" isAuto:" + pinfo.automatic);
		//
		// pinfo = dev.QueryColorSaturationInfo();
		// log.debug("ColorSaturationInfo: defaultValue=" + pinfo.defaultValue +
		// " Range "+ pinfo.range.min + "--" + pinfo.range.max + " step:" +
		// pinfo.step +" isAuto:" + pinfo.automatic);
		//
		// pinfo = dev.QueryColorSharpnessInfo();
		// log.debug("ColorSharpnessInfo: defaultValue=" + pinfo.defaultValue +
		// "
		// Range "+ pinfo.range.min + "--" + pinfo.range.max + " step:" +
		// pinfo.step +" isAuto:" + pinfo.automatic);
		//
		// pinfo = dev.QueryColorWhiteBalanceInfo();
		// log.debug("ColorWhiteBalanceInfo: defaultValue=" + pinfo.defaultValue
		// + " Range "+ pinfo.range.min + "--" + pinfo.range.max + " step:" +
		// pinfo.step +" isAuto:" + pinfo.automatic);
		//
		// pinfo = dev.QueryDepthConfidenceThresholdInfo();
		// log.debug("DepthConfidenceThresholdInfo: defaultValue=" +
		// pinfo.defaultValue + " Range "+ pinfo.range.min + "--" +
		// pinfo.range.max + " step:" + pinfo.step +" isAuto:" +
		// pinfo.automatic);
		//
		//
		// pinfo = dev.QueryIVCAMFilterOptionInfo();
		// log.debug("IVCAMFilterOptionInfo: defaultValue=" + pinfo.defaultValue
		// + " Range "+ pinfo.range.min + "--" + pinfo.range.max + " step:" +
		// pinfo.step +" isAuto:" + pinfo.automatic);
		//
		// pinfo = dev.QueryIVCAMLaserPowerInfo();
		// log.debug("IVCAMLaserPowerInfo: defaultValue=" + pinfo.defaultValue +
		// " Range "+ pinfo.range.min + "--" + pinfo.range.max + " step:" +
		// pinfo.step +" isAuto:" + pinfo.automatic);
		//
		// pinfo = dev.QueryIVCAMMotionRangeTradeOffInfo();
		// log.debug("IVCAMMotionRangeTradeOffInfo: defaultValue=" +
		// pinfo.defaultValue + " Range "+ pinfo.range.min + "--" +
		// pinfo.range.max + " step:" + pinfo.step +" isAuto:" +
		// pinfo.automatic);
		//
		//

		// PropertyInfo pColorBrightness = dev.QueryColorBrightnessInfo();
		// PropertyInfo pColorExposure = dev.QueryColorExposureInfo();
		// PropertyInfo pBackLight = dev.QueryColorBackLightCompensationInfo();

	}

	/**
	 * 检脸算法
	 */
	public void doTracking() throws Exception {
		if (FaceCheckingService.getInstance().isFrontCamera()) {
			log.info("本进程为前置摄像头进程");
			cameraLog.info("本进程为前置摄像头进程");
		} else {
			log.info("本进程为后置摄像头进程");
			cameraLog.info("本进程为后置摄像头进程");
		}
		pid = ProcessUtil.getCurrentProcessID();
		PXCMSession session = PXCMSession.CreateInstance();

		/**
		 * 在有多个realsense摄像头的情况下，首先列出每个设备的DeviceInfo,再根据DeviceInfo进行过滤
		 */
		PXCMSession.ImplDesc desc = new PXCMSession.ImplDesc();
		PXCMSession.ImplDesc outDesc = new PXCMSession.ImplDesc();
		desc.group = EnumSet.of(PXCMSession.ImplGroup.IMPL_GROUP_SENSOR);
		desc.subgroup = EnumSet.of(PXCMSession.ImplSubgroup.IMPL_SUBGROUP_VIDEO_CAPTURE);

		Map<String, PXCMCapture.DeviceInfo> map = new HashMap<String, PXCMCapture.DeviceInfo>();

		int numDevices = 0;
		for (int i = 0;; i++) {
			// log.debug("i==" + i);
			if (session.QueryImpl(desc, i, outDesc).isError())
				break;

			PXCMCapture capture = new PXCMCapture();
			if (session.CreateImpl(outDesc, capture).isError())
				continue;

			for (int j = 0;; j++) {
				// log.debug("j==" + j);
				PXCMCapture.DeviceInfo info = new PXCMCapture.DeviceInfo();
				if (capture.QueryDeviceInfo(j, info).isError())
					break;
				log.info(info.name);
				if (info.name.startsWith("Intel(R)")) {
					log.info("序列号==" + info.serial);
					cameraLog.info("序列号==" + info.serial);
					map.put(info.serial, info);
					numDevices++;
				}
			}
		}
		log.info("Found " + numDevices + " RealSense Devices");

		if (numDevices < Config.getInstance().getCameraCount()) {
			log.error("系统识别到的摄像头数小于配置文件中的数量!!");
			cameraLog.error("系统识别到的摄像头数小于配置文件中的数量!!");
			
			MonitorXMLUtil.updateBaseInfoFonMonitor(Config.getInstance().getBaseInfoXMLPath(), "002", 1);
			MonitorXMLUtil.updateDoorStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), "001", "001", "002", "001");
			MonitorXMLUtil.updateEntirStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), 1);
			return;
		}

		PXCMCapture.DeviceInfo devInfo = null;
		String devSerial = Config.getInstance().getBehindCameraNo();
		log.info("conf.devSerial = " + devSerial);
		devInfo = map.get(devSerial);

		if (devInfo == null) {
			log.error("Failed to create a devInfo instance from devMaps.");
			cameraLog.error("Failed to create a devInfo instance from devMaps.");
			
			MonitorXMLUtil.updateBaseInfoFonMonitor(Config.getInstance().getBaseInfoXMLPath(), "002", 1);
			MonitorXMLUtil.updateDoorStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), "001", "001", "002", "001");
			MonitorXMLUtil.updateEntirStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), 1);
			return;
		} else {
			log.info("当前摄像头:" + devInfo.name + ",序列号 = " + devInfo.serial);
			cameraLog.info("当前摄像头:" + devInfo.name + ",序列号 = " + devInfo.serial);
		}

		PXCMPowerState ps = session.CreatePowerManager();
		// Set the power state
		ps.SetState(PXCMPowerState.State.PERFORMANCE);
		// Set the inactivity interval.
		ps.SetInactivityInterval(5);
		PXCMSenseManager senseMgr = session.CreateSenseManager();

		if (senseMgr == null) {
			log.error("Failed to create a sense manager instance.");
			
			MonitorXMLUtil.updateBaseInfoFonMonitor(Config.getInstance().getBaseInfoXMLPath(), "002", 1);
			MonitorXMLUtil.updateDoorStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), "001", "001", "002", "004");
			MonitorXMLUtil.updateEntirStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), 1);
			return;
		}

		pxcmStatus sts = senseMgr.EnableStream(PXCMCapture.StreamType.STREAM_TYPE_COLOR, cWidth, cHeight, frameRate);

		sts = senseMgr.EnableFace(null);
		PXCMFaceModule faceModule = senseMgr.QueryFace();
		if (sts.isError() || faceModule == null) {
			log.error("Failed to initialize face module.");
			
			MonitorXMLUtil.updateBaseInfoFonMonitor(Config.getInstance().getBaseInfoXMLPath(), "002", 1);
			MonitorXMLUtil.updateDoorStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), "001", "001", "002", "004");
			MonitorXMLUtil.updateEntirStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), 1);
			return;
		}

		// Retrieve the input requirements
		sts = pxcmStatus.PXCM_STATUS_DATA_UNAVAILABLE;
		PXCMFaceConfiguration faceConfig = faceModule.CreateActiveConfiguration();
		if (Config.getInstance().getFaceTrackMode() == Config.FACE_TRACK_IR)
			faceConfig.SetTrackingMode(PXCMFaceConfiguration.TrackingModeType.FACE_MODE_IR);
		else if (Config.getInstance().getFaceTrackMode() == Config.FACE_TRACK_COLOR_DEPTH)
			faceConfig.SetTrackingMode(PXCMFaceConfiguration.TrackingModeType.FACE_MODE_COLOR_PLUS_DEPTH);
		else if (Config.getInstance().getFaceTrackMode() == Config.FACE_TRACK_COLOR)
			faceConfig.SetTrackingMode(PXCMFaceConfiguration.TrackingModeType.FACE_MODE_COLOR);

		faceConfig.strategy = PXCMFaceConfiguration.TrackingStrategyType.STRATEGY_CLOSEST_TO_FARTHEST;
		faceConfig.detection.isEnabled = true;
		faceConfig.detection.maxTrackedFaces = Config.MaxTrackedFaces;
		faceConfig.landmarks.maxTrackedFaces = Config.MaxTrackedLandmark;
		faceConfig.landmarks.numLandmarks = Config.NumOfLandmarks;
		faceConfig.landmarks.isEnabled = true;
		faceConfig.pose.isEnabled = true;
		faceConfig.pose.maxTrackedFaces = Config.MaxTrackedFaces;
		// faceConfig.Update();
		faceConfig.ApplyChanges();

		// sm is a PXCMSenseManager instance
		PXCMCaptureManager captureMgr = senseMgr.QueryCaptureManager();
		captureMgr.FilterByDeviceInfo(devInfo);

		sts = senseMgr.Init();

		if (sts.isError()) {
			log.error("Init failed: " + sts);
			
			MonitorXMLUtil.updateBaseInfoFonMonitor(Config.getInstance().getBaseInfoXMLPath(), "002", 1);
			MonitorXMLUtil.updateDoorStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), "001", "001", "002", "004");
			MonitorXMLUtil.updateEntirStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), 1);
			return;
		}

		PXCMFaceData faceData = faceModule.CreateOutput();

		if (faceData == null) {
			log.error("PXCMFaceData == null");
			return;
		}

		dev = senseMgr.QueryCaptureManager().QueryDevice(); // 获取device对象

		/**
		 * 启动摄像头时，判断当前时间，选择白天设置或夜间设置
		 */
		int dayCameraTime = Integer.parseInt(Config.getInstance().getDayCameraTime());
		int nightCameraTime = Integer.parseInt(Config.getInstance().getNightCameraTime());
		int nowTime = Integer.parseInt(CalUtils.getStringTime()); // HHmm

		if (Config.getInstance().getIsSetBackCameraConfigByInit() == 1) {
			if (nowTime >= dayCameraTime && nowTime < nightCameraTime) { // 08:00<=now<18:00
				log.info("当前设置为白天模式");
				RealsenseDeviceProperties realsenseProp = new RealsenseDeviceProperties();

				realsenseProp.setColorAutoExposure(true);
				realsenseProp.setColorAutoWhiteBalance(true);
				realsenseProp.setColorBackLightCompensation(true);
				realsenseProp.setColorBrightness(Config.getInstance().getInitColorBrightness());
				realsenseProp.setColorExposure(Config.getInstance().getInitColorExposure());
				realsenseProp.setContrast(Config.getInstance().getInitContrast());
				realsenseProp.setGamma(Config.getInstance().getInitGamma());
				realsenseProp.setGain(Config.getInstance().getInitGain());

				setupColorCameraDevice(realsenseProp); // 设置摄像头参数
				log.info("InitColorExposure==" + Config.getInstance().getInitColorExposure() + ",InitColorBrightness==" + Config.getInstance().getInitColorBrightness());
				log.info("InitContrast==" + Config.getInstance().getInitContrast() + ",InitGain==" + Config.getInstance().getInitGain());
			} else {
				log.info("当前设置为夜晚模式");
				RealsenseDeviceProperties realsenseProp = new RealsenseDeviceProperties();

				realsenseProp.setColorAutoExposure(true);
				realsenseProp.setColorAutoWhiteBalance(true);
				realsenseProp.setColorBackLightCompensation(true);
				realsenseProp.setColorBrightness(Config.getInstance().getNightColorBrightness());
				realsenseProp.setColorExposure(Config.getInstance().getNightColorExposure());
				realsenseProp.setContrast(Config.getInstance().getNightContrast());
				realsenseProp.setGamma(Config.getInstance().getNightGamma());
				realsenseProp.setGain(Config.getInstance().getNightGain());

				setupColorCameraDevice(realsenseProp);
				log.info("NightColorExposure==" + Config.getInstance().getNightColorExposure() + ",NightColorBrightness=="
						+ Config.getInstance().getNightColorBrightness());
				log.info("NightContrast==" + Config.getInstance().getNightContrast() + ",NightGain==" + Config.getInstance().getNightGain());
			}
		}

		FaceScreenListener.getInstance().setPidStr(pid); // 设为允许写心跳
		
		MonitorXMLUtil.updateBaseInfoFonMonitor(Config.getInstance().getBaseInfoXMLPath(), "002", 0);
		MonitorXMLUtil.updateDoorStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), "001", "001", "002", "000");
		MonitorXMLUtil.updateEntirStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), 0);

		/**
		 * 
		 */
		while (startCapture) {
			CommUtil.sleep(50);
			try {
				sts = senseMgr.AcquireFrame(true);
				if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) == 0) {
					// ProcessUtil.writeHeartbeat(pid); // 写心跳日志
					FaceScreenListener.getInstance().setPidStr(pid); // 设为允许写心跳
				} else {
					FaceScreenListener.getInstance().setPidStr("");// 设为停止写心跳
					log.error("senseMgr failed! sts=" + sts);
				}

				BufferedImage frameImage = null;
				PXCMCapture.Sample sample = senseMgr.QueryFaceSample();
				if (sample == null || sample.color == null) {
					FaceScreenListener.getInstance().setPidStr("");// 设为停止写心跳
					log.error("QueryFaceSample failed! sample=" + sample);
					senseMgr.ReleaseFrame();
					continue;
				} else {
					frameImage = drawFrameImage(sample); // 截图
					// ProcessUtil.writeHeartbeat(pid); // 写心跳日志
					FaceScreenListener.getInstance().setPidStr(pid);// 设为允许写心跳
				}

				sts = faceData.Update();
				if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) != 0) {
					log.error("faceData.Update() failed! sts=" + sts);
					senseMgr.ReleaseFrame();
					continue;
				}

				List<SortedFace> sortFaces = FaceQualityDetectForRealSense.detectFaceQuality(faceData);
				List<PITData> faceDatas = new ArrayList<PITData>();
				for (SortedFace sf : sortFaces) {
					PXCMFaceData.Face face = sf.face;
					PXCMFaceData.DetectionData detectionData = face.QueryDetection();

					drawLocation(detectionData);// 画脸部框
					PXCMFaceData.PoseEulerAngles pae = checkFacePose(face.QueryPose());

					boolean isRealFace = true;
					// 如果只是RGB检测人脸，则不需检测是否活脸和深度
					if (Config.FACE_TRACK_COLOR != Config.getInstance().getFaceTrackMode()) {
						if (Config.getInstance().getIsCheckRealFace() == Config.Is_Check_RealFace) {
							isRealFace = FaceQualityDetectForRealSense.checkRealFace(sf);
						}
					}

					// log.debug("detectionData==" + detectionData);
					if (detectionData != null && isRealFace) {
						drawLandmark(face);
						PITData fd = createFaceData(frameImage, detectionData);

						fd.setFaceQuality(sf.getQuality());
						if (fd != null) {
							// log.info("isInTracking===" +
							// DeviceConfig.getInstance().isInTracking());					
							
							
							if (DeviceConfig.getInstance().isInTracking()) {
								if (fd.isDetectedFace() && currentIDCard != null && currentTicket != null) {
									/**
									 * 检测到第一张人脸，则停止送前置摄像头的人脸
									 */
									if (FaceCheckingService.getInstance().isSendFrontCameraFace()) {
										log.info("检测到第一张人脸，则停止送前置摄像头的人脸");
										FaceCheckingService.getInstance().setSendFrontCameraFace(false);
										String trackClientId = DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum();
										GatCtrlSenderBroker.getInstance(trackClientId).sendMessage(DeviceConfig.EventTopic, DeviceConfig.Event_StopSend_FrontCameraFace);
										FaceCheckingService.getInstance().clearFaceVerifyQueue();
									}

									fd.setIdCard(currentIDCard);
									fd.setTicket(currentTicket);
									fd.setCameraPosition(2); // 后置摄像头人脸
									fd.setCameraFaceMode(1); // 检脸状态下的人脸
									faceDatas.add(fd);
									
									/**
									 * 新增通过人脸框的y值来大概判断旅客高度
									 */
									PXCMRectI32 rect = new PXCMRectI32();
									boolean ret = detectionData.QueryBoundingRect(rect);
									if (ret) {
										//System.out.println("Face ID:" + face.QueryUserID() +"..............................................");
										//System.out.println("Top Left corner: (" + rect.x + "," + rect.y + ")");
										//计算人脸平均距离
										float[] depth = new float[1]; 
										detectionData.QueryFaceAverageDepth(depth);
										
										log.info("queryUserID=="+face.QueryUserID() + ",rect.y==" + rect.y +",depth==" + depth[0]);
									}
									
									if(rect.y > Config.getInstance().getFaceRectY()){
										String trackClientId = DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum();
										GatCtrlSenderBroker.getInstance(trackClientId).sendMessage(DeviceConfig.EventTopic, DeviceConfig.Event_PersonFaceUnderRed);
									}
								}
							} else { // 非正常检脸流程中
								// log.info("非正常检脸流程中,lastIdCard==" +
								// FaceCheckingService.getInstance().getLastIdCard());
								if (fd.isDetectedFace() && FaceCheckingService.getInstance().isDealNoneTrackFace()
										&& FaceCheckingService.getInstance().getLastIdCard() != null) {
									// log.info("非检脸状态下,具备送入当前人脸同刚才的idCard比对");

									try {
										fd.setIdCard(FaceCheckingService.getInstance().getLastIdCard());
										fd.setCameraPosition(2); // 后置摄像头人脸
										fd.setCameraFaceMode(2); // 非检脸状态下的人脸

										PITVerifyData pvd = new PITVerifyData(fd);
										byte[] buf = CommUtil.serialObjToBytes(pvd);
										if (Config.getInstance().getTransferFaceMode() == Config.TransferFaceByMqtt) { // mqtt
											PTVerifySender.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum()).sendMessage(buf);
										}
										if (Config.getInstance().getTransferFaceMode() == Config.TransferFaceBySocket) { // socket
											FaceSender.getInstance().sendFaceDataByTcp(buf);
										}
									} catch (Exception ex) {
										log.error("", ex);
									}
								}
							}
						}
					}
				}
				if (DeviceConfig.getInstance().isInTracking()) {
					// log.info("将后置人脸插入待检队列,faceDatas.size = " +
					// faceDatas.size());
					FaceCheckingService.getInstance().offerDetectedFaceData(faceDatas); // 将人脸插入待检队列
				}
				senseMgr.ReleaseFrame();
			} catch (Exception ex) {
				FaceScreenListener.getInstance().setPidStr("");// 设为停止写心跳
				log.error("doTracking:", ex);
				senseMgr.ReleaseFrame();
				continue;
			}
		}
	}

}
