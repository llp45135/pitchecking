package com.rxtec.pitchecking.mq.wharf;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.mqtt.GatCtrlReceiverBroker;
import com.rxtec.pitchecking.net.event.wharf.PhotoReceiptBean;
import com.rxtec.pitchecking.utils.BASE64Util;

public class PITInfoWharfPublisher {

	private LinkedBlockingQueue<PhotoReceiptBean> verifyDataQueue = new LinkedBlockingQueue<PhotoReceiptBean>(3);

	private Log log = LogFactory.getLog("PITInfoWharfPublisher");

	private static PITInfoWharfPublisher _instance = new PITInfoWharfPublisher();

	public static synchronized PITInfoWharfPublisher getInstance() {
		if (_instance == null)
			_instance = new PITInfoWharfPublisher();
		return _instance;
	}

	private PITInfoWharfPublisher() {

	}

	public void startService(int msgType) {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		if (msgType == 2) {
			PITInfoWharfTopicSender verifySender = new PITInfoWharfTopicSender(verifyDataQueue);
			scheduler.scheduleWithFixedDelay(verifySender, 0, 100, TimeUnit.MILLISECONDS);
		}

		// ExecutorService es = Executors.newFixedThreadPool(3);
		// es.submit(frameSender);
		// es.shutdown();
		//

	}
	
	

	/**
	 * 将人脸数据转成同广铁公安局网安处同样的json并offer进verifyDataQueue
	 * 
	 * @param d
	 */
	public void offerVerifyData(int msgType, String ipAddress, String pitDate, String pitTime, float verifyResult, int isVerifyPassed, IDCard idCard,
			byte[] idCardImgArray, byte[] faceImgArray, byte[] frameImgArray) {
		try {
			PhotoReceiptBean bean = new PhotoReceiptBean();

			bean.setMsgType(msgType);
			bean.setIpAddress(ipAddress);
			bean.setPitDate(pitDate);
			bean.setPitTime(pitTime);
			bean.setVerifyResult(verifyResult);
			bean.setIsVerifyPassed(isVerifyPassed);
			bean.setIdCardNo(idCard.getIdNo());
			bean.setGender(idCard.getGender());
			bean.setAge(idCard.getAge());
			bean.setIdbirth(idCard.getIDBirth());
			bean.setIdnation(idCard.getIDNation());
			bean.setCitizenName(idCard.getPersonName());
			bean.setIddwelling(idCard.getIDDwelling());
			bean.setIdissue(idCard.getIDIssue());
			bean.setIdefficb(idCard.getIDEfficb());
			bean.setIdeffice(idCard.getIDEffice());
			if (idCardImgArray != null)
				bean.setIdPicImageBase64(BASE64Util.encryptBASE64(idCardImgArray));
			if (faceImgArray != null)
				bean.setFaceImageBase64(BASE64Util.encryptBASE64(faceImgArray));
			if (frameImgArray != null)
				bean.setFrameImageBase64(BASE64Util.encryptBASE64(frameImgArray));

			if (!verifyDataQueue.offer(bean)) {
				verifyDataQueue.poll();
				verifyDataQueue.offer(bean);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("createCAMOpenJson:", e);
		}
	}
	
	
	
	/**
	 * 不使用byte数组，统一用base64编码格式
	 * @param msgType
	 * @param ipAddress
	 * @param pitDate
	 * @param pitTime
	 * @param verifyResult
	 * @param isVerifyPassed
	 * @param idCard
	 * @param idCardImgBase64
	 * @param faceImgBase64
	 * @param frameImgBase64
	 */
	public void offerVerifyData(int msgType, String ipAddress, String pitDate, String pitTime, float verifyResult, int isVerifyPassed, IDCard idCard,
			String idCardImgBase64, String faceImgBase64, String frameImgBase64) {
		try {
			PhotoReceiptBean bean = new PhotoReceiptBean();

			bean.setMsgType(msgType);
			bean.setIpAddress(ipAddress);
			bean.setPitDate(pitDate);
			bean.setPitTime(pitTime);
			bean.setVerifyResult(verifyResult);
			bean.setIsVerifyPassed(isVerifyPassed);
			bean.setIdCardNo(idCard.getIdNo());
			bean.setGender(idCard.getGender());
			bean.setAge(idCard.getAge());
			bean.setIdbirth(idCard.getIDBirth());
			bean.setIdnation(idCard.getIDNation());
			bean.setCitizenName(idCard.getPersonName());
			bean.setIddwelling(idCard.getIDDwelling());
			bean.setIdissue(idCard.getIDIssue());
			bean.setIdefficb(idCard.getIDEfficb());
			bean.setIdeffice(idCard.getIDEffice());
			if (idCardImgBase64 != null)
				bean.setIdPicImageBase64(idCardImgBase64);
			if (faceImgBase64 != null)
				bean.setFaceImageBase64(faceImgBase64);
			if (frameImgBase64 != null)
				bean.setFrameImageBase64(frameImgBase64);

			if (!verifyDataQueue.offer(bean)) {
				verifyDataQueue.poll();
				verifyDataQueue.offer(bean);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("createCAMOpenJson:", e);
		}
	}

	public static void main(String[] args) {
		PITInfoWharfPublisher.getInstance().startService(3);
		GatCtrlReceiverBroker.getInstance("Alone");// 启动PITEventTopic本地监听
		// RemoteMonitorPublisher.getInstance().offerFrameData(new byte[] { 1,
		// 0, 2, 3 });
		// RemoteMonitorPublisher.getInstance().offerEventData(DeviceConfig.Event_OpenFirstDoor);
		// RemoteMonitorPublisher.getInstance().offerEventData(DeviceConfig.Event_OpenSecondDoor);
		// RemoteMonitorPublisher.getInstance().offerEventData(DeviceConfig.Event_OpenThirdDoor);
	}
}
