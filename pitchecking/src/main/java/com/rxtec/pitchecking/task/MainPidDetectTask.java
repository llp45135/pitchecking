package com.rxtec.pitchecking.task;

import java.io.IOException;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.mbean.ProcessUtil;
import com.rxtec.pitchecking.utils.CalUtils;
import com.rxtec.pitchecking.utils.CommUtil;

public class MainPidDetectTask implements Job {
	private Logger log = LoggerFactory.getLogger("PITProcessDetect");

	private boolean ticketHeartStatus = true;

	public boolean isTicketHeartStatus() {
		return ticketHeartStatus;
	}

	public void setTicketHeartStatus(boolean ticketHeartStatus) {
		this.ticketHeartStatus = ticketHeartStatus;
	}

	public MainPidDetectTask() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		String ticketHeartPid = "";
		String ticketHeartStr = "";

		try {
//			ticketHeartStr = ProcessUtil.getLastHeartBeat(Config.getInstance().getTicketVerifyHeartFile());
			ticketHeartStr = DeviceConfig.getInstance().getHeartVStr();
		} catch (Exception ex) {
			log.error("MainPidDetectTask getLastHeartBeat：", ex);
		}

		/**
		 * 主控线程监控
		 */
		if (ticketHeartStr != null && !ticketHeartStr.equals("")) {
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

				if (Config.getInstance().isAllowRebackMain()) { // true代表允许恢复
					log.info("--------------------------------");
					log.info("是否允许恢复java主控进程==" + Config.getInstance().isAllowRebackMain());
					log.info("java主控是否启动==" + Config.getInstance().isMainCrtlWork());
					try {
						Config.getInstance().setAllowRebackMain(false);
						log.info("准备执行恢复java主控进程的批处理");
						Runtime.getRuntime().exec(Config.getInstance().getStartPITVerifyCmd());
						
						DeviceConfig.getInstance().setHeartVStr("");
						log.info("Resatrt PITVerifyApp cmd==" + Config.getInstance().getStartPITVerifyCmd());
					} catch (Exception ex) {
						log.error("Resatrt PITVerifyApp......:", ex);
					}
				}

				/**
				 * 超时未收到心跳，需要杀死当前java主控进程
				 */
				if (heartWaitTime > Config.getInstance().getTicketVerify_Heard_DELAY()) {
					log.info("--------------------------------");
					log.info("heartWaitTime==" + heartWaitTime + ",ticketHeartPid==" + ticketHeartPid);
					log.info("是否允许恢复java主控进程==" + Config.getInstance().isAllowRebackMain());
					log.info("java主控是否启动==" + Config.getInstance().isMainCrtlWork());
					if (Config.getInstance().isMainCrtlWork()) { // java主控启动状态，可以杀死
						try {
							Config.getInstance().setMainCrtlWork(false);
							log.info("准备杀死java主控进程 " + ticketHeartPid);
							Runtime.getRuntime().exec("taskkill /F /PID " + ticketHeartPid);

							Config.getInstance().setAllowRebackMain(true); // 进程已杀死，允许执行恢复java主控操作
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
						log.info("Resatrt PITVerifyApp......When hbs is null");
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
