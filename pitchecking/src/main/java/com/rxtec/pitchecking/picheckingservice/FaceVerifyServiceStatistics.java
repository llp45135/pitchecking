package com.rxtec.pitchecking.picheckingservice;

import com.rxtec.pitchecking.Config;

public class FaceVerifyServiceStatistics {
	private int totalTimes;
	private int totalFailed;
	private int totalPass;
	private int totalDetectFaceFailed;
	private int averageUseTime;
	private int maxUseTime;
	private int minUseTime;
	long totalUseTime;
	double tPassedDistance;
	double tFailedDistance;
	
	private float passAverageDistance;
	private float failedAverageDistance;

	public float getPassAverageDistance() {
		return passAverageDistance;
	}
	public void setPassAverageDistance(float passAverageDistance) {
		this.passAverageDistance = passAverageDistance;
	}
	public float getFailedAverageDistance() {
		return failedAverageDistance;
	}
	public void setFailedAverageDistance(float failedAverageDistance) {
		this.failedAverageDistance = failedAverageDistance;
	}
	public int getTotalTimes() {
		return totalTimes;
	}
	public void setTotalTimes(int totalTimes) {
		this.totalTimes = totalTimes;
	}
	public int getTotalFailed() {
		return totalFailed;
	}
	public void setTotalFailed(int totalFailed) {
		this.totalFailed = totalFailed;
	}
	public int getTotalPass() {
		return totalPass;
	}
	public void setTotalPass(int totalPass) {
		this.totalPass = totalPass;
	}
	public int getTotalDetectFaceFailed() {
		return totalDetectFaceFailed;
	}
	public void setTotalDetectFaceFailed(int totalDetectFaceFailed) {
		this.totalDetectFaceFailed = totalDetectFaceFailed;
	}
	public int getAverageUseTime() {
		return averageUseTime;
	}
	public void setAverageUseTime(int averageUseTime) {
		this.averageUseTime = averageUseTime;
	}
	public int getMaxUseTime() {
		return maxUseTime;
	}
	public void setMaxUseTime(int maxUseTime) {
		this.maxUseTime = maxUseTime;
	}
	public int getMinUseTime() {
		return minUseTime;
	}
	public void setMinUseTime(int minUseTime) {
		this.minUseTime = minUseTime;
	}
	
	public void update(float result, int useTime,float distance){
		if(result<0){
			totalDetectFaceFailed++;
		}else if(Config.getInstance().getFaceCheckThreshold() < result){
			this.totalTimes++;
			totalFailed++;
			if(useTime > maxUseTime) maxUseTime = useTime;
			if(useTime < minUseTime) minUseTime = useTime;
			totalUseTime+=useTime;
			tFailedDistance+=distance;
		}else{
			this.totalTimes++;
			totalPass++;
			if(useTime > maxUseTime) maxUseTime = useTime;
			if(useTime < minUseTime) minUseTime = useTime;
			totalUseTime+=useTime;
			tPassedDistance+=distance;
			
		}
		averageUseTime = (int) (totalUseTime / totalTimes);
		passAverageDistance = (float) (tPassedDistance / totalPass);
		failedAverageDistance = (float) (tFailedDistance / totalFailed);
	}
	
	private static FaceVerifyServiceStatistics _instance = new FaceVerifyServiceStatistics();
	public static synchronized FaceVerifyServiceStatistics getInstance() {
		if (_instance == null)
			_instance = new FaceVerifyServiceStatistics();
		return _instance;
	}

}
