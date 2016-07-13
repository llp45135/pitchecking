package com.rxtec.pitchecking.picheckingservice;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.net.PTVerifyResultPublisher;

public class FaceCheckingStandaloneTask implements Runnable {

	
	PTVerifyResultPublisher publisher = PTVerifyResultPublisher.getInstance();
	FaceVerifyJniEntry faceVerify = null;
	private Logger log = LoggerFactory.getLogger("FaceCheckingStandaloneTask");

	public FaceCheckingStandaloneTask(String DLLName){
		faceVerify = new FaceVerifyJniEntry(DLLName);
	}

	
	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(50);
				FaceVerifyData fd = FaceCheckingService.getInstance().takeFaceVerifyData();//从待验证人脸队列中取出人脸对象
				if (fd != null) {
					if (fd.getIdCardImg() == null)
						continue;
					long nowMils = Calendar.getInstance().getTimeInMillis();
					float resultValue = 0;
					byte[] extractFaceImageBytes = fd.getFaceImg();
					if (extractFaceImageBytes == null)
						continue;
					resultValue = faceVerify.verify(extractFaceImageBytes, fd.getIdCardImg());//比对人脸
					fd.setVerifyResult(resultValue);
					long usingTime = Calendar.getInstance().getTimeInMillis() - nowMils;
					if (resultValue >= Config.getInstance().getFaceCheckThreshold()) {  
						publisher.publishResult(fd);  //比对结果公布
						FaceCheckingService.getInstance().resetFaceDataQueue();
					} 
					FaceImageLog.saveFaceDataToDsk(fd);
				}

			} catch (InterruptedException e) {
				log.error("InterruptedException",e);
			}

		}
	}

}
