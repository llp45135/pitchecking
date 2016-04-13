package com.rxtec.pitchecking;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;

public class Config {

	private float faceCheckThreshold = (float) 0.7;
	private float glassFaceCheckThreshold = (float) 0.68;
	private long getFaceCheckDelayTime = 5000;

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




	
	
	public long getGetFaceCheckDelayTime() {
		return getFaceCheckDelayTime;
	}




	public void setGetFaceCheckDelayTime(long getFaceCheckDelayTime) {
		this.getFaceCheckDelayTime = getFaceCheckDelayTime;
	}




	private static Config _instance = new Config();
	
	private Config(){
		Properties properties = new Properties();
        try
        {
        	ClassLoader cl = this.getClass().getClassLoader(); 
        	InputStream is = cl.getResourceAsStream("conf.properties");
        	properties.load(is);
            is.close(); //关闭流
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
	}
	
	public static Config getInstance(){
		if(_instance == null) _instance = new Config();
		return _instance;
	}

	
	public static void main(String[] args) {
		Config.getInstance();

	}

}
