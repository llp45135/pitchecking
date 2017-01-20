package com.rxtec.pitchecking.device;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.domain.StationInfo;
import com.rxtec.pitchecking.utils.CalUtils;

public class DeviceConfig {
	private Logger log = LoggerFactory.getLogger("DeviceConfig");
	private static DeviceConfig _instance = new DeviceConfig();

	public static String softVersion = "170119.21.01";
	private String softIdNo = "520203197912141118,440111197209283012,440881199502176714";

	public static int SINGLEDOOR = 1;
	public static int DOUBLEDOOR = 2;

	public static int idDeviceSucc = 1;
	public static int qrDeviceSucc = 1;
	private int idDeviceStatus = -1;
	private int qrdeviceStatus = -1;

	private String ticketXmlDir = "./conf/";
	private String ticketImgDir = "./img";
	private String stationDoc = "BaseData.xml";

	public static String readedIdImgPath = "./img/idinfo.jpg";
	public static String readedQRImgPath = "./img/QRReaded.jpg";
	public static String allowImgPath = "./img/tky_allow.gif";
	// public static String forbidenImgPath = "./img/tky_stop.gif";
	public static String forbidenImgPath = "./img/forbiden.jpg";
	public static String ticketImgPath = "./img/ticket.jpg";
	public static String qrReaderImgPath = "./img/qrreader.gif";
	public static String idReaderImgPath = "./img/idreader.gif";

	// public static String initImgPath = "./img/init2.gif";
	public static String initImgPath = "./img/initNew.gif";

	public static String faceBgImgPath = "./img/bluebg.jpg";// "./img/bg.png";

	// 语音文件
	// public static String idReaderWav = "./wav/thanks.wav";
	// public static String qrReaderWav = "./wav/talkforever16.wav";
	// public static String cameraWav = "./wav/camera_glasses.wav";//
	// "./wav/12-35.wav";
	// public static String emerDoorWav = "./wav/emerDoor.wav";

	// public static String takeTicketWav = "./wav/take_ticket.wav";
	// public static String takeTicketWav = "./wav/take_hat_6s.wav";

	public static String AudioUseHelpWav = "./wav/useHelp.wav";
	public static String AudioTakeCardWav = "./wav/takeCard.wav";
	public static String AudioTrackFaceWav = "./wav/trackFace.wav";
	public static String AudioTakeTicketWav = "./wav/longCheck.wav";
	public static String AudioCheckFailedWav = "./wav/checkFailed.wav";
	public static String AudioCheckSuccWav = "./wav/checkSucc.wav";
	public static String AudioFailedIdCardWav = "./wav/failedIdCard.wav";
	public static String AudioFailedQrcodeWav = "./wav/failedQrcode.wav";
	public static String AudioNeverTimeWav = "./wav/neverTime.wav";
	public static String AudioPassStationWav = "./wav/passStation.wav";
	public static String AudioPassTimeWav = "./wav/passTime.wav";
	public static String AudioValidIDandTicketWav = "./wav/validIDandTicket.wav";
	public static String AudioWrongStationWav = "./wav/wrongStation.wav";

	// public static int AudiocameraFlag = 201;
	public static int AudioCheckFailedFlag = 202;
	public static int AudioTakeTicketFlag = 203;
	public static int AudioCheckSuccFlag = 204;
	public static int AudioUseHelpFlag = 205;
	public static int AudioFailedIdCardFlag = 206;
	public static int AudioFailedQrcodeFlag = 207;
	public static int AudioNeverTimeFlag = 208;
	public static int AudioPassTimeFlag = 209;
	public static int AudioPassStationFlag = 210;
	public static int AudioValidIDandTicketFlag = 211;
	public static int AudioWrongStationFlag = 212;
	public static int AudioTakeCardFlag = 213;
	public static int AudioTrackFaceFlag = 214;

	private int versionFlag = 0;
	private int readerTimeDelay = 10;
	private int succTimeDelay = 4;
	private int checkTicketFlag = 1;
	private String belongStationCode = "IZQ";
	private int faceScreen = 0;
	private int ticketScreen = 1;
	private int guideScreen = 2;

