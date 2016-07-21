package com.rxtec.pitchecking.mbean;

import com.rxtec.pitchecking.picheckingservice.FaceVerifyServiceStatistics;

public class FaceVerifyServiceManager implements FaceVerifyServiceManagerMBean {

	@Override
	public int getTotalPassed() {
		return FaceVerifyServiceStatistics.getInstance().getTotalPass();
	}

	@Override
	public int getTotalFailed() {
		return FaceVerifyServiceStatistics.getInstance().getTotalFailed();
	}

	@Override
	public int getTotalDetectFaceFailed() {
		return FaceVerifyServiceStatistics.getInstance().getTotalDetectFaceFailed();
	}

	@Override
	public int getAverageUseTime() {
		return FaceVerifyServiceStatistics.getInstance().getAverageUseTime();
	}

	@Override
	public int getMaxUseTime() {
		return FaceVerifyServiceStatistics.getInstance().getMaxUseTime();
	}

	@Override
	public int getMinUseTime() {
		return FaceVerifyServiceStatistics.getInstance().getMinUseTime();
	}

	@Override
	public float getFailedAverageDistance() {
		return FaceVerifyServiceStatistics.getInstance().getFailedAverageDistance();
	}

	@Override
	public float getPassedAverageDistance() {
		return FaceVerifyServiceStatistics.getInstance().getPassAverageDistance();
	}



}
