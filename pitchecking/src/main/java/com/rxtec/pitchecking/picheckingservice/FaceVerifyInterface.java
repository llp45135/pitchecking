package com.rxtec.pitchecking.picheckingservice;

public interface FaceVerifyInterface {
	public float verify(byte[] faceImgBytes, byte[] idCardBytes);
}