	private String gateNo = "00";
	private String idDeviceType = "X";
	private String qrDeviceType = "V";
	private int CameraLEDPort = 0;
	public static int CameraLEDUnit = 0;
	public static int CameraLEDLevel = 31;
	private String firstGateCrtlPort = "COM2";
	private String secondGateCrtlPort = "COM3";
	private String honeywellQRPort = "COM7";
	private int gateCrtlRate = 9600;

	private int mqStartFlag = 0;
	private String TOPIC = "Pitchecking";
	private String TOPIC_RESULT = "PitcheckingResult";
	private String MQURL = "failover://" + "tcp://127.0.0.1:61616";
	private String USER = "pitchecking";
	private String PASSWORD = "pitchecking";
	private String ipAddress = "127.0.0.1";
	private String autoLogonCron = "0 0 5 * * ?";
	private String MQTT_CONN_STR = "tcp://localhost:1883";
	private String ManualCheck_Address = "127.0.0.1";
	private String ManualCheck_MQTTURL = "tcp://" + ManualCheck_Address + ":1883";
	private String ManualCheck_MQURL = "failover://" + "tcp://" + ManualCheck_Address + ":61616";
	private String PoliceServer_Address = "127.0.0.1";
	private String PoliceServerPort = "61616";
	private String PoliceServer_MQURL = "failover://" + "tcp://" + PoliceServer_Address + ":" + PoliceServerPort;

	// public static int TICKET_FRAME_TOPHEIGHT = 62;
	// public static int TICKET_FRAME_HEIGHT = 720;
	// public static int TICKET_FRAME_BOTTOMHEIGHT = 40;

	public static int TICKET_FRAME_WIGHT = 1024;

	public static int TICKET_FRAME_TOPHEIGHT = 100;
	public static int TICKET_FRAME_HEIGHT = 768;
	public static int TICKET_FRAME_BOTTOMHEIGHT = 60;

	public static String GAT_MQ_Verify_CLIENT = "V";// "Verify";
	public static String GAT_MQ_Track_CLIENT = "T";// "Track";
	public static String GAT_MQ_Standalone_CLIENT = "A";// "Alone";
	public static String GAT_MQ_Guide_CLIENT = "G";// "Guide";

	public static String OPEN_FIRSTDOOR = "{\"Event\": 1,\"Target\": \"127.0.0.1\",\"EventSource\":\"FaceVerify\"}";
	// public static String CLOSE_FIRSTDOOR = "DoorCmd01";
	public static String OPEN_SECONDDOOR = "{\"Event\": 2,\"Target\": \"127.0.0.1\",\"EventSource\":\"FaceVerify\"}";
	// public static String CLOSE_SECONDDDOOR = "DoorCmd11";
	public static String OPEN_THIRDDOOR = "{\"Event\": 3,\"Target\": \"127.0.0.1\",\"EventSource\":\"FaceVerify\"}";
	// public static String CLOSE_THIRDDOOR = "DoorCmd21";
	public static String Close_SECONDDOOR_Jms = "{\"Event\": 20,\"Target\": \"127.0.0.1\",\"EventSource\":\"MainVerify\"}";

