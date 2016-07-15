package com.rxtec.pitchecking.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmsReceiverTask implements Runnable {
	private Logger log = LoggerFactory.getLogger("JmsReceiverTask");
	JmsReceiver receiver = new JmsReceiver();
	@Override
	public void run() {
		// TODO Auto-generated method stub
		this.startMQReceiver();
	}
	
	/**
	 * 启动activemq
	 */
	private void startMQReceiver() {		
		try {
			receiver.receiveMessage();
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			log.error("JmsReceiverTask startMQReceiver" + ex);
		}
	}
}
