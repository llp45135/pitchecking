package com.rxtec.pitchecking.picheckingservice.realsense;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;

import intel.rssdk.PXCMFaceData;
import intel.rssdk.PXCMFaceData.LandmarkPoint;
import intel.rssdk.PXCMFaceData.LandmarkType;
import intel.rssdk.PXCMFaceData.LandmarksData;
import intel.rssdk.PXCMFaceData.LandmarksGroupType;

public class FaceQualityDetectForRealSense {
	private static Logger log = LoggerFactory.getLogger("RSFaceTrackTask");

	public FaceQualityDetectForRealSense() {
		// TODO Auto-generated constructor stub
	}
	public static List<SortedFace> detectFaceQuality(PXCMFaceData faceData) {
		List<SortedFace> sortFaces = new ArrayList<SortedFace>();
		int faceCount = faceData.QueryNumberOfDetectedFaces();
		for (int i = 0; i < faceCount; i++) {
			int quality = 500 - i * 100;
			PXCMFaceData.Face face = faceData.QueryFaceByIndex(i);
			PXCMFaceData.PoseEulerAngles pae = checkFacePose(face.QueryPose());
			if (pae != null)
				quality += 10;
			SortedFace sr = new SortedFace(face, 0);
			sr.setQuality(quality);
			sortFaces.add(sr);
//			log.info("Face" + face.QueryUserID() + " quality=" + quality);
		}
		Collections.sort(sortFaces);
		return sortFaces;
	}
	
	
	private static PXCMFaceData.PoseEulerAngles checkFacePose(PXCMFaceData.PoseData poseData) {
		PXCMFaceData.PoseEulerAngles pea = new PXCMFaceData.PoseEulerAngles();
		if (poseData == null)
			return null;
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
	
	/**
	 * 
	 * @param sf
	 * @return
	 */
	public static boolean checkRealFace(SortedFace sf) {

		boolean isRealFace = false;
		PXCMFaceData.Face face = sf.face;
		if (face == null) {
			isRealFace = false;
			return isRealFace;
		}
		PXCMFaceData.LandmarksData landmarks = face.QueryLandmarks();
		if (landmarks == null) {
			isRealFace = false;
			// log.debug(sf.distance + " face landmarks == null");
			return isRealFace;
		}

		return checkFaceDepth(landmarks) & checkFaceWidth(landmarks);
	}

	
	/**
	 * 
	 * @param landmarks
	 * @return
	 */
	private static boolean checkFaceDepth(LandmarksData landmarks) {

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
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 
	 * @param landmarks
	 * @return
	 */
	private static boolean checkFaceWidth(LandmarksData landmarks) {
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
			return true;
		} else {
			return false;
		}
	}

	
	
}
