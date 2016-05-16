package com.rxtec.pitchecking.picheckingservice.realsense;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.gui.VideoPanel;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.IDCard;
import com.rxtec.pitchecking.picheckingservice.MFFaceTrackTask;
import com.rxtec.pitchecking.picheckingservice.PICData;

import intel.rssdk.PXCMBase;
import intel.rssdk.PXCMCapture;
import intel.rssdk.PXCMFaceConfiguration;
import intel.rssdk.PXCMFaceData;
import intel.rssdk.PXCMFaceModule;
import intel.rssdk.PXCMImage;
import intel.rssdk.PXCMImage.Option;
import intel.rssdk.PXCMRectI32;
import intel.rssdk.PXCMSenseManager;
import intel.rssdk.PXCMVideoModule;
import intel.rssdk.pxcmStatus;
import intel.rssdk.PXCMCapture.Device;
import intel.rssdk.PXCMCapture.Sample;

public class RSFaceTrackTask implements Runnable {

	static int cWidth = 640;
	static int cHeight = 480;
	static int dWidth, dHeight;
	static boolean exit = false;
	private Logger log = LoggerFactory.getLogger("RSFaceTrackTask");
	private VideoPanel videoPanel = null;
	private boolean exist = false;
	private boolean startCapture = true;
	private boolean startTrackFace = true;
	private boolean enableExpression = true;
	private boolean enableFaceLandmark = true;

	private int maxTrackedFaces = 4;
	private static int LandmarkAlignment = -3;

