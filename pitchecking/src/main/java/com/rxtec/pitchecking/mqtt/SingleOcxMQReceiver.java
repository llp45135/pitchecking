package com.rxtec.pitchecking.mqtt;

import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.mqtt.MqttClient;
import com.ibm.mqtt.MqttException;
import com.ibm.mqtt.MqttSimpleCallback;
import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.FaceTrackingScreen;
import com.rxtec.pitchecking.HighFaceTrackingScreen;
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.ScreenCmdEnum;
import com.rxtec.pitchecking.SingleOcxFaceScreen;
import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.mbean.ProcessUtil;
import com.rxtec.pitchecking.net.event.CAMNotifyBean;
import com.rxtec.pitchecking.net.event.GatCrtlBean;
import com.rxtec.pitchecking.net.event.ScreenDisplayBean;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.IFaceTrackService;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
import com.rxtec.pitchecking.utils.BASE64Util;
import com.rxtec.pitchecking.utils.CommUtil;

/**
 * 普通USB摄像头的后置界面，同其他进程通信使用
 * 
 * @author ZhaoLin
 *
 */
public class SingleOcxMQReceiver {
	// 连接参数
	Logger log = LoggerFactory.getLogger("SingleOcxMQReceiver");
	Logger checkLog = LoggerFactory.getLogger("FaceCheckingStandaloneTask");
	private final static boolean CLEAN_START = true;
	private final static short KEEP_ALIVE = 30;// 低耗网络，但是又需要及时获取数据，心跳30s
	private String CLIENT_ID = "SOR";// 客户端标识
	private final static int[] QOS_VALUES = { 0 };// 对应主题的消息级别
	private final static String[] TOPICS = { "PITEventTopic" };
	private String[] unSubscribeTopics = { "PITInfoTopic" };
	// private final static String SEND_TOPIC = "sub_topic";
	private static SingleOcxMQReceiver _instance;

	private MqttClient mqttClient;

	IFaceTrackService faceTrackService = null;

	GatCtrlSenderBroker gatCtrlSender = null;

	private String localIP = DeviceConfig.getInstance().getIpAddress();

	/**
	 * 返回实例对象
	 * 
	 * @return
	 */
	public static synchronized SingleOcxMQReceiver getInstance(String pidname) {
		if (_instance == null)
			_instance = new SingleOcxMQReceiver(pidname);
		return _instance;
	}

