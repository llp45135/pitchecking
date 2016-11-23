package com.rxtec.pitchecking.mq.police.dao;

import java.io.IOException;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.device.SecondGateDevice;
import com.rxtec.pitchecking.domain.FailedFace;
import com.rxtec.pitchecking.mbean.ProcessUtil;
import com.rxtec.pitchecking.mq.PITInfoJson;
import com.rxtec.pitchecking.net.event.CAMOpenBean;
import com.rxtec.pitchecking.picheckingservice.FaceImageLog;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
import com.rxtec.pitchecking.utils.BASE64;
import com.rxtec.pitchecking.utils.CalUtils;
import com.rxtec.pitchecking.utils.CommUtil;

public class PITInfoJmsReceiver implements MessageListener {
	private Log log = LogFactory.getLog("PITInfoJmsReceiver");
	private Destination dest = null;
	private Connection conn = null;
	private Session session = null;
	private MessageConsumer consumer = null;

	private boolean stop = false;

	public PITInfoJmsReceiver() throws Exception {
		initialize();
		conn.start();
	}

	// 初始化
	private void initialize() throws JMSException, Exception {
		// frame.setVisible(true);
		// 连接工厂是用户创建连接的对象.
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
				DeviceConfig.getInstance().getUSER(), DeviceConfig.getInstance().getPASSWORD(),
				DeviceConfig.getInstance().getPoliceServer_MQURL());
		// 连接工厂创建一个jms connection
		conn = connectionFactory.createConnection();
		// 是生产和消费的一个单线程上下文。会话用于创建消息的生产者，消费者和消息。会话提供了一个事务性的上下文。
		session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE); // 不支持事务
		// 目的地是客户用来指定他生产消息的目标还有他消费消息的来源的对象.
		// dest = session.createQueue(SUBJECT);
		dest = session.createTopic("PITInfoTopic");
		log.info("TOPIC_RESULT==" + dest);
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
				String pitInfoMsg = message.getText();
				// log.info("闸机传回的jms数据==" + pitInfoMsg);

				ObjectMapper mapper = new ObjectMapper();
				try {
					PITInfoJmsObj pitInfoJson = mapper.readValue(pitInfoMsg, PITInfoJmsObj.class);
					log.info("getMsgType==" + pitInfoJson.getMsgType());
					if (pitInfoJson.getMsgType() == 2) {
						// log.info("getIdCardNo==" + new
						// String(BASE64.decryptBASE64(pitInfoJson.getIdCardNo())));
						// log.info("getEvent==" + pitInfoJson.getEvent());
						log.info("IpAddress==" + pitInfoJson.getIpAddress());
						// log.info("getAge==" + pitInfoJson.getAge());
						// log.info("getGender==" + pitInfoJson.getGender());
						// log.info("getIdHashCode==" +
						// pitInfoJson.getIdHashCode());
						log.info("IsVerifyPassed==" + pitInfoJson.getIsVerifyPassed());
						log.info("Similarity==" + pitInfoJson.getSimilarity());

						String idNo = new String(BASE64.decryptBASE64(pitInfoJson.getIdCardNo()));
						String dirName = Config.getInstance().getPoliceJsonDir() + CalUtils.getStringDateShort2() + "/";						
						int ret = CommUtil.createDir(dirName);
						if (ret == 0 || ret == 1) {
							String fileName = dirName + idNo + "_" + CalUtils.getStringFullTimeHaomiao() + ".json";
							log.info("fileName==" + fileName);
							ProcessUtil.writeFileContent(fileName, pitInfoMsg);
						}

						PITVerifyData fd = new PITVerifyData();
						fd.setIdNo(new String(BASE64.decryptBASE64(pitInfoJson.getIdCardNo())));
						fd.setVerifyResult(pitInfoJson.getSimilarity());
						fd.setFaceDistance((float) 1.3);
						fd.setFaceImg(BASE64.decryptBASE64(pitInfoJson.getFaceImageBase64()));
						fd.setFrameImg(BASE64.decryptBASE64(pitInfoJson.getFrameImageBase64()));
						fd.setIdCardImg(BASE64.decryptBASE64(pitInfoJson.getIdPicImageBase64()));
						fd.setPitDate(CalUtils.getStringDateShort2());
						fd.setPitStation("IZQ");
						fd.setAge(pitInfoJson.getAge());
						fd.setGender(pitInfoJson.getGender());
						fd.setGateIp(pitInfoJson.getIpAddress());

						FaceImageLog.saveFaceDataToPoliceDsk(fd);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					log.error("PITInfoJson:", e);
				}

			} else if (msg instanceof MapMessage) {
				MapMessage message = (MapMessage) msg;

			} else if (msg instanceof StreamMessage) {
				StreamMessage message = (StreamMessage) msg;
				log.info("------Received StreamMessage------");
				log.info(message.readString());
				log.info(message.readBoolean());
				log.info(message.readLong());
			} else if (msg instanceof ObjectMessage) {
				log.info("------Received ObjectMessage------");
				ObjectMessage message = (ObjectMessage) msg;

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
