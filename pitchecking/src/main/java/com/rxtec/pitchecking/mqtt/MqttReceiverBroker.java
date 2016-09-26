package com.rxtec.pitchecking.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.mqtt.MqttClient;
import com.ibm.mqtt.MqttException;
import com.ibm.mqtt.MqttSimpleCallback;
import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.FaceTrackingScreen;
import com.rxtec.pitchecking.ScreenCmdEnum;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.net.event.CAMOpenBean;
import com.rxtec.pitchecking.net.event.EventHandler;
import com.rxtec.pitchecking.net.event.PIVerifyEventBean;
import com.rxtec.pitchecking.net.event.PIVerifyRequestBean;
import com.rxtec.pitchecking.net.event.ScreenDisplayBean;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.CalUtils;

/**
 * 本类由人脸检测进程使用 目的是接收主控进程的检脸请求，同时发布检脸命令开始检脸 还可以接收主控进程的其他指令
 * 
 * @author ZhaoLin
 *
 */
public class MqttReceiverBroker {
	// 连接参数
	Logger log = LoggerFactory.getLogger("MqttReceiverBroker");
	private final static boolean CLEAN_START = true;
	private final static short KEEP_ALIVE = 30;// 低耗网络，但是又需要及时获取数据，心跳30s
	private final static String CLIENT_ID = "PitcheckSubcriber";// 客户端标识
	private final static int[] QOS_VALUES = { 0 };// 对应主题的消息级别
	private final static String[] TOPICS = { "pub_topic" };
	private String[] unsubscribeTopics = { "sub_topic" };
	private final static String SEND_TOPIC = "sub_topic";
	private static MqttReceiverBroker _instance = new MqttReceiverBroker();
	static EventHandler eventHandler = new EventHandler();

	private MqttClient mqttClient;

	/**
	 * 返回实例对象
	 * 
	 * @return
	 */
	public static synchronized MqttReceiverBroker getInstance() {
		if (_instance == null)
			_instance = new MqttReceiverBroker();
		return _instance;
	}

