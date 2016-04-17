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

public class FaceDetectionTask implements Runnable {

	// private FaceDetectLocaltionJniEntry faceDetecter = new
	// FaceDetectLocaltionJniEntry();
	private IFaceDetect faceDetecter = FaceDetectByPixelJNIEntry.getInstance();
	private Logger log = LoggerFactory.getLogger("FaceDetectionTask");
	Calendar cal = Calendar.getInstance();

	@Override
	public void run() {

		while (true) {
			

			MBFImage frame = null;
			try {
				Thread.sleep(50);
				frame = FaceDetectionService.getInstance().takeFrameImage();
				
				if (frame != null) {

					byte[] bytes = convertImage(frame);
					if (bytes == null)
						return;

					FaceDetectedResult result = new FaceDetectedResult();
					result.setImageBytes(bytes);
					faceDetecter.detectFaceImage(result);
					log.debug("FaceDetectedResult : " + result);

					FaceData fd = new FaceData(frame, result);
					if (detectQuality(fd))
						FaceDetectionService.getInstance().offerDetectedFaceData(fd);

				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private byte[] convertImage(MBFImage frame) {

		BufferedImage bi = ImageUtilities.createBufferedImageForDisplay(frame);
		byte[] buff = ImageToolkit.getImageBytes(bi, "jpg");
		return buff;
	}

	private boolean detectQuality(FaceData fd) {
		FaceDetectedResult r = fd.getFaceDetectedData();
		if (r.isEyesfrontal() && r.isFacefrontal() && r.isEyesopen() && r.isHasface() && r.isExpression())
			return true;
		else
			return false;

	}

}
