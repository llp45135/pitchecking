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
				PICData fd = FaceCheckingService.getInstance().takeDetectedFaceData();
				if (fd != null) {
					IDCard idCard = fd.getIdCard();
					if (idCard == null)
						continue;
					long nowMils = Calendar.getInstance().getTimeInMillis();
					float resultValue = 0;
					byte[] extractFaceImageBytes = fd.getExtractFaceImageBytes();
					if (extractFaceImageBytes == null)
						continue;
					resultValue = faceVerify.verify(extractFaceImageBytes, fd.getIdCard().getImageBytes());
					fd.setFaceCheckResult(resultValue);
					long usingTime = Calendar.getInstance().getTimeInMillis() - nowMils;
					if (resultValue >= Config.getInstance().getFaceCheckThreshold()) {
						FaceCheckingService.getInstance().offerPassFaceData(fd);
					} else {
						if (fd.getFaceDetectedResult() != null) {
							if (fd.getFaceDetectedResult().isWearsglasses()) {
								if (resultValue >= Config.getInstance().getGlassFaceCheckThreshold()) {
									FaceCheckingService.getInstance().offerPassFaceData(fd);
								}
							}
						}
					}
					fd.saveFaceDataToDsk();
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

}
