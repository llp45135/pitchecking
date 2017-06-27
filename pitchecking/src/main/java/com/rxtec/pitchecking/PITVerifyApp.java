package com.rxtec.pitchecking;

import java.util.Date;
import java.util.concurrent.Executors;
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
import com.rxtec.pitchecking.gui.ticketgui.TicketVerifyFrame;
import com.rxtec.pitchecking.mqtt.GatCtrlReceiverBroker;
import com.rxtec.pitchecking.mqtt.GatCtrlSenderBroker;
import com.rxtec.pitchecking.mqtt.MqttCamRequestSender;
import com.rxtec.pitchecking.mqtt.MqttCamResponseReceiver;
import com.rxtec.pitchecking.net.event.CAMJsonUtil;
import com.rxtec.pitchecking.net.event.CAMNotifyBean;
import com.rxtec.pitchecking.net.event.CAMOpenBean;
import com.rxtec.pitchecking.net.event.PIVerifyResultBean;
import com.rxtec.pitchecking.net.event.ScreenDisplayBean;
import com.rxtec.pitchecking.task.AutoLogonJob;
import com.rxtec.pitchecking.task.FrontTrackPidDetectTask;
import com.rxtec.pitchecking.task.MainPidDetectTask;
import com.rxtec.pitchecking.task.QueryUPSStatusTask;
import com.rxtec.pitchecking.task.StandaloneCheckDetectTask;
import com.rxtec.pitchecking.task.TrackingPidDetectTask;
import com.rxtec.pitchecking.task.UpdateFlapCountJob;
import com.rxtec.pitchecking.task.UpdateTrainInfoJob;
import com.rxtec.pitchecking.task.VerifyScreenListener;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.IDCardUtil;

/**
 * 单票证核验版本主入口，从该入口启动主程序，该进程不用管理第二块屏、不做人脸检测
 * 
 * @author ZhaoLin
 *
 */
public class PITVerifyApp {
	static Logger log = LoggerFactory.getLogger("DeviceEventListener");

	public static void main(String[] args) {
		if (Config.getInstance().getPitVerifyMode() == 1) {
			try {
				if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {// 双门模式
					if (Config.getInstance().getFrontScreenMode() == 1) {
						TicketVerifyScreen ticketVerifyScreen = TicketVerifyScreen.getInstance();
						TicketVerifyFrame tickFrame = new TicketVerifyFrame();
						ticketVerifyScreen.setTicketFrame(tickFrame);

						tickFrame.showSuccWait("", "系统启动中...");

						try {
							ticketVerifyScreen.initUI(DeviceConfig.getInstance().getTicketScreen());
						} catch (Exception ex) {
							// TODO Auto-generated catch block
							log.error("PITVerifyApp:", ex);
						}
					} else {
						TicketVerifyScreen ticketVerifyScreen = TicketVerifyScreen.getInstance();
						TicketVerifyFrame tickFrame = new TicketVerifyFrame();
						ticketVerifyScreen.setTicketFrame(tickFrame);

						tickFrame.showSuccWait("", "系统启动中...");

						try {
							ticketVerifyScreen.initUINoFull(DeviceConfig.getInstance().getTicketScreen());
						} catch (Exception ex) {
							// TODO Auto-generated catch block
							log.error("PITVerifyApp:", ex);
						}
					}
				}

				if (Config.getInstance().getIsStartMainListener() == 1) {

					MqttCamResponseReceiver.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT);
					MqttCamRequestSender.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT);

					GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT);
					GatCtrlReceiverBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT); // 启动PITEventTopic本地监听

					if (Config.getInstance().getIsResetVerifyScreen() == 1) {
						ScheduledExecutorService screenScheduler = Executors.newScheduledThreadPool(1);
						VerifyScreenListener verifyScreenListener = VerifyScreenListener.getInstance();
						verifyScreenListener.setScreenNo(DeviceConfig.getInstance().getTicketScreen());
						screenScheduler.scheduleWithFixedDelay(verifyScreenListener, 0, 1500, TimeUnit.MILLISECONDS);
					}
					
					
					
					

					DeviceEventListener eventListener = DeviceEventListener.getInstance();
					try {
						if (Config.getInstance().getPitVerifyMode() == 1) {
							// 初始化闸机部件
							eventListener.startDevice();
						}
						// eventListener.offerDeviceEventTest(); //仅供测试用
					} catch (DeviceException e1) {
						// TODO Auto-generated catch block
						log.error("startDevice:", e1);
					}
					
					
					try {
						SchedulerFactory sf = new StdSchedulerFactory();
						Scheduler sched = sf.getScheduler(); // 初始化调度器

						
						/**
						 * 定时更新旅服信息						
						 */
						JobDetail job5 = JobBuilder.newJob(UpdateTrainInfoJob.class).withIdentity("UpdateTrainInfoJobJob", "queryUPSGroup").build();
						CronTrigger trigger5 = (CronTrigger) TriggerBuilder.newTrigger().withIdentity("UpdateTrainInfoJobTrigger", "queryUPSGroup")
								.withSchedule(CronScheduleBuilder.cronSchedule(Config.getInstance().getUpdateTrainInfoCronStr())).build(); // 设置触发器

						Date ft5 = sched.scheduleJob(job5, trigger5); // 设置调度作业
						log.info(job5.getKey() + " has been scheduled to run at: " + ft5 + " and repeat based on expression: " + trigger5.getCronExpression());
						
						/**
						 * 定时更新开门次数
						 */
						JobDetail job6 = JobBuilder.newJob(UpdateFlapCountJob.class).withIdentity("UpdateFlapCountJob", "queryUPSGroup").build();
						CronTrigger trigger6 = (CronTrigger) TriggerBuilder.newTrigger().withIdentity("UpdateFlapCountJobTrigger", "queryUPSGroup")
								.withSchedule(CronScheduleBuilder.cronSchedule(Config.getInstance().getUpdateFlapCountCronStr())).build(); // 设置触发器

						Date ft6 = sched.scheduleJob(job6, trigger6); // 设置调度作业
						log.info(job6.getKey() + " has been scheduled to run at: " + ft6 + " and repeat based on expression: " + trigger6.getCronExpression());

						sched.start(); // 开启调度任务，执行作业

					} catch (Exception ex) {
						log.error("",ex);
					}