	private SingleOcxMQReceiver(String pidname) {
		gatCtrlSender = GatCtrlSenderBroker.getInstance(pidname);

		CLIENT_ID = CLIENT_ID + localIP + pidname;

		while (true) {
			try {
				if (mqttClient == null || !mqttClient.isConnected()) {
					this.connect();
				}
				break;
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				log.error("connect:", e);
				CommUtil.sleep(5000);
				continue;
			}

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
		log.debug("start connect to " + DeviceConfig.getInstance().getMQTT_CONN_STR() + "# MyClientID==" + this.CLIENT_ID);
		mqttClient = new MqttClient(DeviceConfig.getInstance().getMQTT_CONN_STR());

		SimpleCallbackHandler simpleCallbackHandler = new SimpleCallbackHandler();
		mqttClient.registerSimpleHandler(simpleCallbackHandler);// 注册接收消息方法
		mqttClient.connect(CLIENT_ID, CLEAN_START, KEEP_ALIVE);
		mqttClient.subscribe(TOPICS, QOS_VALUES);// 订阅接收主题
		mqttClient.unsubscribe(unSubscribeTopics);

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
			log.error("SingleOcxMQReceiver sendMessage", e);
		} catch (Exception e) {
			log.error("SingleOcxMQReceiver sendMessage", e);
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
			String orignMqttMessage = mqttMessage;
			// log.info("orign mqttMessage==" + orignMqttMessage);

			try {
				String qrcode = "";
				if (topicName.equals("PITEventTopic")) {
					if (mqttMessage.toLowerCase().indexOf("eventsource") != -1) { // 收到门控制发回的指令
						mqttMessage = mqttMessage.replace("\r\n", "");
						mqttMessage = mqttMessage.replace(" ", "");
						mqttMessage = mqttMessage.toLowerCase();

						GatCrtlBean gatCrtlBean = new GatCrtlBean();
						if (mqttMessage.toLowerCase().indexOf("qrcode") != -1) {
							mqttMessage = mqttMessage.replace("{", "");
							mqttMessage = mqttMessage.replace("}", "");
							StringTokenizer st = new StringTokenizer(mqttMessage, ",");
							while (st.hasMoreTokens()) {
								String tt = st.nextToken();
								if (tt.indexOf("\"event\"") != -1) {
									String event = tt.substring(tt.indexOf(":") + 1).replace("\"", "");
									// log.debug("event==" + event);
									gatCrtlBean.setEvent(Integer.parseInt(event));
								} else if (tt.indexOf("\"target\"") != -1) {
									String target = tt.substring(tt.indexOf(":") + 1).replace("\"", "");
									// log.debug("target==" + target);
									gatCrtlBean.setTarget(target);
								} else if (tt.indexOf("\"eventsource\"") != -1) {
									String eventsource = tt.substring(tt.indexOf(":") + 1).replace("\"", "");
									// log.debug("eventsource==" + eventsource);
									gatCrtlBean.setEventsource(eventsource);
								} else if (tt.indexOf("qrcode") != -1) {
									qrcode = tt.substring(tt.indexOf(":") + 1).replace("\"", "");
									if (qrcode.length() > 144) {
										qrcode = qrcode.substring(0, 144);
									}
								}
							}
						} else {
							ObjectMapper mapper = new ObjectMapper();
							gatCrtlBean = mapper.readValue(mqttMessage, GatCrtlBean.class);
						}

						// log.debug("mqttMessage===" + mqttMessage);
						log.debug("EventSource==" + gatCrtlBean.getEventsource() + ",Event==" + gatCrtlBean.getEvent() + ",Target==" + gatCrtlBean.getTarget());

						if (gatCrtlBean.getEventsource().equals("manual")) { // 来自人工窗消息
							log.debug("来自人工窗消息:" + mqttMessage);

						} else if (gatCrtlBean.getEventsource().equals("tk")) { // ******************************来自铁科主控端消息
							// log.debug("来自铁科主控端消息:" + mqttMessage);
							if (CLIENT_ID.equals("SOR" + localIP + DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum())) { // 由检脸进程处理

								// log.info("检脸进程处理来自铁科主控端的消息:" + mqttMessage);
								/**
								 * 双门模式
								 */
								if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) { // 双门模式
									if (gatCrtlBean.getEvent() == 10010) { // 人脸开始核验
										if (Config.getInstance().getFaceControlMode() == 2 && !FaceCheckingService.getInstance().isFrontCamera()) { // 统一由比对进程处理时才由此调用
											// 设置为处于人脸核验中
											DeviceConfig.getInstance().setAllowOpenSecondDoor(true);
											DeviceConfig.getInstance().setInTracking(true);

											DeviceConfig.getInstance().setFaceScreenDisplay("");

											Ticket ticket = new Ticket();
											IDCard idCard = FaceCheckingService.getInstance().getIdcard();
											faceTrackService.beginCheckingFace(idCard, ticket);
											if (Config.getInstance().getBackScreenMode() == 1)
												FaceTrackingScreen.getInstance().offerEvent(
														new ScreenElementModifyEvent(1, ScreenCmdEnum.ShowBeginCheckFaceContent.getValue(), null, null, null));
											if (Config.getInstance().getBackScreenMode() == 2)
												HighFaceTrackingScreen.getInstance().offerEvent(
														new ScreenElementModifyEvent(1, ScreenCmdEnum.ShowBeginCheckFaceContent.getValue(), null, null, null));
										}
									} else if (gatCrtlBean.getEvent() == 10003) { // 停止送前置摄像头的人脸
										FaceCheckingService.getInstance().setSendFrontCameraFace(false);
										log.debug("停止送前置摄像头的人脸");
									} else if (gatCrtlBean.getEvent() == 10007) { // 允许送前置摄像头的人脸
										FaceCheckingService.getInstance().clearLastIdCard();
										FaceCheckingService.getInstance().setDealNoneTrackFace(false);
										FaceCheckingService.getInstance().setSendFrontCameraFace(true);
										log.debug("允许送前置摄像头的人脸");
									} else if (gatCrtlBean.getEvent() == 10008) {
										FaceCheckingService.getInstance().setIdCardPhotoRet(0);
									} else if (gatCrtlBean.getEvent() == 10011 || gatCrtlBean.getEvent() == 10012) { // 检脸成功
										if (gatCrtlBean.getEvent() == 10011) {
											// log.debug("人脸核验成功");
											log.debug("人脸核验成功");
											log.debug("#############################");
											if (Config.getInstance().getFaceControlMode() == 2 && !FaceCheckingService.getInstance().isFrontCamera()) { // 统一由比对进程处理时才由此调用
												FaceCheckingService.getInstance().setLastIdCard(FaceCheckingService.getInstance().getIdcard());
											}
										}
										if (gatCrtlBean.getEvent() == 10012) {
											// log.debug("人脸核验失败");
											log.debug("人脸核验失败");
											log.debug("#############################");
										}

										if (!FaceCheckingService.getInstance().isFrontCamera()) { // 统一由比对进程处理时才由此调用
											if (Config.getInstance().getFaceControlMode() == 2)
												faceTrackService.stopCheckingFace();

											if (Config.getInstance().getBackScreenMode() == 1)
												FaceTrackingScreen.getInstance()
														.offerEvent(new ScreenElementModifyEvent(1, ScreenCmdEnum.ShowFaceDisplayFromTK.getValue(), null, null, null));
											if (Config.getInstance().getBackScreenMode() == 2)
												HighFaceTrackingScreen.getInstance()
														.offerEvent(new ScreenElementModifyEvent(1, ScreenCmdEnum.ShowFaceDisplayFromTK.getValue(), null, null, null));
										}

										// 清除身份证信息，不再让前置摄像头使用
										FaceCheckingService.getInstance().setSendFrontCameraFace(true); // 允许送前置摄像头人脸
										log.debug("允许送前置摄像头人脸");
										FaceCheckingService.getInstance().resetIdCard();
										FaceCheckingService.getInstance().setIdCardPhotoRet(-1);

										DeviceConfig.getInstance().setInTracking(false); // 设置人脸核验已经完成
										log.debug("清除身份证信息，不再让前置摄像头使用");
									}
								}

								/**
								 * 单门模式
								 */
								if (Config.getInstance().getDoorCountMode() == DeviceConfig.SINGLEDOOR) { // 单门模式
									log.info("单门模式:getEvent = " + gatCrtlBean.getEvent());
									if (gatCrtlBean.getEvent() == 10000) { // 设备启动成功
										SingleOcxFaceScreen.getInstance()
												.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketDefault.getValue(), null, null, null));
									} else if (gatCrtlBean.getEvent() == 10001) { // 摄像头启动成功
										Config.getInstance().setCameraWork(true);
									} else if (gatCrtlBean.getEvent() == 10005) { // java版主控启动成功

									} else if (gatCrtlBean.getEvent() == 10006) { // 独立比对启动成功

									} else if (gatCrtlBean.getEvent() == 10007) { // 允许送前置摄像头的人脸

									} else if (gatCrtlBean.getEvent() == 10008) { // 易胜版本身份证建模成功

									} else if (gatCrtlBean.getEvent() == 10010) { // 开始检脸
										if (Config.getInstance().getFaceControlMode() == 2 && !FaceCheckingService.getInstance().isFrontCamera()) { // 统一由比对进程处理时才由此调用
											// 设置为处于人脸核验中
											DeviceConfig.getInstance().setAllowOpenSecondDoor(true);
											DeviceConfig.getInstance().setInTracking(true);

											DeviceConfig.getInstance().setFaceScreenDisplay("");

											Ticket ticket = new Ticket();
											IDCard idCard = FaceCheckingService.getInstance().getIdcard();
											// faceTrackService.beginCheckingFace(idCard,
											// ticket);

											if (!Config.getInstance().isUseFullScreenCamera()) {
												SingleOcxFaceScreen.getInstance()
														.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketVerifySucc.getValue(), null, null, null));
											}
											log.info("开始核对人脸中.....");

											SingleOcxFaceScreen.getInstance()
													.offerEvent(new ScreenElementModifyEvent(1, ScreenCmdEnum.ShowBeginCheckFaceContent.getValue(), null, null, null));
										}

									} else if (gatCrtlBean.getEvent() == 10011 || gatCrtlBean.getEvent() == 10012) { // 检脸成功
										if (gatCrtlBean.getEvent() == 10011) {
											// log.debug("人脸核验成功");
											log.debug("人脸核验成功");
											log.debug("#############################");
											if (Config.getInstance().getFaceControlMode() == 2 && !FaceCheckingService.getInstance().isFrontCamera()) { // 统一由比对进程处理时才由此调用
												FaceCheckingService.getInstance().setLastIdCard(FaceCheckingService.getInstance().getIdcard());
											}
										}
										if (gatCrtlBean.getEvent() == 10012) {
											// log.debug("人脸核验失败");
											log.debug("人脸核验失败");
											log.debug("#############################");
										}

										if (!FaceCheckingService.getInstance().isFrontCamera()) { // 统一由比对进程处理时才由此调用
											// if
											// (Config.getInstance().getFaceControlMode()
											// == 2)
											// faceTrackService.stopCheckingFace();

											SingleOcxFaceScreen.getInstance()
													.offerEvent(new ScreenElementModifyEvent(1, ScreenCmdEnum.ShowFaceDisplayFromTK.getValue(), null, null, null));

											if (!Config.getInstance().isUseFullScreenCamera()) {
												SingleOcxFaceScreen.getInstance()
														.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketDefault.getValue(), null, null, null));
											}
										}

										// 清除身份证信息，不再让前置摄像头使用
										FaceCheckingService.getInstance().setSendFrontCameraFace(true); // 允许送前置摄像头人脸
										log.debug("允许送前置摄像头人脸");
										FaceCheckingService.getInstance().resetIdCard();
										FaceCheckingService.getInstance().setIdCardPhotoRet(-1);

										DeviceConfig.getInstance().setInTracking(false); // 设置人脸核验已经完成
										log.debug("清除身份证信息，不再让前置摄像头使用");
									} else if (gatCrtlBean.getEvent() == 89001) { // 无电子船票
										SingleOcxFaceScreen.getInstance()
												.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.showNoShipTicket.getValue(), null, null, null));
									} else if (gatCrtlBean.getEvent() == 89002) { // 船票不符
										SingleOcxFaceScreen.getInstance()
												.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.showNoInvalidShipTicket.getValue(), null, null, null));
									} else if (gatCrtlBean.getEvent() == 89004) { // 内部服务器错误
										SingleOcxFaceScreen.getInstance()
												.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.showConnTicketCenterFailed.getValue(), null, null, null));
									} else if (gatCrtlBean.getEvent() == 80006) { // 重复验票
										SingleOcxFaceScreen.getInstance()
												.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.showRepeatCheck.getValue(), null, null, null));
									} else if (gatCrtlBean.getEvent() == 80004) { // 读二代证失败
										SingleOcxFaceScreen.getInstance()
												.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.showFailedIDCard.getValue(), null, null, null));
									} else if (gatCrtlBean.getEvent() == 80001) { // 读二维码失败/无电子票
										SingleOcxFaceScreen.getInstance()
												.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.showFailedQRCode.getValue(), null, null, null));
									} else if (gatCrtlBean.getEvent() == 90202) { // 读二维码失败/无电子票
										SingleOcxFaceScreen.getInstance()
												.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.showNoETicket.getValue(), null, null, null));
									} else if (gatCrtlBean.getEvent() == 80002) { // 票证不符
										SingleOcxFaceScreen.getInstance()
												.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.showInvalidTicketAndIDCard.getValue(), null, null, null));
									} else if (gatCrtlBean.getEvent() == 51681) { // 已过进站时间
										SingleOcxFaceScreen.getInstance()
												.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.showPassTime.getValue(), null, null, null));
									} else if (gatCrtlBean.getEvent() == 90238) { // 电子票已过进站时间
										SingleOcxFaceScreen.getInstance()
												.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.showETicketPassTime.getValue(), null, null, null));
									} else if (gatCrtlBean.getEvent() == 51682) { // 未到进站时间
										SingleOcxFaceScreen.getInstance()
												.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.showNotInTime.getValue(), null, null, null));
									} else if (gatCrtlBean.getEvent() == 90236) { // 电子票未到进站时间
										SingleOcxFaceScreen.getInstance()
												.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.showETicketNotInTime.getValue(), null, null, null));
									} else if (gatCrtlBean.getEvent() == 51605) { // 越站乘车
										SingleOcxFaceScreen.getInstance()
												.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.showPassStation.getValue(), null, null, null));
									} else if (gatCrtlBean.getEvent() == 51666) { // 票不符
										SingleOcxFaceScreen.getInstance()
												.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.showWrongStation.getValue(), null, null, null));
									} else if (gatCrtlBean.getEvent() == ScreenCmdEnum.ShowQRDeviceException.getValue()) { // 二维码读卡器故障
										SingleOcxFaceScreen.getInstance()
												.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowQRDeviceException.getValue(), null, null, null));
									} else if (gatCrtlBean.getEvent() == ScreenCmdEnum.ShowIDDeviceException.getValue()) { // 二代证读卡器故障
										SingleOcxFaceScreen.getInstance()
												.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowIDDeviceException.getValue(), null, null, null));
									} else {
										SingleOcxFaceScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0, gatCrtlBean.getEvent(), null, null, null));
									}
								}
								/**
								 * 
								 */
							}
						} else if (gatCrtlBean.getEventsource().equals("faceverifyback") || gatCrtlBean.getEventsource().equals("manualback")) {
							log.debug("来自DLL的回执:" + mqttMessage);

						} else if (gatCrtlBean.getEventsource().equals("faceaudio")) {
							log.debug("来自检脸进程的语音事件消息:" + mqttMessage);

						} else if (gatCrtlBean.getEventsource().equals("qrdevice")) { // 二维码消息
							// log.info("来自二维码消息:" + mqttMessage);

						} else if (gatCrtlBean.getEventsource().equals("faceverify")) {

						} else if (gatCrtlBean.getEventsource().equals("gate")) {
							log.debug("来自开二门的按钮或遥控消息:" + mqttMessage);
							if (gatCrtlBean.getEvent() == 10022) { // Event_PressOpenSecondDoorButton
								String pidName = "";
								if (Config.getInstance().getFaceControlMode() == 1) {
									pidName = DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum();
								}

								if (Config.getInstance().getFaceControlMode() == 1 && !FaceCheckingService.getInstance().isFrontCamera()) { // 由后置摄像头进程处理
									if (CLIENT_ID.equals("SOR" + localIP + pidName)) {

										log.debug("isInTracking==" + DeviceConfig.getInstance().isInTracking());

										if (DeviceConfig.getInstance().isInTracking()) { // 如果处于人脸核验中，那么必须立即结束本次核验，视为核验成功
											PITVerifyData failedFd = FaceCheckingService.getInstance().pollFailedFaceData();
											if (failedFd != null) {
												log.debug("人工判断该次核验成功，按按钮放行，本次核验中最好的一次比对结果==" + failedFd.getVerifyResult());
												// failedFd.setVerifyResult((float)
												// 0.63);
												FaceCheckingService.getInstance().offerPassFaceData(failedFd);
											} else {
												log.info("还没有检测到任何一张脸，手动生成一张脸返回，目的是快速结束本次检脸..==" + gatCrtlBean.getEvent());
												PITVerifyData manualFd = new PITVerifyData();

												manualFd.setVerifyResult(-1);
												manualFd.setIdNo(MqttSenderBroker.getInstance(pidName).getUuid());
												manualFd.setIdCardImg(MqttSenderBroker.getInstance(pidName).getIdcardBytes());
												manualFd.setFaceImg(MqttSenderBroker.getInstance(pidName).getIdcardBytes());
												manualFd.setFrameImg(MqttSenderBroker.getInstance(pidName).getIdcardBytes());
												try {
													manualFd.setIdCardImgByBase64(BASE64Util.encryptBASE64(MqttSenderBroker.getInstance(pidName).getIdcardBytes()));
													manualFd.setFaceImgByBase64(BASE64Util.encryptBASE64(MqttSenderBroker.getInstance(pidName).getIdcardBytes()));
													manualFd.setFrameImgByBase64(BASE64Util.encryptBASE64(MqttSenderBroker.getInstance(pidName).getIdcardBytes()));
												} catch (Exception ex) {
													log.error("老人或小孩:", ex);
												}

												FaceCheckingService.getInstance().offerPassFaceData(manualFd);
											}

										} else { // 不处于人脸核验中,直接开门
											log.info("不处于人脸核验中,不处理该事件!");
										}
									}
								}
							}
						} else {
							log.debug("该条消息暂时不做处理：" + mqttMessage);
						}

						if (gatCrtlBean.getEvent() == 10020) { // 第二道门关闭
							DeviceConfig.getInstance().setAllowOpenSecondDoor(true);
							log.debug("已收到第二道门的关门回执 by " + gatCrtlBean.getEventsource() + "!允许重新转发手动开第二道门指令");
							log.debug("是否允许手动开第二道门==" + DeviceConfig.getInstance().isAllowOpenSecondDoor());

							if (CLIENT_ID.equals("SOR" + localIP + DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum())) { // 人脸检测进程
								log.info("后门关闭则触发后置摄像头开始在非检脸状态采集人脸,event==" + gatCrtlBean.getEvent());
								FaceCheckingService.getInstance().setDealNoneTrackFace(true); // 后门关闭则触发后置摄像头开始在非检脸状态采集人脸
							}
						}
					} else { // 收到非Event事件
						// log.debug("-----------收到非Event事件-----------");
						if (mqttMessage.indexOf("CAM_Notify") != -1) {
							log.info("--------GatCtrlReciver 收到CAM_Notify请求--------");
							FaceCheckingService.getInstance().clearFaceVerifyQueue();

							ObjectMapper mapper = new ObjectMapper();
							CAMNotifyBean notifyEvent = mapper.readValue(mqttMessage, CAMNotifyBean.class);
							
							IDCard notifyIdCard = new IDCard();
							notifyIdCard.setIdNo(notifyEvent.getUuid());
							notifyIdCard.setAge(notifyEvent.getAge());
							notifyIdCard.setGender(notifyEvent.getGender());
							notifyIdCard.setPersonName(notifyEvent.getPersonName());
							notifyIdCard.setIDBirth(notifyEvent.getIdBirth());
							notifyIdCard.setIDNation(notifyEvent.getIdNation());
							notifyIdCard.setIDDwelling(notifyEvent.getIdDwelling());
							notifyIdCard.setIDEfficb(notifyEvent.getIdEfficb());
							notifyIdCard.setIDEffice(notifyEvent.getIdEffice());
							notifyIdCard.setIDIssue(notifyEvent.getIdIssue());							
							notifyIdCard.setCardImageBytes(notifyEvent.getIdPhoto());
							
							FaceCheckingService.getInstance().setIdcard(notifyIdCard);
						} else if (mqttMessage.indexOf("CAM_ScreenDisplay") != -1) {
							log.info("--------GatCtrlReciver 收到CAM_ScreenDisplay--------");
							ObjectMapper mapper = new ObjectMapper();
							ScreenDisplayBean screenDisplayBean = mapper.readValue(mqttMessage, ScreenDisplayBean.class);
							int displayTimeout = screenDisplayBean.getTimeout(); // 从CAM_ScreenDisplay输出的结构体获取到屏幕超时时间
							String faceScreenDisplay = screenDisplayBean.getScreenDisplay();
							log.info("CAM_ScreenDisplay faceScreenDisplay==" + faceScreenDisplay);
							log.info("CAM_ScreenDisplay displayTimeout==" + displayTimeout);

							DeviceConfig.getInstance().setFaceScreenDisplay(faceScreenDisplay);
							DeviceConfig.getInstance().setFaceScreenDisplayTimeout(displayTimeout);

							if (CLIENT_ID.equals("SOR" + localIP + DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum())) { // 由检脸进程处理
								if (Config.getInstance().getFaceControlMode() == 1 && !FaceCheckingService.getInstance().isFrontCamera()) { // 由后置检脸进程处理由此调用
									if (faceScreenDisplay.indexOf("通过") != -1) {
										// 语音："验证成功，请通过"
										gatCtrlSender.sendDoorCmd(DeviceConfig.EventTopic, ProcessUtil.createTkEventJson(DeviceConfig.AudioCheckSuccFlag, "FaceAudio"));
										// 通知人工控制台
										gatCtrlSender.sendDoorCmd(DeviceConfig.EventTopic, DeviceConfig.Event_VerifyFaceSucc);
									} else {
										// 语音："验证失败，请从侧门离开通道"
										gatCtrlSender.sendDoorCmd(ProcessUtil.createTkEventJson(DeviceConfig.AudioCheckFailedFlag, "FaceAudio"));
										// 通知人工控制台
										gatCtrlSender.sendMessage(DeviceConfig.EventTopic, DeviceConfig.Event_VerifyFaceFailed);
									}
								}
							}

						}
					}
				}
			} catch (Exception ex) {
				log.error("SingleOcxMQReceiver:", ex);
			}

			payload = null;
		}

	}

	public static void main(String[] args) {
		SingleOcxMQReceiver mqttBroker = SingleOcxMQReceiver.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT);// 启动PITEventTopic本地监听
		System.out.println("$$");

	}
}
