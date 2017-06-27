package com.rxtec.pitchecking.picheckingservice;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.db.PitRecordLoger;
import com.rxtec.pitchecking.device.BarUnsecurity;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.device.easen.EasenVerifyResult;
import com.rxtec.pitchecking.mbean.ProcessUtil;
import com.rxtec.pitchecking.mq.RemoteMonitorPublisher;
import com.rxtec.pitchecking.mq.police.PITInfoPolicePublisher;
import com.rxtec.pitchecking.mq.quickhigh.PITInfoQuickPublisher;
import com.rxtec.pitchecking.mqtt.GatCtrlSenderBroker;
import com.rxtec.pitchecking.mqtt.pitevent.PTVerifyResultSender;
import com.rxtec.pitchecking.net.PTVerifyResultPublisher;
import com.rxtec.pitchecking.socket.pitevent.FaceResultSender;
import com.rxtec.pitchecking.utils.BASE64Util;
import com.rxtec.pitchecking.utils.CalUtils;
import com.rxtec.pitchecking.utils.CommUtil;

public class FaceCheckingStandaloneTask implements Runnable {

	private Object publisher = null;

	FaceVerifyInterface faceVerify = null;
	private Logger log = LoggerFactory.getLogger("FaceCheckingStandaloneTask");

	private int initDllStatus = 0;

	// private Map<String, PITVerifyData> facePassMap = new HashMap<String,
	// PITVerifyData>();
	private PITVerifyData succeedFd = null;
	// private byte[] succeedFaceImg = null;
	// private byte[] succeedIdCardImg = null;
	// private byte[] succeedFrameImg = null;

