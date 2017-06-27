package com.rxtec.pitchecking.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.mqtt.MqttClient;
import com.ibm.mqtt.MqttException;
import com.ibm.mqtt.MqttSimpleCallback;
import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.DeviceEventListener;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.net.event.CAMNotifyBean;
import com.rxtec.pitchecking.net.event.CAMOpenBean;
import com.rxtec.pitchecking.net.event.PIVerifyResultBean;
import com.rxtec.pitchecking.net.event.ScreenDisplayBean;
import com.rxtec.pitchecking.utils.CommUtil;

/**
 * 本类由JAVA主控进程使用，目的是接收比对进程的人脸比对结果
 * 
 * @author ZhaoLin
 *
 */
public class MqttCamResponseReceiver {
	// 连接参数
	Logger log = LoggerFactory.getLogger("DeviceEventListener");
	private final static boolean CLEAN_START = true;
	private final static short KEEP_ALIVE = 30;// 低耗网络，但是又需要及时获取数据，心跳30s
	private String CLIENT_ID = "CRR";// 客户端标识
	private final static int[] QOS_VALUES = { 0 };// 对应主题的消息级别
	private final static String[] TOPICS = { "sub_topic" };
	private String[] unsubscribeTopics = { "pub_topic" };
	private static MqttCamResponseReceiver _instance;

	String pidName = "";

	private MqttClient mqttClient;

	/**
	 * 返回实例对象
	 * 
	 * @return
	 */
	public static synchronized MqttCamResponseReceiver getInstance(String pidname) {
		if (_instance == null)
			_instance = new MqttCamResponseReceiver(pidname);
		return _instance;
	}

	private MqttCamResponseReceiver(String pidname) {
		this.pidName = pidname;
		CLIENT_ID = CLIENT_ID + DeviceConfig.getInstance().getIpAddress() + pidname;
		while (true) {
			try {
				if (mqttClient == null || !mqttClient.isConnected()) {
					this.connect();
				}
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				log.error("connect:", e);
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
			log.error("MqttCamResponseReceiver reconnect", ex);
			flag = -1;
		}
		return flag;
	}

	/**
	 * 重新连接服务
	 */
	private void connect() throws MqttException {
		log.info("start connect to " + DeviceConfig.getInstance().getMQTT_CONN_STR() + "# MyClientID==" + this.CLIENT_ID);
		mqttClient = new MqttClient(DeviceConfig.getInstance().getMQTT_CONN_STR());

		SimpleCallbackHandler simpleCallbackHandler = new SimpleCallbackHandler();
		mqttClient.registerSimpleHandler(simpleCallbackHandler);// 注册接收消息方法
		mqttClient.connect(CLIENT_ID, CLEAN_START, KEEP_ALIVE);
		mqttClient.subscribe(TOPICS, QOS_VALUES);// 订阅接收主题
		mqttClient.unsubscribe(unsubscribeTopics);

		log.debug("**" + this.CLIENT_ID + " 连接 " + DeviceConfig.getInstance().getMQTT_CONN_STR() + " 成功**");

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

			log.debug("send message to " + clientId + ", message is " + message);
			// 发布自己的消息
			mqttClient.publish(clientId, message.getBytes(), 0, false);
		} catch (MqttException e) {
			log.error("MqttCamResponseReceiver sendMessage", e);
		} catch (Exception e) {
			log.error("MqttCamResponseReceiver sendMessage", e);
		}
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
			log.info("客户机和broker已经断开");

			while (true) {
				log.debug("3s后开始尝试重新连接...");
				Thread.sleep(3000);
				int flag = reconnect();
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
			// log.debug("订阅主题: " + topicName);
			// log.debug("消息数据: " + new String(payload));
			// log.debug("消息级别(0,1,2): " + Qos);
			// log.debug("是否是实时发送的消息(false=实时，true=服务器上保留的最后消息): " + retained);

			String mqttMessage = new String(payload);
			// log.info("mqttMessage==" + mqttMessage);
			if (topicName.equals("sub_topic")) {
				if (mqttMessage.indexOf("CAM_Open") != -1) { // 程序启动时连接摄像头
					ObjectMapper mapper = new ObjectMapper();
					CAMOpenBean bean = mapper.readValue(mqttMessage, CAMOpenBean.class);
					log.info("CAM_Open回复：EventName = " + bean.getEventName() + ",EventDirection = " + bean.getEventDirection());
					DeviceEventListener.getInstance().offerCamOpenDataQueue(bean);
				} else if (mqttMessage.indexOf("CAM_Notify") != -1) { // 收到CAM_Notify请求,里面包含身份证照片
					ObjectMapper mapper = new ObjectMapper();
					CAMNotifyBean bean = mapper.readValue(mqttMessage, CAMNotifyBean.class);
					log.info("CAM_Notify回复：EventName = " + bean.getEventName() + ",EventDirection = " + bean.getEventDirection());
					DeviceEventListener.getInstance().offerCamNotifyDataQueue(bean);
				} else if (mqttMessage.indexOf("CAM_GetPhotoInfo") != -1) {
					ObjectMapper mapper = new ObjectMapper();
					PIVerifyResultBean bean = mapper.readValue(mqttMessage, PIVerifyResultBean.class);
					DeviceEventListener.getInstance().setFaceVerifyResult(bean);
					log.info("CAM_GetPhotoInfo回复：EventName = " + bean.getEventName() + ",EventDirection = " + bean.getEventDirection() + ",photoLen1 = " + bean.getPhotoLen1()
							+ ",photoLen2 = " + bean.getPhotoLen2() + ",photoLen3 = " + bean.getPhotoLen3() + ",分数 = " + bean.getResult());
					DeviceEventListener.getInstance().offerCamGetPhotoInfoQueue(bean);
				} else if (mqttMessage.indexOf("CAM_ScreenDisplay") != -1) {
					ObjectMapper mapper = new ObjectMapper();
					ScreenDisplayBean bean = mapper.readValue(mqttMessage, ScreenDisplayBean.class);
					log.info("CAM_ScreenDisplay回复：EventName = " + bean.getEventName() + ",EventDirection = " + bean.getEventDirection());
					DeviceEventListener.getInstance().offerCamScreenDisplayQueue(bean);
				}
			}
			payload = null;
		}

	}

	public static void main(String[] args) {
		MqttCamResponseReceiver mqttBroker = MqttCamResponseReceiver.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum());
		// MqttSenderBroker.getInstance().sendMessage("pub_topic", "DoorCmd12");

	}
}
