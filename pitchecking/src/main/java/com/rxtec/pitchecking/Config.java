package com.rxtec.pitchecking;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.domain.StationInfo;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;

public class Config {
	private Logger log = LoggerFactory.getLogger("Config");

	public static int RealSenseVideo = 2;
	public static int UVCVideo = 1;
	public static String UVCCameraName="Logitech HD Pro Webcam C920";
	
	public static String FaceVerifyDLLName = "FaceVRISDK.dll";
	public static String FaceVerifyCloneDLLName = "FaceVRISDK.dll";
	
	
	public static int FrameWidth = 960;
	public static int FrameHeigh = 540;
	public static int IRFrameWidth = 640;
	public static int IRFrameHeigh = 480;

	public static int frameRate = 30;
	
	public static float MinAverageDepth = 400F;
	public static float MaxAverageDepth = 1200F;
	
	public static float DValueMaxDepth = 100F;
	public static float DValueMinDepth = 20F;
	
	public static float DValueMaxWidth = 250F;
	public static float DValueMinWidth = 90F;
	
	
	public static int MaxTrackedFaces = 4;
	public static int MaxTrackedLandmark = 4;
	public static int NumOfLandmarks = 78;
	
	
	
	public static int PIVerify_Send_STREAM_ID = 10;
	public static int PIVerify_Receive_STREAM_ID = 11;
	public static String PIVerify_CHANNEL = "aeron:ipc";
//	public static String PIVerify_CHANNEL = "aeron:udp?endpoint=192.168.1.21:40456";
	
	
	
	
	
	
	public static int IDReaderEvent = 1;
	public static int QRReaderEvent = 2;

	public static int TicketVerifyWaitInput = 0;			//等待票证验证数据
	public static int TicketVerifySucc = 1;					//票证验证成功
	public static int TicketVerifyIDFail = -1;				//票证验证失败
	public static int TicketVerifyStationRuleFail = -2;		//票证验证失败：车站业务规则
	
	public static int StartStatus = 1;
	public static int StopStatus = 0;
	
	private int faceVerifyThreads = 2;						//人脸比对线程数


	private float faceCheckThreshold = (float) 0.68;
	private float glassFaceCheckThreshold = (float) 0.65;
	private int faceCheckDelayTime = 10;
	private int fastFaceDetect = 1;
	private int faceCheckingInteval = 100;
	private int defaultFaceCheckScreenDeley = 1000;
	private String imagesLogDir;
	private int roateCapture = 0;
	private int detectededFaceQueueLen = 5;
	private int detectededFaceQueueThreshold = 2;
	private int videoType = 1;
	private int videoCaptureFrequency=10;
	private int isCheckRealFace = 0;
	private String multicastAddress="234.5.6.7";
	
	
	public int getFaceVerifyThreads() {
		return faceVerifyThreads;
	}

	public void setFaceVerifyThreads(int faceVerifyThreads) {
		this.faceVerifyThreads = faceVerifyThreads;
	}

	public int getIsCheckRealFace() {
		return isCheckRealFace;
	}

	public void setIsCheckRealFace(int isCheckRealFace) {
		this.isCheckRealFace = isCheckRealFace;
	}

	
	public String getMulticastAddress() {
		return multicastAddress;
	}

	public void setMulticastAddress(String multicastAddress) {
		this.multicastAddress = multicastAddress;
	}

	public int getMulticastPort() {
		return multicastPort;
	}

	public void setMulticastPort(int multicastPort) {
		this.multicastPort = multicastPort;
	}

	private int multicastPort=8899;

	//
	
	
	public int getVideoCaptureFrequency() {
		return videoCaptureFrequency;
	}

	public void setVideoCaptureFrequency(int videoCaptureFrequency) {
		this.videoCaptureFrequency = videoCaptureFrequency;
	}

	public int getDetectededFaceQueueThreshold() {
		return detectededFaceQueueThreshold;
	}

	public void setDetectededFaceQueueThreshold(int detectededFaceQueueThreshold) {
		this.detectededFaceQueueThreshold = detectededFaceQueueThreshold;
	}

	public int getDetectededFaceQueueLen() {
		return detectededFaceQueueLen;
	}

