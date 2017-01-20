package com.rxtec.pitchecking;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rxtec.pitchecking.device.CAMDevice;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.device.DeviceException;
import com.rxtec.pitchecking.device.FirstGateDevice;
import com.rxtec.pitchecking.device.SecondGateDevice;
import com.rxtec.pitchecking.device.LightControlBoard;
import com.rxtec.pitchecking.event.IDCardReaderEvent;
import com.rxtec.pitchecking.event.IDeviceEvent;
import com.rxtec.pitchecking.event.QRCodeReaderEvent;
import com.rxtec.pitchecking.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.mbean.ProcessUtil;
import com.rxtec.pitchecking.mq.JmsReceiverTask;
import com.rxtec.pitchecking.mq.JmsSenderTask;
import com.rxtec.pitchecking.mqtt.GatCtrlSenderBroker;
import com.rxtec.pitchecking.mqtt.MqttSenderBroker;
import com.rxtec.pitchecking.net.event.CAMOpenBean;
import com.rxtec.pitchecking.picheckingservice.PITData;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
import com.rxtec.pitchecking.task.AutoLogonJob;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.CalUtils;
import com.rxtec.pitchecking.utils.ImageToolkit;

public class DeviceEventListener implements Runnable {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");
	private static DeviceEventListener _instance = new DeviceEventListener();
	// private IVerifyFaceTask verifyFaceTask ;
	private TicketVerify ticketVerify = new TicketVerify();
	private boolean isStartThread = true;
	private boolean isDealDeviceEvent = true;
	private boolean isReceiveOpenFirstDoorCmd = false;
	private boolean isReceiveOpenSecondDoorCmd = false;
	private LinkedBlockingQueue<IDeviceEvent> deviceEventQueue = new LinkedBlockingQueue<IDeviceEvent>(3);

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
		// if(Config.getInstance().getFaceVerifyTaskVersion().equals(Config.FaceVerifyTaskTKVersion))
		// verifyFaceTask = new VerifyFaceTaskForTKVersion();
		// else
		// if(Config.getInstance().getFaceVerifyTaskVersion().equals(Config.FaceVerifyTaskRXVersion))
		// verifyFaceTask = new VerifyFaceTaskForRXVersion();

