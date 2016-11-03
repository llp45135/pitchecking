package com.rxtec.pitchecking.mbean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.Date;

import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.net.event.GatCrtlBean;
import com.rxtec.pitchecking.utils.CalUtils;

public class ProcessUtil {
	private static Logger log = LoggerFactory.getLogger("RSFaceTrackTask");

	public static void main(String[] args) {
		String pid = getCurrentProcessID();
		System.out.println(pid + " write ret = " + writeHeartbeat(pid));
		System.out.println("last heartbeat time = " + getLastHeartBeat());

	}

	public static String getCurrentProcessID() {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		String pid = name.split("@")[0];
		return pid;

	}
	
	/**
	 * 当检脸线程被杀死时，写入标志0
	 * 当检脸线程被启动时，写入1
	 * @param rebackFile
	 * @param rebackFlag
	 * @return
	 */
	public static boolean writeRebackTrackAppFlag(String rebackFile,String rebackFlag){
		Date now = new Date();
		String nowTime = CalUtils.getStringDateHaomiao();// Long.toString(now.getTime());
		try {
			File logFile = new File(rebackFile);
			if (!logFile.exists()) {
				logFile.createNewFile();
			}
			FileWriter fw = new FileWriter(logFile);
			fw.write(rebackFlag);
			fw.flush();
			fw.close();
			return true;

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public static String getRebackTrackAppFlag(String rebackFile) {
		String s = "";
		try {
			File f = new File(rebackFile);
			if (!f.exists())
				s = "";
			else {
				FileInputStream fis = new FileInputStream(f);
				InputStreamReader reader = new InputStreamReader(fis);
				BufferedReader bufferedReader = new BufferedReader(reader);
				s = bufferedReader.readLine();
				fis.close();
				reader.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			s = "";
		}
		return s;
	}

	/**
	 * 写检脸线程心跳日志
	 * @param pid
	 * @return
	 */
	public static boolean writeHeartbeat(String pid) {
		Date now = new Date();
		String nowTime = CalUtils.getStringDateHaomiao();// Long.toString(now.getTime());
		try {
			File logFile = new File(Config.getInstance().getHeartBeatLogFile());
			if (!logFile.exists()) {
				logFile.createNewFile();
			}
			FileWriter fw = new FileWriter(logFile);
			String pidStr = pid + "@" + nowTime;
//			log.debug("pidStr==" + pidStr);
			fw.write(pidStr);
			fw.flush();
			fw.close();
			return true;

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 写暂停服务标志
	 * @param pid
	 * @return
	 */
	public static boolean writePauseLog(String pid) {
		String nowTime = CalUtils.getStringDateHaomiao();
		try {
			File logFile = new File(Config.getInstance().getPausePitcheckFile());
			if (!logFile.exists()) {
				logFile.createNewFile();
			}
			FileWriter fw = new FileWriter(logFile);
			String pidStr = pid + "@" + nowTime;
//			log.debug("pidStr==" + pidStr);
			fw.write(pidStr);
			fw.flush();
			fw.close();
			return true;

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 读取上次的暂停服务日志
	 * @return
	 */
	public static String getLastPauseFlag() {
		String s = "";
		try {
			File f = new File(Config.getInstance().getPausePitcheckFile());
			if (!f.exists())
				s = "";
			else {
				FileInputStream fis = new FileInputStream(f);
				InputStreamReader reader = new InputStreamReader(fis);
				BufferedReader bufferedReader = new BufferedReader(reader);
				s = bufferedReader.readLine();
				fis.close();
				reader.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			s = "";
		}
		return s;
	}

	/**
	 * 读取上次的检脸线程心跳日志
	 * @return
	 */
	public static String getLastHeartBeat() {
		String s = "";
		try {
			File f = new File(Config.getInstance().getHeartBeatLogFile());
			if (!f.exists())
				s = "";
			else {
				FileInputStream fis = new FileInputStream(f);
				InputStreamReader reader = new InputStreamReader(fis);
				BufferedReader bufferedReader = new BufferedReader(reader);
				s = bufferedReader.readLine();
				fis.close();
				reader.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			s = "";
		}
		return s;
	}
	
	
	/**
	 * 生成语音事件json
	 * @param audioEventType
	 * @return
	 */
	public static String createAudioJson(int audioEventType){
		ObjectMapper mapper = new ObjectMapper();
		GatCrtlBean gcBean  = new GatCrtlBean();
		String json = "";
		try {
			gcBean.setEvent(audioEventType);
			gcBean.setEventsource("FaceAudio");
			gcBean.setTarget("127.0.0.1");
			json = mapper.writeValueAsString(gcBean);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}

}