	public static String EventTopic = "PITEventTopic";
	public static String Event_StartTracking = "{\"Event\": 10010,\"Target\": \"127.0.0.1\",\"EventSource\":\"TK\"}"; // 人脸开始核验
	public static String Event_VerifyFaceSucc = "{\"Event\": 10011,\"Target\": \"127.0.0.1\",\"EventSource\":\"TK\"}"; // 人脸核验成功
	public static String Event_VerifyFaceFailed = "{\"Event\": 10012,\"Target\": \"127.0.0.1\",\"EventSource\":\"TK\"}"; // 人脸核验失败
	public static String Event_ReadIDCardFailed = "{\"Event\": 80004,\"Target\": \"127.0.0.1\",\"EventSource\":\"TK\"}"; // 读二代证失败
	public static String Event_ReadTicketFailed = "{\"Event\": 80001,\"Target\": \"127.0.0.1\",\"EventSource\":\"TK\"}"; // 读二维码失败
	public static String Event_InvalidCardAndTicket = "{\"Event\": 80002,\"Target\": \"127.0.0.1\",\"EventSource\":\"TK\"}"; // 票证不符
	public static String Event_InvalidStation = "{\"Event\": 51605,\"Target\": \"127.0.0.1\",\"EventSource\":\"TK\"}"; // 非本站乘车
	public static String Event_InvalidTrainDate = "{\"Event\": 51666,\"Target\": \"127.0.0.1\",\"EventSource\":\"TK\"}"; // 非当日乘车
	public static String Event_NotInTime = "{\"Event\": 90236,\"Target\": \"127.0.0.1\",\"EventSource\":\"TK\"}"; // 未到进站时间
	public static String Event_PassCheckTime = "{\"Event\": 90238,\"Target\": \"127.0.0.1\",\"EventSource\":\"TK\"}"; // 已过进站时间
	public static String Event_QRDeviceException = "{\"Event\": -1,\"Target\": \"127.0.0.1\",\"EventSource\":\"TK\"}"; // 二维码读卡器故障
	public static String Event_IDDeviceException = "{\"Event\": -2,\"Target\": \"127.0.0.1\",\"EventSource\":\"TK\"}"; // 二代证读卡器故障
	public static String Event_DeviceStartupSucc = "{\"Event\": 10000,\"Target\": \"127.0.0.1\",\"EventSource\":\"TK\"}"; // 设备启动成功

	public static String Clear_QrCode_Jms = "{\"EventSource\":\"QRDevice\",\"QRCode\":\"\",\"Target\":\"127.0.0.1\"}";

	// 开关门
	public static int Event_OpenFirstDoor = 1; // 开前门
	public static int Event_OpenSecondDoor = 2; // 开后门
	public static int Event_OpenThirdDoor = 3; // 开边门
	public static int Event_PauseService = 4; // 暂停服务
	public static int Event_ContinueService = 5; // 恢复服务
	public static int Event_ResetService = 6; // 重启闸机

	public static int Event_ErrorReadIDCard = 10; // 读二代证失败
	public static int Event_ErrorReadQRCode = 11; // 读二维码失败
	public static int Event_ErrorVerifyTicket = 12; // 票证不符
	public static int Event_ErrorFindETicket = 13; // 找不到电子票
	public static int Event_BeforeCheckinTime = 14; // 未到检票时间
	public static int Event_HasPassedCheckinTime = 15; // 已过检票时间
	public static int Event_NotThisCheckinStation = 16; // 非本站乘车

	public static int Event_SecondDoorHasClosed = 20; // 第二道门已关

	private boolean isAllowOpenSecondDoor = true; // 是否允许开第二道门
	private boolean isInTracking = false; // 是否处于人脸核验中

	private boolean isInService = true; // 是否处于服务中

	private String gateIPList = "192.168.0.2,192.168.0.3,192.168.0.4,192.168.0.5";

	private Map<String, String> SZQTrainsMap;

	private int stopCheckMinutes;
	private int NotStartCheckMinutes;

	public int getStopCheckMinutes() {
		return stopCheckMinutes;
	}

	public void setStopCheckMinutes(int stopCheckMinutes) {
		this.stopCheckMinutes = stopCheckMinutes;
	}

	public int getNotStartCheckMinutes() {
		return NotStartCheckMinutes;
	}

	public void setNotStartCheckMinutes(int notStartCheckMinutes) {
		NotStartCheckMinutes = notStartCheckMinutes;
	}

	public Map<String, String> getSZQTrainsMap() {
		return SZQTrainsMap;
	}

	public void setSZQTrainsMap(Map<String, String> sZQTrainsMap) {
		SZQTrainsMap = sZQTrainsMap;
	}

	public boolean isInService() {
		return isInService;
	}

	public void setInService(boolean isInService) {
		this.isInService = isInService;
	}

	public String getGateIPList() {
		return gateIPList;
	}

	public void setGateIPList(String gateIPList) {
		this.gateIPList = gateIPList;
	}

	public int getGuideScreen() {
		return guideScreen;
	}

	public void setGuideScreen(int guideScreen) {
		this.guideScreen = guideScreen;
	}

	public boolean isInTracking() {
		return isInTracking;
	}

