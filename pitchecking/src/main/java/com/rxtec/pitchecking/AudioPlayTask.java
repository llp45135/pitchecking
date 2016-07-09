package com.rxtec.pitchecking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.AudioDevice;

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
	}

	public void start() {
		deviceStatus = Config.StartStatus;
	}

	public void stop() {
		deviceStatus = Config.StopStatus;
	}

	@Override
	public void run() {
		if (deviceStatus == Config.StartStatus) {
			AudioDevice.getInstance().play();
			this.stop();
		}
	}

}