	private RSModuleStatus rsStatus;

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
		doTracking();

	}

	class FaceFrameHandler implements PXCMSenseManager.Handler {
		public pxcmStatus OnModuleProcessedFrame(int mid, PXCMBase module, PXCMCapture.Sample sample) {
			// check if the callback is from the face tracking module.
			if (mid == PXCMFaceModule.CUID) {

				BufferedImage frameImage = null;
				if (sample.color != null) {
					frameImage = drawFrameImage(sample);
				}

				PXCMFaceModule faceModule = (PXCMFaceModule) module.QueryInstance(PXCMFaceModule.CUID);
				PXCMFaceData faceData = faceModule.CreateOutput();

				faceData.Update();

				for (int fidx = 0;; fidx++) {
					PXCMFaceData.Face face = faceData.QueryFaceByIndex(fidx);
					if (face == null)
						break;
					PXCMFaceData.DetectionData detection = face.QueryDetection();
					if (detection != null) {
						PICData fd = createFaceData(frameImage, detection);
						if (fd.isDetectedFace() && currentIDCard != null) {
							fd.setIdCard(currentIDCard);
							FaceCheckingService.getInstance().offerDetectedFaceData(fd);
						}
					}

					drawLocation(detection);
					// drawLandmark(face);

				}
			}

			// return NO_ERROR to continue, or any error to abort.

			sample.ReleaseImages();

			return pxcmStatus.PXCM_STATUS_NO_ERROR;
		}

		@Override
		public pxcmStatus OnConnect(Device arg0, boolean arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public pxcmStatus OnModuleSetProfile(int arg0, PXCMBase arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public pxcmStatus OnNewSample(int arg0, Sample arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void OnStatus(int arg0, pxcmStatus arg1) {
			// TODO Auto-generated method stub

		}
	};

	
	private PICData createFaceData(BufferedImage frame, PXCMFaceData.DetectionData detection) {
		if (frame == null)
			return null;
		PXCMRectI32 rect = new PXCMRectI32();
		PICData fd = new PICData(frame);
		boolean ret = detection.QueryBoundingRect(rect);
		if (ret) {
			int x, y, w, h;
			x = (int) (rect.x);
			y = (int) (rect.y * 0.8);
			w = (int) (rect.w * 1.2);
			h = (int) (rect.h * 1.3);

			if (frame.getWidth() < (x + w) || frame.getHeight() < (y + h)) {
				if (frame.getWidth() < (x + w)) {
					w = frame.getWidth() - x;
				}
				if (frame.getHeight() < (y + h)) {
					h = frame.getHeight() - y;
				}
				BufferedImage faceImage = frame.getSubimage(x, y, w, h);
				fd.setFaceImage(faceImage);
				fd.getFaceLocation().setLocation(rect.x, rect.y, rect.w, rect.h);
				fd.setDetectedFace(true);
			} else {
				BufferedImage faceImage = frame.getSubimage(x, y, w, h);
				fd.setFaceImage(faceImage);
				fd.getFaceLocation().setLocation(rect.x, rect.y, rect.w, rect.h);
				fd.setDetectedFace(true);
			}
		}
		return fd;
	}

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
			g.drawRect(rect.x, rect.y, rect.w, rect.h);
		}
	}

	private void drawLandmark(PXCMFaceData.Face face) {
		if (face == null)
			return;
		PXCMFaceData.LandmarksData landmarks = face.QueryLandmarks();
		int npoints = landmarks.QueryNumPoints();
		PXCMFaceData.LandmarkPoint[] points = new PXCMFaceData.LandmarkPoint[npoints];
		landmarks.QueryPoints(points);

		Point point = new Point();
		for (PXCMFaceData.LandmarkPoint landmark : points) {
			if (landmark == null)
				continue;
			point.x = (int) (landmark.image.x + LandmarkAlignment);
			point.y = (int) (landmark.image.y + LandmarkAlignment);

			Graphics2D graphics = (Graphics2D) videoPanel.getGraphics();
			if (landmark.confidenceImage == 0) {
				graphics.setColor(Color.RED);
				graphics.drawString("x", point.x, point.y);
			} else {
				graphics.setColor(Color.YELLOW);
				graphics.drawString("•", point.x, point.y);
			}
		}

	}

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
		// videoPanel.image.setRGB(0, 0, 480, 480, cBuff, 0, cData.pitches[0] /
		// 4);
		videoPanel.image.setRGB(0, 0, cWidth, cHeight, cBuff, 0, cData.pitches[0] / 4);

		videoPanel.paintImg();
		sts = sample.color.ReleaseAccess(cData);
		if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) < 0) {
			log.error("Failed to ReleaseAccess of color image data");
		}
		return videoPanel.image;
	}

	private boolean checkFacePose(PXCMFaceData.PoseData poseData) {
		PXCMFaceData.PoseEulerAngles pea = new PXCMFaceData.PoseEulerAngles();
		poseData.QueryPoseAngles(pea);
		System.out.println("(Roll, Yaw, Pitch) = (" + pea.roll + "," + pea.yaw + "," + pea.pitch + ")");
		return true;
	}

	private boolean checkFaceExpression() {
		return true;
	}

	public void beginCheckingFace(IDCard idCard) {
		currentIDCard = idCard;

	}

	private IDCard currentIDCard = null;

	public IDCard getCurrentIDCard() {
		return currentIDCard;
	}

	public void setCurrentIDCard(IDCard currentIDCard) {
		this.currentIDCard = currentIDCard;
	}

	public void stopCheckingFace() {
		currentIDCard = null;
		FaceCheckingService.getInstance().resetFaceDataQueue();
	}

	private void doTracking() {
		PXCMSenseManager senseMgr = PXCMSenseManager.CreateInstance();

		if (senseMgr == null) {
			log.error("Failed to create a sense manager instance.");
			return;
		}

		pxcmStatus sts = senseMgr.EnableStream(PXCMCapture.StreamType.STREAM_TYPE_COLOR, cWidth, cHeight);

		sts = senseMgr.EnableFace(null);
		PXCMFaceModule faceModule = senseMgr.QueryFace();
		PXCMFaceData faceData = faceModule.CreateOutput();

		if (sts.isError() || faceModule == null) {
			log.error("Failed to initialize face module.");
			return;
		}

		// Retrieve the input requirements
		sts = pxcmStatus.PXCM_STATUS_DATA_UNAVAILABLE;
		PXCMFaceConfiguration faceConfig = faceModule.CreateActiveConfiguration();
		faceConfig.SetTrackingMode(PXCMFaceConfiguration.TrackingModeType.FACE_MODE_COLOR_PLUS_DEPTH);
		faceConfig.detection.isEnabled = true;
		faceConfig.landmarks.isEnabled = true;
		faceConfig.ApplyChanges();
		faceConfig.Update();

		sts = senseMgr.Init();

		if (sts.isError()) {
			log.error("Init failed: " + sts);
			return;
		}

		PXCMCapture.Device dev = senseMgr.QueryCaptureManager().QueryDevice();
		PXCMCapture.DeviceInfo info = new PXCMCapture.DeviceInfo();
		dev.QueryDeviceInfo(info);
		log.debug("Using Camera: " + info.name);

		while (startCapture) {
			sts = senseMgr.AcquireFrame(true);
			PXCMCapture.Sample sample = senseMgr.QueryFaceSample();
			if (sample == null) {
				senseMgr.ReleaseFrame();
				continue;
			}

			BufferedImage frameImage = null;
			if (sample.color != null) {
				frameImage = drawFrameImage(sample);
			}

			faceData.Update();

			for (int fidx = 0;; fidx++) {
				PXCMFaceData.Face face = faceData.QueryFaceByIndex(fidx);
				if (face == null)
					break;
				PXCMFaceData.DetectionData detection = face.QueryDetection();
				float[] depthArray = new float[4];
				if (detection.QueryFaceAverageDepth(depthArray)) {
					// log.debug("QueryFaceAverageDepth=" + depthArray[0]);
				}

				if (detection != null) {
					PICData fd = createFaceData(frameImage, detection);
					if (fd != null) {
						if (fd.isDetectedFace() && currentIDCard != null) {
							fd.setIdCard(currentIDCard);
							FaceCheckingService.getInstance().offerDetectedFaceData(fd);
						}
						drawLocation(detection);
					}
				}

				// drawLandmark(face);
			}
			senseMgr.ReleaseFrame();
		}
	}

}
