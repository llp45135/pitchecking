package com.rxtec.pitchecking.picheckingservice;

import java.util.Calendar;
import java.util.List;
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
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.domain.FailedFace;
import com.rxtec.pitchecking.mq.JmsSender;



public class FaceCheckingService {
	private Logger log = LoggerFactory.getLogger("FaceCheckingService");

	
	private boolean isCheck = true;


	private static FaceCheckingService _instance = new FaceCheckingService();

	//已经检测人脸质量，待验证的队列
	private LinkedBlockingQueue<PITData> detectedFaceDataQueue ;

	//比对验证通过的队列
	private LinkedBlockingQueue<PITData> passFaceDataQueue;

	private FailedFace failedFace;
	
	public FailedFace getFailedFace() {
		return failedFace;
	}
	public void setFailedFace(FailedFace failedFace) {
		this.failedFace = failedFace;
	}
	public LinkedBlockingQueue<PITData> getPassFaceDataQueue() {
		return passFaceDataQueue;
	}
	private FaceCheckingService(){
		detectedFaceDataQueue = new LinkedBlockingQueue<PITData>(Config.getInstance().getDetectededFaceQueueLen());
		passFaceDataQueue =new LinkedBlockingQueue<PITData>(1);
	}
	public static synchronized FaceCheckingService getInstance(){
		if(_instance == null) _instance = new FaceCheckingService();
		return _instance;
	}
	
	public PITData pollPassFaceData() throws InterruptedException{
		PITData fd = passFaceDataQueue.poll(Config.getInstance().getFaceCheckDelayTime(),TimeUnit.SECONDS );
		return fd;
	}
	
	
	public PITData takeDetectedFaceData() throws InterruptedException{
		PITData p = detectedFaceDataQueue.take();
		log.debug("detectedFaceDataQueue length=" + detectedFaceDataQueue.size());
		
		return p;
	}
	
	public void offerPassFaceData(PITData fd){
		isCheck = passFaceDataQueue.offer(fd);
		log.debug("offerPassFaceData...... " + isCheck);
	}
	
	public void offerDetectedFaceData(PITData faceData){
		if(!detectedFaceDataQueue.offer(faceData)){
			detectedFaceDataQueue.poll();
			detectedFaceDataQueue.offer(faceData);
		}
	}
	
	
	public void offerDetectedFaceData(List<PITData> faceDatas){
		detectedFaceDataQueue.clear();
		detectedFaceDataQueue.addAll(faceDatas);
	}
	

	
	public void resetFaceDataQueue(){
		detectedFaceDataQueue.clear();
		passFaceDataQueue.clear();
	}
	
	ExecutorService executor = Executors.newCachedThreadPool();
	
	public void beginFaceCheckerTask(){
		ExecutorService executer = Executors.newCachedThreadPool();
		FaceCheckingTask task1 = new FaceCheckingTask(Config.FaceVerifyDLLName);
		executer.execute(task1);

//		FaceCheckingTask task2 = new FaceCheckingTask(Config.FaceVerifyCloneDLLName);
//		executer.execute(task2);
		
		
	}



//	@Override
//	public void run() {
//		while (true) {
//			if(!isCheck) continue;
//			try {
//				Thread.sleep(50);
//				PITData fd = FaceCheckingService.getInstance().takeDetectedFaceData();//从待验证人脸队列中取出人脸对象
//				
//				if (fd != null) {
//					IDCard idCard = fd.getIdCard();
//					if (idCard == null)
//						continue;
//					long nowMils = Calendar.getInstance().getTimeInMillis();
//					float resultValue = 0;
//					byte[] extractFaceImageBytes = fd.getExtractFaceImageBytes();
//					if (extractFaceImageBytes == null)
//						continue;
//					resultValue = faceVerify.verify(extractFaceImageBytes, fd.getIdCard().getImageBytes());//比对人脸
//					fd.setFaceCheckResult(resultValue);
//					long usingTime = Calendar.getInstance().getTimeInMillis() - nowMils;
//					if (resultValue >= Config.getInstance().getFaceCheckThreshold()) {
//						offerPassFaceData(fd);
//						this.setFailedFace(null);
//					} else {
//						if (fd.getFaceDetectedResult() != null) {
//							if (fd.getFaceDetectedResult().isWearsglasses()) {
//								if (resultValue >= Config.getInstance().getGlassFaceCheckThreshold()) {
//									offerPassFaceData(fd);
//									this.setFailedFace(null);
//								}else{
//									FailedFace failedFD = new FailedFace();
//									failedFD.setIdNo(fd.getIdCard().getIdNo());
//									failedFD.setIpAddress(DeviceConfig.getInstance().getIpAddress());
//									failedFD.setGateNo(DeviceConfig.getInstance().getGateNo());
//									failedFD.setCardImage(fd.getIdCard().getImageBytes());
//									failedFD.setFaceImage(extractFaceImageBytes);
//									this.setFailedFace(failedFD);
//								}
//							}else{
//								FailedFace failedFD = new FailedFace();
//								failedFD.setIdNo(fd.getIdCard().getIdNo());
//								failedFD.setIpAddress(DeviceConfig.getInstance().getIpAddress());
//								failedFD.setGateNo(DeviceConfig.getInstance().getGateNo());
//								failedFD.setCardImage(fd.getIdCard().getImageBytes());
//								failedFD.setFaceImage(extractFaceImageBytes);
//								this.setFailedFace(failedFD);
//							}
//						}else{
//							FailedFace failedFD = new FailedFace();
//							failedFD.setIdNo(fd.getIdCard().getIdNo());
//							failedFD.setIpAddress(DeviceConfig.getInstance().getIpAddress());
//							failedFD.setGateNo(DeviceConfig.getInstance().getGateNo());
//							failedFD.setCardImage(fd.getIdCard().getImageBytes());
//							failedFD.setFaceImage(extractFaceImageBytes);
//							this.setFailedFace(failedFD);
//						}
//					}
//					//将人脸图像存盘
//					fd.saveFaceDataToDsk();
//				}
//
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		}
//	}

	

}
