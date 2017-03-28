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

public class StandaloneCheckDetectTask implements Job {
	private Logger log = LoggerFactory.getLogger("PITProcessDetect");
	
	private boolean checkHeartStatus = true;

	public boolean isCheckHeartStatus() {
		return checkHeartStatus;
	}

	public void setCheckHeartStatus(boolean checkHeartStatus) {
		this.checkHeartStatus = checkHeartStatus;
	}

	public StandaloneCheckDetectTask() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		String checkHeartPid = "";
		String checkHeartStr = "";

		try {
//			checkHeartStr = ProcessUtil.getLastHeartBeat(Config.getInstance().getStandaloneCheckHeartFile());
			checkHeartStr = DeviceConfig.getInstance().getHeartAStr();
		} catch (Exception ex) {
			log.error("StandaloneCheckDetectTask getLastHeartBeat：", ex);
		}

		/**
		 * 主控线程监控
		 */
		if (checkHeartStr != null && !checkHeartStr.equals("")) {
			checkHeartStr = checkHeartStr.trim();
			if (!checkHeartStr.equals("")) { // 读到进程号
				checkHeartPid = checkHeartStr.split("@")[0];
				String t = checkHeartStr.split("@")[1];
				String nowHT = CalUtils.getStringDateHaomiao();// (new
																// Date()).getTime();
				// log.debug(pid + " last heartbeat time=" + t);
				long heartWaitTime = 0;
				try {
					heartWaitTime = CalUtils.howLong("ms", t, nowHT);
				} catch (Exception ex) {
					log.error("Cal heartWaitTime:", ex);
				}

				if (Config.getInstance().isAllowRebackStandalone()) { // true代表允许恢复
					log.info("--------------------------------");
					log.info("是否允许恢复独立比对进程==" + Config.getInstance().isAllowRebackStandalone());
					log.info("独立比对是否启动==" + Config.getInstance().isStandaloneCheckWork());
					try {
						Config.getInstance().setAllowRebackStandalone(false);
						log.info("准备执行恢复恢复独立比对进程的批处理");
						Runtime.getRuntime().exec(Config.getInstance().getRestartStandaloneCheckCmd());
						
						DeviceConfig.getInstance().setHeartAStr("");
						log.info("Resatrt StandaloneCheck cmd==" + Config.getInstance().getRestartStandaloneCheckCmd());
					} catch (Exception ex) {
						log.error("Resatrt StandaloneCheck......:", ex);
					}
				}

				/**
				 * 超时未收到心跳，需要杀死当前独立比对进程
				 */
				if (heartWaitTime > 4000) {
					log.info("--------------------------------");
					log.info("heartWaitTime==" + heartWaitTime + ",checkHeartPid==" + checkHeartPid);
					log.info("是否允许恢复独立比对进程==" + Config.getInstance().isAllowRebackStandalone());
					log.info("独立比对是否启动==" + Config.getInstance().isStandaloneCheckWork());
					if (Config.getInstance().isStandaloneCheckWork()) { // 独立比对启动状态，可以杀死
						try {
							Config.getInstance().setStandaloneCheckWork(false);
							log.info("准备杀死独立比对进程 " + checkHeartPid);
							Runtime.getRuntime().exec("taskkill /F /PID " + checkHeartPid);

							Config.getInstance().setAllowRebackStandalone(true); // 进程已杀死，允许执行恢复独立比对操作
						} catch (IOException e) {
							// TODO Auto-generated catch block
							log.error("PITProcessDetect taskkill:", e);
						} catch (Exception ex) {
							log.error("PITProcessDetect taskkill:", ex);
						}
					}
				}
			} else if (checkHeartStr.equals("null")) {
				if (checkHeartStatus) {
					this.setCheckHeartStatus(false);
					try {
						Runtime.getRuntime().exec(Config.getInstance().getRestartStandaloneCheckCmd());
						CommUtil.sleep(10 * 1000);
						log.info("Resatrt StandaloneCheck......When hbs is null");
						this.setCheckHeartStatus(true);
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
