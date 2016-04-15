package com.rxtec.pitchecking.picheckingservice;

import java.util.Calendar;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.tools.ant.types.resources.selectors.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;



public class FaceCheckingService {
	private Logger log = LoggerFactory.getLogger("FaceCheckingService");

	private static FaceCheckingService _instance = new FaceCheckingService();

	private LinkedBlockingQueue<FaceData> inFaceDataQueue = new LinkedBlockingQueue<FaceData>(10);

	private LinkedBlockingQueue<FaceData> checkedFaceDataQueue =new LinkedBlockingQueue<FaceData>(10);
	
	
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
	
	public FaceData pollCheckedFaceData() throws InterruptedException{
		FaceData fd = checkedFaceDataQueue.poll(Config.getInstance().getFaceCheckDelayTime(),TimeUnit.MILLISECONDS );
		return fd;
	}
	
	

	FaceData preFaceData = null;
	
	public void sendFaceDataForChecking(FaceData faceData){
//		log.debug("sendFaceDataForChecking inFaceDataQueue size:"+inFaceDataQueue.size());	

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
		

		if(inteval>Config.getInstance().getFaceCheckingInteval()) {
			inFaceDataQueue.offer(newFD);
//			log.debug("offerFaceDataForChecking inFaceDataQueue size:"+inFaceDataQueue.size());	
			preFaceData = newFD;
		}

	}
	
	public FaceData pollFaceDataForChecking(){
		//log.debug("takeFaceDataForChecking inFaceDataQueue size:"+inFaceDataQueue.size());	
		return inFaceDataQueue.poll();
	}
	
	public void resetFaceDataForCheckingQueue(){
		inFaceDataQueue.clear();
		preFaceData = null;
	}
	
	public void offerCheckedFaceData(FaceData fd){
		float resultValue = fd.getFaceCheckResult();
		
		if(resultValue>=Config.getInstance().getFaceCheckThreshold()){
			checkedFaceDataQueue.offer(fd);
		}else{
			if(fd.getFaceDetectedData().isWearsglasses()){
				if(resultValue>=Config.getInstance().getGlassFaceCheckThreshold()){
					checkedFaceDataQueue.offer(fd);
				}
			}
		}
	}
	
	public void beginFaceCheckerTask(){
		ExecutorService executor = Executors.newCachedThreadPool();
		FaceCheckerTask checker = new FaceCheckerTask();
		executor.execute(checker);
		
	}


	

}
