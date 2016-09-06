package com.rxtec.pitchecking.net.event;

public class PIVerifyResultBean {
	private String eventName = "CAM_GetPhotoInfo";
	private int eventDirection = 2;
	private int result;
	private String uuid;
	private int photoLen1;
	private int photoLen2;
	private byte[] photo1;
	private byte[] photo2;
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
	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public int getPhotoLen1() {
		return photoLen1;
	}
	public void setPhotoLen1(int photoLen1) {
		this.photoLen1 = photoLen1;
	}
	public int getPhotoLen2() {
		return photoLen2;
	}
	public void setPhotoLen2(int photoLen2) {
		this.photoLen2 = photoLen2;
	}
	public byte[] getPhoto1() {
		return photo1;
	}
	public void setPhoto1(byte[] photo1) {
		this.photo1 = photo1;
	}
	public byte[] getPhoto2() {
		return photo2;
	}
	public void setPhoto2(byte[] photo2) {
		this.photo2 = photo2;
	}

	
}
