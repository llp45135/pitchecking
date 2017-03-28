package com.rxtec.pitchecking.mbean;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
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

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.mqtt.GatCtrlSenderBroker;
import com.rxtec.pitchecking.task.AutoLogonJob;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.CalUtils;

/**
 * 由人脸比对进程启动 检脸进程保护
 * 
 * @author llp_l
 *
 */
public class PITProcessDetect implements Runnable {

	private String pid = "";
	private String ticketHeartPid = "";
	private String frontCamearPid = "";
	private Logger log = LoggerFactory.getLogger("PITProcessDetect");
	boolean startStatus = true;
	boolean ticketHeartStatus = true;

	public boolean isTicketHeartStatus() {
		return ticketHeartStatus;
	}

	public void setTicketHeartStatus(boolean ticketHeartStatus) {
		this.ticketHeartStatus = ticketHeartStatus;
	}

	public boolean isStartStatus() {
		return startStatus;
	}

	public void setStartStatus(boolean startStatus) {
		this.startStatus = startStatus;
	}

	public PITProcessDetect() {
		log.debug("初始化心跳侦测线程");
		this.deleteHeartLog();
		this.addQuartzJobs();
	}

	private void deleteHeartLog() {
		File file = new File("D:/pitchecking/work/HEART.log");
		if (file.exists()) {
			file.delete();
		}
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

	@Override
	public void run() {
		while (true) {
			CommUtil.sleep(3000);
			String HeartBeatStr = "", ticketHeartStr = "", frontHeartStr = "";
			try {
				ticketHeartStr = ProcessUtil.getLastHeartBeat(Config.getInstance().getTicketVerifyHeartFile());
				HeartBeatStr = ProcessUtil.getLastHeartBeat(Config.getInstance().getHeartBeatLogFile());
				frontHeartStr = ProcessUtil.getLastHeartBeat(Config.getInstance().getFrontHeartBeatLogFile());

			} catch (Exception ex) {
				log.error("PITProcessDetect getLastHeartBeat：", ex);
			}
			// log.debug("HeartBeatStr==" + HeartBeatStr + "##");
			/**
			 * 后置摄像头心跳监测
			 */
			if (HeartBeatStr != null) {
				HeartBeatStr = HeartBeatStr.trim();
				if (!HeartBeatStr.equals("")) {
					pid = HeartBeatStr.split("@")[0];
					String t = HeartBeatStr.split("@")[1];
					String nowHT = CalUtils.getStringDateHaomiao();

					// log.debug(pid + " last heartbeat time=" + t);
					long heartWaitTime = 0;
					try {
						heartWaitTime = CalUtils.howLong("ms", t, nowHT);
					} catch (Exception ex) {
						log.error("Cal heartWaitTime:", ex);
					}

					// log.debug("heartWaitTime===" + heartWaitTime);
					// log.debug("--------------------------------");
					// log.debug("是否允许恢复检脸进程==" +
					// Config.getInstance().isRebackTrackFlag());
					// log.debug("摄像头是否启动==" +
					// Config.getInstance().isCameraWork());

					if (Config.getInstance().isRebackTrackFlag()) { // true允许恢复
						log.debug("--------------------------------");
						log.debug("是否允许恢复检脸进程==" + Config.getInstance().isRebackTrackFlag());
						log.debug("摄像头是否启动==" + Config.getInstance().isCameraWork());
						try {
							Config.getInstance().setRebackTrackFlag(false);
							log.debug("准备执行恢复检脸进程的批处理");
							Runtime.getRuntime().exec(Config.getInstance().getStartPITTrackCmd());
							log.debug("Resatrt PITTrackApp cmd==" + Config.getInstance().getStartPITTrackCmd());
						} catch (Exception ex) {
							log.error("Resatrt PITTrackApp......:", ex);
						}

						CommUtil.sleep(2000);

						try {
							// File heartFile = new
							// File(Config.getInstance().getHeartBeatLogFile());
							// heartFile.delete();
							log.debug("已经执行恢复检脸进程的批处理");
							GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT)
									.sendDoorCmd("PITEventTopic", DeviceConfig.OPEN_SECONDDOOR);
							DeviceConfig.getInstance().setAllowOpenSecondDoor(false);
							continue;
						} catch (Exception ex) {
							log.error("Resatrt PITTrackApp......:", ex);
						}

					}
					/**
					 * 超时未收到心跳，需要杀死当前检脸进程
					 */
					if (heartWaitTime > Config.getInstance().getHEART_BEAT_DELAY()) {
						log.debug("--------------------------------");
						log.debug("heartWaitTime==" + heartWaitTime + ",pid==" + pid);
						log.debug("是否允许恢复检脸进程==" + Config.getInstance().isRebackTrackFlag());
						log.debug("摄像头是否启动==" + Config.getInstance().isCameraWork());
						if (Config.getInstance().isCameraWork()) { // 摄像头处于启动状态，可以杀死
							try {
								Config.getInstance().setCameraWork(false);
								log.debug("准备杀死进程 " + pid);
								Runtime.getRuntime().exec("taskkill /F /PID " + pid);

								Config.getInstance().setRebackTrackFlag(true);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								log.error("PITProcessDetect taskkill:", e);
							} catch (Exception ex) {
								log.error("PITProcessDetect taskkill:", ex);
							}
						}
					}
				}
			}

			/**
			 * 前置摄像头心跳监测
			 */
			if (frontHeartStr != null) {
				frontHeartStr = frontHeartStr.trim();
				if (!frontHeartStr.equals("")) {
					frontCamearPid = frontHeartStr.split("@")[0];
					String t = frontHeartStr.split("@")[1];
					String nowHT = CalUtils.getStringDateHaomiao();

					// log.debug(pid + " last heartbeat time=" + t);
					long heartWaitTime = 0;
					try {
						heartWaitTime = CalUtils.howLong("ms", t, nowHT);
					} catch (Exception ex) {
						log.error("Cal heartWaitTime:", ex);
					}

					// log.debug("heartWaitTime===" + heartWaitTime);
					// log.debug("--------------------------------");
					// log.debug("是否允许恢复检脸进程==" +
					// Config.getInstance().isRebackTrackFlag());
					// log.debug("摄像头是否启动==" +
					// Config.getInstance().isCameraWork());

					if (Config.getInstance().isRebackTrackFlag()) { // true允许恢复
						log.debug("--------------------------------");
						log.debug("是否允许恢复检脸进程==" + Config.getInstance().isRebackTrackFlag());
						log.debug("摄像头是否启动==" + Config.getInstance().isCameraWork());
						try {
							Config.getInstance().setRebackTrackFlag(false);
							log.debug("准备执行恢复检脸进程的批处理");
							Runtime.getRuntime().exec(Config.getInstance().getRestartFrontTrackCmd());
							log.debug("Resatrt PITFrontTrackApp cmd==" + Config.getInstance().getRestartFrontTrackCmd());
						} catch (Exception ex) {
							log.error("Resatrt PITFrontTrackApp......:", ex);
						}
					}
					/**
					 * 超时未收到心跳，需要杀死当前检脸进程
					 */
					if (heartWaitTime > Config.getInstance().getHEART_BEAT_DELAY()) {
						log.debug("--------------------------------");
						log.debug("heartWaitTime==" + heartWaitTime + ",frontCamearPid==" + frontCamearPid);
						log.debug("是否允许恢复检脸进程==" + Config.getInstance().isRebackTrackFlag());
						log.debug("摄像头是否启动==" + Config.getInstance().isCameraWork());
						if (Config.getInstance().isCameraWork()) { // 摄像头处于启动状态，可以杀死
							try {
								Config.getInstance().setCameraWork(false);
								log.debug("准备杀死进程 " + frontCamearPid);
								Runtime.getRuntime().exec("taskkill /F /PID " + frontCamearPid);

								Config.getInstance().setRebackTrackFlag(true);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								log.error("PITProcessDetect taskkill:", e);
							} catch (Exception ex) {
								log.error("PITProcessDetect taskkill:", ex);
							}
						}
					}
				}
			}

			/**
			 * 主控线程监控
			 */
			if (ticketHeartStr != null) {
				ticketHeartStr = ticketHeartStr.trim();
				if (!ticketHeartStr.equals("")) { // 读到进程号
					ticketHeartPid = ticketHeartStr.split("@")[0];
					String t = ticketHeartStr.split("@")[1];
					String nowHT = CalUtils.getStringDateHaomiao();// (new
																	// Date()).getTime();
					// log.debug(pid + " last heartbeat time=" + t);
					long heartWaitTime = 0;
					try {
						heartWaitTime = CalUtils.howLong("ms", t, nowHT);
					} catch (Exception ex) {
						log.error("Cal heartWaitTime:", ex);
					}

					/**
					 * 超时未收到心跳，需要杀死当前java主控进程
					 */
					if (heartWaitTime > Config.getInstance().getTicketVerify_Heard_DELAY()) {
						log.debug("heartWaitTime==" + heartWaitTime + ",ticketHeartPid==" + ticketHeartPid);
						if (ticketHeartStatus) {
							this.setTicketHeartStatus(false);
							try {
								log.debug("准备杀死java版主控进程 " + ticketHeartPid);
								Runtime.getRuntime().exec("taskkill /F /PID " + ticketHeartPid);

								CommUtil.sleep(2000);
								log.debug("准备恢复java版主控进程...");
								Runtime.getRuntime().exec(Config.getInstance().getStartPITVerifyCmd());

								this.setTicketHeartStatus(true);
								log.debug("已经恢复java版主控进程!!");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								log.error("PITProcessDetect taskkill:", e);
							} catch (Exception ex) {
								log.error("PITProcessDetect taskkill:", ex);
							}
						}
					}
				} else if (ticketHeartStr.equals("null")) {
					if (ticketHeartStatus) {
						this.setTicketHeartStatus(false);
						try {
							Runtime.getRuntime().exec(Config.getInstance().getStartPITVerifyCmd());
							CommUtil.sleep(10 * 1000);
							log.debug("Resatrt PITVerifyApp......When hbs is null");
							this.setTicketHeartStatus(true);
						} catch (Exception ex) {
							log.error("Resatrt:", ex);
						}
					}
				} else {
					// log.debug("未读取到 HEART work log");
				}
			}

		}
	}

	public static void main(String[] args) {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleWithFixedDelay(new PITProcessDetect(), 0, 1, TimeUnit.SECONDS);

	}

}
