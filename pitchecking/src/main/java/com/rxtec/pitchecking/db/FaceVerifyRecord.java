package com.rxtec.pitchecking.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Document
public class FaceVerifyRecord {
	private String faceId;
	private double verifyResult = 0f;
	private float faceDistance;
	private int useTime;
	private byte[] faceImg;
	private byte[] frameImg;
	private byte[] idCardImg;
	
	public String getFaceID() {
		return faceId;
	}
	public void setFaceId(String id) {
		this.faceId = id;
	}

	public double getVerifyResult() {
		return verifyResult;
	}
	public void setVerifyResult(double verifyResult) {
		this.verifyResult = verifyResult;
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
	public byte[] getFaceImg() {
		return faceImg;
	}
	public void setFaceImg(byte[] faceImg) {
		this.faceImg = faceImg;
	}
	public byte[] getFrameImg() {
		return frameImg;
	}
	public void setFrameImg(byte[] frameImg) {
		this.frameImg = frameImg;
	}
	public byte[] getIdCardImg() {
		return idCardImg;
	}
	public void setIdCardImg(byte[] idCardImg) {
		this.idCardImg = idCardImg;
	}


}
