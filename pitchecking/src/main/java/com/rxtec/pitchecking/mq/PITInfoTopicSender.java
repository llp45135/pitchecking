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

import com.fasterxml.jackson.databind.ObjectMapper;
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
		this.connectMQ();
	}

	private void connectMQ() {
		log.debug("Sender准备连接人工控制台MQ..." + DeviceConfig.getInstance().getManualCheck_MQURL());
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
		// ActiveMQConnectionFactory connectionFactory = new
		// ActiveMQConnectionFactory(
		// DeviceConfig.getInstance().getUSER(),
		// DeviceConfig.getInstance().getPASSWORD(),
		// DeviceConfig.getInstance().getMQURL());

		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
				DeviceConfig.getInstance().getManualCheck_MQURL());
		conn = connectionFactory.createConnection();
		// 事务性会话，自动确认消息
		session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
		// 消息的目的地（Queue/Topic）
		// destination = session.createQueue(SUBJECT);
		destination = session.createTopic("PITInfoTopic");
		log.debug("TOPIC==" + destination);
		// 消息的提供者（生产者）
		producer = session.createProducer(destination);
		// 不持久化消息
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		log.debug("Sender连接人工控制台MQ成功!");
	}

	public void sendMessage(String strMsg) throws JMSException, Exception {
		// log.debug("starting send one Message...");
		TextMessage msg = session.createTextMessage();
		msg.setText(strMsg);
		producer.send(msg);
		// log.debug("send one Message####"+strMsg);
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
		// while (true) {
		PITInfoJson info = null;
		try {
			info = infoQueue.poll();
			// log.debug("infoQueue.take=="+info);
		} catch (Exception e) {
			log.error("pitDataQueue take data error", e);
		}

		if (info != null) {
			try {
				if (info.getMsgType() == PITInfoJson.MSG_TYPE_VERIFY)
					log.debug("人脸图片准备发送至人工处置口");
				String msg = info.getJsonStr();
				if (msg != null && isInitOK)
					sendMessage(msg);
			} catch (Exception e) {
				log.error("buildPITDataJsonBytes error", e);
			}
		}
		// }

	}

}
