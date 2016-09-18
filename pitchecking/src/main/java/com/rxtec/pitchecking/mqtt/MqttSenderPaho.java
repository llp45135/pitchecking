package com.rxtec.pitchecking.mqtt;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rxtec.pitchecking.net.event.EventHandler;
import com.rxtec.pitchecking.net.event.PIVerifyResultBean;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.ImageToolkit;

public class MqttSenderPaho {
	Logger log = LoggerFactory.getLogger("MqttServerPaho");
	private MqttClient mqttClient;
//	private final static String CONNECTION_STRING = "tcp://222.51.4.186:5001";
	private final static String CONNECTION_STRING = "tcp://localhost:1883";
	private final static String CLIENT_ID = "ResultSubcriber";// 客户端标识
	private String userName = "test";
	private String passWord = "test";
	private final static int[] QOS_VALUES = { 0 };// 对应主题的消息级别
	private final static String[] RECEIVE_TOPIC = { "sub_topic" };
//	private final static String RECEIVE_TOPIC = "pub_topic";
	private final static String SEND_TOPIC = "sub_topic";
	private MqttConnectOptions options;
	private ObjectMapper mapper = new ObjectMapper();
	

	private static MqttSenderPaho _instance = new MqttSenderPaho();

	public static MqttSenderPaho getInstance() {
		return _instance;
	}

	private MqttSenderPaho() {
		try {
			this.connectMQServer();
		} catch (Exception e) {
			log.error("MqttServerPaho",e);
		}
	}

	/**
	 * 连接服务
	 * 
	 * @throws MqttException
	 */
	private void connectMQServer() throws MqttException {
		log.debug("####start connect####");
		mqttClient = new MqttClient(CONNECTION_STRING, CLIENT_ID, new MemoryPersistence());
		mqttClient.setCallback(new mqttCallHandler());
		options = new MqttConnectOptions();
//		options.setCleanSession(false);
//		 options.setAutomaticReconnect(true);

		// options.setUserName(userName);
		// options.setPassword(passWord.toCharArray());
//		// 设置超时时间
//		options.setConnectionTimeout(30);
//		// 设置会话心跳时间
//		options.setKeepAliveInterval(20);
		mqttClient.connect(options);
//		mqttClient.subscribe(RECEIVE_TOPIC,QOS_VALUES);
		mqttClient.unsubscribe(RECEIVE_TOPIC);

		log.debug("###,topic:" + RECEIVE_TOPIC[0] + ",  " + (CLIENT_ID));

	}

	/**
	 * 重新连接
	 * 
	 * @return
	 */
	// private int reconnect() {
	// int flag = -1;
	// try {
	// mqttClient.connect(options);
	// mqttClient.subscribe(TOPICS, QOS_VALUES);
	// flag = 0;
	// } catch (MqttException ex) {
	// log.error("reconnect:", ex);
	// flag = -1;
	// }
	// return flag;
	// }

	/**
	 * 发送消息
	 * 
	 * @param clientId
	 * @param messageId
	 */
	public void sendMessage(String clientId, String message) {
		try {

			log.debug("send message to " + clientId + ", message is " + message);
			// 发布自己的消息
			mqttClient.publish(clientId, message.getBytes(), 0, false);
		} catch (MqttException e) {
			log.error("sendMessage:", e);
		}
	}

	/**
	 * 
	 */
	public void testPublishFace() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			PIVerifyResultBean b = new PIVerifyResultBean();
			b.setResult(86);
			b.setUuid("520203199612169998");
			b.setEventDirection(2);
			b.setEventName("CAM_GetPhotoInfo");

			BufferedImage bi;

			bi = ImageIO.read(new File("C:/maven/git/pitchecking/zp.jpg"));
			byte[] biArray = ImageToolkit.getImageBytes(bi, "jpeg");
			
			byte[] bb = CommUtil.image2byte("C:/maven/git/pitchecking/zp.jpg");
			
//			b.setPhotoLen1(bb.length);
//			b.setPhoto1(bb);
//			b.setPhotoLen2(bb.length);
//			b.setPhoto2(bb);
//			b.setPhotoLen3(bb.length);
//			b.setPhoto3(bb);
			
			b.setPhotoLen1(biArray.length);
			b.setPhoto1(biArray);
			b.setPhotoLen2(biArray.length);
			b.setPhoto2(biArray);
			b.setPhotoLen3(biArray.length);
			b.setPhoto3(biArray);

			String jsonString = mapper.writeValueAsString(b);
//			log.debug("jsonString=="+jsonString);

			mqttClient.publish(SEND_TOPIC, jsonString.getBytes(), 0, false);

		} catch (IOException | MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 将人脸比对结果通过mq方式发送到CAM dll接口
	 * 
	 * @param data
	 */
	public boolean publishResult(PITVerifyData data) {
		if (data == null)
			return false;
		PIVerifyResultBean resultBean = new PIVerifyResultBean();
		resultBean.setResult((int) data.getVerifyResult());
		resultBean.setUuid(data.getIdNo());
		resultBean.setEventDirection(2);
		resultBean.setEventName("CAM_GetPhotoInfo");
		resultBean.setPhotoLen1(data.getFaceImg().length);
		resultBean.setPhotoLen2(data.getFrameImg().length);
		resultBean.setPhotoLen3(data.getIdCardImg().length);
		resultBean.setPhoto1(data.getFaceImg());
		resultBean.setPhoto2(data.getFrameImg());
		resultBean.setPhoto3(data.getFaceImg());
		
		String jsonString;
		try {
			jsonString = mapper.writeValueAsString(resultBean);
		} catch (JsonProcessingException e) {
			log.error("PIVerifyResultBean to json failed!", e);
			return false;
		}

		if (jsonString == null)
			return false;
		try {
			mqttClient.publish(SEND_TOPIC, jsonString.getBytes(), 0, false);
		} catch (MqttPersistenceException e) {
			log.error("publish json failed!", e);
			return false;
		} catch (MqttException e) {
			log.error("publish json failed!", e);
			return false;
		}
		return true;
	}

	/**
	 * 回调函数，处理server接收到的主题消息
	 * 
	 * @author ZhaoLin
	 *
	 */
	class mqttCallHandler implements MqttCallback {

		@Override
		public void connectionLost(Throwable arg0) {
			// TODO Auto-generated method stub
			log.debug("connectionLost-----------");
			// try {
			// while (true) {
			// log.debug("3s后开始尝试重新连接...");
			// Thread.sleep(3000);
			//
			// int flag = MqttServerPaho.getInstance().reconnect();
			// if (flag == 0) {
			// log.debug("重新连接mq服务成功,退出重连循环!");
			// // MqttServerPaho.getInstance().sendMessage("trainNotify",
			// // "重新连接mq服务成功,退出重连循环!");
			// break;
			// }
			// }
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken token) {
			// TODO Auto-generated method stub
			log.debug("deliveryComplete---------" + token.isComplete());
		}

		@Override
		public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
			// TODO Auto-generated method stub
			log.debug("messageArrived----------");
			log.debug("topic==" + topic);
			log.debug("mqttMessage==" + mqttMessage);
		}

	}

	public static void main(String[] args) {
		MqttSenderPaho mqttServerPaho = MqttSenderPaho.getInstance();
		mqttServerPaho.testPublishFace();

	}
}
