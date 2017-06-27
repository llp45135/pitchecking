package com.rxtec.pitchecking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.StringTokenizer;
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

	public static float FACE_POSE_YAW = 20;
	public static float FACE_POSE_PITCH = 20;
	public static float FACE_POSE_ROLL = 20;

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
	public static int PIVerifyResultEvent_STREAM_ID = 13; // 独立人脸比对进程发布比对结果
															// 用于铁科版本

	public static String PIVerify_CHANNEL = "aeron:ipc";
	// public static String PIVerify_CHANNEL =
	// "aeron:udp?endpoint=192.168.1.21:40456";

	public static int IDReaderEvent = 1;
	public static int QRReaderEvent = 2;

	public static String TicketVerifyWaitInput = "-00888"; // 等待票证验证数据
	public static String TicketVerifySucc = "000000"; // 票证验证成功
	public static String TicketVerifyIDFail = "-00801"; // 票证验证失败
	public static String TicketVerifyStationRuleFail = "-00802"; // 票证验证失败：非本站乘车
	public static String TicketVerifyTrainDateRuleFail = "-00803"; // 票证验证失败：非当日乘车
	public static String TicketVerifyNotStartCheckFail = "-00804"; // 票证验证失败，未开始检票
	public static String TicketVerifyStopCheckFail = "-00805"; // 票证验证失败，已经停止检票
	public static String TicketVerifyRepeatCheck = "-00806"; // 重复刷票，已经停止检票
	public static String TicketVerifyInvalidTrainCode = "-00807"; // 無效車次
	public static String TicketVerifyTrainStopped = "-00808"; // 列車已停運
	
	

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
	// private String rebackTrackFile = "D:/pitchecking/work/RebackFlag.log";
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

	public static int TransferFaceByAeron = 1;
	public static int TransferFaceByMqtt = 2;
	public static int TransferFaceBySocket = 3;

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
	private int detectededFaceQueueLen = 4;
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
	private int doorCountMode = 2;   //闸门数量

	/**
	 * 
	 */
	private int initColorExposure = -4; // -6
	private int initColorBrightness = 0; // 0
	private int initContrast = 50;
	private int initGain = 64;
	private int initGamma = 300;
	private String initCronStr = "0 0 7 * * ?";

	private String easenConfigPath = "D:/maven/git/pitchecking";
	private String easenActivationFile = "D:/pitchecking/config/license.txt";
	private int isLightFaceLED = 1;

	private int nightColorExposure = -3;
	private int nightColorBrightness = 40;
	private int nightContrast = 50;
	private int nightGain = 64;
	private int nightGamma = 300;
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
	private int backLightGamma = 100;

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
	private int cameraNum = 0; // 准备启动的摄像头顺序号
	private String frontCameraNo = ""; // 前置摄像头序列号
	private String behindCameraNo = ""; // 后置摄像头序列号
	private String frontHeartBeatLogFile = "D:/pitchecking/work/FrontHeart.log"; // 前置摄像头检脸进程心跳日志
	private String restartFrontTrackCmd = "D:/pitchecking/work/RestartFrontTrack.bat";
	private boolean rebackFrontTrackFlag = false; // 是否允许恢复检脸进程
	private boolean isFrontCameraWork = true; // 摄像头是否启动成功
	private boolean isAllowRebackMain = false; // 是否允许恢复java版主控
	private boolean isMainCrtlWork = true; // java版主控是否启动成功
	private float frontCameraWindowOpacity = 0.2f; // 前置摄像头窗口透明度
	private long secondDoorWaitTime = 2000; // 比对通过到开2门的时间
	private int cameraCount = 1;
	private String StandaloneCheckHeartFile = "D:/pitchecking/work/PhotoCheckHeart.log";
	private String restartStandaloneCheckCmd = "D:/pitchecking/work/RestartCheckPhoto.bat";
	private boolean isAllowRebackStandalone = false; // 是否允许恢复独立比对进程
	private boolean isStandaloneCheckWork = true; // 独立比对进程是否启动成功
	private int successFontSize = 150; // 请通过的字号
	private int shutdownPCMode = 2; // 打下空开时重启模式 1：注销 2：重启 3：彻底关闭
	private int isSavePhotoByTK = 0; // 是否按照铁科格式保存照片
	private int isMatchTodayByFirst = 0; // 票证校验时，是否优先匹配当日票规则
	private int frontFaceTrackMode = 2; // 前置摄像头检脸模式
	private int transferFaceMode = 1; // 进程间传输face的方式 1：aeron 2:mqtt
	private int pitVerifyMode = 1; // java版主控版本 1:正式程序 0：测试程序
	private int openSecondDoorMode = 1; // 打开后门的模式 1：人证一致即开门
										// 2：人证一致后还需要判断前门是否已经关闭，前门关闭才开后门
	private int waitFirstDoorClosedTime = 3; // 等待前门关闭的时间 单位：秒
	private int rebackReadCardMode = 1; // 恢复寻卡的模式 1：1门关2门开即可寻卡 2：2门关才可以寻卡
										// 3：2A040023还是关门指令时
	private int isCheckStandaloneHeart = 0; // 比对进程是否写心跳
	private int isVerifyFaceToCard = 1; // 是否核验人证
	private int luminanceX = 0; // 光亮度截图X点
	private int luminanceY = 0; // 光亮度截图Y点
	private int luminanceWidth = 960; // 光亮度截图宽点
	private int luminanceHeight = 540; // 光亮度截图高点
	private int isUseFrontLuminanceListener = 0; // 是否开启前置摄像头自动调光
	private String frontLuminancePara = "0"; // 前置摄像头图像九宫格选择
	private String backLuminancePara = "0"; // 后置摄像头图像九宫格选择

	private int superBackLightColorExposure = -5;
	private int superBackLightColorBrightness = -10;
	private int superBackLightContrast = 50;
	private int superBackLightGain = 64;
	private int superBackLightGamma = 300;

	private int superAdd1BackLightColorExposure = -6;
	private int superAdd1BackLightColorBrightness = -10;
	private int superAdd1BackLightContrast = 50;
	private int superAdd1BackLightGain = 64;
	private int superAdd1BackLightGamma = 300;

	private int superAdd2BackLightColorExposure = -6;
	private int superAdd2BackLightColorBrightness = -20;
	private int superAdd2BackLightContrast = 50;
	private int superAdd2BackLightGain = 64;
	private int superAdd2BackLightGamma = 200;

	private int frontInitColorExposure = -4;
	private int frontInitColorBrightness = 0;
	private int frontInitContrast = 50;
	private int frontInitGain = 64;
	private int frontInitGamma = 300;

	private int frontNightColorExposure = -4;
	private int frontNightColorBrightness = 40;
	private int frontNightContrast = 50;
	private int frontNightGain = 64;
	private int frontNightGamma = 300;

	private int frontBackLightColorExposure = -5;
	private int frontBackLightColorBrightness = -25;
	private int frontBackLightContrast = 50;
	private int frontBackLightGain = 64;
	private int frontBackLightGamma = 300;

	private int frontSuperBackLightColorExposure = -6;
	private int frontSuperBackLightColorBrightness = -25;
	private int frontSuperBackLightContrast = 50;
	private int frontSuperBackLightGain = 64;
	private int frontSuperBackLightGamma = 300;

	private String resetCameraLightTime = "1200";

	private int isUseTrail = 0; // 是否启用防尾随检测

	private int faceControlMode = 1; // 人脸比对逻辑控制模式:1-后置摄像头进程处理;2-比对进程处理

	private int mysqlRecordType = 1; //

	private int isUseThirdMQ = 0; // 是否使用第三方MQ
	private String thirdMQUrl = "failover://tcp://127.0.0.1:61616";   //第三方mq地址
	
	private int faceReceiveSocketPort = 36861;           //
	private int faceResultReceiveSocketPort = 36862;
	private int ocxFaceReceiveSocketPort = 36863;
	
	private int isSetFrontCameraConfigByInit = 1;    //初始化时是否设定前置realsense参数
	private int isSetBackCameraConfigByInit = 1;     //初始化时是否设定后置realsense参数
	private int isVerifyFaceOnce = 1;     //是否人脸比对成功一次就不再比对
	private int sendFaceSourceBySocket = 1;  //通过socket传输人脸的来源:1-realsense;2-ocx方式usb摄像头
	private int isAdmitRepeatCheck = 0;   //是否允许重复验票:1-允许;0-不允许
	
	private int ocxUsbCameraNo = 0;   //普通高清usb摄像头序号
	
	private int isVerifyScreenAlwaysTop = 1;  //主控程序界面是否一直最上层
	
	private String gateSerialNo = "0000000000000000JWAGGT2017010001";   //闸机序列号
	
	private int isConnectTRService = 1;// 是否连客票服务器
	
	private int backScreenMode = 1;   //后屏幕尺寸模式：1-1024*768;2-1080*1920
	private int frontScreenMode = 1;  //前屏幕尺寸模式：1-1024*768;2-1080*1920
	
	private int usbCameraCanvasWidth = 720;
	private int usbCameraCanvasHeight = 1280;
	
	private int isCheckBackCameraHeart = 1;  //是否监听后者摄像头
	
	private int isNeedUseFirstDoor = 1;   //是否需要使用第一道门
	
	private int sendReceiptCount = 3;    //照片回执上传数量
	
	private int isUseWharfMQ = 0 ;  //是否启用桂林MQ服务
	private String wharfMQUrl = "failover://tcp://127.0.0.1:61616";  //桂林mq服务地址
	private boolean isUseFullScreenCamera = false;
	
	private int isSupportTicketWithC = 1;
	private int isSupportTicketWithD = 1;
	private int isSupportTicketWithG = 1;
	private int isSupportTicketWithK = 1;
	
	private int isGGQChaoshanMode = 0;  //是否是广州动潮汕车候车模式
	
	private String shipTicketUrl = "http://ticket.gxpft.com:8090/twebservice/realNameForJson/checkRealName";
	private String shipTicketCenterUser = "admin";
	private String shipTicketCenterPwd = "21218CCA77804D2BA1922C33E0151105";
	private String wharfDevId = "000100";
	
	private int isCheckShipTicketRule = 1;   //是否核验船票规则
	
	private String baseInfoXMLPath = "C:/SmartMonitor/RealNameGate/BaseInfo.xml";
	private String statusInfoXMLPath = "C:/SmartMonitor/RealNameGate/StatusInfo.xml";
	
	private String productionDate = "2017-01-01";  //出厂日期
	
	private int isOutGateForCSQ = 0;   //是否是长沙的出闸机
	
	private String updateFlapCountCronStr = "0 0 18 * * ?";
	
	private String myStatusXML = "D:/pitchecking/config/StatusInfo.xml";
	
	private String updateTrainInfoCronStr = "0 0/10 * * * ?";
	
	private int isConnectLvFu = 0;  //是否连旅服平台
	private String lvFuDatasource = "FILE";  //服务数据来源
	private String lvFuFtpAddress = "10.168.72.60";
	private String lvFuFtpUser = "pitcheck";
	private String lvFuFtpPwd = "pitcheck";

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
	
	public int getIsUseThirdMQ() {
		return isUseThirdMQ;
	}

	public String getLvFuFtpAddress() {
		return lvFuFtpAddress;
	}

	public void setLvFuFtpAddress(String lvFuFtpAddress) {
		this.lvFuFtpAddress = lvFuFtpAddress;
	}

	public String getLvFuFtpUser() {
		return lvFuFtpUser;
	}

	public void setLvFuFtpUser(String lvFuFtpUser) {
		this.lvFuFtpUser = lvFuFtpUser;
	}

	public String getLvFuFtpPwd() {
		return lvFuFtpPwd;
	}

	public void setLvFuFtpPwd(String lvFuFtpPwd) {
		this.lvFuFtpPwd = lvFuFtpPwd;
	}

	public String getLvFuDatasource() {
		return lvFuDatasource;
	}

	public void setLvFuDatasource(String lvFuDatasource) {
		this.lvFuDatasource = lvFuDatasource;
	}

	public int getIsConnectLvFu() {
		return isConnectLvFu;
	}

	public void setIsConnectLvFu(int isConnectLvFu) {
		this.isConnectLvFu = isConnectLvFu;
	}

	public String getUpdateTrainInfoCronStr() {
		return updateTrainInfoCronStr;
	}

	public void setUpdateTrainInfoCronStr(String updateTrainInfoCronStr) {
		this.updateTrainInfoCronStr = updateTrainInfoCronStr;
	}

	public String getMyStatusXML() {
		return myStatusXML;
	}

	public void setMyStatusXML(String myStatusXML) {
		this.myStatusXML = myStatusXML;
	}

	public String getUpdateFlapCountCronStr() {
		return updateFlapCountCronStr;
	}

	public void setUpdateFlapCountCronStr(String updateFlapCountCronStr) {
		this.updateFlapCountCronStr = updateFlapCountCronStr;
	}

	public int getIsOutGateForCSQ() {
		return isOutGateForCSQ;
	}

	public void setIsOutGateForCSQ(int isOutGateForCSQ) {
		this.isOutGateForCSQ = isOutGateForCSQ;
	}

	public String getProductionDate() {
		return productionDate;
	}

	public void setProductionDate(String productionDate) {
		this.productionDate = productionDate;
	}

	public String getBaseInfoXMLPath() {
		return baseInfoXMLPath;
	}

	public void setBaseInfoXMLPath(String baseInfoXMLPath) {
		this.baseInfoXMLPath = baseInfoXMLPath;
	}

	public String getStatusInfoXMLPath() {
		return statusInfoXMLPath;
	}

	public void setStatusInfoXMLPath(String statusInfoXMLPath) {
		this.statusInfoXMLPath = statusInfoXMLPath;
	}

	public int getIsCheckShipTicketRule() {
		return isCheckShipTicketRule;
	}

	public void setIsCheckShipTicketRule(int isCheckShipTicketRule) {
		this.isCheckShipTicketRule = isCheckShipTicketRule;
	}

	public String getShipTicketUrl() {
		return shipTicketUrl;
	}

	public void setShipTicketUrl(String shipTicketUrl) {
		this.shipTicketUrl = shipTicketUrl;
	}

	public String getShipTicketCenterUser() {
		return shipTicketCenterUser;
	}

	public void setShipTicketCenterUser(String shipTicketCenterUser) {
		this.shipTicketCenterUser = shipTicketCenterUser;
	}

	public String getShipTicketCenterPwd() {
		return shipTicketCenterPwd;
	}

	public void setShipTicketCenterPwd(String shipTicketCenterPwd) {
		this.shipTicketCenterPwd = shipTicketCenterPwd;
	}

	public String getWharfDevId() {
		return wharfDevId;
	}

	public void setWharfDevId(String wharfDevId) {
		this.wharfDevId = wharfDevId;
	}

	public int getIsGGQChaoshanMode() {
		return isGGQChaoshanMode;
	}

	public void setIsGGQChaoshanMode(int isGGQChaoshanMode) {
		this.isGGQChaoshanMode = isGGQChaoshanMode;
	}

	public int getIsSupportTicketWithC() {
		return isSupportTicketWithC;
	}

	public void setIsSupportTicketWithC(int isSupportTicketWithC) {
		this.isSupportTicketWithC = isSupportTicketWithC;
	}

	public int getIsSupportTicketWithD() {
		return isSupportTicketWithD;
	}

	public void setIsSupportTicketWithD(int isSupportTicketWithD) {
		this.isSupportTicketWithD = isSupportTicketWithD;
	}

	public int getIsSupportTicketWithG() {
		return isSupportTicketWithG;
	}

	public void setIsSupportTicketWithG(int isSupportTicketWithG) {
		this.isSupportTicketWithG = isSupportTicketWithG;
	}

	public int getIsSupportTicketWithK() {
		return isSupportTicketWithK;
	}

	public void setIsSupportTicketWithK(int isSupportTicketWithK) {
		this.isSupportTicketWithK = isSupportTicketWithK;
	}

	public boolean isUseFullScreenCamera() {
		return isUseFullScreenCamera;
	}

	public void setUseFullScreenCamera(boolean isUseFullScreenCamera) {
		this.isUseFullScreenCamera = isUseFullScreenCamera;
	}

	public int getIsUseWharfMQ() {
		return isUseWharfMQ;
	}

	public void setIsUseWharfMQ(int isUseWharfMQ) {
		this.isUseWharfMQ = isUseWharfMQ;
	}

	public String getWharfMQUrl() {
		return wharfMQUrl;
	}

	public void setWharfMQUrl(String wharfMQUrl) {
		this.wharfMQUrl = wharfMQUrl;
	}

	public int getSendReceiptCount() {
		return sendReceiptCount;
	}

	public void setSendReceiptCount(int sendReceiptCount) {
		this.sendReceiptCount = sendReceiptCount;
	}

	public int getIsNeedUseFirstDoor() {
		return isNeedUseFirstDoor;
	}

	public void setIsNeedUseFirstDoor(int isNeedUseFirstDoor) {
		this.isNeedUseFirstDoor = isNeedUseFirstDoor;
	}

	public int getIsCheckBackCameraHeart() {
		return isCheckBackCameraHeart;
	}

	public void setIsCheckBackCameraHeart(int isCheckBackCameraHeart) {
		this.isCheckBackCameraHeart = isCheckBackCameraHeart;
	}

	public int getUsbCameraCanvasWidth() {
		return usbCameraCanvasWidth;
	}

	public void setUsbCameraCanvasWidth(int usbCameraCanvasWidth) {
		this.usbCameraCanvasWidth = usbCameraCanvasWidth;
	}

	public int getUsbCameraCanvasHeight() {
		return usbCameraCanvasHeight;
	}

	public void setUsbCameraCanvasHeight(int usbCameraCanvasHeight) {
		this.usbCameraCanvasHeight = usbCameraCanvasHeight;
	}

	public int getBackScreenMode() {
		return backScreenMode;
	}

	public void setBackScreenMode(int backScreenMode) {
		this.backScreenMode = backScreenMode;
	}

	public int getFrontScreenMode() {
		return frontScreenMode;
	}

	public void setFrontScreenMode(int frontScreenMode) {
		this.frontScreenMode = frontScreenMode;
	}

	public int getIsConnectTRService() {
		return isConnectTRService;
	}

	public void setIsConnectTRService(int isConnectTRService) {
		this.isConnectTRService = isConnectTRService;
	}

	public String getGateSerialNo() {
		return gateSerialNo;
	}

	public void setGateSerialNo(String gateSerialNo) {
		this.gateSerialNo = gateSerialNo;
	}

	public int getIsVerifyScreenAlwaysTop() {
		return isVerifyScreenAlwaysTop;
	}

	public void setIsVerifyScreenAlwaysTop(int isVerifyScreenAlwaysTop) {
		this.isVerifyScreenAlwaysTop = isVerifyScreenAlwaysTop;
	}

	public int getOcxUsbCameraNo() {
		return ocxUsbCameraNo;
	}

	public void setOcxUsbCameraNo(int ocxUsbCameraNo) {
		this.ocxUsbCameraNo = ocxUsbCameraNo;
	}

	public int getIsAdmitRepeatCheck() {
		return isAdmitRepeatCheck;
	}

	public void setIsAdmitRepeatCheck(int isAdmitRepeatCheck) {
		this.isAdmitRepeatCheck = isAdmitRepeatCheck;
	}

	public int getSendFaceSourceBySocket() {
		return sendFaceSourceBySocket;
	}

	public void setSendFaceSourceBySocket(int sendFaceSourceBySocket) {
		this.sendFaceSourceBySocket = sendFaceSourceBySocket;
	}

	public int getIsVerifyFaceOnce() {
		return isVerifyFaceOnce;
	}

	public void setIsVerifyFaceOnce(int isVerifyFaceOnce) {
		this.isVerifyFaceOnce = isVerifyFaceOnce;
	}

	public int getIsSetFrontCameraConfigByInit() {
		return isSetFrontCameraConfigByInit;
	}

	public void setIsSetFrontCameraConfigByInit(int isSetFrontCameraConfigByInit) {
		this.isSetFrontCameraConfigByInit = isSetFrontCameraConfigByInit;
	}

	public int getIsSetBackCameraConfigByInit() {
		return isSetBackCameraConfigByInit;
	}

	public void setIsSetBackCameraConfigByInit(int isSetBackCameraConfigByInit) {
		this.isSetBackCameraConfigByInit = isSetBackCameraConfigByInit;
	}

	public int getOcxFaceReceiveSocketPort() {
		return ocxFaceReceiveSocketPort;
	}

	public void setOcxFaceReceiveSocketPort(int ocxFaceReceiveSocketPort) {
		this.ocxFaceReceiveSocketPort = ocxFaceReceiveSocketPort;
	}

	public void setIsUseThirdMQ(int isUseThirdMQ) {
		this.isUseThirdMQ = isUseThirdMQ;
	}
	
	public int getNightColorExposure() {
		return nightColorExposure;
	}

	public int getFaceReceiveSocketPort() {
		return faceReceiveSocketPort;
	}

	public void setFaceReceiveSocketPort(int faceReceiveSocketPort) {
		this.faceReceiveSocketPort = faceReceiveSocketPort;
	}

	public int getFaceResultReceiveSocketPort() {
		return faceResultReceiveSocketPort;
	}

	public void setFaceResultReceiveSocketPort(int faceResultReceiveSocketPort) {
		this.faceResultReceiveSocketPort = faceResultReceiveSocketPort;
	}

	public String getThirdMQUrl() {
		return thirdMQUrl;
	}

	public void setThirdMQUrl(String thirdMQUrl) {
		this.thirdMQUrl = thirdMQUrl;
	}

	public int getMysqlRecordType() {
		return mysqlRecordType;
	}

	public void setMysqlRecordType(int mysqlRecordType) {
		this.mysqlRecordType = mysqlRecordType;
	}

	public int getFaceControlMode() {
		return faceControlMode;
	}

	public void setFaceControlMode(int faceControlMode) {
		this.faceControlMode = faceControlMode;
	}

	public int getIsUseTrail() {
		return isUseTrail;
	}

	public void setIsUseTrail(int isUseTrail) {
		this.isUseTrail = isUseTrail;
	}

	public String getResetCameraLightTime() {
		return resetCameraLightTime;
	}

	public void setResetCameraLightTime(String resetCameraLightTime) {
		this.resetCameraLightTime = resetCameraLightTime;
	}

	public int getSuperAdd1BackLightColorExposure() {
		return superAdd1BackLightColorExposure;
	}

	public void setSuperAdd1BackLightColorExposure(int superAdd1BackLightColorExposure) {
		this.superAdd1BackLightColorExposure = superAdd1BackLightColorExposure;
	}

	public int getSuperAdd1BackLightColorBrightness() {
		return superAdd1BackLightColorBrightness;
	}

	public void setSuperAdd1BackLightColorBrightness(int superAdd1BackLightColorBrightness) {
		this.superAdd1BackLightColorBrightness = superAdd1BackLightColorBrightness;
	}

	public int getSuperAdd1BackLightContrast() {
		return superAdd1BackLightContrast;
	}

	public void setSuperAdd1BackLightContrast(int superAdd1BackLightContrast) {
		this.superAdd1BackLightContrast = superAdd1BackLightContrast;
	}

	public int getSuperAdd1BackLightGain() {
		return superAdd1BackLightGain;
	}

	public void setSuperAdd1BackLightGain(int superAdd1BackLightGain) {
		this.superAdd1BackLightGain = superAdd1BackLightGain;
	}

	public int getSuperAdd1BackLightGamma() {
		return superAdd1BackLightGamma;
	}

	public void setSuperAdd1BackLightGamma(int superAdd1BackLightGamma) {
		this.superAdd1BackLightGamma = superAdd1BackLightGamma;
	}

	public int getSuperAdd2BackLightColorExposure() {
		return superAdd2BackLightColorExposure;
	}

	public void setSuperAdd2BackLightColorExposure(int superAdd2BackLightColorExposure) {
		this.superAdd2BackLightColorExposure = superAdd2BackLightColorExposure;
	}

	public int getSuperAdd2BackLightColorBrightness() {
		return superAdd2BackLightColorBrightness;
	}

	public void setSuperAdd2BackLightColorBrightness(int superAdd2BackLightColorBrightness) {
		this.superAdd2BackLightColorBrightness = superAdd2BackLightColorBrightness;
	}

	public int getSuperAdd2BackLightContrast() {
		return superAdd2BackLightContrast;
	}

	public void setSuperAdd2BackLightContrast(int superAdd2BackLightContrast) {
		this.superAdd2BackLightContrast = superAdd2BackLightContrast;
	}

	public int getSuperAdd2BackLightGain() {
		return superAdd2BackLightGain;
	}

	public void setSuperAdd2BackLightGain(int superAdd2BackLightGain) {
		this.superAdd2BackLightGain = superAdd2BackLightGain;
	}

	public int getSuperAdd2BackLightGamma() {
		return superAdd2BackLightGamma;
	}

	public void setSuperAdd2BackLightGamma(int superAdd2BackLightGamma) {
		this.superAdd2BackLightGamma = superAdd2BackLightGamma;
	}

	public int getSuperBackLightColorExposure() {
		return superBackLightColorExposure;
	}

	public void setSuperBackLightColorExposure(int superBackLightColorExposure) {
		this.superBackLightColorExposure = superBackLightColorExposure;
	}

	public int getSuperBackLightColorBrightness() {
		return superBackLightColorBrightness;
	}

	public void setSuperBackLightColorBrightness(int superBackLightColorBrightness) {
		this.superBackLightColorBrightness = superBackLightColorBrightness;
	}

	public int getSuperBackLightContrast() {
		return superBackLightContrast;
	}

	public void setSuperBackLightContrast(int superBackLightContrast) {
		this.superBackLightContrast = superBackLightContrast;
	}

	public int getSuperBackLightGain() {
		return superBackLightGain;
	}

	public void setSuperBackLightGain(int superBackLightGain) {
		this.superBackLightGain = superBackLightGain;
	}

	public int getSuperBackLightGamma() {
		return superBackLightGamma;
	}

	public void setSuperBackLightGamma(int superBackLightGamma) {
		this.superBackLightGamma = superBackLightGamma;
	}

	public int getFrontSuperBackLightColorExposure() {
		return frontSuperBackLightColorExposure;
	}

	public void setFrontSuperBackLightColorExposure(int frontSuperBackLightColorExposure) {
		this.frontSuperBackLightColorExposure = frontSuperBackLightColorExposure;
	}

	public int getFrontSuperBackLightColorBrightness() {
		return frontSuperBackLightColorBrightness;
	}

	public void setFrontSuperBackLightColorBrightness(int frontSuperBackLightColorBrightness) {
		this.frontSuperBackLightColorBrightness = frontSuperBackLightColorBrightness;
	}

	public int getFrontSuperBackLightContrast() {
		return frontSuperBackLightContrast;
	}

	public void setFrontSuperBackLightContrast(int frontSuperBackLightContrast) {
		this.frontSuperBackLightContrast = frontSuperBackLightContrast;
	}

	public int getFrontSuperBackLightGain() {
		return frontSuperBackLightGain;
	}

	public void setFrontSuperBackLightGain(int frontSuperBackLightGain) {
		this.frontSuperBackLightGain = frontSuperBackLightGain;
	}

	public int getFrontSuperBackLightGamma() {
		return frontSuperBackLightGamma;
	}

	public void setFrontSuperBackLightGamma(int frontSuperBackLightGamma) {
		this.frontSuperBackLightGamma = frontSuperBackLightGamma;
	}

	public int getFrontInitColorExposure() {
		return frontInitColorExposure;
	}

	public void setFrontInitColorExposure(int frontInitColorExposure) {
		this.frontInitColorExposure = frontInitColorExposure;
	}

	public int getFrontInitColorBrightness() {
		return frontInitColorBrightness;
	}

	public void setFrontInitColorBrightness(int frontInitColorBrightness) {
		this.frontInitColorBrightness = frontInitColorBrightness;
	}

	public int getFrontInitContrast() {
		return frontInitContrast;
	}

	public void setFrontInitContrast(int frontInitContrast) {
		this.frontInitContrast = frontInitContrast;
	}

	public int getFrontInitGain() {
		return frontInitGain;
	}

	public void setFrontInitGain(int frontInitGain) {
		this.frontInitGain = frontInitGain;
	}

	public int getFrontInitGamma() {
		return frontInitGamma;
	}

	public void setFrontInitGamma(int frontInitGamma) {
		this.frontInitGamma = frontInitGamma;
	}

	public int getFrontNightColorExposure() {
		return frontNightColorExposure;
	}

	public void setFrontNightColorExposure(int frontNightColorExposure) {
		this.frontNightColorExposure = frontNightColorExposure;
	}

	public int getFrontNightColorBrightness() {
		return frontNightColorBrightness;
	}

	public void setFrontNightColorBrightness(int frontNightColorBrightness) {
		this.frontNightColorBrightness = frontNightColorBrightness;
	}

	public int getFrontNightContrast() {
		return frontNightContrast;
	}

	public void setFrontNightContrast(int frontNightContrast) {
		this.frontNightContrast = frontNightContrast;
	}

	public int getFrontNightGain() {
		return frontNightGain;
	}

	public void setFrontNightGain(int frontNightGain) {
		this.frontNightGain = frontNightGain;
	}

	public int getFrontNightGamma() {
		return frontNightGamma;
	}

	public void setFrontNightGamma(int frontNightGamma) {
		this.frontNightGamma = frontNightGamma;
	}

	public int getFrontBackLightColorExposure() {
		return frontBackLightColorExposure;
	}

	public void setFrontBackLightColorExposure(int frontBackLightColorExposure) {
		this.frontBackLightColorExposure = frontBackLightColorExposure;
	}

	public int getFrontBackLightColorBrightness() {
		return frontBackLightColorBrightness;
	}

	public void setFrontBackLightColorBrightness(int frontBackLightColorBrightness) {
		this.frontBackLightColorBrightness = frontBackLightColorBrightness;
	}

	public int getFrontBackLightContrast() {
		return frontBackLightContrast;
	}

	public void setFrontBackLightContrast(int frontBackLightContrast) {
		this.frontBackLightContrast = frontBackLightContrast;
	}

	public int getFrontBackLightGain() {
		return frontBackLightGain;
	}

	public void setFrontBackLightGain(int frontBackLightGain) {
		this.frontBackLightGain = frontBackLightGain;
	}

	public int getFrontBackLightGamma() {
		return frontBackLightGamma;
	}

	public void setFrontBackLightGamma(int frontBackLightGamma) {
		this.frontBackLightGamma = frontBackLightGamma;
	}

	public int getIsUseFrontLuminanceListener() {
		return isUseFrontLuminanceListener;
	}

	public void setIsUseFrontLuminanceListener(int isUseFrontLuminanceListener) {
		this.isUseFrontLuminanceListener = isUseFrontLuminanceListener;
	}

	public String getFrontLuminancePara() {
		return frontLuminancePara;
	}

	public void setFrontLuminancePara(String frontLuminancePara) {
		this.frontLuminancePara = frontLuminancePara;
	}

	public String getBackLuminancePara() {
		return backLuminancePara;
	}

	public void setBackLuminancePara(String backLuminancePara) {
		this.backLuminancePara = backLuminancePara;
	}

	public int getInitGamma() {
		return initGamma;
	}

	public void setInitGamma(int initGamma) {
		this.initGamma = initGamma;
	}

	public int getNightGamma() {
		return nightGamma;
	}

	public void setNightGamma(int nightGamma) {
		this.nightGamma = nightGamma;
	}

	public int getBackLightGamma() {
		return backLightGamma;
	}

	public void setBackLightGamma(int backLightGamma) {
		this.backLightGamma = backLightGamma;
	}

	public int getLuminanceX() {
		return luminanceX;
	}

	public void setLuminanceX(int luminanceX) {
		this.luminanceX = luminanceX;
	}

	public int getLuminanceY() {
		return luminanceY;
	}

	public void setLuminanceY(int luminanceY) {
		this.luminanceY = luminanceY;
	}

	public int getLuminanceWidth() {
		return luminanceWidth;
	}

	public void setLuminanceWidth(int luminanceWidth) {
		this.luminanceWidth = luminanceWidth;
	}

	public int getLuminanceHeight() {
		return luminanceHeight;
	}

	public void setLuminanceHeight(int luminanceHeight) {
		this.luminanceHeight = luminanceHeight;
	}

	public int getIsVerifyFaceToCard() {
		return isVerifyFaceToCard;
	}

	public void setIsVerifyFaceToCard(int isVerifyFaceToCard) {
		this.isVerifyFaceToCard = isVerifyFaceToCard;
	}

	public int getIsCheckStandaloneHeart() {
		return isCheckStandaloneHeart;
	}

	public void setIsCheckStandaloneHeart(int isCheckStandaloneHeart) {
		this.isCheckStandaloneHeart = isCheckStandaloneHeart;
	}

	public int getRebackReadCardMode() {
		return rebackReadCardMode;
	}

	public void setRebackReadCardMode(int rebackReadCardMode) {
		this.rebackReadCardMode = rebackReadCardMode;
	}

	public int getWaitFirstDoorClosedTime() {
		return waitFirstDoorClosedTime;
	}

	public void setWaitFirstDoorClosedTime(int waitFirstDoorClosedTime) {
		this.waitFirstDoorClosedTime = waitFirstDoorClosedTime;
	}

	public int getOpenSecondDoorMode() {
		return openSecondDoorMode;
	}

	public void setOpenSecondDoorMode(int openSecondDoorMode) {
		this.openSecondDoorMode = openSecondDoorMode;
	}

	public int getPitVerifyMode() {
		return pitVerifyMode;
	}

	public void setPitVerifyMode(int pitVerifyMode) {
		this.pitVerifyMode = pitVerifyMode;
	}

	public int getTransferFaceMode() {
		return transferFaceMode;
	}

	public void setTransferFaceMode(int transferFaceMode) {
		this.transferFaceMode = transferFaceMode;
	}

	public int getFrontFaceTrackMode() {
		return frontFaceTrackMode;
	}

	public void setFrontFaceTrackMode(int frontFaceTrackMode) {
		this.frontFaceTrackMode = frontFaceTrackMode;
	}

	public int getIsMatchTodayByFirst() {
		return isMatchTodayByFirst;
	}

	public void setIsMatchTodayByFirst(int isMatchTodayByFirst) {
		this.isMatchTodayByFirst = isMatchTodayByFirst;
	}

	public int getIsSavePhotoByTK() {
		return isSavePhotoByTK;
	}

	public void setIsSavePhotoByTK(int isSavePhotoByTK) {
		this.isSavePhotoByTK = isSavePhotoByTK;
	}

	public int getShutdownPCMode() {
		return shutdownPCMode;
	}

	public void setShutdownPCMode(int shutdownPCMode) {
		this.shutdownPCMode = shutdownPCMode;
	}

	public int getSuccessFontSize() {
		return successFontSize;
	}

	public void setSuccessFontSize(int successFontSize) {
		this.successFontSize = successFontSize;
	}

	public boolean isStandaloneCheckWork() {
		return isStandaloneCheckWork;
	}

	public void setStandaloneCheckWork(boolean isStandaloneCheckWork) {
		this.isStandaloneCheckWork = isStandaloneCheckWork;
	}

	public boolean isAllowRebackStandalone() {
		return isAllowRebackStandalone;
	}

	public void setAllowRebackStandalone(boolean isAllowRebackStandalone) {
		this.isAllowRebackStandalone = isAllowRebackStandalone;
	}

	public String getRestartStandaloneCheckCmd() {
		return restartStandaloneCheckCmd;
	}

	public void setRestartStandaloneCheckCmd(String restartStandaloneCheckCmd) {
		this.restartStandaloneCheckCmd = restartStandaloneCheckCmd;
	}

	public String getStandaloneCheckHeartFile() {
		return StandaloneCheckHeartFile;
	}

	public void setStandaloneCheckHeartFile(String standaloneCheckHeartFile) {
		StandaloneCheckHeartFile = standaloneCheckHeartFile;
	}

	public int getCameraCount() {
		return cameraCount;
	}

	public void setCameraCount(int cameraCount) {
		this.cameraCount = cameraCount;
	}

	public long getSecondDoorWaitTime() {
		return secondDoorWaitTime;
	}

	public void setSecondDoorWaitTime(long secondDoorWaitTime) {
		this.secondDoorWaitTime = secondDoorWaitTime;
	}

	public float getFrontCameraWindowOpacity() {
		return frontCameraWindowOpacity;
	}

	public void setFrontCameraWindowOpacity(float frontCameraWindowOpacity) {
		this.frontCameraWindowOpacity = frontCameraWindowOpacity;
	}

	public boolean isAllowRebackMain() {
		return isAllowRebackMain;
	}

	public void setAllowRebackMain(boolean isAllowRebackMain) {
		this.isAllowRebackMain = isAllowRebackMain;
	}

	public boolean isMainCrtlWork() {
		return isMainCrtlWork;
	}

	public void setMainCrtlWork(boolean isMainCrtlWork) {
		this.isMainCrtlWork = isMainCrtlWork;
	}

	public boolean isRebackFrontTrackFlag() {
		return rebackFrontTrackFlag;
	}

	public void setRebackFrontTrackFlag(boolean rebackFrontTrackFlag) {
		this.rebackFrontTrackFlag = rebackFrontTrackFlag;
	}

	public boolean isFrontCameraWork() {
		return isFrontCameraWork;
	}

	public void setFrontCameraWork(boolean isFrontCameraWork) {
		this.isFrontCameraWork = isFrontCameraWork;
	}

	public String getRestartFrontTrackCmd() {
		return restartFrontTrackCmd;
	}

	public void setRestartFrontTrackCmd(String restartFrontTrackCmd) {
		this.restartFrontTrackCmd = restartFrontTrackCmd;
	}

	public String getFrontHeartBeatLogFile() {
		return frontHeartBeatLogFile;
	}

	public void setFrontHeartBeatLogFile(String frontHeartBeatLogFile) {
		this.frontHeartBeatLogFile = frontHeartBeatLogFile;
	}

	public String getFrontCameraNo() {
		return frontCameraNo;
	}

	public void setFrontCameraNo(String frontCameraNo) {
		this.frontCameraNo = frontCameraNo;
	}

	public String getBehindCameraNo() {
		return behindCameraNo;
	}

	public void setBehindCameraNo(String behindCameraNo) {
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
			// log.error("Config build:", ex);
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
			log.debug("conf.properties dir==" + fn);

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
			// this.rebackTrackFile = p.getProperty("rebackTrackFile",
			// "D:/pitchecking/work/RebackFlag.log");
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
			this.initGamma = Integer.valueOf(p.getProperty("initGamma", "300"));
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
			this.nightGamma = Integer.valueOf(p.getProperty("nightGamma", "300"));
			this.nightCronStr = p.getProperty("nightCronStr", "0 0 18 * * ?");
			this.dayCameraTime = p.getProperty("dayCameraTime", "0800");
			this.nightCameraTime = p.getProperty("nightCameraTime", "1800");
			this.isUseLightLevelDevice = Integer.valueOf(p.getProperty("isUseLightLevelDevice", "0"));
			this.isReadLightLevel = Integer.valueOf(p.getProperty("isReadLightLevel", "0"));
			this.readLightLevelCronStr = p.getProperty("readLightLevelCronStr", "0/20 * * * * ?");
			this.backLightColorExposure = Integer.valueOf(p.getProperty("backLightColorExposure", "-5"));
			this.backLightColorBrightness = Integer.valueOf(p.getProperty("backLightColorBrightness", "-10"));
			this.backLightContrast = Integer.valueOf(p.getProperty("backLightContrast", "50"));
			this.backLightGain = Integer.valueOf(p.getProperty("backLightGain", "64"));
			this.backLightGamma = Integer.valueOf(p.getProperty("backLightGamma", "50"));
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
			this.frontCameraNo = p.getProperty("frontCameraNo", "111111111111");
			this.behindCameraNo = p.getProperty("behindCameraNo", "222222222222");
			this.frontHeartBeatLogFile = p.getProperty("frontHeartBeatLogFile", "D:/pitchecking/work/FrontHeart.log");
			this.restartFrontTrackCmd = p.getProperty("restartFrontTrackCmd",
					"D:/pitchecking/work/RestartFrontTrack.bat");
			this.frontCameraWindowOpacity = Float.valueOf(p.getProperty("frontCameraWindowOpacity", "0.2"));
			this.secondDoorWaitTime = Long.valueOf(p.getProperty("secondDoorWaitTime", "2000"));
			this.cameraCount = Integer.valueOf(p.getProperty("cameraCount", "2"));
			this.StandaloneCheckHeartFile = p.getProperty("StandaloneCheckHeartFile",
					"D:/pitchecking/work/PhotoCheckHeart.log");
			this.successFontSize = Integer.valueOf(p.getProperty("successFontSize", "150"));
			this.shutdownPCMode = Integer.valueOf(p.getProperty("shutdownPCMode", "2"));
			this.isSavePhotoByTK = Integer.valueOf(p.getProperty("isSavePhotoByTK", "0"));
			this.isMatchTodayByFirst = Integer.valueOf(p.getProperty("isMatchTodayByFirst", "0"));
			this.frontFaceTrackMode = Integer.valueOf(p.getProperty("frontFaceTrackMode", "2"));
			this.transferFaceMode = Integer.valueOf(p.getProperty("transferFaceMode", "1"));
			this.pitVerifyMode = Integer.valueOf(p.getProperty("pitVerifyMode", "1"));
			this.openSecondDoorMode = Integer.valueOf(p.getProperty("openSecondDoorMode", "1"));
			this.waitFirstDoorClosedTime = Integer.valueOf(p.getProperty("waitFirstDoorClosedTime", "3"));
			this.rebackReadCardMode = Integer.valueOf(p.getProperty("rebackReadCardMode", "1"));
			this.isCheckStandaloneHeart = Integer.valueOf(p.getProperty("isCheckStandaloneHeart", "0"));
			this.isVerifyFaceToCard = Integer.valueOf(p.getProperty("isVerifyFaceToCard", "1"));
			this.luminanceX = Integer.valueOf(p.getProperty("luminanceX", "0"));
			this.luminanceY = Integer.valueOf(p.getProperty("luminanceY", "0"));
			this.luminanceWidth = Integer.valueOf(p.getProperty("luminanceWidth", "960"));
			this.luminanceHeight = Integer.valueOf(p.getProperty("luminanceHeight", "540"));
			this.isUseFrontLuminanceListener = Integer.valueOf(p.getProperty("isUseFrontLuminanceListener", "0"));
			this.frontLuminancePara = p.getProperty("frontLuminancePara", "0");
			this.backLuminancePara = p.getProperty("backLuminancePara", "0");

			this.superBackLightColorExposure = Integer.valueOf(p.getProperty("superBackLightColorExposure", "-5"));
			this.superBackLightColorBrightness = Integer.valueOf(p.getProperty("superBackLightColorBrightness", "-30"));
			this.superBackLightContrast = Integer.valueOf(p.getProperty("superBackLightContrast", "50"));
			this.superBackLightGain = Integer.valueOf(p.getProperty("superBackLightGain", "64"));
			this.superBackLightGamma = Integer.valueOf(p.getProperty("superBackLightGamma", "300"));

			this.superAdd1BackLightColorExposure = Integer
					.valueOf(p.getProperty("superAdd1BackLightColorExposure", "-6"));
			this.superAdd1BackLightColorBrightness = Integer
					.valueOf(p.getProperty("superAdd1BackLightColorBrightness", "-10"));
			this.superAdd1BackLightContrast = Integer.valueOf(p.getProperty("superAdd1BackLightContrast", "50"));
			this.superAdd1BackLightGain = Integer.valueOf(p.getProperty("superAdd1BackLightGain", "64"));
			this.superAdd1BackLightGamma = Integer.valueOf(p.getProperty("superAdd1BackLightGamma", "300"));

			this.superAdd2BackLightColorExposure = Integer
					.valueOf(p.getProperty("superAdd2BackLightColorExposure", "-6"));
			this.superAdd2BackLightColorBrightness = Integer
					.valueOf(p.getProperty("superAdd2BackLightColorBrightness", "-30"));
			this.superAdd2BackLightContrast = Integer.valueOf(p.getProperty("superAdd2BackLightContrast", "50"));
			this.superAdd2BackLightGain = Integer.valueOf(p.getProperty("superAdd2BackLightGain", "64"));
			this.superAdd2BackLightGamma = Integer.valueOf(p.getProperty("superAdd2BackLightGamma", "200"));

			this.frontInitColorExposure = Integer.valueOf(p.getProperty("frontInitColorExposure", "-4"));
			this.frontInitColorBrightness = Integer.valueOf(p.getProperty("frontInitColorBrightness", "0"));
			this.frontInitContrast = Integer.valueOf(p.getProperty("frontInitContrast", "50"));
			this.frontInitGain = Integer.valueOf(p.getProperty("frontInitGain", "64"));
			this.frontInitGamma = Integer.valueOf(p.getProperty("frontInitGamma", "300"));

			this.frontNightColorExposure = Integer.valueOf(p.getProperty("frontNightColorExposure", "-4"));
			this.frontNightColorBrightness = Integer.valueOf(p.getProperty("frontNightColorBrightness", "40"));
			this.frontNightContrast = Integer.valueOf(p.getProperty("frontNightContrast", "50"));
			this.frontNightGain = Integer.valueOf(p.getProperty("frontNightGain", "64"));
			this.frontNightGamma = Integer.valueOf(p.getProperty("frontNightGamma", "300"));

			this.frontBackLightColorExposure = Integer.valueOf(p.getProperty("frontBackLightColorExposure", "-5"));
			this.frontBackLightColorBrightness = Integer.valueOf(p.getProperty("frontBackLightColorBrightness", "0"));
			this.frontBackLightContrast = Integer.valueOf(p.getProperty("frontBackLightContrast", "50"));
			this.frontBackLightGain = Integer.valueOf(p.getProperty("frontBackLightGain", "64"));
			this.frontBackLightGamma = Integer.valueOf(p.getProperty("frontBackLightGamma", "300"));

			this.frontSuperBackLightColorExposure = Integer
					.valueOf(p.getProperty("frontSuperBackLightColorExposure", "-6"));
			this.frontSuperBackLightColorBrightness = Integer
					.valueOf(p.getProperty("frontSuperBackLightColorBrightness", "-25"));
			this.frontSuperBackLightContrast = Integer.valueOf(p.getProperty("frontSuperBackLightContrast", "50"));
			this.frontSuperBackLightGain = Integer.valueOf(p.getProperty("frontSuperBackLightGain", "64"));
			this.frontSuperBackLightGamma = Integer.valueOf(p.getProperty("frontSuperBackLightGamma", "300"));

			this.resetCameraLightTime = p.getProperty("resetCameraLightTime", "1200");
			this.isUseTrail = Integer.valueOf(p.getProperty("isUseTrail", "0"));
			this.faceControlMode = Integer.valueOf(p.getProperty("faceControlMode", "1"));
			this.mysqlRecordType = Integer.valueOf(p.getProperty("mysqlRecordType", "1"));
			this.isUseThirdMQ = Integer.valueOf(p.getProperty("isUseThirdMQ", "0"));
			this.thirdMQUrl = p.getProperty("thirdMQUrl", "failover://tcp://127.0.0.1:61616");
			this.faceReceiveSocketPort = Integer.valueOf(p.getProperty("faceReceiveSocketPort", "36861"));
			this.faceResultReceiveSocketPort = Integer.valueOf(p.getProperty("faceResultReceiveSocketPort", "36862"));
			this.ocxFaceReceiveSocketPort = Integer.valueOf(p.getProperty("ocxFaceReceiveSocketPort", "36863"));
			this.isSetFrontCameraConfigByInit = Integer.valueOf(p.getProperty("isSetFrontCameraConfigByInit", "1"));
			this.isSetBackCameraConfigByInit = Integer.valueOf(p.getProperty("isSetBackCameraConfigByInit", "1"));
			this.isVerifyFaceOnce = Integer.valueOf(p.getProperty("isVerifyFaceOnce", "1"));
			this.sendFaceSourceBySocket = Integer.valueOf(p.getProperty("sendFaceSourceBySocket", "1"));
			this.isAdmitRepeatCheck = Integer.valueOf(p.getProperty("isAdmitRepeatCheck", "0"));
			this.ocxUsbCameraNo = Integer.valueOf(p.getProperty("ocxUsbCameraNo", "0"));
			this.isVerifyScreenAlwaysTop = Integer.valueOf(p.getProperty("isVerifyScreenAlwaysTop", "1"));
			this.gateSerialNo = p.getProperty("gateSerialNo", "00000000000000001111111111111111");
			this.isConnectTRService = Integer.valueOf(p.getProperty("isConnectTRService", "1"));
			this.backScreenMode = Integer.valueOf(p.getProperty("backScreenMode", "1"));
			this.frontScreenMode = Integer.valueOf(p.getProperty("frontScreenMode", "1"));
			this.usbCameraCanvasWidth = Integer.valueOf(p.getProperty("usbCameraCanvasWidth", "720"));
			this.usbCameraCanvasHeight = Integer.valueOf(p.getProperty("usbCameraCanvasHeight", "1280"));
			this.isCheckBackCameraHeart = Integer.valueOf(p.getProperty("isCheckBackCameraHeart", "1"));
			this.isNeedUseFirstDoor = Integer.valueOf(p.getProperty("isNeedUseFirstDoor", "1"));
			this.sendReceiptCount = Integer.valueOf(p.getProperty("sendReceiptCount", "1"));
			this.isUseWharfMQ = Integer.valueOf(p.getProperty("isUseWharfMQ", "0"));
			this.wharfMQUrl = p.getProperty("wharfMQUrl", "failover://tcp://127.0.0.1:61616");
			this.isSupportTicketWithC = Integer.valueOf(p.getProperty("isSupportTicketWithC", "1"));
			this.isSupportTicketWithD = Integer.valueOf(p.getProperty("isSupportTicketWithD", "1"));
			this.isSupportTicketWithG = Integer.valueOf(p.getProperty("isSupportTicketWithG", "1"));
			this.isSupportTicketWithK = Integer.valueOf(p.getProperty("isSupportTicketWithK", "1"));
			this.isGGQChaoshanMode = Integer.valueOf(p.getProperty("isGGQChaoshanMode", "0"));
			this.shipTicketUrl = p.getProperty("shipTicketUrl", "http://ticket.gxpft.com:8090/twebservice/realNameForJson/checkRealName");
			this.shipTicketCenterUser = p.getProperty("shipTicketCenterUser", "admin");
			this.shipTicketCenterPwd = p.getProperty("shipTicketCenterPwd", "21218CCA77804D2BA1922C33E0151105");
			this.wharfDevId = p.getProperty("wharfDevId", "000001");
			this.isCheckShipTicketRule = Integer.valueOf(p.getProperty("isCheckShipTicketRule", "1"));
			this.baseInfoXMLPath = p.getProperty("baseInfoXMLPath", "C:/SmartMonitor/RealNameGate/BaseInfo.xml");
			this.statusInfoXMLPath = p.getProperty("statusInfoXMLPath", "C:/SmartMonitor/RealNameGate/StatusInfo.xml");
			this.productionDate = p.getProperty("productionDate", "2017-01-01");
			this.isOutGateForCSQ = Integer.valueOf(p.getProperty("isOutGateForCSQ", "0"));
			this.updateFlapCountCronStr = p.getProperty("updateFlapCountCronStr", "0 0 18 * * ?");
			this.myStatusXML = p.getProperty("myStatusXML", "D:/pitchecking/config/StatusInfo.xml");
			this.updateTrainInfoCronStr = p.getProperty("updateTrainInfoCronStr", "0 0/10 * * * ?");
			this.isConnectLvFu = Integer.valueOf(p.getProperty("isConnectLvFu", "0"));
			this.lvFuDatasource = p.getProperty("lvFuDatasource", "FILE");
			this.lvFuFtpAddress = p.getProperty("lvFuFtpAddress", "10.168.72.60");
			this.lvFuFtpUser = p.getProperty("lvFuFtpUser", "pitcheck");
			this.lvFuFtpPwd = p.getProperty("lvFuFtpPwd", "pitcheck");
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
		System.out.println("getStandaloneCheckHeartFile==" + Config.getInstance().getStandaloneCheckHeartFile());
		System.out.println(Config.FrameHeigh / 2);
		System.out.println("" + Config.getInstance().getFrontLuminancePara());
		StringTokenizer st = new StringTokenizer(Config.getInstance().getFrontLuminancePara(), ",");
		while (st.hasMoreTokens()) {
			System.out.println("token = " + st.nextToken());
		}
		float ll = (float) 0.45678 + (float) 0.5678 + (float) 0.6789;
		System.out.println(ll / 3);
		System.out.println("" + Config.getInstance().getFaceCheckThreshold() * (float)0.4);
	}

}
