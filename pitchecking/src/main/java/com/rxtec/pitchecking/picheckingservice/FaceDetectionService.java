package com.rxtec.pitchecking.picheckingservice;

import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.gui.VideoPanel;

public class FaceDetectionService implements IFaceTrackService {
	private Logger log = LoggerFactory.getLogger("FaceTrackingService");

	private JPanel videoPanel = null;

	public void setVideoPanel(JPanel videoPanel) {
		this.videoPanel = videoPanel;
	}
	

	private LinkedBlockingQueue<BufferedImage> frameImageQueue = new LinkedBlockingQueue<BufferedImage>(3);

	private LinkedBlockingQueue<PICData> trackededFaceQueue = new LinkedBlockingQueue<PICData>(3);

	private LinkedBlockingQueue<PICData> waitForDetectedFaceQueue = new LinkedBlockingQueue<PICData>(3);

	public LinkedBlockingQueue<PICData> getDetectedFaceQueue() {
		return trackededFaceQueue;
	}

	public BufferedImage takeFrameImage() throws InterruptedException {
		return frameImageQueue.take();
	}

	public void offerWaitForDetectedFaceData(PICData fd) {
		if (!waitForDetectedFaceQueue.offer(fd)) {
			waitForDetectedFaceQueue.poll();
			waitForDetectedFaceQueue.offer(fd);
		}

	}

	public PICData takeWaitForDetectedFaceData() throws InterruptedException {
		// log.debug("takeWaitForDetectedFaceData,waitForDetectedFaceQueue
		// size=" + waitForDetectedFaceQueue.size());
		return waitForDetectedFaceQueue.take();
	}

	public void offerTrackedFaceData(PICData fd) {
		if (!trackededFaceQueue.offer(fd)) {
			trackededFaceQueue.poll();
			trackededFaceQueue.offer(fd);
		}

	}

	public PICData takeTrackedFaceData() throws InterruptedException {
		log.debug("takeDetectedFaceData,detectedFaceQueue size=" + trackededFaceQueue.size());
		return trackededFaceQueue.take();
	}

	private static FaceDetectionService _instance = null;

	private FaceDetectionService() {
	}

	public static synchronized FaceDetectionService getInstance() {
		if (_instance == null)
			_instance = new FaceDetectionService();
		return _instance;
	}

	/**
	 * inFrameQueueMaxSize size,default = 5
	 */

	private int inFrameQueueMaxSize = 5;

	public int getInFrameQueueMaxSize() {
		return inFrameQueueMaxSize;
	}

	public void setInFrameQueueMaxSize(int inFrameQueueMaxSize) {
		this.inFrameQueueMaxSize = inFrameQueueMaxSize;
	}

	/**
	 * offerFrame
	 * 
	 * @param frame
	 */
	private void offerFrame(BufferedImage frame) {

		if (!frameImageQueue.offer(frame)) {
			frameImageQueue.poll();
			frameImageQueue.offer(frame);
		}
	}

	/**
	 * get detected face
	 * 
	 * @return
	 */
	private PICData pollTrackedFaceData() {
		return trackededFaceQueue.poll();
	}



	int frameCounter = 0;

	public void beginVideoCaptureAndTracking() {
		MFFaceTrackTask.startTracking();

		Video<MBFImage> video = null;
		try {
			if (Config.getInstance().getRoateCapture() == 1)
				video = new RotationVideoCapture(640, 480);
			else
				video = new VideoCapture(640, 480);
		} catch (VideoCaptureException e) {
			log.error("beginVideoCaptureAndTracking",e);;
		}

		video.setCurrentFrameIndex(10);
		VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay(video, videoPanel);
		vd.addVideoListener(new VideoDisplayListener<MBFImage>() {
			@Override
			public void beforeUpdate(MBFImage frame) {

				BufferedImage bi = ImageUtilities.createBufferedImageForDisplay(frame.clone());
				offerFrame(bi);

				PICData face = pollTrackedFaceData();

				if (face != null) {
					face.setIdCard(currentIDCard);
					offerWaitForDetectedFaceData(face);
					frame.drawShape(face.getFaceLocation().getFaceBounds(), RGBColour.RED);
				}
			}

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {
			}
		});
	}

	private IDCard currentIDCard = null;

	public IDCard getCurrentIDCard() {
		return currentIDCard;
	}

	public void setCurrentIDCard(IDCard currentIDCard) {
		this.currentIDCard = currentIDCard;
	}

	/**
	 * 开始人证对比
	 * 
	 * @param idCard
	 */
	public void beginCheckingFace(IDCard idCard) {
		currentIDCard = idCard;

	}

	/**
	 * 结束人证对比
	 */
	public void stopCheckingFace() {
		currentIDCard = null;
		FaceCheckingService.getInstance().resetFaceDataQueue();
		this.frameImageQueue.clear();
		trackededFaceQueue.clear();
		waitForDetectedFaceQueue.clear();
	}



}
