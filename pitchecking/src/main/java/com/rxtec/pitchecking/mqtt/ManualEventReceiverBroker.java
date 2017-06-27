package com.rxtec.pitchecking.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.mqtt.MqttClient;
import com.ibm.mqtt.MqttException;
import com.ibm.mqtt.MqttSimpleCallback;
import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.net.event.CameraColorBean;
import com.rxtec.pitchecking.net.event.GatCrtlBean;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
import com.rxtec.pitchecking.picheckingservice.realsense.RSFaceDetectionService;
import com.rxtec.pitchecking.picheckingservice.realsense.RealsenseDeviceProperties;
import com.rxtec.pitchecking.utils.CommUtil;

/**
 * 本类监听人工处置窗的PITEventTopic，收到人工窗指令做相应逻辑处理 主要由比对进程调用
 * 
 * @author ZhaoLin
 *
 */
public class ManualEventReceiverBroker {
	// 连接参数
	Logger log = LoggerFactory.getLogger("ManualEventReceiverBroker");
	private final static boolean CLEAN_START = true;
	private final static short KEEP_ALIVE = 30;// 低耗网络，但是又需要及时获取数据，心跳30s
	private String CLIENT_ID = "MER";// 客户端标识
	private final static int[] QOS_VALUES = { 0 };// 对应主题的消息级别
	private final static String[] TOPICS = { "PITEventTopic" };
	private String[] unSubscribeTopics = { "PITInfoTopic" };
	// private final static String SEND_TOPIC = "sub_topic";
	private static ManualEventReceiverBroker _instance;

	private MqttClient mqttClient;

	/**
	 * 返回实例对象
	 * 
	 * @return
	 */
	public static synchronized ManualEventReceiverBroker getInstance(String pidname) {
		if (_instance == null)
			_instance = new ManualEventReceiverBroker(pidname);
		return _instance;
	}

	private ManualEventReceiverBroker(String pidname) {
		CLIENT_ID = CLIENT_ID + DeviceConfig.getInstance().getIpAddress() + pidname;
		this.initMQ();
	}

