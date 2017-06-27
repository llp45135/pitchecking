package com.rxtec.pitchecking.task;

import java.io.IOException;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.device.smartmonitor.MonitorXMLUtil;
import com.rxtec.pitchecking.mbean.ProcessUtil;
import com.rxtec.pitchecking.utils.CalUtils;

public class FrontTrackPidDetectTask implements Job {
	private Logger log = LoggerFactory.getLogger("PITProcessDetect");

	public FrontTrackPidDetectTask() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		String frontCamearPid = "";
		String frontHeartStr = "";
		try {
//			frontHeartStr = ProcessUtil.getLastHeartBeat(Config.getInstance().getFrontHeartBeatLogFile());
			
			frontHeartStr = DeviceConfig.getInstance().getHeartFStr();

		} catch (Exception ex) {
			log.error("FrontTrackPidDetectTask getLastHeartBeat：", ex);
		}
		/**
		 * 前置摄像头心跳监测
		 */
		if (frontHeartStr != null && !frontHeartStr.equals("")) {
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

				if (Config.getInstance().isRebackFrontTrackFlag()) { // true代表允许恢复
					log.info("--------------------------------");
					log.info("是否允许恢复前置检脸进程==" + Config.getInstance().isRebackFrontTrackFlag());
					log.info("前置摄像头是否启动==" + Config.getInstance().isFrontCameraWork());
					try {
						Config.getInstance().setRebackFrontTrackFlag(false);
						log.info("准备执行恢复前置检脸进程的批处理");
						Runtime.getRuntime().exec(Config.getInstance().getRestartFrontTrackCmd());
						
						DeviceConfig.getInstance().setHeartFStr("");
						log.info("Resatrt PITFrontTrackApp cmd==" + Config.getInstance().getRestartFrontTrackCmd());
					} catch (Exception ex) {
						log.error("Resatrt PITFrontTrackApp......:", ex);
					}
				}
				/**
				 * 超时未收到心跳，需要杀死当前检脸进程
				 */
				if (heartWaitTime > Config.getInstance().getHEART_BEAT_DELAY()) {
					log.info("--------------------------------");
					log.info("heartWaitTime==" + heartWaitTime + ",frontCamearPid==" + frontCamearPid);
					log.info("是否允许恢复前置检脸进程==" + Config.getInstance().isRebackFrontTrackFlag());
					log.info("前置摄像头是否启动==" + Config.getInstance().isFrontCameraWork());
					if (Config.getInstance().isFrontCameraWork()) { // 摄像头处于启动状态，可以杀死
						try {
							Config.getInstance().setFrontCameraWork(false);
							log.info("准备杀死前置检脸进程 " + frontCamearPid);
							Runtime.getRuntime().exec("taskkill /F /PID " + frontCamearPid);

							Config.getInstance().setRebackFrontTrackFlag(true); // 进程已杀死，允许执行恢复操作
							
							//更新前置摄像头状态
							MonitorXMLUtil.updateBaseInfoFonMonitor(Config.getInstance().getBaseInfoXMLPath(), "002", 1);
							MonitorXMLUtil.updateDoorStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), "001", "001", "002", "002");
							MonitorXMLUtil.updateEntirStatusForMonitor(Config.getInstance().getStatusInfoXMLPath(), 1);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							log.error("PITProcessDetect taskkill:", e);
						} catch (Exception ex) {
							log.error("PITProcessDetect taskkill:", ex);
						}
					} else { // 长时间未恢复，说明没有恢复成功，需要再次恢复
						if (heartWaitTime > 14 * 1000) {
							try {
								Config.getInstance().setFrontCameraWork(false);
								log.info("准备再次杀死前置检脸进程 " + frontCamearPid);
								Runtime.getRuntime().exec("taskkill /F /PID " + frontCamearPid);

								Config.getInstance().setRebackFrontTrackFlag(true); // 进程已杀死，允许执行恢复操作
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
		}
	}

}