		try {
			// 初始化闸机部件
			startDevice();

			// this.offerDeviceEventTest(); //仅供测试用
		} catch (DeviceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static DeviceEventListener getInstance() {
		return _instance;
	}

	public void offerDeviceEvent(IDeviceEvent e) {
		// 队列满了需要处理Exception,注意！
		deviceEventQueue.offer(e);
	}

	/**
	 * 测试用例，手动创建身份证
	 * 
	 * @param fn
	 * @return
	 */
//	private static IDCard createIDCard(String fn) {
//		IDCard card = new IDCard();
//		card.setIdNo("520203197912141119");
//		BufferedImage bi = null;
//		try {
//			bi = ImageIO.read(new File(fn));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		card.setCardImageBytes(ImageToolkit.getImageBytes(bi, "jpeg"));
//		card.setCardImage(bi);
//		card.setAge(44);
//		return card;
//	}

	/**
	 * 测试用例
	 */
//	public void offerDeviceEventTest() {
//		log.info("offerDeviceEvent readCardEvent");
//		IDCardReaderEvent readCardEvent = new IDCardReaderEvent();
//		IDCard idCard = createIDCard("C:/maven/git/pitchecking/zp.jpg");
//		readCardEvent.setIdCard(idCard);
//		this.offerDeviceEvent(readCardEvent);
//
//		QRCodeReaderEvent qrEvent = new QRCodeReaderEvent(Config.QRReaderEvent);
//		Ticket ticket = new Ticket();
//		ticket.setCardNo("520203197912141119");
//		ticket.setTicketNo("T129999");
//		ticket.setTrainDate(CalUtils.getStringDateShort2());
//		ticket.setFromStationCode("IZQ");
//		ticket.setEndStationCode("SZQ");
//		ticket.setTicketType("0");
//		ticket.setCardType("1");
//		qrEvent.setTicket(ticket);
//		this.offerDeviceEvent(qrEvent);
//		log.info("offerDeviceEvent qrEvent");
//	}

	@Override
	public void run() {

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
		log.debug("/*************Start*******************/");
		if (e.getEventType() == Config.QRReaderEvent && e.getData() != null) {
			/**
			 * 二维码读卡器读到新数据
			 */
			ticketVerify.setTicket((Ticket) e.getData());
			verifyTicket();
		} else if (e.getEventType() == Config.IDReaderEvent && e.getData() != null) {
			/**
			 * 二代证读卡器读到新数据
			 */
			ticketVerify.setIdCard((IDCard) e.getData());
			verifyTicket();
		}
		log.debug("/################End######################/");
		// this.setDealDeviceEvent(true);//允许处理新的事件
		// } else {
		// return;
		// }
	}

	/**
	 * 本函数主要用于票证核验极其后续处理
	 */
	private void verifyTicket() {
		int ticketVerifyResult = ticketVerify.verify();// 票证核验结果

		if (ticketVerifyResult == Config.TicketVerifySucc) { // 核验成功
			this.setDeviceReader(false);// 停止寻卡

			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) { // 双门模式需要开第一道门
				log.debug("票证核验通过，停止寻卡，打开第一道闸门，开始人证比对");
				if (Config.getInstance().getIsUseGatDll() == 0) {
					this.setReceiveOpenFirstDoorCmd(false);
					// FirstGateDevice.getInstance().openFirstDoor();// 打开第一道门
					for (int rc = 0; rc < 3; rc++) {
						if (!this.isReceiveOpenFirstDoorCmd) {
							FirstGateDevice.getInstance().openFirstDoor();
						} else {
							break;
						}
						CommUtil.sleep(100);
					}
				} else {
					GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT)
							.sendDoorCmd(DeviceConfig.OPEN_FIRSTDOOR);
				}

				TicketVerifyScreen.getInstance()
						.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketVerifySucc.getValue(),
								ticketVerify.getTicket(), ticketVerify.getIdCard(), null));
			} else { // 单门模式
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT)
						.sendDoorCmd(DeviceConfig.OPEN_FIRSTDOOR);
			}

			// 开始进行人脸检测和比对
			if (this.verifyFace(ticketVerify.getIdCard(), ticketVerify.getTicket()) == 0) { // 人脸比对通过
				log.debug("人脸比对通过，开第二道闸门");
				if (Config.getInstance().getIsUseGatDll() == 0) { // 不调用门控dll
					this.setReceiveOpenSecondDoorCmd(false);
					// SecondGateDevice.getInstance().openTheSecondDoor(); //
					SecondGateDevice.getInstance().LightGreenLED();
					CommUtil.sleep(20);
					for (int rc = 0; rc < 3; rc++) {
						if (!this.isReceiveOpenSecondDoorCmd) {
							SecondGateDevice.getInstance().openTheSecondDoor();
						} else {
							break;
						}
						CommUtil.sleep(100);
					}
				} else {
					GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT)
							.sendDoorCmd(DeviceConfig.OPEN_SECONDDOOR);
				}

				int displayTimeOut = 3 - 1;
				CAMDevice.getInstance().CAM_ScreenDisplay("人证核验成功#请通过", displayTimeOut);

				if (Config.getInstance().getDoorCountMode() == DeviceConfig.SINGLEDOOR) {
					DeviceEventListener.getInstance().setDeviceReader(true); // 允许寻卡
					DeviceEventListener.getInstance().setDealDeviceEvent(true); // 允许处理新的事件
				}
			} else { // 人脸比对失败
				if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) { // 双门模式
					log.debug("人脸比对失败，开第三道电磁门");
					if (Config.getInstance().getIsUseGatDll() == 0) {// 不调用门控dll
						SecondGateDevice.getInstance().LightRedLED();
//						SecondGateDevice.getInstance().openEmerDoor(); // 打开电磁门
					} else {
						GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT)
								.sendDoorCmd(DeviceConfig.OPEN_THIRDDOOR);
					}

					int displayTimeOut = 5 - 1;
					CAMDevice.getInstance().CAM_ScreenDisplay("人证核验失败#请从侧门离开", displayTimeOut);
				} else { // 单门模式
					int displayTimeOut = 5 - 1;
					CAMDevice.getInstance().CAM_ScreenDisplay("人证核验失败#请走人工通道", displayTimeOut);
				}

				CommUtil.sleep(5 * 1000);

				if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) { // 双门模式
					TicketVerifyScreen.getInstance()
							.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketDefault.getValue(),
									ticketVerify.getTicket(), ticketVerify.getIdCard(), null));
				} else { // 单门模式

				}
				DeviceEventListener.getInstance().setDeviceReader(true); // 允许寻卡
				DeviceEventListener.getInstance().setDealDeviceEvent(true); // 允许处理新的事件
			}

			ticketVerify.reset();

		} else if (ticketVerifyResult == Config.TicketVerifyWaitInput) { // 等待票证验证数据
			if (ticketVerify.getTicket() == null || ticketVerify.getIdCard() == null) {
				if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) { // 双门模式

					if (ticketVerify.getIdCard() == null) {
						GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT)
								.sendDoorCmd(DeviceConfig.Event_ReadIDCardFailed);
					} else {
						GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT)
								.sendDoorCmd(DeviceConfig.Event_ReadTicketFailed);
					}

					TicketVerifyScreen.getInstance().offerEvent(
							new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketVerifyWaitInput.getValue(),
									ticketVerify.getTicket(), ticketVerify.getIdCard(), null));
				} else { // 单门模式
					log.debug("等待票证验证数据,ticketVerifyResult==" + ticketVerifyResult);
					if (ticketVerify.getIdCard() == null) {
						GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT)
								.sendDoorCmd(DeviceConfig.Event_ReadIDCardFailed);
					} else {
						GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT)
								.sendDoorCmd(DeviceConfig.Event_ReadTicketFailed);
					}
				}
			}
			this.setDealDeviceEvent(true);// 允许处理新的事件
		} else if (ticketVerifyResult == Config.TicketVerifyIDFail) { // 票证不一致
			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) { // 双门模式
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT)
						.sendDoorCmd(DeviceConfig.Event_InvalidCardAndTicket);

				TicketVerifyScreen.getInstance()
						.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketVerifyIDFail.getValue(),
								ticketVerify.getTicket(), ticketVerify.getIdCard(), null));
			} else { // 单门模式
				log.debug("票证验证失败,ticketVerifyResult==" + ticketVerifyResult);
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT)
						.sendDoorCmd(DeviceConfig.Event_InvalidCardAndTicket);
			}
			
			CommUtil.sleep(600);
			ticketVerify.reset();			
			this.setDealDeviceEvent(true);// 允许处理新的事件
		} else if (ticketVerifyResult == Config.TicketVerifyStationRuleFail) { // 非本站乘车
			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) { // 双门模式
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT)
						.sendDoorCmd(DeviceConfig.Event_InvalidStation);

				TicketVerifyScreen.getInstance()
						.offerEvent(new ScreenElementModifyEvent(0,
								ScreenCmdEnum.ShowTicketVerifyStationRuleFail.getValue(), ticketVerify.getTicket(),
								ticketVerify.getIdCard(), null));
			} else {
				log.debug("车票未通过非本站规则,ticketVerifyResult==" + ticketVerifyResult);
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT)
						.sendDoorCmd(DeviceConfig.Event_InvalidStation);
			}
			CommUtil.sleep(600);
			ticketVerify.reset();			
			this.setDealDeviceEvent(true);// 允许处理新的事件
		} else if (ticketVerifyResult == Config.TicketVerifyTrainDateRuleFail) { // 非当日乘车
			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) { // 双门模式
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT)
						.sendDoorCmd(DeviceConfig.Event_InvalidTrainDate);

				TicketVerifyScreen.getInstance()
						.offerEvent(new ScreenElementModifyEvent(0,
								ScreenCmdEnum.ShowTicketVerifyTrainDateRuleFail.getValue(), ticketVerify.getTicket(),
								ticketVerify.getIdCard(), null));
			} else {
				log.debug("车票未通过非当日规则,ticketVerifyResult==" + ticketVerifyResult);
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT)
						.sendDoorCmd(DeviceConfig.Event_InvalidTrainDate);
			}
			CommUtil.sleep(600);
			ticketVerify.reset();			
			this.setDealDeviceEvent(true);// 允许处理新的事件
		} else if (ticketVerifyResult == Config.TicketVerifyNotStartCheckFail) { // 车票未到进站时间
			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) { // 双门模式
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT)
						.sendDoorCmd(DeviceConfig.Event_NotInTime);

				TicketVerifyScreen.getInstance()
						.offerEvent(new ScreenElementModifyEvent(0,
								ScreenCmdEnum.showETicketNotInTime.getValue(), ticketVerify.getTicket(),
								ticketVerify.getIdCard(), null));
			} else {
				log.debug("车票未到进站时间,ticketVerifyResult==" + ticketVerifyResult);
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT)
						.sendDoorCmd(DeviceConfig.Event_NotInTime);
			}
			CommUtil.sleep(600);
			ticketVerify.reset();			
			this.setDealDeviceEvent(true);// 允许处理新的事件
		} else if (ticketVerifyResult == Config.TicketVerifyStopCheckFail) { // 车票已过进站时间
			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) { // 双门模式
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT)
						.sendDoorCmd(DeviceConfig.Event_PassCheckTime);

				TicketVerifyScreen.getInstance()
						.offerEvent(new ScreenElementModifyEvent(0,
								ScreenCmdEnum.showETicketPassTime.getValue(), ticketVerify.getTicket(),
								ticketVerify.getIdCard(), null));
			} else {
				log.debug("车票未到进站时间,ticketVerifyResult==" + ticketVerifyResult);
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT)
						.sendDoorCmd(DeviceConfig.Event_PassCheckTime);
			}
			CommUtil.sleep(600);
			ticketVerify.reset();			
			this.setDealDeviceEvent(true);// 允许处理新的事件
		}
	}

	/**
	 * 通知开始检脸 同时等待比对结果
	 * 
	 * @param idCard
	 * @param ticket
	 */
	private int verifyFace(IDCard idCard, Ticket ticket) {
		// int delaySeconds = Config.getInstance().getFaceCheckDelayTime();
		// verifyFaceTask.beginCheckFace(idCard, ticket, delaySeconds);

		String uuidStr = idCard.getIdNo();
		String IDPhoto_str = "zp.jpg";
		log.debug("身份证路径==" + IDPhoto_str);
		log.debug("CAM_Notify begin");
		int notifyRet = CAMDevice.getInstance().CAM_Notify(1, uuidStr, IDPhoto_str);
		int getPhotoRet = -1;
		if (notifyRet == 0) {
			int delaySeconds = 10;
			getPhotoRet = CAMDevice.getInstance().CAM_GetPhotoInfo(uuidStr, delaySeconds); // 此处传入的delaySeconds是为了控制检脸通过速率而设置
		}
		log.debug("getphotoRet==" + getPhotoRet);
		return getPhotoRet;
	}

	// 启动设备
	private void startDevice() throws DeviceException {

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

		log.debug("核验软件版本");
		if (DeviceConfig.getInstance().getVersionFlag() != 1) {
			log.debug("软件版本错误:" + DeviceConfig.getInstance().getVersionFlag());
			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {// 双门模式
				TicketVerifyScreen.getInstance().offerEvent(
						new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowVersionFault.getValue(), null, null, null));
			}
			this.isStartThread = false;
			LightEntryLED(false);
			return;
		}

		if (Config.getInstance().getIsUseGatDll() == 0) {// 不使用门控dll时，需要启动串口设备
			if (this.startGateDevice() != 1) {
				this.isStartThread = false;
				return;
			}
		}

		if (this.OpenCAM() != 0) {
			this.setDealDeviceEvent(false);// 停止处理新的事件
			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {// 双门模式
				TicketVerifyScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0,
						ScreenCmdEnum.ShowCamOpenException.getValue(), null, null, null));
			}
			this.isStartThread = false;
			LightEntryLED(false);
			return;
		}

		if (this.startIDDevice() != 1) {
			this.isStartThread = false;
			LightEntryLED(false);
			return;
		}

		if (this.startQRDevice() != 1) {
			this.isStartThread = false;
			LightEntryLED(false);
			return;
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

		scheduler.scheduleWithFixedDelay(IDReader.getInstance(), 0, 150, TimeUnit.MILLISECONDS);
		scheduler.scheduleWithFixedDelay(BarCodeReader.getInstance(), 0, 150, TimeUnit.MILLISECONDS);
		// 求助按钮事件处理线程
		// scheduler.scheduleWithFixedDelay(EmerButtonTask.getInstance(), 0,
		// 100, TimeUnit.MILLISECONDS);
		// // 语音调用线程
		// scheduler.scheduleWithFixedDelay(AudioPlayTask.getInstance(), 0, 100,
		// TimeUnit.MILLISECONDS);
		// 门模块状态
		// scheduler.scheduleWithFixedDelay(GATStatusQuery.getInstance(), 0,
		// 100, TimeUnit.MILLISECONDS);

		// mq sender线程
		if (DeviceConfig.getInstance().getMqStartFlag() == 1) {
			scheduler.scheduleWithFixedDelay(JmsSenderTask.getInstance(), 0, 100, TimeUnit.MILLISECONDS);
		}

		if (Config.getInstance().getIsUseGatDll() == 0) {
			LightEntryLED(true);
		}
		if (Config.getInstance().getDoorCountMode() == DeviceConfig.SINGLEDOOR) {
			GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT)
					.sendDoorCmd(DeviceConfig.Event_DeviceStartupSucc);
		}
		log.debug("DeviceEventListener 初始化成功");
		if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {// 双门模式
			TicketVerifyScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketDefault.getValue(), null, null, null));
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
				FirstGateDevice.getInstance().LightEntryArrow();// 启动成功点亮绿色通行箭头
			} else {
				FirstGateDevice.getInstance().LightEntryCross(); // 启动失败点亮红色叉
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
			log.debug("开始调用CAM_Open");
			retVal = CAMDevice.getInstance().CAM_Open(region);
			log.debug("调用CAM_Open收到返回值");
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
			CronTrigger trigger = (CronTrigger) TriggerBuilder.newTrigger()
					.withIdentity("autoLogonTrigger", "pitcheckGroup")
					.withSchedule(CronScheduleBuilder.cronSchedule(cronStr)).build(); // 设置触发器
																						// 每20秒执行一次
			Date ft = sched.scheduleJob(job, trigger); // 设置调度作业
			log.debug(job.getKey() + " has been scheduled to run at: " + ft + " and repeat based on expression: "
					+ trigger.getCronExpression());
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
		log.debug("启动通行控制设备");
		try {
			FirstGateDevice.getInstance().connect(DeviceConfig.getInstance().getFirstGateCrtlPort());
			SecondGateDevice.getInstance().connect(DeviceConfig.getInstance().getSecondGateCrtlPort());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	private int startIDDevice() {
		log.debug("启动二代证读卡器");
		IDReader idReader = IDReader.getInstance();
		// log.debug("getIdDeviceStatus=="+DeviceConfig.getInstance().getIdDeviceStatus());
		if (DeviceConfig.getInstance().getIdDeviceStatus() != DeviceConfig.idDeviceSucc) {
			log.debug("二代证读卡器异常,请联系维护人员!");
			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {
				TicketVerifyScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0,
						ScreenCmdEnum.ShowIDDeviceException.getValue(), null, null, null));
			} else {
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT)
						.sendDoorCmd(DeviceConfig.Event_QRDeviceException);
			}
			setDeviceReader(false);

			// CommUtil.sleep(5000);
			// try {
			// log.info("自动注销计算机...");
			// Runtime.getRuntime().exec(Config.AutoLogonCmd);
			// } catch (Exception e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

			return 0;
		}
		return 1;
	}

	private int startQRDevice() {
		log.debug("启动二维码读卡器");
		// QRReader qrReader = QRReader.getInstance();
		BarCodeReader barcodeReader = BarCodeReader.getInstance();
		CommUtil.sleep(1000);
		log.debug("getQrdeviceStatus==" + DeviceConfig.getInstance().getQrdeviceStatus());
		if (DeviceConfig.getInstance().getQrdeviceStatus() != DeviceConfig.qrDeviceSucc) {
			log.debug("二维码扫描器异常,请联系维护人员!");
			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {
				TicketVerifyScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0,
						ScreenCmdEnum.ShowQRDeviceException.getValue(), null, null, null));

			} else {
				GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT)
						.sendDoorCmd(DeviceConfig.Event_QRDeviceException);
			}
			setDeviceReader(false);

			// CommUtil.sleep(5000);
			// try {
			// log.info("自动注销计算机...");
			// Runtime.getRuntime().exec(Config.AutoLogonCmd);
			// } catch (Exception e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			return 0;
		}
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
	
	public void clearTicket(){
		ticketVerify.clearTicket();
	}
	
	public void clearIdCard(){
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
			IDReader.getInstance().start();
			// QRReader.getInstance().start();
			BarCodeReader.getInstance().start();
		} else {
			log.debug("读卡器停止寻卡");
			IDReader.getInstance().stop();
			// QRReader.getInstance().stop();
			BarCodeReader.getInstance().stop();
		}
	}
}