	private void initMQ() {
		log.debug("人工控制台主题PITEventTopic监听,initMQ..." + DeviceConfig.getInstance().getManualCheck_MQTTURL());
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
			log.error("人工控制台PITEventTopic监听 reconnect failed!");
			flag = -1;
		}
		return flag;
	}

	/**
	 * 重新连接服务
	 */
	private void connect() throws MqttException {
		log.debug("start connect to " + DeviceConfig.getInstance().getManualCheck_MQTTURL() + "# MyClientID==" + this.CLIENT_ID);
		mqttClient = new MqttClient(DeviceConfig.getInstance().getManualCheck_MQTTURL());

		SimpleCallbackHandler simpleCallbackHandler = new SimpleCallbackHandler();
		mqttClient.registerSimpleHandler(simpleCallbackHandler);// 注册接收消息方法
		mqttClient.connect(CLIENT_ID, CLEAN_START, KEEP_ALIVE);
		log.debug("subscribe receiver topics==" + TOPICS[0]);
		mqttClient.subscribe(TOPICS, QOS_VALUES);// 订阅接收主题
		mqttClient.unsubscribe(unSubscribeTopics);

		log.debug("**人工控制台主题PITEventTopic监听,连接 " + DeviceConfig.getInstance().getManualCheck_MQTTURL() + " 成功**");

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
			log.error("ManualEventReceiverBroker sendMessage", e);
		} catch (Exception e) {
			log.error("ManualEventReceiverBroker sendMessage", e);
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
		public void publishArrived(String topicName, byte[] payload, int Qos, boolean retained) {
			// TODO Auto-generated method stub
			// log.debug("订阅主题: " + topicName);
			// log.debug("消息数据: " + new String(payload));
			// log.debug("消息级别(0,1,2): " + Qos);
			// log.debug("是否是实时发送的消息(false=实时，true=服务器上保留的最后消息): " + retained);
			try {
				String mqttMessage = new String(payload);
				// log.debug("mqttMessage==" + mqttMessage);
				if (topicName.equals("PITEventTopic")) {
					// log.debug("mqttMessage==" + mqttMessage);
					if (mqttMessage.toLowerCase().indexOf("eventsource") != -1) { // 收到门控制发回的指令

						if (mqttMessage.toLowerCase().indexOf("123321") != -1) { // 设置摄像头曝光度等模式
							ObjectMapper mapper = new ObjectMapper();
							CameraColorBean cameraColorBean = mapper.readValue(mqttMessage.toLowerCase(), CameraColorBean.class);
							log.debug("Receive msg = " + mqttMessage.toString());
							if (CLIENT_ID
									.equals("MER" + DeviceConfig.getInstance().getIpAddress() + DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum())) { // 由检脸进程处理
								log.debug("getEvent==" + cameraColorBean.getEvent());
								log.debug("getEventsource==" + cameraColorBean.getEventsource());
								log.debug("getTarget==" + cameraColorBean.getTarget());
								log.debug("getColorbrightness==" + cameraColorBean.getColorbrightness());
								log.debug("getColorexposure==" + cameraColorBean.getColorexposure());
								log.debug("getColorautoexposure==" + cameraColorBean.getColorautoexposure());
								log.debug("getColorautowhitebalance==" + cameraColorBean.getColorautowhitebalance());
								log.debug("getColorbacklightcompensation==" + cameraColorBean.getColorbacklightcompensation());
								log.debug("getHdr==" + cameraColorBean.getHdr());
								log.debug("getContrast==" + cameraColorBean.getContrast());
								log.debug("getGamma==" + cameraColorBean.getGamma());
								log.debug("getGain==" + cameraColorBean.getGain());

								if (cameraColorBean.getEventsource().equals("manual") && cameraColorBean.getTarget().equals(DeviceConfig.getInstance().getIpAddress())) {
									RealsenseDeviceProperties rdp = new RealsenseDeviceProperties();
									rdp.setColorAutoExposure(cameraColorBean.getColorautoexposure());
									rdp.setColorAutoWhiteBalance(cameraColorBean.getColorautowhitebalance());
									rdp.setColorBackLightCompensation(cameraColorBean.getColorbacklightcompensation());
									rdp.setColorBrightness(cameraColorBean.getColorbrightness());
									rdp.setColorExposure(cameraColorBean.getColorexposure());
									rdp.setSR300_HDR(cameraColorBean.getHdr());
									rdp.setContrast(cameraColorBean.getContrast());
									rdp.setGamma(cameraColorBean.getGamma());
									rdp.setGain(cameraColorBean.getGain());

									RSFaceDetectionService.getInstance().setDeviceProperties(rdp);
									log.debug("接收到控制台设置Realsense参数指令，参数已动态重置!!");
								}
							} else {
								log.debug("本线程没有权限处理该消息:" + mqttMessage);
							}
						} else {
							ObjectMapper mapper = new ObjectMapper();
							GatCrtlBean gatCrtlBean = mapper.readValue(mqttMessage.toLowerCase(), GatCrtlBean.class);

							if (gatCrtlBean.getEventsource().equals("manual") && gatCrtlBean.getTarget().equals(DeviceConfig.getInstance().getIpAddress())) {
								log.debug("来自人工窗消息：" + mqttMessage);

								if (gatCrtlBean.getEvent() == DeviceConfig.Event_OpenFirstDoor || gatCrtlBean.getEvent() == DeviceConfig.Event_OpenThirdDoor) { // 开1门或开3门指令
									if (CLIENT_ID.equals("MER" + DeviceConfig.getInstance().getIpAddress() + DeviceConfig.GAT_MQ_Standalone_CLIENT)) { // 由比对进程处理
										log.debug("是否允许开1门==" + DeviceConfig.getInstance().isAllowOpenFirstDoor());
										if (DeviceConfig.getInstance().isAllowOpenFirstDoor()) {
											DeviceConfig.getInstance().setAllowOpenFirstDoor(false);

											log.debug("转发前门或者边门的开门指令==" + gatCrtlBean.getEvent());
											String sndmsg = mqttMessage.replaceAll(DeviceConfig.getInstance().getIpAddress(), "127.0.0.1");
											log.debug("替换真实IP后,sndmsg==" + sndmsg);
											GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT).sendDoorCmd("PITEventTopic", sndmsg);
											CommUtil.sleep(1000);
											DeviceConfig.getInstance().setAllowOpenFirstDoor(true);
										}
									}
								} else if (gatCrtlBean.getEvent() == DeviceConfig.Event_OpenSecondDoor) { // 收到开后门指令，即“放行”指令
									if (Config.getInstance().getFaceControlMode() == 1) {
										if (CLIENT_ID.equals("MER" + DeviceConfig.getInstance().getIpAddress() + DeviceConfig.GAT_MQ_Track_CLIENT
												+ Config.getInstance().getCameraNum())) { // 由检脸进程处理
											log.debug("isAllowOpenSecondDoor==" + DeviceConfig.getInstance().isAllowOpenSecondDoor());

											if (DeviceConfig.getInstance().isAllowOpenSecondDoor()) {
												DeviceConfig.getInstance().setAllowOpenSecondDoor(false);

												log.debug("isInTracking==" + DeviceConfig.getInstance().isInTracking());

												if (DeviceConfig.getInstance().isInTracking()) { // 如果处于人脸核验中，那么必须立即结束本次核验，视为核验成功
													PITVerifyData failedFd = FaceCheckingService.getInstance().pollFailedFaceData();
													if (failedFd != null) {
														log.debug("控制台人工判断该次核验成功，人工放行，本次核验中最好的一次比对结果==" + failedFd.getVerifyResult());
														// failedFd.setVerifyResult((float)
														// 0.63);
														FaceCheckingService.getInstance().offerPassFaceData(failedFd);
													} else {
														log.info("还没有检测到任何一张脸，手动生成一张脸返回，目的是快速结束本次检脸..==" + gatCrtlBean.getEvent());
														PITVerifyData manualFd = new PITVerifyData();

														manualFd.setVerifyResult(-1);
														manualFd.setIdNo(MqttSenderBroker
																.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum()).getUuid());
														manualFd.setIdCardImg(MqttSenderBroker
																.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum()).getIdcardBytes());
														manualFd.setFaceImg(MqttSenderBroker
																.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum()).getIdcardBytes());
														manualFd.setFrameImg(MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT).getIdcardBytes());

														FaceCheckingService.getInstance().offerPassFaceData(manualFd);

														// GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT+Config.getInstance().getCameraNum())
														// .sendDoorCmd("PITEventTopic",
														// mqttMessage);
													}

												} else { // 不处于人脸核验中,直接开门
													log.debug("不处于人脸核验中,转发后门的开门指令==" + gatCrtlBean.getEvent());
													String sndmsg = mqttMessage.replaceAll(DeviceConfig.getInstance().getIpAddress(), "127.0.0.1");
													log.debug("替换真实IP后,sndmsg==" + sndmsg);
													GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum())
															.sendDoorCmd("PITEventTopic", sndmsg);
												}
											} else {
												log.debug("前次后门指令转发后还未收到关门回执");
											}
										}
									} else {
										if (CLIENT_ID.equals("MER" + DeviceConfig.getInstance().getIpAddress() + DeviceConfig.GAT_MQ_Standalone_CLIENT)) { // 由比对进程处理

											log.debug("isAllowOpenSecondDoor==" + DeviceConfig.getInstance().isAllowOpenSecondDoor());

											if (DeviceConfig.getInstance().isAllowOpenSecondDoor()) {
												DeviceConfig.getInstance().setAllowOpenSecondDoor(false);

												log.debug("isInTracking==" + DeviceConfig.getInstance().isInTracking());

												if (DeviceConfig.getInstance().isInTracking()) { // 如果处于人脸核验中，那么必须立即结束本次核验，视为核验成功
													PITVerifyData failedFd = FaceCheckingService.getInstance().pollFailedFaceData();
													if (failedFd != null) {
														log.debug("控制台人工判断该次核验成功，人工放行，本次核验中最好的一次比对结果==" + failedFd.getVerifyResult());
														// failedFd.setVerifyResult((float)
														// 0.63);
														FaceCheckingService.getInstance().offerPassFaceData(failedFd);
													} else {
														log.info("还没有检测到任何一张脸，手动生成一张脸返回，目的是快速结束本次检脸..==" + gatCrtlBean.getEvent());
														PITVerifyData manualFd = new PITVerifyData();

														manualFd.setVerifyResult(-1);
														manualFd.setIdNo(MqttSenderBroker
																.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum()).getUuid());
														manualFd.setIdCardImg(MqttSenderBroker
																.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum()).getIdcardBytes());
														manualFd.setFaceImg(MqttSenderBroker
																.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum()).getIdcardBytes());
														manualFd.setFrameImg(MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT).getIdcardBytes());

														FaceCheckingService.getInstance().offerPassFaceData(manualFd);

														// GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT+Config.getInstance().getCameraNum())
														// .sendDoorCmd("PITEventTopic",
														// mqttMessage);
													}

												} else { // 不处于人脸核验中,直接开门
													log.debug("不处于人脸核验中,转发后门的开门指令==" + gatCrtlBean.getEvent());
													String sndmsg = mqttMessage.replaceAll(DeviceConfig.getInstance().getIpAddress(), "127.0.0.1");
													log.debug("替换真实IP后,sndmsg==" + sndmsg);
													GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum())
															.sendDoorCmd("PITEventTopic", sndmsg);
												}
											} else {
												log.debug("前次后门指令转发后还未收到关门回执");
											}
										}
									}

								} else if (gatCrtlBean.getEvent() == DeviceConfig.Event_PauseService) { // 暂停服务
									if (CLIENT_ID.equals("MER" + DeviceConfig.getInstance().getIpAddress() + DeviceConfig.GAT_MQ_Standalone_CLIENT)) { // 由比对进程处理

										if (DeviceConfig.getInstance().isInService()) {
											log.debug("准备执行暂停服务指令");
											if (Config.getInstance().getIsUseGatDll() == 1) {
												Runtime.getRuntime().exec(Config.getInstance().getKillTKExeCmd()); // 杀死中铁程gui.exe
												log.debug("已经执行暂停服务指令KillTKExeCmd");
											}

											DeviceConfig.getInstance().setInService(false);

											// CommUtil.sleep(3000);
											// ProcessUtil.writePauseLog("kill");
											// Runtime.getRuntime().exec(Config.getInstance().getStartPITVerifyCmd());

											mqttMessage = mqttMessage.replace(DeviceConfig.getInstance().getIpAddress(), "127.0.0.1");
											GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT).sendDoorCmd("PITEventTopic", mqttMessage);
										} else {
											log.debug("服务已经处于暂停中!");
										}
									}
								} else if (gatCrtlBean.getEvent() == DeviceConfig.Event_ContinueService) { // 恢复服务
									if (CLIENT_ID.equals("MER" + DeviceConfig.getInstance().getIpAddress() + DeviceConfig.GAT_MQ_Standalone_CLIENT)) { // 由比对进程处理

										if (!DeviceConfig.getInstance().isInService()) {
											log.debug("准备执行恢复服务指令");
											// String pauseFlagStr =
											// ProcessUtil.getLastPauseFlag();
											// if (pauseFlagStr != null &&
											// !pauseFlagStr.equals("")) {
											// String pid =
											// pauseFlagStr.split("@")[0];
											// Runtime.getRuntime().exec("taskkill
											// /F /PID " + pid);
											// CommUtil.sleep(1000);
											// Runtime.getRuntime().exec(Config.getInstance().getStartTKExeCmd());
											// // 启动gui.exe
											// }
											if (Config.getInstance().getIsUseGatDll() == 1) {
												Runtime.getRuntime().exec(Config.getInstance().getStartTKExeCmd()); // 启动gui.exe
												CommUtil.sleep(3000);
												log.debug("已经执行恢复服务指令StartTKExeCmd==" + Config.getInstance().getStartTKExeCmd());
											}

											CommUtil.sleep(10 * 1000);

											DeviceConfig.getInstance().setInService(true);

											mqttMessage = mqttMessage.replace(DeviceConfig.getInstance().getIpAddress(), "127.0.0.1");
											GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT).sendDoorCmd("PITEventTopic", mqttMessage);
										} else {
											log.debug("已经处于正常服务状态!!");
										}
									}
								} else if (gatCrtlBean.getEvent() == DeviceConfig.Event_ResetService) { // 重启闸机
									if (CLIENT_ID.equals("MER" + DeviceConfig.getInstance().getIpAddress() + DeviceConfig.GAT_MQ_Standalone_CLIENT)) { // 由比对进程处理
										log.debug("准备执行重启闸机指令");
										Runtime.getRuntime().exec("shutdown -l");
									}
								} else { // 人工窗其他指令
									if (CLIENT_ID.equals("MER" + DeviceConfig.getInstance().getIpAddress() + DeviceConfig.GAT_MQ_Standalone_CLIENT)) { // 由比对进程处理
										log.debug("转发人工窗其他指令==" + gatCrtlBean.getEvent());
										GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT).sendDoorCmd("PITEventTopic", mqttMessage);
									}
								}
							}
						}
					}
				}
				payload = null;
			} catch (Exception ex) {
				log.error("", ex);
			}
		}

	}

	public static void main(String[] args) {
		// ManualEventReceiverBroker mer1 =
		// ManualEventReceiverBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT+Config.getInstance().getCameraNum());

		ManualEventReceiverBroker mer2 = ManualEventReceiverBroker.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT);
		System.out.println("$$");

	}
}
