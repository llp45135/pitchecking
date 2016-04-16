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

	//已经裁脸，待检测的队列
	private LinkedBlockingQueue<FaceData> inFaceDataQueue = new LinkedBlockingQueue<FaceData>(10);
	
	//已经检测人脸质量，待验证的队列
	private LinkedBlockingQueue<FaceData> checkedFaceDataQueue = new LinkedBlockingQueue<FaceData>(5);

	//比对验证通过的队列
	private LinkedBlockingQueue<FaceData> passFaceDataQueue =new LinkedBlockingQueue<FaceData>(5);

	public LinkedBlockingQueue<FaceData> getCheckedFaceDataQueue() {
		return checkedFaceDataQueue;
	}
	public void setCheckedFaceDataQueue(LinkedBlockingQueue<FaceData> checkedFaceDataQueue) {
		this.checkedFaceDataQueue = checkedFaceDataQueue;
	}

	public LinkedBlockingQueue<FaceData> getInFaceDataQueue() {
		return inFaceDataQueue;
	}
	public LinkedBlockingQueue<FaceData> getPassFaceDataQueue() {
		return passFaceDataQueue;
	}
	private FaceCheckingService(){
	}
	public static FaceCheckingService getInstance(){
		if(_instance == null) _instance = new FaceCheckingService();
		return _instance;
	}
	
	public FaceData pollPassFaceData() throws InterruptedException{
		FaceData fd = passFaceDataQueue.poll(Config.getInstance().getFaceCheckDelayTime(),TimeUnit.MILLISECONDS );
		return fd;
	}
	
	

	FaceData preFaceData = null;
	
	public void offerFaceDataForChecking(FaceData faceData){
//		log.debug("sendFaceDataForChecking inFaceDataQueue size:"+inFaceDataQueue.size());	

		if(preFaceData == null){
			preFaceData = faceData;
			inFaceDataQueue.offer(faceData);
		}else{
			long now = Calendar.getInstance().getTimeInMillis();
			long inteval = now - preFaceData.getCreateTime();
			

			if(inteval>Config.getInstance().getFaceCheckingInteval()) {
				inFaceDataQueue.offer(faceData);
//				log.debug("offerFaceDataForChecking inFaceDataQueue size:"+inFaceDataQueue.size());	
				preFaceData = faceData;
			}
		}
	}
	
	
	public FaceData pollCheckedFaceData(){
		//log.debug("takeFaceDataForChecking inFaceDataQueue size:"+inFaceDataQueue.size());	
		return checkedFaceDataQueue.poll();
	}
	
	
	public void offerCheckedFaceData(FaceData faceData){
		checkedFaceDataQueue.offer(faceData);
	}
	
	
	public FaceData pollFaceDataForChecking(){
		//log.debug("takeFaceDataForChecking inFaceDataQueue size:"+inFaceDataQueue.size());	
		return inFaceDataQueue.poll();
	}
	
	public void resetFaceDataForCheckingQueue(){
		inFaceDataQueue.clear();
		preFaceData = null;
	}
	
	public void offerPassFaceData(FaceData fd){
		float resultValue = fd.getFaceCheckResult();
		
		if(resultValue>=Config.getInstance().getFaceCheckThreshold()){
			passFaceDataQueue.offer(fd);
		}else{
			if(fd.getFaceDetectedData().isWearsglasses()){
				if(resultValue>=Config.getInstance().getGlassFaceCheckThreshold()){
					passFaceDataQueue.offer(fd);
				}
			}
		}
	}
	ExecutorService executor = Executors.newCachedThreadPool();
	
	public void beginFaceCheckerTask(){
		FaceCheckerTask checker = new FaceCheckerTask();
		executor.execute(checker);
		
	}

	public void beginFaceQualityDetecterTask(){
		FaceQualityDetecter detecter = new FaceQualityDetecter();
		executor.execute(detecter);
		
	}

	

}
