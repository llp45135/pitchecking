package com.rxtec.pitchecking.device;

public class DeviceConfig {
	private static DeviceConfig _instance = new DeviceConfig();
	
	public static int idDeviceSucc = 1;
	public static int qrDeviceSucc = 1;
	private int idDeviceStatus = -1;
	private int qrdeviceStatus = -1;
	
	public static String allowImgPath = "./img/tky_allow.gif";
//	public static String forbidenImgPath = "./img/tky_stop.gif";
	public static String forbidenImgPath = "./img/forbiden.jpg";
	public static String ticketImgPath = "./img/ticket.jpg";
	public static String qrReaderImgPath = "./img/qrreader.gif";
	public static String idReaderImgPath = "./img/idreader.gif";
	

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

	private DeviceConfig() {

	}

	public static synchronized DeviceConfig getInstance() {
		if (_instance == null)
			_instance = new DeviceConfig();
		return _instance;
	}
}
