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

	private float faceCheckThreshold = (float) 0.7;
	private float glassFaceCheckThreshold = (float) 0.68;
	private long faceCheckDelayTime = 5000;
	private int fastFaceDetect = 1;
	private int faceCheckingInteval = 100;
	private int defaultFaceCheckScreenDeley = 1000;
	private String imagesLogDir;
	private int roateCapture = 0;
	private int detectededFaceQueueLen = 5;
	private int detectededFaceQueueThreshold = 2;
	private int videoType = 1;
	private int videoCaptureFrequency=10;
	

	//
	private String ticketXmlDir;
	private String ticketImgDir;
	private String stationDoc;
	private boolean isDealQR = true;
	
	public int getVideoCaptureFrequency() {
		return videoCaptureFrequency;
	}

	public void setVideoCaptureFrequency(int videoCaptureFrequency) {
		this.videoCaptureFrequency = videoCaptureFrequency;
	}

	public boolean isDealQR() {
		return isDealQR;
	}

	public void setDealQR(boolean isDealQR) {
		this.isDealQR = isDealQR;
	}

	public String getTicketXmlDir() {
		return ticketXmlDir;
	}

	public void setTicketXmlDir(String ticketXmlDir) {
		this.ticketXmlDir = ticketXmlDir;
	}

	public String getTicketImgDir() {
		return ticketImgDir;
	}

	public void setTicketImgDir(String ticketImgDir) {
		this.ticketImgDir = ticketImgDir;
	}

	public String getStationDoc() {
		return stationDoc;
	}

	public void setStationDoc(String stationDoc) {
		this.stationDoc = stationDoc;
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

	public long getFaceCheckDelayTime() {
		return faceCheckDelayTime;
	}

	public void setFaceCheckDelayTime(long faceCheckDelayTime) {
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
			ClassLoader cl = this.getClass().getClassLoader();
			InputStream is = cl.getResourceAsStream("conf.properties");
			p.load(is);
			this.faceCheckThreshold = Float.valueOf(p.getProperty("FaceCheckThreshold", "0.7"));
			this.glassFaceCheckThreshold = Float.valueOf(p.getProperty("GlassFaceCheckThreshold", "0.68"));
			this.faceCheckDelayTime = Integer.valueOf(p.getProperty("FaceCheckDelayTime", "5000"));
			this.fastFaceDetect = Integer.valueOf(p.getProperty("FastFaceDetect", "1"));
			this.faceCheckingInteval = Integer.valueOf(p.getProperty("faceCheckingInteval", "100"));
			this.defaultFaceCheckScreenDeley = Integer.valueOf(p.getProperty("DefaultFaceCheckScreenDeley", "1000"));
			this.imagesLogDir = p.getProperty("ImagesLogDir", "C:/pitchecking/images");
			this.roateCapture = Integer.valueOf(p.getProperty("RoateCapture", "0"));
			this.detectededFaceQueueLen = Integer.valueOf(p.getProperty("DetectededFaceQueueLen", "5"));
			this.detectededFaceQueueThreshold = Integer.valueOf(p.getProperty("DetectededFaceQueueThreshold", "2"));
			this.videoType = Integer.valueOf(p.getProperty("VideoType", "2"));
			this.videoCaptureFrequency = Integer.valueOf(p.getProperty("VideoCaptureFrequency", "5"));
			
			//
			this.ticketXmlDir = p.getProperty("TicketXmlDir", "./xml/");
			this.ticketImgDir = p.getProperty("TicketImgDir", "./img/");
			this.stationDoc = p.getProperty("StationDoc", "BaseData.xml");

			is.close(); // 关闭流
		} catch (IOException e) {
			e.printStackTrace();
		}

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
	}

	public static synchronized Config getInstance() {
		if (_instance == null)
			_instance = new Config();
		return _instance;
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

	public static void main(String[] args) {
		Config.getInstance();
		System.out.println(Config.getInstance().getStationsMap().size());
		System.out.println(Config.getInstance().getStationsMap().get("GGQ").getStationName());
		System.out.println(Config.getInstance().getStationName("KQW"));
		System.out.println(Config.getInstance().getTicketTypesMap().size());
		System.out.println(Config.getInstance().getSeatTypesMap().size());
		System.out.println(Config.getInstance().getSeatTypesMap().get("O"));
	}

}
