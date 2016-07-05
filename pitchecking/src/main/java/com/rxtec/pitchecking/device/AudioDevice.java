package com.rxtec.pitchecking.device;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import com.rxtec.pitchecking.utils.CommUtil;

public class AudioDevice {
	private Log log = LogFactory.getLog("AudioDevice");
	private static AudioDevice _instance = new AudioDevice();

	public static synchronized AudioDevice getInstance() {
		if (_instance == null) {
			_instance = new AudioDevice();
		}
		return _instance;
	}

	private AudioDevice() {

	}

	/**
	 * 播放语音
	 * 
	 * @param filename
	 */
	public void play(String filename) {
		AudioInputStream audioStream = null;
		AudioFormat audioFormat = null;
		DataInputStream audioDis = null;
		byte[] audioSamples = null;
		try {
			log.debug("载入声音文件:" + filename);
			audioStream = AudioSystem.getAudioInputStream(new File(filename));
			audioFormat = audioStream.getFormat();

			int length = (int) (audioStream.getFrameLength() * audioFormat.getFrameSize());
			audioSamples = new byte[length];
			audioDis = new DataInputStream(audioStream);
			audioDis.readFully(audioSamples);
			audioStream.close();
			audioDis.close();
		} catch (UnsupportedAudioFileException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (audioStream != null)
					audioStream.close();
				if (audioDis != null)
					audioDis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		InputStream audioInputStream= null;
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
			log.debug("语音播放完毕..."+filename);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (dataLline != null) {
					dataLline.drain();
					dataLline.close();
				}
				if (audioInputStream != null) {
					audioInputStream.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void main(String args[]) throws Exception {
//		for (int i = 0; i < 3; i++) {
////			AudioDevice.getInstance().play(DeviceConfig.idReaderWav);
////			AudioDevice.getInstance().play(DeviceConfig.qrReaderWav);
//			
//			CommUtil.sleep(5000);
//		}
		
		AudioPlayTask audioTask = new AudioPlayTask();
		ExecutorService audioExecuter = Executors.newCachedThreadPool();
		audioExecuter.execute(audioTask);		
		audioExecuter.shutdown();		
		// exit
//		 System.exit(0);
	}
}
