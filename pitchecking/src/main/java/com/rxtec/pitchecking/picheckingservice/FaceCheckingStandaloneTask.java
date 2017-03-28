package com.rxtec.pitchecking.picheckingservice;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.db.PitRecordLoger;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.device.easen.EasenVerifyResult;
import com.rxtec.pitchecking.mq.RemoteMonitorPublisher;
import com.rxtec.pitchecking.mq.police.PITInfoPolicePublisher;
import com.rxtec.pitchecking.mqtt.GatCtrlSenderBroker;
import com.rxtec.pitchecking.mqtt.pitevent.PTVerifyResultSender;
import com.rxtec.pitchecking.net.PTVerifyResultPublisher;
import com.rxtec.pitchecking.utils.CommUtil;

public class FaceCheckingStandaloneTask implements Runnable {

	// PTVerifyResultPublisher publisher =
	// PTVerifyResultPublisher.getInstance(); // 启动人脸比对结果向外发布
	// PTVerifyResultSender publisher =
	// PTVerifyResultSender.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT);
	Object publisher = null;

	FaceVerifyInterface faceVerify = null;
	private Logger log = LoggerFactory.getLogger("FaceCheckingStandaloneTask");

	private int initDllStatus = 0;

	public FaceCheckingStandaloneTask() {
		if (Config.getInstance().getTransferFaceMode() == 1) {
			publisher = PTVerifyResultPublisher.getInstance(); // 启动人脸比对结果向外发布
		}
		if (Config.getInstance().getTransferFaceMode() == 2) {
			publisher = PTVerifyResultSender.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT);
		}
		if (Config.getInstance().getFaceVerifyType().equals(Config.FaceVerifyPIXEL)) {
			faceVerify = new PIXELFaceVerifyJniEntry(Config.PIXELFaceVerifyDLLName);
			initDllStatus = faceVerify.getInitStatus();
		} else if (Config.getInstance().getFaceVerifyType().equals(Config.FaceVerifyMicro)) {
			faceVerify = new MICROPFaceVerifyJNIEntry(Config.MICROFaceVerifyCloneDLLName);
			initDllStatus = faceVerify.getInitStatus();
		} else if (Config.getInstance().getFaceVerifyType().equals(Config.FaceVerifyEASEN)) {
			faceVerify = new EASENFaceVerifyJNAEntry();
			initDllStatus = faceVerify.getInitStatus();
		}
	}

	@Override
	public void run() {

		if (initDllStatus < 1) {
			log.error("!!!!!!!!!VerifyDLL init failed!!!!!!未启动循环");
			return;
		} else {
			FaceCheckingService.getInstance().setFaceVerify(faceVerify);
		}
		log.info("begin going while loop...");
		while (true) {
			try {
				CommUtil.sleep(20);

				PITVerifyData fd = FaceCheckingService.getInstance().takeFaceVerifyData();// 从待验证人脸队列中取出人脸对象
				if (DeviceConfig.getInstance().isInTracking()) {
					if (fd != null) {
						if (fd.getIdCardImg() == null) {
							// log.debug("没有检测到身份证照片，不比对人脸,fd==" + fd);
							continue;
						}

						// if (FaceCheckingService.getInstance().getIdcard() ==
						// null) {
						// // log.debug("没有检测到身份证照片，不比对人脸,fd==" + fd);
						// continue;
						// } else {
						// if (fd.getIdCardImg() == null) {
						// fd.setIdCard(FaceCheckingService.getInstance().getIdcard());
						// fd.setIdNo(FaceCheckingService.getInstance().getIdcard().getIdNo());
						// fd.setAge(FaceCheckingService.getInstance().getIdcard().getAge());
						// fd.setIdCardImg(FaceCheckingService.getInstance().getIdcard().getCardImageBytes());
						// }
						// }

						if (!FaceCheckingService.getInstance().isSendFrontCameraFace() && fd.getCameraPosition() == 1) {
							log.debug("已经停止比对前置摄像头人脸,fd.getCameraPosition==" + fd.getCameraPosition());
							continue;
						}

						long nowMils = Calendar.getInstance().getTimeInMillis();
						float resultValue = 0;
						byte[] extractFaceImageBytes = fd.getFaceImg();
						if (extractFaceImageBytes == null)
							continue;

						fd.setGateIp(DeviceConfig.getInstance().getIpAddress());
						if (fd.getAge() <= Config.ByPassMinAge || fd.getAge() >= Config.ByPassMaxAge) {
							CommUtil.sleep(2000);
							fd.setVerifyResult(0.8f);
							if (Config.getInstance().getTransferFaceMode() == 1) {
								((PTVerifyResultPublisher) publisher).publishResult(fd); // 比对结果公布
							}
							if (Config.getInstance().getTransferFaceMode() == 2) {
								((PTVerifyResultSender) publisher).publishResult(fd); // 比对结果公布
							}
							FaceCheckingService.getInstance().resetFaceDataQueue();
							// FaceImageLog.saveFaceDataToDskByTK(fd);
							FaceCheckingService.getInstance().setDealNoneTrackFace(true);
							continue;
						}
						if (Config.getInstance().getFaceVerifyType().equals(Config.FaceVerifyEASEN)) { // 易胜sdk比对人脸
							if (FaceCheckingService.getInstance().getIdCardPhotoRet() != 0) {
								continue;
							}
							log.info("证照齐全,开始比对人脸...,fd.cameraPosition==" + fd.getCameraPosition());
							resultValue = faceVerify.verify(fd.getFaceImg());
						} else {
							log.info("证照齐全,开始比对人脸...,fd.cameraPosition==" + fd.getCameraPosition());
							resultValue = faceVerify.verify(extractFaceImageBytes, fd.getIdCardImg());// 比对人脸
						}
						log.info("Face verify result=" + resultValue);
						fd.setVerifyResult(resultValue);

						if (resultValue > 0) {
							if (Config.getInstance().getTransferFaceMode() == 1) {
								((PTVerifyResultPublisher) publisher).publishResult(fd); // 比对结果公布
							}
							if (Config.getInstance().getTransferFaceMode() == 2) {
								((PTVerifyResultSender) publisher).publishResult(fd); // 比对结果公布
							}
						}

						int usingTime = (int) (Calendar.getInstance().getTimeInMillis() - nowMils);

						if (resultValue >= Config.getInstance().getFaceCheckThreshold()) {
							FaceCheckingService.getInstance().resetFaceDataQueue();
							FaceCheckingService.getInstance().setDealNoneTrackFace(true);
						}

						if (Config.getInstance().getIsUseManualMQ() == 1) { // 将人脸数据传输至人工验证台
							RemoteMonitorPublisher.getInstance().offerVerifyData(fd);
						}

						if (Config.getInstance().getIsUsePoliceMQ() == 1) { // 将人脸数据传输至公安处
							// PITInfoPolicePublisher.getInstance().offerFrameData(fd.getFrameImg());
							PITInfoPolicePublisher.getInstance().offerVerifyData(fd);
						}

						// offer进mongodb的处理队列
						// if (Config.getInstance().getIsUseMongoDB() == 1) {
						// PitRecordLoger.getInstance().offer(fd);
						// FaceVerifyServiceStatistics.getInstance().update(resultValue,
						// usingTime, fd.getFaceDistance());
						// }

						// FaceImageLog.saveFaceDataToDskAndMongoDB(fd,
						// usingTime);
						// log.info("getIsSavePhotoByTK=="+Config.getInstance().getIsSavePhotoByTK()+",resultValue=="+resultValue);
						// log.info("getIdNo=="+fd.getIdNo()+",getIdCardImg=="+fd.getIdCardImg());
						if (Config.getInstance().getIsSavePhotoByTK() == 1) { // 按铁科格式存储照片
							if (fd.getIdNo() != null && fd.getIdCardImg() != null
									&& resultValue >= Config.getInstance().getFaceCheckThreshold()) {
								log.info("准备存储照片saveFaceDataToDskByTK");
								FaceImageLog.saveFaceDataToDskByTK(fd);
							}
						}

						FaceImageLog.saveFaceDataToMySQL(fd, usingTime);
					}
				} else {
					if (FaceCheckingService.getInstance().isDealNoneTrackFace()) {
						if (fd != null) {
							if (fd.getIdCardImg() == null) {
								log.info("没有检测到身份证照片，不比对人脸,fd==" + fd);
								continue;
							}
							float resultValue = 0;
							byte[] extractFaceImageBytes = fd.getFaceImg();
							if (extractFaceImageBytes == null)
								continue;

							if (fd.getCameraFaceMode() == 1) {
								log.info("已经停止比对检脸状态下的人脸,fd.getCameraFaceMode==" + fd.getCameraFaceMode());
								continue;
							}
							
							fd.setGateIp(DeviceConfig.getInstance().getIpAddress());

							if (Config.getInstance().getFaceVerifyType().equals(Config.FaceVerifyEASEN)) { // 易胜sdk比对人脸
								log.info("非检脸状态，证照齐全,开始比对人脸...,fd.cameraPosition==" + fd.getCameraPosition());
								resultValue = faceVerify.verify(fd.getFaceImg());
							} else {
								log.info("非检脸状态，证照齐全,开始比对人脸...,fd.cameraPosition==" + fd.getCameraPosition());
								resultValue = faceVerify.verify(extractFaceImageBytes, fd.getIdCardImg());// 比对人脸
							}
							log.info("Face verify result=" + resultValue);
							fd.setVerifyResult(resultValue);

							if (resultValue >= Config.getInstance().getFaceCheckThreshold()) {
								FaceCheckingService.getInstance().setDealNoneTrackFace(false);
								FaceCheckingService.getInstance().resetFaceDataQueue();
								log.info("非检脸状态下，再次比对通过，开门！");
								GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT)
										.sendDoorCmd("PITEventTopic", DeviceConfig.OPEN_SECONDDOOR);
								DeviceConfig.getInstance().setAllowOpenSecondDoor(false);
								GatCtrlSenderBroker
										.getInstance(
												DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum())
										.sendMessage(DeviceConfig.EventTopic,
												DeviceConfig.Event_LetSend_FrontCameraFace);
							}
						}
					}
				}
			} catch (Exception e) {
				log.error("FaceCheckingStandaloneTask run loop", e);
			}

		}
	}

}
