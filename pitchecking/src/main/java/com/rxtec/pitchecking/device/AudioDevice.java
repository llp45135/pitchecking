package com.rxtec.pitchecking.device;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rxtec.pitchecking.AudioPlayTask;
import com.rxtec.pitchecking.utils.CalUtils;
import com.rxtec.pitchecking.utils.CommUtil;

import sun.audio.AudioData;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;
import sun.audio.ContinuousAudioDataStream;

/**
 * 音频播放
 * 
 * @author ZhaoLin
 *
 */
public class AudioDevice {
	private Log log = LogFactory.getLog("AudioDevice");
	private static AudioDevice _instance = new AudioDevice();
	// File audioFile = null;
	// File emerAudioFile = null; // 应急门离开语音
	// File takeTicketAudioFile = null;
	// File useHelpAudioFile = null;
	private String pidstr;

	FileInputStream fileIn = null;
	AudioStream as = null;
	AudioData ad = null;
	ContinuousAudioDataStream cads = null;
	private String startPlayTime = "";
	private long lastingTime = 4 * 1000;

	public static synchronized AudioDevice getInstance() {
		if (_instance == null) {
			_instance = new AudioDevice();
		}
		return _instance;
	}

	public long getLastingTime() {
		return lastingTime;
	}

	public void setLastingTime(long lastingTime) {
		this.lastingTime = lastingTime;
	}

	public String getStartPlayTime() {
		return startPlayTime;
	}

	public void setStartPlayTime(String startPlayTime) {
		this.startPlayTime = startPlayTime;
	}

	public String getPidstr() {
		return pidstr;
	}

	public void setPidstr(String pidstr) {
		this.pidstr = pidstr;
	}

	private AudioDevice() {
		// audioFile = new File(DeviceConfig.cameraWav);
		// emerAudioFile = new File(DeviceConfig.emerDoorWav);
		// takeTicketAudioFile = new File(DeviceConfig.takeTicketWav);
		// useHelpAudioFile = new File(DeviceConfig.useHelpWav);
	}

