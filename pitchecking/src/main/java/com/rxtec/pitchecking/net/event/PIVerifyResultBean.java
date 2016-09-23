package com.rxtec.pitchecking.net.event;

public class PIVerifyResultBean {
	private String eventName = "CAM_GetPhotoInfo";
	private int eventDirection = 2;
	private int result;
	private String uuid;
	private int iDelay;
	private int photoLen1;
	private int photoLen2;
	private int photoLen3;
	private byte[] photo1;
	private byte[] photo2;
	private byte[] photo3;
	private int verifyStatus = 0;		//比对结果状态，0 代表比对成功，1代表比对未通过（到达超时时间！）
	
	public int getiDelay() {
		return iDelay;
	}
	public void setiDelay(int iDelay) {
		this.iDelay = iDelay;
	}
	public int getVerifyStatus() {
		return verifyStatus;
	}
	public void setVerifyStatus(int verifyStatus) {
		this.verifyStatus = verifyStatus;
	}
	public int getPhotoLen3() {
		return photoLen3;
	}
	public void setPhotoLen3(int photoLen3) {
		this.photoLen3 = photoLen3;
	}
	public byte[] getPhoto3() {
		return photo3;
	}
	public void setPhoto3(byte[] photo3) {
		this.photo3 = photo3;
		if(photo3 != null)
			this.photoLen3 = photo3.length;
		
	}
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
		if(photo1 != null)
			this.photoLen1 = photo1.length;

	}
	public byte[] getPhoto2() {
		return photo2;
	}
	public void setPhoto2(byte[] photo2) {
		this.photo2 = photo2;
		if(photo2 != null)
			this.photoLen2 = photo2.length;

	}

	
}
