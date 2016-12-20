package com.rxtec.pitchecking.picheckingservice.realsense;

/**
 * 设置摄像头参数
 * SetColorExposure 设置曝光值 范围 -8,0 step 1.0 ,系统缺省值 -6 。值越小曝光值越小 。强背光的时候曝光值放大，正面直射的时候曝光值减少  
 * SetColorBrightness 设置亮度 范围 -64，64 step 1.0 ,系统缺省值 0。 值越小越暗。
 * 
 * @param RealsenseDeviceProperties 设备参数
 */
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
