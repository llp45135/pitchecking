package com.rxtec.pitchecking;

import java.io.File;
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
	public static int PIVerifyResultEvent_STREAM_ID = 13; // 独立人脸比对进程发布比对结果 用于铁科版本

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
	public static int TicketVerifyNotStartCheckFail = -4; // 票证验证失败，未开始检票
	public static int TicketVerifyStopCheckFail = -5; // 票证验证失败，已经停止检票

	public static int StartStatus = 1;
	public static int StopStatus = 0;

	public static String MongoDBName = "pitcheck";
	public static String PassedMongoCollectionName = "passed_record";
	public static String FailedMongoCollectionName = "failed_record";

	public static String FaceVerifyMicro = "MICRO";
	public static String FaceVerifyPIXEL = "PIXEL";
	public static String FaceVerifyEASEN = "EASEN";

	private int HEART_BEAT_DELAY = 15 * 1000;

	public static int ByPassMaxAge = 65;
	public static int ByPassMinAge = 12;

	private String heartBeatLogFile = "D:/pitchecking/work/HEART.log"; // 检脸进程心跳日志
	private String ticketVerifyHeartFile = "D:/pitchecking/work/TicketVerifyHeart.log";
	private int TicketVerify_Heard_DELAY = 3 * 1000;
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
	private String multicastAddress = "234.5.6.7"; //
	private int faceLogRemainDays = 14; // 照片保留天数
	private int isUseMongoDB = 1; // 是否保存照片至mongodb
	private String mongoDBAddress = "localhost";
	private int mongoDBPort = 27017;
	private int isSaveFaceImageToLocaldisk = 1; // 是否保存照片至硬盘
	private String faceVerifyType = "MICRO";
	private float faceFrameTransparency = (float) 0.75;
	private String PITTrackPidstr = "-1"; // 人脸检测进程号
	private String trackPidForKill = "-1";
	private boolean rebackTrackFlag = false; // 是否允许恢复检脸进程
	private boolean isCameraWork = true; // 摄像头是否启动成功
	private int isUseManualMQ = 0; // 是否连接人工控制台
	private int isUseMySQLDB = 0; // 是否保存数据至mysql数据库
	private int isPlayHelpAudio = 1; // 是否播放引导语音
	private int isStartMainListener = 1; // 是否允许启动睿新版本的主控线程
	private int isUsePoliceMQ = 0; // 是否连接公安
	private int isUseGatDll = 0; // 是否实用门控动态库
	private int isResetVerifyScreen = 1; // 是否重置java版屏幕
	private int isUseCamDll = 1; // 是否调用CAM_RXTa.dll
	private int isSendFrame = 1; // 是否发送图片帧至人工台
	/**
	 * 
	 */
	private int doorCountMode = 2;

	/**
	 * 
	 */
	private int initColorExposure = -4; // -6
	private int initColorBrightness = 0; // 0
	private int initContrast = 50;
	private int initGain = 64;
	private String initCronStr = "0 0 7 * * ?";

	private String easenConfigPath = "D:/maven/git/pitchecking";
	private String easenActivationFile = "D:/pitchecking/config/license.txt";
	private int isLightFaceLED = 1;

	private int nightColorExposure = -3;
	private int nightColorBrightness = 40;
	private int nightContrast = 50;
	private int nightGain = 64;
	private String nightCronStr = "0 0 18 * * ?";
	private String dayCameraTime = "0800";
	private String nightCameraTime = "1800";

	private int isUseLightLevelDevice = 0;
	private int isReadLightLevel = 0; // 是否读光照监测器的值
	private String readLightLevelCronStr = "0/20 * * * * ?";

	private int backLightColorExposure = -5;
	private int backLightColorBrightness = -25;
	private int backLightContrast = 50;
	private int backLightGain = 64;

	private int isUseUPSDevice = 0;
	private String UPSPort = "COM5";
	private int closePCDelay = 15;
	private String queryUPSCronStr = "0/10 * * * * ?";

	private int isUseLuminanceListener = 0;
	private int isSaveLuminanceImage = 0;
	private String queryLuminanceCronStr = "0/5 * * * * ?";
	private float minLuminance = (float) 0.30;
	private float maxLuminance = (float) 0.75;

	private int isSendClosePCCmd = 0;
	private String closePCCmdUrl = "tcp://127.0.0.1:1883";
	private int cameraNum = 0;  //准备启动的摄像头顺序号
	private int frontCameraNo = 1;  //前置摄像头序号
	private int behindCameraNo = 2;  //后置摄像头序号
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

	/***********************************************************/

	public int getNightColorExposure() {
		return nightColorExposure;
	}

	public int getFrontCameraNo() {
		return frontCameraNo;
	}

	public void setFrontCameraNo(int frontCameraNo) {
		this.frontCameraNo = frontCameraNo;
	}

	public int getBehindCameraNo() {
		return behindCameraNo;
	}

	public void setBehindCameraNo(int behindCameraNo) {
		this.behindCameraNo = behindCameraNo;
	}

	public int getCameraNum() {
		return cameraNum;
	}

	public void setCameraNum(int cameraNum) {
		this.cameraNum = cameraNum;
	}

	public int getIsSendClosePCCmd() {
		return isSendClosePCCmd;
	}

	public void setIsSendClosePCCmd(int isSendClosePCCmd) {
		this.isSendClosePCCmd = isSendClosePCCmd;
	}

	

	public String getClosePCCmdUrl() {
		return closePCCmdUrl;
	}

	public void setClosePCCmdUrl(String closePCCmdUrl) {
		this.closePCCmdUrl = closePCCmdUrl;
	}

	public float getMinLuminance() {
		return minLuminance;
	}

	public void setMinLuminance(float minLuminance) {
		this.minLuminance = minLuminance;
	}

	public float getMaxLuminance() {
		return maxLuminance;
	}

	public void setMaxLuminance(float maxLuminance) {
		this.maxLuminance = maxLuminance;
	}

	public String getQueryLuminanceCronStr() {
		return queryLuminanceCronStr;
	}

	public void setQueryLuminanceCronStr(String queryLuminanceCronStr) {
		this.queryLuminanceCronStr = queryLuminanceCronStr;
	}

	public boolean isCameraWork() {
		return isCameraWork;
	}

	public void setCameraWork(boolean isCameraWork) {
		this.isCameraWork = isCameraWork;
	}

	public int getIsSaveLuminanceImage() {
		return isSaveLuminanceImage;
	}

	public void setIsSaveLuminanceImage(int isSaveLuminanceImage) {
		this.isSaveLuminanceImage = isSaveLuminanceImage;
	}

	public int getIsUseLuminanceListener() {
		return isUseLuminanceListener;
	}

	public void setIsUseLuminanceListener(int isUseLuminanceListener) {
		this.isUseLuminanceListener = isUseLuminanceListener;
	}

	public int getClosePCDelay() {
		return closePCDelay;
	}

	public void setClosePCDelay(int closePCDelay) {
		this.closePCDelay = closePCDelay;
	}

	public String getUPSPort() {
		return UPSPort;
	}

	public void setUPSPort(String uPSPort) {
		UPSPort = uPSPort;
	}

	public String getQueryUPSCronStr() {
		return queryUPSCronStr;
	}

	public void setQueryUPSCronStr(String queryUPSCronStr) {
		this.queryUPSCronStr = queryUPSCronStr;
	}

	public int getIsUseUPSDevice() {
		return isUseUPSDevice;
	}

	public void setIsUseUPSDevice(int isUseUPSDevice) {
		this.isUseUPSDevice = isUseUPSDevice;
	}

	public int getBackLightColorExposure() {
		return backLightColorExposure;
	}

	public void setBackLightColorExposure(int backLightColorExposure) {
		this.backLightColorExposure = backLightColorExposure;
	}

	public int getBackLightColorBrightness() {
		return backLightColorBrightness;
	}

	public void setBackLightColorBrightness(int backLightColorBrightness) {
		this.backLightColorBrightness = backLightColorBrightness;
	}

	public int getBackLightContrast() {
		return backLightContrast;
	}

	public void setBackLightContrast(int backLightContrast) {
		this.backLightContrast = backLightContrast;
	}

	public int getBackLightGain() {
		return backLightGain;
	}

	public void setBackLightGain(int backLightGain) {
		this.backLightGain = backLightGain;
	}

	public int getIsUseLightLevelDevice() {
		return isUseLightLevelDevice;
	}

	public void setIsUseLightLevelDevice(int isUseLightLevelDevice) {
		this.isUseLightLevelDevice = isUseLightLevelDevice;
	}

	public String getReadLightLevelCronStr() {
		return readLightLevelCronStr;
	}

	public void setReadLightLevelCronStr(String readLightLevelCronStr) {
		this.readLightLevelCronStr = readLightLevelCronStr;
	}

	public int getIsReadLightLevel() {
		return isReadLightLevel;
	}

	public void setIsReadLightLevel(int isReadLightLevel) {
		this.isReadLightLevel = isReadLightLevel;
	}

	public String getInitCronStr() {
		return initCronStr;
	}

	public void setInitCronStr(String initCronStr) {
		this.initCronStr = initCronStr;
	}

	public String getNightCronStr() {
		return nightCronStr;
	}

	public void setNightCronStr(String nightCronStr) {
		this.nightCronStr = nightCronStr;
	}

	public int getInitContrast() {
		return initContrast;
	}

	public void setInitContrast(int initContrast) {
		this.initContrast = initContrast;
	}

	public int getInitGain() {
		return initGain;
	}

	public void setInitGain(int initGain) {
		this.initGain = initGain;
	}

	public int getNightContrast() {
		return nightContrast;
	}

	public void setNightContrast(int nightContrast) {
		this.nightContrast = nightContrast;
	}

	public int getNightGain() {
		return nightGain;
	}

	public void setNightGain(int nightGain) {
		this.nightGain = nightGain;
	}

	public void setNightColorExposure(int nightColorExposure) {
		this.nightColorExposure = nightColorExposure;
	}

	public int getNightColorBrightness() {
		return nightColorBrightness;
	}

	public void setNightColorBrightness(int nightColorBrightness) {
		this.nightColorBrightness = nightColorBrightness;
	}

	public String getDayCameraTime() {
		return dayCameraTime;
	}

	public void setDayCameraTime(String dayCameraTime) {
		this.dayCameraTime = dayCameraTime;
	}

	public String getNightCameraTime() {
		return nightCameraTime;
	}

	public void setNightCameraTime(String nightCameraTime) {
		this.nightCameraTime = nightCameraTime;
	}

	public int getIsLightFaceLED() {
		return isLightFaceLED;
	}

	public void setIsLightFaceLED(int isLightFaceLED) {
		this.isLightFaceLED = isLightFaceLED;
	}

	public String getEasenActivationFile() {
		return easenActivationFile;
	}

	public void setEasenActivationFile(String easenActivationFile) {
		this.easenActivationFile = easenActivationFile;
	}

	public String getEasenConfigPath() {
		return easenConfigPath;
	}

	public void setEasenConfigPath(String easenConfigPath) {
		this.easenConfigPath = easenConfigPath;
	}

	public int getIsSendFrame() {
		return isSendFrame;
	}

	public void setIsSendFrame(int isSendFrame) {
		this.isSendFrame = isSendFrame;
	}

	public int getTicketVerify_Heard_DELAY() {
		return TicketVerify_Heard_DELAY;
	}

	public void setTicketVerify_Heard_DELAY(int ticketVerify_Heard_DELAY) {
		TicketVerify_Heard_DELAY = ticketVerify_Heard_DELAY;
	}

	public String getTicketVerifyHeartFile() {
		return ticketVerifyHeartFile;
	}

	public void setTicketVerifyHeartFile(String ticketVerifyHeartFile) {
		this.ticketVerifyHeartFile = ticketVerifyHeartFile;
	}

	public int getIsUseCamDll() {
		return isUseCamDll;
	}

	public void setIsUseCamDll(int isUseCamDll) {
		this.isUseCamDll = isUseCamDll;
	}

	public int getDoorCountMode() {
		return doorCountMode;
	}

	public int getInitColorBrightness() {
		return initColorBrightness;
	}

	public void setInitColorBrightness(int initColorBrightness) {
		this.initColorBrightness = initColorBrightness;
	}

	public int getInitColorExposure() {
		return initColorExposure;
	}

	public void setInitColorExposure(int initColorExposure) {
		this.initColorExposure = initColorExposure;
	}

	public void setDoorCountMode(int doorCountMode) {
		this.doorCountMode = doorCountMode;
	}

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
		String filePath = Thread.currentThread().getContextClassLoader().getResource("").toString();

		String fn = filePath + "conf/conf.properties";
		URL url = null;
		FileInputStream is = null;
		boolean flag = false;
		try {
			url = new URL(fn);
			is = new FileInputStream(url.getFile());
			flag = true;
		} catch (Exception ex) {
			log.error("Config build:", ex);
			flag = false;
		}

		if (!flag) {
			try {
				fn = filePath + "conf.properties";
				url = new URL(fn);
				is = new FileInputStream(url.getFile());
			} catch (Exception ex) {
				log.error("", ex);
			}
		}

		try {
			log.info("conf.properties dir==" + fn);

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
			this.ticketVerifyHeartFile = p.getProperty("ticketVerifyHeartFile",
					"D:/pitchecking/work/TicketVerifyHeart.log");
			this.rebackTrackFile = p.getProperty("rebackTrackFile", "D:/pitchecking/work/RebackFlag.log");
			this.startPITTrackCmd = p.getProperty("StartPITTrackCmd", "D:/pitchecking/work/RestartPITTrack.bat");
			this.startPITVerifyCmd = p.getProperty("StartPITVerifyCmd", "D:/pitchecking/work/RestartPITVerify.bat");
			this.KillTKExeCmd = p.getProperty("KillTKExeCmd", "C:/AFC/shared/kill.exe");
			this.StartTKExeCmd = p.getProperty("StartTKExeCmd", "C:/AFC/gui/gui.exe");
			this.faceVerifyTaskVersion = p.getProperty("FaceVerifyTaskVersion", "TK");
			this.faceFrameTransparency = Float.valueOf(p.getProperty("faceFrameTransparency", "0.75"));
			this.HEART_BEAT_DELAY = Integer.valueOf(p.getProperty("HEART_BEAT_DELAY", "15000"));
			this.TicketVerify_Heard_DELAY = Integer.valueOf(p.getProperty("TicketVerify_Heard_DELAY", "5000"));
			this.isUseManualMQ = Integer.valueOf(p.getProperty("isUseManualMQ", "0"));
			this.isUseMySQLDB = Integer.valueOf(p.getProperty("isUseMySQLDB", "0"));
			this.isPlayHelpAudio = Integer.valueOf(p.getProperty("isPlayHelpAudio", "1"));
			this.isStartMainListener = Integer.valueOf(p.getProperty("isStartMainListener", "1"));
			this.isUsePoliceMQ = Integer.valueOf(p.getProperty("isUsePoliceMQ", "0"));
			this.isUseGatDll = Integer.valueOf(p.getProperty("isUseGatDll", "0"));
			this.isResetVerifyScreen = Integer.valueOf(p.getProperty("isResetVerifyScreen", "1"));
			this.doorCountMode = Integer.valueOf(p.getProperty("doorCountMode", "2"));
			this.initColorExposure = Integer.valueOf(p.getProperty("initColorExposure", "-3"));
			this.initColorBrightness = Integer.valueOf(p.getProperty("initColorBrightness", "9"));
			this.initContrast = Integer.valueOf(p.getProperty("initContrast", "50"));
			this.initGain = Integer.valueOf(p.getProperty("initGain", "64"));
			this.initCronStr = p.getProperty("initCronStr", "0 0 7 * * ?");
			this.isUseCamDll = Integer.valueOf(p.getProperty("isUseCamDll", "1"));
			this.isSendFrame = Integer.valueOf(p.getProperty("isSendFrame", "1"));
			this.easenConfigPath = p.getProperty("easenConfigPath", "D:/maven/git/pitchecking");
			this.easenActivationFile = p.getProperty("easenActivationFile", "D:/pitchecking/config/license.txt");
			this.isLightFaceLED = Integer.valueOf(p.getProperty("isLightFaceLED", "1"));
			this.nightColorExposure = Integer.valueOf(p.getProperty("nightColorExposure", "-2"));
			this.nightColorBrightness = Integer.valueOf(p.getProperty("nightColorBrightness", "40"));
			this.nightContrast = Integer.valueOf(p.getProperty("nightContrast", "50"));
			this.nightGain = Integer.valueOf(p.getProperty("nightGain", "64"));
			this.nightCronStr = p.getProperty("nightCronStr", "0 0 18 * * ?");
			this.dayCameraTime = p.getProperty("dayCameraTime", "0800");
			this.nightCameraTime = p.getProperty("nightCameraTime", "1800");
			this.isUseLightLevelDevice = Integer.valueOf(p.getProperty("isUseLightLevelDevice", "0"));
			this.isReadLightLevel = Integer.valueOf(p.getProperty("isReadLightLevel", "0"));
			this.readLightLevelCronStr = p.getProperty("readLightLevelCronStr", "0/20 * * * * ?");
			this.backLightColorExposure = Integer.valueOf(p.getProperty("backLightColorExposure", "-5"));
			this.backLightColorBrightness = Integer.valueOf(p.getProperty("backLightColorBrightness", "-25"));
			this.backLightContrast = Integer.valueOf(p.getProperty("backLightContrast", "50"));
			this.backLightGain = Integer.valueOf(p.getProperty("backLightGain", "64"));
			this.isUseUPSDevice = Integer.valueOf(p.getProperty("isUseUPSDevice", "0"));
			this.queryUPSCronStr = p.getProperty("queryUPSCronStr", "0/10 * * * * ?");
			this.closePCDelay = Integer.valueOf(p.getProperty("closePCDelay", "15"));
			this.UPSPort = p.getProperty("UPSPort", "COM5");
			this.isUseLuminanceListener = Integer.valueOf(p.getProperty("isUseLuminanceListener", "0"));
			this.isSaveLuminanceImage = Integer.valueOf(p.getProperty("isSaveLuminanceImage", "0"));
			this.queryLuminanceCronStr = p.getProperty("queryLuminanceCronStr", "0/5 * * * * ?");
			this.minLuminance = Float.valueOf(p.getProperty("minLuminance", "0.30"));
			this.maxLuminance = Float.valueOf(p.getProperty("maxLuminance", "0.75"));
			this.isSendClosePCCmd = Integer.valueOf(p.getProperty("isSendClosePCCmd", "0"));
			this.closePCCmdUrl = p.getProperty("closePCCmdUrl", "tcp://127.0.0.1:1883");
			this.frontCameraNo = Integer.valueOf(p.getProperty("frontCameraNo", "1"));
			this.behindCameraNo = Integer.valueOf(p.getProperty("behindCameraNo", "2"));
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
		System.out.println("getHEART_BEAT_DELAY==" + Config.getInstance().getHEART_BEAT_DELAY());
		System.out.println("getImagesLogDir==" + Config.getInstance().getImagesLogDir());
		System.out.println("getPausePitcheckFile==" + Config.getInstance().getPausePitcheckFile());
		System.out.println("getClosePCCmdUrl==" + Config.getInstance().getClosePCCmdUrl());
		System.out.println("frontCameraNo==" + Config.getInstance().getFrontCameraNo());
		System.out.println("behindCameraNo==" + Config.getInstance().getBehindCameraNo());
	}

}
