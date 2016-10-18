package com.rxtec.pitchecking.mbean;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.mqtt.GatCtrlSenderBroker;
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
	private Logger log = LoggerFactory.getLogger("PITProcessDetect");
	boolean startStatus = true;

	public boolean isStartStatus() {
		return startStatus;
	}

	public void setStartStatus(boolean startStatus) {
		this.startStatus = startStatus;
	}

	public PITProcessDetect() {
		log.info("初始化心跳侦测线程");
	}

	@Override
	public void run() {
		while (true) {
			CommUtil.sleep(3000);
			String HeartBeatStr = "";
			try {
				HeartBeatStr = ProcessUtil.getLastHeartBeat();
			} catch (Exception ex) {
				log.error("PITProcessDetect getLastHeartBeat：", ex);
			}
			// log.debug("HeartBeatStr==" + HeartBeatStr + "##");
			if (HeartBeatStr != null) {
				HeartBeatStr = HeartBeatStr.trim();
				if (!HeartBeatStr.equals("")) {
					pid = HeartBeatStr.split("@")[0];
					String t = HeartBeatStr.split("@")[1];
					String nowHT = CalUtils.getStringDateHaomiao();// (new
																	// Date()).getTime();
					// log.debug(pid + " last heartbeat time=" + t);
					long heartWaitTime = 0;
					try {
						heartWaitTime = CalUtils.howLong("ms", t, nowHT);
					} catch (Exception ex) {
						log.error("Cal heartWaitTime:", ex);
					}
					// log.debug("heartWaitTime===" + heartWaitTime);
					// log.info("rebackFlag=="+Config.getInstance().isRebackTrackFlag());
					if (!Config.getInstance().isRebackTrackFlag()) {
						try {
							Config.getInstance().setRebackTrackFlag(true);
							log.info("准备执行恢复检脸进程的批处理");
							Runtime.getRuntime().exec(Config.getInstance().getStartPITAppCmd());
							log.info("Resatrt PITTrackApp......");
							this.setStartStatus(true);
						} catch (Exception ex) {
							log.error("Resatrt PITTrackApp......:", ex);
						}
						try {
							File heartFile = new File(Config.getInstance().getHeartBeatLogFile());
							heartFile.delete();
							log.info("已经执行恢复检脸进程的批处理");
							GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT)
									.sendDoorCmd("PITEventTopic", DeviceConfig.OPEN_SECONDDOOR);
							continue;
						} catch (Exception ex) {
							log.error("Resatrt PITTrackApp......:", ex);
						}

					}
					/**
					 * 超时未收到心跳，需要杀死当前检脸进程
					 */
					if (heartWaitTime > Config.getInstance().getHEART_BEAT_DELAY()) {
						log.debug("heartWaitTime==" + heartWaitTime + ",pid==" + pid);
						if (startStatus) {
							this.setStartStatus(false);
							try {
								log.info("准备杀死进程 " + pid);
								Config.getInstance().setRebackTrackFlag(false);
								Runtime.getRuntime().exec("taskkill /F /PID " + pid);

								// GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT)
								// .sendDoorCmd("PITEventTopic",
								// DeviceConfig.OPEN_SECONDDOOR);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								log.error("PITProcessDetect taskkill:", e);
							} catch (Exception ex) {
								log.error("PITProcessDetect taskkill:", ex);
							}
						}
					}
				} else if (HeartBeatStr.equals("null")) {
					if (startStatus) {
						try {
							Runtime.getRuntime().exec(Config.getInstance().getStartPITAppCmd());
							CommUtil.sleep(10 * 1000);
							log.info("Resatrt PITCheckApp......When hbs is null");
							this.setStartStatus(true);
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
