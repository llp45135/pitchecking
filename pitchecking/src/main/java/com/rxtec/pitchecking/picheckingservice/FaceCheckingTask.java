package com.rxtec.pitchecking.picheckingservice;

import java.io.IOException;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.domain.FailedFace;

public class FaceCheckingTask implements Runnable {

	FaceVerifyInterface faceVerify = null;
	private Logger log = LoggerFactory.getLogger("FaceCheckingTask");

	public FaceCheckingTask(){
		if(Config.getInstance().getFaceVerifyType().equals(Config.FaceVerifyPIXEL)){
			faceVerify = new PIXELFaceVerifyJniEntry(Config.PIXELFaceVerifyDLLName);
		}else if(Config.getInstance().getFaceVerifyType().equals(Config.FaceVerifyMicro)){
			faceVerify = new PIXELFaceVerifyJniEntry(Config.MICROFaceVerifyCloneDLLName);
		}
		
	}
	
	
	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(50);
				PITData fd = FaceCheckingService.getInstance().takeDetectedFaceData();//从待验证人脸队列中取出人脸对象
				
				if (fd != null) {
					IDCard idCard = fd.getIdCard();
					if (idCard == null)
						continue;
					long nowMils = Calendar.getInstance().getTimeInMillis();
					float resultValue = 0;
					byte[] extractFaceImageBytes = fd.getExtractFaceImageBytes();
					if (extractFaceImageBytes == null)
						continue;
					resultValue = faceVerify.verify(extractFaceImageBytes, fd.getIdCard().getCardImageBytes());//比对人脸
					fd.setFaceCheckResult(resultValue);
					long usingTime = Calendar.getInstance().getTimeInMillis() - nowMils;
					if (resultValue >= Config.getInstance().getFaceCheckThreshold()) {
						FaceCheckingService.getInstance().offerPassFaceData(fd);
					} 
					//将人脸图像存盘
					fd.saveFaceDataToDsk();
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

}