	private MqttReceiverBroker() {
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
			log.error("MqttReceiverBroker reconnect", ex);
			flag = -1;
		}
		return flag;
	}

	/**
	 * 重新连接服务
	 */
	private void connect() throws MqttException {
		log.info("connect to MqttReceiverBroker.");
		mqttClient = new MqttClient(DeviceConfig.getInstance().getMQTT_CONN_STR());
		log.info("***********register Simple Handler " + DeviceConfig.getInstance().getMQTT_CONN_STR() + "***********");

		SimpleCallbackHandler simpleCallbackHandler = new SimpleCallbackHandler();
		mqttClient.registerSimpleHandler(simpleCallbackHandler);// 注册接收消息方法
		mqttClient.connect(CLIENT_ID, CLEAN_START, KEEP_ALIVE);
		log.info("***********subscribe receiver topics***********");
		mqttClient.subscribe(TOPICS, QOS_VALUES);// 订阅接收主题
		mqttClient.unsubscribe(unsubscribeTopics);

		log.info("***********CLIENT_ID:" + CLIENT_ID);

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
			log.error("MqttBroker sendMessage", e);
		} catch (Exception e) {
			log.error("MqttBroker sendMessage", e);
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
				int flag = MqttReceiverBroker.getInstance().reconnect();
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
			// log.info("订阅主题: " + topicName);
			// log.info("消息数据: " + new String(payload));
			// log.info("消息级别(0,1,2): " + Qos);
			// log.info("是否是实时发送的消息(false=实时，true=服务器上保留的最后消息): " + retained);

			String mqttMessage = new String(payload);

			if (topicName.equals("pub_topic")) {
				if (mqttMessage.indexOf("CAM_Open") != -1) {
					ObjectMapper mapper = new ObjectMapper();
					CAMOpenBean camOpenBean = mapper.readValue(mqttMessage, CAMOpenBean.class);
					int faceTimeout = camOpenBean.getTimeout(); // 从cam_open输出的结构体获取到总超时时间
					Config.getInstance().setFaceCheckDelayTime(faceTimeout);
					log.info("CAM_Open faceTimeout==" + faceTimeout);

					camOpenBean.setEventDirection(2);
					String camOpenResultJson = mapper.writeValueAsString(camOpenBean);
					MqttSenderBroker.getInstance().sendMessage(SEND_TOPIC, camOpenResultJson);

				} else if (mqttMessage.indexOf("CAM_Notify") != -1) {
					MqttSenderBroker.getInstance().setNotifyJson(mqttMessage); // 把notify保存起来

					ObjectMapper mapper = new ObjectMapper();
					PIVerifyEventBean b1 = mapper.readValue(mqttMessage, PIVerifyEventBean.class);
					b1.setEventDirection(2);
					String ss = "";
					b1.setIdPhoto(ss.getBytes());

					String notifyResultJson = mapper.writeValueAsString(b1);
					MqttSenderBroker.getInstance().sendMessage(SEND_TOPIC, notifyResultJson);

				} else if (mqttMessage.indexOf("CAM_GetPhotoInfo") != -1) {
					// System.out.println("^^^^^^^^^^^^^^^^^^^");
					ObjectMapper mapper = new ObjectMapper();
					PIVerifyRequestBean bean = mapper.readValue(mqttMessage, PIVerifyRequestBean.class);
					String idCardNo = bean.getUuid();
					log.info("CAM_GetPhotoInfo.uuid==" + bean.getUuid());
					log.info("CAM_GetPhotoInfo.iDelay==" + bean.getiDelay()); // 控制通过速率的延时时间
					Config.getInstance().setCheckDelayPassTime(bean.getiDelay());

					if (idCardNo != null && idCardNo.trim().length() == 18) {
						// 收到调用CAM_GetPhotoInfo方式的请求开始人脸检测
						if (DeviceConfig.getInstance().getVersionFlag() == 1) {
							eventHandler.InComeEventHandler(MqttSenderBroker.getInstance().getNotifyJson());
						}

						if (DeviceConfig.getInstance().getVersionFlag() == 0) {
							MqttSenderBroker.getInstance().testPublishFace();
						}
					} else {
						MqttSenderBroker.getInstance().PublishWrongIDNo();
					}
				} else if (mqttMessage.indexOf("CAM_ScreenDisplay") != -1) {
//					log.info("CAM_ScreenDisplay json==" + mqttMessage);
					ObjectMapper mapper = new ObjectMapper();
					ScreenDisplayBean screenDisplayBean = mapper.readValue(mqttMessage, ScreenDisplayBean.class);
					int displayTimeout = screenDisplayBean.getTimeout(); // 从CAM_ScreenDisplay输出的结构体获取到屏幕超时时间
					String faceScreenDisplay = screenDisplayBean.getScreenDisplay();
					log.info("CAM_ScreenDisplay faceScreenDisplay==" + faceScreenDisplay);
					log.info("CAM_ScreenDisplay displayTimeout==" + displayTimeout);
					
					MqttSenderBroker.getInstance().setFaceScreenDisplay(faceScreenDisplay);
					MqttSenderBroker.getInstance().setFaceScreenDisplayTimeout(displayTimeout);

					screenDisplayBean.setEventDirection(2);
					String screenDisplayResultJson = mapper.writeValueAsString(screenDisplayBean);
					MqttSenderBroker.getInstance().sendMessage(SEND_TOPIC, screenDisplayResultJson);
					
					FaceTrackingScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(1, ScreenCmdEnum.ShowFaceDisplayFromTK.getValue(), null, null, null));
				}
			}
			payload = null;
		}

	}

	public static void main(String[] args) {
		MqttReceiverBroker mqttBroker = MqttReceiverBroker.getInstance();

	}
}
