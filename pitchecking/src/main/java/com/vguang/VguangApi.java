package com.vguang;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rxtec.pitchecking.IDReader;
import com.rxtec.pitchecking.QRReader;
import com.rxtec.pitchecking.utils.DateUtils;

public class VguangApi implements Runnable {
	private static Log log = LogFactory.getLog("VguangApi");
	public static final int DEVICE_VALID = 1; // 设备有效
	public static final int DEVICE_INVALID = 2; // 设备无效
	static {
		System.loadLibrary("dll_vguang_java");
	}

	// 设置QR引擎状态，true时qr引擎开启；false时qr引擎关闭
	public native static void setQRable(boolean bqr);

	// 设置DM引擎状态，true时dm引擎开启；false时dm引擎关闭
	public native static void setDMable(boolean bdm);

	// 设置一维码引擎状态，true时一维码引擎开启；false时一维码引擎关闭
	public native static void setBarcode(boolean bbarcode);

	// 设置自动休眠状态，true时自动休眠开启；false时自动休眠关闭
	public native static void setAI(boolean bai);

	// 设置自动休眠灵敏度，1~64，缺省10
	public native static void setAISensitivity(int aiLimit);

	// 设置自动休眠响应时间，单位秒
	public native static void setAIResponseTime(int responseTime);

	// 设置解码间隔时间，单位毫秒
	public native static void setDeodeIntervalTime(int intervalTime);

	// 设置扬声器状态，true时扬声器(缺省声音)开启；false时扬声器(缺省声音)关闭
	public native static void setBeepable(boolean bbeep);

	// 扬声器声音，times取值为1,2,3
	public native static void beep(int times);

	// 开灯
	public native static void lightOn();

	// 关灯
	public native static void lightOff();

	// 打开设备
	public native static void openDevice();

	// 关闭设备
	public native static void closeDevice();

	// 重启设备
	public native static void resetDevice();

	public static void applyDeviceSetting() {
		// 设置QR状态
		VguangApi.setQRable(true);
		// 设置DM状态
		VguangApi.setDMable(true);
		// 设置Bar状态
		VguangApi.setBarcode(true);

		// 设置解码间隔时间，单位毫秒
		VguangApi.setDeodeIntervalTime(300);

		// 设置自动休眠状态
		VguangApi.setAI(false);
		int aiLimit = 20;
		if (aiLimit < 1 || aiLimit > 64) {
			aiLimit = 20;
		}
		// 设置自动休眠灵敏度
		VguangApi.setAISensitivity(aiLimit);
		// 设置自动休眠响应时间，单位秒
		VguangApi.setAIResponseTime(30);

		// 设置扬声器状态
		VguangApi.setBeepable(true);
	}

	/**
	 * 扫码回调函数） 需要根据实现情况修改实现
	 * 
	 * @param decodeStrBytes
	 *            解码字符串对应的byte数组（缺省是GBK编码）
	 */
	public static void decodeCallBack(byte[] decodeStrBytes) {
		String str = new String(decodeStrBytes);
//		 log.debug("str==" + str);
		String year = DateUtils.getStringDateShort2().substring(0, 4);
		QRReader.getInstance().performDeviceCallback(str, year);
	}

	/**
	 * 设备状态变化回调函数 需要根据实现情况修改
	 * 
	 * @param status
	 *            设备状态,DEVICE_VALID(1)-设备有效,DEVICE_INVALID(2)-设备无效
	 */
	public static void deviceStatusCallBack(int status) {
		// if(VguangSample.vguangSample != null){
		// VguangSample.vguangSample.setDeviceStatus(status);
		// }
//		log.debug("#########==="+status);
		return;
	}

	public static void startScan() {
//		ExecutorService executor = Executors.newCachedThreadPool();
//		executor.execute(new VguangApi());
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleWithFixedDelay(new VguangApi(), 0, 150, TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}
}