	public void setInTracking(boolean isInTracking) {
		this.isInTracking = isInTracking;
	}

	public boolean isAllowOpenSecondDoor() {
		return isAllowOpenSecondDoor;
	}

	public void setAllowOpenSecondDoor(boolean isAllowOpenSecondDoor) {
		this.isAllowOpenSecondDoor = isAllowOpenSecondDoor;
	}

	public String getSoftIdNo() {
		return softIdNo;
	}

	public void setSoftIdNo(String softIdNo) {
		this.softIdNo = softIdNo;
	}

	public String getPoliceServerPort() {
		return PoliceServerPort;
	}

	public void setPoliceServerPort(String policeServerPort) {
		PoliceServerPort = policeServerPort;
	}

	public String getPoliceServer_Address() {
		return PoliceServer_Address;
	}

	public void setPoliceServer_Address(String policeServer_Address) {
		PoliceServer_Address = policeServer_Address;
	}

	public String getPoliceServer_MQURL() {
		return PoliceServer_MQURL;
	}

	public void setPoliceServer_MQURL(String policeServer_MQURL) {
		PoliceServer_MQURL = policeServer_MQURL;
	}

	public String getManualCheck_Address() {
		return ManualCheck_Address;
	}

	public void setManualCheck_Address(String manualCheck_Address) {
		ManualCheck_Address = manualCheck_Address;
	}

	public String getManualCheck_MQTTURL() {
		return ManualCheck_MQTTURL;
	}

	public void setManualCheck_MQTTURL(String manualCheck_MQTTURL) {
		ManualCheck_MQTTURL = manualCheck_MQTTURL;
	}

	public String getManualCheck_MQURL() {
		return ManualCheck_MQURL;
	}

	public void setManualCheck_MQURL(String manualCheck_MQURL) {
		ManualCheck_MQURL = manualCheck_MQURL;
	}

	public String getMQTT_CONN_STR() {
		return MQTT_CONN_STR;
	}

	public void setMQTT_CONN_STR(String mQTT_CONN_STR) {
		MQTT_CONN_STR = mQTT_CONN_STR;
	}

	public String getAutoLogonCron() {
		return autoLogonCron;
	}

	public void setAutoLogonCron(String autoLogonCron) {
		this.autoLogonCron = autoLogonCron;
	}

	public String getIdDeviceType() {
		return idDeviceType;
	}

	public void setIdDeviceType(String idDeviceType) {
		this.idDeviceType = idDeviceType;
	}

	public int getVersionFlag() {
		return versionFlag;
	}

	public void setVersionFlag(int versionFlag) {
		this.versionFlag = versionFlag;
	}

	public int getSuccTimeDelay() {
		return succTimeDelay;
	}

	public void setSuccTimeDelay(int succTimeDelay) {
		this.succTimeDelay = succTimeDelay;
	}

	public String getQrDeviceType() {
		return qrDeviceType;
	}

	public void setQrDeviceType(String qrDeviceType) {
		this.qrDeviceType = qrDeviceType;
	}

	public String getHoneywellQRPort() {
		return honeywellQRPort;
	}

	public void setHoneywellQRPort(String honeywellQRPort) {
		this.honeywellQRPort = honeywellQRPort;
	}

	public int getReaderTimeDelay() {
		return readerTimeDelay;
	}

	public void setReaderTimeDelay(int readerTimeDelay) {
		this.readerTimeDelay = readerTimeDelay;
	}

	public int getCheckTicketFlag() {
		return checkTicketFlag;
	}

	public void setCheckTicketFlag(int checkTicketFlag) {
		this.checkTicketFlag = checkTicketFlag;
	}

	public int getMqStartFlag() {
		return mqStartFlag;
	}

	public void setMqStartFlag(int mqStartFlag) {
		this.mqStartFlag = mqStartFlag;
	}

	public String getBelongStationCode() {
		return belongStationCode;
	}

	public void setBelongStationCode(String belongStationCode) {
		this.belongStationCode = belongStationCode;
	}

	public String getGateNo() {
		return gateNo;
	}

	public int getFaceScreen() {
		return faceScreen;
	}

