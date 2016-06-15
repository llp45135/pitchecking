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
import com.rxtec.pitchecking.IDCard;



public class FaceCheckingService implements Runnable {
	private Logger log = LoggerFactory.getLogger("FaceCheckingService");
	FaceVerifyJniEntry faceVerify = new FaceVerifyJniEntry();
	
	private boolean isCheck = true;


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
		passFaceDataQueue =new LinkedBlockingQueue<PICData>(1);
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
		return detectedFaceDataQueue.take();
	}
	
	public void offerPassFaceData(PICData fd){
		isCheck = passFaceDataQueue.offer(fd);
		log.debug("offerPassFaceData...... " + isCheck);
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
	
	ExecutorService executor = Executors.newCachedThreadPool();
	
	public void beginFaceCheckerTask(){
		ExecutorService executer = Executors.newCachedThreadPool();
		executer.execute(this);
		
	}

	public void beginFaceQualityDetecterTask(){
//		FaceQualityDetecter detecter = new FaceQualityDetecter();
//		executor.execute(detecter);
		
	}

	@Override
	public void run() {
		while (true) {
			if(!isCheck) continue;
			try {
				Thread.sleep(50);
				PICData fd = FaceCheckingService.getInstance().takeDetectedFaceData();
				
				if (fd != null) {
					IDCard idCard = fd.getIdCard();
					if (idCard == null)
						continue;
					long nowMils = Calendar.getInstance().getTimeInMillis();
					float resultValue = 0;
					byte[] extractFaceImageBytes = fd.getExtractFaceImageBytes();
					if (extractFaceImageBytes == null)
						continue;
					resultValue = faceVerify.verify(extractFaceImageBytes, fd.getIdCard().getImageBytes());
					fd.setFaceCheckResult(resultValue);
					long usingTime = Calendar.getInstance().getTimeInMillis() - nowMils;
					if (resultValue >= Config.getInstance().getFaceCheckThreshold()) {
						offerPassFaceData(fd);
					} else {
						if (fd.getFaceDetectedResult() != null) {
							if (fd.getFaceDetectedResult().isWearsglasses()) {
								if (resultValue >= Config.getInstance().getGlassFaceCheckThreshold()) {
									offerPassFaceData(fd);
								}
							}
						}
					}
					fd.saveFaceDataToDsk();
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	

}
