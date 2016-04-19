package com.rxtec.pitchecking.picheckingservice;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.tracking.KLTHaarFaceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.utils.ImageToolkit;

public class FaceTrackTask implements Runnable {

	// private FaceDetectLocaltionJniEntry faceDetecter = new
	// FaceDetectLocaltionJniEntry();
//	private FaceDetectByPixelJNIEntry faceDetecter = FaceDetectByPixelJNIEntry.getInstance();
	private FaceDetectByPixelJNIEntryClone faceDetecter = FaceDetectByPixelJNIEntryClone.getInstance();
	
	private Logger log = LoggerFactory.getLogger("FaceDetectionTask");
	Calendar cal = Calendar.getInstance();

	@Override
	public void run() {

		while (true) {
			

			try {
				Thread.sleep(50);
				detectFaceImage();
//				detectFaceLocation();
			} catch (InterruptedException e) {
				log.error("FaceTrackTask ",e);
			}

		}
	}
	
	
	private void detectFaceLocation(){
		MBFImage frame = null;
		try {
			frame = FaceDetectionService.getInstance().takeFrameImage();
			if (frame != null) {
				byte[] bytes = convertImage(frame);
				if (bytes == null) return;

				FaceDetectedResult result = new FaceDetectedResult();
				result.setFrameImageBytes(bytes);
				FaceData fd = new FaceData(frame, result);
				faceDetecter.detectFaceLocation(fd);

				if(fd.isDetectedFace())
					FaceDetectionService.getInstance().offerTrackedFaceData(fd);
			}
		} catch (InterruptedException e) {
			log.error("FaceTrackTask ",e);
		}

	}

	
	private void detectFaceImage(){
		MBFImage frame = null;
		try {
			frame = FaceDetectionService.getInstance().takeFrameImage();
			if (frame != null) {
				byte[] bytes = convertImage(frame);
				if (bytes == null) return;

				FaceDetectedResult result = new FaceDetectedResult();
				result.setFrameImageBytes(bytes);
				FaceData fd = new FaceData(frame, result);
				faceDetecter.detectFaceImage(fd);

				if(detectQuality(fd)&&fd.isDetectedFace())
					FaceDetectionService.getInstance().offerTrackedFaceData(fd);
					FaceCheckingService.getInstance().offerDetectedFaceData(fd);
			}
		} catch (InterruptedException e) {
			log.error("FaceTrackTask ",e);
		}

	}

	private byte[] convertImage(MBFImage frame) {

		BufferedImage bi = ImageUtilities.createBufferedImageForDisplay(frame);
		byte[] buff = ImageToolkit.getImageBytes(bi, "jpg");
		return buff;
	}

	private boolean detectQuality(FaceData fd) {
		FaceDetectedResult r = fd.getFaceDetectedResult();
		if (r.isEyesfrontal() && r.isFacefrontal() && r.isEyesopen() && r.isHasface() && r.isExpression())
			return true;
		else
			return false;

	}

}
