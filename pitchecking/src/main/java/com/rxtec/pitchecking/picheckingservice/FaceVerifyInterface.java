package com.rxtec.pitchecking.picheckingservice;

import java.awt.image.BufferedImage;


public interface FaceVerifyInterface {
	public float verify(byte[] faceImgBytes, byte[] idCardBytes);
	public int getInitStatus();
	public int setIDCardPhoto(BufferedImage idCardImg);
	public float verify(byte[] faceImgBytes);
	
}