	public void setFaceScreen(int faceScreen) {
		this.faceScreen = faceScreen;
	}

	public int getTicketScreen() {
		return ticketScreen;
	}

	public void setTicketScreen(int ticketScreen) {
		this.ticketScreen = ticketScreen;
	}

	public void setGateNo(String gateNo) {
		this.gateNo = gateNo;
	}

	private DeviceConfig() {
		try {
			readStationConfigDataFromLocal();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.readDeviceConfigFromFile();

		this.getLocalIPAddress();

		this.getSZQTrains();
	}

	public static synchronized DeviceConfig getInstance() {
		if (_instance == null)
			_instance = new DeviceConfig();
		return _instance;
	}

	public int getCameraLEDPort() {
		return CameraLEDPort;
	}

	public void setCameraLEDPort(int cameraLEDPort) {
		CameraLEDPort = cameraLEDPort;
	}

	public String getFirstGateCrtlPort() {
		return firstGateCrtlPort;
	}

	public void setFirstGateCrtlPort(String firstGateCrtlPort) {
		this.firstGateCrtlPort = firstGateCrtlPort;
	}

	public String getSecondGateCrtlPort() {
		return secondGateCrtlPort;
	}

	public void setSecondGateCrtlPort(String secondGateCrtlPort) {
		this.secondGateCrtlPort = secondGateCrtlPort;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getGateCrtlRate() {
		return gateCrtlRate;
	}

	public void setGateCrtlRate(int gateCrtlRate) {
		this.gateCrtlRate = gateCrtlRate;
	}

	public String getTOPIC() {
		return TOPIC;
	}

	public void setTOPIC(String tOPIC) {
		TOPIC = tOPIC;
	}

	public String getTOPIC_RESULT() {
		return TOPIC_RESULT;
	}

	public void setTOPIC_RESULT(String tOPIC_RESULT) {
		TOPIC_RESULT = tOPIC_RESULT;
	}

	public String getMQURL() {
		return MQURL;
	}

	public void setMQURL(String mQURL) {
		MQURL = mQURL;
	}

	public String getUSER() {
		return USER;
	}

	public void setUSER(String uSER) {
		USER = uSER;
	}

	public String getPASSWORD() {
		return PASSWORD;
	}

	public void setPASSWORD(String pASSWORD) {
		PASSWORD = pASSWORD;
	}

	public int getIdDeviceStatus() {
		return idDeviceStatus;
	}

	public void setIdDeviceStatus(int idDeviceStatus) {
		this.idDeviceStatus = idDeviceStatus;
	}

	public int getQrdeviceStatus() {
		return qrdeviceStatus;
	}

	public void setQrdeviceStatus(int qrdeviceStatus) {
		this.qrdeviceStatus = qrdeviceStatus;
	}

	private Map<String, StationInfo> stationsMap;
	private Map<Integer, String> ticketTypesMap;
	private Map<String, String> seatTypesMap;

	public Map<String, StationInfo> getStationsMap() {
		return stationsMap;
	}

	public void setStationsMap(Map stationsMap) {
		this.stationsMap = stationsMap;
	}

	public String getStationName(String stationTeleCode) {
		String stationName = "";
		if (this.stationsMap.get(stationTeleCode) != null) {
			stationName = this.stationsMap.get(stationTeleCode).getStationName();
		}
		return stationName;
	}

	public Map<Integer, String> getTicketTypesMap() {
		return ticketTypesMap;
	}

	public void setTicketTypesMap(Map ticketTypesMap) {
		this.ticketTypesMap = ticketTypesMap;
	}

	public Map<String, String> getSeatTypesMap() {
		return seatTypesMap;
	}

	public void setSeatTypesMap(Map seatTypesMap) {
		this.seatTypesMap = seatTypesMap;
	}

	/**
	 * 读取本地站名表文件
	 * 
	 * @throws FileNotFoundException
	 * @throws JDOMException
	 * @throws IOException
	 */
	private void readStationConfigDataFromLocal() throws FileNotFoundException, JDOMException, IOException {
		log.debug("we get Stations Config from local file!");
		SAXBuilder saxBuilder = new org.jdom.input.SAXBuilder();
		Document stationdoc;
		stationdoc = saxBuilder.build(new FileInputStream(this.ticketXmlDir + this.stationDoc));

		org.jdom.Element tkyRoot = stationdoc.getRootElement();
		List<Element> stationlists;
		stationlists = XPath.selectNodes(tkyRoot, "/ETicketMsg/StationInfos/StationInfo");
		Map<String, StationInfo> stationsMap = new HashMap();

		for (int i = 0; i < stationlists.size(); i++) {
			Element node = (Element) stationlists.get(i);
			StationInfo stationInfo = new StationInfo();
			stationInfo.setStationTelecode(node.getAttributeValue("stationTelecode").trim());
			stationInfo.setStationName(node.getAttributeValue("stationName").trim());
			stationInfo.setBelongLineCode(node.getAttributeValue("belongLineCode").trim());
			stationInfo.setBelongLineName(node.getAttributeValue("belongLineName").trim());
			stationInfo.setStartDate(node.getAttributeValue("startDate").trim());
			stationInfo.setDistance(Integer.parseInt(node.getAttributeValue("distance").trim()));
			stationsMap.put(node.getAttributeValue("stationTelecode").trim(), stationInfo);
		}
		//
		List<Element> ticketTypes;
		ticketTypes = XPath.selectNodes(tkyRoot, "/ETicketMsg/TicketTypes/TicketType");
		Map<Integer, String> ticketTypesMap = new HashMap<Integer, String>();
		for (int i = 0; i < ticketTypes.size(); i++) {
			Element node = (Element) ticketTypes.get(i);
			int ticketTypeId = Integer.parseInt(node.getAttributeValue("ticketTypeId"));
			String ticketTypeName = node.getAttributeValue("ticketTypeName").trim();
			// log.debug("ticketType==" + ticketTypeId + "-" + ticketTypeName);
			ticketTypesMap.put(ticketTypeId, ticketTypeName);
		}
		//
		List<Element> seatTypes;
		seatTypes = XPath.selectNodes(tkyRoot, "/ETicketMsg/SeatTypes/SeatType");
		Map<String, String> seatTypesMap = new HashMap();
		for (int i = 0; i < seatTypes.size(); i++) {
			Element node = (Element) seatTypes.get(i);
			String seatTypeId = node.getAttributeValue("seatTypeId").trim();
			String seatTypeName = node.getAttributeValue("seatTypeName").trim();
			// log.debug("seatType==" + seatTypeId + "-" + seatTypeName);
			seatTypesMap.put(seatTypeId, seatTypeName);
		}

		setStationsMap(stationsMap);
		setTicketTypesMap(ticketTypesMap);
		setSeatTypesMap(seatTypesMap);
	}

	/**
	 * 读本机配置文件
	 * 
	 * @throws FileNotFoundException
	 * @throws JDOMException
	 * @throws IOException
	 */
	private void readDeviceConfigFromFile() {
		try {
			SAXBuilder saxhandle = new org.jdom.input.SAXBuilder();
			Document deviceDoc;
			deviceDoc = saxhandle.build(new FileInputStream("./conf/GateConfig.xml"));
			org.jdom.Element root = deviceDoc.getRootElement();
			this.setVersionFlag(Integer.parseInt(root.getChild("GateConfig").getAttributeValue("versionFlag")));
			this.setCheckTicketFlag(Integer.parseInt(root.getChild("GateConfig").getAttributeValue("checkTicketFlag")));
			this.setBelongStationCode(root.getChild("GateConfig").getAttributeValue("belongStationCode"));
			this.setGateNo(root.getChild("GateConfig").getAttributeValue("gateNo"));
			this.setFaceScreen(Integer.parseInt(root.getChild("GateConfig").getAttributeValue("faceScreen")));
			this.setTicketScreen(Integer.parseInt(root.getChild("GateConfig").getAttributeValue("ticketScreen")));
			this.setGuideScreen(Integer.parseInt(root.getChild("GateConfig").getAttributeValue("guideScreen")));

			this.setReaderTimeDelay(Integer.parseInt(root.getChild("GateConfig").getAttributeValue("readerTimeDelay")));
			this.setSuccTimeDelay(Integer.parseInt(root.getChild("GateConfig").getAttributeValue("succTimeDelay")));
			this.setIdDeviceType(root.getChild("GateConfig").getAttributeValue("idDeviceType"));
			this.setQrDeviceType(root.getChild("GateConfig").getAttributeValue("qrDeviceType"));
			this.setAutoLogonCron(root.getChild("GateConfig").getAttributeValue("autoLogonCron"));
			this.setStopCheckMinutes(
					Integer.parseInt(root.getChild("GateConfig").getAttributeValue("stopCheckMinutes")));
			this.setNotStartCheckMinutes(
					Integer.parseInt(root.getChild("GateConfig").getAttributeValue("NotStartCheckMinutes")));

			this.setCameraLEDPort(Integer.parseInt(root.getChild("GateCrtlConfig").getAttributeValue("cameraLEDPort")));
			this.setFirstGateCrtlPort(root.getChild("GateCrtlConfig").getAttributeValue("firstGateCrtlPort"));
			this.setSecondGateCrtlPort(root.getChild("GateCrtlConfig").getAttributeValue("secondGateCrtlPort"));
			this.setHoneywellQRPort(root.getChild("GateCrtlConfig").getAttributeValue("honeywellQRPort"));
			this.setGateCrtlRate(Integer.parseInt(root.getChild("GateCrtlConfig").getAttributeValue("gateCrtlRate")));
			this.setMqStartFlag(Integer.parseInt(root.getChild("MQConfig").getAttributeValue("mqStartFlag")));
			this.setMQURL(root.getChild("MQConfig").getAttributeValue("MQURL"));
			this.setTOPIC(root.getChild("MQConfig").getAttributeValue("TOPIC"));
			this.setTOPIC_RESULT(root.getChild("MQConfig").getAttributeValue("TOPIC_RESULT"));
			this.setUSER(root.getChild("MQConfig").getAttributeValue("USER"));
			this.setPASSWORD(root.getChild("MQConfig").getAttributeValue("PASSWORD"));
			this.setManualCheck_Address(root.getChild("MQConfig").getAttributeValue("ManualCheck_Address"));
			this.setManualCheck_MQURL("failover://" + "tcp://" + ManualCheck_Address + ":61616");
			this.setManualCheck_MQTTURL("tcp://" + ManualCheck_Address + ":1883");
			this.setSoftIdNo(root.getChild("AppConfig").getAttributeValue("softIdNo"));
			this.setGateIPList(root.getChild("AppConfig").getAttributeValue("gateIPList"));

			this.setPoliceServer_Address(root.getChild("MQConfig").getAttributeValue("PoliceServer_Address"));
			this.setPoliceServerPort(root.getChild("MQConfig").getAttributeValue("PoliceServerPort"));
			this.setPoliceServer_MQURL("failover://" + "tcp://" + PoliceServer_Address + ":" + PoliceServerPort);
		} catch (JDOMException | IOException ex) {
			// TODO Auto-generated catch block
			log.error("DeviceConfig readDeviceConfigFromFile:" + ex);
		}
	}

	/**
	 * 获取本机IP地址
	 * 
	 * @return
	 */
	private String getLocalIPAddress() {
		String etcIP = "";
		try {
			InetAddress ip;
			ip = InetAddress.getLocalHost();// 这个ip是和/etc/hosts中查找到
			etcIP = ip.getHostAddress();
			log.debug("gateConfig.getIpAddr==" + etcIP);
			this.setIpAddress(etcIP);
			// this.setMQURL("failover://tcp://" + etcIP + ":61616");
		} catch (UnknownHostException ex) {
			// TODO Auto-generated catch block
			log.error("DeviceConfig getLocalIPAddress:" + ex);
		}
		return etcIP;
	}

	/**
	 * 
	 * @param fileName
	 * @return
	 */
	private void getSZQTrains() {
		Map<String, String> trainsMap = new HashMap<String, String>();

		String fileName = "./conf/train_list.txt";
		// if (this.belongStationCode.equals("SZQ")) {
		// fileName = "./conf/szq_adPlan.txt";
		// }
		// if (this.belongStationCode.equals("CSQ")) {
		// fileName = "./conf/csq_plan.txt";
		// }
		// if (this.belongStationCode.equals("GGQ")) {
		// fileName = "./conf/ggq_adPlan.txt";
		// }
		log.info("fileName==" + fileName);
		if (!fileName.equals("")) {
			File file = new File(fileName);
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
				String tempString = null;
				int line = 1;
				while ((tempString = reader.readLine()) != null) {
					log.debug("line " + line + ": " + tempString);
					if (!tempString.trim().equals("")) {
						int k = tempString.indexOf("@");
						int j = tempString.lastIndexOf("@");
						String stationCode = tempString.substring(j + 1);
//						log.info("stationCode=="+stationCode);
						if (stationCode.equals(this.belongStationCode)) {
							String trainCode = tempString.substring(0, k).startsWith("0") ? tempString.substring(1, k)
									: tempString.substring(0, k);
							String startTime = tempString.substring(k + 1, k + 6);
							log.info("trainCode==" + trainCode + ",startTime==" + " " + startTime);
							trainsMap.put(trainCode, startTime);
						}
					}
					line++;
				}
				reader.close();
			} catch (IOException e) {
				log.error("getSZQTrains:", e);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e1) {
						log.error("getSZQTrains:", e1);
					}
				}
			}

			this.SZQTrainsMap = trainsMap;
			log.info("TrainsMap.size==" + this.SZQTrainsMap.size());
		}
	}

	public static void main(String[] args) {
		DeviceConfig dconfig = DeviceConfig.getInstance();

		System.out.println("getStationsMap.size==" + DeviceConfig.getInstance().getStationsMap().size());
		System.out.println(
				"GGQ's stationName==" + DeviceConfig.getInstance().getStationsMap().get("GGQ").getStationName());
		System.out.println("KQW's stationName==" + DeviceConfig.getInstance().getStationName("KQW"));
		System.out.println("getTicketTypesMap.size==" + DeviceConfig.getInstance().getTicketTypesMap().size());
		System.out.println("getSeatTypesMap.size==" + DeviceConfig.getInstance().getSeatTypesMap().size());
		System.out.println("seatType O is==" + DeviceConfig.getInstance().getSeatTypesMap().get("O"));

		System.out.println("getBelongStationCode==" + dconfig.getBelongStationCode());
		System.out.println(
				"getStationName==" + DeviceConfig.getInstance().getStationName(dconfig.getBelongStationCode()));
		System.out.println("getGateNo==" + dconfig.getGateNo());
		System.out.println("getCameraLEDPort==" + dconfig.getCameraLEDPort());
		System.out.println("getFirstGateCrtlPort==" + dconfig.getFirstGateCrtlPort());
		System.out.println("getSecondGateCrtlPort==" + dconfig.getSecondGateCrtlPort());
		System.out.println("GateCrtlRate==" + dconfig.getGateCrtlRate());
		System.out.println("getMQURL==" + dconfig.getMQURL());
		System.out.println("getTOPIC==" + dconfig.getTOPIC());
		System.out.println("getTOPIC_RESULT==" + dconfig.getTOPIC_RESULT());
		System.out.println("getUSER==" + dconfig.getUSER());
		System.out.println("getPASSWORD==" + dconfig.getPASSWORD());
		System.out.println("ipAddress==" + dconfig.getIpAddress());
		System.out.println("autoLogonCron==" + dconfig.getAutoLogonCron());
		System.out.println("getManualCheck_MQURL==" + dconfig.getManualCheck_MQURL());
		System.out.println("getManualCheck_MQTTURL==" + dconfig.getManualCheck_MQTTURL());
		System.out.println("getPoliceServer_MQURL==" + dconfig.getPoliceServer_MQURL());
		System.out.println("getSoftIdNo==" + dconfig.getSoftIdNo());
		if (dconfig.getSZQTrainsMap() != null)
			System.out.println("getSZQTrainsMap.size==" + dconfig.getSZQTrainsMap().size());
		System.out.println("getStopCheckMinutes==" + dconfig.getStopCheckMinutes());
		System.out.println("getNotStartCheckMinutes==" + dconfig.getNotStartCheckMinutes());
	}
}
