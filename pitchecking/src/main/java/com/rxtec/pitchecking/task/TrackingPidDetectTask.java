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
import com.rxtec.pitchecking.mqtt.GatCtrlSenderBroker;
import com.rxtec.pitchecking.utils.CalUtils;
import com.rxtec.pitchecking.utils.CommUtil;

public class TrackingPidDetectTask implements Job {
	private Logger log = LoggerFactory.getLogger("PITProcessDetect");

	public TrackingPidDetectTask() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		String pid = "";
		String HeartBeatStr = "";
		try {
//			HeartBeatStr = ProcessUtil.getLastHeartBeat(Config.getInstance().getHeartBeatLogFile());
			HeartBeatStr = DeviceConfig.getInstance().getHeartBStr();
//			log.debug("HeartBeatStr=="+HeartBeatStr);
		} catch (Exception ex) {
			log.error("TrackingPidDetectTask getLastHeartBeat：", ex);
		}

		/**
		 * 后置摄像头心跳监测
		 */
		if (HeartBeatStr != null && !HeartBeatStr.equals("")) {
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
					log.info("--------------------------------");
					log.info("是否允许恢复后置检脸进程==" + Config.getInstance().isRebackTrackFlag());
					log.info("后置摄像头是否启动==" + Config.getInstance().isCameraWork());
					try {
						Config.getInstance().setRebackTrackFlag(false);
						log.info("准备执行恢复后置检脸进程的批处理");
						Runtime.getRuntime().exec(Config.getInstance().getStartPITTrackCmd());
						
						DeviceConfig.getInstance().setHeartBStr("");
						log.info("Resatrt PITTrackApp cmd==" + Config.getInstance().getStartPITTrackCmd());
					} catch (Exception ex) {
						log.error("Resatrt PITTrackApp......:", ex);
					}

					CommUtil.sleep(2000);

					try {
						log.info("已经执行恢复后置检脸进程的批处理");
						GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_PidProtect_CLIENT)
								.sendDoorCmd("PITEventTopic", DeviceConfig.OPEN_SECONDDOOR);
						DeviceConfig.getInstance().setAllowOpenSecondDoor(false);
						return;
					} catch (Exception ex) {
						log.error("Resatrt PITTrackApp......:", ex);
					}

				}
				/**
				 * 超时未收到心跳，需要杀死当前检脸进程
				 */
				if (heartWaitTime > Config.getInstance().getHEART_BEAT_DELAY()) {
					log.info("--------------------------------");
					log.info("heartWaitTime==" + heartWaitTime + ",pid==" + pid);
					log.info("是否允许恢复后置检脸进程==" + Config.getInstance().isRebackTrackFlag());
					log.info("后置摄像头是否启动==" + Config.getInstance().isCameraWork());
					if (Config.getInstance().isCameraWork()) { // 摄像头处于启动状态，可以杀死
						try {
							Config.getInstance().setCameraWork(false);
							log.info("准备杀死后置检脸进程 " + pid);
							Runtime.getRuntime().exec("taskkill /F /PID " + pid);

							Config.getInstance().setRebackTrackFlag(true);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							log.error("TrackingPidDetectTask taskkill:", e);
						} catch (Exception ex) {
							log.error("TrackingPidDetectTask taskkill:", ex);
						}
					}
//					else { // 长时间未恢复，说明没有恢复成功，需要再次恢复
//						if (heartWaitTime > 14 * 1000) {
//							try {
//								Config.getInstance().setCameraWork(false);
//								log.debug("准备再次杀死后置检脸进程 " + pid);
//								Runtime.getRuntime().exec("taskkill /F /PID " + pid);
//
//								Config.getInstance().setRebackTrackFlag(true);
//							} catch (IOException e) {
//								// TODO Auto-generated catch block
//								log.error("TrackingPidDetectTask taskkill:", e);
//							} catch (Exception ex) {
//								log.error("TrackingPidDetectTask taskkill:", ex);
//							}
//						}
//					}
				}
			}
		}
	}

}
