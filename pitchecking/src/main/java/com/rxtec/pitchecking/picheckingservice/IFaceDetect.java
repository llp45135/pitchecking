package com.rxtec.pitchecking.picheckingservice;

public interface IFaceDetect {
	public FaceDetectedResult detectFaceImage(byte[] imgBytes);
}
