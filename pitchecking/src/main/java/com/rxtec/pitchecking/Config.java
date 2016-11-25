package com.rxtec.pitchecking;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
	public static String UVCCameraName = "Logitech HD Pro Webcam C920";

	public static String PIXELFaceVerifyDLLName = "FaceVRISDK.dll";
	public static String MICROFaceVerifyCloneDLLName = "micropattern/MPALLibFaceRecFInf.dll";

	public static int FrameWidth = 960;
	public static int FrameHeigh = 540;
	public static int IRFrameWidth = 640;
	public static int IRFrameHeigh = 480;
	public static int FACE_TRACK_IR = 1;
	public static int FACE_TRACK_COLOR_DEPTH = 2;
	public static int FACE_TRACK_COLOR = 3;
	

	public static int frameRate = 30;

	public static float FACE_POSE_YAW = 15;
	public static float FACE_POSE_PITCH = 15;
	public static float FACE_POSE_ROLL = 15;

	public static int Is_Check_RealFace = 1;

	public static float DValueMaxDepth = 100F;
	public static float DValueMinDepth = 20F;

	public static float DValueMaxWidth = 250F;
	public static float DValueMinWidth = 90F;

	public static int MaxTrackedFaces = 4;
	public static int MaxTrackedLandmark = 4;
	public static int NumOfLandmarks = 78;

	public static int PIVerify_Begin_STREAM_ID = 10; // 通知独立人脸比对进程开始人脸比对 用于睿新版本
	public static int PIVerify_Result_STREAM_ID = 11; // 独立人脸比对进程发布比对结果 用于睿新版本

	public static int PIVerifyEvent_STREAM_ID = 12; // 通知独立人脸比对进程开始人脸比对 用于铁科版本
	public static int PIVerifyResultEvent_STREAM_ID = 13; // 独立人脸比对进程发布比对结果, 用于铁科版本
	
	

	public static String PIVerify_CHANNEL = "aeron:ipc";
	// public static String PIVerify_CHANNEL =
	// "aeron:udp?endpoint=192.168.1.21:40456";

	public static int IDReaderEvent = 1;
	public static int QRReaderEvent = 2;

	public static int TicketVerifyWaitInput = 0; // 等待票证验证数据
	public static int TicketVerifySucc = 1; // 票证验证成功
	public static int TicketVerifyIDFail = -1; // 票证验证失败
	public static int TicketVerifyStationRuleFail = -2; // 票证验证失败：非本站乘车
	public static int TicketVerifyTrainDateRuleFail = -3; // 票证验证失败：非当日乘车

	public static int StartStatus = 1;
	public static int StopStatus = 0;

	public static String MongoDBName = "pitcheck";
	public static String PassedMongoCollectionName = "passed_record";
	public static String FailedMongoCollectionName = "failed_record";

	public static String FaceVerifyMicro = "MICRO";
	public static String FaceVerifyPIXEL = "PIXEL";

	private int HEART_BEAT_DELAY = 15 * 1000;

	public static int ByPassMaxAge = 65;
	public static int ByPassMinAge = 12;

	private String heartBeatLogFile = "D:/pitchecking/work/HEART.log";
	private String rebackTrackFile = "D:/pitchecking/work/RebackFlag.log";
	private String pausePitcheckFile = "D:/pitchecking/work/PAUSE.log";
	private String startPITVerifyCmd = "D:/pitchecking/work/RestartPITVerify.bat";
	private String startPITTrackCmd = "D:/pitchecking/work/RestartPITTrack.bat";
	public static String AutoRestartCmd = "D:/pitchecking/work/autoRestartPC.bat";
	public static String AutoLogonCmd = "D:/pitchecking/work/autoLogonPC.bat";
	private String KillTKExeCmd = "C:/AFC/shared/kill.exe";
	private String StartTKExeCmd = "C:/AFC/gui/gui.exe";

	// 人脸比对结果状态
	public static int VerifyPassedStatus = 0;
	public static int VerifyFailedStatus = 1;

	/************************************************************************************
	 * 铁科主控程序通过Aeron MediaDriver 发过来的事件名称定义
	 */

	public static String BeginVerifyFaceEvent = "CAM_Notify";
	public static String GetVerifyFaceResultEvent = "CAM_GetPhotoInfo";
	public static String GetVerifyFaceResultInnerEvent = "CAM_GetVerifyResult";
	public static String ScreenDisplayEvent = "CAM_ScreenDisplay";

	/**
	 * *************************************************************************
	 * ******
	 */

	private int faceVerifyThreads = 1; // 人脸比对线程数

	private float faceCheckThreshold = (float) 0.68;
	private float glassFaceCheckThreshold = (float) 0.65;
	/**
	 * 总超时时间
	 */
	private int faceCheckDelayTime = 20; // 总超时时间
	/**
	 * 延时通过时间
	 */
	private int checkDelayPassTime = 8; // 延时通过时间
	private int fastFaceDetect = 1;
	private int faceCheckingInteval = 100;
	private int defaultFaceCheckScreenDeley = 1000;
	private String imagesLogDir;
	private String policeJsonDir;
	private int roateCapture = 0;
	private int detectededFaceQueueLen = 5;
	private int detectededFaceQueueThreshold = 2;
	private int videoType = 1;
	private int videoCaptureFrequency = 10;
	private int isCheckRealFace = 0;
	private String multicastAddress = "234.5.6.7";
	private int faceLogRemainDays = 14;
	private int isUseMongoDB = 1;
	private String mongoDBAddress = "localhost";
	private int mongoDBPort = 27017;
	private int isSaveFaceImageToLocaldisk = 1;
	private String faceVerifyType = "MICRO";
	private float faceFrameTransparency = (float) 0.75;
	private String PITTrackPidstr = "-1"; // 人脸检测进程号
	private String trackPidForKill = "-1";
	private boolean rebackTrackFlag = true;
	private int isUseManualMQ=0;
	private int isUseMySQLDB = 0;
	private int isPlayHelpAudio = 1;  //是否播放引导语音
	private int isStartMainListener = 1;  //是否允许启动睿新版本的主控线程
	private int isUsePoliceMQ=0;
	private int isUseGatDll=0;
	private int isResetVerifyScreen=1;

	/************************************************************
	 * 人脸检测-比对任务 版本 人脸检测-比对任务有两个版本： RX=睿新版本用于睿新自有java版本闸机主控程序 TK=铁科版本主控程序
	 */

	public static String FaceVerifyTaskTKVersion = "TK";
	public static String FaceVerifyTaskRXVersion = "RX";

	private String faceVerifyTaskVersion = "TK";

	public String getFaceVerifyTaskVersion() {
		return faceVerifyTaskVersion;
	}

	public void setFaceVerifyTaskVersion(String faceVerifyTaskVersion) {
		faceVerifyTaskVersion = faceVerifyTaskVersion;
	}

	/***********************************************************/

	private float minAverageDepth = 400F;
	private float maxAverageDepth = 1000F;

	private int faceTrackMode = 1; // 人臉追蹤模式 1 紅外，2 顔色+景深

	public int getIsResetVerifyScreen() {
		return isResetVerifyScreen;
	}

	public void setIsResetVerifyScreen(int isResetVerifyScreen) {
		this.isResetVerifyScreen = isResetVerifyScreen;
	}

	public int getIsUseGatDll() {
		return isUseGatDll;
	}

	public void setIsUseGatDll(int isUseGatDll) {
		this.isUseGatDll = isUseGatDll;
	}

	public String getPoliceJsonDir() {
		return policeJsonDir;
	}

	public void setPoliceJsonDir(String policeJsonDir) {
		this.policeJsonDir = policeJsonDir;
	}

	public int getIsUsePoliceMQ() {
		return isUsePoliceMQ;
	}

	public void setIsUsePoliceMQ(int isUsePoliceMQ) {
		this.isUsePoliceMQ = isUsePoliceMQ;
	}

	public int getIsStartMainListener() {
		return isStartMainListener;
	}

	public void setIsStartMainListener(int isStartMainListener) {
		this.isStartMainListener = isStartMainListener;
	}

	public int getIsPlayHelpAudio() {
		return isPlayHelpAudio;
	}

	public void setIsPlayHelpAudio(int isPlayHelpAudio) {
		this.isPlayHelpAudio = isPlayHelpAudio;
	}

	public String getKillTKExeCmd() {
		return KillTKExeCmd;
	}

	public void setKillTKExeCmd(String killTKExeCmd) {
		KillTKExeCmd = killTKExeCmd;
	}

	public String getStartTKExeCmd() {
		return StartTKExeCmd;
	}

	public void setStartTKExeCmd(String startTKExeCmd) {
		StartTKExeCmd = startTKExeCmd;
	}

	public String getPausePitcheckFile() {
		return pausePitcheckFile;
	}

	public void setPausePitcheckFile(String pausePitcheckFile) {
		this.pausePitcheckFile = pausePitcheckFile;
	}

	public boolean isRebackTrackFlag() {
		return rebackTrackFlag;
	}

	public void setRebackTrackFlag(boolean rebackTrackFlag) {
		this.rebackTrackFlag = rebackTrackFlag;
	}

	public String getRebackTrackFile() {
		return rebackTrackFile;
	}

	public void setRebackTrackFile(String rebackTrackFile) {
		this.rebackTrackFile = rebackTrackFile;
	}

	public int getHEART_BEAT_DELAY() {
		return HEART_BEAT_DELAY;
	}

	public void setHEART_BEAT_DELAY(int hEART_BEAT_DELAY) {
		HEART_BEAT_DELAY = hEART_BEAT_DELAY;
	}

	public String getTrackPidForKill() {
		return trackPidForKill;
	}

	public void setTrackPidForKill(String trackPidForKill) {
		this.trackPidForKill = trackPidForKill;
	}

	public String getPITTrackPidstr() {
		return PITTrackPidstr;
	}

	public void setPITTrackPidstr(String pITTrackPidstr) {
		PITTrackPidstr = pITTrackPidstr;
	}

	public float getFaceFrameTransparency() {
		return faceFrameTransparency;
	}

	public void setFaceFrameTransparency(float faceFrameTransparency) {
		this.faceFrameTransparency = faceFrameTransparency;
	}

	public int getIsUseMongoDB() {
		return isUseMongoDB;
	}

	public void setIsUseMongoDB(int isUseMongoDB) {
		this.isUseMongoDB = isUseMongoDB;
	}

	private float faceDetectionScale = 1.5f; // 人臉放大倍數

	public float getFaceDetectionScale() {
		return faceDetectionScale;
	}



	public String getStartPITTrackCmd() {
		return startPITTrackCmd;
	}

	public void setStartPITTrackCmd(String startPITTrackCmd) {
		this.startPITTrackCmd = startPITTrackCmd;
	}

	public String getStartPITVerifyCmd() {
		return startPITVerifyCmd;
	}

	public void setStartPITVerifyCmd(String startPITVerifyCmd) {
		this.startPITVerifyCmd = startPITVerifyCmd;
	}

	public void setFaceDetectionScale(float faceDetectionScale) {
		this.faceDetectionScale = faceDetectionScale;
	}

	public String getHeartBeatLogFile() {
		return heartBeatLogFile;
	}

	public void setHeartBeatLogFile(String heartBeatLogFile) {
		this.heartBeatLogFile = heartBeatLogFile;
	}

	public int getFaceTrackMode() {
		return faceTrackMode;
	}

	public void setFaceTrackMode(int faceTrackMode) {
		this.faceTrackMode = faceTrackMode;
	}

	public float getMinAverageDepth() {
		return minAverageDepth;
	}

	public void setMinAverageDepth(float minAverageDepth) {
		this.minAverageDepth = minAverageDepth;
	}

	public float getMaxAverageDepth() {
		return maxAverageDepth;
	}

	public void setMaxAverageDepth(float maxAverageDepth) {
		this.maxAverageDepth = maxAverageDepth;
	}

	public String getFaceVerifyType() {
		return faceVerifyType;
	}

	public void setFaceVerifyType(String faceVerifyType) {
		this.faceVerifyType = faceVerifyType;
	}

	public String getMongoDBAddress() {
		return mongoDBAddress;
	}

	public void setMongoDBAddress(String mongoDBAddress) {
		this.mongoDBAddress = mongoDBAddress;
	}

	public int getMongoDBPort() {
		return mongoDBPort;
	}

	public void setMongoDBPort(int mongoDBPort) {
		this.mongoDBPort = mongoDBPort;
	}

	public int getFaceLogRemainDays() {
		return faceLogRemainDays;
	}

	public void setFaceLogRemainDays(int faceLogRemainDays) {
		this.faceLogRemainDays = faceLogRemainDays;
	}

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

	private int multicastPort = 8899;

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

	public int getCheckDelayPassTime() {
		return checkDelayPassTime;
	}

	public void setCheckDelayPassTime(int checkDelayPassTime) {
		this.checkDelayPassTime = checkDelayPassTime;
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

	public int getIsSaveFaceImageToLocaldisk() {
		return isSaveFaceImageToLocaldisk;
	}

	public void setIsSaveFaceImageToLocaldisk(int isSaveFaceImageToLocaldisk) {
		this.isSaveFaceImageToLocaldisk = isSaveFaceImageToLocaldisk;
	}

	public int getIsUseManualMQ() {
		return isUseManualMQ;
	}

	public void setIsUseManualMQ(int isUseManualMQ) {
		this.isUseManualMQ = isUseManualMQ;
	}

	public int getIsUseMySQLDB() {
		return isUseMySQLDB;
	}

	public void setIsUseMySQLDB(int isUseMySQLDB) {
		this.isUseMySQLDB = isUseMySQLDB;
	}

	private static Config _instance = new Config();

	private Config() {
		Properties p = new Properties();
		try {

			String filePath = Thread.currentThread().getContextClassLoader().getResource("").toString();
			String fn = filePath + "conf/conf.properties";
			URL url = new URL(fn);
			FileInputStream is = new FileInputStream(url.getFile());
			p.load(is);
			this.faceCheckThreshold = Float.valueOf(p.getProperty("FaceCheckThreshold", "0.7"));
			this.glassFaceCheckThreshold = Float.valueOf(p.getProperty("GlassFaceCheckThreshold", "0.68"));
			this.faceCheckDelayTime = Integer.valueOf(p.getProperty("FaceCheckDelayTime", "20"));
			this.checkDelayPassTime = Integer.valueOf(p.getProperty("CheckDelayPassTime", "10"));
			this.fastFaceDetect = Integer.valueOf(p.getProperty("FastFaceDetect", "1"));
			this.faceCheckingInteval = Integer.valueOf(p.getProperty("faceCheckingInteval", "100"));
			this.defaultFaceCheckScreenDeley = Integer.valueOf(p.getProperty("DefaultFaceCheckScreenDeley", "1000"));
			this.imagesLogDir = p.getProperty("ImagesLogDir", "C:/pitchecking/images");
			this.policeJsonDir = p.getProperty("policeJsonDir", "C:/pitchecking/json");
			this.roateCapture = Integer.valueOf(p.getProperty("RoateCapture", "0"));
			this.detectededFaceQueueLen = Integer.valueOf(p.getProperty("DetectededFaceQueueLen", "5"));
			this.detectededFaceQueueThreshold = Integer.valueOf(p.getProperty("DetectededFaceQueueThreshold", "2"));
			this.videoType = Integer.valueOf(p.getProperty("VideoType", "2"));
			this.videoCaptureFrequency = Integer.valueOf(p.getProperty("VideoCaptureFrequency", "5"));
			this.isCheckRealFace = Integer.valueOf(p.getProperty("IsCheckRealFace", "0"));
			this.faceVerifyThreads = Integer.valueOf(p.getProperty("FaceVerifyThreads", "1"));
			this.faceLogRemainDays = Integer.valueOf(p.getProperty("FaceLogRemainDays", "14"));
			this.isUseMongoDB = Integer.valueOf(p.getProperty("isUseMongoDB", "1"));
			this.mongoDBPort = Integer.valueOf(p.getProperty("MongoDBPort", "27017"));
			this.mongoDBAddress = p.getProperty("MongoDBAddress", "localhost");
			this.isSaveFaceImageToLocaldisk = Integer.valueOf(p.getProperty("IsSaveFaceImageToLocaldisk", "1"));
			this.faceVerifyType = p.getProperty("FaceVerifyType", "PIXEL");
			this.maxAverageDepth = Integer.valueOf(p.getProperty("MaxAverageDepth", "1000"));
			this.minAverageDepth = Integer.valueOf(p.getProperty("MinAverageDepth", "400"));
			this.faceTrackMode = Integer.valueOf(p.getProperty("FaceTrackMode", "1"));
			this.faceDetectionScale = Float.valueOf(p.getProperty("FaceDetectionScale", "1.3"));
			this.pausePitcheckFile = p.getProperty("pausePitcheckFile", "D:/pitchecking/work/PAUSE.log");
			this.heartBeatLogFile = p.getProperty("HeartBeatLogFile", "D:/pitchecking/work/HEART.log");
			this.rebackTrackFile = p.getProperty("rebackTrackFile", "D:/pitchecking/work/RebackFlag.log");
			this.startPITTrackCmd = p.getProperty("StartPITTrackCmd", "D:/pitchecking/work/RestartPITTrack.bat");
			this.startPITVerifyCmd = p.getProperty("StartPITVerifyCmd", "D:/pitchecking/work/RestartPITVerify.bat");
			this.KillTKExeCmd = p.getProperty("KillTKExeCmd", "C:/AFC/shared/kill.exe");
			this.StartTKExeCmd = p.getProperty("StartTKExeCmd", "C:/AFC/gui/gui.exe");
			this.faceVerifyTaskVersion = p.getProperty("FaceVerifyTaskVersion", "TK");
			this.faceFrameTransparency = Float.valueOf(p.getProperty("faceFrameTransparency", "0.75"));
			this.HEART_BEAT_DELAY = Integer.valueOf(p.getProperty("HEART_BEAT_DELAY", "15000"));
			this.isUseManualMQ = Integer.valueOf(p.getProperty("isUseManualMQ", "0"));
			this.isUseMySQLDB = Integer.valueOf(p.getProperty("isUseMySQLDB", "0"));
			this.isPlayHelpAudio = Integer.valueOf(p.getProperty("isPlayHelpAudio", "1"));
			this.isStartMainListener = Integer.valueOf(p.getProperty("isStartMainListener", "1"));
			this.isUsePoliceMQ = Integer.valueOf(p.getProperty("isUsePoliceMQ", "0"));
			this.isUseGatDll = Integer.valueOf(p.getProperty("isUseGatDll", "0"));
			this.isResetVerifyScreen = Integer.valueOf(p.getProperty("isResetVerifyScreen", "1"));
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
		System.out.println("getIsCheckRealFace==" + Config.getInstance().getIsCheckRealFace());
		System.out.println("getHEART_BEAT_DELAY=="+Config.getInstance().getHEART_BEAT_DELAY());
		System.out.println("getImagesLogDir=="+Config.getInstance().getImagesLogDir());
		System.out.println("getPausePitcheckFile=="+Config.getInstance().getPausePitcheckFile());
	}

}
