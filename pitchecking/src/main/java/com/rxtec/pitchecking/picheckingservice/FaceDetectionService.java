package com.rxtec.pitchecking.picheckingservice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FaceDetectionService {
	private Logger log = LoggerFactory.getLogger("FaceTrackingService");

	private JPanel videoPanel = null;

	public void setVideoPanel(JPanel videoPanel) {
		this.videoPanel = videoPanel;
	}

	private LinkedBlockingQueue<MBFImage> frameImageQueue = new LinkedBlockingQueue<MBFImage>(5);

	private LinkedBlockingQueue<FaceData> detectedFaceQueue = new LinkedBlockingQueue<FaceData>(5);

	public LinkedBlockingQueue<FaceData> getDetectedFaceQueue() {
		return detectedFaceQueue;
	}

	public MBFImage takeFrameImage() {
		return frameImageQueue.poll();
	}

	public void offerDetectedFaceData(FaceData fd) {
		detectedFaceQueue.offer(fd);
	}

	public FaceData takeDetectedFaceData() {
		log.debug("takeDetectedFaceData,detectedFaceQueue size=" + detectedFaceQueue.size());
		return detectedFaceQueue.poll();
	}

	private static FaceDetectionService _instance = new FaceDetectionService();

	private FaceDetectionService() {
	}

	public static FaceDetectionService getInstance() {
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
	private void offerFrame(MBFImage frame) {
		frameImageQueue.offer(frame);

	}

	/**
	 * get detected face
	 * 
	 * @return
	 */
	private FaceData getFaceFromDetectedFaceQueue() {
		return detectedFaceQueue.poll();
	}

	private void beginFaceTrackThread() {
		FaceDetectionTask task = new FaceDetectionTask();
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleWithFixedDelay(task, 0, 50, TimeUnit.MILLISECONDS);
	}

	boolean isRotation = true;

	int frameCounter = 0;

	public void beginVideoCaptureAndTracking() throws VideoCaptureException {
		beginFaceTrackThread();

		final Video<MBFImage> video;
		if (isRotation)
			video = new RotationVideoCapture(640, 480);
		else
			video = new VideoCapture(320, 480);

		video.setCurrentFrameIndex(1);
		VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay(video, videoPanel);
		vd.addVideoListener(new VideoDisplayListener<MBFImage>() {
			@Override
			public void beforeUpdate(MBFImage frame) {

				offerFrame(frame.clone());
				FaceData face = getFaceFromDetectedFaceQueue();
				if (face != null) {
					if (currentIDCard != null) {
						face.setIdCard(currentIDCard);
						FaceCheckingService.getInstance().offerCheckedFaceData(face);

					}
					frame.drawShape(face.getFaceBounds(), RGBColour.RED);
				}
			}

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {
			}
		});
	}

	private IDCard currentIDCard = null;

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
		FaceCheckingService.getInstance().resetFaceDataForCheckingQueue();
	}

}
