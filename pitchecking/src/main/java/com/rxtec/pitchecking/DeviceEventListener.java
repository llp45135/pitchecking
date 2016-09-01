package com.rxtec.pitchecking;

import java.io.IOException;
import java.util.Date;
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

import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.device.DeviceException;
import com.rxtec.pitchecking.device.FirstGateDevice;
import com.rxtec.pitchecking.device.SecondGateDevice;
import com.rxtec.pitchecking.device.LightControlBoard;
import com.rxtec.pitchecking.event.IDeviceEvent;
import com.rxtec.pitchecking.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.mbean.ProcessUtil;
import com.rxtec.pitchecking.mq.JmsReceiverTask;
import com.rxtec.pitchecking.mq.JmsSenderTask;
import com.rxtec.pitchecking.picheckingservice.PITData;
import com.rxtec.pitchecking.task.AutoLogonJob;
import com.rxtec.pitchecking.utils.CommUtil;

public class DeviceEventListener implements Runnable {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");
	private static DeviceEventListener _instance = new DeviceEventListener();
	private VerifyFaceTask verifyFaceTask = new VerifyFaceTask();
	private TicketVerify ticketVerify = new TicketVerify();
	private boolean isDealDeviceEvent = true;

	public boolean isDealDeviceEvent() {
		return isDealDeviceEvent;
	}

	public void setDealDeviceEvent(boolean isDealDeviceEvent) {
		this.isDealDeviceEvent = isDealDeviceEvent;
	}

