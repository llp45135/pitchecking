package com.rxtec.pitchecking.mbean;

public interface FaceVerifyServiceManagerMBean {
	public int getTotalPassed();
	public int getTotalFailed();
	public int getTotalDetectFaceFailed();
	public int getAverageUseTime();
	public int getMaxUseTime();
	public int getMinUseTime();
	public float getFailedAverageDistance();
	public float getPassedAverageDistance();

}
