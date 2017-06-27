package com.rxtec.pitchecking;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.event.IDCardReaderEvent;
import com.rxtec.pitchecking.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.mbean.ProcessUtil;
import com.rxtec.pitchecking.mq.police.PITInfoPolicePublisher;
import com.rxtec.pitchecking.mq.quickhigh.PITInfoQuickPublisher;
import com.rxtec.pitchecking.mq.wharf.PITInfoWharfPublisher;
import com.rxtec.pitchecking.mqtt.GatCtrlSenderBroker;
import com.rxtec.pitchecking.mqtt.MqttSenderBroker;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.FaceDetectionService;
import com.rxtec.pitchecking.picheckingservice.IFaceTrackService;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
import com.rxtec.pitchecking.picheckingservice.realsense.RSFaceDetectionService;
import com.rxtec.pitchecking.utils.BASE64Util;
import com.rxtec.pitchecking.utils.CommUtil;

/**
 * 由人脸检测进程调用（进程2） 铁科版人脸比对处理任务 用于铁科版本主控闸机程序
 * 
 * @author ZhaoLin
 *
 */
public class VerifyFaceTaskForTKVersion implements IVerifyFaceTask {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");

	IDCardReaderEvent event;
	IFaceTrackService faceTrackService = null;
	MqttSenderBroker mqttSender = null;
	GatCtrlSenderBroker gatCtrlSender = null;

