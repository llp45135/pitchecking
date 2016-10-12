package com.rxtec.pitchecking.device;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rxtec.pitchecking.AudioPlayTask;
import com.rxtec.pitchecking.utils.CalUtils;
import com.rxtec.pitchecking.utils.CommUtil;

import sun.audio.AudioData;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;
import sun.audio.ContinuousAudioDataStream;

public class AudioDevice {
	private Log log = LogFactory.getLog("AudioDevice");
	private static AudioDevice _instance = new AudioDevice();
	File audioFile = null;
	File emerAudioFile = null; // 应急门离开语音
	File takeTicketAudioFile = null;
	private String pidstr;

	FileInputStream fileIn = null;
	AudioStream as = null;
	AudioData ad = null;
	ContinuousAudioDataStream cads = null;
	private String startPlayTime = "";

	public static synchronized AudioDevice getInstance() {
		if (_instance == null) {
			_instance = new AudioDevice();
		}
		return _instance;
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
		audioFile = new File(DeviceConfig.cameraWav);
		emerAudioFile = new File(DeviceConfig.emerDoorWav);
		takeTicketAudioFile = new File(DeviceConfig.takeTicketWav);
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
	 * 播放语音
	 * 
	 * @param filename
	 */
	public void play(int audioFlag) {
		AudioInputStream audioStream = null;
		AudioFormat audioFormat = null;
		DataInputStream audioDis = null;
		byte[] audioSamples = null;
		try {
			if (audioFlag == DeviceConfig.cameraFlag) {
				// log.debug("载入声音文件:" + audioFile);
				audioStream = AudioSystem.getAudioInputStream(audioFile);
			} else if (audioFlag == DeviceConfig.emerDoorFlag) {
				// log.debug("载入声音文件:" + emerAudioFile);
				audioStream = AudioSystem.getAudioInputStream(emerAudioFile);
			} else if (audioFlag == DeviceConfig.takeTicketFlag) {
				// log.debug("载入声音文件:" + takeTicketAudioFile);
				audioStream = AudioSystem.getAudioInputStream(takeTicketAudioFile);
			}
			audioFormat = audioStream.getFormat();

			int length = (int) (audioStream.getFrameLength() * audioFormat.getFrameSize());
			audioSamples = new byte[length];
			audioDis = new DataInputStream(audioStream);
			audioDis.readFully(audioSamples);
			audioStream.close();
			audioDis.close();
		} catch (UnsupportedAudioFileException ex) {
			log.error("AudioDevice:" + ex);
		} catch (IOException ex) {
			log.error("AudioDevice:" + ex);
		} catch (Exception ex) {
			log.error("AudioDevice:" + ex);
		} finally {
			try {
				if (audioStream != null)
					audioStream.close();
				if (audioDis != null)
					audioDis.close();
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				log.error("AudioDevice:" + ex);
			}
		}
		int bufferSize = audioFormat.getFrameSize() * Math.round(audioFormat.getSampleRate() / 10);
		byte[] audioBuffer = new byte[bufferSize];
		// create a line to play to
		SourceDataLine dataLline = null;
		try {
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
			dataLline = (SourceDataLine) AudioSystem.getLine(info);
			dataLline.open(audioFormat, bufferSize);
		} catch (LineUnavailableException ex) {
			ex.printStackTrace();
			return;
		}
		// start the line
		dataLline.start();
		// copy data to the line
		InputStream audioInputStream = null;
		try {
			audioInputStream = new ByteArrayInputStream(audioSamples);
			int numBytesRead = 0;
			while (numBytesRead != -1) {
				numBytesRead = audioInputStream.read(audioBuffer, 0, audioBuffer.length);
				if (numBytesRead != -1) {
					dataLline.write(audioBuffer, 0, numBytesRead);
				}
			}
			dataLline.drain();
			dataLline.close();
			audioInputStream.close();
			audioBuffer = null;
			audioSamples = null;
			// log.debug("语音播放完毕..." + audioFile);
		} catch (IOException ex) {
			log.error("AudioDevice:" + ex);
		} catch (Exception ex) {
			log.error("AudioDevice:" + ex);
		} finally {
			try {
				if (dataLline != null) {
					dataLline.drain();
					dataLline.close();
				}
				if (audioInputStream != null) {
					audioInputStream.close();
				}
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				log.error("AudioDevice:" + ex);
			}
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
			if (audioFlag == DeviceConfig.cameraFlag) {
				log.info("开始播放语音" + DeviceConfig.cameraWav);
				fileIn = new FileInputStream(DeviceConfig.cameraWav);
			} else if (audioFlag == DeviceConfig.emerDoorFlag) {
				log.info("开始播放语音" + DeviceConfig.emerDoorWav);
				fileIn = new FileInputStream(DeviceConfig.emerDoorWav);
			} else if (audioFlag == DeviceConfig.takeTicketFlag) {
				log.info("开始播放语音" + DeviceConfig.takeTicketWav);
				fileIn = new FileInputStream(DeviceConfig.takeTicketWav);
			} else if (audioFlag == DeviceConfig.checkSuccFlag) {
				log.info("开始播放语音" + DeviceConfig.checkSuccWav);
				fileIn = new FileInputStream(DeviceConfig.checkSuccWav);
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

		AudioPlayTask.getInstance().start(DeviceConfig.takeTicketFlag); // 调用语音“请平视摄像头”

		CommUtil.sleep(10 * 1000);

		AudioPlayTask.getInstance().start(DeviceConfig.checkSuccFlag);
		CommUtil.sleep(4000);

		AudioPlayTask.getInstance().start(DeviceConfig.takeTicketFlag);
		CommUtil.sleep(4500);

		AudioPlayTask.getInstance().start(DeviceConfig.emerDoorFlag);
		CommUtil.sleep(4000);

		// AudioPlayTask.getInstance().start(DeviceConfig.takeTicketFlag);
		CommUtil.sleep(12 * 1000);

		// AudioDevice.getInstance().killpid(AudioDevice.getInstance().pidstr);
	}
}
