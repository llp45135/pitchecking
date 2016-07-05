package com.rxtec.pitchecking.mq;

import javax.jms.JMSException;

public class RunReceiverLinstener {

	public static void main(String[] args) throws JMSException, Exception {
		// TODO Auto-generated method stub
		JmsReceiver receiver = new JmsReceiver();
		receiver.receiveMessage();
//		receiver.close();
	}

}
