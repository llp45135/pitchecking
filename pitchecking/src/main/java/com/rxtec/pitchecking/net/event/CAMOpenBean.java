package com.rxtec.pitchecking.net.event;

public class CAMOpenBean {
	// String ss = "{\"eventDirection\" : 2,\"eventName\" :
	// \"CAM_Open\",\"threshold\" : 75,\"timeout\" : 3000}";
	private String eventName = "CAM_Open";
	private int eventDirection = 1;
	private int threshold;
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

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
}
