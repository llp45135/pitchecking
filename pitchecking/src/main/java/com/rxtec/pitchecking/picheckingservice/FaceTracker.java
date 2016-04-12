package com.rxtec.pitchecking.picheckingservice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.tracking.KLTHaarFaceTracker;

public class FaceTracker implements Runnable {

	private FaceLocationDetection faceTracker = new FaceLocationDetection();


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
			
			MBFImage frame = FaceTrackingService.getInstance().takeFrameImage();
			if(frame != null){
				
				FaceLocation fl = faceTracker.detect(convertImage(frame));
				FaceData fd = new FaceData(frame, fl);
				FaceTrackingService.getInstance().offerDetectedFaceData(fd);
				
			}
		}
	}
	
	
	private byte[] convertImage(MBFImage frame ){
		ByteArrayOutputStream output = new ByteArrayOutputStream ();
		try {
			ImageUtilities.write(frame, "JPG", output);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] buff = output.toByteArray();
		return buff;
	}

}
