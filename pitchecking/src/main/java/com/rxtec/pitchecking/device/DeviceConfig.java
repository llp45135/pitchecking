package com.rxtec.pitchecking.device;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

public class DeviceConfig {
	private Logger log = LoggerFactory.getLogger("DeviceConfig");
	private static DeviceConfig _instance = new DeviceConfig();

	public static String softVersion = "160709.15.02";

	public static int idDeviceSucc = 1;
	public static int qrDeviceSucc = 1;
	private int idDeviceStatus = -1;
	private int qrdeviceStatus = -1;

	private String ticketXmlDir = "./conf/";
	private String ticketImgDir = "./img";
	private String stationDoc = "BaseData.xml";

	public static String readedIdImgPath = "./img/idinfo.jpg";
	public static String readerQRImgPath = "./img/QRReaded.jpg";
	public static String allowImgPath = "./img/tky_allow.gif";
	// public static String forbidenImgPath = "./img/tky_stop.gif";
	public static String forbidenImgPath = "./img/forbiden.jpg";
	public static String ticketImgPath = "./img/ticket.jpg";
	public static String qrReaderImgPath = "./img/qrreader.gif";
	public static String idReaderImgPath = "./img/idreader.gif";

	// 语音文件
	public static String idReaderWav = "./wav/thanks.wav";
	public static String qrReaderWav = "./wav/talkforever16.wav";
	public static String cameraWav = "./wav/camera.wav";// "./wav/12-35.wav";

	private int faceScreen = 0;
	private int ticketScreen = 1;
	private String gateNo = "00";
	private int CameraLEDPort = 0;
	public static int CameraLEDUnit = 0;
	public static int CameraLEDLevel = 31;
	private String GateCrtlPort = "COM2";
	private String GteCrtlSecondPort = "COM3";
	private int GateCrtoRate = 9600;
	private String TOPIC = "Pitchecking";
	private String TOPIC_RESULT = "PitcheckingResult";
	private String MQURL = "failover://" + "tcp://127.0.0.1:61616";
	private String USER = "pitchecking";
	private String PASSWORD = "pitchecking";
	private String ipAddress = "127.0.0.1";

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
	}

	public static synchronized DeviceConfig getInstance() {
		if (_instance == null)
			_instance = new DeviceConfig();
		return _instance;
	}

	public String getGteCrtlSecondPort() {
		return GteCrtlSecondPort;
	}

	public void setGteCrtlSecondPort(String gteCrtlSecondPort) {
		GteCrtlSecondPort = gteCrtlSecondPort;
	}

	public int getCameraLEDPort() {
		return CameraLEDPort;
	}

	public void setCameraLEDPort(int cameraLEDPort) {
		CameraLEDPort = cameraLEDPort;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getGateCrtlPort() {
		return GateCrtlPort;
	}

	public void setGateCrtlPort(String gateCrtlPort) {
		GateCrtlPort = gateCrtlPort;
	}

	public int getGateCrtoRate() {
		return GateCrtoRate;
	}

	public void setGateCrtoRate(int gateCrtoRate) {
		GateCrtoRate = gateCrtoRate;
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
			this.setGateNo(root.getChild("GateConfig").getAttributeValue("gateNo"));
			this.setFaceScreen(Integer.parseInt(root.getChild("GateConfig").getAttributeValue("faceScreen")));
			this.setTicketScreen(Integer.parseInt(root.getChild("GateConfig").getAttributeValue("ticketScreen")));
			this.setCameraLEDPort(Integer.parseInt(root.getChild("GateCrtlConfig").getAttributeValue("cameraLEDPort")));
			this.setGateCrtlPort(root.getChild("GateCrtlConfig").getAttributeValue("gateCrtlPort"));
			this.setGteCrtlSecondPort(root.getChild("GateCrtlConfig").getAttributeValue("gateCrtlSecondPort"));
			this.setGateCrtoRate(Integer.parseInt(root.getChild("GateCrtlConfig").getAttributeValue("gateCrtoRate")));
//			 this.setMQURL(root.getChild("MQConfig").getAttributeValue("MQURL"));
			this.setTOPIC(root.getChild("MQConfig").getAttributeValue("TOPIC"));
			this.setTOPIC_RESULT(root.getChild("MQConfig").getAttributeValue("TOPIC_RESULT"));
			this.setUSER(root.getChild("MQConfig").getAttributeValue("USER"));
			this.setPASSWORD(root.getChild("MQConfig").getAttributeValue("PASSWORD"));
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
			this.setMQURL("failover://tcp://" + etcIP + ":61616");
		} catch (UnknownHostException ex) {
			// TODO Auto-generated catch block
			log.error("DeviceConfig getLocalIPAddress:" + ex);
		}
		return etcIP;
	}

	public static void main(String[] args) {
		DeviceConfig dconfig = DeviceConfig.getInstance();

		System.out.println(DeviceConfig.getInstance().getStationsMap().size());
		System.out.println(DeviceConfig.getInstance().getStationsMap().get("GGQ").getStationName());
		System.out.println(DeviceConfig.getInstance().getStationName("KQW"));
		System.out.println(DeviceConfig.getInstance().getTicketTypesMap().size());
		System.out.println(DeviceConfig.getInstance().getSeatTypesMap().size());
		System.out.println(DeviceConfig.getInstance().getSeatTypesMap().get("O"));

		System.out.println("getGateNo==" + dconfig.getGateNo());
		System.out.println("getCameraLEDPort==" + dconfig.getCameraLEDPort());
		System.out.println("GateCrtoPort==" + dconfig.getGateCrtlPort());
		System.out.println("getGteCrtlSecondPort==" + dconfig.getGteCrtlSecondPort());
		System.out.println("GateCrtlRate==" + dconfig.getGateCrtoRate());
		System.out.println("getMQURL==" + dconfig.getMQURL());
		System.out.println("getTOPIC==" + dconfig.getTOPIC());
		System.out.println("getTOPIC_RESULT==" + dconfig.getTOPIC_RESULT());
		System.out.println("getUSER==" + dconfig.getUSER());
		System.out.println("getPASSWORD==" + dconfig.getPASSWORD());
		System.out.println("ipAddress==" + dconfig.getIpAddress());
	}
}
