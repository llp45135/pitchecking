package com.rxtec.pitchecking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.AudioDevice;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.mbean.ProcessUtil;
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
		if (deviceStatus > Config.StopStatus) {
			log.info("开始播放语音" + deviceStatus);
			AudioDevice.getInstance().play(deviceStatus);
			this.stop();
		}
	}

}
