package com.rxtec.pitchecking.picheckingservice.realsense;

import intel.rssdk.*;
import java.lang.System.*;
import java.util.*;
import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.utils.CommUtil;

import java.awt.event.*;
import java.awt.image.*;
import java.awt.*;

public class FaceTrackingNoneUI {
	static Logger log = LoggerFactory.getLogger("DeviceEventListener");
	public static void main(String s[]) throws java.io.IOException {
		PXCMSenseManager senseMgr = PXCMSenseManager.CreateInstance();

		if (senseMgr == null) {
			log.error("Failed to create a sense manager instance.");
			return;
		}

		pxcmStatus sts = senseMgr.EnableFace(null);
		PXCMFaceModule faceModule = senseMgr.QueryFace();

		if (sts.isError() || faceModule == null) {
			log.error("Failed to initialize face module.");
			return;
		}

		// Retrieve the input requirements
		sts = pxcmStatus.PXCM_STATUS_DATA_UNAVAILABLE;
		PXCMFaceConfiguration faceConfig = faceModule.CreateActiveConfiguration();
		faceConfig.SetTrackingMode(PXCMFaceConfiguration.TrackingModeType.FACE_MODE_COLOR_PLUS_DEPTH);
		faceConfig.detection.isEnabled = true;
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
		log.error("Using Camera: " + info.name);

		PXCMFaceData faceData = faceModule.CreateOutput();

		while (senseMgr.AcquireFrame(true).isSuccessful()) {
			CommUtil.sleep(1000);

			PXCMCapture.Sample sample = senseMgr.QueryFaceSample();

			// faceData = faceModule.CreateOutput();
			faceData.Update();

			// Read and print data
			for (int fidx = 0;; fidx++) {
				PXCMFaceData.Face face = faceData.QueryFaceByIndex(fidx);
				if (face == null)
					break;
				PXCMFaceData.DetectionData detectData = face.QueryDetection();

				if (detectData != null) {
					PXCMRectI32 rect = new PXCMRectI32();
					boolean ret = detectData.QueryBoundingRect(rect);
					if (ret) {
						//System.out.println("Face ID:" + face.QueryUserID() +"..............................................");
						//System.out.println("Top Left corner: (" + rect.x + "," + rect.y + ")");
						//计算人脸平均距离
						float[] depth = new float[1]; 
						detectData.QueryFaceAverageDepth(depth);
						
						log.info("queryUserID=="+face.QueryUserID() + ",rect.y==" + rect.y +",depth==" + depth[0]);
					}
				} else
					break;

//				PXCMFaceData.PoseData poseData = face.QueryPose();
//				if (poseData != null) {
//					PXCMFaceData.PoseEulerAngles pea = new PXCMFaceData.PoseEulerAngles();
//					poseData.QueryPoseAngles(pea);
//					System.out.println("(Roll, Yaw, Pitch) = (" + pea.roll + "," + pea.yaw + "," + pea.pitch + ")");
//				}
			}

			// faceData.close();
			senseMgr.ReleaseFrame();
		}
		faceData.close();
		senseMgr.Close();
		System.exit(0);
	}
}
