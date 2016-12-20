package com.rxtec.pitchecking.mq.police.dao;

import java.io.File;
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
import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.device.BarUnsecurity;
import com.rxtec.pitchecking.device.DeviceConfig;
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
					PITInfoJmsObj pitInfoJsonBean = mapper.readValue(pitInfoMsg, PITInfoJmsObj.class);
					log.info("getMsgType==" + pitInfoJsonBean.getMsgType());
					if (pitInfoJsonBean.getMsgType() == 2) {

						// log.info("getEvent==" + pitInfoJson.getEvent());
						log.info("IpAddress==" + pitInfoJsonBean.getIpAddress());
						// log.info("getAge==" + pitInfoJson.getAge());
						// log.info("getGender==" + pitInfoJson.getGender());
						// log.info("getIdHashCode==" +
						// pitInfoJson.getIdHashCode());
						log.info("IsVerifyPassed==" + pitInfoJsonBean.getIsVerifyPassed());
						log.info("Similarity==" + pitInfoJsonBean.getSimilarity());
						log.info("idcard.CardNo==" + new String(BASE64.decryptBASE64(pitInfoJsonBean.getIdCardNo())));

						if (pitInfoJsonBean.getSimilarity() > 0) {
							String idNo = new String(BASE64.decryptBASE64(pitInfoJsonBean.getIdCardNo()));

							PITVerifyData fd = new PITVerifyData();
							fd.setIdNo(new String(BASE64.decryptBASE64(pitInfoJsonBean.getIdCardNo())));
							fd.setVerifyResult(pitInfoJsonBean.getSimilarity());
							fd.setFaceDistance((float) 1.3);
							fd.setFaceImg(BASE64.decryptBASE64(pitInfoJsonBean.getFaceImageBase64()));
							fd.setFrameImg(BASE64.decryptBASE64(pitInfoJsonBean.getFrameImageBase64()));
							fd.setIdCardImg(BASE64.decryptBASE64(pitInfoJsonBean.getIdPicImageBase64()));
							fd.setPitDate(CalUtils.getStringDateShort2());
							fd.setPitStation("IZQ");
							fd.setAge(pitInfoJsonBean.getAge());
							fd.setGender(pitInfoJsonBean.getGender());
							fd.setGateIp(pitInfoJsonBean.getIpAddress());

							PITInfoImgPath imgPath = new PITInfoImgPath();
							FaceImageLog.saveFaceDataToPoliceDsk(fd, imgPath);

							log.info("getQrCode==" + pitInfoJsonBean.getQrCode());
							Ticket ticket = null;
							if (pitInfoJsonBean.getQrCode() != null
									&& pitInfoJsonBean.getQrCode().trim().length() == 144) {
								ticket = BarUnsecurity.getInstance().buildTicket(BarUnsecurity.getInstance().uncompress(
										pitInfoJsonBean.getQrCode(), CalUtils.getStringDateShort2().substring(0, 4)));
								log.info("ticket.TrainCode==" + ticket.getTrainCode());
								log.info("ticket.CardNo==" + ticket.getCardNo());
								log.info("ticket.PassengerName==" + ticket.getPassengerName());

								pitInfoJsonBean.setFromStationCode(ticket.getFromStationCode());
								pitInfoJsonBean.setEndStationCode(ticket.getEndStationCode());
								pitInfoJsonBean.setChangeStationCode(ticket.getChangeStationCode());
								pitInfoJsonBean.setTicketNo(ticket.getTicketNo());
								pitInfoJsonBean.setTrainCode(ticket.getTrainCode());
								pitInfoJsonBean.setCoachNo(ticket.getCoachNo());
								pitInfoJsonBean.setSeatCode(ticket.getSeatCode());
								pitInfoJsonBean.setTicketType(ticket.getTicketType());
								pitInfoJsonBean.setSeatNo(ticket.getSeatNo());
								pitInfoJsonBean.setTrainDate(ticket.getTrainDate());
								pitInfoJsonBean.setCardNo(ticket.getCardNo());
								pitInfoJsonBean.setPassengerName(ticket.getPassengerName());
								pitInfoJsonBean.setSaleOfficeNo(ticket.getSaleOfficeNo());
								pitInfoJsonBean.setSaleWindowNo(ticket.getSaleWindowNo());
								pitInfoJsonBean.setSaleDate(ticket.getSaleDate());
							}

							pitInfoJsonBean.setIdCardNo(idNo);
							pitInfoJsonBean.setPitTime(CalUtils.getStringDateHaomiao());
							// pitInfoJsonBean.setFrameImageBase64(
							// new
							// String(BASE64.decryptBASE64(pitInfoJsonBean.getFrameImageBase64())));
							// pitInfoJsonBean.setFaceImageBase64(
							// new
							// String(BASE64.decryptBASE64(pitInfoJsonBean.getFaceImageBase64())));
							// pitInfoJsonBean.setIdPicImageBase64(
							// new
							// String(BASE64.decryptBASE64(pitInfoJsonBean.getIdPicImageBase64())));

							String pitInfoNewJson = mapper.writeValueAsString(pitInfoJsonBean);
							pitInfoNewJson = pitInfoNewJson.replace("\\r\\n", "");

							String dirName = Config.getInstance().getPoliceJsonDir() + CalUtils.getStringDateShort2()
									+ "/";
							int ret = CommUtil.createDir(dirName);
							if (ret == 0 || ret == 1) {
								String fileName = dirName + idNo + "_" + CalUtils.getStringFullTimeHaomiao() + ".json";
								log.info("fileName==" + fileName);
//								 ProcessUtil.writeFileContent(fileName, pitInfoNewJson);
								ProcessUtil.writeFileContent(fileName, pitInfoNewJson, "utf-8");

//								File logFile = new File(fileName);
//								if (!logFile.exists()) {
//									logFile.createNewFile();
//								}
//								mapper.writeValue(logFile, pitInfoJsonBean);
							}
						}
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
