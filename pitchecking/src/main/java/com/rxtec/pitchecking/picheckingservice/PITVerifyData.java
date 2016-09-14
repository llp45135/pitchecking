package com.rxtec.pitchecking.picheckingservice;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.utils.ImageToolkit;

public class PITVerifyData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1318465620096874506L;

	private String eventName = "CAM_GetVerifyResult";

	private int eventDirection = 1;

	
	private byte[] faceImg;
	private byte[] idCardImg;
	private byte[] frameImg;

	private float verifyResult = 0f;

	private String idNo;
	private String personName;
	private int gender;
	private int age;
	private Ticket ticket;
	private float faceDistance;
	private int useTime;
	private String pitDate;
	private String pitStation;
	private String pitTime;
	
	private float facePosePitch;
	private float facePoseRoll;
	private float facePoseYaw;
	
	
	
	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public int getEventDirection() {
		return eventDirection;
	}

	public void setEventDirection(int eventDirection) {
		this.eventDirection = eventDirection;
	}


	public float getFacePosePitch() {
		return facePosePitch;
	}

	public void setFacePosePitch(float facePosePitch) {
		this.facePosePitch = facePosePitch;
	}

	public float getFacePoseRoll() {
		return facePoseRoll;
	}

	public void setFacePoseRoll(float facePoseRoll) {
		this.facePoseRoll = facePoseRoll;
	}

	public float getFacePoseYaw() {
		return facePoseYaw;
	}

	public void setFacePoseYaw(float facePoseYaw) {
		this.facePoseYaw = facePoseYaw;
	}


	public String getPitStation() {
		return pitStation;
	}

	public void setPitStation(String pitStation) {
		this.pitStation = pitStation;
	}

	public String getPitDate() {
		return pitDate;
	}

	public void setPitDate(String pitDate) {
		this.pitDate = pitDate;
	}

	public String getPitTime() {
		return pitTime;
	}

	public void setPitTime(String pitTime) {
		this.pitTime = pitTime;
	}


	public int getUseTime() {
		return useTime;
	}

	public void setUseTime(int useTime) {
		this.useTime = useTime;
	}

	public float getFaceDistance() {
		return faceDistance;
	}

	public void setFaceDistance(float faceDistance) {
		this.faceDistance = faceDistance;
	}

	public Ticket getTicket() {
		return ticket;
	}

	public void setTicket(Ticket ticket) {
		this.ticket = ticket;
	}

	public String getIdNo() {
		return idNo;
	}

	public void setIdNo(String idNo) {
		this.idNo = idNo;
	}

	public String getPersonName() {
		return personName;
	}

	public void setPersonName(String personName) {
		this.personName = personName;
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

	public byte[] getFrameImg() {
		return frameImg;
	}

	public void setFrameImg(byte[] frameImg) {
		this.frameImg = frameImg;
	}

	public float getVerifyResult() {
		return verifyResult;
	}

	public void setVerifyResult(float verifyResult) {
		this.verifyResult = verifyResult;
	}

	public byte[] getFaceImg() {
		return faceImg;
	}

	public void setFaceImg(byte[] faceImg) {
		this.faceImg = faceImg;
	}

	public byte[] getIdCardImg() {
		return idCardImg;
	}

	public void setIdCardImg(byte[] idCardImg) {
		this.idCardImg = idCardImg;
	}

	public PITVerifyData(PITData pd) {
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat sf2 = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		
		if (pd != null && pd.getIdCard() != null) {
			this.idCardImg = pd.getIdCard().getCardImageBytes();
			this.faceImg = ImageToolkit.getImageBytes(pd.getFaceImage(), "JPEG");
			this.idNo = pd.getIdCard().getIdNo();
			this.frameImg = ImageToolkit.getImageBytes(pd.getFrame(), "JPEG");
			this.personName = pd.getIdCard().getPersonName();
			this.age = pd.getIdCard().getAge();
			this.gender = pd.getIdCard().getGender();
			this.ticket = pd.getTicket();
			this.faceDistance = pd.getFaceDistance();
			Date now = new Date();
			this.pitDate = sf.format(now);
			this.pitTime = sf.format(now);
			this.pitStation = pd.getPitStation();
			this.facePosePitch = pd.getFacePosePitch();
			this.facePoseRoll = pd.getFacePoseRoll();
			this.facePoseYaw = pd.getFacePoseYaw();
		}
	}

	public PITVerifyData() {

	}

	@Override
	public String toString() {
		if (idCardImg != null && faceImg != null) {
			return "idNo=" + idNo + "   idImg length=" + idCardImg.length + " faceImg length=" + faceImg.length + " verifyResult="
					+ verifyResult;
		}else{
			return "idNo=" + idNo + " 图像数据缺失: idCardImg=" + idCardImg +" faceImg=" + faceImg;
		}
	}
}
