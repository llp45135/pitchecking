package com.rxtec.pitchecking.net;

import java.io.Serializable;

public class PITInfoJson implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int idHashCode;
	private int gender;
	private static int Male =1;
	private static int Female =2;
	private int age;
	private String idPicImageBase64;
	private String frameImageBase64;
	private float similarity;
	
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
	}
}
