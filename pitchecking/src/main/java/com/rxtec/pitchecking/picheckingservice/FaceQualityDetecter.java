package com.rxtec.pitchecking.picheckingservice;

import java.io.IOException;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FaceQualityDetecter implements Runnable {

	private FaceDetectImageQualityJNIEntry faceDetecter = FaceDetectImageQualityJNIEntry.getInstance();
	private Logger log = LoggerFactory.getLogger("FaceQualityChecker");
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			
			FaceData fd = FaceCheckingService.getInstance().pollFaceDataForChecking();
			if(fd == null) continue;
			//log.debug("FaceCheckingService.getInstance().takeFaceDataForChecking");
			if(!fd.isDetectedFace()) continue;
			
			if(fd != null) {
				long nowMils = Calendar.getInstance().getTimeInMillis();
				faceDetecter.detectFaceQuality(fd.getFaceDetectedData());
				long usingTime = Calendar.getInstance().getTimeInMillis() - nowMils;

				//log.debug("Detect using" + usingTime + fl.toString());
				if(detectQuality(fd)){
					log.debug(fd.getFaceDetectedData().toString());
					fd.saveIDCardImageToLogDir();
					FaceCheckingService.getInstance().offerCheckedFaceData(fd);
				}
			}
		}
	}
	
	
	private boolean detectQuality(FaceData fd){
		FaceDetectedResult r = fd.getFaceDetectedData();
		if(r.isEyesfrontal() && r.isFacefrontal() && r.isEyesopen() && r.isHasface() && r.isExpression()) return true;
		else return false;
		
	}

}
