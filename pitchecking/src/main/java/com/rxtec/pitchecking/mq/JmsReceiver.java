package com.rxtec.pitchecking.mq;

import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.device.SecondGateDevice;
import com.rxtec.pitchecking.domain.FailedFace;
import com.rxtec.pitchecking.utils.CommUtil;

public class JmsReceiver implements MessageListener {
	private Log log = LogFactory.getLog("JmsReceiver");
	private Destination dest = null;
	private Connection conn = null;
	private Session session = null;
	private MessageConsumer consumer = null;

	private boolean stop = false;

	// 初始化
	private void initialize() throws JMSException, Exception {
		// frame.setVisible(true);
		// 连接工厂是用户创建连接的对象.
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
				DeviceConfig.getInstance().getUSER(), DeviceConfig.getInstance().getPASSWORD(),
				DeviceConfig.getInstance().getMQURL());
		// 连接工厂创建一个jms connection
		conn = connectionFactory.createConnection();
		// 是生产和消费的一个单线程上下文。会话用于创建消息的生产者，消费者和消息。会话提供了一个事务性的上下文。
		session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE); // 不支持事务
		// 目的地是客户用来指定他生产消息的目标还有他消费消息的来源的对象.
		// dest = session.createQueue(SUBJECT);
		dest = session.createTopic(DeviceConfig.getInstance().getTOPIC_RESULT());
		log.info("TOPIC==" + dest);
		// dest = session.createTopic(SUBJECT);
		// 会话创建消息的生产者将消息发送到目的地
		consumer = session.createConsumer(dest);
	}

	/**
	 * 消费消息
	 * 
	 * @throws JMSException
	 * @throws Exception
	 */
	public void receiveMessage() throws JMSException, Exception {
		initialize();
		conn.start();
		consumer.setMessageListener(this);
		// 等待接收消息
		while (!stop) {
			Thread.sleep(1000);
		}

	}

	@Override
	public void onMessage(Message msg) {
		try {
			if (msg instanceof TextMessage) {
				TextMessage message = (TextMessage) msg;
				log.info("------Received TextMessage------");
				// Y192.168.0.201*201605281634158191
				String backMsg = message.getText();
				log.info("人工验证返回的结果：" + backMsg);
				if (backMsg.indexOf(DeviceConfig.getInstance().getIpAddress()) != -1) {
					if (backMsg.startsWith("Y")) {
						SecondGateDevice.getInstance().openThirdDoor(); // 人脸比对通过，开第三道闸门
					} else {
						SecondGateDevice.getInstance().openSecondDoor(); // 人脸比对失败，开第二道电磁门
					}
				}
			} else if (msg instanceof MapMessage) {
				MapMessage message = (MapMessage) msg;
				log.info("idCardNo==" + message.getString("idCardNo"));
				log.info("ipAddress==" + message.getString("ipAddress"));
				log.info("gateNo==" + message.getString("gateNo"));

				byte[] idCardAarray = message.getBytes("cardImage");
				log.info("idCardAarray length==" + idCardAarray.length);
				CommUtil.getFileFromBytes(idCardAarray, "dd.jpg");

				byte[] faceArrayy = message.getBytes("faceImage");
				log.info("faceArray.length==" + faceArrayy.length);
				CommUtil.getFileFromBytes(faceArrayy, "zhaobak1.jpg");
				CommUtil.byte2image(faceArrayy, "zhaobak2.jpg");

				FailedFace face = new FailedFace();
				face.setIdNo(message.getString("idCardNo"));
				face.setIpAddress(message.getString("ipAddress"));
				face.setGateNo(message.getString("gateNo"));
				face.setCardImage(idCardAarray);
				face.setFaceImage(faceArrayy);
				// CommUtil.showImageLabel(face);

			} else if (msg instanceof StreamMessage) {
				StreamMessage message = (StreamMessage) msg;
				log.info("------Received StreamMessage------");
				log.info(message.readString());
				log.info(message.readBoolean());
				log.info(message.readLong());
			} else if (msg instanceof ObjectMessage) {
				log.info("------Received ObjectMessage------");
				ObjectMessage message = (ObjectMessage) msg;
				// JmsObjectMessageBean jmsObject = (JmsObjectMessageBean)
				// message.getObject();
				FailedFace failedFace = (FailedFace) message.getObject();
				// log.info(jmsObject.getUserName() + "__" + jmsObject.getAge()
				// + "__" + jmsObject.isFlag());
				log.info("idCard no==" + failedFace.getIdNo());
				log.info("getIpAddress==" + failedFace.getIpAddress());

				byte[] idCardAarray = failedFace.getCardImage();
				log.info("idCardAarray length==" + idCardAarray.length);
				CommUtil.getFileFromBytes(idCardAarray, "dd.jpg");

				byte[] faceArrayy = failedFace.getFaceImage();
				log.info("faceArray.length==" + faceArrayy.length);
				CommUtil.getFileFromBytes(faceArrayy, "zhaobak1.jpg");
				CommUtil.byte2image(faceArrayy, "zhaobak2.jpg");
			} else if (msg instanceof BytesMessage) {
				log.info("------Received BytesMessage------");
				BytesMessage message = (BytesMessage) msg;
				byte[] byteContent = new byte[1024];
				int length = -1;
				StringBuffer content = new StringBuffer();
				while ((length = message.readBytes(byteContent)) != -1) {
					content.append(new String(byteContent, 0, length));
				}
				log.info(content.toString());
			} else {
				log.info(msg);
			}
			stop = true;
		} catch (JMSException e) {
			e.printStackTrace();
		}
		// finally {
		// try {
		// this.close();
		// } catch (JMSException e) {
		// e.printStackTrace();
		// }
		// }
	}

	// 关闭连接
	public void close() throws JMSException {
		log.info("Consumer:->Closing connection");
		if (consumer != null)
			consumer.close();
		if (session != null)
			session.close();
		if (conn != null)
			conn.close();
	}

}
