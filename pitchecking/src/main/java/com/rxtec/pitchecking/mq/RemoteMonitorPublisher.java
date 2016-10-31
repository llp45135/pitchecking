package com.rxtec.pitchecking.mq;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.mqtt.GatCtrlReceiverBroker;
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

	public void startService(int msgType) {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
		if (msgType == 1) {
			PITInfoTopicSender frameSender = new PITInfoTopicSender(frameQueue);
			scheduler.scheduleWithFixedDelay(frameSender, 0, 100, TimeUnit.MILLISECONDS);
		} else if (msgType == 2) {
			PITInfoTopicSender verifySender = new PITInfoTopicSender(verifyDataQueue);
			scheduler.scheduleWithFixedDelay(verifySender, 0, 100, TimeUnit.MILLISECONDS);
		} else {
			PITInfoTopicSender eventSender = new PITInfoTopicSender(eventQueue);
			scheduler.scheduleWithFixedDelay(eventSender, 0, 1000, TimeUnit.MILLISECONDS);
		}

		// ExecutorService es = Executors.newFixedThreadPool(3);
		// es.submit(frameSender);
		// es.shutdown();
		//

	}

	/**
	 * 
	 * @param event
	 */
	public void offerEventData(int event) {
		PITInfoJson info = null;
		try {
			info = new PITInfoJson(event);
		} catch (Exception e) {
			log.error(e);
		}
		if (info == null)
			return;
		if (!eventQueue.offer(info)) {
			eventQueue.poll();
			eventQueue.offer(info);
		}
	}
	
	/**
	 * 
	 * @param d
	 */
	public void offerVerifyData(PITVerifyData d) {
		PITInfoJson info = null;
		try {
			info = new PITInfoJson(d);
		} catch (Exception e) {
			log.error(e);
		}
		if (info == null)
			return;
		if (!verifyDataQueue.offer(info)) {
			verifyDataQueue.poll();
			verifyDataQueue.offer(info);
		}
	}

	/**
	 * 
	 * @param imgBytes
	 */
	public void offerFrameData(byte[] imgBytes) {
		PITInfoJson info = null;
		try {
			info = new PITInfoJson(imgBytes);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (info == null)
			return;
		if (!frameQueue.offer(info)) {
			frameQueue.poll();
			frameQueue.offer(info);
		}
	}

	public static void main(String[] args) {
		RemoteMonitorPublisher.getInstance().startService(3);
		GatCtrlReceiverBroker.getInstance("Alone");//启动PITEventTopic本地监听
//		RemoteMonitorPublisher.getInstance().offerFrameData(new byte[] { 1, 0, 2, 3 });
//		RemoteMonitorPublisher.getInstance().offerEventData(DeviceConfig.Event_OpenFirstDoor);
//		RemoteMonitorPublisher.getInstance().offerEventData(DeviceConfig.Event_OpenSecondDoor);
//		RemoteMonitorPublisher.getInstance().offerEventData(DeviceConfig.Event_OpenThirdDoor);
	}
}
