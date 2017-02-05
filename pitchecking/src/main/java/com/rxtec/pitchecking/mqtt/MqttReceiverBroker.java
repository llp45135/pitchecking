package com.rxtec.pitchecking.mqtt;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.mqtt.MqttClient;
import com.ibm.mqtt.MqttException;
import com.ibm.mqtt.MqttSimpleCallback;
import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.FaceTrackingScreen;
import com.rxtec.pitchecking.ScreenCmdEnum;
import com.rxtec.pitchecking.SingleFaceTrackingScreen;
import com.rxtec.pitchecking.device.AudioDevice;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.mbean.ProcessUtil;
import com.rxtec.pitchecking.net.event.CAMOpenBean;
import com.rxtec.pitchecking.net.event.EventHandler;
import com.rxtec.pitchecking.net.event.PIVerifyEventBean;
import com.rxtec.pitchecking.net.event.PIVerifyRequestBean;
import com.rxtec.pitchecking.net.event.ScreenDisplayBean;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.JsonUtils;
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
	Logger logTrack = LoggerFactory.getLogger("RSFaceTrackTask");
	private final static boolean CLEAN_START = true;
	private final static short KEEP_ALIVE = 30;// 低耗网络，但是又需要及时获取数据，心跳30s
	private String CLIENT_ID = "PIR";// 客户端标识
	private final static int[] QOS_VALUES = { 0 };// 对应主题的消息级别
	private final static String[] TOPICS = { "pub_topic" };
	private String[] unsubscribeTopics = { "sub_topic" };
	private final static String SEND_TOPIC = "sub_topic";
	private static MqttReceiverBroker _instance;
	static EventHandler eventHandler = new EventHandler();

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
		log.info("start connect to " + DeviceConfig.getInstance().getMQTT_CONN_STR() + "# MyClientID=="
				+ this.CLIENT_ID);
		mqttClient = new MqttClient(DeviceConfig.getInstance().getMQTT_CONN_STR());

		SimpleCallbackHandler simpleCallbackHandler = new SimpleCallbackHandler();
		mqttClient.registerSimpleHandler(simpleCallbackHandler);// 注册接收消息方法
		mqttClient.connect(CLIENT_ID, CLEAN_START, KEEP_ALIVE);
		mqttClient.subscribe(TOPICS, QOS_VALUES);// 订阅接收主题
		mqttClient.unsubscribe(unsubscribeTopics);

		log.info("**" + this.CLIENT_ID + " 连接 " + DeviceConfig.getInstance().getMQTT_CONN_STR() + " 成功**");

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

			if (topicName.equals("pub_topic")) {
				// log.debug("mqttMessage==" + mqttMessage);
				if (mqttMessage.indexOf("CAM_Open") != -1) {
					ObjectMapper mapper = new ObjectMapper();
					CAMOpenBean camOpenBean = mapper.readValue(mqttMessage, CAMOpenBean.class);

					// CAMOpenBean camOpenBean = new CAMOpenBean();
					// camOpenBean = (CAMOpenBean)
					// JsonUtils.toJavaBean(camOpenBean,
					// JsonUtils.toMap(mqttMessage));
					int faceTimeout = camOpenBean.getTimeout(); // 从cam_open输出的结构体获取到总超时时间
					Config.getInstance().setFaceCheckDelayTime(faceTimeout);
					log.info("CAM_Open faceTimeout==" + faceTimeout);//
					camOpenBean.setEventDirection(2);

					String camOpenResultJson = mapper.writeValueAsString(camOpenBean);
					// String camOpenResultJson = "{\"eventDirection\" :
					// 2,\"eventName\" : \"CAM_Open\", \"threshold\" : 77,
					// \"timeout\" : 3000}";

					// String camOpenResultJson =
					// JsonUtils.toJSON(camOpenBean).toString();
					MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT).sendMessage(SEND_TOPIC,
							camOpenResultJson);

				} else if (mqttMessage.indexOf("CAM_Notify") != -1) {
					log.info("@@@@@@@@@@@@@@@收到CAM_Notify请求@@@@@@@@@@@@@@@@");

					MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT).setNotifyJson(mqttMessage); // 把notify
																												// jsonstring保存起来

					ObjectMapper mapper = new ObjectMapper();
					PIVerifyEventBean b1 = mapper.readValue(mqttMessage, PIVerifyEventBean.class);

					MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT).setUuid(b1.getUuid());
					MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT).setIdcardBytes(b1.getIdPhoto());

					if (Config.getInstance().getFaceVerifyType().equals(Config.FaceVerifyEASEN)) {
						String easenzp = Config.getInstance().getEasenConfigPath() + "/easenzp.jpg";
						File zpFile = new File(easenzp);
						if (zpFile.exists()) {
							zpFile.delete();
						}
						CommUtil.byte2image(b1.getIdPhoto(), easenzp);
						log.info("易胜版本--将读取的身份证照片转存至：" + Config.getInstance().getEasenConfigPath());
					}

					b1.setEventName(Config.BeginVerifyFaceEvent);
					b1.setEventDirection(2);
					String ss = "";
					b1.setIdPhoto(ss.getBytes());

					String notifyResultJson = mapper.writeValueAsString(b1);
					log.info("notifyResultJson==" + notifyResultJson);
					MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT).sendMessage(SEND_TOPIC,
							notifyResultJson);

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
							eventHandler.InComeEventHandler(
									MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT).getNotifyJson());
						}

						if (DeviceConfig.getInstance().getVersionFlag() == 0) {
							MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT).testPublishFace();
						}
					} else {
						log.info("########PublishWrongIDNo  身份号错误########");
						MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT).PublishWrongIDNo();
					}
				} else if (mqttMessage.indexOf("CAM_ScreenDisplay") != -1) {
					ObjectMapper mapper = new ObjectMapper();
					ScreenDisplayBean screenDisplayBean = mapper.readValue(mqttMessage, ScreenDisplayBean.class);
					int displayTimeout = screenDisplayBean.getTimeout(); // 从CAM_ScreenDisplay输出的结构体获取到屏幕超时时间
					String faceScreenDisplay = screenDisplayBean.getScreenDisplay();
					log.info("CAM_ScreenDisplay faceScreenDisplay==" + faceScreenDisplay);
					log.info("CAM_ScreenDisplay displayTimeout==" + displayTimeout);

					MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT)
							.setFaceScreenDisplay(faceScreenDisplay);
					MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT)
							.setFaceScreenDisplayTimeout(displayTimeout);

					screenDisplayBean.setEventName(Config.ScreenDisplayEvent);
					screenDisplayBean.setEventDirection(2);

					String screenDisplayResultJson = mapper.writeValueAsString(screenDisplayBean);
					MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT).sendMessage(SEND_TOPIC,
							screenDisplayResultJson);

					// 语音："验证成功，请通过"
					if (faceScreenDisplay.indexOf("成功") != -1) {
						log.info("播放语音：验证成功，请通过");
						MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT).sendMessage("PITEventTopic",
								ProcessUtil.createAudioJson(DeviceConfig.AudioCheckSuccFlag, "FaceAudio"));
					}

					if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {
						FaceTrackingScreen.getInstance().offerEvent(new ScreenElementModifyEvent(1,
								ScreenCmdEnum.ShowFaceDisplayFromTK.getValue(), null, null, null));
					} else {
						SingleFaceTrackingScreen.getInstance().offerEvent(new ScreenElementModifyEvent(1,
								ScreenCmdEnum.ShowFaceDisplayFromTK.getValue(), null, null, null));
					}
				}
			}
			payload = null;
		}

	}

	public static void main(String[] args) {
		MqttReceiverBroker mqttBroker = MqttReceiverBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT);
		// MqttSenderBroker.getInstance().sendMessage("pub_topic", "DoorCmd12");

	}
}
