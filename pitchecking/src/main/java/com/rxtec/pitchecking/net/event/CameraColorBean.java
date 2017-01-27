package com.rxtec.pitchecking.net.event;

public class CameraColorBean {
	private String event = "123321";
	private String target;
	private String eventsource;
	private boolean colorautoexposure;
	private boolean colorautowhitebalance;
	private boolean colorbacklightcompensation;
	private float hdr = 0f;
	private int colorbrightness = 0;
	private int colorexposure = -6;
	private int contrast = 50 ;
	private int gamma = 300;
	private int gain = 64;
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getEventsource() {
		return eventsource;
	}
	public void setEventsource(String eventsource) {
		this.eventsource = eventsource;
	}
	public boolean getColorautoexposure() {
		return colorautoexposure;
	}
	public void setColorautoexposure(boolean colorautoexposure) {
		this.colorautoexposure = colorautoexposure;
	}
	public boolean getColorautowhitebalance() {
		return colorautowhitebalance;
	}
	public void setColorautowhitebalance(boolean colorautowhitebalance) {
		this.colorautowhitebalance = colorautowhitebalance;
	}
	public boolean getColorbacklightcompensation() {
		return colorbacklightcompensation;
	}
	public void setColorbacklightcompensation(boolean colorbacklightcompensation) {
		this.colorbacklightcompensation = colorbacklightcompensation;
	}
	public int getColorbrightness() {
		return colorbrightness;
	}
	public void setColorbrightness(int colorbrightness) {
		this.colorbrightness = colorbrightness;
	}
	public int getColorexposure() {
		return colorexposure;
	}
	public void setColorexposure(int colorexposure) {
		this.colorexposure = colorexposure;
	}

	public int getContrast() {
		return contrast;
	}
	public void setContrast(int contrast) {
		this.contrast = contrast;
	}

	public float getHdr() {
		return hdr;
	}
	public void setHdr(float hdr) {
		this.hdr = hdr;
	}
	public int getGamma() {
		return gamma;
	}
	public void setGamma(int gamma) {
		this.gamma = gamma;
	}
	public int getGain() {
		return gain;
	}
	public void setGain(int gain) {
		this.gain = gain;
	}
	

	
}
