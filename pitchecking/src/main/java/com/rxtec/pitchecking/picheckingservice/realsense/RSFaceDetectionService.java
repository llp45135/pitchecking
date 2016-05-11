package com.rxtec.pitchecking.picheckingservice.realsense;

import intel.rssdk.*;
import intel.rssdk.PXCMCapture.Device;
import intel.rssdk.PXCMCapture.Sample;

import java.lang.System.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.gui.VideoPanel;
import com.rxtec.pitchecking.picheckingservice.PICData;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.FaceDetectionService;
import com.rxtec.pitchecking.picheckingservice.IDCard;
import com.rxtec.pitchecking.picheckingservice.IFaceTrackService;
import com.rxtec.pitchecking.utils.ImageToolkit;

import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.*;
import java.awt.*;

public class RSFaceDetectionService implements IFaceTrackService{

	static int cWidth = 640;
	static int cHeight = 480;
	static int dWidth, dHeight;
	static boolean exit = false;
	private Logger log = LoggerFactory.getLogger("RSCameraViewer");
	private boolean exist = false;
	private boolean startCapture = true;
	private boolean startTrackFace = true;
	private boolean enableExpression = true;
	private boolean enableFaceLandmark = true;
	
	RSFaceTrackTask trackTask ;


	private int maxTrackedFaces = 4;
	private static int LandmarkAlignment = -3;

	private RSModuleStatus rsStatus;



	public void setVideoPanel(JPanel videoPanel) {
		trackTask.setVideoPanel(videoPanel);
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



	private static RSFaceDetectionService _instance = null;

	private RSFaceDetectionService() {
		trackTask = new RSFaceTrackTask(null);
	}

	public static synchronized RSFaceDetectionService getInstance() {
		if (_instance == null)
			_instance = new RSFaceDetectionService();
		return _instance;
	}



	public void beginVideoCaptureAndTracking() {
		ExecutorService executer = Executors.newCachedThreadPool();
		executer.execute(trackTask);
	}
	

	public void beginCheckingFace(IDCard idCard) {
		currentIDCard = idCard;
		trackTask.beginCheckingFace(idCard);

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
		trackTask.stopCheckingFace();
		FaceCheckingService.getInstance().resetFaceDataQueue();
	}


}

class VideoPanelListener extends WindowAdapter {
	public boolean exit = false;

	@Override
	public void windowClosing(WindowEvent e) {
		exit = true;
	}
}

enum RSModuleStatus {
	Succ, InitError;
}