	public void killpid(String pidstr) {
		try {
			log.info("准备杀死进程" + pidstr);
			Runtime.getRuntime().exec("taskkill /F /PID " + pidstr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 中断当前语音
	 */
	public void cleanLastAudio() {
		try {
			if (this.cads != null) {
				AudioPlayer.player.stop(cads);
				cads.close();
				log.debug("中断当前循环语音");
			}
			if (this.as != null) {
				AudioPlayer.player.stop(as);
				as.close();
				if (cads == null) {
					log.debug("中断当前主控语音");
				}
			}
			if (this.fileIn != null) {
				fileIn.close();
			}
			cads = null;
			as = null;
			fileIn = null;

		} catch (Exception ex) {
			log.error("AudioDevice:" + ex);
		}
	}

	/**
	 * 
	 * @param audioFlag
	 */
	public void playAudio(int audioFlag) {
		this.cleanLastAudio();
		try {
			if (audioFlag == DeviceConfig.takeTicketFlag) {
				log.info("开始右声道播放语音" + DeviceConfig.takeTicketWav);
				VolumeControl.getInstance().LeftClose();
				fileIn = new FileInputStream(DeviceConfig.takeTicketWav);
				this.setLastingTime((long) (8.2 * 1000));
			} else if (audioFlag == DeviceConfig.checkFailedFlag) {
				log.info("开始右声道播放语音" + DeviceConfig.checkFailedWav);
				VolumeControl.getInstance().LeftClose();
				fileIn = new FileInputStream(DeviceConfig.checkFailedWav);
				this.setLastingTime((long) (5.6 * 1000));
			} else if (audioFlag == DeviceConfig.checkSuccFlag) {
				log.info("开始右声道播放语音" + DeviceConfig.checkSuccWav);
				VolumeControl.getInstance().LeftClose();
				fileIn = new FileInputStream(DeviceConfig.checkSuccWav);
				this.setLastingTime((long) (0.8 * 1000));
			} else if (audioFlag == DeviceConfig.useHelpFlag) {
				log.info("开始左声道播放语音" + DeviceConfig.useHelpWav);
				VolumeControl.getInstance().RightClose();
				fileIn = new FileInputStream(DeviceConfig.useHelpWav);
				this.setLastingTime((long) (60 * 60 * 24 * 1000));
			} else if (audioFlag == DeviceConfig.failedIdCardFlag) {
				log.info("开始左声道播放语音" + DeviceConfig.failedIdCardWav);
				VolumeControl.getInstance().RightClose();
				fileIn = new FileInputStream(DeviceConfig.failedIdCardWav);
				this.setLastingTime((long) (4.1 * 1000));
			} else if (audioFlag == DeviceConfig.failedQrcodeFlag) {
				log.info("开始左声道播放语音" + DeviceConfig.failedQrcodeWav);
				VolumeControl.getInstance().RightClose();
				fileIn = new FileInputStream(DeviceConfig.failedQrcodeWav);
				this.setLastingTime((long) (4 * 1000));
			} else if (audioFlag == DeviceConfig.neverTimeFlag) {
				log.info("开始左声道播放语音" + DeviceConfig.neverTimeWav);
				VolumeControl.getInstance().RightClose();
				fileIn = new FileInputStream(DeviceConfig.neverTimeWav);
				this.setLastingTime((long) (2.9 * 1000));
			} else if (audioFlag == DeviceConfig.passTimeFlag) {
				log.info("开始左声道播放语音" + DeviceConfig.passTimeWav);
				VolumeControl.getInstance().RightClose();
				fileIn = new FileInputStream(DeviceConfig.passTimeWav);
				this.setLastingTime((long) (2.9 * 1000));
			} else if (audioFlag == DeviceConfig.passStationFlag) {
				log.info("开始左声道播放语音" + DeviceConfig.passStationWav);
				VolumeControl.getInstance().RightClose();
				fileIn = new FileInputStream(DeviceConfig.passStationWav);
				this.setLastingTime((long) (2.2 * 1000));
			} else if (audioFlag == DeviceConfig.validIDandTicketFlag) {
				log.info("开始左声道播放语音" + DeviceConfig.validIDandTicketWav);
				VolumeControl.getInstance().RightClose();
				fileIn = new FileInputStream(DeviceConfig.validIDandTicketWav);
				this.setLastingTime((long) (3.3 * 1000));
			} else if (audioFlag == DeviceConfig.wrongStationFlag) {
				log.info("开始左声道播放语音" + DeviceConfig.wrongStationWav);
				VolumeControl.getInstance().RightClose();
				fileIn = new FileInputStream(DeviceConfig.wrongStationWav);
				this.setLastingTime((long) (2.4 * 1000));
			}

			as = new AudioStream(fileIn);
			if (audioFlag == DeviceConfig.takeTicketFlag) {
				AudioDevice.getInstance().setStartPlayTime("");
				AudioPlayer.player.start(as);
			} else {
				AudioDevice.getInstance().setStartPlayTime(CalUtils.getStringDateHaomiao());
				ad = as.getData();
				// 设置循环播放
				cads = new ContinuousAudioDataStream(ad);
				// 循环播放开始
				AudioPlayer.player.start(cads);
			}

		} catch (Exception ex) {
			log.error("AudioDevice:" + ex);
		}
	}

	public static void main(String args[]) throws Exception {
		// for (int i = 0; i < 3; i++) {
		//// AudioDevice.getInstance().play(DeviceConfig.idReaderWav);
		//// AudioDevice.getInstance().play(DeviceConfig.qrReaderWav);
		//
		// CommUtil.sleep(5000);
		// }

		// AudioPlayTask audioTask = new AudioPlayTask();
		// ExecutorService audioExecuter = Executors.newCachedThreadPool();
		// audioExecuter.execute(audioTask);
		// audioExecuter.shutdown();
		// exit
		// System.exit(0);

		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleWithFixedDelay(AudioPlayTask.getInstance(), 0, 100, TimeUnit.MILLISECONDS);

		CommUtil.sleep(1000);

		// AudioPlayTask.getInstance().start(DeviceConfig.takeTicketFlag); //
		//// 调用语音“请平视摄像头”
		//
		// CommUtil.sleep(10 * 1000);
		//
		 
		
		 AudioPlayTask.getInstance().start(DeviceConfig.takeTicketFlag);
		 CommUtil.sleep(20 * 1000);
		 
		 AudioPlayTask.getInstance().start(DeviceConfig.checkSuccFlag);
		 CommUtil.sleep(3 * 1000);

		AudioPlayTask.getInstance().start(DeviceConfig.checkFailedFlag);
		CommUtil.sleep(10 * 1000);

		AudioPlayTask.getInstance().start(DeviceConfig.useHelpFlag);
		CommUtil.sleep(10 * 1000);

		AudioPlayTask.getInstance().start(DeviceConfig.failedIdCardFlag);
		CommUtil.sleep(5 * 1000);

		AudioPlayTask.getInstance().start(DeviceConfig.failedQrcodeFlag);
		CommUtil.sleep(5 * 1000);

		AudioPlayTask.getInstance().start(DeviceConfig.neverTimeFlag);
		CommUtil.sleep(5 * 1000);

		AudioPlayTask.getInstance().start(DeviceConfig.passTimeFlag);
		CommUtil.sleep(5 * 1000);

		AudioPlayTask.getInstance().start(DeviceConfig.passStationFlag);
		CommUtil.sleep(5 * 1000);

		AudioPlayTask.getInstance().start(DeviceConfig.validIDandTicketFlag);
		CommUtil.sleep(5 * 1000);
		
		AudioPlayTask.getInstance().start(DeviceConfig.wrongStationFlag);
		CommUtil.sleep(5 * 1000);

		AudioDevice.getInstance().killpid(AudioDevice.getInstance().pidstr);
	}
}
