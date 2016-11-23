package com.rxtec.pitchecking.task;

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.mqtt.ManualEventSenderBroker;

public class SendPITEventTask implements Runnable {
	private Logger log = LoggerFactory.getLogger("GatCtrlReceiverBroker");
	private static SendPITEventTask _instance;

	private LinkedBlockingQueue<String> infoQueue = new LinkedBlockingQueue<String>(3);;

	public static synchronized SendPITEventTask getInstance() {
		if (_instance == null) {
			_instance = new SendPITEventTask();
		}
		return _instance;
	}

	private SendPITEventTask() {
		// TODO Auto-generated constructor stub
	}

	public void offerEventData(String eventMsg) {
		if (eventMsg == null)
			return;
		if (!infoQueue.offer(eventMsg)) {
			infoQueue.poll();
			infoQueue.offer(eventMsg);
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		String info = null;
		try {
			info = infoQueue.poll();
			// log.info("infoQueue.take=="+info);
		} catch (Exception e) {
			log.error("pitDataQueue take data error", e);
		}

		if (info != null) {
			try {
				ManualEventSenderBroker.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT).sendDoorCmd(info);
				log.debug("消息已发送:"+info);
			} catch (Exception e) {
				log.error("buildPITDataJsonBytes error", e);
			}
		}
	}

}
