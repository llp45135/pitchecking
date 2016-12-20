package com.rxtec.pitchecking.mq;

import java.io.Serializable;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
import com.rxtec.pitchecking.utils.BASE64;
import com.rxtec.pitchecking.utils.JsonUtils;

public class PITInfoJson implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int msgType = -1;
	private int idHashCode = -1;
	private String idCardNo = "";
	private String qrCode="";
	private int gender = 0;
	private int event = -1;
	private String ipAddress = "";
	public static int Male = 1;
	public static int Female = 2;

	public static int MSG_TYPE_FRAME = 1;
	public static int MSG_TYPE_VERIFY = 2;
	public static int MSG_TYPE_EVENT = 3;
	public static int VERIFY_PASSED = 1;
	public static int VERIFY_FAILED = 0;

	private int age = -1;
	private String idPicImageBase64 = "";
	private String faceImageBase64 = "";
	private String frameImageBase64 = "";
	private float similarity = 0;
	private int isVerifyPassed = -1;

	public String getQrCode() {
		return qrCode;
	}

	public void setQrCode(String qrCode) {
		this.qrCode = qrCode;
	}

	public String getIdCardNo() {
		return idCardNo;
	}

	public void setIdCardNo(String idCardNo) {
		this.idCardNo = idCardNo;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getEvent() {
		return event;
	}

	public void setEvent(int event) {
		this.event = event;
	}

	public int getIsVerifyPassed() {
		return isVerifyPassed;
	}

	public void setIsVerifyPassed(int isVerifyPassed) {
		this.isVerifyPassed = isVerifyPassed;
	}

	private String jsonStr = "";

	public String getJsonStr() {
		return jsonStr;
	}

	public void setJsonStr(String jsonStr) {
		this.jsonStr = jsonStr;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public String getFaceImageBase64() {
		return faceImageBase64;
	}

	public void setFaceImageBase64(String faceImageBase64) {
		this.faceImageBase64 = faceImageBase64;
	}

	public String getIdPicImageBase64() {
		return idPicImageBase64;
	}

	public void setIdPicImageBase64(String idPicImageBase64) {
		this.idPicImageBase64 = idPicImageBase64;
	}

	public String getFrameImageBase64() {
		return frameImageBase64;
	}

	public void setFrameImageBase64(String frameImageBase64) {
		this.frameImageBase64 = frameImageBase64;
	}

	public int getIdHashCode() {
		return idHashCode;
	}

	public void setIdHashCode(int idHashCode) {
		this.idHashCode = idHashCode;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public float getSimilarity() {
		return similarity;
	}

	public void setSimilarity(float similarity) {
		this.similarity = similarity;
		if (similarity >= Config.getInstance().getFaceCheckThreshold())
			this.isVerifyPassed = VERIFY_PASSED;
		else
			this.isVerifyPassed = VERIFY_FAILED;
	}

	/**
	 * 
	 * @param pitData
	 * @throws Exception
	 */
	public PITInfoJson(PITVerifyData pitData) throws Exception {
		if (pitData == null)
			return;
		if (pitData.getFrameImg() != null)
			this.setFrameImageBase64(BASE64.encryptBASE64(pitData.getFrameImg()));

		this.setIdCardNo(BASE64.encryptBASE64(pitData.getIdNo().getBytes()));
		this.setQrCode(pitData.getQrCode());
		this.setGender(pitData.getGender());
		this.setAge(pitData.getAge());
		this.setIdHashCode(pitData.getIdNo().hashCode());
		if (pitData.getIdCardImg() != null)
			this.setIdPicImageBase64(BASE64.encryptBASE64(pitData.getIdCardImg()));
		this.setSimilarity(pitData.getVerifyResult());

		if (pitData.getFaceImg() != null)
			this.setFaceImageBase64(BASE64.encryptBASE64(pitData.getFaceImg()));

		this.ipAddress = DeviceConfig.getInstance().getIpAddress();
		this.msgType = PITInfoJson.MSG_TYPE_VERIFY;		
		jsonStr = JsonUtils.serialize(this);
	}

	public PITInfoJson(byte[] frameImgBytes) throws Exception {
		if (frameImgBytes == null)
			return;
		this.frameImageBase64 = BASE64.encryptBASE64(frameImgBytes);
		this.ipAddress = DeviceConfig.getInstance().getIpAddress();
		this.msgType = PITInfoJson.MSG_TYPE_FRAME;
		jsonStr = JsonUtils.serialize(this);

	}

	public PITInfoJson(int event) throws Exception {
		this.ipAddress = DeviceConfig.getInstance().getIpAddress();
		this.msgType = PITInfoJson.MSG_TYPE_EVENT;
		this.event = event;
		jsonStr = JsonUtils.serialize(this);
	}

}
