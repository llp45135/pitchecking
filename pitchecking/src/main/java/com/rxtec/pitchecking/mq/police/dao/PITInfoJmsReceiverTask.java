package com.rxtec.pitchecking.mq.police.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.utils.CommUtil;

public class PITInfoJmsReceiverTask implements Runnable {
	private Logger log = LoggerFactory.getLogger("PITInfoJmsReceiverTask");
	PITInfoJmsReceiver jmsReceiver = null;

	@Override
	public void run() {
		while (true) {
			if (jmsReceiver == null) {
				try {
					jmsReceiver = new PITInfoJmsReceiver();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					jmsReceiver = null;
					log.error("JmsReceiverTask JmsReceiver 实例化失败");
				}
			}
			if (jmsReceiver != null) {
				log.debug("JmsReceiverTask JmsReceiver 实例化已经成功");
				break;
			}
			CommUtil.sleep(1000);
		}
		if (jmsReceiver != null) {
			this.startMQReceiver();
		}
	}

	/**
	 * 启动activemq
	 */
	private void startMQReceiver() {
		try {
			jmsReceiver.receiveMessage();
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			log.error("JmsReceiverTask startMQReceiver" + ex);
		}
	}
}
