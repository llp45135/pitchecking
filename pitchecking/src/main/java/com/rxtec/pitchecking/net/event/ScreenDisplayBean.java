package com.rxtec.pitchecking.net.event;

public class ScreenDisplayBean {
	private String eventName = "CAM_GetPhotoInfo";
	private int eventDirection = 1;
	private String screenDisplay;
	private int timeout;
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
	public String getScreenDisplay() {
		return screenDisplay;
	}
	public void setScreenDisplay(String screenDisplay) {
		this.screenDisplay = screenDisplay;
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
}
