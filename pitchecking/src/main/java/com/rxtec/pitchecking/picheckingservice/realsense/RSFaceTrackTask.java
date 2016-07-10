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
import com.rxtec.pitchecking.gui.VideoPanel;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.PITData;

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
import intel.rssdk.PXCMRectI32;
import intel.rssdk.PXCMSenseManager;
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

	
	private PITData createFaceData(BufferedImage frame, PXCMFaceData.DetectionData detection) {
		if (frame == null)
			return null;
		PXCMRectI32 rect = new PXCMRectI32();
		PITData fd = new PITData(frame);
		boolean ret = detection.QueryBoundingRect(rect);
		if (ret) {
			int x, y, w, h;
			x = (int) (rect.x);
			y = (int) (rect.y * 0.9);
			w = (int) (rect.w * 1.2);
			h = (int) (rect.h * 1.5);

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
			g.dispose();
		}
	}

	private void drawLandmark(PXCMFaceData.Face face) {
		if (face == null)
			return ;
		PXCMFaceData.LandmarksData landmarks = face.QueryLandmarks();
		if(landmarks == null) return ;
		int npoints = landmarks.QueryNumPoints();
		PXCMFaceData.LandmarkPoint[] points = new PXCMFaceData.LandmarkPoint[npoints];
		for(int i=0;i<npoints;i++){
			points[i] = new LandmarkPoint();
		}
		landmarks.QueryPoints(points);

		Point point = new Point();
		Graphics2D graphics = (Graphics2D) videoPanel.getGraphics();
		for (PXCMFaceData.LandmarkPoint landmark : points) {
			if (landmark == null)
				continue;
			point.x = (int) (landmark.image.x + LandmarkAlignment);
			point.y = (int) (landmark.image.y + LandmarkAlignment);

			
//			log.debug("landmark.confidenceImage=" + landmark.confidenceImage +"  landmark.confidenceWorld=" + landmark.confidenceWorld );
			if (landmark.confidenceWorld == 0) {
				graphics.setColor(Color.RED);
				graphics.drawString("x", point.x, point.y);
			} else {
				graphics.setColor(Color.YELLOW);
				graphics.drawString("•", point.x, point.y);
			}
			
//			log.debug("landmark :" + landmark.source.alias +"  z=" + landmark.world.z);
		}
		
		
		graphics.dispose();
		landmarks = null;
	}
	
	
	private boolean checkRealFace(SortFace sf ){
		
		boolean isRealFace = false;
		PXCMFaceData.Face face = sf.face;
		if (face == null) {
			isRealFace = false;
			return isRealFace;
		}
		PXCMFaceData.LandmarksData landmarks = face.QueryLandmarks();
		if(landmarks == null) {
			isRealFace = false;
			log.debug(sf.distance + " face landmarks == null");
			return isRealFace;
		}

		return dValueDepthAndWidth(landmarks);
	}

	private boolean dValueDepthAndWidth(LandmarksData landmarks){
		
		int nJawPoints = landmarks.QueryNumPointsByGroup(LandmarksGroupType.LANDMARK_GROUP_JAW);
		PXCMFaceData.LandmarkPoint[] jawPoints = new PXCMFaceData.LandmarkPoint[nJawPoints];
		
		for(int i=0;i<nJawPoints;i++){
			jawPoints[i] = new LandmarkPoint();
		}

		int nLeftEyePoints = landmarks.QueryNumPointsByGroup(LandmarksGroupType.LANDMARK_GROUP_LEFT_EYE);
		PXCMFaceData.LandmarkPoint[] leftEyePoints = new PXCMFaceData.LandmarkPoint[nLeftEyePoints];
		
		for(int i=0;i<nLeftEyePoints;i++){
			leftEyePoints[i] = new LandmarkPoint();
		}
		
		landmarks.QueryPointsByGroup(LandmarksGroupType.LANDMARK_GROUP_JAW, jawPoints);
		landmarks.QueryPointsByGroup(LandmarksGroupType.LANDMARK_GROUP_LEFT_EYE, leftEyePoints);
		
		float d1=0,d2=0;
		
		for(LandmarkPoint p : jawPoints){
			d1 += p.world.z;
		}
		
		d1 = d1 / nJawPoints;
		
		for(LandmarkPoint p : leftEyePoints){
			d2 += p.world.z;
		}
		
		d2 = d2 / nLeftEyePoints;
		
		float zDIF = Math.abs(d1 - d2) * 1000;
		
		int faceBorderLeftIdx = landmarks.QueryPointIndex(LandmarkType.LANDMARK_FACE_BORDER_TOP_LEFT);
		int faceBorderRightIdx = landmarks.QueryPointIndex(LandmarkType.LANDMARK_FACE_BORDER_TOP_RIGHT);
		LandmarkPoint pLeftBorder = new LandmarkPoint();
		LandmarkPoint pRightBorder = new LandmarkPoint();
		
		landmarks.QueryPoint(faceBorderLeftIdx, pLeftBorder) ;
		landmarks.QueryPoint(faceBorderRightIdx, pRightBorder) ;
		
		float wDIF = Math.abs(pLeftBorder.world.x - pRightBorder.world.x) * 1000;
		log.debug("wDIF=" + wDIF + "	zDIF=" + zDIF);
		
//		if(checkFaceDepth(zDIF) && checkFaceWidth(wDIF)) return true;
//		else return false;
		
		
		return checkFaceWidth(wDIF);
		
	}
	
	
	private boolean checkFaceDepth(float f){
		if(Config.DValueMinWidth< f && f<Config.DValueMaxDepth) return true;
		else return false;
	}
	
	private boolean checkFaceWidth(float f){
		if(Config.DValueMinWidth< f && f<Config.DValueMaxWidth) return true;
		else return false;
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
		log.debug("beginCheckingFace......");

	}
	public void stopCheckingFace() {
		currentIDCard = null;
		FaceCheckingService.getInstance().resetFaceDataQueue();
		log.debug("stopCheckingFace......");
	}

	private IDCard currentIDCard = null;

	public IDCard getCurrentIDCard() {
		return currentIDCard;
	}

	public void setCurrentIDCard(IDCard currentIDCard) {
		this.currentIDCard = currentIDCard;
	}


	private void doTracking() {
		PXCMSenseManager senseMgr = PXCMSenseManager.CreateInstance();

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
		faceConfig.SetTrackingMode(PXCMFaceConfiguration.TrackingModeType.FACE_MODE_COLOR_PLUS_DEPTH);
		faceConfig.strategy = PXCMFaceConfiguration.TrackingStrategyType.STRATEGY_CLOSEST_TO_FARTHEST;
		faceConfig.detection.isEnabled = true;
		faceConfig.detection.maxTrackedFaces = Config.MaxTrackedFaces;
		faceConfig.landmarks.maxTrackedFaces = Config.MaxTrackedLandmark;
		faceConfig.landmarks.numLandmarks = Config.MaxTrackedLandmark;
		faceConfig.landmarks.isEnabled = true;
		faceConfig.pose.isEnabled = true;
		faceConfig.pose.maxTrackedFaces = Config.MaxTrackedFaces;
		faceConfig.Update();
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
			
			List<SortFace> sortFaces = sortFaceByDistence(faceData);
			for (SortFace sf : sortFaces) {
				PXCMFaceData.Face face = sf.face;
				
//				log.debug(sf.distance + "  SortFace'size=" + sortFaces.size());
				PXCMFaceData.DetectionData detection = face.QueryDetection();
				
				drawLocation(detection);
				boolean isRealFace = true;
				if(Config.getInstance().getIsCheckRealFace() == 1){
					isRealFace = checkRealFace(sf);
					drawLandmark(face);
				}
				if (detection != null && isRealFace) {
					PITData fd = createFaceData(frameImage, detection);
					if (fd != null) {
						if (fd.isDetectedFace() && currentIDCard != null) {
							fd.setIdCard(currentIDCard);
							FaceCheckingService.getInstance().offerDetectedFaceData(fd);
						}
					}
				}
			}
			senseMgr.ReleaseFrame();
		}
	}
	
	private List<SortFace> sortFaceByDistence(PXCMFaceData faceData){
		List<SortFace> sortFaces = new ArrayList<SortFace>();
		int faceCount = faceData.QueryNumberOfDetectedFaces();
		for(int i=0;i<faceCount;i++){
			PXCMFaceData.Face face = faceData.QueryFaceByIndex(i);
			
			PXCMRectI32 rect = new PXCMRectI32();
			PXCMFaceData.DetectionData detection = face.QueryDetection();
			boolean ret = detection.QueryBoundingRect(rect);
			if(ret){
				
				int w = rect.w;
//				log.debug("face width = " + w);
				float[] averageDepth = new float[1];
				detection.QueryFaceAverageDepth(averageDepth);
				float distance = averageDepth[0];

				
				
				if(distance>Config.MinAverageDepth && distance<Config.MaxAverageDepth){
					SortFace sf = new SortFace(face, distance);
					sortFaces.add(sf);
					
				}
				
			}
			
		}
		
		Collections.sort(sortFaces);
		 
		return sortFaces;
				
				
	}

}

class SortFace implements Comparable<SortFace>{
	public PXCMFaceData.Face face;
	public float distance = 0;
	
	public SortFace(PXCMFaceData.Face face,float distance){
		this.face = face;
		this.distance = distance;
		
	}

	@Override
	public int compareTo(SortFace o) {
		if(distance < o.distance)
			return 0;
		else 
			return 1;
	}
	
}


