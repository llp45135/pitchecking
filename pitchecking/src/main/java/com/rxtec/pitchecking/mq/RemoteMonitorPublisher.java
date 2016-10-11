package com.rxtec.pitchecking.mq;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;

public class RemoteMonitorPublisher {

	private LinkedBlockingQueue<PITInfoJson> frameQueue = new LinkedBlockingQueue<PITInfoJson>(3);
	private LinkedBlockingQueue<PITInfoJson> verifyDataQueue = new LinkedBlockingQueue<PITInfoJson>(3);
	private LinkedBlockingQueue<PITInfoJson> eventQueue = new LinkedBlockingQueue<PITInfoJson>(3);
	


	private Log log = LogFactory.getLog("RemoteMonitorPublisher");

	private static RemoteMonitorPublisher _instance = new RemoteMonitorPublisher();

	public static synchronized RemoteMonitorPublisher getInstance() {
		if (_instance == null)
			_instance = new RemoteMonitorPublisher();
		return _instance;
	}

	private RemoteMonitorPublisher() {
		
		
	}
	
	public void startService(){
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
		PITInfoTopicSender frameSender = new PITInfoTopicSender(frameQueue);
//		PITInfoTopicSender verifySender = new PITInfoTopicSender(verifyDataQueue);
//		PITInfoTopicSender eventSender = new PITInfoTopicSender(eventQueue);
		scheduler.scheduleWithFixedDelay(frameSender, 0, 500, TimeUnit.MILLISECONDS);
//		scheduler.scheduleWithFixedDelay(verifySender, 0, 1000, TimeUnit.MILLISECONDS);
//		scheduler.scheduleWithFixedDelay(eventSender, 0, 1000, TimeUnit.MILLISECONDS);

	}

	public void offerVerifyData(PITVerifyData d) {
		PITInfoJson info = null;
		try {
			info = new PITInfoJson(d);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(info == null) return;
		if (!verifyDataQueue.offer(info)) {
			verifyDataQueue.poll();
			verifyDataQueue.offer(info);
		}
	}
	
	public void offerFrameData(byte[] imgBytes) {
		PITInfoJson info = null;
		try {
			info = new PITInfoJson(imgBytes);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(info == null) return;
		if (!frameQueue.offer(info)) {
			frameQueue.poll();
			frameQueue.offer(info);
		}
	}
	
	
	public static void main(String[] args){
		RemoteMonitorPublisher.getInstance().startService();
		RemoteMonitorPublisher.getInstance().offerFrameData(new byte[]{1,0,2,3});
	}
}
