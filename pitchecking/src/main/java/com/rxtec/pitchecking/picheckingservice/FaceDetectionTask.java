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


//	private FaceDetectLocaltionJniEntry faceDetecter = new FaceDetectLocaltionJniEntry();
	private FaceDetectImageQualityJNIEntry faceDetecter = FaceDetectImageQualityJNIEntry.getInstance();
	private Logger log = LoggerFactory.getLogger("FaceDetectionTask");


	@Override
	public void run() {
		System.out.println("FaceTrack thread is running......");


		
		while (true) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			MBFImage frame = FaceDetectionService.getInstance().takeFrameImage();
			if (frame != null) {

				FaceDetectedResult fl = new FaceDetectedResult();
				byte[] bytes = convertImage(frame);
				if(bytes == null) continue;
				fl.setImageBytes(bytes);
				//faceTracker.detectFaceImage(fl);
				
				long nowMils = Calendar.getInstance().getTimeInMillis();
				faceDetecter.detectFaceLocation(fl);
				
				
				faceDetecter.detectFaceQuality(fl);
				
				long usingTime = Calendar.getInstance().getTimeInMillis() - nowMils;

				//log.debug("Detect using" + usingTime + fl.toString());

				FaceData fd = new FaceData(frame, fl);
				if(fd.isDetectedFace()) FaceDetectionService.getInstance().offerDetectedFaceData(fd);
			
			}
		}
	}

	private byte[] convertImage(MBFImage frame) {
		
		BufferedImage bi = ImageUtilities.createBufferedImageForDisplay(frame);
		byte[] buff = ImageToolkit.getImageBytes(bi,"jpg");
		return buff;
	}
	

}
