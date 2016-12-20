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
import com.rxtec.pitchecking.net.PTVerifyResultPublisher;
import com.rxtec.pitchecking.utils.CommUtil;

public class FaceCheckingStandaloneTask implements Runnable {

	PTVerifyResultPublisher publisher = PTVerifyResultPublisher.getInstance();
	FaceVerifyInterface faceVerify = null;
	private Logger log = LoggerFactory.getLogger("FaceCheckingStandaloneTask");

	private int initDllStatus = 0;

	public FaceCheckingStandaloneTask() {
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
			log.error("!!!!!!!!!!!!!!!!!!VerifyDLL init failed!!!!!!!!!!!!!!!");
			return;
		} else {
			FaceCheckingService.getInstance().setFaceVerify(faceVerify);
		}
		while (true) {

			try {
				Thread.sleep(50);
				PITVerifyData fd = FaceCheckingService.getInstance().takeFaceVerifyData();// 从待验证人脸队列中取出人脸对象
				if (fd != null) {
					if (fd.getIdCardImg() == null)
						continue;
					long nowMils = Calendar.getInstance().getTimeInMillis();
					float resultValue = 0;
					byte[] extractFaceImageBytes = fd.getFaceImg();
					if (extractFaceImageBytes == null)
						continue;

					fd.setGateIp(DeviceConfig.getInstance().getIpAddress());
					if (fd.getAge() <= Config.ByPassMinAge || fd.getAge() >= Config.ByPassMaxAge) {
						CommUtil.sleep(2000);
						fd.setVerifyResult(0.8f);
						publisher.publishResult(fd); // 比对结果公布
						FaceCheckingService.getInstance().resetFaceDataQueue();
						FaceImageLog.saveFaceDataToDsk(fd);
						continue;
					}
					if (Config.getInstance().getFaceVerifyType().equals(Config.FaceVerifyEASEN)) {
						resultValue = faceVerify.verify(fd.getFrameImg());
					} else {
						resultValue = faceVerify.verify(extractFaceImageBytes, fd.getIdCardImg());// 比对人脸
					}
					fd.setVerifyResult(resultValue);
					int usingTime = (int) (Calendar.getInstance().getTimeInMillis() - nowMils);

					if (resultValue >= Config.getInstance().getFaceCheckThreshold()) {
						FaceCheckingService.getInstance().resetFaceDataQueue();
					}
					publisher.publishResult(fd); // 比对结果公布

					if (Config.getInstance().getIsUseManualMQ() == 1) { // 将人脸数据传输至人工验证台
						log.info("Face verify result=" + resultValue);
						RemoteMonitorPublisher.getInstance().offerVerifyData(fd);
					}

					if (Config.getInstance().getIsUsePoliceMQ() == 1) { // 将人脸数据传输至公安处
						log.info("Face verify result=" + resultValue);
						// PITInfoPolicePublisher.getInstance().offerFrameData(fd.getFrameImg());
						PITInfoPolicePublisher.getInstance().offerVerifyData(fd);
					}

					// offer进mongodb的处理队列
					// if (Config.getInstance().getIsUseMongoDB() == 1) {
					// PitRecordLoger.getInstance().offer(fd);
					// FaceVerifyServiceStatistics.getInstance().update(resultValue,
					// usingTime, fd.getFaceDistance());
					// }
					FaceImageLog.saveFaceDataToDskAndMongoDB(fd, usingTime);
				}

			} catch (Exception e) {
				log.error("FaceCheckingStandaloneTask run loop", e);
			}

		}
	}

}
