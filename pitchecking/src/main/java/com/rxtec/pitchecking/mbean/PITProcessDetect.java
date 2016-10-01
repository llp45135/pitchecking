package com.rxtec.pitchecking.mbean;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.CalUtils;

/**
 * 由人脸比对进程启动
 * 检脸进程保护
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

	@Override
	public void run() {
		while (true) {
			CommUtil.sleep(3000);
			String hbs = "";
			try {
				hbs = ProcessUtil.getLastHeartBeat();
			} catch (Exception ex) {
				log.error("", ex);
			}
			log.debug("hbs==" + hbs + "##");
			if (hbs != null) {
				hbs = hbs.trim();
				if (!hbs.equals("")) {
					pid = hbs.split("@")[0];
					String t = hbs.split("@")[1];
					String nowHT = CalUtils.getStringDateHaomiao();// (new
																	// Date()).getTime();
					log.debug(pid + " last heartbeat time=" + t);
					long ss = 0;
					try {
						ss = CalUtils.howLong("ms", t, nowHT);
					} catch (Exception ex) {
						log.error("", ex);
					}
					log.debug("ss===" + ss);
					if (ss > Config.HEART_BEAT_DELAY) {
						if (startStatus) {
							this.setStartStatus(false);
							try {
								log.info("准备杀死进程 " + pid);
								Runtime.getRuntime().exec("taskkill /F /PID " + pid);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							try {
								Runtime.getRuntime().exec(Config.getInstance().getStartPITAppCmd());
								CommUtil.sleep(5 * 1000);
								log.info("Resatrt PITTrackApp......");
								this.setStartStatus(true);
							} catch (Exception ex) {
								log.error("Restart:", ex);
							}
						}
					}
				} else if (hbs.equals("null")) {
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
					log.debug("未读取到 work log");
				}
			}
		}
	}

	public static void main(String[] args) {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleWithFixedDelay(new PITProcessDetect(), 0, 1, TimeUnit.SECONDS);

	}

}
