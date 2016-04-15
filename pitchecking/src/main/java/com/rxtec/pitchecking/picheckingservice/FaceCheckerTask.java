package com.rxtec.pitchecking.picheckingservice;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;

public class FaceCheckerTask implements Runnable {

	FaceVerifyJniEntry faceVerify = new FaceVerifyJniEntry();
	private Logger log = LoggerFactory.getLogger("FaceChecker");
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			
			FaceData fd = FaceCheckingService.getInstance().pollFaceDataForChecking();
			if(fd == null) continue;
			//log.debug("FaceCheckingService.getInstance().takeFaceDataForChecking");
			if(!fd.isDetectedFace()) continue;
			float resultValue = 0;
			
			if(fd != null) {
				try {
					resultValue = faceVerify.verify(fd.getFaceImageByteArray(), fd.getIdCard().getImageBytes());
					fd.setFaceCheckResult(resultValue);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				FaceCheckingService.getInstance().offerCheckedFaceData(fd);
			}
		}
	}

}
