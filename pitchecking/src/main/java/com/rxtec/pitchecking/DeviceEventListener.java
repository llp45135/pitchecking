package com.rxtec.pitchecking;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.db.sqlserver.GetTrainInfoFromTxt;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.device.DeviceException;
import com.rxtec.pitchecking.device.FirstGateDevice;
import com.rxtec.pitchecking.device.LightControlBoard;
import com.rxtec.pitchecking.device.RSIVIDcardDevice;
import com.rxtec.pitchecking.device.SecondGateDevice;
import com.rxtec.pitchecking.device.smartmonitor.MonitorXMLUtil;
import com.rxtec.pitchecking.event.IDeviceEvent;
import com.rxtec.pitchecking.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.mbean.ProcessUtil;
import com.rxtec.pitchecking.mq.JmsReceiverTask;
import com.rxtec.pitchecking.mq.JmsSenderTask;
import com.rxtec.pitchecking.mqtt.GatCtrlSenderBroker;
import com.rxtec.pitchecking.mqtt.MqttCamRequestSender;
import com.rxtec.pitchecking.net.event.CAMJsonUtil;
import com.rxtec.pitchecking.net.event.CAMNotifyBean;
import com.rxtec.pitchecking.net.event.CAMOpenBean;
import com.rxtec.pitchecking.net.event.PIVerifyResultBean;
import com.rxtec.pitchecking.net.event.ScreenDisplayBean;
import com.rxtec.pitchecking.net.event.quickhigh.GetTrainInfoFromSoap;
import com.rxtec.pitchecking.net.event.quickhigh.TrainInfomation;
import com.rxtec.pitchecking.service.ticketsystem.TicketReservationService;
import com.rxtec.pitchecking.task.AutoLogonJob;
import com.rxtec.pitchecking.utils.CalUtils;
import com.rxtec.pitchecking.utils.CommUtil;

public class DeviceEventListener implements Runnable {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");
	private static DeviceEventListener _instance = new DeviceEventListener();
	private TicketVerify ticketVerify = new TicketVerify();
	private boolean isStartThread = true; // 是否启动主线程
	private boolean isDealDeviceEvent = true; // 是否处理票证事件
	private boolean isReceiveOpenFirstDoorCmd = false;
	private boolean isReceiveOpenSecondDoorCmd = false;
	private int trailingStatus = 0; // 尾随状态
									// 0:无尾随;1:中央区域两人尾随;2：中间区域四组对射全部遮挡;3:第一个人在后门正通过、第二个人已经走到中间位置;

	private LinkedBlockingQueue<IDeviceEvent> deviceEventQueue = new LinkedBlockingQueue<IDeviceEvent>(1);

	private Map<String, IDCard> personPassMap = new HashMap<String, IDCard>(); // 重复刷卡时使用
	private Map<String, Ticket> ticketPassMap = new HashMap<String, Ticket>(); // 重复刷票时使用

	//
	private LinkedBlockingQueue<CAMOpenBean> camOpenDataQueue;
	private LinkedBlockingQueue<CAMNotifyBean> camNotifyDataQueue;
	private LinkedBlockingQueue<PIVerifyResultBean> camGetPhotoInfoQueue;
	private LinkedBlockingQueue<ScreenDisplayBean> camScreenDisplayQueue;

	TicketReservationService TRService;

	private PIVerifyResultBean faceVerifyResult = null; // 比对进程比对后的结果

	private int flapCount = 0; // 闸门开门次数

	private Map<String, TrainInfomation> trainInfoMap = new HashMap<String, TrainInfomation>(); // 旅服数据-正晚点信息

	private int childCheckCount = 0; // 根据身高判断是否为儿童

	/********************************************************************************/

	public int getFlapCount() {
		return flapCount;
	}

	public Map<String, Ticket> getTicketPassMap() {
		return ticketPassMap;
	}

	public void setTicketPassMap(Map<String, Ticket> ticketPassMap) {
		this.ticketPassMap = ticketPassMap;
	}

	public int getChildCheckCount() {
		return childCheckCount;
	}

	public void setChildCheckCount(int childCheckCount) {
		this.childCheckCount = childCheckCount;
	}

	public Map<String, TrainInfomation> getTrainInfoMap() {
		return trainInfoMap;
	}

	public void setTrainInfoMap(Map<String, TrainInfomation> trainInfoMap) {
		this.trainInfoMap = trainInfoMap;
	}

	public void setFlapCount(int flapCount) {
		this.flapCount = flapCount;
	}

	public TicketVerify getTicketVerify() {
		return ticketVerify;
	}

	public PIVerifyResultBean getFaceVerifyResult() {
		return faceVerifyResult;
	}

	public void setFaceVerifyResult(PIVerifyResultBean faceVerifyResult) {
		this.faceVerifyResult = faceVerifyResult;
	}

	public void offerCamOpenDataQueue(CAMOpenBean camOpenBean) {
		this.camOpenDataQueue.offer(camOpenBean);
		log.info("插入CAMOpen回执到CAMOpen队列");
	}

	public CAMOpenBean pollCamOpenDataQueue() throws InterruptedException {
		CAMOpenBean b = this.camOpenDataQueue.poll(30, TimeUnit.SECONDS);
		return b;
	}

	public void offerCamNotifyDataQueue(CAMNotifyBean camNotifyBean) {
		this.camNotifyDataQueue.offer(camNotifyBean);
		log.info("插入CAMNotify回执到CAMNotify队列");
	}

	public CAMNotifyBean pollCamNotifyDataQueue() throws InterruptedException {
		CAMNotifyBean b = this.camNotifyDataQueue.poll(1000, TimeUnit.MILLISECONDS);
		return b;
	}

	public void offerCamGetPhotoInfoQueue(PIVerifyResultBean piVerifyResultBean) {
		this.camGetPhotoInfoQueue.offer(piVerifyResultBean);
		log.info("插入CamGetPhotoInfo回执到CamGetPhotoInfo队列");
	}

	public PIVerifyResultBean pollCamGetPhotoInfoQueue() throws InterruptedException {
		PIVerifyResultBean b = this.camGetPhotoInfoQueue.poll(Config.getInstance().getFaceCheckDelayTime(), TimeUnit.SECONDS);
		return b;
	}

	public void offerCamScreenDisplayQueue(ScreenDisplayBean screenDisplayBean) {
		this.camScreenDisplayQueue.offer(screenDisplayBean);
		log.info("插入CamScreenDisplay回执到CamScreenDisplay队列");
	}

	public ScreenDisplayBean pollCamScreenDisplayQueue() throws InterruptedException {
		ScreenDisplayBean b = this.camScreenDisplayQueue.poll(1000, TimeUnit.MILLISECONDS);
		return b;
	}

	public Map<String, IDCard> getPersonPassMap() {
		return personPassMap;
	}

	public void setPersonPassMap(Map<String, IDCard> personPassMap) {
		this.personPassMap = personPassMap;
	}

	public int getTrailingStatus() {
		return trailingStatus;
	}

	public void setTrailingStatus(int trailingStatus) {
		this.trailingStatus = trailingStatus;
	}

	public boolean isReceiveOpenSecondDoorCmd() {
		return isReceiveOpenSecondDoorCmd;
	}

	public void setReceiveOpenSecondDoorCmd(boolean isReceiveOpenSecondDoorCmd) {
		this.isReceiveOpenSecondDoorCmd = isReceiveOpenSecondDoorCmd;
	}

	public boolean isReceiveOpenFirstDoorCmd() {
		return isReceiveOpenFirstDoorCmd;
	}

	public void setReceiveOpenFirstDoorCmd(boolean isReceiveOpenFirstDoorCmd) {
		this.isReceiveOpenFirstDoorCmd = isReceiveOpenFirstDoorCmd;
	}

	public boolean isStartThread() {
		return isStartThread;
	}

	public void setStartThread(boolean isStartThread) {
		this.isStartThread = isStartThread;
	}

	public boolean isDealDeviceEvent() {
		return isDealDeviceEvent;
	}

	public void setDealDeviceEvent(boolean isDealDeviceEvent) {
		this.isDealDeviceEvent = isDealDeviceEvent;
	}

	private DeviceEventListener() {
		if (Config.getInstance().getIsConnectTRService() == 1) {
			TRService = TicketReservationService.getInstance();
		}
		camOpenDataQueue = new LinkedBlockingQueue<CAMOpenBean>(1);
		camNotifyDataQueue = new LinkedBlockingQueue<CAMNotifyBean>(1);
		camGetPhotoInfoQueue = new LinkedBlockingQueue<PIVerifyResultBean>(1);
		camScreenDisplayQueue = new LinkedBlockingQueue<ScreenDisplayBean>(1);
	}

