package com.rxtec.pitchecking.picheckingservice;

import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.tracking.KLTHaarFaceTracker;

public class FaceTrackerByKLHaar implements Runnable {

	private KLTHaarFaceTracker faceTracker = new KLTHaarFaceTracker(40);


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
				List<DetectedFace> faces = faceTracker
						.trackFace(frame.flatten());
				for(DetectedFace f : faces){
					FaceData fd = new FaceData(frame, f);
					FaceTrackingService.getInstance().offerDetectedFaceData(fd);
				}
			}
		}
	}

}
