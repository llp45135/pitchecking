package com.rxtec.pitchecking.mq.wharf;

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
import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.mq.PITInfoJson;
import com.rxtec.pitchecking.net.event.wharf.PhotoReceiptBean;

public class PITInfoWharfTopicSender implements Runnable {

	private Destination destination = null;
	private Connection conn = null;
	private Session session = null;
	private MessageProducer producer = null;
	private boolean isInitOK = false;
	private Log log = LogFactory.getLog("PITInfoWharfTopicSender");

	private LinkedBlockingQueue<PhotoReceiptBean> infoQueue;

	public PITInfoWharfTopicSender(LinkedBlockingQueue<PhotoReceiptBean> q) {
		infoQueue = q;
		this.connectMQ();
	}

	private void connectMQ() {
		log.info("Sender准备连接桂林码头MQ..." + Config.getInstance().getWharfMQUrl());
		try {
			initialize();
			isInitOK = true;
		} catch (Exception e) {
			log.error("PITInfoWharfTopicSender init error!", e);
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

		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(DeviceConfig.getInstance().getUSER(), DeviceConfig.getInstance().getPASSWORD(),
				Config.getInstance().getWharfMQUrl());
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
		log.info("Sender连接桂林码头MQ成功!");
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
		PhotoReceiptBean info = null;
		try {
			info = infoQueue.poll();
			// log.debug("infoQueue.take=="+info);
		} catch (Exception e) {
			log.error("pitDataQueue take data error", e);
		}

		if (info != null) {
			try {
				if (info.getMsgType() == PITInfoJson.MSG_TYPE_VERIFY) {
					log.info("人脸图片准备发送至桂林MQ");
				} else if (info.getMsgType() == PITInfoJson.MSG_TYPE_FRAME) {
					log.info("通道帧图片准备发送至桂林MQ");
				}

				ObjectMapper mapper = new ObjectMapper();
				String msg = mapper.writeValueAsString(info);
				msg = msg.replace("\\r\\n", "");

				if (msg != null && isInitOK)
					sendMessage(msg);
				// deviceEventListenerlog.debug("发送至公安的json==" + msg);
			} catch (Exception e) {
				log.error("buildPITDataJsonBytes error", e);
			}
		}
		// }

	}

}
