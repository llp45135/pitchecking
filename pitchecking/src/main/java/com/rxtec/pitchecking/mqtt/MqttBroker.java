package com.rxtec.pitchecking.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.mqtt.MqttBrokerUnavailableException;
import com.ibm.mqtt.MqttClient;
import com.ibm.mqtt.MqttException;
import com.ibm.mqtt.MqttNotConnectedException;
import com.ibm.mqtt.MqttPersistenceException;
import com.ibm.mqtt.MqttSimpleCallback;
import com.rxtec.pitchecking.net.event.EventHandler;
import com.rxtec.pitchecking.net.event.PIVerifyEventBean;
import com.rxtec.pitchecking.utils.CommUtil;

public class MqttBroker {
	// 连接参数
	private final static String CONNECTION_STRING = "tcp://localhost:1883";
//	 private final static String CONNECTION_STRING =	 "tcp://222.51.4.186:5001";
	private final static boolean CLEAN_START = true;
	private final static short KEEP_ALIVE = 30;// 低耗网络，但是又需要及时获取数据，心跳30s
	private final static String CLIENT_ID = "PitcheckSubcriber";// 客户端标识
	private final static int[] QOS_VALUES = { 0 };// 对应主题的消息级别
	private final static String[] TOPICS = { "pub_topic" };
	private final static String SEND_TOPIC = "sub_topic";
	private static MqttBroker _instance = new MqttBroker();
	static EventHandler eventHandler = new EventHandler();

	private MqttClient mqttClient;

	/**
	 * 返回实例对象
	 * 
	 * @return
	 */
	public static synchronized MqttBroker getInstance() {
		return _instance;
	}

	private MqttBroker() {
		try {
			if (mqttClient == null || !mqttClient.isConnected()) {
				this.connect();
			}
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private int reconnect() {
		int flag = -1;
		try {
			mqttClient.connect(CLIENT_ID, CLEAN_START, KEEP_ALIVE);
			mqttClient.subscribe(TOPICS, QOS_VALUES);// 订阅接收主题
			flag = 0;
		} catch (MqttException ex) {
			System.out.println(ex);
			flag = -1;
		}
		return flag;
	}

	/**
	 * 重新连接服务
	 */
	private void connect() throws MqttException {
		System.out.println("connect to mqtt broker.");
		mqttClient = new MqttClient(CONNECTION_STRING);
		System.out.println("***********register Simple Handler***********");

		SimpleCallbackHandler simpleCallbackHandler = new SimpleCallbackHandler();
		mqttClient.registerSimpleHandler(simpleCallbackHandler);// 注册接收消息方法
		mqttClient.connect(CLIENT_ID, CLEAN_START, KEEP_ALIVE);
		System.out.println("***********subscribe receiver topics***********");
		mqttClient.subscribe(TOPICS, QOS_VALUES);// 订阅接收主题

		System.out.println("***********CLIENT_ID:" + CLIENT_ID);

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

			System.out.println("send message to " + clientId + ", message is " + message);
			// 发布自己的消息
			mqttClient.publish(clientId, message.getBytes(), 0, false);
		} catch (MqttException e) {
			System.err.println(e.getCause());
			e.printStackTrace();
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
			System.out.println("客户机和broker已经断开");

			while (true) {
				System.out.println("3s后开始尝试重新连接...");
				Thread.sleep(3000);
				int flag = MqttBroker.getInstance().reconnect();
				if (flag == 0) {
					System.out.println("重新连接mq服务成功,退出重连循环!");
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
			//System.out.println("订阅主题: " + topicName);
			//System.out.println("消息数据: " + new String(payload));
			//System.out.println("消息级别(0,1,2): " + Qos);
			//System.out.println("是否是实时发送的消息(false=实时，true=服务器上保留的最后消息): " + retained);

			String mqttMessage = new String(payload);
			//System.out.println("mqttMesssage==" + mqttMessage);
			//System.out.println("topicName==" + topicName);
			if (topicName.equals("pub_topic")) {
				if (mqttMessage.toString().indexOf("CAM_Open") != -1) {
					String ss = "{\"eventDirection\" : 2,\"eventName\" : \"CAM_Open\",\"threshold\" : 75,\"timeout\" : 3000}";
					MqttServerPaho.getInstance().sendMessage(SEND_TOPIC, ss);

				} else if (mqttMessage.toString().indexOf("CAM_Notify") != -1) {
					ObjectMapper mapper = new ObjectMapper(); 
					PIVerifyEventBean b1 = mapper.readValue(mqttMessage,PIVerifyEventBean.class);
					System.out.println(b1);
					
					byte[] photo_array = b1.getIdPhoto();
					CommUtil.byte2image(photo_array, "D:/maven/git/test.jpg");
					
					//System.out.println("$$$$$$$$$$$");
					String ss = "{ \"age\" : \"1\", \"delaySeconds\" : \"\", \"eventDirection\" : 2, \"eventName\" : \"CAM_Notify\",  \"gender\" : \"\", \"idPhoto\" : \"\", \"personName\" : \"\", \"ticket\" : { },  \"uuid\" : \"440822197908164431\"}";
					MqttServerPaho.getInstance().sendMessage(SEND_TOPIC, ss);
					
					eventHandler.InComeEventHandler(mqttMessage.toString());
				} else if (mqttMessage.toString().indexOf("CAM_GetPhotoInfo") != -1) {
					//System.out.println("^^^^^^^^^^^^^^^^^^^");
					
					//CommUtil.sleep(1000);
					MqttServerPaho.getInstance().testPublishFace();
				}
			}
			payload = null;
		}

	}

	public static void main(String[] args) {
//		MqttBroker mqttBroker = MqttBroker.getInstance();

	}
}
