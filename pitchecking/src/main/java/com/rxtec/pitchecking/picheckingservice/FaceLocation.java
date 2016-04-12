package com.rxtec.pitchecking.picheckingservice;

public class FaceLocation {
	private int id;
	private int x;
	private int y;
	private int width;
	private int height;
	private float confidence;
	private int xFirstEye;
	private int yFirstEye;
	private float firstConfidence;
	private int xSecondEye;
	private int ySecondEye;
	private float secondConfidence;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public float getConfidence() {
		return confidence;
	}
	public void setConfidence(float confidence) {
		this.confidence = confidence;
	}
	public int getxFirstEye() {
		return xFirstEye;
	}
	public void setxFirstEye(int xFirstEye) {
		this.xFirstEye = xFirstEye;
	}
	public int getyFirstEye() {
		return yFirstEye;
	}
	public void setyFirstEye(int yFirstEye) {
		this.yFirstEye = yFirstEye;
	}
	public float getFirstConfidence() {
		return firstConfidence;
	}
	public void setFirstConfidence(float firstConfidence) {
		this.firstConfidence = firstConfidence;
	}
	public int getxSecondEye() {
		return xSecondEye;
	}
	public void setxSecondEye(int xSecondEye) {
		this.xSecondEye = xSecondEye;
	}
	public int getySecondEye() {
		return ySecondEye;
	}
	public void setySecondEye(int ySecondEye) {
		this.ySecondEye = ySecondEye;
	}
	public float getSecondConfidence() {
		return secondConfidence;
	}
	public void setSecondConfidence(float secondConfidence) {
		this.secondConfidence = secondConfidence;
	}
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("Detected face ");
		sb.append("X:");
		sb.append(this.x);
		sb.append(" Y:");
		sb.append(this.y);
		sb.append(" Width:");
		sb.append(this.width);
		sb.append(" Height:");
		sb.append(this.height);
		return sb.toString();
	}
	
}