	public static DeviceEventListener getInstance() {
		return _instance;
	}

	/**
	 * 每次开始检脸前前清掉之前可能的残留
	 */
	public void resetCamDataQueue() {
		this.camOpenDataQueue.clear();
		this.camNotifyDataQueue.clear();
		this.camGetPhotoInfoQueue.clear();
		this.camScreenDisplayQueue.clear();
		log.info("CAM事件的队列已清理");
	}

	/**
	 * 
	 * @param e
	 */
	public void offerDeviceEvent(IDeviceEvent e) {
		// 队列满了需要处理Exception,注意！
		deviceEventQueue.offer(e);
	}

	/**
	 * 清理读票证事件
	 */
	public void clearDeviceEvent() {
		this.deviceEventQueue.clear();
	}

	/**
	 * 测试用例，手动创建身份证
	 * 
	 * @param fn
	 * @return
	 */
	// private static IDCard createIDCard(String fn) {
	// IDCard card = new IDCard();
	// card.setIdNo("520203197912141119");
	// BufferedImage bi = null;
	// try {
	// bi = ImageIO.read(new File(fn));
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// card.setCardImageBytes(ImageToolkit.getImageBytes(bi, "jpeg"));
	// card.setCardImage(bi);
	// card.setAge(44);
	// return card;
	// }

	/**
	 * 测试用例
	 */
	// private void offerDeviceEvent() {
	// log.debug("offerDeviceEvent readCardEvent");
	// IDCardReaderEvent readCardEvent = new IDCardReaderEvent();
	// IDCard idCard = IDCardUtil.createIDCard("zp.jpg");
	// readCardEvent.setIdCard(idCard);
	// DeviceEventListener.getInstance().offerDeviceEvent(readCardEvent);
	//
	// QRCodeReaderEvent qrEvent = new QRCodeReaderEvent(Config.QRReaderEvent);
	// Ticket ticket = new Ticket();
	// ticket.setCardNo("520203197912141119");
	// ticket.setTicketNo("T129999");
	// ticket.setFromStationCode("IZQ");
	// ticket.setEndStationCode("SZQ");
	// ticket.setTicketType("0");
	// ticket.setCardType("1");
	// ticket.setTrainDate(CalUtils.getStringDateShort2());
	// qrEvent.setTicket(ticket);
	// DeviceEventListener.getInstance().offerDeviceEvent(qrEvent);
	// log.debug("offerDeviceEvent qrEvent");
	// }

	/**
	 * 线程主入口
	 */
	@Override
	public void run() {

		// this.offerDeviceEvent();

		IDeviceEvent e;
		try {
			if (isDealDeviceEvent) {
				e = deviceEventQueue.poll();
				// log.debug("e==" + e);
				if (e != null) {
					this.setDealDeviceEvent(false);// 停止处理新的事件
					this.processEvent(e);
				}
			}
		} catch (Exception ex) {
			log.error("DeviceEventListener", ex);
		}
	}

	/**
	 * 票证事件处理
	 * 
	 * @param e
	 */
	private void processEvent(IDeviceEvent e) {
		// if (isDealDeviceEvent) {
		// this.setDealDeviceEvent(false);//停止处理新的事件
		log.info("/*************Start*******************/");
		if (e.getEventType() == Config.QRReaderEvent && e.getData() != null) {
			/**
			 * 二维码读卡器读到新数据
			 */
			ticketVerify.setTicket((Ticket) e.getData());
			if (Config.getInstance().getIsOutGateForCSQ() == 0) {
				if (ticketVerify.getIdCard() == null) {
					CommUtil.sleep(1000);
				}
				verifyTicket();
			} else {
				verifyTicketForCSQOutgate();
			}

		} else if (e.getEventType() == Config.IDReaderEvent && e.getData() != null) {
			/**
			 * 二代证读卡器读到新数据
			 */
			ticketVerify.setIdCard((IDCard) e.getData());
			if (ticketVerify.getTicket() == null) {
				CommUtil.sleep(500);
			}
			verifyTicket();
		}
		log.info("/################End######################/");
		// this.setDealDeviceEvent(true);//允许处理新的事件
		// } else {
		// return;
		// }
	}

	/**
	 * 
	 */
	private void verifyTicketForCSQOutgate() {
		this.setDeviceReader(false);// 停止寻卡

		String ticketVerifyResult = "";
		ticketVerifyResult = ticketVerify.verifyForCSQOutgate();

		if (Config.TicketVerifySucc.equals(ticketVerifyResult)) { // 票证核验成功

			TicketVerifyScreen.getInstance()
					.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketSuccCSQ.getValue(), ticketVerify.getTicket(), ticketVerify.getIdCard(), null));

