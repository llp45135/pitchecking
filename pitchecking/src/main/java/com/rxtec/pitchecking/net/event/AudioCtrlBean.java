package com.rxtec.pitchecking.net.event;

public class AudioCtrlBean implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int Event = -1;
	private String Target;
	private String EventSource;

	public AudioCtrlBean() {
		// TODO Auto-generated constructor stub
	}

	public int getEvent() {
		return Event;
	}

	public void setEvent(int event) {
		Event = event;
	}

	public String getTarget() {
		return Target;
	}

	public void setTarget(String target) {
		Target = target;
	}

	public String getEventSource() {
		return EventSource;
	}

	public void setEventSource(String eventSource) {
		EventSource = eventSource;
	}

}
