package com.rxtec.pitchecking.picheckingservice.realsense;

public class RealsenseDeviceProperties {

	
	public RealsenseDeviceProperties() {
		
	}
	
	private boolean ColorAutoExposure = true;
	private boolean ColorAutoWhiteBalance = true;
	private boolean ColorBackLightCompensation = true;
	private int ColorBrightness = 9;  //0
	private int ColorExposure = -3;   //-6
	
	public boolean isColorAutoExposure() {
		return ColorAutoExposure;
	}
	public void setColorAutoExposure(boolean colorAutoExposure) {
		ColorAutoExposure = colorAutoExposure;
	}
	public boolean isColorAutoWhiteBalance() {
		return ColorAutoWhiteBalance;
	}
	public void setColorAutoWhiteBalance(boolean colorAutoWhiteBalance) {
		ColorAutoWhiteBalance = colorAutoWhiteBalance;
	}
	public boolean isColorBackLightCompensation() {
		return ColorBackLightCompensation;
	}
	public void setColorBackLightCompensation(boolean colorBackLightCompensation) {
		ColorBackLightCompensation = colorBackLightCompensation;
	}
	public int getColorBrightness() {
		return ColorBrightness;
	}
	public void setColorBrightness(int colorBrightness) {
		ColorBrightness = colorBrightness;
	}
	public int getColorExposure() {
		return ColorExposure;
	}
	public void setColorExposure(int colorExposure) {
		ColorExposure = colorExposure;
	}




}
