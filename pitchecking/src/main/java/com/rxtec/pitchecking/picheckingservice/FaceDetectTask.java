package com.rxtec.pitchecking.picheckingservice;

import java.awt.image.BufferedImage;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FaceDetectTask implements Runnable {

	// private FaceDetectLocaltionJniEntry faceDetecter = new
	// FaceDetectLocaltionJniEntry();
	private IFaceDetect faceDetecter = FaceDetectByPixelJNIEntry.getInstance();
	private Logger log = LoggerFactory.getLogger("FaceDetectionTask");
	Calendar cal = Calendar.getInstance();

	@Override
	public void run() {

		while (true) {
			try {
				Thread.sleep(50);
				FaceData fd = FaceDetectionService.getInstance().takeWaitForDetectedFaceData();
				if (fd != null) {
					faceDetecter.detectFaceImageQuality(fd.getFaceDetectedData());
//					log.debug("FaceDetectedResult : " + fd.getFaceDetectedData());
					if (detectQuality(fd))
						FaceCheckingService.getInstance().offerDetectedFaceData(fd);
				}
			} catch (InterruptedException e) {
				log.error("FaceDetectTask",e);
			}

		}
	}


	private boolean detectQuality(FaceData fd) {
		FaceDetectedResult r = fd.getFaceDetectedData();
		if (r.isEyesfrontal() && r.isFacefrontal() && r.isEyesopen() && r.isHasface() && r.isExpression())
			return true;
		else
			return false;

	}

}
