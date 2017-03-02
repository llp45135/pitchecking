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
			if (audioFlag == DeviceConfig.AudioTakeTicketFlag) {
				log.info("开始右声道播放语音" + DeviceConfig.AudioTakeTicketWav);
				VolumeControl.getInstance().LeftClose();
				fileIn = new FileInputStream(DeviceConfig.AudioTakeTicketWav);
				this.setLastingTime((long) (8.2 * 1000));
			} else if (audioFlag == DeviceConfig.AudioTakeCardFlag) {
				log.info("开始左声道播放语音" + DeviceConfig.AudioTakeAndTrackWav);
				VolumeControl.getInstance().RightClose();
				fileIn = new FileInputStream(DeviceConfig.AudioTakeAndTrackWav);
				this.setLastingTime((long) (15 * 1000));
			} else if (audioFlag == DeviceConfig.AudioTrackFaceFlag) {
				log.info("开始右声道播放语音" + DeviceConfig.AudioTrackFaceWav);
				VolumeControl.getInstance().LeftClose();
				fileIn = new FileInputStream(DeviceConfig.AudioTrackFaceWav);
				this.setLastingTime((long) (12.5 * 1000));
			} else if (audioFlag == DeviceConfig.AudioCheckFailedFlag) {
				log.info("开始右声道播放语音" + DeviceConfig.AudioCheckFailedWav);
				VolumeControl.getInstance().LeftClose();
				fileIn = new FileInputStream(DeviceConfig.AudioCheckFailedWav);
				this.setLastingTime((long) (5.6 * 1000));
			} else if (audioFlag == DeviceConfig.AudioCheckSuccFlag) {
				log.info("开始右声道播放语音" + DeviceConfig.AudioCheckSuccWav);
				VolumeControl.getInstance().LeftClose();
				fileIn = new FileInputStream(DeviceConfig.AudioCheckSuccWav);
				this.setLastingTime((long) (0.8 * 1000));
			} else if (audioFlag == DeviceConfig.AudioUseHelpFlag) {
				log.info("开始左声道播放语音" + DeviceConfig.AudioUseHelpWav);
				VolumeControl.getInstance().RightClose();
				fileIn = new FileInputStream(DeviceConfig.AudioUseHelpWav);
				this.setLastingTime((long) (60 * 60 * 24 * 1000));
			} else if (audioFlag == DeviceConfig.AudioFailedIdCardFlag) {
				log.info("开始左声道播放语音" + DeviceConfig.AudioFailedIdCardWav);
				VolumeControl.getInstance().RightClose();
				fileIn = new FileInputStream(DeviceConfig.AudioFailedIdCardWav);
				this.setLastingTime((long) (4.1 * 1000));
			} else if (audioFlag == DeviceConfig.AudioFailedQrcodeFlag) {
				log.info("开始左声道播放语音" + DeviceConfig.AudioFailedQrcodeWav);
				VolumeControl.getInstance().RightClose();
				fileIn = new FileInputStream(DeviceConfig.AudioFailedQrcodeWav);
				this.setLastingTime((long) (4 * 1000));
			} else if (audioFlag == DeviceConfig.AudioNeverTimeFlag) {
				log.info("开始左声道播放语音" + DeviceConfig.AudioNeverTimeWav);
				VolumeControl.getInstance().RightClose();
				fileIn = new FileInputStream(DeviceConfig.AudioNeverTimeWav);
				this.setLastingTime((long) (2.9 * 1000));
			} else if (audioFlag == DeviceConfig.AudioPassTimeFlag) {
				log.info("开始左声道播放语音" + DeviceConfig.AudioPassTimeWav);
				VolumeControl.getInstance().RightClose();
				fileIn = new FileInputStream(DeviceConfig.AudioPassTimeWav);
				this.setLastingTime((long) (2.9 * 1000));
			} else if (audioFlag == DeviceConfig.AudioPassStationFlag) {
				log.info("开始左声道播放语音" + DeviceConfig.AudioPassStationWav);
				VolumeControl.getInstance().RightClose();
				fileIn = new FileInputStream(DeviceConfig.AudioPassStationWav);
				this.setLastingTime((long) (2.2 * 1000));
			} else if (audioFlag == DeviceConfig.AudioValidIDandTicketFlag) {
				log.info("开始左声道播放语音" + DeviceConfig.AudioValidIDandTicketWav);
				VolumeControl.getInstance().RightClose();
				fileIn = new FileInputStream(DeviceConfig.AudioValidIDandTicketWav);
				this.setLastingTime((long) (3.3 * 1000));
			} else if (audioFlag == DeviceConfig.AudioWrongStationFlag) {
				log.info("开始左声道播放语音" + DeviceConfig.AudioWrongStationWav);
				VolumeControl.getInstance().RightClose();
				fileIn = new FileInputStream(DeviceConfig.AudioWrongStationWav);
				this.setLastingTime((long) (2.4 * 1000));
			}

			as = new AudioStream(fileIn);
			if (audioFlag == DeviceConfig.AudioTakeTicketFlag || audioFlag == DeviceConfig.AudioTakeCardFlag
					|| audioFlag == DeviceConfig.AudioTrackFaceFlag) {
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

		// AudioPlayTask.getInstance().start(DeviceConfig.AudioTakeTicketFlag);
		// CommUtil.sleep(20 * 1000);

		AudioPlayTask.getInstance().start(DeviceConfig.AudioTakeCardFlag);
//		CommUtil.sleep((long) (2.2 * 1000));
//
//		AudioPlayTask.getInstance().start(DeviceConfig.AudioTrackFaceFlag);
		CommUtil.sleep(15 * 1000);

		 AudioPlayTask.getInstance().start(DeviceConfig.AudioCheckSuccFlag);
		 CommUtil.sleep(3 * 1000);
		
		 AudioPlayTask.getInstance().start(DeviceConfig.AudioCheckFailedFlag);
		 CommUtil.sleep(10 * 1000);
		
		 AudioPlayTask.getInstance().start(DeviceConfig.AudioUseHelpFlag);
		 CommUtil.sleep(10 * 1000);
		
		 AudioPlayTask.getInstance().start(DeviceConfig.AudioFailedIdCardFlag);
		 CommUtil.sleep(5 * 1000);
		
		 AudioPlayTask.getInstance().start(DeviceConfig.AudioFailedQrcodeFlag);
		 CommUtil.sleep(5 * 1000);
		
		 AudioPlayTask.getInstance().start(DeviceConfig.AudioNeverTimeFlag);
		 CommUtil.sleep(5 * 1000);
		
		 AudioPlayTask.getInstance().start(DeviceConfig.AudioPassTimeFlag);
		 CommUtil.sleep(5 * 1000);
		
		 AudioPlayTask.getInstance().start(DeviceConfig.AudioPassStationFlag);
		 CommUtil.sleep(5 * 1000);
		
		 AudioPlayTask.getInstance().start(DeviceConfig.AudioValidIDandTicketFlag);
		 CommUtil.sleep(5 * 1000);
		
		 AudioPlayTask.getInstance().start(DeviceConfig.AudioWrongStationFlag);
		 CommUtil.sleep(5 * 1000);

		AudioDevice.getInstance().killpid(AudioDevice.getInstance().pidstr);
	}
}
