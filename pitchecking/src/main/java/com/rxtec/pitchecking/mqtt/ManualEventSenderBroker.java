package com.rxtec.pitchecking.mqtt;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.jasypt.commons.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.mqtt.MqttClient;
import com.ibm.mqtt.MqttException;
import com.ibm.mqtt.MqttSimpleCallback;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.mbean.ProcessUtil;
import com.rxtec.pitchecking.mq.PITInfoTopicSender;
import com.rxtec.pitchecking.net.event.CAMOpenBean;
import com.rxtec.pitchecking.net.event.EventHandler;
import com.rxtec.pitchecking.net.event.PIVerifyEventBean;
import com.rxtec.pitchecking.net.event.PIVerifyResultBean;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.ImageToolkit;

/**
 * 本类用来向人工处置窗的 PITEventTopic 发送门控消息
 * @author ZhaoLin
 *
 */
public class ManualEventSenderBroker {
	// 连接参数
	Logger log = LoggerFactory.getLogger("ManualEventSenderBroker");
	private final static boolean CLEAN_START = true;
	private final static short KEEP_ALIVE = 30;// 低耗网络，但是又需要及时获取数据，心跳30s
	private String CLIENT_ID = "MES";// 客户端标识
	private final static int[] QOS_VALUES = { 0 };// 对应主题的消息级别
	private final static String[] TOPICS = { "PITEventTopic" };
//	private String[] UNSUB_TOPICS = { "sub_topic" };
	private final static String SEND_TOPIC = "PITEventTopic";
	private static ManualEventSenderBroker _instance;
	private ObjectMapper mapper = new ObjectMapper();

	private MqttClient mqttClient;
	private String notifyJson;
	private String faceScreenDisplay = "";
	private int faceScreenDisplayTimeout = 0;

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
	public static synchronized ManualEventSenderBroker getInstance(String pidname) {
		if (_instance == null)
			_instance = new ManualEventSenderBroker(pidname);
		return _instance;
	}

	private ManualEventSenderBroker(String pidname) {
		CLIENT_ID = CLIENT_ID + DeviceConfig.getInstance().getIpAddress() + pidname;
		while (true) {
			try {
				if (mqttClient == null || !mqttClient.isConnected()) {
					this.connect();
				}
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				log.error("connect failed!!");
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
			log.error("ManualEventSenderBroker reconnect failed!");
			flag = -1;
		}
		return flag;
	}

	/**
	 * 重新连接服务
	 */
	private void connect() throws MqttException {
		log.info("start connect to "+DeviceConfig.getInstance().getManualCheck_MQTTURL()+"# MyClientID==" + this.CLIENT_ID);
		mqttClient = new MqttClient(DeviceConfig.getInstance().getManualCheck_MQTTURL());

		SimpleCallbackHandler simpleCallbackHandler = new SimpleCallbackHandler();
		mqttClient.registerSimpleHandler(simpleCallbackHandler);// 注册接收消息方法
		mqttClient.connect(CLIENT_ID, CLEAN_START, KEEP_ALIVE);
		mqttClient.subscribe(TOPICS, QOS_VALUES);// 订阅接收主题
		// mqttClient.unsubscribe(UNSUB_TOPICS);

		log.info("**ManualEventSenderBroker,连接 " + DeviceConfig.getInstance().getManualCheck_MQTTURL() + " 成功**");

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
			mqttClient.publish(clientId, message.getBytes(), 0, false);
			log.debug("sendMessage ok");
		} catch (MqttException e) {
			log.error("ManualEventSenderBroker sendMessage", e);
		} catch (Exception e) {
			log.error("ManualEventSenderBroker sendMessage", e);
		}
	}

	/**
	 * 给底层发门控制指令
	 * 
	 * @param cmdstr
	 */
	public void sendDoorCmd(String cmdstr) {
		try {
			mqttClient.publish(SEND_TOPIC, cmdstr.getBytes(), 0, false);
			log.debug("sendDoorCmd ok");
		} catch (MqttException e) {
			log.error("ManualEventSenderBroker sendMessage", e);
		} catch (Exception e) {
			log.error("ManualEventSenderBroker sendMessage", e);
		}
	}
	
	/**
	 *  给底层发门控制指令
	 * @param clientId
	 * @param cmdstr
	 */
	public void sendDoorCmd(String clientId,String cmdstr) {
		try {
			mqttClient.publish(clientId, cmdstr.getBytes(), 0, false);
			log.debug("sendDoorCmd ok");
		} catch (MqttException e) {
			log.error("ManualEventSenderBroker sendMessage", e);
		} catch (Exception e) {
			log.error("ManualEventSenderBroker sendMessage", e);
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
			if (mqttMessage.indexOf("CAM_Open") != -1) {

			} 
			payload = null;
		}

	}

	public static void main(String[] args) {
		String ss = "{\"Event\": 80004,\"Target\": \""+DeviceConfig.getInstance().getIpAddress()+"\",\"EventSource\":\"Manual\"}";
//		String ss = "{\"Event\": 5,\"Target\": \""+DeviceConfig.getInstance().getIpAddress()+"\",\"EventSource\":\"Manual\"}";
		
//		String ss = "{ \"Event\": \"123321\",  \"Target\": \"192.168.85.1\",  \"EventSource\": \"Manual\",  \"ColorAutoExposure\": true,  \"ColorAutoWhiteBalance\": true,  \"ColorBackLightCompensation\": true,  \"ColorBrightness\": -16,  \"ColorExposure\": -6 }";
		ManualEventSenderBroker gatCtrlSenderBroker = ManualEventSenderBroker.getInstance("Verify");
		gatCtrlSenderBroker.sendDoorCmd(ss);
//
//		CommUtil.sleep(2000);
//		String pid = ProcessUtil.getCurrentProcessID();
//		try {
//			Runtime.getRuntime().exec("taskkill /F /PID " + pid);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//		String mqttMessage = "{\"Event\": 1,\"Target\": \"127.0.0.1\",\"EventSource\":\"FaceVerify\"}";
//		mqttMessage=mqttMessage.replace("127.0.0.1", DeviceConfig.getInstance().getIpAddress());
//		System.out.println("来自铁科主控端消息:" + mqttMessage);
	}
}
