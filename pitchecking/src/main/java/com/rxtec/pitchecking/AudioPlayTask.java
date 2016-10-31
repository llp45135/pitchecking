package com.rxtec.pitchecking;

import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.AudioDevice;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.mbean.ProcessUtil;
import com.rxtec.pitchecking.utils.CalUtils;
import com.rxtec.pitchecking.utils.CommUtil;

public class AudioPlayTask implements Runnable {
	private Logger log = LoggerFactory.getLogger("AudioPlayTask");
	private static AudioPlayTask _instance;
	private int deviceStatus = Config.StopStatus;

	public static synchronized AudioPlayTask getInstance() {
		if (_instance == null) {
			_instance = new AudioPlayTask();
		}
		return _instance;
	}

	private AudioPlayTask() {
		String pidStr = ProcessUtil.getCurrentProcessID();
		log.info("" + pidStr);
		AudioDevice.getInstance().setPidstr(pidStr);
	}

	public void start(int audioStatus) {
		deviceStatus = audioStatus;
	}

	public void stop() {
		deviceStatus = Config.StopStatus;
	}

	@Override
	public void run() {
		if (CalUtils.getStringFullTime().equals("233500")) {
			log.info("超过23:30:00,必须停止引导语音");
			AudioDevice.getInstance().cleanLastAudio();
			AudioDevice.getInstance().setStartPlayTime("");
		}
		if (AudioDevice.getInstance().getStartPlayTime() != null
				&& !AudioDevice.getInstance().getStartPlayTime().equals("")) {
			try {
				long playtime = CalUtils.howLong("ms", AudioDevice.getInstance().getStartPlayTime(),
						CalUtils.getStringDateHaomiao());
				if (playtime >= AudioDevice.getInstance().getLastingTime()) {
					AudioDevice.getInstance().cleanLastAudio();
					AudioDevice.getInstance().setStartPlayTime("");
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				log.error("cleanLastAudio:", e);
			}

		}

		if (deviceStatus > Config.StopStatus) {
			if (deviceStatus == DeviceConfig.useHelpFlag) {
				if (Config.getInstance().getIsPlayHelpAudio() == 1) {
					AudioDevice.getInstance().playAudio(deviceStatus);
				}
			} else {
				AudioDevice.getInstance().playAudio(deviceStatus);
			}
			this.stop();
		}
	}

}
