package com.rxtec.pitchecking.mq.police;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.mq.PITInfoJson;
import com.rxtec.pitchecking.mqtt.GatCtrlReceiverBroker;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;

public class PITInfoPolicePublisher {

	private LinkedBlockingQueue<PITInfoJson> frameQueue = new LinkedBlockingQueue<PITInfoJson>(3);
	private LinkedBlockingQueue<PITInfoJson> verifyDataQueue = new LinkedBlockingQueue<PITInfoJson>(3);

	private Log log = LogFactory.getLog("PITInfoPolicePublisher");
	
	private String qrCode="";

	public String getQrCode() {
		return qrCode;
	}

	public void setQrCode(String qrCode) {
		this.qrCode = qrCode;
	}

	private static PITInfoPolicePublisher _instance = new PITInfoPolicePublisher();

	public static synchronized PITInfoPolicePublisher getInstance() {
		if (_instance == null)
			_instance = new PITInfoPolicePublisher();
		return _instance;
	}

	private PITInfoPolicePublisher() {

	}

	public void startService(int msgType) {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
		if (msgType == 1) {
			PITInfoPoliceTopicSender frameSender = new PITInfoPoliceTopicSender(frameQueue);
			scheduler.scheduleWithFixedDelay(frameSender, 0, 100, TimeUnit.MILLISECONDS);
		} else if (msgType == 2) {
			PITInfoPoliceTopicSender verifySender = new PITInfoPoliceTopicSender(verifyDataQueue);
			scheduler.scheduleWithFixedDelay(verifySender, 0, 100, TimeUnit.MILLISECONDS);
		}

		// ExecutorService es = Executors.newFixedThreadPool(3);
		// es.submit(frameSender);
		// es.shutdown();
		//

	}

		
	/**
	 * 将人脸数据转成json并offer进verifyDataQueue
	 * @param d
	 */
	public void offerVerifyData(PITVerifyData d) {
		PITInfoJson info = null;
		try {
			d.setQrCode(this.qrCode);
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
	 * 将通道帧图片转成json并offer进frameQueue
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
		PITInfoPolicePublisher.getInstance().startService(3);
		GatCtrlReceiverBroker.getInstance("Alone");//启动PITEventTopic本地监听
//		RemoteMonitorPublisher.getInstance().offerFrameData(new byte[] { 1, 0, 2, 3 });
//		RemoteMonitorPublisher.getInstance().offerEventData(DeviceConfig.Event_OpenFirstDoor);
//		RemoteMonitorPublisher.getInstance().offerEventData(DeviceConfig.Event_OpenSecondDoor);
//		RemoteMonitorPublisher.getInstance().offerEventData(DeviceConfig.Event_OpenThirdDoor);
	}
}
