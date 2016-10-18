package com.rxtec.pitchecking.net.event;

public class GatCrtlBean {
	private int event = -1;
	private String target;
	private String eventsource;

	public String getEventsource() {
		return eventsource;
	}

	public void setEventsource(String eventsource) {
		this.eventsource = eventsource;
	}

	public int getEvent() {
		return event;
	}

	public void setEvent(int event) {
		this.event = event;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
}
