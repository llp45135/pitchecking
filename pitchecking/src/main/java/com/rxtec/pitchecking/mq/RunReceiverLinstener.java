package com.rxtec.pitchecking.mq;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.JMSException;

public class RunReceiverLinstener {

	public static void main(String[] args) throws JMSException, Exception {
		// TODO Auto-generated method stub
		ExecutorService executer = Executors.newCachedThreadPool();
		JmsReceiverTask jmsReceiverTask = new JmsReceiverTask();
		executer.execute(jmsReceiverTask);
	}

}
