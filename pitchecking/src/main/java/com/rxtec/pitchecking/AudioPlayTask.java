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
	private int lastAudioType = -1;

	public static synchronized AudioPlayTask getInstance() {
		if (_instance == null) {
			_instance = new AudioPlayTask();
		}
		return _instance;
	}

	private AudioPlayTask() {
		String pidStr = ProcessUtil.getCurrentProcessID();
		log.debug("" + pidStr);
		AudioDevice.getInstance().setPidstr(pidStr);
	}

	public int getLastAudioType() {
		return lastAudioType;
	}

	public void setLastAudioType(int lastAudioType) {
		this.lastAudioType = lastAudioType;
	}

	public void start(int audioStatus) {
		deviceStatus = audioStatus;
	}

	public void stop() {
		deviceStatus = Config.StopStatus;
	}

	@Override
	public void run() {
		if (CalUtils.getStringFullTime().equals("230000")) {
			log.debug("超过23:00:00,必须停止引导语音");
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
					AudioDevice.getInstance().setInPlaying(false);
					log.info("语音播放时间到，已经切断");
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				log.error("cleanLastAudio:", e);
			}

		}

		if (deviceStatus > Config.StopStatus) {
			if (deviceStatus == DeviceConfig.AudioUseHelpFlag) {
				if (Config.getInstance().getIsPlayHelpAudio() == 1) {
					AudioDevice.getInstance().playAudio(deviceStatus);
				}
			} else {
				if (!AudioDevice.getInstance().isInPlaying() || (AudioDevice.getInstance().isInPlaying() && AudioDevice.getInstance().getNowPlayId() != deviceStatus)) {
//					log.info("是否在播放中 = " + AudioDevice.getInstance().isInPlaying());
//					log.info("是否满足条件：准备播放和正在播放的不一样 = " + (AudioDevice.getInstance().isInPlaying() && AudioDevice.getInstance().getNowPlayId() != deviceStatus));
					AudioDevice.getInstance().playAudio(deviceStatus);
				}
//				else{
//					log.info("是否在播放中 = " + AudioDevice.getInstance().isInPlaying());
//					log.info("是否满足条件：准备播放和正在播放的不一样 = " + (AudioDevice.getInstance().isInPlaying() && AudioDevice.getInstance().getNowPlayId() != deviceStatus));
//				}
			}
			this.stop();
		}
	}

}
