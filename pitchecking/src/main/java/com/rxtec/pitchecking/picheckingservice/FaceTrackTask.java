package com.rxtec.pitchecking.picheckingservice;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
	// private FaceDetectByPixelJNIEntry faceDetecter =
	// FaceDetectByPixelJNIEntry.getInstance();
	private FaceDetectByPixelJNIEntry faceDetecter = FaceDetectByPixelJNIEntry.getInstance();

	private Logger log = LoggerFactory.getLogger("FaceDetectionTask");
	Calendar cal = Calendar.getInstance();

	@Override
	public void run() {

		while (true) {

			try {
				Thread.sleep(50);
				detectFaceImage();
			} catch (InterruptedException e) {
				log.error("FaceTrackTask ", e);
			}

		}
	}
	
	
	private static FaceTrackTask _instance;
	private FaceTrackTask() {
	}

	public static synchronized FaceTrackTask getInstance() {
		if (_instance == null)
			_instance = new FaceTrackTask();
		return _instance;
	}
	
	
	public static void startTracking(){
		FaceTrackTask trackTask = FaceTrackTask.getInstance();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ExecutorService executer = Executors.newCachedThreadPool();
		executer.execute(trackTask);
		// executer.execute(detectTask);
	}
		
	

	private void detectFaceLocation() {
		try {
			BufferedImage frame = FaceDetectionService.getInstance().takeFrameImage();
			if (frame != null) {
				PICData fd = new PICData(frame);
				faceDetecter.detectFaceLocation(fd);

				if (fd.isDetectedFace())
					FaceDetectionService.getInstance().offerTrackedFaceData(fd);
			}
		} catch (InterruptedException e) {
			log.error("FaceTrackTask ", e);
		}

	}

	private void detectFaceImage() {
		try {
			BufferedImage frame = FaceDetectionService.getInstance().takeFrameImage();
			if (frame != null) {
				PICData fd = new PICData(frame);
				faceDetecter.detectFaceImage(fd);

				if (detectQuality(fd) && fd.isDetectedFace())
					FaceDetectionService.getInstance().offerTrackedFaceData(fd);
				FaceCheckingService.getInstance().offerDetectedFaceData(fd);
				if(fd.isDetectedFace()) fd.saveFaceDataToDsk();
			}

		} catch (InterruptedException e) {
			log.error("FaceTrackTask ", e);
		}

	}

	private byte[] convertImage(MBFImage frame) {

		BufferedImage bi = ImageUtilities.createBufferedImageForDisplay(frame);
		byte[] buff = ImageToolkit.getImageBytes(bi, "jpg");
		return buff;
	}

	private boolean detectQuality(PICData fd) {
		FaceDetectedResult r = fd.getFaceDetectedResult();
		if (r.isEyesfrontal() && r.isFacefrontal() && r.isEyesopen() && r.isHasface() && r.isExpression())
			return true;
		else
			return false;

	}

}
