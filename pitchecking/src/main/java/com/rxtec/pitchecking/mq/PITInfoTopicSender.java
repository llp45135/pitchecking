package com.rxtec.pitchecking.mq;

import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rxtec.pitchecking.device.DeviceConfig;

public class PITInfoTopicSender implements Runnable {

	private Destination destination = null;
	private Connection conn = null;
	private Session session = null;
	private MessageProducer producer = null;
	private boolean isInitOK = false;
	private Log log = LogFactory.getLog("PITInfoTopic");

	private LinkedBlockingQueue<PITInfoJson> infoQueue;

	public PITInfoTopicSender(LinkedBlockingQueue<PITInfoJson> q) {
		infoQueue = q;
		try {
			initialize();
			isInitOK = true;
		} catch (Exception e) {
			log.error("RemoteMonitorPublisher init error!", e);
			isInitOK = false;
		}

	}

	// 初始化
	private void initialize() throws JMSException, Exception {
		// 连接工厂
//		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
//				DeviceConfig.getInstance().getUSER(), DeviceConfig.getInstance().getPASSWORD(),
//				DeviceConfig.getInstance().getMQURL());
		
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://127.0.0.1:61616");
		conn = connectionFactory.createConnection();
		// 事务性会话，自动确认消息
		session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
		// 消息的目的地（Queue/Topic）
		// destination = session.createQueue(SUBJECT);
		destination = session.createTopic("PITInfoTopic");
		log.info("TOPIC==" + destination);
		// 消息的提供者（生产者）
		producer = session.createProducer(destination);
		// 不持久化消息
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		log.info("initialize succ!");
	}

	public void sendMessage(String strMsg) throws JMSException, Exception {
		TextMessage msg = session.createTextMessage();
		msg.setText(strMsg);
		producer.send(msg);
		log.debug(strMsg);
	}

	// 关闭连接
	public void close() throws JMSException {
		if (producer != null)
			producer.close();
		if (session != null)
			session.close();
		if (conn != null)
			conn.close();
	}

	@Override
	public void run() {
		PITInfoJson info = null;
		try {
			info = infoQueue.take();
		} catch (InterruptedException e) {
			log.error("pitDataQueue take data error", e);
		}

		if (info != null) {
			try {
				String msg = info.getJsonStr();
				if (msg != null && isInitOK)
					sendMessage(msg);
			} catch (Exception e) {
				log.error("buildPITDataJsonBytes error", e);
			}
		}

	}

}
