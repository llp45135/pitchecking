package com.rxtec.pitchecking.mqtt.pitevent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.mqtt.MqttClient;
import com.ibm.mqtt.MqttException;
import com.ibm.mqtt.MqttSimpleCallback;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.utils.CommUtil;

/**
 * 本类由人脸检测进程使用 ,发送待验证人脸至PTVerify主题
 * 
 * @author ZhaoLin
 *
 */
public class PTVerifySender {
	// 连接参数
	Logger log = LoggerFactory.getLogger("PTVerifySender");
	private final static boolean CLEAN_START = true;
	private final static short KEEP_ALIVE = 30;// 低耗网络，但是又需要及时获取数据，心跳30s
	private String CLIENT_ID = "PTVS";// 客户端标识
	private final static int[] QOS_VALUES = { 0 };// 对应主题的消息级别
	private final static String[] TOPICS = { "PTVerify" };
	private String[] UNSUB_TOPICS = { "PTVerifyResult" };
	private final static String SEND_TOPIC = "PTVerify";
	private static PTVerifySender _instance;
	private ObjectMapper mapper = new ObjectMapper();

	private MqttClient mqttClient;

	/**
	 * 返回实例对象
	 * 
	 * @return
	 */
	public static synchronized PTVerifySender getInstance(String pidname) {
		if (_instance == null)
			_instance = new PTVerifySender(pidname);
		return _instance;
	}

	private PTVerifySender(String pidname) {
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
			log.error("PTVerifySender reconnect", ex);
			flag = -1;
		}
		return flag;
	}

	/**
	 * 重新连接服务
	 */
	private void connect() throws MqttException {
		log.info("start connect to " + DeviceConfig.getInstance().getMQTT_CONN_STR() + "# MyClientID=="
				+ this.CLIENT_ID);
		log.info("connect to PTVerifySender.");
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
	public void sendMessage(String clientId, byte[] pitDataBytes) {
		try {

			// log.debug("send message to " + clientId + ", message is " +
			// message);
			// 发布自己的消息
			mqttClient.publish(clientId, pitDataBytes, 0, false);
			// log.debug("sendMessage ok");
		} catch (MqttException e) {
			log.error("PTVerifySender sendMessage", e);
		} catch (Exception e) {
			log.error("PTVerifySender sendMessage", e);
		}
	}

	/**
	 * 发送消息
	 * 
	 * @param clientId
	 * @param messageId
	 */
	public void sendMessage(byte[] pitDataBytes) {
		try {

			// log.debug("send message to " + clientId + ", message is " +
			// message);
			// 发布自己的消息
			mqttClient.publish(SEND_TOPIC, pitDataBytes, 0, false);
			// log.debug("sendMessage ok");
		} catch (MqttException e) {
			log.error("PTVerifySender sendMessage", e);
		} catch (Exception e) {
			log.error("PTVerifySender sendMessage", e);
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
			log.debug("客户机和broker已经断开");

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

			payload = null;
		}

	}

	public static void main(String[] args) {
		// MqttBroker mqttBroker = MqttBroker.getInstance();

		String ss = "";
		System.out.println("ss.bytes==" + ss.getBytes().length);

	}
}
