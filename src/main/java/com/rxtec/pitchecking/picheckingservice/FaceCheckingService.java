package com.rxtec.pitchecking.picheckingservice;

import java.util.Calendar;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.tools.ant.types.resources.selectors.Date;


public class FaceCheckingService {
	private static FaceCheckingService _instance = new FaceCheckingService();

	private LinkedBlockingQueue<FaceData> inFaceDataQueue = new LinkedBlockingQueue<FaceData>(10);

	private LinkedBlockingQueue<FaceData> checkedFaceDataQueue =new LinkedBlockingQueue<FaceData>(10);
	
	private LinkedBlockingQueue<FaceData> passedFaceDataQueue =new LinkedBlockingQueue<FaceData>(3);
	
	
	public LinkedBlockingQueue<FaceData> getInFaceDataQueue() {
		return inFaceDataQueue;
	}
	public LinkedBlockingQueue<FaceData> getCheckedFaceDataQueue() {
		return checkedFaceDataQueue;
	}
	private FaceCheckingService(){
	}
	public static FaceCheckingService getInstance(){
		if(_instance == null) _instance = new FaceCheckingService();
		return _instance;
	}
	
	public FaceData getCheckedFaceData() {
		return checkedFaceDataQueue.poll();
	}
	
	
	/*
	 * 视频流抽帧频次
	 * 缺省200ms送1帧
	 */
	private int checkTimeInterval = 200;
	
	public int getCheckTimeInterval() {
		return checkTimeInterval;
	}
	public void setCheckTimeInterval(int checkTimeInterval) {
		this.checkTimeInterval = checkTimeInterval;
	}

	FaceData preFaceData = null;
	
	public void sendFaceDataForChecking(FaceData faceData){
		if(preFaceData == null){
			preFaceData = faceData;
			inFaceDataQueue.offer(faceData);
		}else{
			offerFaceDataForChecking(faceData);
		}
	}
	
	
	private void offerFaceDataForChecking(FaceData newFD){
		long now = Calendar.getInstance().getTimeInMillis();
		long inteval = now - preFaceData.getCreateTime();
		System.out.println(inteval);
		if(inteval>200) {
			inFaceDataQueue.offer(newFD);
			//System.out.println("^^^^^^^^^^^^^^ inFaceDataQueue size = " + inFaceDataQueue.size());
			preFaceData = newFD;
		}

	}
	
	public FaceData takeFaceDataForChecking(){
		//System.out.println("%%%%%%%%%%%%%%%%%%% inFaceDataQueue size = " + inFaceDataQueue.size());
		return inFaceDataQueue.poll();
	}
	
	public void resetFaceDataForCheckingQueue(){
		inFaceDataQueue.clear();
		preFaceData = null;
	}
	
	public void offerCheckedFaceData(FaceData f){
		checkedFaceDataQueue.offer(f);
	}
	
	public void beginFaceCheckerTask(){
		ExecutorService executor = Executors.newCachedThreadPool();
		FaceCheckerTask checker = new FaceCheckerTask();
		executor.execute(checker);
		
	}

	public void putPassedFaceDataToBlockedQueue(FaceData d){
		passedFaceDataQueue.offer(d);
	}
	
	

}
