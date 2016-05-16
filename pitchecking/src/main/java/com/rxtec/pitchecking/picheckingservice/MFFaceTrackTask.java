package com.rxtec.pitchecking.picheckingservice;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.facedetect.Face;
import com.rxtec.pitchecking.facedetect.FaceDetect;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.ImageToolkit;

public class MFFaceTrackTask implements Runnable {

	// private FaceDetectLocaltionJniEntry faceDetecter = new
	// FaceDetectLocaltionJniEntry();
	// private FaceDetectByPixelJNIEntry faceDetecter =
	// FaceDetectByPixelJNIEntry.getInstance();
	// private FaceDetectByPixelJNIEntry faceDetecter =
	// FaceDetectByPixelJNIEntry.getInstance();

	private FaceDetect faceDetecter = new FaceDetect();

	private Logger log = LoggerFactory.getLogger("FaceDetectionTask");
	Calendar cal = Calendar.getInstance();

	private static MFFaceTrackTask _instance;

	private MFFaceTrackTask() {
	}

	public static synchronized MFFaceTrackTask getInstance() {
		if (_instance == null)
			_instance = new MFFaceTrackTask();
		return _instance;
	}

	public static void startTracking() {
		MFFaceTrackTask trackTask = MFFaceTrackTask.getInstance();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ExecutorService executer = Executors.newCachedThreadPool();
		executer.execute(trackTask);
		// executer.execute(detectTask);

	}

	@Override
	public void run() {

		while (true) {

			try {
				Thread.sleep(50);

				detectFaceLocation();
			} catch (InterruptedException e) {
				log.error("FaceTrackTask ", e);
			}

		}
	}

	private void detectFaceLocation() {
		try {
			BufferedImage frame = FaceDetectionService.getInstance().takeFrameImage();
			if (frame != null) {
				PICData fd = new PICData(frame);
				String fn = saveFImageToDsk(frame);
				if (!fn.equals("")) {
					Face[] faces = faceDetecter.frontal(fn, 0, 0, 0, 0, 0, 0, 0);
//					faces[0].

					if (fd.isDetectedFace())
						FaceDetectionService.getInstance().offerTrackedFaceData(fd);
				}
			}
		} catch (InterruptedException e) {
			log.error("FaceTrackTask ", e);
		}

	}

	private String saveFImageToDsk(BufferedImage frame) {
		if (frame == null)
			return "";
		BufferedImage fimg = ImageUtilities.createBufferedImage(ImageUtilities.createFImage(frame));
		String dirName = Config.getInstance().getImagesLogDir();
		String fn = "";
		int ret = CommUtil.createDir(dirName);
		if (ret == 0 || ret == 1) {
			fn = dirName + "/FCTMP.jpg";
			java.io.File f = new java.io.File(fn);
			if (f.exists())
				f.deleteOnExit();
			try {
				ImageIO.write(fimg, "JPEG", f);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fn = "";
			}

		}

		return fn;
	}

	private void detectFaceImage() {
		// try {
		// BufferedImage frame =
		// FaceDetectionService.getInstance().takeFrameImage();
		// if (frame != null) {
		// PICData fd = new PICData(frame);
		// faceDetecter.detectFaceImage(fd);
		//
		// if (detectQuality(fd) && fd.isDetectedFace())
		// FaceDetectionService.getInstance().offerTrackedFaceData(fd);
		// FaceCheckingService.getInstance().offerDetectedFaceData(fd);
		// if(fd.isDetectedFace()) fd.saveFaceDataToDsk();
		// }
		//
		// } catch (InterruptedException e) {
		// log.error("FaceTrackTask ", e);
		// }

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
