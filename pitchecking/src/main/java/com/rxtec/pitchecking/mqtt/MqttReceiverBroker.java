package com.rxtec.pitchecking.mqtt;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.mqtt.MqttClient;
import com.ibm.mqtt.MqttException;
import com.ibm.mqtt.MqttSimpleCallback;
import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.net.event.CAMNotifyBean;
import com.rxtec.pitchecking.net.event.CAMOpenBean;
import com.rxtec.pitchecking.net.event.EventHandler;
import com.rxtec.pitchecking.net.event.PIVerifyRequestBean;
import com.rxtec.pitchecking.net.event.ScreenDisplayBean;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.utils.CommUtil;

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
	private String CLIENT_ID = "PIR";// 客户端标识
	private final static int[] QOS_VALUES = { 0 };// 对应主题的消息级别
	private final static String[] TOPICS = { "pub_topic" };
	private String[] unsubscribeTopics = { "sub_topic" };
	private final static String SEND_TOPIC = "sub_topic";
	private static MqttReceiverBroker _instance;
	static EventHandler eventHandler = new EventHandler();

	String pidName = "";

	private MqttClient mqttClient;

	/**
	 * 返回实例对象
	 * 
	 * @return
	 */
	public static synchronized MqttReceiverBroker getInstance(String pidname) {
		if (_instance == null)
			_instance = new MqttReceiverBroker(pidname);
		return _instance;
	}

	private MqttReceiverBroker(String pidname) {
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
			log.error("MqttReceiverBroker reconnect", ex);
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

			if (topicName.equals("pub_topic")) {
				// log.debug("mqttMessage==" + mqttMessage);
				if (mqttMessage.indexOf("CAM_Open") != -1) { // 程序启动时连接摄像头
					log.info("mqttMessage==" + mqttMessage);
					ObjectMapper mapper = new ObjectMapper();
					CAMOpenBean camOpenBean = mapper.readValue(mqttMessage, CAMOpenBean.class);

					int faceTimeout = camOpenBean.getTimeout(); // 从cam_open输出的结构体获取到总超时时间
					Config.getInstance().setFaceCheckDelayTime(faceTimeout);
					log.debug("CAM_Open faceTimeout==" + faceTimeout);//
					camOpenBean.setEventDirection(2);

					String camOpenResultJson = mapper.writeValueAsString(camOpenBean);
					MqttSenderBroker.getInstance(pidName).sendMessage(SEND_TOPIC, camOpenResultJson);
					log.info("sendMessage==" + camOpenResultJson);
				} else if (mqttMessage.indexOf("CAM_Notify") != -1) { // 收到CAM_Notify请求,里面包含身份证照片
					log.debug("--------收到CAM_Notify请求--------");

					FaceCheckingService.getInstance().setVerifyFace(true); // 允许比对线程开始取脸比对

					// 把notify-jsonstring保存起来
					MqttSenderBroker.getInstance(pidName).setNotifyJson(mqttMessage);

					// 将身份证信息通知其他进程
					GatCtrlSenderBroker.getInstance(pidName).sendMessage(DeviceConfig.EventTopic, mqttMessage);

					ObjectMapper mapper = new ObjectMapper();
					CAMNotifyBean b1 = mapper.readValue(mqttMessage, CAMNotifyBean.class);

					MqttSenderBroker.getInstance(pidName).setUuid(b1.getUuid());
					MqttSenderBroker.getInstance(pidName).setIdcardBytes(b1.getIdPhoto());

					if (Config.getInstance().getFaceVerifyType().equals(Config.FaceVerifyEASEN)) {
						String easenzp = Config.getInstance().getEasenConfigPath() + "/easenzp.jpg";
						File zpFile = new File(easenzp);
						if (zpFile.exists()) {
							zpFile.delete();
						}
						CommUtil.byte2image(b1.getIdPhoto(), easenzp);
						log.debug("易胜版本--将读取的身份证照片转存至：" + Config.getInstance().getEasenConfigPath());
					}

					b1.setEventName(Config.BeginVerifyFaceEvent);
					b1.setEventDirection(2);
					String ss = "";
					b1.setIdPhoto(ss.getBytes());

					String notifyResultJson = mapper.writeValueAsString(b1);
					log.debug("notifyResultJson==" + notifyResultJson);
					MqttSenderBroker.getInstance(pidName).sendMessage(SEND_TOPIC, notifyResultJson);

				} else if (mqttMessage.indexOf("CAM_GetPhotoInfo") != -1) {
					// System.out.println("^^^^^^^^^^^^^^^^^^^");
					ObjectMapper mapper = new ObjectMapper();
					PIVerifyRequestBean bean = mapper.readValue(mqttMessage, PIVerifyRequestBean.class);
					String idCardNo = bean.getUuid();
					log.debug("CAM_GetPhotoInfo.uuid==" + bean.getUuid());
					log.debug("CAM_GetPhotoInfo.iDelay==" + bean.getiDelay()); // 控制通过速率的延时时间
					Config.getInstance().setCheckDelayPassTime(bean.getiDelay());

					if (idCardNo != null && idCardNo.trim().length() == 18) {
						// 收到调用CAM_GetPhotoInfo方式的请求开始人脸检测
						// 通知其他进程开始检脸
						GatCtrlSenderBroker.getInstance(pidName).sendMessage(DeviceConfig.EventTopic, DeviceConfig.Event_StartTracking);

						if (DeviceConfig.getInstance().getVersionFlag() == 1) { // 正式版本时使用
							eventHandler.beginCheckFaceEventHandler(MqttSenderBroker.getInstance(pidName).getNotifyJson());
						}

						// if (DeviceConfig.getInstance().getVersionFlag() == 0)
						// { // 仅仅用于测试
						// MqttSenderBroker.getInstance(pidName).testPublishFace();
						// }
					} else {
						log.debug("########PublishWrongIDNo  身份号错误########");
						MqttSenderBroker.getInstance(pidName).PublishWrongIDNo();
					}
				} else if (mqttMessage.indexOf("CAM_ScreenDisplay") != -1) {
					ObjectMapper mapper = new ObjectMapper();
					ScreenDisplayBean screenDisplayBean = mapper.readValue(mqttMessage, ScreenDisplayBean.class);

					// 将是否同通过的屏幕信息通知其他进程
					GatCtrlSenderBroker.getInstance(pidName).sendMessage(DeviceConfig.EventTopic, mqttMessage);

					int displayTimeout = screenDisplayBean.getTimeout(); // 从CAM_ScreenDisplay输出的结构体获取到屏幕超时时间
					String faceScreenDisplay = screenDisplayBean.getScreenDisplay();
					log.debug("CAM_ScreenDisplay faceScreenDisplay==" + faceScreenDisplay);
					log.debug("CAM_ScreenDisplay displayTimeout==" + displayTimeout);

					// if (Config.getInstance().getFaceControlMode() == 1) { //
					// 后置摄像头进程才处理
					// MqttSenderBroker.getInstance(pidName).setFaceScreenDisplay(faceScreenDisplay);
					// MqttSenderBroker.getInstance(pidName).setFaceScreenDisplayTimeout(displayTimeout);
					// }

					screenDisplayBean.setEventName(Config.ScreenDisplayEvent);
					screenDisplayBean.setEventDirection(2);

					String screenDisplayResultJson = mapper.writeValueAsString(screenDisplayBean);
					MqttSenderBroker.getInstance(pidName).sendMessage(SEND_TOPIC, screenDisplayResultJson);

				}
			}
			payload = null;
		}

	}

	public static void main(String[] args) {
		MqttReceiverBroker mqttBroker = MqttReceiverBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum());
		// MqttSenderBroker.getInstance().sendMessage("pub_topic", "DoorCmd12");

	}
}