	public FaceCheckingStandaloneTask() {

		if (Config.getInstance().getFaceControlMode() == 1) {
			if (Config.getInstance().getTransferFaceMode() == Config.TransferFaceByAeron) {
				publisher = PTVerifyResultPublisher.getInstance(); // 启动人脸比对结果向外发布
			}
			if (Config.getInstance().getTransferFaceMode() == Config.TransferFaceByMqtt) {
				publisher = PTVerifyResultSender.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT);
			}
			if (Config.getInstance().getTransferFaceMode() == Config.TransferFaceBySocket) {
				publisher = FaceResultSender.getInstance(); // 启动人脸比对结果向外发布
			}
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
		log.info("当前软件版本号:" + DeviceConfig.softVersion);
		if (initDllStatus < 1) {
			log.error("!!!!!!!!!VerifyDLL init failed!!!!!!未启动循环");
			return;
		} else {
			FaceCheckingService.getInstance().setFaceVerify(faceVerify);
		}
		log.info("开始进入人脸比对启动循环线程...");
		while (true) {
			try {
				CommUtil.sleep(50);

				// PITVerifyData fd =
				// FaceCheckingService.getInstance().takeFaceVerifyData();//
				// 从待验证人脸队列中取出人脸对象

				if (DeviceConfig.getInstance().isInTracking()) { // 正常检脸状态

					PITVerifyData fd = null;
					if (FaceCheckingService.getInstance().getIdcard() != null) {
						fd = FaceCheckingService.getInstance().takeFaceVerifyData();// 从待验证人脸队列中取出人脸对象
					}
					if (!FaceCheckingService.getInstance().isVerifyFace()) {
						continue;
					}
					if (fd != null) {
						if (fd.getIdCardImg() == null) {
							log.info("没有检测到身份证照片，不比对人脸,fd==" + fd);
							continue;
						}

						if (!FaceCheckingService.getInstance().isSendFrontCameraFace() && fd.getCameraPosition() == 1) {
							log.info("已经停止比对前置摄像头人脸,fd.getCameraPosition==" + fd.getCameraPosition());
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

							if (Config.getInstance().getFaceControlMode() == 1) {
								if (Config.getInstance().getTransferFaceMode() == Config.TransferFaceByAeron) {
									((PTVerifyResultPublisher) publisher).publishResult(fd); // 比对结果公布
								}
								if (Config.getInstance().getTransferFaceMode() == Config.TransferFaceByMqtt) {
									((PTVerifyResultSender) publisher).publishResult(fd); // 比对结果公布
								}
								if (Config.getInstance().getTransferFaceMode() == Config.TransferFaceBySocket) {
									((FaceResultSender) publisher).publishResult(fd); // 比对结果公布
								}
							}

							if (Config.getInstance().getFaceControlMode() == 2) {
								if (fd.getVerifyResult() >= Config.getInstance().getFaceCheckThreshold()) {
									// 比对成功的offer到成功队列
									log.info("比对成功的offer到成功队列!verifyResult = " + fd.getVerifyResult());
									FaceCheckingService.getInstance().offerPassFaceData(fd);
								} else {
									// 比对失败的offer到失败队列
									log.info("比对失败的offer到失败队列!verifyResult = " + fd.getVerifyResult());
									FaceCheckingService.getInstance().offerFailedFaceData(fd);
									// FaceCheckingService.getInstance().putFailedFace(fd);
								}
							}

							FaceCheckingService.getInstance().resetFaceDataQueue();
							FaceCheckingService.getInstance().setDealNoneTrackFace(false);
							// FaceImageLog.saveFaceDataToDskByTK(fd);
							continue;
						}

						// PITVerifyData lastFd = facePassMap.get(fd.getIdNo());

						/**
						 * 已经有过成功的比对，不需要再次比对，直接获取成功结果
						 */
						if (succeedFd != null && succeedFd.getIdNo().equals(fd.getIdNo())) {
							// succeedFd.setIdCardImg(succeedIdCardImg);
							// succeedFd.setFaceImg(succeedFaceImg);
							// succeedFd.setFrameImg(succeedFrameImg);

							resultValue = succeedFd.getVerifyResult();
							log.info(succeedFd.getIdNo() + "--已经有过成功的比对，不需要再次比对，直接获取成功结果");
							log.info("Map中的人脸实例:idCardImg.length = " + BASE64Util.decryptBASE64(succeedFd.getIdCardImgByBase64()).length + ",faceImg.length = "
									+ BASE64Util.decryptBASE64(succeedFd.getFaceImgByBase64()).length + ",frameImg.length = "
									+ BASE64Util.decryptBASE64(succeedFd.getFrameImgByBase64()).length);
							fd = succeedFd;
							// fd.setIdCardImg(succeedIdCardImg);
							// fd.setFaceImg(succeedFaceImg);
							// fd.setFrameImg(succeedFrameImg);
							log.info("fd.idCardImg.length = " + BASE64Util.decryptBASE64(fd.getIdCardImgByBase64()).length + ",faceImg.length = "
									+ BASE64Util.decryptBASE64(fd.getFaceImgByBase64()).length + ",frameImg.length = "
									+ BASE64Util.decryptBASE64(fd.getFrameImgByBase64()).length);

							CommUtil.sleep(100);
						} else {
							// if (!facePassMap.isEmpty()) {
							// facePassMap.clear();
							// log.info("新的身份证人脸比对，首先清除facePassMap");
							// }

							succeedFd = null;
							// succeedFaceImg = null;
							// succeedIdCardImg = null;
							// succeedFrameImg = null;

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
						}

						log.info(fd.getIdNo() + "--人脸比对结果 = " + resultValue);
						fd.setVerifyResult(resultValue);

						/**
						 * 将成功的暂存至map
						 */
						if (Config.getInstance().getIsVerifyFaceOnce() == 1 && resultValue >= Config.getInstance().getFaceCheckThreshold() && fd.getFaceImg().length > 0
								&& fd.getFrameImg().length > 0) {
							// facePassMap.put(fd.getIdNo(), fd);
							succeedFd = fd;
							// succeedFaceImg = fd.getFaceImg();
							// succeedIdCardImg = fd.getIdCardImg();
							// succeedFrameImg = fd.getFrameImg();
							log.info("将成功的暂存至map:idCardImg.length = " + succeedFd.getIdCardImg().length + ",faceImg.length = " + succeedFd.getFaceImg().length
									+ ",frameImg.length = " + succeedFd.getFrameImg().length);
						}

						Ticket ticket = null;
						String ticketQRCode = DeviceConfig.getInstance().getQrCode();
						if (ticketQRCode != null && ticketQRCode.trim().length() == 144) {
							ticket = BarUnsecurity.getInstance()
									.buildTicket(BarUnsecurity.getInstance().uncompress(ticketQRCode, CalUtils.getStringDateShort2().substring(0, 4)));
							log.info("ticket.CardNo==" + ticket.getCardNo());
							log.info("ticket.PassengerName==" + ticket.getPassengerName());
						}
						if (ticket != null) {
							if (fd.getIdNo().equals(ticket.getCardNo())) {
								fd.setQrCode(DeviceConfig.getInstance().getQrCode()); // 票面二维码
								log.info("票证一致!fd.qrCode = " + fd.getQrCode());
							} else {
								fd.setQrCode("");
								log.info("票证不一致!fd.qrCode = " + fd.getQrCode());
							}
						} else {
							fd.setQrCode("");
							log.info("未读取到有效二维码!fd.qrCode = " + fd.getQrCode());
						}

						if (resultValue > 0) {
							if (Config.getInstance().getFaceControlMode() == 1) { // 由后置摄像头进程处理
								if (Config.getInstance().getTransferFaceMode() == Config.TransferFaceByAeron) {
									((PTVerifyResultPublisher) publisher).publishResult(fd); // 比对结果公布
								}
								if (Config.getInstance().getTransferFaceMode() == Config.TransferFaceByMqtt) {
									((PTVerifyResultSender) publisher).publishResult(fd); // 比对结果公布
								}
								if (Config.getInstance().getTransferFaceMode() == Config.TransferFaceBySocket) {
									((FaceResultSender) publisher).publishResult(fd); // 比对结果公布
								}
							}

							if (Config.getInstance().getFaceControlMode() == 2) {
								if (fd.getVerifyResult() >= Config.getInstance().getFaceCheckThreshold()) {
									// 比对成功的offer到成功队列
									log.info("比对成功的offer到成功队列!verifyResult = " + fd.getVerifyResult());
									if (FaceCheckingService.getInstance().offerPassFaceData(fd))
										FaceCheckingService.getInstance().setVerifyFace(false);
								} else {
									// 比对失败的offer到失败队列
									log.info("比对失败的offer到失败队列!fd = " + fd);
									FaceCheckingService.getInstance().offerFailedFaceData(fd);
									// FaceCheckingService.getInstance().putFailedFace(fd);
								}
							}
						}

						int usingTime = (int) (Calendar.getInstance().getTimeInMillis() - nowMils);

						if (resultValue >= Config.getInstance().getFaceCheckThreshold()) {
							FaceCheckingService.getInstance().resetFaceDataQueue();
							FaceCheckingService.getInstance().setDealNoneTrackFace(false);
						}

						if (Config.getInstance().getIsUseManualMQ() == 1) { // 将人脸数据传输至人工验证台
							RemoteMonitorPublisher.getInstance().offerVerifyData(fd);
						}

						// if (Config.getInstance().getIsUsePoliceMQ() == 1) {
						// // 将人脸数据传输至公安处
						// float kk =
						// Config.getInstance().getFaceCheckThreshold() *
						// (float) 1;
						// if (resultValue >= kk) {
						// PITInfoPolicePublisher.getInstance().offerVerifyData(fd);
						// } else {
						//
						// }
						// }
						//
						// if (Config.getInstance().getIsUseThirdMQ() == 1) { //
						// 将人脸数据传输至第三方
						// float kk =
						// Config.getInstance().getFaceCheckThreshold() *
						// (float) 1;
						// if (resultValue >= kk) {
						// PITInfoQuickPublisher.getInstance().offerVerifyData(fd);
						// }
						// }

						FaceImageLog.saveFaceDataToDskAndMongoDB(fd, usingTime); // 将数据照片保存到硬盘
						// log.info("getIsSavePhotoByTK=="+Config.getInstance().getIsSavePhotoByTK()+",resultValue=="+resultValue);
						// log.info("getIdNo=="+fd.getIdNo()+",getIdCardImg=="+fd.getIdCardImg());

						if (Config.getInstance().getIsSavePhotoByTK() == 1) { // 按铁科格式存储照片
							if (fd.getIdNo() != null && fd.getIdCardImg() != null && fd.getFaceImg() != null && fd.getFrameImg() != null
									&& resultValue >= Config.getInstance().getFaceCheckThreshold()) {
								log.info("准备存储照片saveFaceDataToDskByTK");
								FaceImageLog.saveFaceDataToDskByTK(fd);
							}
						}

						/**
						 * 存入本地mysql数据库
						 */
						FaceImageLog.saveFaceDataToMySQL(fd, usingTime);
					}
				} else { // 非检脸状态
					if (Config.getInstance().getDoorCountMode() == 2 && FaceCheckingService.getInstance().isDealNoneTrackFace()) {
						PITVerifyData fd = FaceCheckingService.getInstance().takeFaceVerifyData();// 从待验证人脸队列中取出人脸对象

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

								GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT).sendMessage(DeviceConfig.EventTopic,
										DeviceConfig.Event_LetSend_FrontCameraFace);

								CommUtil.sleep(300);
								GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT).sendDoorCmd("PITEventTopic", DeviceConfig.OPEN_SECONDDOOR);

								GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT)
										.sendDoorCmd(ProcessUtil.createTkEventJson(DeviceConfig.AudioCheckSuccFlag, "FaceAudio"));

								FaceCheckingService.getInstance().setSendFrontCameraFace(true);
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
