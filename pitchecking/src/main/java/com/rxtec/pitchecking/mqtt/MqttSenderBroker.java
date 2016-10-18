package com.rxtec.pitchecking.mqtt;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.mqtt.MqttClient;
import com.ibm.mqtt.MqttException;
import com.ibm.mqtt.MqttSimpleCallback;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.net.event.CAMOpenBean;
import com.rxtec.pitchecking.net.event.EventHandler;
import com.rxtec.pitchecking.net.event.PIVerifyEventBean;
import com.rxtec.pitchecking.net.event.PIVerifyResultBean;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.ImageToolkit;

/**
 * 本类由人脸检测进程使用 同CAM_RXTa.dll通信，发送回执到sub_topic
 * 
 * @author ZhaoLin
 *
 */
public class MqttSenderBroker {
	// 连接参数
	Logger log = LoggerFactory.getLogger("MqttSenderBroker");
	private final static boolean CLEAN_START = true;
	private final static short KEEP_ALIVE = 30;// 低耗网络，但是又需要及时获取数据，心跳30s
	private final static String CLIENT_ID = "ResultSubcriber";// 客户端标识
	private final static int[] QOS_VALUES = { 0 };// 对应主题的消息级别
	private final static String[] TOPICS = { "sub_topic" };
	private String[] UNSUB_TOPICS = { "sub_topic" };
	private final static String SEND_TOPIC = "sub_topic";
	private static MqttSenderBroker _instance = new MqttSenderBroker();
	private ObjectMapper mapper = new ObjectMapper();

	private MqttClient mqttClient;
	private String notifyJson;
	private String faceScreenDisplay = "";
	private int faceScreenDisplayTimeout =0;

	public String getFaceScreenDisplay() {
		return faceScreenDisplay;
	}

	public void setFaceScreenDisplay(String faceScreenDisplay) {
		this.faceScreenDisplay = faceScreenDisplay;
	}

	public int getFaceScreenDisplayTimeout() {
		return faceScreenDisplayTimeout;
	}

	public void setFaceScreenDisplayTimeout(int faceScreenDisplayTimeout) {
		this.faceScreenDisplayTimeout = faceScreenDisplayTimeout;
	}

	public String getNotifyJson() {
		return notifyJson;
	}

	public void setNotifyJson(String notifyJson) {
		this.notifyJson = notifyJson;
	}

	/**
	 * 返回实例对象
	 * 
	 * @return
	 */
	public static synchronized MqttSenderBroker getInstance() {
		return _instance;
	}

	private MqttSenderBroker() {
		while (true) {
			try {
				if (mqttClient == null || !mqttClient.isConnected()) {
					this.connect();
				}
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				log.error("connect:",e);
				CommUtil.sleep(5000);
				continue;
			}
			break;
		}
	}

	private int reconnect() {
		int flag = -1;
		try {
			mqttClient.connect(CLIENT_ID, CLEAN_START, KEEP_ALIVE);
			mqttClient.subscribe(TOPICS, QOS_VALUES);// 订阅接收主题
			flag = 0;
		} catch (MqttException ex) {
			log.error("MqttSenderBroker reconnect", ex);
			flag = -1;
		}
		return flag;
	}

	/**
	 * 重新连接服务
	 */
	private void connect() throws MqttException {
		
		log.debug("connect to MqttSenderBroker.");
		mqttClient = new MqttClient(DeviceConfig.getInstance().getMQTT_CONN_STR());
		log.debug(
				"***********register Simple Handler " + DeviceConfig.getInstance().getMQTT_CONN_STR() + "***********");

		SimpleCallbackHandler simpleCallbackHandler = new SimpleCallbackHandler();
		mqttClient.registerSimpleHandler(simpleCallbackHandler);// 注册接收消息方法
		mqttClient.connect(CLIENT_ID, CLEAN_START, KEEP_ALIVE);
		log.debug("***********subscribe receiver topics***********");
		mqttClient.subscribe(TOPICS, QOS_VALUES);// 订阅接收主题
		// mqttClient.unsubscribe(UNSUB_TOPICS);

		log.debug("***********CLIENT_ID:" + CLIENT_ID);

		// /**
		// * 完成订阅后，可以增加心跳，保持网络通畅，也可以发布自己的消息
		// */
		// mqttClient.publish("keepalive", "keepalive".getBytes(),
		// QOS_VALUES[0], true);// 增加心跳，保持网络通畅
	}

