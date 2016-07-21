package com.rxtec.pitchecking.mq;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.domain.FailedFace;
import com.rxtec.pitchecking.utils.CommUtil;

public class JmsSender {
	private Log log = LogFactory.getLog("JmsSender");
	private Destination destination = null;
	private Connection conn = null;
	private Session session = null;
	private MessageProducer producer = null;
	
	public JmsSender() throws Exception{
			initialize();
			// 连接到JMS提供者（服务器）
			conn.start();
	}

	// 初始化
	private void initialize() throws JMSException, Exception {
		// 连接工厂
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(DeviceConfig.getInstance().getUSER(), DeviceConfig.getInstance().getPASSWORD(),
				DeviceConfig.getInstance().getMQURL());
		conn = connectionFactory.createConnection();
		// 事务性会话，自动确认消息
		session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
		// 消息的目的地（Queue/Topic）
		// destination = session.createQueue(SUBJECT);
		destination = session.createTopic(DeviceConfig.getInstance().getTOPIC());
		log.info("TOPIC==" + destination);
		// destination = session.createTopic(SUBJECT);
		// 消息的提供者（生产者）
		producer = session.createProducer(destination);
		// 不持久化消息
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
	}

	public void sendMessage(String msgType, String strMsg, FailedFace failedFace) throws JMSException, Exception {
		
		// 发送文本消息
		if ("text".equals(msgType)) {
			// String textMsg = "ActiveMQ Text Message!";
			TextMessage msg = session.createTextMessage();
			// TextMessage msg = session.createTextMessage(textMsg);
			msg.setText(strMsg);
			producer.send(msg);
		}
		// 发送Map消息
		if ("map".equals(msgType)) {
			MapMessage mapMsg = session.createMapMessage();
			log.debug("failedFace.getIdNo=="+failedFace.getIdNo());
			log.debug("failedFace.getGateNo=="+failedFace.getGateNo());
			mapMsg.setString("idCardNo", failedFace.getIdNo());
			mapMsg.setString("ipAddress", failedFace.getIpAddress());
			mapMsg.setString("gateNo", failedFace.getGateNo());
			mapMsg.setBytes("cardImage", failedFace.getCardImage());
			mapMsg.setBytes("faceImage", failedFace.getFaceImage());

			producer.send(mapMsg);
		}
		// 发送流消息
		if ("stream".equals(msgType)) {
			String streamValue = "ActiveMQ stream Message!";
			StreamMessage msg = session.createStreamMessage();
			msg.writeString(streamValue);
			msg.writeBoolean(false);
			msg.writeLong(1234567890);
			producer.send(msg);
		}
		// 发送对象消息
		if ("object".equals(msgType)) {
			// FailedFace failedFace = new FailedFace();
			failedFace.setIdNo("520203197912141118");
			failedFace.setIpAddress("192.168.0.137");

			File image = null;
			image = new File("zp.jpg");
			failedFace.setCardImage(CommUtil.getBytesFromFile(image));

			File faceImage = new File("zhao.jpg");
			failedFace.setFaceImage(CommUtil.getBytesFromFile(faceImage));

			ObjectMessage msg = session.createObjectMessage();
			msg.setObject(failedFace);
			producer.send(msg);
		}
		// 发送字节消息
		if ("bytes".equals(msgType)) {
			String byteValue = "字节消息";
			BytesMessage msg = session.createBytesMessage();
			msg.writeBytes(byteValue.getBytes());
			producer.send(msg);
		}
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
}