					// 启动事件监听

					// eventListener.setPitStatus(PITStatusEnum.DefaultStatus.getValue());
					// log.debug("准备初始化界面");
					// CommUtil.sleep(10*1000);

					// ExecutorService executorService =
					// Executors.newCachedThreadPool();
					// executorService.execute(eventListener);

					if (eventListener.isStartThread()) {
						ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
						scheduler.scheduleWithFixedDelay(eventListener, 0, 200, TimeUnit.MILLISECONDS);
					}
				}
			} catch (Exception ex) {
				log.error("", ex);
			}
		} else {
			/**
			 * 一下代码为测试代码
			 */
			MqttCamResponseReceiver.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT);

			MqttCamRequestSender.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT);

			System.out.println("$$$$$$$$$$$$$$$$$$$$$$");

			int retVal = -1;
			int x = 0; // 窗口左边界
			int y = 0; // 窗口顶边界。
			int cx = 640; // 以像素指定窗口的新的宽度。
			int cy = 480; // 以像素指定窗口的新的高度。
			int checkThreshold = 65; // 人脸比对阀值，取值0-100
			int iCollect = 1; // 摄像头采集图片方式 0:固采 1：预采
			int faceTimeout = Config.getInstance().getFaceCheckDelayTime(); // 人脸识别算法的超时时间，单位秒

			int[] region = { x, y, cx, cy, checkThreshold, iCollect, faceTimeout };
			log.debug("开始调用CAM_Open");
			// retVal = CAMDevice.getInstance().CAM_Open(region);
			MqttCamRequestSender.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendMessage(CAMJsonUtil.createCAMOpenJson(region));
			try {
				CAMOpenBean camOpenBean = DeviceEventListener.getInstance().pollCamOpenDataQueue();
				if (camOpenBean != null) {
					retVal = 0;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				log.error("OpenCAM:", e);
			}
			log.info("调用CAM_Open收到返回值 = " + retVal);

			while (true) {
				CommUtil.sleep(3 * 1000);
				IDCard idCard = IDCardUtil.createIDCard("zp.jpg");
				String uuidStr = idCard.getIdNo();
				String IDPhoto_str = "zp.jpg";
				log.debug("身份证路径==" + IDPhoto_str);
				log.debug("CAM_Notify begin");
				// int notifyRet = CAMDevice.getInstance().CAM_Notify(1,
				// uuidStr, IDPhoto_str);
				int notifyRet = -1;
				MqttCamRequestSender.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendMessage(CAMJsonUtil.createCAMNotifyJson(1, uuidStr, IDPhoto_str));
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
					int delaySeconds = 10;
					// getPhotoRet =
					// CAMDevice.getInstance().CAM_GetPhotoInfo(uuidStr,
					// delaySeconds); // 此处传入的delaySeconds是为了控制检脸通过速率而设置
					MqttCamRequestSender.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendMessage(CAMJsonUtil.createCAM_GetPhotoInfoRequest(uuidStr, delaySeconds));
					try {
						PIVerifyResultBean piVerifyResultBean = DeviceEventListener.getInstance().pollCamGetPhotoInfoQueue();
						if (piVerifyResultBean != null) {
							getPhotoRet = 0;
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						log.error("CAM_GetPhotoInfo:", e);
					}
				}
				log.info("调用CAM_GetPhotoInfo收到返回值 = " + getPhotoRet);

				if (getPhotoRet == 0) {
					int displayTimeOut = 3 - 1;
					// CAMDevice.getInstance().CAM_ScreenDisplay("succed#Pass!",
					// displayTimeOut);
					int displayRet = -1;
					MqttCamRequestSender.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendMessage(CAMJsonUtil.createCAM_ScreenDisplay("succed#Pass!", displayTimeOut));
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
				} else {
					int displayTimeOut = 5 - 1;
					// CAMDevice.getInstance().CAM_ScreenDisplay("failed#Leave!",
					// displayTimeOut);
					int displayRet = -1;
					MqttCamRequestSender.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendMessage(CAMJsonUtil.createCAM_ScreenDisplay("failed#Leave!", displayTimeOut));
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

				// GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendDoorCmd(DeviceConfig.Event_RepeatCheck);

				CommUtil.sleep(10 * 1000);
			}
		}
	}
}
