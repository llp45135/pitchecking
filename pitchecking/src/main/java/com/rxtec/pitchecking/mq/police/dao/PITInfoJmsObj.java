package com.rxtec.pitchecking.mq.police.dao;

import java.io.Serializable;

public class PITInfoJmsObj implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int msgType;
	private String idCardNo;
	private int idHashCode;
	private int gender;
	private int event;
	private String ipAddress;
	private int age;               
	private String idPicImageBase64;
	private String faceImageBase64;
	private String frameImageBase64;
	private float similarity;
	private int isVerifyPassed;
	private String jsonStr;

	public PITInfoJmsObj() {
		// TODO Auto-generated constructor stub
	}

	public String getIdCardNo() {
		return idCardNo;
	}

	public void setIdCardNo(String idCardNo) {
		this.idCardNo = idCardNo;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
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

	public int getEvent() {
		return event;
	}

	public void setEvent(int event) {
		this.event = event;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getIdPicImageBase64() {
		return idPicImageBase64;
	}

	public void setIdPicImageBase64(String idPicImageBase64) {
		this.idPicImageBase64 = idPicImageBase64;
	}

	public String getFaceImageBase64() {
		return faceImageBase64;
	}

	public void setFaceImageBase64(String faceImageBase64) {
		this.faceImageBase64 = faceImageBase64;
	}

	public String getFrameImageBase64() {
		return frameImageBase64;
	}

	public void setFrameImageBase64(String frameImageBase64) {
		this.frameImageBase64 = frameImageBase64;
	}



	public float getSimilarity() {
		return similarity;
	}

	public void setSimilarity(float similarity) {
		this.similarity = similarity;
	}

	public int getIsVerifyPassed() {
		return isVerifyPassed;
	}

	public void setIsVerifyPassed(int isVerifyPassed) {
		this.isVerifyPassed = isVerifyPassed;
	}

	public String getJsonStr() {
		return jsonStr;
	}

	public void setJsonStr(String jsonStr) {
		this.jsonStr = jsonStr;
	}

}
