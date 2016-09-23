package com.rxtec.pitchecking.picheckingservice;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.domain.FailedFace;
import com.rxtec.pitchecking.net.PIVerifyResultSubscriber;
import com.rxtec.pitchecking.net.PTVerifyPublisher;

public class FaceCheckingService {
	private Logger log = LoggerFactory.getLogger("FaceCheckingService");

	private boolean isCheck = true;

	private static FaceCheckingService _instance = new FaceCheckingService();

	// 已经检测人脸质量，待验证的队列
	private LinkedBlockingQueue<PITData> detectedFaceDataQueue;

	// 待验证的队列,独立进程比对
	private LinkedBlockingQueue<PITVerifyData> faceVerifyDataQueue;

	// 比对验证通过的队列
	private LinkedBlockingQueue<PITVerifyData> passFaceDataQueue;
	
	// 比对未通过验证的List，按照分值排序，分值高的在前
	private List<PITVerifyData> failedFaceDataList;

	private FailedFace failedFace = null;

	public FailedFace getFailedFace() {
		return failedFace;
	}

	public void setFailedFace(FailedFace failedFace) {
		this.failedFace = failedFace;
	}

	public LinkedBlockingQueue<PITVerifyData> getPassFaceDataQueue() {
		return passFaceDataQueue;
	}

	private FaceCheckingService() {
		detectedFaceDataQueue = new LinkedBlockingQueue<PITData>(Config.getInstance().getDetectededFaceQueueLen());
		faceVerifyDataQueue = new LinkedBlockingQueue<PITVerifyData>(Config.getInstance().getDetectededFaceQueueLen());
		passFaceDataQueue = new LinkedBlockingQueue<PITVerifyData>(1);
		failedFaceDataList = new LinkedList<PITVerifyData>();
	}

	public static synchronized FaceCheckingService getInstance() {
		if (_instance == null)
			_instance = new FaceCheckingService();
		return _instance;
	}

	//从阻塞队列中返回比对成功的人脸
	public PITVerifyData pollPassFaceData(int delaySeconds) throws InterruptedException {
		PITVerifyData fd = passFaceDataQueue.poll(delaySeconds, TimeUnit.SECONDS);
		return fd;
	}
	
	//返回人脸比对分值最高的,比对未通过的人脸数据
	public PITVerifyData pollFailedFaceData(){
		if(failedFaceDataList.size()>0) return failedFaceDataList.get(0);
		return null;
	}

	public PITData takeDetectedFaceData() throws InterruptedException {
		PITData p = detectedFaceDataQueue.take();
		log.debug("detectedFaceDataQueue length=" + detectedFaceDataQueue.size());

		return p;
	}

	public PITVerifyData takeFaceVerifyData() throws InterruptedException {
		PITVerifyData v = faceVerifyDataQueue.take();
		log.debug("detectedFaceDataQueue length=" + detectedFaceDataQueue.size());
		return v;
	}

	//Offer 比对成功的人脸到阻塞队列
	public void offerPassFaceData(PITVerifyData fd) {
		isCheck = passFaceDataQueue.offer(fd);
		log.debug("offerPassFaceData...... " + isCheck);
	}

	//添加比对失败的人脸到队列，按照比对比对结果分值排序
	public void offerFailedFaceData(PITVerifyData fd) {
		log.debug("offerFailedFaceData list length= " + failedFaceDataList.size());
		failedFaceDataList.add(fd);
		Collections.sort(failedFaceDataList);
	}

	
	public void offerDetectedFaceData(PITData faceData) {
		if (!detectedFaceDataQueue.offer(faceData)) {
			detectedFaceDataQueue.poll();
			detectedFaceDataQueue.offer(faceData);
		}

		PITVerifyData vd = new PITVerifyData(faceData);
		if (vd.getIdCardImg() != null && vd.getFrameImg() != null && vd.getFaceImg() != null) {

			if (!faceVerifyDataQueue.offer(vd)) {
				faceVerifyDataQueue.poll();
				faceVerifyDataQueue.offer(vd);
			}
		}else{
			log.error("offerDetectedFaceData 输入数据不完整！ vd.getIdCardImg()="+vd.getIdCardImg() +" vd.getFrameImg()=" + vd.getFrameImg() + " vd.getFaceImg()=" + vd.getFaceImg() );
		}

	}

	public void offerFaceVerifyData(PITVerifyData faceData) {
		if (!faceVerifyDataQueue.offer(faceData)) {
			faceVerifyDataQueue.poll();
			faceVerifyDataQueue.offer(faceData);
		}

	}

	public void offerDetectedFaceData(List<PITData> faceDatas) {
		detectedFaceDataQueue.clear();
		detectedFaceDataQueue.addAll(faceDatas);
	}

	public void resetFaceDataQueue() {
		detectedFaceDataQueue.clear();
		faceVerifyDataQueue.clear();
		passFaceDataQueue.clear();
		failedFaceDataList.clear();
	}

	ExecutorService executor = Executors.newCachedThreadPool();

	/**
	 * 启动人脸比对
	 * 本函数由独立人脸检测进程执行
	 */
	public void beginFaceCheckerTask() {
		/**
		 * 启动独立人脸比对进程的结果订阅
		 */
		PIVerifyResultSubscriber.getInstance().startSubscribing();
		/**
		 * 启动向独立人脸比对进程发布比对请求的发布者
		 */
		PTVerifyPublisher.getInstance();
	}

	/**
	 * 单独比对人脸进程Task，通过共享内存通信
	 * 此函数由独立人脸比对进程执行
	 */
	public void beginFaceCheckerStandaloneTask() {
		ExecutorService executer = Executors.newCachedThreadPool();
		FaceCheckingStandaloneTask task1 = new FaceCheckingStandaloneTask();
		executer.execute(task1);
		if (Config.getInstance().getFaceVerifyThreads() == 2) {
			FaceCheckingStandaloneTask task2 = new FaceCheckingStandaloneTask();
			executer.execute(task2);
		}
		log.info(".............Start " + Config.getInstance().getFaceVerifyThreads() + " FaceVerifyThreads");
		executer.shutdown();
	}

}
