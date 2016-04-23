package com.rxtec.pitchecking.picheckingservice;

import java.io.IOException;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;

public class FaceCheckerTask implements Runnable {

	FaceVerifyJniEntry faceVerify = new FaceVerifyJniEntry();
	private Logger log = LoggerFactory.getLogger("FaceChecker");

	@Override
	public void run() {

		while (true) {
			try {
				Thread.sleep(50);
				FaceData fd = FaceCheckingService.getInstance().takeDetectedFaceData();
				IDCard idCard = FaceDetectionService.getInstance().getCurrentIDCard();
				if (fd == null || idCard == null) continue;
				fd.setIdCard(idCard);
				long nowMils = Calendar.getInstance().getTimeInMillis();
				float resultValue = 0;

				if (fd != null) {
					byte[] extractFaceImageBytes = fd.getExtractFaceImageBytes(true);
					if(extractFaceImageBytes == null) continue;
					resultValue = faceVerify.verify(extractFaceImageBytes, fd.getIdCard().getImageBytes());
					fd.setFaceCheckResult(resultValue);

					resultValue = fd.getFaceCheckResult();

					if (resultValue >= Config.getInstance().getFaceCheckThreshold()) {
						FaceCheckingService.getInstance().offerPassFaceData(fd);
						fd.saveVerifyFaceImageToDisk("Passed");
						log.debug("FaceData:"+fd.getFaceDetectedResult());
					} else {
						if (fd.getFaceDetectedResult().isWearsglasses()) {
							if (resultValue >= Config.getInstance().getGlassFaceCheckThreshold()) {
								FaceCheckingService.getInstance().offerPassFaceData(fd);
								fd.saveVerifyFaceImageToDisk("Passed");
								log.debug("FaceData:"+fd.getFaceDetectedResult());
							}else{
								fd.saveVerifyFaceImageToDisk("Failed");
							}
						}else{
							fd.saveVerifyFaceImageToDisk("Failed");
						}
					}
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

}
