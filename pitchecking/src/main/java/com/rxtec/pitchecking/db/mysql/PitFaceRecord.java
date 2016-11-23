package com.rxtec.pitchecking.db.mysql;

import java.util.ArrayList;
import java.util.List;

import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.db.FaceVerifyRecord;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;

public class PitFaceRecord {
	private String id;
	private String idNo;
	private String personName;
	private int gender;
	private int age;
	private double verifyResult = 0f;
	private String pitDate;
	private String pitStation;
	private String pitTime;
	private Ticket ticket;
	private String gateNo;
	private String faceId;
	private float faceDistance;
	private int useTime;
	private String faceImgPath;
	private String frameImgPath;
	private String idCardImgPath;
	private float facePosePitch;
	private float facePoseRoll;
	private float facePoseYaw;

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

	public PitFaceRecord(PITVerifyData pd) {
		this.age = pd.getAge();
		this.gender = pd.getGender();
		this.idNo = pd.getIdNo();
		this.personName = pd.getPersonName();
		this.pitDate = pd.getPitDate();
		this.pitTime = pd.getPitTime();
		this.pitStation = pd.getPitStation();
		this.ticket = pd.getTicket();
		this.verifyResult = pd.getVerifyResult();
		this.faceDistance = pd.getFaceDistance();
		this.faceId = Integer.toString(pd.getIdNo().hashCode());
		if (pd.getIdCardImg() != null)
			this.idCardImgPath = new String(pd.getIdCardImg());
		if (pd.getFaceImg() != null)
			this.faceImgPath = new String(pd.getFaceImg());
		if (pd.getFrameImg() != null)
			this.frameImgPath = new String(pd.getFrameImg());
		this.facePosePitch = pd.getFacePosePitch();
		this.facePoseRoll = pd.getFacePoseRoll();
		this.facePoseYaw = pd.getFacePoseYaw();
		this.useTime = pd.getUseTime();
		this.gateNo = pd.getGateIp();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public double getVerifyResult() {
		return verifyResult;
	}

	public void setVerifyResult(double verifyResult) {
		this.verifyResult = verifyResult;
	}

	public String getPitDate() {
		return pitDate;
	}

	public void setPitDate(String pitDate) {
		this.pitDate = pitDate;
	}

	public String getPitStation() {
		return pitStation;
	}

	public void setPitStation(String pitStation) {
		this.pitStation = pitStation;
	}

	public String getPitTime() {
		return pitTime;
	}

	public void setPitTime(String pitTime) {
		this.pitTime = pitTime;
	}

	public Ticket getTicket() {
		return ticket;
	}

	public void setTicket(Ticket ticket) {
		this.ticket = ticket;
	}

	public String getGateNo() {
		return gateNo;
	}

	public void setGateNo(String gateNo) {
		this.gateNo = gateNo;
	}

	public String getFaceId() {
		return faceId;
	}

	public void setFaceId(String faceId) {
		this.faceId = faceId;
	}

	public float getFaceDistance() {
		return faceDistance;
	}

	public void setFaceDistance(float faceDistance) {
		this.faceDistance = faceDistance;
	}

	public int getUseTime() {
		return useTime;
	}

	public void setUseTime(int useTime) {
		this.useTime = useTime;
	}

	public String getFaceImgPath() {
		return faceImgPath;
	}

	public void setFaceImgPath(String faceImgPath) {
		this.faceImgPath = faceImgPath;
	}

	public String getFrameImgPath() {
		return frameImgPath;
	}

	public void setFrameImgPath(String frameImgPath) {
		this.frameImgPath = frameImgPath;
	}

	public String getIdCardImgPath() {
		return idCardImgPath;
	}

	public void setIdCardImgPath(String idCardImgPath) {
		this.idCardImgPath = idCardImgPath;
	}

}