	private DeviceEventListener() {
		try {
			startDevice();
		} catch (DeviceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static DeviceEventListener getInstance() {
		return _instance;
	}

	private LinkedBlockingQueue<IDeviceEvent> deviceEventQueue = new LinkedBlockingQueue<IDeviceEvent>(3);

	public void offerDeviceEvent(IDeviceEvent e) {
		// 队列满了需要处理Exception,注意！
		deviceEventQueue.offer(e);
	}

	@Override
	public void run() {
		IDeviceEvent e;
		try {
			if (isDealDeviceEvent) {
				e = deviceEventQueue.take();

				this.setDealDeviceEvent(false);// 停止处理新的事件

				this.processEvent(e);
			}
		} catch (InterruptedException ex) {
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

	private void verifyTicket() {
		int ticketVerifyResult = ticketVerify.verify();// 票证核验结果

		if (ticketVerifyResult == Config.TicketVerifySucc) { // 核验成功
			// 停止寻卡
			this.setDeviceReader(false);
			log.debug("票证核验通过，停止寻卡，打开第一道闸门，开始人证比对");
			// 打开第一道门
			FirstGateDevice.getInstance().openFirstDoor();

			TicketCheckScreen.getInstance()
					.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketVerifySucc.getValue(),
							ticketVerify.getTicket(), ticketVerify.getIdCard(), null));

			// 开始进行人脸检测和比对
			verifyFace(ticketVerify.getIdCard(), ticketVerify.getTicket());

			ticketVerify.reset();

			// TicketCheckScreen.getInstance().offerEvent(
			// new ScreenElementModifyEvent(0,
			// ScreenCmdEnum.showDefaultContent.getValue(), null, null, null));
			// 开始寻卡
			// this.setDeviceReader(true);
			// log.debug("人证比对完成，开始寻卡");
		} else if (ticketVerifyResult == Config.TicketVerifyWaitInput) { // 等待票证验证数据
			if (ticketVerify.getTicket() == null || ticketVerify.getIdCard() == null) {
				TicketCheckScreen.getInstance()
						.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketVerifyWaitInput.getValue(),
								ticketVerify.getTicket(), ticketVerify.getIdCard(), null));
			}
			this.setDealDeviceEvent(true);// 允许处理新的事件
		} else if (ticketVerifyResult == Config.TicketVerifyIDFail) { // 票证验证失败
			TicketCheckScreen.getInstance()
					.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketVerifyIDFail.getValue(),
							ticketVerify.getTicket(), ticketVerify.getIdCard(), null));
			ticketVerify.reset();
			this.setDealDeviceEvent(true);// 允许处理新的事件
		} else if (ticketVerifyResult == Config.TicketVerifyStationRuleFail) { // 车票未通过非本站规则
			TicketCheckScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketVerifyStationRuleFail.getValue(),
							ticketVerify.getTicket(), ticketVerify.getIdCard(), null));
			ticketVerify.reset();
			this.setDealDeviceEvent(true);// 允许处理新的事件
		} else if (ticketVerifyResult == Config.TicketVerifyTrainDateRuleFail) { // 车票未通过非当日规则
			TicketCheckScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketVerifyTrainDateRuleFail.getValue(),
							ticketVerify.getTicket(), ticketVerify.getIdCard(), null));
			ticketVerify.reset();
			this.setDealDeviceEvent(true);// 允许处理新的事件
		}
	}

	/**
	 * 人证比对
	 * 
	 * @param idCard
	 * @param ticket
	 */
	private void verifyFace(IDCard idCard, Ticket ticket) {
		PITData picData = verifyFaceTask.beginCheckFace(idCard, ticket);
	}

	// 启动设备
	private void startDevice() throws DeviceException {
		if (this.startGateDevice() != 1) {
			return;
		}
		log.debug("核验软件版本");
		if (DeviceConfig.getInstance().getVersionFlag() != 1) {
			log.debug("软件版本错误:" + DeviceConfig.getInstance().getVersionFlag());
			TicketCheckScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowVersionFault.getValue(), null, null, null));
			FirstGateDevice.getInstance().LightEntryCross(); // 启动失败点亮红色叉
			return;
		}
		if (this.startIDDevice() != 1) {
			FirstGateDevice.getInstance().LightEntryCross(); // 启动失败点亮红色叉
			return;
		}
		if (this.startQRDevice() != 1) {
			FirstGateDevice.getInstance().LightEntryCross(); // 启动失败点亮红色叉
			return;
		}
		if (this.startLED() != 1) {
			FirstGateDevice.getInstance().LightEntryCross(); // 启动失败点亮红色叉
			return;
		}
		if (this.addQuartzJobs() != 1) {
			FirstGateDevice.getInstance().LightEntryCross(); // 启动失败点亮红色叉
			return;
		}

		/**
		 * 连接mq服务，启动mq receiver线程
		 */
		if (DeviceConfig.getInstance().getMqStartFlag() == 1) {
			ExecutorService executer = Executors.newCachedThreadPool();
			JmsReceiverTask jmsReceiverTask = new JmsReceiverTask();
			executer.execute(jmsReceiverTask);
		}

		ScheduledExecutorService scheduler = null;
		if (DeviceConfig.getInstance().getMqStartFlag() == 1) {
			scheduler = Executors.newScheduledThreadPool(5);
		} else {
			scheduler = Executors.newScheduledThreadPool(4);
		}
		scheduler.scheduleWithFixedDelay(IDReader.getInstance(), 0, 150, TimeUnit.MILLISECONDS);
		scheduler.scheduleWithFixedDelay(QRReader.getInstance(), 0, 100, TimeUnit.MILLISECONDS);
		// 求助按钮事件处理线程
		scheduler.scheduleWithFixedDelay(EmerButtonTask.getInstance(), 0, 100, TimeUnit.MILLISECONDS);
		// 语音调用线程
		scheduler.scheduleWithFixedDelay(AudioPlayTask.getInstance(), 0, 100, TimeUnit.MILLISECONDS);
		// mq sender线程
		if (DeviceConfig.getInstance().getMqStartFlag() == 1) {
			scheduler.scheduleWithFixedDelay(JmsSenderTask.getInstance(), 0, 100, TimeUnit.MILLISECONDS);
		}
		// 启动成功点亮绿色通行箭头
		FirstGateDevice.getInstance().LightEntryArrow();
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
		LightControlBoard cb = new LightControlBoard();
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
			TicketCheckScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowIDDeviceException.getValue(), null, null, null));
			setDeviceReader(false);
			CommUtil.sleep(5000);
			try {
				log.info("自动注销计算机...");
				Runtime.getRuntime().exec(Config.AutoLogonCmd);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return 0;
		}
		return 1;
	}

	private int startQRDevice() {
		log.debug("启动二维码读卡器");
		QRReader qrReader = QRReader.getInstance();
		CommUtil.sleep(1000);
		log.debug("getQrdeviceStatus==" + DeviceConfig.getInstance().getQrdeviceStatus());
		if (DeviceConfig.getInstance().getQrdeviceStatus() != DeviceConfig.qrDeviceSucc) {
			log.debug("二维码扫描器异常,请联系维护人员!");
			TicketCheckScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowQRDeviceException.getValue(), null, null, null));

			setDeviceReader(false);
			CommUtil.sleep(5000);
			try {
				log.info("自动注销计算机...");
				Runtime.getRuntime().exec(Config.AutoLogonCmd);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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

	/**
	 * 读卡器是否开始寻卡
	 * 
	 * @param isRead
	 */
	public void setDeviceReader(boolean isRead) {
		if (isRead) {
			log.debug("读卡器开始寻卡");
			IDReader.getInstance().start();
			QRReader.getInstance().start();
		} else {
			log.debug("读卡器停止寻卡");
			IDReader.getInstance().stop();
//			QRReader.getInstance().stop();
		}
	}
}
