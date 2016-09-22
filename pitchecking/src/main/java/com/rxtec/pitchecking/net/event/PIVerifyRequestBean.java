package com.rxtec.pitchecking.net.event;

public class PIVerifyRequestBean {
	private String eventName = "CAM_GetPhotoInfo";
	private int eventDirection = 1;
	private String uuid;
	private int iDelay;

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

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getiDelay() {
		return iDelay;
	}

	public void setiDelay(int iDelay) {
		this.iDelay = iDelay;
	}
}