	public void setDetectededFaceQueueLen(int detectededFaceQueueLen) {
		this.detectededFaceQueueLen = detectededFaceQueueLen;
	}

	public int getRoateCapture() {
		return roateCapture;
	}

	public void setRoateCapture(int roateCapture) {
		this.roateCapture = roateCapture;
	}

	public String getImagesLogDir() {
		return imagesLogDir;
	}

	public void setImagesLogDir(String imagesLogDir) {
		this.imagesLogDir = imagesLogDir;
	}

	public int getFaceCheckDelayTime() {
		return faceCheckDelayTime;
	}

	public void setFaceCheckDelayTime(int faceCheckDelayTime) {
		this.faceCheckDelayTime = faceCheckDelayTime;
	}

	public int getDefaultFaceCheckScreenDeley() {
		return defaultFaceCheckScreenDeley;
	}

	public void setDefaultFaceCheckScreenDeley(int defaultFaceCheckScreenDeley) {
		this.defaultFaceCheckScreenDeley = defaultFaceCheckScreenDeley;
	}

	public int getFaceCheckingInteval() {
		return faceCheckingInteval;
	}

	public void setFaceCheckingInteval(int faceCheckingInteval) {
		this.faceCheckingInteval = faceCheckingInteval;
	}

	public int getFastFaceDetect() {
		return fastFaceDetect;
	}

	public void setFastFaceDetect(int fastFaceDetect) {
		this.fastFaceDetect = fastFaceDetect;
	}

	public float getFaceCheckThreshold() {
		return faceCheckThreshold;
	}

	public void setFaceCheckThreshold(float faceCheckThreshold) {
		this.faceCheckThreshold = faceCheckThreshold;
	}

	public float getGlassFaceCheckThreshold() {
		return glassFaceCheckThreshold;
	}

	public void setGlassFaceCheckThreshold(float glassFaceCheckThreshold) {
		this.glassFaceCheckThreshold = glassFaceCheckThreshold;
	}

	public int getVideoType() {
		return videoType;
	}

	public void setVideoType(int videoType) {
		this.videoType = videoType;
	}

	private static Config _instance = new Config();

	private Config() {
		Properties p = new Properties();
		try {
			InputStream is = new FileInputStream("./conf/conf.properties");
			p.load(is);
			this.faceCheckThreshold = Float.valueOf(p.getProperty("FaceCheckThreshold", "0.7"));
			this.glassFaceCheckThreshold = Float.valueOf(p.getProperty("GlassFaceCheckThreshold", "0.68"));
			this.faceCheckDelayTime = Integer.valueOf(p.getProperty("FaceCheckDelayTime", "10"));
			this.fastFaceDetect = Integer.valueOf(p.getProperty("FastFaceDetect", "1"));
			this.faceCheckingInteval = Integer.valueOf(p.getProperty("faceCheckingInteval", "100"));
			this.defaultFaceCheckScreenDeley = Integer.valueOf(p.getProperty("DefaultFaceCheckScreenDeley", "1000"));
			this.imagesLogDir = p.getProperty("ImagesLogDir", "C:/pitchecking/images");
			this.roateCapture = Integer.valueOf(p.getProperty("RoateCapture", "0"));
			this.detectededFaceQueueLen = Integer.valueOf(p.getProperty("DetectededFaceQueueLen", "5"));
			this.detectededFaceQueueThreshold = Integer.valueOf(p.getProperty("DetectededFaceQueueThreshold", "2"));
			this.videoType = Integer.valueOf(p.getProperty("VideoType", "2"));
			this.videoCaptureFrequency = Integer.valueOf(p.getProperty("VideoCaptureFrequency", "5"));
			this.isCheckRealFace = Integer.valueOf(p.getProperty("IsCheckRealFace", "0"));
			this.faceVerifyThreads = Integer.valueOf(p.getProperty("FaceVerifyThreads", "1"));
			is.close(); // 关闭流
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	public static synchronized Config getInstance() {
		if (_instance == null)
			_instance = new Config();
		return _instance;
	}	

	public static void main(String[] args) {
		Config.getInstance();
		System.out.println("getIsCheckRealFace=="+Config.getInstance().getIsCheckRealFace());
	}

}
