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
				FaceData fd = FaceCheckingService.getInstance().takeCheckedFaceData();
				if (fd == null)
					return;
				long nowMils = Calendar.getInstance().getTimeInMillis();
				float resultValue = 0;

				if (fd != null) {

					resultValue = faceVerify.verify(fd.getExtractFaceImageBytes(), fd.getIdCard().getImageBytes());
					fd.setFaceCheckResult(resultValue);
					long usingTime = Calendar.getInstance().getTimeInMillis() - nowMils;

					resultValue = fd.getFaceCheckResult();

					if (resultValue >= Config.getInstance().getFaceCheckThreshold()) {
						FaceCheckingService.getInstance().offerPassFaceData(fd);
					} else {
						if (fd.getFaceDetectedData().isWearsglasses()) {
							if (resultValue >= Config.getInstance().getGlassFaceCheckThreshold()) {
								FaceCheckingService.getInstance().offerPassFaceData(fd);
							}
						}
					}

					log.debug("FaceVerifyJniEntry using:" + usingTime + " ret=" + resultValue);
				}

			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

}
