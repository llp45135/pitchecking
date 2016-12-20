package com.rxtec.pitchecking.device.easen;

public class EasenVerifyResult {
	private float[] matchingScore = new float[1];
	private int[] yesNo = new int[1];
	private float[] matchingTime = new float[1];
	private int[] faceX = new int[1];
	private int[] faceY = new int[1];
	private int[] faceWidth = new int[1];
	private int[] faceHeight = new int[1];

	public EasenVerifyResult() {
		// TODO Auto-generated constructor stub
	}

	public float[] getMatchingScore() {
		return matchingScore;
	}

	public void setMatchingScore(float[] matchingScore) {
		this.matchingScore = matchingScore;
	}

	public int[] getYesNo() {
		return yesNo;
	}

	public void setYesNo(int[] yesNo) {
		this.yesNo = yesNo;
	}

	public float[] getMatchingTime() {
		return matchingTime;
	}

	public void setMatchingTime(float[] matchingTime) {
		this.matchingTime = matchingTime;
	}

	public int[] getFaceX() {
		return faceX;
	}

	public void setFaceX(int[] faceX) {
		this.faceX = faceX;
	}

	public int[] getFaceY() {
		return faceY;
	}

	public void setFaceY(int[] faceY) {
		this.faceY = faceY;
	}

	public int[] getFaceWidth() {
		return faceWidth;
	}

	public void setFaceWidth(int[] faceWidth) {
		this.faceWidth = faceWidth;
	}

	public int[] getFaceHeight() {
		return faceHeight;
	}

	public void setFaceHeight(int[] faceHeight) {
		this.faceHeight = faceHeight;
	}

}
