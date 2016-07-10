package com.rxtec.pitchecking.picheckingservice;

import java.io.Serializable;

import com.rxtec.pitchecking.utils.ImageToolkit;

public class FaceVerifyData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1318465620096874506L;

	private byte[] faceImg;
	private byte[] idCardImg;
	private byte[] frameImg;

	private String faceID;
	private float verifyResult = 0f;

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

	public String getFaceID() {
		return faceID;
	}

	public void setFaceID(String faceID) {
		this.faceID = faceID;
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

	public FaceVerifyData(PITData pd) {
		if (pd != null) {
			this.idCardImg = pd.getIdCard().getImageBytes();
			this.faceImg = ImageToolkit.getImageBytes(pd.getFaceImage(), "JPEG");
			this.faceID = pd.getIdCard().getIdNo();
			this.frameImg = ImageToolkit.getImageBytes(pd.getFrame(), "JPEG");
		}
	}

	public FaceVerifyData() {

	}

	@Override
	public String toString() {
		return faceID + "   idImg=" + idCardImg.length + " faceImg=" + faceImg.length + " verifyResult=" + verifyResult;
	}
}
