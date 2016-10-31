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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.gui.VideoPanel;
import com.rxtec.pitchecking.mbean.ProcessUtil;
import com.rxtec.pitchecking.mq.RemoteMonitorPublisher;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.PITData;
import com.rxtec.pitchecking.task.FaceScreenListener;
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
import intel.rssdk.PXCMCapture.Sample;

public class RSFaceTrackTask implements Runnable {

	static int cWidth = Config.FrameWidth;
	static int cHeight = Config.FrameHeigh;
	static int frameRate = Config.frameRate;
	static int dWidth, dHeight;
	static boolean exit = false;
	private Logger log = LoggerFactory.getLogger("RSFaceTrackTask");
	private VideoPanel videoPanel = null;
	private boolean exist = false;
	private boolean startCapture = true;
	private boolean startTrackFace = true;
	private boolean enableExpression = true;
	private boolean enableFaceLandmark = true;
	private IDCard currentIDCard = null;
	private Ticket currentTicket = null;
	private String pid = "";

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
//		log.debug("detection.QueryBoundingRect(rect)==" + ret + ",rect==" + rect);
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
				g.drawRect(rect.x * Config.FrameWidth / Config.IRFrameWidth,
						rect.y * Config.FrameHeigh / Config.IRFrameHeigh,
						rect.w * Config.FrameWidth / Config.IRFrameWidth,
						rect.h * Config.FrameHeigh / Config.IRFrameHeigh);
			} else if (Config.getInstance().getFaceTrackMode() == Config.FACE_TRACK_COLOR_DEPTH) {
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
					graphics.drawString("x", point.x * Config.FrameWidth / Config.IRFrameWidth,
							point.y * Config.FrameHeigh / Config.IRFrameHeigh);
				else if (Config.getInstance().getFaceTrackMode() == Config.FACE_TRACK_COLOR_DEPTH) {
					graphics.drawString("x", point.x, point.y);

				}
			} else {
				graphics.setColor(Color.YELLOW);
				if (Config.getInstance().getFaceTrackMode() == 1)
					graphics.drawString("x", point.x * Config.FrameWidth / Config.IRFrameWidth,
							point.y * Config.FrameHeigh / Config.IRFrameHeigh);
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
	 * @param sf
	 * @return
	 */
	private boolean checkRealFace(SortFace sf) {

		boolean isRealFace = false;
		PXCMFaceData.Face face = sf.face;
		if (face == null) {
			isRealFace = false;
			return isRealFace;
		}
		PXCMFaceData.LandmarksData landmarks = face.QueryLandmarks();
		if (landmarks == null) {
			isRealFace = false;
			log.debug(sf.distance + " face landmarks == null");
			return isRealFace;
		}

		return checkFaceDepth(landmarks) & checkFaceWidth(landmarks);
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
			log.debug("faceLeftPoint confidenceWorld=" + faceLeftPoint.confidenceWorld + " X=" + faceLeftPointX + " Y="
					+ faceLeftPointY);
		}

		float faceRightPointX = 0f;
		float faceRightPointY = 0f;

		LandmarkPoint faceRightPoint = new LandmarkPoint();
		if (landmarks.QueryPoint(faceRightPointIdx, faceRightPoint)) {
			faceRightPointX = faceRightPoint.world.z;
			faceRightPointY = faceRightPoint.world.y;
			log.debug("faceRightPoint confidenceWorld=" + faceRightPoint.confidenceWorld + " X=" + faceRightPointX
					+ " Y=" + faceRightPointY);
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
	 * @param landmarks
	 * @return
	 */
	private boolean checkFaceDepth(LandmarksData landmarks) {

		int nJawPoints = landmarks.QueryNumPointsByGroup(LandmarksGroupType.LANDMARK_GROUP_JAW);
		PXCMFaceData.LandmarkPoint[] jawPoints = new PXCMFaceData.LandmarkPoint[nJawPoints];

		// printFaceLandmarkZ(landmarks);

		for (int i = 0; i < nJawPoints; i++) {
			jawPoints[i] = new LandmarkPoint();
		}

		int nLeftEyePoints = landmarks.QueryNumPointsByGroup(LandmarksGroupType.LANDMARK_GROUP_LEFT_EYE);
		PXCMFaceData.LandmarkPoint[] leftEyePoints = new PXCMFaceData.LandmarkPoint[nLeftEyePoints];

		int nRightEyePoints = landmarks.QueryNumPointsByGroup(LandmarksGroupType.LANDMARK_GROUP_RIGHT_EYE);
		PXCMFaceData.LandmarkPoint[] rightEyePoints = new PXCMFaceData.LandmarkPoint[nRightEyePoints];

		for (int i = 0; i < nRightEyePoints; i++) {
			rightEyePoints[i] = new LandmarkPoint();
		}

		for (int i = 0; i < nLeftEyePoints; i++) {
			leftEyePoints[i] = new LandmarkPoint();
		}

		landmarks.QueryPointsByGroup(LandmarksGroupType.LANDMARK_GROUP_JAW, jawPoints);
		landmarks.QueryPointsByGroup(LandmarksGroupType.LANDMARK_GROUP_LEFT_EYE, leftEyePoints);
		landmarks.QueryPointsByGroup(LandmarksGroupType.LANDMARK_GROUP_RIGHT_EYE, rightEyePoints);

		float d1 = 0, d2 = 0;

		for (LandmarkPoint p : jawPoints) {
			if (p.confidenceWorld == 0) {
				return false;
			}
			d1 += p.world.z;
		}

		d1 = d1 / nJawPoints;

		for (LandmarkPoint p : leftEyePoints) {
			if (p.confidenceWorld == 0) {
				return false;
			}
			d2 += p.world.z;
		}

		for (LandmarkPoint p : rightEyePoints) {
			if (p.confidenceWorld == 0) {
				return false;
			}
			d2 += p.world.z;
		}

		d2 = d2 / (nLeftEyePoints + nRightEyePoints);

		float zDIF = Math.abs(d1 - d2) * 1000;

		if (Config.DValueMinDepth < zDIF && zDIF < Config.DValueMaxDepth) {
			log.debug("zDIF=" + zDIF + "  checkFaceDepth = true");
			return true;
		} else {
			log.debug("zDIF=" + zDIF + "  checkFaceDepth = false");
			return false;
		}
	}

	/**
	 * 
	 * @param landmarks
	 * @return
	 */
	private boolean checkFaceWidth(LandmarksData landmarks) {
		int faceBorderLeftIdx = landmarks.QueryPointIndex(LandmarkType.LANDMARK_FACE_BORDER_TOP_LEFT);
		int faceBorderRightIdx = landmarks.QueryPointIndex(LandmarkType.LANDMARK_FACE_BORDER_TOP_RIGHT);
		LandmarkPoint pLeftBorder = new LandmarkPoint();
		LandmarkPoint pRightBorder = new LandmarkPoint();

		landmarks.QueryPoint(faceBorderLeftIdx, pLeftBorder);
		landmarks.QueryPoint(faceBorderRightIdx, pRightBorder);
		if (pLeftBorder.confidenceWorld == 0 || pRightBorder.confidenceWorld == 0) {
			return false;
		}

		float wDIF = Math.abs(pLeftBorder.world.x - pRightBorder.world.x) * 1000;

		if (Config.DValueMinWidth < wDIF && wDIF < Config.DValueMaxWidth) {
			log.debug("wDIF=" + wDIF + "  checkFaceWidth = true");
			return true;
		} else {
			log.debug("wDIF=" + wDIF + "  checkFaceWidth = false");
			return false;
		}
	}

	/**
	 * 
	 * @param sample
	 * @return
	 */
	private BufferedImage drawFrameImage(PXCMCapture.Sample sample) {

		PXCMImage.ImageData cData = new PXCMImage.ImageData();

		pxcmStatus sts = sample.color.AcquireAccess(PXCMImage.Access.ACCESS_READ,
				PXCMImage.PixelFormat.PIXEL_FORMAT_RGB32,
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
		if (Math.abs(pea.yaw) > Config.FACE_POSE_YAW || Math.abs(pea.pitch) > Config.FACE_POSE_PITCH
				|| Math.abs(pea.roll) > Config.FACE_POSE_ROLL)
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
		log.debug("/********beginCheckingFace*********/");
	}

	public void stopCheckingFace() {
		currentIDCard = null;
		currentTicket = null;
		log.debug("stopCheckingFace......");
		log.debug("/####################################/");
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
			log.info("IDCard 数据不完整！ currentIDCard.getCardImageBytes=" + currentIDCard.getCardImageBytes());
			currentIDCard = null;
		}
	}

	/**
	 * 
	 * @param dev
	 */
	private void setupColorCameraDevice(PXCMCapture.Device dev) {
		if (dev == null)
			return;
		PXCMCapture.DeviceInfo info = new PXCMCapture.DeviceInfo();
		dev.QueryDeviceInfo(info);
		log.info("Using Camera: " + info.name);
		dev.SetColorAutoExposure(true);
		dev.SetColorAutoWhiteBalance(true);
		dev.SetColorBackLightCompensation(true);
	}

	/**
	 * 检脸算法
	 */
	public void doTracking() throws Exception {
		pid = ProcessUtil.getCurrentProcessID();
		PXCMSession session = PXCMSession.CreateInstance();
		PXCMPowerState ps = session.CreatePowerManager();
		// Set the power state
		ps.SetState(PXCMPowerState.State.PERFORMANCE);
		// Set the inactivity interval.
		ps.SetInactivityInterval(5);
		PXCMSenseManager senseMgr = session.CreateSenseManager();

		if (senseMgr == null) {
			log.error("Failed to create a sense manager instance.");
			return;
		}

		pxcmStatus sts = senseMgr.EnableStream(PXCMCapture.StreamType.STREAM_TYPE_COLOR, cWidth, cHeight, frameRate);

		sts = senseMgr.EnableFace(null);
		PXCMFaceModule faceModule = senseMgr.QueryFace();
		if (sts.isError() || faceModule == null) {
			log.error("Failed to initialize face module.");
			return;
		}

		// Retrieve the input requirements
		sts = pxcmStatus.PXCM_STATUS_DATA_UNAVAILABLE;
		PXCMFaceConfiguration faceConfig = faceModule.CreateActiveConfiguration();
		if (Config.getInstance().getFaceTrackMode() == 1)
			faceConfig.SetTrackingMode(PXCMFaceConfiguration.TrackingModeType.FACE_MODE_IR);
		else if (Config.getInstance().getFaceTrackMode() == 2)
			faceConfig.SetTrackingMode(PXCMFaceConfiguration.TrackingModeType.FACE_MODE_COLOR_PLUS_DEPTH);

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

		sts = senseMgr.Init();

		if (sts.isError()) {
			log.error("Init failed: " + sts);
			return;
		}

		PXCMFaceData faceData = faceModule.CreateOutput();

		if (faceData == null) {
			log.error("PXCMFaceData == null");
			return;
		}

		PXCMCapture.Device dev = senseMgr.QueryCaptureManager().QueryDevice();
		setupColorCameraDevice(dev);
		while (startCapture) {
			try {
				sts = senseMgr.AcquireFrame(true);
				if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) == 0) {
					// ProcessUtil.writeHeartbeat(pid); // 写心跳日志
					FaceScreenListener.getInstance().setPidStr(pid); // 设为允许写心跳
				} else {
					FaceScreenListener.getInstance().setPidStr("");// 设为停止写心跳
					log.info("senseMgr failed! sts=" + sts);
				}

				BufferedImage frameImage = null;
				PXCMCapture.Sample sample = senseMgr.QueryFaceSample();
				if (sample == null || sample.color == null) {
					FaceScreenListener.getInstance().setPidStr("");// 设为停止写心跳
					log.info("QueryFaceSample failed! sample=" + sample);
					senseMgr.ReleaseFrame();
					continue;
				} else {
					frameImage = drawFrameImage(sample);
					// ProcessUtil.writeHeartbeat(pid); // 写心跳日志
					FaceScreenListener.getInstance().setPidStr(pid);// 设为允许写心跳
				}

				sts = faceData.Update();
				if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) != 0) {
					log.info("faceData.Update() failed! sts=" + sts);
					senseMgr.ReleaseFrame();
					continue;
				}

				//通过MQ发送帧画面
				RemoteMonitorPublisher.getInstance()
					.offerFrameData(ImageToolkit.getImageBytes(frameImage, "jpeg"));

				
				List<SortFace> sortFaces = sortFaceByDistence(faceData);
				for (SortFace sf : sortFaces) {
					PXCMFaceData.Face face = sf.face;
					PXCMFaceData.DetectionData detection = face.QueryDetection();

					drawLocation(detection);// 画脸部框
					PXCMFaceData.PoseEulerAngles pae = checkFacePose(face.QueryPose());

					boolean isRealFace = true;
					if (Config.getInstance().getIsCheckRealFace() == Config.Is_Check_RealFace) {
						isRealFace = checkRealFace(sf);
					}
					if (detection != null && isRealFace && pae != null) {
						drawLandmark(face);
						PITData fd = createFaceData(frameImage, detection);
						
						fd.setFaceDistance(sf.distance);
						if (fd != null) {
							if (fd.isDetectedFace() && currentIDCard != null && currentTicket != null) {
								fd.setIdCard(currentIDCard);
								fd.setTicket(currentTicket);
								fd.setFacePosePitch(pae.pitch);
								fd.setFacePoseRoll(pae.roll);
								fd.setFacePoseYaw(pae.yaw);
								// log.debug("Begin to verify face.........");
								FaceCheckingService.getInstance().offerDetectedFaceData(fd);
							}
						}
					}
				}
				senseMgr.ReleaseFrame();
			} catch (Exception ex) {
				FaceScreenListener.getInstance().setPidStr("");// 设为停止写心跳
				log.info("doTracking:", ex);
				senseMgr.ReleaseFrame();
				continue;
			}
		}
	}

	/**
	 * 
	 * @param faceData
	 * @return
	 */
	private List<SortFace> sortFaceByDistence(PXCMFaceData faceData) {
		List<SortFace> sortFaces = new ArrayList<SortFace>();
		int faceCount = faceData.QueryNumberOfDetectedFaces();
		for (int i = 0; i < faceCount; i++) {
			PXCMFaceData.Face face = faceData.QueryFaceByIndex(i);
			PXCMRectI32 rect = new PXCMRectI32();
			PXCMFaceData.DetectionData detection = face.QueryDetection();
			boolean ret = detection.QueryBoundingRect(rect);
			if (ret) {
				int w = rect.w;
				// log.debug("face width = " + w);
				float[] averageDepth = new float[1];
				detection.QueryFaceAverageDepth(averageDepth);
				float distance = averageDepth[0];
				if (distance > Config.getInstance().getMinAverageDepth()
						&& distance < Config.getInstance().getMaxAverageDepth()) {
					SortFace sf = new SortFace(face, distance);
					sortFaces.add(sf);
				}
			}
		}
		Collections.sort(sortFaces);
		return sortFaces;
	}

}

class SortFace implements Comparable<SortFace> {
	public PXCMFaceData.Face face;
	public float distance = 0;

	public SortFace(PXCMFaceData.Face face, float distance) {
		this.face = face;
		this.distance = distance;

	}

	@Override
	public int compareTo(SortFace o) {
		if (distance < o.distance)
			return 0;
		else
			return 1;
	}

}