			/**
			 * 发送开2门指令
			 */
			if (Config.getInstance().getIsUseGatDll() == 0) { // 不调用门控动态库
				DeviceConfig.getInstance().setSecondGateOpenCount(0);
				this.setReceiveOpenSecondDoorCmd(false);
				SecondGateDevice.getInstance().LightGreenLED();
				CommUtil.sleep(20);
				for (int rc = 0; rc < 2; rc++) {
					if (!this.isReceiveOpenSecondDoorCmd) {
						SecondGateDevice.getInstance().openTheSecondDoor();
					} else {
						log.info("门模块已经收到开门指令");
						break;
					}
					CommUtil.sleep(120);
				}
			}
			log.info("开门指令已发");
			this.setDealDeviceEvent(true);// 允许处理新的事件
			this.setDeviceReader(true);// 恢复寻卡
			TicketVerifyScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketDefault.getValue(), null, null, null)); // 恢复初始界面

		} else if (ticketVerifyResult.equals(Config.TicketVerifyStationRuleFail)) { // 非本站出闸
			// GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_InvalidStation);

			TicketVerifyScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketVerifyStationRuleFail.getValue(), ticketVerify.getTicket(), ticketVerify.getIdCard(), null));
			CommUtil.sleep(600);
			ticketVerify.reset();
			this.setDealDeviceEvent(true);// 允许处理新的事件
			this.setDeviceReader(true);// 恢复寻卡
		} else if (ticketVerifyResult.equals(Config.TicketVerifyTrainDateRuleFail)) { // 非当日出闸
			// GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_InvalidStation);

			TicketVerifyScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketVerifyTrainDateRuleFail.getValue(),
					ticketVerify.getTicket(), ticketVerify.getIdCard(), null));
			CommUtil.sleep(600);
			ticketVerify.reset();
			this.setDealDeviceEvent(true);// 允许处理新的事件
			this.setDeviceReader(true);// 恢复寻卡
		}
	}

	/**
	 * 本函数主要用于票证核验以及后续处理
	 */
	private void verifyTicket() {

		this.setDeviceReader(false);// 停止寻卡

		/**
		 * 已经是不同旅客的车票,则将当前车票map清空
		 */
		if (this.ticketVerify.getTicket() != null && this.ticketPassMap.get(this.ticketVerify.getTicket().getTicketNo()) == null) {
			this.ticketPassMap.clear();
			log.info("已经是不同旅客的车票,则将当前车票map清空");
		}

		/**
		 * 20170529 开始核验票证前增加防尾随检测
		 */
		if (Config.getInstance().getIsUseTrail() == 1) { // 启用防尾随检测
			for (int tc = 0; tc < 2; tc++) {
				CommUtil.sleep(200);
				log.info("防尾随状态==" + this.trailingStatus);
				if (this.trailingStatus != 1 && this.trailingStatus != 2 && this.trailingStatus != 5) {
					break;
				} else {
					int releaseCount = 0;
					while (true) {
						CommUtil.sleep(40);
						log.info("等待获取最新的防尾随状态,releaseCount = " + releaseCount);
						if (this.trailingStatus != 1 && this.trailingStatus != 2 && this.trailingStatus != 5) {
							releaseCount++;
						} else {
							releaseCount = 0;
						}
						if (releaseCount >= 5) {
							break;
						}
					}
				}
			}
			log.info("$$$$$$$$$跳出核验票证前增加防尾随检测等待循环$$$$$$$$");
		}

		String ticketVerifyResult = "";
		if (Config.getInstance().getIsConnectTRService() == 1) {
			ticketVerifyResult = ticketVerify.verifyFromTK(); // 联机票证核验
		} else if (Config.getInstance().getIsUseWharfMQ() == 1) {
			ticketVerifyResult = ticketVerify.verifyShipTicket(); // 电子船票联机核验
		} else if (Config.getInstance().getIsConnectLvFu() == 1) {
			if (Config.getInstance().getIsGGQChaoshanMode() == 0) {
				ticketVerifyResult = ticketVerify.verifyFromLvFuService(); // 连接旅服系统校验
			} else {
				ticketVerifyResult = ticketVerify.verify(); // 潮汕聯機檢票
			}
		} else {
			ticketVerifyResult = ticketVerify.verify(); // 单机票证核验
		}

		if (Config.TicketVerifySucc.equals(ticketVerifyResult)) { // 票证核验成功
			// this.setDeviceReader(false);// 停止寻卡

			// DeviceConfig.getInstance().setSecondGateOpenCount(0);

			this.childCheckCount = 0; // 判断是否儿童次数值重置

			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Audio_TakeCard_Jms);

			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {
				TicketVerifyScreen.getInstance().offerEvent(
						new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketVerifySucc.getValue(), ticketVerify.getTicket(), ticketVerify.getIdCard(), null));
			} else {

			}

			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) { // 双门模式需要开第一道门

				// this.waitIdCardLeave(20); // 做二代证离场检测

				log.info("票证核验通过，停止寻卡，打开第一道闸门，开始人证比对");
				if (Config.getInstance().getIsUseGatDll() == 0) {
					this.setReceiveOpenFirstDoorCmd(false);
					for (int rc = 0; rc < 2; rc++) {
						if (!this.isReceiveOpenFirstDoorCmd) {
							FirstGateDevice.getInstance().openFirstDoor();
						} else {
							break;
						}
						CommUtil.sleep(120);
					}
				} else {
					GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.OPEN_FIRSTDOOR);
				}

				if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {
					TicketVerifyScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowBeginCheckFaceContent.getValue(),
							ticketVerify.getTicket(), ticketVerify.getIdCard(), null));
				} else {

				}
			} else { // 单门模式-不用管第1道门

			}

			// 开始进行人脸检测和比对
			String begintime = CalUtils.getStringDateHaomiao(); // 开始检脸时间
			if (this.verifyFace(ticketVerify.getIdCard(), ticketVerify.getTicket()) == 0) { // 人脸比对通过

				if (Config.getInstance().getDoorCountMode() == DeviceConfig.SINGLEDOOR) { // 单门机型首先显示请通过

					// int displayTimeOut = 5 - 1;
					log.info("开始调用CAM_ScreenDisplay");
					// CAMDevice.getInstance().CAM_ScreenDisplay("人证核验成功#请通过",
					// displayTimeOut);
					int displayRetVal = -1;
					MqttCamRequestSender.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendMessage(CAMJsonUtil.createCAM_ScreenDisplay("人证核验成功#请通过", 4));
					try {
						ScreenDisplayBean screenDisplayBean = DeviceEventListener.getInstance().pollCamScreenDisplayQueue();
						if (screenDisplayBean != null) {
							displayRetVal = 0;
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						log.error("CAM_ScreenDisplay:", e);
					}
					log.info("调用CAM_ScreenDisplay收到返回值 = " + displayRetVal);
				}

				// this.waitIdCardLeave(20); // 做二代证离场检测

				if (Config.getInstance().getIsNeedUseFirstDoor() == 1) { // 当双门机型运行单门模式时，需要在此控制开第前门
					/**
					 * 打开第一道闸门
					 */
					log.info("票证人比对通过，停止寻卡，打开第一道闸门");
					if (Config.getInstance().getIsUseGatDll() == 0) {
						this.setReceiveOpenFirstDoorCmd(false);
						for (int rc = 0; rc < 2; rc++) {
							if (!this.isReceiveOpenFirstDoorCmd) {
								FirstGateDevice.getInstance().openFirstDoor();
							} else {
								break;
							}
							CommUtil.sleep(120);
						}
					}
					/******************************************************************/

					CommUtil.sleep(1000);
				}

				log.info("人脸比对通过，开第二道闸门");

				boolean isAdmitPass = true;

				if (Config.getInstance().getIsUseGatDll() == 0) { // 不调用门控动态库
					DeviceConfig.getInstance().setSecondGateOpenCount(0);
					this.setReceiveOpenSecondDoorCmd(false);

					/**
					 * 需要等待前门关闭才可以开后门
					 */
					if (Config.getInstance().getOpenSecondDoorMode() == 2) { // 需要等待前门关闭才可以开后门

						boolean logflag = true;
						for (int sc = 0; sc < Config.getInstance().getWaitFirstDoorClosedTime() * 100; sc++) {
							// while (true) {
							if (logflag) {
								log.info("进入循环等待，等待前门关闭才可以开后门...");
								logflag = false;
							}
							if (DeviceConfig.getInstance().isFirstGateClosed()) {
								log.info("前门已经关闭,跳出循环！");
								break;
							}
							CommUtil.sleep(10);
						}
					}

					/**
					 * 20170430 增加防尾随检测
					 */
					if (Config.getInstance().getIsUseTrail() == 1) { // 启用防尾随检测
						log.info("开始进入防尾随检测...");
						for (int tc = 0; tc < 2; tc++) {
							CommUtil.sleep(200);
							log.info("防尾随状态==" + this.trailingStatus);
							if (this.trailingStatus != 1 && this.trailingStatus != 2 && this.trailingStatus != 5) {
								break;
							} else {
								int releaseCount = 0;
								while (true) {
									CommUtil.sleep(40);
									log.info("等待获取最新的防尾随状态,releaseCount = " + releaseCount);
									if (this.trailingStatus != 1 && this.trailingStatus != 2 && this.trailingStatus != 5) {
										releaseCount++;
									} else {
										releaseCount = 0;
									}
									if (releaseCount >= 5 || this.childCheckCount >= Config.getInstance().getJudgeIsChildCount()) {
										break;
									}
								}
							}
						}
						log.info("$$$$$$$$$跳出防尾随检测等待循环$$$$$$$$");
					}

					String endtime = CalUtils.getStringDateHaomiao();
					long hh = (long) 0;
					try {
						hh = CalUtils.howLong("ms", begintime, endtime);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						log.error("", e);
					}

					if (hh > Config.getInstance().getFaceCheckDelayTime() * 1000) {
						isAdmitPass = false;
					}

					if (isAdmitPass) {
						/**
						 * 发送开2门指令
						 */
						SecondGateDevice.getInstance().LightGreenLED();
						CommUtil.sleep(20);
						for (int rc = 0; rc < 2; rc++) {
							if (!this.isReceiveOpenSecondDoorCmd) {
								SecondGateDevice.getInstance().openTheSecondDoor();
							} else {
								break;
							}
							CommUtil.sleep(120);
						}
					}
				} else { // 使用门控动态库
					// GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.OPEN_SECONDDOOR);
				}

				if (isAdmitPass) { // 允许开门放行
					if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {
						String display_str = "人证核验成功#请通过";
						if (Config.getInstance().getIsConnectLvFu() == 1) {
							String trainCode = this.ticketVerify.getTicket().getTrainCode();
							String trainDate = this.ticketVerify.getTicket().getTrainDate().substring(0, 4) + "-"
									+ this.ticketVerify.getTicket().getTrainDate().substring(4, 6) + "-" + this.ticketVerify.getTicket().getTrainDate().substring(6, 8);
							log.info("train_key==" + trainCode + trainDate);
							TrainInfomation tis = this.trainInfoMap.get(trainCode + trainDate);
							log.info("tis == " + tis);
							if (tis != null && tis.getWaitRoom() != null && !tis.getWaitRoom().trim().equals("")) {
								display_str = "请到" + tis.getWaitRoom() + "候车";
							}
						}

						int displayTimeOut = 5 - 1;
						log.info("开始调用CAM_ScreenDisplay");
						//
						// CAMDevice.getInstance().CAM_ScreenDisplay("人证核验成功#请通过",
						// displayTimeOut);
						int displayRet = -1;
						MqttCamRequestSender.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendMessage(CAMJsonUtil.createCAM_ScreenDisplay(display_str, displayTimeOut));
						try {
							ScreenDisplayBean screenDisplayBean = DeviceEventListener.getInstance().pollCamScreenDisplayQueue();
							if (screenDisplayBean != null) {
								displayRet = 0;
							}
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							log.error("CAM_ScreenDisplay:", e);
						}
						log.info("调用CAM_ScreenDisplay收到返回值 = " + displayRet);
					}

					// // 更新监控状态
					flapCount = flapCount + 1;
					MonitorXMLUtil.updateFlapTotalForMonitor(Config.getInstance().getMyStatusXML(), this.flapCount);

					/**
					 * 票证人核验通过，开门成功:上传回执照片
					 */
					if (Config.getInstance().getIsConnectTRService() == 1) {
						int photoLen2 = this.faceVerifyResult.getPhotoLen2();
						int photoLen3 = this.faceVerifyResult.getPhotoLen3();
						int[] kk = new int[2];
						kk[0] = photoLen2;
						kk[1] = photoLen3;
						byte[] iPhoto = new byte[photoLen2 + photoLen3];
						for (int k = 0; k < photoLen2; k++) {
							iPhoto[k] = faceVerifyResult.getPhoto2()[k];
						}
						for (int k = 0; k < photoLen3; k++) {
							iPhoto[k + photoLen2] = faceVerifyResult.getPhoto3()[k];
						}
						int retval = TRService.ReceiptStatus("0000" + faceVerifyResult.getUuid(), iPhoto, kk);
						log.info("票证人通过:上传照片回执==" + retval);
					}
				} else { // 已超时，禁止放行
					if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) { // 双门模式
						log.info("已超时，禁止放行");
						if (Config.getInstance().getIsUseGatDll() == 0) {// 不调用门控dll
							FirstGateDevice.getInstance().LightRedLED();
							FirstGateDevice.getInstance().openEmerDoor(); // 打开电磁门
						} else { // 要调用门控动态库
							// GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.OPEN_THIRDDOOR);
						}

						int displayTimeOut = 5 - 1;
						log.info("开始调用CAM_ScreenDisplay");
						// CAMDevice.getInstance().CAM_ScreenDisplay("人证核验失败#请从侧门离开",
						// displayTimeOut);
						int displayRet = -1;
						MqttCamRequestSender.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT)
								.sendMessage(CAMJsonUtil.createCAM_ScreenDisplay("人证核验失败#请从侧门离开", displayTimeOut));
						try {
							ScreenDisplayBean screenDisplayBean = DeviceEventListener.getInstance().pollCamScreenDisplayQueue();
							if (screenDisplayBean != null) {
								displayRet = 0;
							}
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							log.error("CAM_ScreenDisplay:", e);
						}
						log.info("调用CAM_ScreenDisplay收到返回值 = " + displayRet);
					} else { // 单门模式
						int displayTimeOut = 5 - 1;
						log.info("开始调用CAM_ScreenDisplay");
						// CAMDevice.getInstance().CAM_ScreenDisplay("人证核验失败#请走人工通道",
						// displayTimeOut);
						int displayRet = -1;
						MqttCamRequestSender.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT)
								.sendMessage(CAMJsonUtil.createCAM_ScreenDisplay("人证核验失败#请走人工通道", displayTimeOut));
						try {
							ScreenDisplayBean screenDisplayBean = DeviceEventListener.getInstance().pollCamScreenDisplayQueue();
							if (screenDisplayBean != null) {
								displayRet = 0;
							}
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							log.error("CAM_ScreenDisplay:", e);
						}
						log.info("调用CAM_ScreenDisplay收到返回值 = " + displayRet);
					}

					if (Config.getInstance().getIsConnectTRService() == 1) {
						/**
						 * 票证人核验通过，开门失败:上传回执照片
						 */
						if (this.faceVerifyResult != null) {
							int photoLen2 = this.faceVerifyResult.getPhotoLen2();
							int photoLen3 = this.faceVerifyResult.getPhotoLen3();
							int[] kk = null;
							if (photoLen3 > 0) {
								kk = new int[2];
							} else if (photoLen2 > 0) {
								kk = new int[1];
							}
							if (kk == null) {
								log.info("没有检测到任何一张人脸，不上传回执");
							} else {
								if (photoLen2 > 0)
									kk[0] = photoLen2;
								if (photoLen3 > 0)
									kk[1] = photoLen3;
								byte[] iPhoto = new byte[photoLen2 + photoLen3];
								if (photoLen2 > 0) {
									for (int k = 0; k < photoLen2; k++) {
										iPhoto[k] = faceVerifyResult.getPhoto2()[k];
									}
								}
								if (photoLen3 > 0) {
									for (int k = 0; k < photoLen3; k++) {
										iPhoto[k + photoLen2] = faceVerifyResult.getPhoto3()[k];
									}
								}
								int retval = TRService.ReceiptStatus("0001" + faceVerifyResult.getUuid(), iPhoto, kk);
								log.info("票证人核验通过，开门失败:上传照片回执==" + retval);
							}
						}
					}

					CommUtil.sleep(5 * 1000);

					if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) { // 双门模式
						TicketVerifyScreen.getInstance().offerEvent(
								new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketDefault.getValue(), ticketVerify.getTicket(), ticketVerify.getIdCard(), null));
					} else { // 单门模式

					}

					ticketVerify.reset(); // 清理读到的票证事件
					DeviceEventListener.getInstance().setDeviceReader(true); // 允许寻卡
					DeviceEventListener.getInstance().setDealDeviceEvent(true); // 允许处理新的事件
				}

				// -------------------------------------------------------------------

				if (Config.getInstance().getDoorCountMode() == DeviceConfig.SINGLEDOOR) { // 单门模式下不用等待闸门关闭即可恢复寻卡
					// DeviceEventListener.getInstance().setDeviceReader(true);
					// // 允许寻卡
					// DeviceEventListener.getInstance().setDealDeviceEvent(true);
					// // 允许处理新的事件
				}
			} else { // 人脸比对失败
				if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) { // 双门模式
					log.info("人脸比对失败，开第三道电磁门");
					if (Config.getInstance().getIsUseGatDll() == 0) {// 不调用门控dll
						FirstGateDevice.getInstance().LightRedLED();
						FirstGateDevice.getInstance().openEmerDoor(); // 打开电磁门
					} else { // 要调用门控动态库
						// GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.OPEN_THIRDDOOR);
					}

					int displayTimeOut = 5 - 1;
					log.info("开始调用CAM_ScreenDisplay");
					// CAMDevice.getInstance().CAM_ScreenDisplay("人证核验失败#请从侧门离开",
					// displayTimeOut);
					int displayRet = -1;
					MqttCamRequestSender.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendMessage(CAMJsonUtil.createCAM_ScreenDisplay("人证核验失败#请从侧门离开", displayTimeOut));
					try {
						ScreenDisplayBean screenDisplayBean = DeviceEventListener.getInstance().pollCamScreenDisplayQueue();
						if (screenDisplayBean != null) {
							displayRet = 0;
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						log.error("CAM_ScreenDisplay:", e);
					}
					log.info("调用CAM_ScreenDisplay收到返回值 = " + displayRet);
				} else { // 单门模式
					int displayTimeOut = 5 - 1;
					log.info("开始调用CAM_ScreenDisplay");
					// CAMDevice.getInstance().CAM_ScreenDisplay("人证核验失败#请走人工通道",
					// displayTimeOut);
					int displayRet = -1;
					MqttCamRequestSender.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendMessage(CAMJsonUtil.createCAM_ScreenDisplay("人证核验失败#请走人工通道", displayTimeOut));
					try {
						ScreenDisplayBean screenDisplayBean = DeviceEventListener.getInstance().pollCamScreenDisplayQueue();
						if (screenDisplayBean != null) {
							displayRet = 0;
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						log.error("CAM_ScreenDisplay:", e);
					}
					log.info("调用CAM_ScreenDisplay收到返回值 = " + displayRet);
				}

				if (Config.getInstance().getIsConnectTRService() == 1) {
					/**
					 * 人脸不通过(票证通过, 不开门):上传照片回执
					 */
					if (this.faceVerifyResult != null) {
						int photoLen2 = this.faceVerifyResult.getPhotoLen2();
						int photoLen3 = this.faceVerifyResult.getPhotoLen3();
						int[] kk = null;
						if (photoLen3 > 0) {
							kk = new int[2];
						} else if (photoLen2 > 0) {
							kk = new int[1];
						}
						if (kk == null) {
							log.info("没有检测到任何一张人脸，不上传回执");
						} else {
							if (photoLen2 > 0)
								kk[0] = photoLen2;
							if (photoLen3 > 0)
								kk[1] = photoLen3;
							byte[] iPhoto = new byte[photoLen2 + photoLen3];
							if (photoLen2 > 0) {
								for (int k = 0; k < photoLen2; k++) {
									iPhoto[k] = faceVerifyResult.getPhoto2()[k];
								}
							}
							if (photoLen3 > 0) {
								for (int k = 0; k < photoLen3; k++) {
									iPhoto[k + photoLen2] = faceVerifyResult.getPhoto3()[k];
								}
							}
							int retval = TRService.ReceiptStatus("0003" + faceVerifyResult.getUuid(), iPhoto, kk);
							log.info("人脸不通过(票证通过, 不开门):上传照片回执==" + retval);
						}
					}
				}

				CommUtil.sleep(5 * 1000);

				if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) { // 双门模式
					TicketVerifyScreen.getInstance().offerEvent(
							new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketDefault.getValue(), ticketVerify.getTicket(), ticketVerify.getIdCard(), null));
				} else { // 单门模式

				}

				ticketVerify.reset(); // 清理读到的票证事件
				DeviceEventListener.getInstance().setDeviceReader(true); // 允许寻卡
				DeviceEventListener.getInstance().setDealDeviceEvent(true); // 允许处理新的事件
			}

			ticketVerify.reset();

		} else if (Config.TicketVerifyWaitInput.equals(ticketVerifyResult)) { // 等待票证验证数据
			if (ticketVerify.getTicket() == null || ticketVerify.getIdCard() == null) {
				if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) { // 双门模式

					if (ticketVerify.getIdCard() == null) {
						GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_ReadIDCardFailed);
						TicketVerifyScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketVerifyWaitInput.getValue(),
								ticketVerify.getTicket(), ticketVerify.getIdCard(), null));
					} else {
						GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_ReadTicketFailed);
						TicketVerifyScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketVerifyWaitInput.getValue(),
								ticketVerify.getTicket(), ticketVerify.getIdCard(), null));
					}

				} else { // 单门模式
					log.debug("等待票证验证数据,ticketVerifyResult==" + ticketVerifyResult);
					if (ticketVerify.getIdCard() == null) {
						// GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_ReadIDCardFailed);
					} else {
						GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_ReadTicketFailed);
					}
				}
			}
			this.setDealDeviceEvent(true);// 允许处理新的事件
			this.setDeviceReader(true);// 恢复寻卡
		} else if (ticketVerifyResult.equals(Config.TicketVerifyIDFail)) { // 票证不一致
			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) { // 双门模式
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_InvalidCardAndTicket);

				TicketVerifyScreen.getInstance().offerEvent(
						new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketVerifyIDFail.getValue(), ticketVerify.getTicket(), ticketVerify.getIdCard(), null));
			} else { // 单门模式
				log.debug("票证验证失败,ticketVerifyResult==" + ticketVerifyResult);
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_InvalidCardAndTicket);
			}

			CommUtil.sleep(600);
			ticketVerify.reset();
			this.setDealDeviceEvent(true);// 允许处理新的事件
			this.setDeviceReader(true);// 恢复寻卡
		} else if (ticketVerifyResult.equals(Config.TicketVerifyStationRuleFail)) { // 非本站乘车
			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) { // 双门模式
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_InvalidStation);

				TicketVerifyScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketVerifyStationRuleFail.getValue(),
						ticketVerify.getTicket(), ticketVerify.getIdCard(), null));
			} else {
				log.debug("车票未通过非本站规则,ticketVerifyResult==" + ticketVerifyResult);
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_InvalidStation);
			}
			CommUtil.sleep(600);
			ticketVerify.reset();
			this.setDealDeviceEvent(true);// 允许处理新的事件
			this.setDeviceReader(true);// 恢复寻卡
		} else if (ticketVerifyResult.equals(Config.TicketVerifyTrainDateRuleFail)) { // 非当日乘车
			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) { // 双门模式
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_InvalidTrainDate);

				TicketVerifyScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketVerifyTrainDateRuleFail.getValue(),
						ticketVerify.getTicket(), ticketVerify.getIdCard(), null));
			} else {
				log.debug("车票未通过非当日规则,ticketVerifyResult==" + ticketVerifyResult);
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_InvalidTrainDate);
			}
			CommUtil.sleep(600);
			ticketVerify.reset();
			this.setDealDeviceEvent(true);// 允许处理新的事件
			this.setDeviceReader(true);// 恢复寻卡
		} else if (ticketVerifyResult.equals(Config.TicketVerifyNotStartCheckFail)) { // 车票未到进站时间
			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) { // 双门模式
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_NotInTime);

				TicketVerifyScreen.getInstance().offerEvent(
						new ScreenElementModifyEvent(0, ScreenCmdEnum.showETicketNotInTime.getValue(), ticketVerify.getTicket(), ticketVerify.getIdCard(), null));
			} else {
				log.debug("车票未到进站时间,ticketVerifyResult==" + ticketVerifyResult);
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_NotInTime);
			}
			CommUtil.sleep(600);
			ticketVerify.reset();
			this.setDealDeviceEvent(true);// 允许处理新的事件
			this.setDeviceReader(true);// 恢复寻卡
		} else if (ticketVerifyResult.equals(Config.TicketVerifyStopCheckFail)) { // 车票已过进站时间
			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) { // 双门模式
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_PassCheckTime);

				TicketVerifyScreen.getInstance().offerEvent(
						new ScreenElementModifyEvent(0, ScreenCmdEnum.showETicketPassTime.getValue(), ticketVerify.getTicket(), ticketVerify.getIdCard(), null));
			} else {
				log.debug("车票未到进站时间,ticketVerifyResult==" + ticketVerifyResult);
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_PassCheckTime);
			}
			CommUtil.sleep(600);
			ticketVerify.reset();
			this.setDealDeviceEvent(true);// 允许处理新的事件
			this.setDeviceReader(true);// 恢复寻卡
		} else if (ticketVerifyResult.equals(Config.TicketVerifyTrainStopped)) { // 列车已经停运
			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) { // 双门模式
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_TrainStopped);

				TicketVerifyScreen.getInstance()
						.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.showTrainStopped.getValue(), ticketVerify.getTicket(), ticketVerify.getIdCard(), null));
			} else {
				log.debug("列车已经停运,ticketVerifyResult==" + ticketVerifyResult);
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_TrainStopped);
			}
			CommUtil.sleep(600);
			ticketVerify.reset();
			this.setDealDeviceEvent(true);// 允许处理新的事件
			this.setDeviceReader(true);// 恢复寻卡
		} else if (ticketVerifyResult.equals(Config.TicketVerifyInvalidTrainCode)) { // 无效数据或无此车次
			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) { // 双门模式
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_InvalidTrain);

				TicketVerifyScreen.getInstance()
						.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.showInvalidTrain.getValue(), ticketVerify.getTicket(), ticketVerify.getIdCard(), null));
			} else {
				log.debug("无效数据或无此车次,ticketVerifyResult==" + ticketVerifyResult);
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_InvalidTrain);
			}
			CommUtil.sleep(600);
			ticketVerify.reset();
			this.setDealDeviceEvent(true);// 允许处理新的事件
			this.setDeviceReader(true);// 恢复寻卡
		} else if (ticketVerifyResult.equals(Config.TicketVerifyRepeatCheck)) { // 重复验票
			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) { // 双门模式
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_RepeatCheck);

				TicketVerifyScreen.getInstance()
						.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.showRepeatCheck.getValue(), ticketVerify.getTicket(), ticketVerify.getIdCard(), null));
			} else {
				log.debug("重复刷证,ticketVerifyResult==" + ticketVerifyResult);
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_RepeatCheck);
			}
			CommUtil.sleep(600);
			ticketVerify.reset();
			this.setDealDeviceEvent(true);// 允许处理新的事件
			this.setDeviceReader(true);// 恢复寻卡
		} else { // 中铁程返回的代码
			log.info("中铁程返回,ticketVerifyResult==" + ticketVerifyResult);
			if (ticketVerifyResult.equals("000001")) {
				ticketVerifyResult = "-80002";
			}
			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) { // 双门模式
				int errCode = Integer.parseInt(ticketVerifyResult);
				this.notifyAudioEvent(errCode);
				TicketVerifyScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0, errCode, ticketVerify.getTicket(), ticketVerify.getIdCard(), null));
			} else {
				int errCode = Integer.parseInt(ticketVerifyResult);
				this.notifyAudioEvent(errCode);
			}

			/**
			 * 票证不通过，人脸不通过:上传身份证照片
			 */
			// if (Config.getInstance().getIsConnectTRService() == 1) {
			// IDCard errIdCard = ticketVerify.getIdCard();
			// int tt = errIdCard.getCardImageBytes().length;
			// int[] kk = new int[1];
			// kk[0] = errIdCard.getCardImageBytes().length;
			// byte[] iPhoto = new byte[tt];
			// for (int k = 0; k < tt; k++) {
			// iPhoto[k] = errIdCard.getCardImageBytes()[k];
			// }
			// int retval =
			// TicketReservationService.getInstance().ReceiptStatus("0004" +
			// errIdCard.getIdNo(), errIdCard.getCardImageBytes(), kk);
			// log.info("票证不通过，人脸不通过:上传身份证照片回执==" + retval);
			// }

			// this.waitIdCardLeave(20); // 做二代证离场检测

			// CommUtil.sleep(600);

			ticketVerify.reset();
			this.deviceEventQueue.clear();
			this.setDealDeviceEvent(true);// 允许处理新的事件
			this.setDeviceReader(true);// 恢复寻卡
		}
	}

	/**
	 * 通知与中铁程返回代码相应的语音事件
	 * 
	 * @param errCode
	 */
	private void notifyAudioEvent(int errCode) {
		switch (errCode) {
		case -80244: // 乘车站不符,禁止异地上车
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_InvalidStation);
			break;
		case -80250: // 检票次数超限
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_RepeatCheck);
			break;
		case -80253: // 票不符,已到站
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_InvalidStation);
			break;
		case -80255: // 越站乘车
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_InvalidStation);
			break;
		case -80256: // 未到进站时间
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_NotInTime);
			break;
		case -80257: // 已过进站时间
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_PassCheckTime);
			break;
		case -80258: // 查询到站名失败
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_ReadTicketFailed);
			break;
		case -80259: // 查询中转站名失败
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_ReadTicketFailed);
			break;
		case -80260: // 查询终端交易流水号失败
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_ReadTicketFailed);
			break;
		case -80261: // 保存核验记录失败
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_ReadTicketFailed);
			break;
		case -80262: // 票不符,列车不经过本站
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_InvalidStation);
			break;
		case -80263: // 获取电子票出错
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_ReadTicketFailed);
			break;
		case -80264: // 解析电子票数据失败
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_ReadTicketFailed);
			break;
		case -80265: // 售处号或票号无效
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_ReadTicketFailed);
			break;
		case -80266: // 车票列车信息无效
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_ReadTicketFailed);
			break;
		case -80267: // 实名信息无效
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_ReadTicketFailed);
			break;
		case -80268: // 窗口号无效
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_ReadTicketFailed);
			break;
		case -80270: // 票种无效
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_ReadTicketFailed);
			break;
		case -80271: // 票价无效
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_ReadTicketFailed);
			break;
		case -80272: // 核对列车开行状态失败
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_ReadTicketFailed);
			break;
		case -80273: // 查询核验记录失败
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_ReadTicketFailed);
			break;
		case -80274: // 检票次数超限
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_RepeatCheck);
			break;
		case -80275: // 未到进站时间
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_NotInTime);
			break;
		case -80276: // 已过进站时间
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_PassCheckTime);
			break;
		case -80277: // 无电子票信息
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_ReadTicketFailed);
			break;
		case -80281: // 无电子票信息
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_ReadTicketFailed);
			break;
		case -80002: // 票证不符
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_InvalidCardAndTicket);
			break;
		case -89001: // 单据不存在
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_NoShipTicket);
			break;
		case -89002: // 船票不符
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_InvalidShipTicket);
			break;
		case -89004: // 内部服务器错误
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_ConnTicketCerterFailed);
			break;
		default:
			log.info("未知错误代码==" + errCode);
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(ProcessUtil.createTkEventJson(errCode, "TK"));
			break;
		}
	}

	/**
	 * 通知开始检脸 同时等待比对结果
	 * 
	 * @param idCard
	 * @param ticket
	 */
	private int verifyFace(IDCard idCard, Ticket ticket) {
		this.resetCamDataQueue();

		String uuidStr = idCard.getIdNo();
		String IDPhoto_str = "zp.jpg";
		log.debug("身份证路径==" + IDPhoto_str);
		log.info("开始调用CAM_Notify");
		// int notifyRet = CAMDevice.getInstance().CAM_Notify(1, uuidStr,
		// IDPhoto_str);
		int notifyRet = -1;
		// String notify_json = CAMJsonUtil.createCAMNotifyJson(1, uuidStr,
		// IDPhoto_str);
		String notify_json = CAMJsonUtil.createCAMNotifyJson(1, idCard);
		MqttCamRequestSender.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendMessage(notify_json);
		try {
			CAMNotifyBean camNotifyBean = DeviceEventListener.getInstance().pollCamNotifyDataQueue();
			if (camNotifyBean != null) {
				notifyRet = 0;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			log.error("CAM_Notify:", e);
		}
		log.info("调用CAM_Notify收到返回值 = " + notifyRet);

		int getPhotoRet = -1;
		if (notifyRet == 0) {
			log.info("开始调用CAM_GetPhotoInfo");
			int delaySeconds = 10;
			// getPhotoRet = CAMDevice.getInstance().CAM_GetPhotoInfo(uuidStr,
			// delaySeconds); // 此处传入的delaySeconds是为了控制检脸通过速率而设置
			MqttCamRequestSender.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendMessage(CAMJsonUtil.createCAM_GetPhotoInfoRequest(uuidStr, delaySeconds));
			try {
				PIVerifyResultBean piVerifyResultBean = DeviceEventListener.getInstance().pollCamGetPhotoInfoQueue();
				if (piVerifyResultBean != null) {
					getPhotoRet = 0;
					this.faceVerifyResult = piVerifyResultBean;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				log.error("CAM_GetPhotoInfo:", e);
			}
		}
		log.info("调用CAM_GetPhotoInfo收到返回值 = " + getPhotoRet);
		return getPhotoRet;
	}

	/**
	 * 等待二代证离场
	 */
	private void waitIdCardLeave(int iTime) {
		int cc = 0;
		while (true) {
			CommUtil.sleep(iTime);
			int idcCheck = RSIVIDcardDevice.getInstance().IDC_Check();
			log.info("idcCheck==" + idcCheck + "#");
			if (idcCheck == 0) {
				cc++;
			}
			if (cc >= 2) {
				log.info("二代证已经离场");
				break;
			}
		}
	}

	/**
	 * 启动设备
	 * 
	 * @throws DeviceException
	 */
	public void startDevice() throws DeviceException {

		// String pauseFlagStr = ProcessUtil.getLastPauseFlag();
		// log.debug("pauseFlagStr==" + pauseFlagStr);
		// if (pauseFlagStr != null) {
		// if (pauseFlagStr.indexOf("kill") != -1) {
		// log.debug("启动暂停服务版本...");
		// TicketVerifyScreen.getInstance().offerEvent(
		// new ScreenElementModifyEvent(0,
		// ScreenCmdEnum.ShowStopCheckFault.getValue(), null, null, null));
		// ProcessUtil.writePauseLog(ProcessUtil.getCurrentProcessID());
		// this.isStartThread = false;
		// Config.getInstance().setIsUseManualMQ(0);
		// if (this.startGateDevice() != 1) {
		// return;
		// }
		// LightEntryLED(false);
		// return;
		// }
		// }
		//
		// ProcessUtil.writePauseLog(ProcessUtil.getCurrentProcessID());

		GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_MainCrtlStartupSucc);

		log.debug("核验软件版本");
		if (DeviceConfig.getInstance().getVersionFlag() != 1) {
			log.debug("软件版本错误:" + DeviceConfig.getInstance().getVersionFlag());
			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {// 双门模式
				TicketVerifyScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowVersionFault.getValue(), null, null, null));
			}
			this.isStartThread = false;
			LightEntryLED(false);
			return;
		}

		try {
			MonitorXMLUtil.createBaseInfoXml(Config.getInstance().getBaseInfoXMLPath(), "001");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			log.error("", e1);
		}

		try {
			File statusInfoXmlFile = new File(Config.getInstance().getStatusInfoXMLPath());

			if (statusInfoXmlFile.exists()) {

			} else {
				Map<String, String> map = new HashMap<String, String>();
				map.put("StatusInf", "0");
				map.put("MaintainsDateTime", CalUtils.getStringDate());
				map.put("FlapTotal", "0");
				map.put("FlapUpdTime", CalUtils.getStringDate());
				map.put("DoorFaultCode", "001001001001000");
				map.put("DoorMaintainsDateTime", CalUtils.getStringDate());
				map.put("FaceFaultCode", "001001001002000");
				map.put("FaceMaintainsDateTime", CalUtils.getStringDate());
				map.put("IDCardFaultCode", "001001001004000");
				map.put("IDCardMaintainsDateTime", CalUtils.getStringDate());
				map.put("QRCodeFaultCode", "001001001003000");
				map.put("QRCodeMaintainsDateTime", CalUtils.getStringDate());
				MonitorXMLUtil.createStatusInfoXml(Config.getInstance().getStatusInfoXMLPath(), map);
			}

			File mystatusXml = new File(Config.getInstance().getMyStatusXML());
			if (mystatusXml.exists()) {
				this.flapCount = Integer.parseInt(MonitorXMLUtil.getFlapCountFromStatusXML(Config.getInstance().getMyStatusXML()));
				log.info("闸门开门次数 = " + flapCount);
			} else {
				Map<String, String> map = new HashMap<String, String>();
				map.put("StatusInf", "0");
				map.put("MaintainsDateTime", CalUtils.getStringDate());
				map.put("FlapTotal", "0");
				map.put("FlapUpdTime", CalUtils.getStringDate());
				map.put("DoorFaultCode", "001001001001000");
				map.put("DoorMaintainsDateTime", CalUtils.getStringDate());
				map.put("FaceFaultCode", "001001001002000");
				map.put("FaceMaintainsDateTime", CalUtils.getStringDate());
				map.put("IDCardFaultCode", "001001001004000");
				map.put("IDCardMaintainsDateTime", CalUtils.getStringDate());
				map.put("QRCodeFaultCode", "001001001003000");
				map.put("QRCodeMaintainsDateTime", CalUtils.getStringDate());

				MonitorXMLUtil.createStatusInfoXml(Config.getInstance().getMyStatusXML(), map);
				this.flapCount = 0;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("Init device:", e);
		}

		/**
		 * 启动门模块
		 */
		if (Config.getInstance().getIsUseGatDll() == 0) {// 不使用门控dll时，需要启动串口设备
			if (this.startGateDevice() != 1) {
				this.setDealDeviceEvent(false);// 停止处理新的事件

				if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {
					TicketVerifyScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowDoorGateExceptoin.getValue(), null, null, null));
				} else {
					if (Config.getInstance().getIsOutGateForCSQ() == 1) {
						TicketVerifyScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowDoorGateExceptoin.getValue(), null, null, null));
					} else {
						GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_QRDeviceException);
					}
				}

				MonitorXMLUtil.updateBaseInfoFonMonitor(Config.getInstance().getBaseInfoXMLPath(), "001", 1);
				MonitorXMLUtil.updateDoorStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), "001", "001", "001", "002");
				MonitorXMLUtil.updateEntirStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), 1);

				this.isStartThread = false;
				return;
			}
		}

		/**
		 * 启动摄像头
		 */
		if (Config.getInstance().getIsOutGateForCSQ() == 0) {
			if (this.OpenCAM() != 0) {
				this.setDealDeviceEvent(false);// 停止处理新的事件
				if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {// 双门模式
					TicketVerifyScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowCamOpenException.getValue(), null, null, null));
				}

				MonitorXMLUtil.updateBaseInfoFonMonitor(Config.getInstance().getBaseInfoXMLPath(), "002", 1);
				MonitorXMLUtil.updateDoorStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), "001", "001", "002", "002");
				MonitorXMLUtil.updateEntirStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), 1);

				this.isStartThread = false;
				LightEntryLED(false);
				return;
			}
		}

		/**
		 * 启动二代证读卡器
		 */
		if (Config.getInstance().getIsOutGateForCSQ() == 0) {
			if (this.startIDDevice() != 1) {

				MonitorXMLUtil.updateBaseInfoFonMonitor(Config.getInstance().getBaseInfoXMLPath(), "004", 1);
				MonitorXMLUtil.updateDoorStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), "001", "001", "004", "002");
				MonitorXMLUtil.updateEntirStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), 1);

				this.isStartThread = false;
				LightEntryLED(false);
				return;
			}
		}

		/**
		 * 启动二维码读卡器
		 */
		if (Config.getInstance().getIsUseWharfMQ() == 0) {
			if (this.startQRDevice() != 1) {

				MonitorXMLUtil.updateBaseInfoFonMonitor(Config.getInstance().getBaseInfoXMLPath(), "003", 1);
				MonitorXMLUtil.updateDoorStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), "001", "001", "003", "002");
				MonitorXMLUtil.updateEntirStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), 1);

				this.isStartThread = false;
				LightEntryLED(false);
				return;
			}
		}

		/**
		 * 中铁程客票接口初始化
		 */
		if (Config.getInstance().getIsConnectTRService() == 1) {
			if (TRService.Init(DeviceConfig.companyCode, DeviceConfig.gateMachineType, Config.getInstance().getGateSerialNo(), DeviceConfig.softVersionToTK) != 0) {
				log.info("InitOutStr==" + Integer.parseInt(TRService.getInitOutStr().substring(0, 6)));
				if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {// 双门模式
					TicketVerifyScreen.getInstance()
							.offerEvent(new ScreenElementModifyEvent(0, Integer.parseInt(TRService.getInitOutStr().substring(0, 6)), null, null, null));
				} else {
					int errcode = Integer.parseInt(TRService.getInitOutStr().substring(0, 6));
					GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(ProcessUtil.createTkEventJson(errcode, "TK"));
				}
				this.isStartThread = false;
				LightEntryLED(false);
				return;
			}
		}

		/**
		 * 启动定时任务
		 */
		// if (this.addQuartzJobs() != 1) {
		// this.isStartThread = false;
		// LightEntryLED(false);
		// return;
		// }

		/**
		 * 连接mq服务，启动mq receiver线程 传输数据至平板
		 */
		if (DeviceConfig.getInstance().getMqStartFlag() == 1) {
			ExecutorService executer = Executors.newCachedThreadPool();
			JmsReceiverTask jmsReceiverTask = new JmsReceiverTask();
			executer.execute(jmsReceiverTask);
		}

		ScheduledExecutorService scheduler = null;
		if (DeviceConfig.getInstance().getMqStartFlag() == 1) {
			scheduler = Executors.newScheduledThreadPool(3);
		} else {
			scheduler = Executors.newScheduledThreadPool(2);
		}

		if (Config.getInstance().getIsOutGateForCSQ() == 0) {
			scheduler.scheduleWithFixedDelay(IDReader.getInstance(), 0, 200, TimeUnit.MILLISECONDS);
		}

		if (Config.getInstance().getIsUseWharfMQ() == 0) {
			scheduler.scheduleWithFixedDelay(BarCodeReader.getInstance(), 0, 400, TimeUnit.MILLISECONDS);
		}
		// 门模块状态
		// scheduler.scheduleWithFixedDelay(GATStatusQuery.getInstance(), 0,
		// 100, TimeUnit.MILLISECONDS);

		// mq sender线程
		if (DeviceConfig.getInstance().getMqStartFlag() == 1) {
			scheduler.scheduleWithFixedDelay(JmsSenderTask.getInstance(), 0, 100, TimeUnit.MILLISECONDS);
		}

		/**
		 * 中铁程客票接口-上传设备状态
		 */
		if (Config.getInstance().getIsConnectTRService() == 1) {
			if (TRService.SendDeviceInfo("000000") != 0) {
				log.info("SendDeviceInfoOutStr==" + Integer.parseInt(TRService.getSendDeviceInfoOutStr().substring(0, 6)));
				if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {// 双门模式
					TicketVerifyScreen.getInstance()
							.offerEvent(new ScreenElementModifyEvent(0, Integer.parseInt(TRService.getSendDeviceInfoOutStr().substring(0, 6)), null, null, null));
				} else {
					int errcode = Integer.parseInt(TRService.getInitOutStr().substring(0, 6));
					GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(ProcessUtil.createTkEventJson(errcode, "TK"));
				}
				this.isStartThread = false;
				LightEntryLED(false);
				return;
			}
		}

		if (Config.getInstance().getIsConnectLvFu() == 1) {
			/**
			 * 
			 */
			if (Config.getInstance().getLvFuDatasource().equals("SOAP")) {
				GetTrainInfoFromSoap sqqt = new GetTrainInfoFromSoap();
				try {
					sqqt.sendSms();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					log.error("", e);
				}
			} else {
				GetTrainInfoFromTxt gtift = new GetTrainInfoFromTxt();
				gtift.getTrainInfoFromTxtFile();
			}
		}

		if (Config.getInstance().getIsUseGatDll() == 0) {
			LightEntryLED(true);
		}

		log.info("闸机主控程序启动成功!当前软件版本号:" + DeviceConfig.softVersion);

		if (Config.getInstance().getDoorCountMode() == DeviceConfig.SINGLEDOOR) { // 单门模式
			if (Config.getInstance().getIsOutGateForCSQ() == 0)
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_DeviceStartupSucc);
			else
				TicketVerifyScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketDefaultCSQ.getValue(), null, null, null));
		}

		if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {// 双门模式
			TicketVerifyScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketDefault.getValue(), null, null, null));
		}
	}

	/**
	 * 控制入口LED灯的显示
	 * 
	 * @param isArrow
	 * @return
	 */
	private int LightEntryLED(boolean isArrow) {
		int retval = -1;
		if (Config.getInstance().getIsUseGatDll() == 0) { // 不使用门控 dll
			if (isArrow) {
				if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {
					FirstGateDevice.getInstance().LightEntryArrow();// 启动成功点亮绿色通行箭头
				} else {
					SecondGateDevice.getInstance().LightEntryArrow();
				}
			} else {
				if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {
					FirstGateDevice.getInstance().LightEntryCross(); // 启动失败点亮红色叉
				} else {
					SecondGateDevice.getInstance().LightEntryCross();
				}
			}
		} else {
			retval = 0;
		}
		return retval;
	}

	/**
	 * 打开摄像头
	 * 
	 * @return
	 */
	private int OpenCAM() {
		int retVal = -1;
		if (Config.getInstance().getIsUseCamDll() == 1) {
			int x = 0; // 窗口左边界
			int y = 0; // 窗口顶边界。
			int cx = 640; // 以像素指定窗口的新的宽度。
			int cy = 480; // 以像素指定窗口的新的高度。
			int checkThreshold = 65; // 人脸比对阀值，取值0-100
			int iCollect = 1; // 摄像头采集图片方式 0:固采 1：预采
			int faceTimeout = Config.getInstance().getFaceCheckDelayTime(); // 人脸识别算法的超时时间，单位秒

			int[] region = { x, y, cx, cy, checkThreshold, iCollect, faceTimeout };
			log.info("开始调用CAM_Open");
			// retVal = CAMDevice.getInstance().CAM_Open(region);

			// MqttCamRequestSender.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendMessage(CAMJsonUtil.createCAMOpenJson(region));
			// CommUtil.sleep(100);
			// try {
			// CAMOpenBean camOpenBean =
			// DeviceEventListener.getInstance().pollCamOpenDataQueue();
			// if (camOpenBean != null) {
			// retVal = 0;
			// }
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// log.error("OpenCAM:", e);
			// }
			// log.info("调用CAM_Open收到返回值 = " + retVal);

			MqttCamRequestSender.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendMessage(CAMJsonUtil.createCAMOpenJson(region));
			try {
				CAMOpenBean bb = this.pollCamOpenDataQueue();
				if (bb != null) {
					retVal = 0;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				log.error("OpenCAM:", e);
			}

			log.info("调用CAM_Open收到返回值");
		} else {
			// ObjectMapper mapper = new ObjectMapper();
			// CAMOpenBean camOpenBean = new CAMOpenBean();
			// try {
			// String camOpenRequestJson =
			// mapper.writeValueAsString(camOpenBean);
			// MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendMessage("pub_topic",
			// camOpenRequestJson);
			// } catch (JsonProcessingException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		}
		return retVal;
	}

	/**
	 * 定时启动任务
	 */
	private int addQuartzJobs() {
		int retVal = 1;
		try {
			String cronStr = DeviceConfig.getInstance().getAutoLogonCron();
			SchedulerFactory sf = new StdSchedulerFactory();
			Scheduler sched = sf.getScheduler(); // 初始化调度器
			JobDetail job = JobBuilder.newJob(AutoLogonJob.class).withIdentity("autoLogonJob", "pitcheckGroup").build();
			CronTrigger trigger = (CronTrigger) TriggerBuilder.newTrigger().withIdentity("autoLogonTrigger", "pitcheckGroup")
					.withSchedule(CronScheduleBuilder.cronSchedule(cronStr)).build(); // 设置触发器
																						// 每20秒执行一次
			Date ft = sched.scheduleJob(job, trigger); // 设置调度作业
			log.debug(job.getKey() + " has been scheduled to run at: " + ft + " and repeat based on expression: " + trigger.getCronExpression());
			sched.start(); // 开启调度任务，执行作业

		} catch (Exception ex) {
			retVal = 0;
			ex.printStackTrace();
		}
		return retVal;
	}

	/**
	 * 点亮摄像头补光灯
	 * 
	 * @return
	 */
	private int startLED() {
		LightControlBoard cb = LightControlBoard.getInstance();
		if (cb.Cb_InitSDK() == 0) {
			if (cb.Cb_OpenCom(DeviceConfig.getInstance().getCameraLEDPort()) == 0) {
				if (cb.Cb_LightUnitOff(DeviceConfig.CameraLEDUnit, DeviceConfig.CameraLEDLevel) != 0) {
					return 0;
				}
				CommUtil.sleep(1000);
				if (cb.Cb_LightUnitOn(DeviceConfig.CameraLEDUnit, DeviceConfig.CameraLEDLevel) != 0) {
					return 0;
				}
			} else {
				return 0;
			}
		} else {
			return 0;
		}
		return 1;
	}

	/**
	 * 启动通行控制板
	 * 
	 * @return
	 */
	private int startGateDevice() {
		log.info("启动通行控制设备");
		try {
			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {
				FirstGateDevice.getInstance().connect(DeviceConfig.getInstance().getFirstGateCrtlPort());
				// FirstGateDevice.getInstance().selfCheck();
			}
			SecondGateDevice.getInstance().connect(DeviceConfig.getInstance().getSecondGateCrtlPort());
			// SecondGateDevice.getInstance().selfCheck();

			CommUtil.sleep(1000 * 6);
			if (!FirstGateDevice.getInstance().getErrcode().equals("00") || !SecondGateDevice.getInstance().getErrcode().equals("00")) {
				return 0;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("", e);
			return 0;
		}
		return 1;
	}

	/**
	 * 启动二代证读卡器
	 * 
	 * @return
	 */
	private int startIDDevice() {
		log.debug("启动二代证读卡器");
		IDReader idReader = IDReader.getInstance();
		// log.debug("getIdDeviceStatus=="+DeviceConfig.getInstance().getIdDeviceStatus());
		if (DeviceConfig.getInstance().getIdDeviceStatus() != DeviceConfig.idDeviceSucc) {
			log.debug("二代证读卡器异常,请联系维护人员!");
			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {
				TicketVerifyScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowIDDeviceException.getValue(), null, null, null));
			} else {
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_QRDeviceException);
			}
			setDeviceReader(false);

			// CommUtil.sleep(5000);
			// try {
			// log.debug("自动注销计算机...");
			// Runtime.getRuntime().exec(Config.AutoLogonCmd);
			// } catch (Exception e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

			return 0;
		}
		return 1;
	}

	/**
	 * 启动二维码读卡器
	 * 
	 * @return
	 */
	private int startQRDevice() {
		log.info("启动二维码读卡器");
		// QRReader qrReader = QRReader.getInstance();
		BarCodeReader barcodeReader = BarCodeReader.getInstance();

		CommUtil.sleep(1000);
		log.debug("getQrdeviceStatus==" + DeviceConfig.getInstance().getQrdeviceStatus());
		if (DeviceConfig.getInstance().getQrdeviceStatus() != DeviceConfig.qrDeviceSucc) {
			log.debug("二维码扫描器异常,请联系维护人员!");
			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {
				TicketVerifyScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowQRDeviceException.getValue(), null, null, null));
			} else {
				if (Config.getInstance().getIsOutGateForCSQ() == 1) {
					TicketVerifyScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowQRDeviceException.getValue(), null, null, null));
				} else {
					GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_QRDeviceException);
				}
			}
			setDeviceReader(false);

			// CommUtil.sleep(5000);
			// try {
			// log.debug("自动注销计算机...");
			// Runtime.getRuntime().exec(Config.AutoLogonCmd);
			// } catch (Exception e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			return 0;
		}
		// for(int i=0;i<10;i++){
		// TKQRDevice.getInstance().setStatusCode(TKQRDevice.getInstance().BAR_GetErrMessage());
		// }
		return 1;
	}

	private int pitStatus = -1;

	public int getPitStatus() {
		return pitStatus;
	}

	public void setPitStatus(int pitStatus) {
		this.pitStatus = pitStatus;
	}

	/**
	 * 将已读取到的车票和身份证信息置空
	 */
	public void resetTicketAndIDCard() {
		ticketVerify.reset();
	}

	public void clearTicket() {
		ticketVerify.clearTicket();
	}

	public void clearIdCard() {
		ticketVerify.clearIdCard();
	}

	/**
	 * 读卡器是否开始寻卡
	 * 
	 * @param isRead
	 */
	public void setDeviceReader(boolean isRead) {
		if (isRead) {
			log.debug("读卡器开始寻卡");
			if (Config.getInstance().getIsOutGateForCSQ() == 1) {
				// QRReader.getInstance().start();
				BarCodeReader.getInstance().start();
			} else {
				IDReader.getInstance().start();
				BarCodeReader.getInstance().start();
			}
		} else {
			log.debug("读卡器停止寻卡");
			if (Config.getInstance().getIsOutGateForCSQ() == 1) {
				// QRReader.getInstance().stop();
				BarCodeReader.getInstance().start();
			} else {
				IDReader.getInstance().stop();
				BarCodeReader.getInstance().stop();
			}
		}
	}
}
