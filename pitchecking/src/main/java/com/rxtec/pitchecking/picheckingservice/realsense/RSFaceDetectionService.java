package com.rxtec.pitchecking.picheckingservice.realsense;

import intel.rssdk.*;
import intel.rssdk.PXCMCapture.Device;
import intel.rssdk.PXCMCapture.Sample;
import sun.security.krb5.Config;

import java.lang.System.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.gui.VideoPanel;
import com.rxtec.pitchecking.picheckingservice.PITData;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.FaceDetectionService;
import com.rxtec.pitchecking.picheckingservice.IFaceTrackService;
import com.rxtec.pitchecking.utils.ImageToolkit;

import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.*;
import java.awt.*;

public class RSFaceDetectionService implements IFaceTrackService {
	private static RSFaceDetectionService _instance = null;
	static int cWidth = 640;
	static int cHeight = 480;
	static int dWidth, dHeight;
	static boolean exit = false;
	private Logger log = LoggerFactory.getLogger("RSFaceDetectionService");
	private boolean exist = false;
	private boolean startCapture = true;
	private boolean startTrackFace = true;
	private boolean enableExpression = true;
	private boolean enableFaceLandmark = true;

	Object trackTask;
	private IDCard currentIDCard = null;
	private Ticket currentTicket = null;

	private int maxTrackedFaces = 4;
	private static int LandmarkAlignment = -3;

	// private RSModuleStatus rsStatus;
	private int cameraMode = -1; // 摄像头模式
	private int cameraPosition = 2; // 摄像头位置 1：前置 2：后置
	// --------------------------------------------------

	public void setVideoPanel(JPanel videoPanel) {
		if (cameraPosition == 2) {
			((RSFaceTrackTask) trackTask).setVideoPanel(videoPanel);
		}
		if (cameraPosition == 1) {
			((RSFaceFrontTrackTask) trackTask).setVideoPanel(videoPanel);
		}
	}

	public int getCameraPosition() {
		return cameraPosition;
	}

	public void setCameraPosition(int cameraPosition) {
		this.cameraPosition = cameraPosition;
	}

	public int getCameraMode() {
		return cameraMode;
	}

	public void setCameraMode(int cameraMode) {
		this.cameraMode = cameraMode;
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

	private RSFaceDetectionService() {
		if (FaceCheckingService.getInstance().isFrontCamera()) {  //前置摄像头
			this.cameraPosition = 1;
		}
		if (cameraPosition == 2) {
			trackTask = new RSFaceTrackTask(null);
		}
		if (cameraPosition == 1) {
			trackTask = new RSFaceFrontTrackTask(null);
		}
	}

	public static synchronized RSFaceDetectionService getInstance() {
		if (_instance == null)
			_instance = new RSFaceDetectionService();
		return _instance;
	}

	public void setDeviceProperties(RealsenseDeviceProperties props) {
		if (trackTask != null) {
			if (cameraPosition == 2) {
				((RSFaceTrackTask) trackTask).setupColorCameraDevice(props);
			}
			if (cameraPosition == 1) {
				((RSFaceFrontTrackTask) trackTask).setupColorCameraDevice(props);
			}
		}
	}

	/**
	 * RealSense输出视频流同时开始检脸
	 */
	public void beginVideoCaptureAndTracking() {
		try {
			if (cameraPosition == 2) {
				((RSFaceTrackTask) trackTask).doTracking();
			}
			if (cameraPosition == 1) {
				((RSFaceFrontTrackTask) trackTask).doTracking();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("trackTask.doTracking:", e);
		}
	}

	public void beginCheckingFace(IDCard idCard, Ticket t) {
		FaceCheckingService.getInstance().resetFaceDataQueue(); // by
																// zhao-20160705
		currentIDCard = idCard;
		currentTicket = t;
		if (cameraPosition == 2) {
			((RSFaceTrackTask) trackTask).beginCheckingFace(idCard, currentTicket);
		}
		if (cameraPosition == 1) {
			((RSFaceFrontTrackTask) trackTask).beginCheckingFace(idCard, currentTicket);
		}

	}

	public void stopCheckingFace() {
		currentIDCard = null;
		currentTicket = null;
		if (cameraPosition == 2) {
			((RSFaceTrackTask) trackTask).stopCheckingFace();
		}
		if (cameraPosition == 1) {
			((RSFaceFrontTrackTask) trackTask).stopCheckingFace();
		}
		FaceCheckingService.getInstance().resetFaceDataQueue();
	}

	public IDCard getCurrentIDCard() {
		return currentIDCard;
	}

	public void setCurrentIDCard(IDCard currentIDCard) {
		this.currentIDCard = currentIDCard;
	}

}

// class VideoPanelListener extends WindowAdapter {
// public boolean exit = false;
//
// @Override
// public void windowClosing(WindowEvent e) {
// exit = true;
// }
// }

// enum RSModuleStatus {
// Succ, InitError;
// }