	public VerifyFaceTaskForTKVersion() {
		if (Config.getInstance().getFaceControlMode() == 1) { // 由后置摄像头进程处理
			if (Config.getInstance().getVideoType() == Config.RealSenseVideo)
				faceTrackService = RSFaceDetectionService.getInstance();
			else
				faceTrackService = FaceDetectionService.getInstance();
		}
		/**
		 * 
		 */
		if (Config.getInstance().getFaceControlMode() == 1) { // 由后置摄像头进程处理
			mqttSender = MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum());
			gatCtrlSender = GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum());
		}
		if (Config.getInstance().getFaceControlMode() == 2) {
			mqttSender = MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT);
			gatCtrlSender = GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT);
		}
	}

	/**
	 * 
	 * @param idCard
	 * @param ticket
	 * @return
	 */
	public PITVerifyData beginCheckFace(IDCard idCard, Ticket ticket, int delaySeconds) {
		log.info("$$$$$$$$$$$$$$$开始人脸检测$$$$$$$$$$$$$$$$");
		int faceCheckTimeout = Config.getInstance().getFaceCheckDelayTime();
		int checkPassTimeout = Config.getInstance().getCheckDelayPassTime();

		FaceCheckingService.getInstance().setFailedFace(null); // 清掉之前的失败人脸
		FaceCheckingService.getInstance().clearPassFaceDataQueue();

		// 设置为处于人脸核验中
		DeviceConfig.getInstance().setAllowOpenSecondDoor(true);
		DeviceConfig.getInstance().setInTracking(true);

		PITVerifyData fd = null;

		// 发送mq消息-取走票证、走进通道...
		// gatCtrlSender.sendDoorCmd(ProcessUtil.createAudioJson(DeviceConfig.AudioTakeTicketFlag,
		// "FaceAudio"));

		if (Config.getInstance().getFaceControlMode() == 1) { // 由后置摄像头进程处理
			if (Config.getInstance().getDoorCountMode() == DeviceConfig.DOUBLEDOOR) {
				if (Config.getInstance().getBackScreenMode() == 1)
					FaceTrackingScreen.getInstance().offerEvent(new ScreenElementModifyEvent(1, ScreenCmdEnum.ShowBeginCheckFaceContent.getValue(), null, null, fd));
				if (Config.getInstance().getBackScreenMode() == 2)
					HighFaceTrackingScreen.getInstance().offerEvent(new ScreenElementModifyEvent(1, ScreenCmdEnum.ShowBeginCheckFaceContent.getValue(), null, null, fd));
			} else {
				SingleFaceTrackingScreen.getInstance().offerEvent(new ScreenElementModifyEvent(1, ScreenCmdEnum.ShowBeginCheckFaceContent.getValue(), null, null, fd));
			}
		}

		/**
		 * 小孩及老人的特殊处理
		 */
		if (idCard.getAge() <= Config.ByPassMinAge || idCard.getAge() >= Config.ByPassMaxAge || Config.getInstance().getIsVerifyFaceToCard() == 0) {
			fd = new PITVerifyData();
			fd.setIdNo(idCard.getIdNo());
			fd.setAge(idCard.getAge());
			fd.setPersonName(idCard.getPersonName());
			fd.setGender(idCard.getGender());
			fd.setIdBirth(idCard.getIDBirth());
			fd.setIdNation(idCard.getIDNation());
			fd.setIdDwelling(idCard.getIDDwelling());
			fd.setIdEfficb(idCard.getIDEfficb());
			fd.setIdEffice(idCard.getIDEffice());
			fd.setIdIssue(idCard.getIDIssue());
			
			fd.setIdCardImg(idCard.getCardImageBytes());
			fd.setFaceImg(idCard.getCardImageBytes());
			fd.setFrameImg(idCard.getCardImageBytes());
			fd.setVerifyResult(1);
			try {
				fd.setIdCardImgByBase64(BASE64Util.encryptBASE64(idCard.getCardImageBytes()));
				fd.setFaceImgByBase64(BASE64Util.encryptBASE64(idCard.getCardImageBytes()));
				fd.setFrameImgByBase64(BASE64Util.encryptBASE64(idCard.getCardImageBytes()));
			} catch (Exception ex) {
				log.error("老人或小孩:", ex);
			}
			log.info("老人或小孩Age=" + idCard.getAge() + "：PITVerifyData==" + fd);

			CommUtil.sleep(Config.getInstance().getSecondDoorWaitTime());

			// 向闸机主控程序发布比对结果
			mqttSender.publishResult(fd, Config.VerifyPassedStatus); // MQTT版本-比对结果发布

			DeviceConfig.getInstance().setInTracking(false); // 设置人脸核验已经完成

			this.sendFaceDataToMQServer(fd); // 将人脸数据传输至服务端

			return fd;
		}

		// 通知人脸检测线程开始人脸比对
		if (Config.getInstance().getFaceControlMode() == 1) { // 由后置摄像头进程处理
			faceTrackService.beginCheckingFace(idCard, ticket);
		}

		long nowMils = Calendar.getInstance().getTimeInMillis();

		try {
			/**
			 * 阻塞等待人脸比对线程或独立进程完成人脸比对 此处设置了超时等待时间
			 */
			fd = FaceCheckingService.getInstance().pollPassFaceData(faceCheckTimeout);
		} catch (InterruptedException ex) {
			log.error("pollPassFaceData call", ex);
		} catch (Exception ex) {
			log.error("pollPassFaceData call", ex);
		}

		// 如果返回结果为空，则代表人脸比对失败
		if (fd == null) {
			long usingTime = Calendar.getInstance().getTimeInMillis() - nowMils;
			log.info("pollPassFaceData, using " + usingTime + " value = null");
			log.info("验证超时失败，请从侧门离开通道");

			PITVerifyData failedFd = FaceCheckingService.getInstance().pollFailedFaceData();
			// PITVerifyData failedFd =
			// FaceCheckingService.getInstance().getFailedFace();
			log.info("验证超时,尝试从失败List中取出人脸 :" + failedFd);
			if (failedFd != null) {
				try {
					log.info("failedFd:IdCardImg.length = " + BASE64Util.decryptBASE64(failedFd.getIdCardImgByBase64()).length + ",faceImg.length = "
							+ BASE64Util.decryptBASE64(failedFd.getFaceImgByBase64()).length + ",frameImg.length = "
							+ BASE64Util.decryptBASE64(failedFd.getFrameImgByBase64()).length + " idNo = " + failedFd.getIdNo() + " verifyResult = "
							+ failedFd.getVerifyResult());

					if (failedFd.getIdCardImgByBase64() != null && failedFd.getIdCardImgByBase64().length() > 0) {
						mqttSender.publishResult(failedFd, Config.VerifyFailedStatus); // MQTT版本,比对结果发布,人脸比对失败！状态为1

						this.sendFaceDataToMQServer(failedFd);
					}
				} catch (Exception ex) {
					log.error("", ex);
				}
			} else {
				PITVerifyData pitData = new PITVerifyData();
				pitData.setIdNo(idCard.getIdNo());
				pitData.setAge(idCard.getAge());
				pitData.setPersonName(idCard.getPersonName());
				pitData.setGender(idCard.getGender());
				pitData.setIdBirth(idCard.getIDBirth());
				pitData.setIdNation(idCard.getIDNation());
				pitData.setIdDwelling(idCard.getIDDwelling());
				pitData.setIdEfficb(idCard.getIDEfficb());
				pitData.setIdEffice(idCard.getIDEffice());
				pitData.setIdIssue(idCard.getIDIssue());
				
				pitData.setIdCardImg(idCard.getCardImageBytes());
				pitData.setVerifyResult(0);
				pitData.setFaceImg("".getBytes());
				pitData.setFrameImg("".getBytes());

				// mqttSender.publishResult(pitData, Config.VerifyFailedStatus,
				// pitData.getIdCardImg(), pitData.getFaceImg(),
				// pitData.getFrameImg());
			}

			if (Config.getInstance().getFaceControlMode() == 1) { // 由后置摄像头进程处理
				faceTrackService.stopCheckingFace();
			}

			DeviceConfig.getInstance().setInTracking(false); // 设置人脸核验已经完成

		} else { // 验证成功

			long usingTime = Calendar.getInstance().getTimeInMillis() - nowMils;
			log.info("pollPassFaceData, using " + usingTime + " ms,fd.CameraPosition=" + fd.getCameraPosition() + ", value=" + fd.getVerifyResult());

			// byte[] passIdCardImg = fd.getIdCardImg();
			// byte[] passFaceImg = fd.getFaceImg();
			// byte[] passFrameImg = fd.getFrameImg();

			if (fd.getVerifyResult() >= -1) {
				log.info("验证成功，请通过");
				log.info("pollPassFaceData:" + fd);

				// 向闸机主控程序发布比对结果
				long totalTime = Config.getInstance().getSecondDoorWaitTime();
				if (usingTime < totalTime) {
					long tt = totalTime - usingTime;
					log.info("检脸过快，设置" + tt + "ms后再开门");
					CommUtil.sleep(tt);
				}

				mqttSender.publishResult(fd, Config.VerifyPassedStatus); // MQTT版本,比对结果发布,人脸比对成功,状态为0

				this.sendFaceDataToMQServer(fd); // 将人脸数据传输至服务端

				if (Config.getInstance().getFaceControlMode() == 1) { // 由后置摄像头进程处理
					faceTrackService.stopCheckingFace();
				}

				/*
				 * if (Config.getInstance().getFaceControlMode() == 1) { //
				 * 由后置摄像头进程处理 // 语音："验证成功，请通过" gatCtrlSender
				 * .sendDoorCmd(ProcessUtil.createAudioJson(DeviceConfig.
				 * AudioCheckSuccFlag, "FaceAudio")); // 通知人工控制台
				 * gatCtrlSender.sendMessage(DeviceConfig.EventTopic,
				 * DeviceConfig.Event_VerifyFaceSucc); }
				 */

				DeviceConfig.getInstance().setAllowOpenSecondDoor(true); // 允许重新开2门

				FaceCheckingService.getInstance().setLastIdCard(fd.getIdCard());
			} else {
				log.info("验证失败，请从侧门离开通道");
				log.info("pollPassFaceData:" + fd);
				fd.setVerifyResult((float) 0.01);

				mqttSender.publishResult(fd, Config.VerifyFailedStatus); // MQTT版本,比对结果发布,人脸比对成功,状态为0

				this.sendFaceDataToMQServer(fd); // 将人脸数据传输至服务端

				if (Config.getInstance().getFaceControlMode() == 1) { // 由后置摄像头进程处理
					faceTrackService.stopCheckingFace();
				}

				/*
				 * if (Config.getInstance().getFaceControlMode() == 1) { //
				 * 由后置摄像头进程处理 // 语音："验证失败，请从侧门离开通道" gatCtrlSender
				 * .sendDoorCmd(ProcessUtil.createAudioJson(DeviceConfig.
				 * AudioCheckFailedFlag, "FaceAudio")); // 通知人工控制台
				 * gatCtrlSender.sendMessage(DeviceConfig.EventTopic,
				 * DeviceConfig.Event_VerifyFaceFailed); }
				 */

				DeviceConfig.getInstance().setAllowOpenSecondDoor(true); // 允许重新开2门
			}

			DeviceConfig.getInstance().setInTracking(false); // 设置人脸核验已经完成
		}

		return fd;
	}

	/**
	 * 将人脸数据传输至服务端 3张照片数组单独作为入参
	 * 
	 * @param faceData
	 */
	private void sendFaceDataToMQServer(PITVerifyData faceData, byte[] idCardImg, byte[] faceImg, byte[] frameImg) {
		if (Config.getInstance().getIsUsePoliceMQ() == 1) { // 将人脸数据传输至公安处
			// PITInfoPolicePublisher.getInstance().offerVerifyData(faceData,
			// idCardImg, faceImg, frameImg);
			PITInfoPolicePublisher.getInstance().offerVerifyData(faceData);
		}

		if (Config.getInstance().getIsUseThirdMQ() == 1) { // 将人脸数据传输至第三方
			// PITInfoQuickPublisher.getInstance().offerVerifyData(faceData,
			// idCardImg, faceImg, frameImg);
			PITInfoQuickPublisher.getInstance().offerVerifyData(faceData);
		}

		if (Config.getInstance().getIsUseWharfMQ() == 1) { // 将人脸数据传输至桂林码头
			int msgType = 2;
			String ipAddress = faceData.getGateIp();
			String pitDate = faceData.getPitDate();
			String pitTime = faceData.getPitTime();
			float verifyResult = faceData.getVerifyResult();
			int isVerifyPassed = 0;
			IDCard idCard = faceData.getIdCard();

			PITInfoWharfPublisher.getInstance().offerVerifyData(msgType, ipAddress, pitDate, pitTime, verifyResult, isVerifyPassed, idCard, idCardImg, faceImg, frameImg);
		}
	}

	/**
	 * 
	 * @param faceData
	 * @param idCardImg
	 * @param faceImg
	 * @param frameImg
	 */
	private void sendFaceDataToMQServer(PITVerifyData faceData) {
		try {
			if (Config.getInstance().getIsUsePoliceMQ() == 1) { // 将人脸数据传输至公安处
				PITInfoPolicePublisher.getInstance().offerVerifyData(faceData);
			}

			if (Config.getInstance().getIsUseThirdMQ() == 1) { // 将人脸数据传输至第三方
				PITInfoQuickPublisher.getInstance().offerVerifyData(faceData);
			}

			if (Config.getInstance().getIsUseWharfMQ() == 1) { // 将人脸数据传输至桂林码头
				int msgType = 2;
				String ipAddress = faceData.getGateIp();
				String pitDate = faceData.getPitDate();
				String pitTime = faceData.getPitTime();
				float verifyResult = faceData.getVerifyResult();
				int isVerifyPassed = 1;
				if (verifyResult >= Config.getInstance().getFaceCheckThreshold()) {
					isVerifyPassed = 0;
				}
				IDCard idCard = faceData.getIdCard();

				PITInfoWharfPublisher.getInstance().offerVerifyData(msgType, ipAddress, pitDate, pitTime, verifyResult, isVerifyPassed, idCard,
						faceData.getIdCardImgByBase64(), faceData.getFaceImgByBase64(), faceData.getFrameImgByBase64());
			}
		} catch (Exception ex) {
			log.error("sendFaceDataToMQServer:", ex);
		}
	}

}
