package com.rxtec.pitchecking.db;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
@Document
public class PitRecord {
	@Id private String id;
	private String idNo;
	private String personName;
	private int gender;
	private int age;
	private byte[] idCardImg;
	private double verifyResult = 0f;
	private String pitDate;
	private String pitStation;
	private String pitTime;
	private List<FaceVerifyRecord> faceVerifyRecords = new ArrayList<FaceVerifyRecord>();
	private Ticket ticket;
	private String gateNo;
	public String getGateNo() {
		return gateNo;
	}
	public void setGateNo(String gateNo) {
		this.gateNo = gateNo;
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
	public byte[] getIdCardImg() {
		return idCardImg;
	}
	public void setIdCardImg(byte[] idCardImg) {
		this.idCardImg = idCardImg;
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
	public List<FaceVerifyRecord> getFaceVerifyRecords() {
		return faceVerifyRecords;
	}
	public void setFaceVerifyRecords(List<FaceVerifyRecord> faceVerifyRecords) {
		this.faceVerifyRecords = faceVerifyRecords;
	}
	public Ticket getTicket() {
		return ticket;
	}
	public void setTicket(Ticket ticket) {
		this.ticket = ticket;
	}
	
	public PitRecord(PITVerifyData pd){
		this.age = pd.getAge();
		this.gender = pd.getGender();
		this.idCardImg = pd.getIdCardImg();
		this.idNo = pd.getIdNo();
		this.personName = pd.getPersonName();
		this.pitDate = pd.getPitDate();
		this.pitTime = pd.getPitTime();
		this.pitStation = pd.getPitStation();
		this.ticket = pd.getTicket();
		this.verifyResult = pd.getVerifyResult();
		FaceVerifyRecord fvr = new FaceVerifyRecord();
		fvr.setFaceDistance(pd.getFaceDistance());
		fvr.setFaceId(Integer.toString(pd.getIdNo().hashCode()));
		fvr.setFaceImg(pd.getFaceImg());
		fvr.setFrameImg(pd.getFrameImg());
		fvr.setIdCardImg(pd.getIdCardImg());
		fvr.setVerifyResult(pd.getVerifyResult());
		fvr.setPitTime(pd.getPitTime());
		fvr.setFacePosePitch(pd.getFacePosePitch());
		fvr.setFacePoseRoll(pd.getFacePoseRoll());
		fvr.setFacePoseYaw(pd.getFacePoseYaw());
		fvr.setUseTime(pd.getUseTime());
		this.getFaceVerifyRecords().add(fvr);
	}
	public PitRecord(){
		
	}

	@Override
	public String toString(){
		return idNo +"	" +personName +"   " +pitTime;
	}
}
