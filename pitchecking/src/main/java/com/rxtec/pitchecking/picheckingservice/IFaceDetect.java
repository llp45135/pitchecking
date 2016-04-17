package com.rxtec.pitchecking.picheckingservice;

public interface IFaceDetect {
	public void detectFaceImageQuality(FaceDetectedResult fd);
	public void detectFaceLocation(FaceDetectedResult fd);
	public void detectFaceImage(FaceDetectedResult fd);
	
}
