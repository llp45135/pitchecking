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
	private float sr300_hdr = 1.0f;
	private int ColorBrightness = 0;  //-64 ~ 64
	private int ColorExposure = -6;   //曝光值  -8 ~ 0
	private int IVCAM_Option = 5;		//平滑系数，5代表中等距离 6,7代表远距离
	private int contrast = 50;		//对比度0-100
	private int gamma = 300;			//灰度 1-500
	private int focalLength = 100*10;	//对焦距离
	private int gain = 64;				//图像增益
	private int hue = 0;				//色相 -180 ~ 180
	private int saturation = 64;		//色彩饱和度 0-100
	private int sharpness = 50;			//清晰度 0-100
	private int whitebalance = 4600;		//白平衡 2800-5600
	
	
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
	public int getIVCAM_Option() {
		return IVCAM_Option;
	}
	public void setIVCAM_Option(int iVCAM_Option) {
		IVCAM_Option = iVCAM_Option;
	}
	public float getSR300_HDR() {
		return sr300_hdr;
	}
	public void setSR300_HDR(float sR300_HDR) {
		sr300_hdr = sR300_HDR;
	}
	public int getContrast() {
		return contrast;
	}
	public void setContrast(int contrast) {
		this.contrast = contrast;
	}
	public int getGamma() {
		return gamma;
	}
	public void setGamma(int gamma) {
		this.gamma = gamma;
	}
	public int getFocalLength() {
		return focalLength;
	}
	public void setFocalLength(int focalLength) {
		this.focalLength = focalLength;
	}
	public int getGain() {
		return gain;
	}
	public void setGain(int gain) {
		this.gain = gain;
	}
	public int getHue() {
		return hue;
	}
	public void setHue(int hue) {
		this.hue = hue;
	}
	public int getSaturation() {
		return saturation;
	}
	public void setSaturation(int saturation) {
		this.saturation = saturation;
	}
	public int getSharpness() {
		return sharpness;
	}
	public void setSharpness(int sharpness) {
		this.sharpness = sharpness;
	}
	public int getWhitebalance() {
		return whitebalance;
	}
	public void setWhitebalance(int whitebalance) {
		this.whitebalance = whitebalance;
	}




}
