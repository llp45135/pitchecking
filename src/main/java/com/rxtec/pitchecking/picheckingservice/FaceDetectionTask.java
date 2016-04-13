package com.rxtec.pitchecking.picheckingservice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.tracking.KLTHaarFaceTracker;

public class FaceDetectionTask implements Runnable {

	private FaceDetectionJniEntry faceTracker = new FaceDetectionJniEntry();

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

				FaceDetectedResult fl = faceTracker.detectLocation(convertImage(frame));
				FaceData fd = new FaceData(frame, fl);
				FaceDetectionService.getInstance().offerDetectedFaceData(fd);
			}
		}
	}

	private byte[] convertImage(MBFImage frame) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			ImageUtilities.write(frame, "JPG", output);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] buff = output.toByteArray();
		return buff;
	}

}
