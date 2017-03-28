package com.rxtec.pitchecking.mqtt.pitevent;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.jfree.util.Log;
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
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.JsonUtils;
import com.rxtec.pitchecking.utils.CalUtils;

/**
 * 本类由人脸比对进程使用，目的是从PTVerify主题接收待验证队列
 * 
 * @author ZhaoLin
 *
 */
public class PTVerifyReceiver {
	// 连接参数
	Logger log = LoggerFactory.getLogger("PTVerifyReceiver");
	private final static boolean CLEAN_START = true;
	private final static short KEEP_ALIVE = 30;// 低耗网络，但是又需要及时获取数据，心跳30s
	private String CLIENT_ID = "PTVR";// 客户端标识
	private final static int[] QOS_VALUES = { 0 };// 对应主题的消息级别
	private final static String[] TOPICS = { "PTVerify" };
	private String[] unsubscribeTopics = { "PTVerifyResult" };
	private final static String SEND_TOPIC = "PTVerify";
	private static PTVerifyReceiver _instance;

	private MqttClient mqttClient;

	/**
	 * 返回实例对象
	 * 
	 * @return
	 */
	public static synchronized PTVerifyReceiver getInstance(String pidname) {
		if (_instance == null)
			_instance = new PTVerifyReceiver(pidname);
		return _instance;
	}

	private PTVerifyReceiver(String pidname) {
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
			log.error("PTVerifyReceiver reconnect", ex);
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
	public void sendMessage(String clientId, byte[] pitDataBytes) {
		try {

			// log.debug("send message to " + clientId + ", message is " +
			// message);
			// 发布自己的消息
			mqttClient.publish(clientId, pitDataBytes, 0, false);
			// log.debug("sendMessage ok");
		} catch (MqttException e) {
			log.error("PTVerifyReceiver sendMessage", e);
		} catch (Exception e) {
			log.error("PTVerifyReceiver sendMessage", e);
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

			if (topicName.equals("PTVerify")) {
				// log.debug("mqttMessage==" + mqttMessage);
				ByteArrayInputStream bais = null;
				ObjectInputStream ois = null;
				try {
					bais = new ByteArrayInputStream(payload);
					ois = new ObjectInputStream(bais);
					PITVerifyData fd = (PITVerifyData) ois.readObject();
					/**
					 * 收到第一个后置摄像头送来的人脸时，首先做一次清理
					 */
					if (DeviceConfig.getInstance().isInTracking()) {
						if (!FaceCheckingService.getInstance().isReceiveBehindCameraFace()
								&& fd.getCameraPosition() == 2) {
							FaceCheckingService.getInstance().setReceiveBehindCameraFace(true);
							FaceCheckingService.getInstance().resetFaceDataQueue();
							// Log.debug("收到第一个后置摄像头送来的人脸时，首先做一次清理");
						}

						FaceCheckingService.getInstance().offerFaceVerifyData(fd); // 加入待验证队列
						// Log.debug("Receive request message : " + fd);
					} else {
						if (FaceCheckingService.getInstance().isDealNoneTrackFace() && fd.getCameraFaceMode() == 2) {
							log.info("非检脸状态下，再次收到人脸");
							FaceCheckingService.getInstance().offerFaceVerifyData(fd); // 加入待验证队列
						}
					}
					if (ois != null) {
						ois.close();
						ois = null;
					}
					if (bais != null) {
						bais.close();
						bais = null;
					}
				} catch (IOException | ClassNotFoundException e) {
					log.error("publishArrived", e);
				} finally {
					if (ois != null) {
						ois.close();
						ois = null;
					}
					if (bais != null) {
						bais.close();
						bais = null;
					}
				}
			}
			payload = null;
		}

	}

	public static void main(String[] args) {
		PTVerifyReceiver mqttBroker = PTVerifyReceiver
				.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum());
		// MqttSenderBroker.getInstance().sendMessage("pub_topic", "DoorCmd12");

	}
}
