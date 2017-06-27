package com.rxtec.pitchecking.mq.police.dao;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import com.rxtec.pitchecking.db.mysql.PhotoReceiptSqlLoger;
import com.rxtec.pitchecking.device.BarUnsecurity;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.domain.FailedFace;
import com.rxtec.pitchecking.mbean.ProcessUtil;
import com.rxtec.pitchecking.mq.PITInfoJson;
import com.rxtec.pitchecking.net.event.CAMOpenBean;
import com.rxtec.pitchecking.net.event.wharf.PhotoReceiptBean;
import com.rxtec.pitchecking.picheckingservice.FaceImageLog;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
import com.rxtec.pitchecking.utils.BASE64Util;
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
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(DeviceConfig.getInstance().getUSER(), DeviceConfig.getInstance().getPASSWORD(),
				DeviceConfig.getInstance().getPoliceServer_MQURL());
		// 连接工厂创建一个jms connection
		conn = connectionFactory.createConnection();
		// 是生产和消费的一个单线程上下文。会话用于创建消息的生产者，消费者和消息。会话提供了一个事务性的上下文。
		session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE); // 不支持事务
		// 目的地是客户用来指定他生产消息的目标还有他消费消息的来源的对象.
		// dest = session.createQueue(SUBJECT);
		dest = session.createTopic("PITInfoTopic");
		log.debug("TOPIC_RESULT==" + dest);
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
				if (Config.getInstance().getIsUseWharfMQ() == 0) {
					ObjectMapper mapper = new ObjectMapper();
					try {
						PITInfoJmsObj pitInfoJsonBean = mapper.readValue(pitInfoMsg, PITInfoJmsObj.class);
						log.info("getMsgType==" + pitInfoJsonBean.getMsgType());
						if (pitInfoJsonBean.getMsgType() == 2) {

							// log.debug("getEvent==" + pitInfoJson.getEvent());
							log.info("IpAddress==" + pitInfoJsonBean.getIpAddress());
							// log.debug("getAge==" + pitInfoJson.getAge());
							// log.debug("getGender==" +
							// pitInfoJson.getGender());
							// log.debug("getIdHashCode==" +
							// pitInfoJson.getIdHashCode());
							log.info("IsVerifyPassed==" + pitInfoJsonBean.getIsVerifyPassed());
							log.info("Similarity==" + pitInfoJsonBean.getSimilarity());
							log.info("idcard.CardNo==" + new String(BASE64Util.decryptBASE64(pitInfoJsonBean.getIdCardNo())));

							if (pitInfoJsonBean.getSimilarity() > 0) {
								String idNo = new String(BASE64Util.decryptBASE64(pitInfoJsonBean.getIdCardNo()));

								PITVerifyData fd = new PITVerifyData();
								fd.setIdNo(new String(BASE64Util.decryptBASE64(pitInfoJsonBean.getIdCardNo())));
								fd.setVerifyResult(pitInfoJsonBean.getSimilarity());
								fd.setFaceDistance((float) 1.3);
								
								if (pitInfoJsonBean.getIdPicImageBase64() != null)
									fd.setIdCardImg(BASE64Util.decryptBASE64(pitInfoJsonBean.getIdPicImageBase64()));
								if (pitInfoJsonBean.getFaceImageBase64() != null)
									fd.setFaceImg(BASE64Util.decryptBASE64(pitInfoJsonBean.getFaceImageBase64()));
								if (pitInfoJsonBean.getFrameImageBase64() != null)
									fd.setFrameImg(BASE64Util.decryptBASE64(pitInfoJsonBean.getFrameImageBase64()));

								fd.setPitDate(CalUtils.getStringDateShort2());
								fd.setPitStation(pitInfoJsonBean.getDbCode());
								fd.setAge(pitInfoJsonBean.getAge());
								fd.setGender(pitInfoJsonBean.getGender());
								fd.setGateIp(pitInfoJsonBean.getIpAddress());
								fd.setQrCode(pitInfoJsonBean.getQrCode());
								fd.setPitTime(pitInfoJsonBean.getPitTime());

								PITInfoImgPath imgPath = new PITInfoImgPath();
								FaceImageLog.saveFaceDataToPoliceDsk(fd, imgPath);

								log.info("getQrCode==" + pitInfoJsonBean.getQrCode());
								Ticket ticket = null;
								if (pitInfoJsonBean.getQrCode() != null && pitInfoJsonBean.getQrCode().trim().length() == 144) {
									ticket = BarUnsecurity.getInstance().buildTicket(
											BarUnsecurity.getInstance().uncompress(pitInfoJsonBean.getQrCode(), CalUtils.getStringDateShort2().substring(0, 4)));
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
								String dbcode = pitInfoJsonBean.getDbCode();
								String gateNo = pitInfoJsonBean.getGateNo();
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

								String dirName = Config.getInstance().getPoliceJsonDir() + CalUtils.getStringDateShort2() + "/";
								int ret = CommUtil.createDir(dirName);
								if (ret == 0 || ret == 1) {
									String fileName = dirName + idNo + "_" + idNo + "_" + dbcode + "_" + gateNo + "_" + CalUtils.getStringFullTimeHaomiao() + ".json";
									log.info("fileName==" + fileName);
									// ProcessUtil.writeFileContent(fileName,
									// pitInfoNewJson);
									ProcessUtil.writeFileContent(fileName, pitInfoNewJson, "utf-8");

									// File logFile = new File(fileName);
									// if (!logFile.exists()) {
									// logFile.createNewFile();
									// }
									// mapper.writeValue(logFile,
									// pitInfoJsonBean);
								}
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						log.error("PITInfoJson:", e);
					}
				} else {
					if (pitInfoMsg.indexOf("{\"msgType\":1") < 0 && pitInfoMsg.indexOf("idHashCode") < 0) {
						ObjectMapper mapper = new ObjectMapper();
						try {
							PhotoReceiptBean photoReceiptBean = mapper.readValue(pitInfoMsg, PhotoReceiptBean.class);
							log.info("getMsgType==" + photoReceiptBean.getMsgType());
							if (photoReceiptBean.getMsgType() == 2) {
								log.info("getIdCardNo==" + photoReceiptBean.getIdCardNo());
								log.info("getTicketNo==" + photoReceiptBean.getTicketNo());
								log.info("getPitDate==" + photoReceiptBean.getPitDate());
								log.info("getPitTime==" + photoReceiptBean.getPitTime());
								log.info("getCitizenName==" + photoReceiptBean.getCitizenName());
								log.info("IpAddress==" + photoReceiptBean.getIpAddress());
								
								log.info("VerifyResult==" + photoReceiptBean.getVerifyResult());
								if(photoReceiptBean.getVerifyResult()>=Config.getInstance().getFaceCheckThreshold()){
									photoReceiptBean.setIsVerifyPassed(0);
								}else{
									photoReceiptBean.setIsVerifyPassed(1);
								}
								log.info("IsVerifyPassed==" + photoReceiptBean.getIsVerifyPassed());
								
								if (photoReceiptBean.getVerifyResult() > 0) {
									String idNo = photoReceiptBean.getIdCardNo();

									PITVerifyData fd = new PITVerifyData();
									fd.setIdNo(idNo);
									fd.setVerifyResult(photoReceiptBean.getVerifyResult());
									fd.setFaceDistance((float) 1.3);
									fd.setFaceImg(BASE64Util.decryptBASE64(photoReceiptBean.getFaceImageBase64()));
									fd.setFrameImg(BASE64Util.decryptBASE64(photoReceiptBean.getFrameImageBase64()));
									fd.setIdCardImg(BASE64Util.decryptBASE64(photoReceiptBean.getIdPicImageBase64()));
									fd.setPitDate(CalUtils.getStringDateShort2());
									fd.setPitTime(photoReceiptBean.getPitTime());
									// fd.setPitStation(pitInfoJsonBean.getDbCode());
									fd.setAge(photoReceiptBean.getAge());
									fd.setGender(photoReceiptBean.getGender());
									fd.setGateIp(photoReceiptBean.getIpAddress());
									// fd.setQrCode(pitInfoJsonBean.getQrCode());

									PITInfoImgPath imgPath = new PITInfoImgPath();
									FaceImageLog.saveFaceDataToDskByReal(fd); // 将照片数据保存至硬盘

									String dirName = Config.getInstance().getImagesLogDir();
									dirName += CalUtils.getStringDateShort2();
									String idcardDir = dirName + "/Id_card/" + photoReceiptBean.getIdCardNo() + ".bmp";
									String faceDir = dirName + "/Faces/" + photoReceiptBean.getIdCardNo() + "_" + photoReceiptBean.getVerifyResult() + ".jpg";
									String photoDir = dirName + "/Photo/" + photoReceiptBean.getIdCardNo() + "_" + photoReceiptBean.getVerifyResult() + ".jpg";

									photoReceiptBean.setIdcardImagePath(idcardDir);
									photoReceiptBean.setFaceImagePath(faceDir);
									photoReceiptBean.setFrameImagePath(photoDir);
									if (Config.getInstance().getIsUseMySQLDB() == 1) {
										PhotoReceiptSqlLoger.getInstance().offer(photoReceiptBean);
									}
								}
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							log.error("PITInfoJson:", e);
						}
					}else{
						log.info("Json 格式不符,不做任何处理！");
					}
				}
			} else if (msg instanceof MapMessage) {
				MapMessage message = (MapMessage) msg;

			} else if (msg instanceof StreamMessage) {
				StreamMessage message = (StreamMessage) msg;
				log.debug("------Received StreamMessage------");
				log.debug(message.readString());
				log.debug(message.readBoolean());
				log.debug(message.readLong());
			} else if (msg instanceof ObjectMessage) {
				log.debug("------Received ObjectMessage------");
				ObjectMessage message = (ObjectMessage) msg;

			} else if (msg instanceof BytesMessage) {
				log.debug("------Received BytesMessage------");
				BytesMessage message = (BytesMessage) msg;
				byte[] byteContent = new byte[1024];
				int length = -1;
				StringBuffer content = new StringBuffer();
				while ((length = message.readBytes(byteContent)) != -1) {
					content.append(new String(byteContent, 0, length));
				}
				log.debug(content.toString());
			} else {
				log.debug(msg);
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
		log.debug("Consumer:->Closing connection");
		if (consumer != null)
			consumer.close();
		if (session != null)
			session.close();
		if (conn != null)
			conn.close();
	}

}
