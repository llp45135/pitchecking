package com.rxtec.pitchecking.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.mqtt.MqttClient;
import com.ibm.mqtt.MqttException;
import com.ibm.mqtt.MqttSimpleCallback;
import com.rxtec.pitchecking.AudioPlayTask;
import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.DeviceEventListener;
import com.rxtec.pitchecking.FaceTrackingScreen;
import com.rxtec.pitchecking.ScreenCmdEnum;
import com.rxtec.pitchecking.TicketVerifyScreen;
import com.rxtec.pitchecking.device.AudioDevice;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.device.SecondGateDevice;
import com.rxtec.pitchecking.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.mq.RemoteMonitorPublisher;
import com.rxtec.pitchecking.net.event.CAMOpenBean;
import com.rxtec.pitchecking.net.event.EventHandler;
import com.rxtec.pitchecking.net.event.GatCrtlBean;
import com.rxtec.pitchecking.net.event.PIVerifyEventBean;
import com.rxtec.pitchecking.net.event.PIVerifyRequestBean;
import com.rxtec.pitchecking.net.event.ScreenDisplayBean;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.JsonUtils;
import com.rxtec.pitchecking.utils.CalUtils;

/**
 * 本类用来监听本地PITEventTopic,同GAT_RXTa.dll通信，间接控制门模块、转发铁科消息至人工处置窗
 * 
 * @author ZhaoLin
 *
 */
public class GatCtrlReceiverBroker {
	// 连接参数
	Logger log = LoggerFactory.getLogger("GatCtrlReceiverBroker");
	private final static boolean CLEAN_START = true;
	private final static short KEEP_ALIVE = 30;// 低耗网络，但是又需要及时获取数据，心跳30s
	private String CLIENT_ID = "GatCtrlReceiver";// 客户端标识
	private final static int[] QOS_VALUES = { 0 };// 对应主题的消息级别
	private final static String[] TOPICS = { "PITEventTopic" };
	private String[] unSubscribeTopics = { "PITInfoTopic" };
	// private final static String SEND_TOPIC = "sub_topic";
	private static GatCtrlReceiverBroker _instance;

	private MqttClient mqttClient;

	/**
	 * 返回实例对象
	 * 
	 * @return
	 */
	public static synchronized GatCtrlReceiverBroker getInstance(String pidname) {
		if (_instance == null)
			_instance = new GatCtrlReceiverBroker(pidname);
		return _instance;
	}

	private GatCtrlReceiverBroker(String pidname) {
		CLIENT_ID += pidname;
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
			log.error("MqttReceiverBroker reconnect", ex);
			flag = -1;
		}
		return flag;
	}

	/**
	 * 重新连接服务
	 */
	private void connect() throws MqttException {
		log.info("connect to GatCtrlReceiverBroker.");
		mqttClient = new MqttClient(DeviceConfig.getInstance().getMQTT_CONN_STR());
		log.info("***********register Simple Handler " + DeviceConfig.getInstance().getMQTT_CONN_STR() + "***********");

		SimpleCallbackHandler simpleCallbackHandler = new SimpleCallbackHandler();
		mqttClient.registerSimpleHandler(simpleCallbackHandler);// 注册接收消息方法
		mqttClient.connect(CLIENT_ID, CLEAN_START, KEEP_ALIVE);
		log.info("***********subscribe receiver topics***********");
		mqttClient.subscribe(TOPICS, QOS_VALUES);// 订阅接收主题
		mqttClient.unsubscribe(unSubscribeTopics);

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
			log.error("GatCtrlReceiverBroker sendMessage", e);
		} catch (Exception e) {
			log.error("GatCtrlReceiverBroker sendMessage", e);
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
			// log.info("订阅主题: " + topicName);
			// log.info("消息数据: " + new String(payload));
			// log.info("消息级别(0,1,2): " + Qos);
			// log.info("是否是实时发送的消息(false=实时，true=服务器上保留的最后消息): " + retained);

			String mqttMessage = new String(payload);
			log.info("mqttMessage==" + mqttMessage);
			if (topicName.equals("PITEventTopic")) {
				// log.info("mqttMessage==" + mqttMessage);
				if (mqttMessage.toLowerCase().indexOf("eventsource") != -1) { // 收到门控制发回的指令
					ObjectMapper mapper = new ObjectMapper();
					GatCrtlBean gatCrtlBean = mapper.readValue(mqttMessage.toLowerCase(), GatCrtlBean.class);
					log.debug("getEvent==" + gatCrtlBean.getEvent());
					log.debug("getTarget==" + gatCrtlBean.getTarget());
					log.debug("getEventsource==" + gatCrtlBean.getEventsource());
					if (gatCrtlBean.getEventsource().equals("manual")) {
						log.debug("来自人工窗消息");
						if (CLIENT_ID.equals("GatCtrlReceiver" + DeviceConfig.GAT_MQ_Track_CLIENT)) {
							if (Config.getInstance().getIsUseManualMQ() == 1) { // 是否连人工窗
								if (gatCrtlBean.getEvent() == DeviceConfig.Event_OpenSecondDoor) {
									AudioPlayTask.getInstance().start(DeviceConfig.checkSuccFlag); // 语音："验证成功，请通过"
								} else if (gatCrtlBean.getEvent() == DeviceConfig.Event_OpenThirdDoor) {
									AudioPlayTask.getInstance().start(DeviceConfig.emerDoorFlag); // 语音："验证失败，请从侧门离开通道"
								}
							}
						}

					} else if (gatCrtlBean.getEventsource().equals("tk")) {
						log.debug("来自铁科主控端消息");
						if (CLIENT_ID.equals("GatCtrlReceiver" + DeviceConfig.GAT_MQ_Standalone_CLIENT)) {
							if (Config.getInstance().getIsUseManualMQ() == 1) { // 是否连人工窗
								RemoteMonitorPublisher.getInstance().offerEventData(gatCrtlBean.getEvent());
							}
						}
					} else if (gatCrtlBean.getEventsource().equals("faceverify")) {
						log.debug("来自检脸程序消息");
						if (CLIENT_ID.equals("GatCtrlReceiver" + DeviceConfig.GAT_MQ_Verify_CLIENT)) {
							if (gatCrtlBean.getEvent() == DeviceConfig.Event_SecondDoorHasClosed) {
								TicketVerifyScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0,
										ScreenCmdEnum.ShowTicketDefault.getValue(), null, null, null)); // 恢复初始界面
								DeviceEventListener.getInstance().setDeviceReader(true); // 允许寻卡
								DeviceEventListener.getInstance().setDealDeviceEvent(true); // 允许处理新的事件
								log.debug("人证比对完成，第三道闸门已经关闭，重新寻卡");
							}
						}
					}
				}
			}
			payload = null;
		}

	}

	public static void main(String[] args) {
		// GatCtrlReceiverBroker mqttBroker =
		// GatCtrlReceiverBroker.getInstance("Verify");
		// GatCtrlReceiverBroker mqttBroker =
		// GatCtrlReceiverBroker.getInstance("Track");
		GatCtrlReceiverBroker mqttBroker = GatCtrlReceiverBroker.getInstance("Alone");
		System.out.println("$$");

	}
}
