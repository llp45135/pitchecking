package com.rxtec.pitchecking;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;

public class Config {

	private float faceCheckThreshold = (float) 0.7;
	private float glassFaceCheckThreshold = (float) 0.68;
	private long faceCheckDelayTime = 5000;
	private int fastFaceDetect = 1;
	private int faceCheckingInteval = 100;
	private int defaultFaceCheckScreenDeley=1000;
	private String imagesLogDir;
	private int roateCapture=0;
	private int detectededFaceQueueLen = 5;
	private int detectededFaceQueueThreshold = 2;

	public int getDetectededFaceQueueThreshold() {
		return detectededFaceQueueThreshold;
	}




	public void setDetectededFaceQueueThreshold(int detectededFaceQueueThreshold) {
		this.detectededFaceQueueThreshold = detectededFaceQueueThreshold;
	}




	public int getDetectededFaceQueueLen() {
		return detectededFaceQueueLen;
	}




	public void setDetectededFaceQueueLen(int detectededFaceQueueLen) {
		this.detectededFaceQueueLen = detectededFaceQueueLen;
	}




	public int getRoateCapture() {
		return roateCapture;
	}




	public void setRoateCapture(int roateCapture) {
		this.roateCapture = roateCapture;
	}




	public String getImagesLogDir() {
		return imagesLogDir;
	}




	public void setImagesLogDir(String imagesLogDir) {
		this.imagesLogDir = imagesLogDir;
	}




	public long getFaceCheckDelayTime() {
		return faceCheckDelayTime;
	}




	public void setFaceCheckDelayTime(long faceCheckDelayTime) {
		this.faceCheckDelayTime = faceCheckDelayTime;
	}





	public int getDefaultFaceCheckScreenDeley() {
		return defaultFaceCheckScreenDeley;
	}




	public void setDefaultFaceCheckScreenDeley(int defaultFaceCheckScreenDeley) {
		this.defaultFaceCheckScreenDeley = defaultFaceCheckScreenDeley;
	}




	public int getFaceCheckingInteval() {
		return faceCheckingInteval;
	}




	public void setFaceCheckingInteval(int faceCheckingInteval) {
		this.faceCheckingInteval = faceCheckingInteval;
	}




	public int getFastFaceDetect() {
		return fastFaceDetect;
	}




	public void setFastFaceDetect(int fastFaceDetect) {
		this.fastFaceDetect = fastFaceDetect;
	}




	public float getFaceCheckThreshold() {
		return faceCheckThreshold;
	}




	public void setFaceCheckThreshold(float faceCheckThreshold) {
		this.faceCheckThreshold = faceCheckThreshold;
	}




	public float getGlassFaceCheckThreshold() {
		return glassFaceCheckThreshold;
	}




	public void setGlassFaceCheckThreshold(float glassFaceCheckThreshold) {
		this.glassFaceCheckThreshold = glassFaceCheckThreshold;
	}




	
	





	private static Config _instance = new Config();
	
	private Config(){
		Properties p = new Properties();
        try
        {
        	ClassLoader cl = this.getClass().getClassLoader(); 
        	InputStream is = cl.getResourceAsStream("conf.properties");
        	p.load(is);
        	this.faceCheckThreshold = Float.valueOf(p.getProperty("FaceCheckThreshold", "0.7"));
        	this.glassFaceCheckThreshold = Float.valueOf(p.getProperty("GlassFaceCheckThreshold", "0.68"));
        	this.faceCheckDelayTime = Integer.valueOf(p.getProperty("FaceCheckDelayTime", "5000"));
        	this.fastFaceDetect = Integer.valueOf(p.getProperty("FastFaceDetect", "1"));
        	this.faceCheckingInteval = Integer.valueOf(p.getProperty("faceCheckingInteval", "100"));
        	this.defaultFaceCheckScreenDeley = Integer.valueOf(p.getProperty("DefaultFaceCheckScreenDeley", "1000"));
        	this.imagesLogDir = p.getProperty("ImagesLogDir", "C:/pitchecking/images");
        	this.roateCapture = Integer.valueOf(p.getProperty("RoateCapture", "0"));
        	this.detectededFaceQueueLen = Integer.valueOf(p.getProperty("DetectededFaceQueueLen", "5"));
        	this.detectededFaceQueueThreshold = Integer.valueOf(p.getProperty("DetectededFaceQueueThreshold", "2"));
            is.close(); //关闭流
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
	}
	
	public static synchronized Config getInstance(){
		if(_instance == null) _instance = new Config();
		return _instance;
	}

	
	public static void main(String[] args) {
		Config.getInstance();

	}

}