	/**
	 * 发送消息
	 * 
	 * @param clientId
	 * @param messageId
	 */
	public void sendMessage(String clientId, String message) {
		try {

			// log.debug("send message to " + clientId + ", message is " +
			// message);
			// 发布自己的消息
			if(message.indexOf("CAM_ScreenDisplay")!=-1){
				log.info("CAM_ScreenDisplay:send message=="+message);
			}
			mqttClient.publish(clientId, message.getBytes(), 0, false);
			log.debug("sendMessage ok");
		} catch (MqttException e) {
			log.error("MqttSenderBroker sendMessage", e);
		} catch (Exception e) {
			log.error("MqttSenderBroker sendMessage", e);
		}
	}

	/**
	 * 当主控端传入的uuid，即身份证号为空时，立即返回检脸失败
	 */
	public void PublishWrongIDNo() {
		log.info("########PublishWrongIDNo  身份号错误########");
		try {
			ObjectMapper mapper = new ObjectMapper();
			PIVerifyResultBean PIVBean = new PIVerifyResultBean();
			PIVBean.setResult(0);
			PIVBean.setUuid("");
			PIVBean.setEventDirection(2);
			PIVBean.setEventName("CAM_GetPhotoInfo");

			String ww = "";
			PIVBean.setPhoto1(ww.getBytes());
			PIVBean.setPhoto2(ww.getBytes());
			PIVBean.setPhoto3(ww.getBytes());

			PIVBean.setVerifyStatus(1);

			String jsonString = mapper.writeValueAsString(PIVBean);
			log.info("PublishWrongIDNo json=="+jsonString);

			mqttClient.publish(SEND_TOPIC, jsonString.getBytes(), 0, false);

		} catch (IOException | MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 测试用例
	 */
	public void testPublishFace() {
		log.info("testPublishFace###############");
		try {
			ObjectMapper mapper = new ObjectMapper();
			PIVerifyResultBean PIVBean = new PIVerifyResultBean();
			PIVBean.setResult(86);
			PIVBean.setUuid("520203199612169998");
			PIVBean.setEventDirection(2);
			PIVBean.setEventName("CAM_GetPhotoInfo");

			BufferedImage bi;

			bi = ImageIO.read(new File("wxs.jpg"));
			byte[] biArray = ImageToolkit.getImageBytes(bi, "jpg");

			byte[] bb = CommUtil.image2byte("face1.jpg");

			PIVBean.setPhotoLen1(bb.length);
			PIVBean.setPhoto1(bb);
			PIVBean.setPhotoLen2(bb.length);
			PIVBean.setPhoto2(bb);
			PIVBean.setPhotoLen3(bb.length);
			PIVBean.setPhoto3(bb);
			log.debug("length==" + bb.length);

			// PIVBean.setPhotoLen1(biArray.length);
			// PIVBean.setPhoto1(biArray);
			// PIVBean.setPhotoLen2(biArray.length);
			// PIVBean.setPhoto2(biArray);
			// PIVBean.setPhotoLen3(biArray.length);
			// PIVBean.setPhoto3(biArray);
			// log.debug("length==" + biArray.length);

			PIVBean.setVerifyStatus(0);

			String jsonString = mapper.writeValueAsString(PIVBean);

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
	public boolean publishResult(PITVerifyData data, int verifyStatus) {
		log.info("********准备发送人脸比对结果到DLL by MQ********verifyStatus==" + verifyStatus);
		log.info("PITVerifyData==" + data);
		if (data == null)
			return false;
		PIVerifyResultBean resultBean = new PIVerifyResultBean();
		float faceResult = 1f;
		try {
			faceResult = CommUtil.round(2, data.getVerifyResult());
			// log.info("faceResult==" + (int) (faceResult * 100));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			log.error("faceResult round:", e1);
		}
		resultBean.setResult((int) (faceResult * 100));
		resultBean.setUuid(data.getIdNo());
		resultBean.setiDelay(com.rxtec.pitchecking.Config.getInstance().getCheckDelayPassTime());
		resultBean.setEventDirection(2);
		resultBean.setEventName("CAM_GetPhotoInfo");
		resultBean.setPhoto1(data.getFaceImg()); // for debug
		resultBean.setPhoto2(data.getFrameImg());
		String photo3str = "";
		resultBean.setPhoto3(photo3str.getBytes());
		resultBean.setVerifyStatus(verifyStatus);

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
			// log.info("jsonString==" + jsonString);
			mqttClient.publish(SEND_TOPIC, jsonString.getBytes(), 0, false);
			log.info("##########人脸比对结果发送结束##########");
		} catch (MqttException e) {
			log.error("publish json failed!", e);
			return false;
		}
		return true;
	}

	/**
	 * 简单回调函数，处理server接收到的主题消息
	 * 
	 * @author Join
	 * 
	 */
	class SimpleCallbackHandler implements MqttSimpleCallback {

		/**
		 * 当客户机和broker意外断开时触发 可以再此处理重新订阅
		 */
		@Override
		public void connectionLost() throws Exception {
			// TODO Auto-generated method stub
			log.debug("客户机和broker已经断开");

			while (true) {
				log.debug("3s后开始尝试重新连接...");
				Thread.sleep(3000);
				int flag = MqttSenderBroker.getInstance().reconnect();
				if (flag == 0) {
					log.debug("重新连接mq服务成功,退出重连循环!");
					break;
				}
			}
		}

		/**
		 * 客户端订阅消息后，该方法负责回调接收处理消息
		 */
		@Override
		public void publishArrived(String topicName, byte[] payload, int Qos, boolean retained) throws Exception {
			// TODO Auto-generated method stub
//			log.debug("订阅主题: " + topicName);
			// log.debug("消息数据: " + new String(payload));
			// log.debug("消息级别(0,1,2): " + Qos);
//			log.debug("是否是实时发送的消息(false=实时，true=服务器上保留的最后消息): " + retained);

			String mqttMessage = new String(payload);
			if (mqttMessage.indexOf("CAM_Open") != -1) {

			} else if (mqttMessage.indexOf("CAM_Notify") != -1) {

			} else if (mqttMessage.indexOf("CAM_GetPhotoInfo") != -1) {
				ObjectMapper mapper = new ObjectMapper();
				PIVerifyResultBean b = mapper.readValue(mqttMessage, PIVerifyResultBean.class);
				log.debug("eventName==" + b.getEventName());
				log.debug("uuid==" + b.getUuid());
				log.debug("photoLen1==" + b.getPhotoLen1());
				if (b.getPhoto1() != null) {
					log.debug("getPhoto1().length==" + b.getPhoto1().length);
				}
				log.debug("photoLen2==" + b.getPhotoLen2());
				if (b.getPhoto2() != null) {
					log.debug("getPhoto2().length==" + b.getPhoto2().length);
				}
				log.debug("photoLen3==" + b.getPhotoLen3());
				if (b.getPhoto3() != null) {
					log.debug("getPhoto3().length==" + b.getPhoto3().length);
				}
				log.debug("verifyStatus==" + b.getVerifyStatus());
			}
			payload = null;
		}

	}

	public static void main(String[] args) {
		// MqttBroker mqttBroker = MqttBroker.getInstance();

		String ss = "";
		System.out.println("ss.bytes==" + ss.getBytes().length);

	}
}
