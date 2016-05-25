package com.rxtec.pitchecking.picheckingservice;

import java.util.Calendar;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.tools.ant.types.resources.selectors.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;



public class FaceCheckingService {
	private Logger log = LoggerFactory.getLogger("FaceCheckingService");

	private static FaceCheckingService _instance = new FaceCheckingService();

	//已经检测人脸质量，待验证的队列
	private LinkedBlockingQueue<PICData> detectedFaceDataQueue ;

	//比对验证通过的队列
	private LinkedBlockingQueue<PICData> passFaceDataQueue;


	public LinkedBlockingQueue<PICData> getPassFaceDataQueue() {
		return passFaceDataQueue;
	}
	private FaceCheckingService(){
		detectedFaceDataQueue = new LinkedBlockingQueue<PICData>(Config.getInstance().getDetectededFaceQueueLen());
		passFaceDataQueue =new LinkedBlockingQueue<PICData>(Config.getInstance().getDetectededFaceQueueLen());
	}
	public static synchronized FaceCheckingService getInstance(){
		if(_instance == null) _instance = new FaceCheckingService();
		return _instance;
	}
	
	public PICData pollPassFaceData() throws InterruptedException{
		PICData fd = passFaceDataQueue.poll(Config.getInstance().getFaceCheckDelayTime(),TimeUnit.MILLISECONDS );
		return fd;
	}
	
	
	public PICData takeDetectedFaceData() throws InterruptedException{
		log.debug("takeFaceDataForChecking takeDetectedFaceData size:"+detectedFaceDataQueue.size());	
		return detectedFaceDataQueue.take();
	}
	
	
	public void offerDetectedFaceData(PICData faceData){
		if(!detectedFaceDataQueue.offer(faceData)){
			detectedFaceDataQueue.poll();
			detectedFaceDataQueue.offer(faceData);
		}
	}
	

	
	public void resetFaceDataQueue(){
		detectedFaceDataQueue.clear();
		passFaceDataQueue.clear();
	}
	
	public void offerPassFaceData(PICData fd){
		passFaceDataQueue.offer(fd);
	}
	ExecutorService executor = Executors.newCachedThreadPool();
	
	public void beginFaceCheckerTask(){
		FaceCheckerTask checker = new FaceCheckerTask();
		ExecutorService executer = Executors.newCachedThreadPool();
		executer.execute(checker);
		
	}

	public void beginFaceQualityDetecterTask(){
//		FaceQualityDetecter detecter = new FaceQualityDetecter();
//		executor.execute(detecter);
		
	}

	

}
