package com.rxtec.pitchecking.picheckingservice;

public class FaceAuther implements Runnable {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			
			FaceData fd = FaceCheckingService.getInstance().takeFaceDataForChecking();
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(fd != null) {
				FaceCheckingService.getInstance().offerCheckedFaceData(fd);
			}

		}
	}

}
