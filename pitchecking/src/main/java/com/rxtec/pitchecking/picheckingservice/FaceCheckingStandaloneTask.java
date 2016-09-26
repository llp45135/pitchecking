package com.rxtec.pitchecking.picheckingservice;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.db.PitRecordLoger;
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
		}
	}

	@Override
	public void run() {
		
		if(initDllStatus <1){
			log.error("!!!!!!!!!!!!!!!!!!VerifyDLL init failed!!!!!!!!!!!!!!!");
			return;
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
					if (fd.getAge() <= Config.ByPassMinAge || fd.getAge() >= Config.ByPassMaxAge) {
						CommUtil.sleep(2000);
						fd.setVerifyResult(0.8f);
						publisher.publishResult(fd); // 比对结果公布
						FaceCheckingService.getInstance().resetFaceDataQueue();
						FaceImageLog.saveFaceDataToDsk(fd);
						continue;
					}
					resultValue = faceVerify.verify(extractFaceImageBytes, fd.getIdCardImg());// 比对人脸
					fd.setVerifyResult(resultValue);
					int usingTime = (int) (Calendar.getInstance().getTimeInMillis() - nowMils);

//					if (resultValue >= Config.getInstance().getFaceCheckThreshold()) {
//						publisher.publishResult(fd); // 比对结果公布
//						FaceCheckingService.getInstance().resetFaceDataQueue();
//					}
					
					log.info("Face verify result="+resultValue);
					publisher.publishResult(fd); // 比对结果公布
					FaceCheckingService.getInstance().resetFaceDataQueue();
					
//					PitRecordLoger.getInstance().offer(fd);
//					FaceVerifyServiceStatistics.getInstance().update(resultValue, usingTime, fd.getFaceDistance());
					FaceImageLog.saveFaceDataToDsk(fd);
				}

			} catch (Exception e) {
				log.error("FaceCheckingStandaloneTask run loop", e);
			}

		}
	}

}
