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

public class FaceDetectionService {
	private Logger log = LoggerFactory.getLogger("FaceTrackingService");

	private JPanel videoPanel = null;

	public void setVideoPanel(JPanel videoPanel) {
		this.videoPanel = videoPanel;
	}

	private LinkedBlockingQueue<BufferedImage> frameImageQueue = new LinkedBlockingQueue<BufferedImage>(3);

	private LinkedBlockingQueue<FaceData> trackededFaceQueue = new LinkedBlockingQueue<FaceData>(3);

	private LinkedBlockingQueue<FaceData> waitForDetectedFaceQueue = new LinkedBlockingQueue<FaceData>(3);

	
	public LinkedBlockingQueue<FaceData> getDetectedFaceQueue() {
		return trackededFaceQueue;
	}

	public BufferedImage takeFrameImage() throws InterruptedException {
		return frameImageQueue.take();
	}

	public void offerWaitForDetectedFaceData(FaceData fd) {
		if(!waitForDetectedFaceQueue.offer(fd)){
			waitForDetectedFaceQueue.poll();
			waitForDetectedFaceQueue.offer(fd);
		}
		
	}

	public FaceData takeWaitForDetectedFaceData() throws InterruptedException {
//		log.debug("takeWaitForDetectedFaceData,waitForDetectedFaceQueue size=" + waitForDetectedFaceQueue.size());
		return waitForDetectedFaceQueue.take();
	}
	
	
	public void offerTrackedFaceData(FaceData fd) {
		if(!trackededFaceQueue.offer(fd)){
			trackededFaceQueue.poll();
			trackededFaceQueue.offer(fd);
		}
		
	}


	public FaceData takeTrackedFaceData() throws InterruptedException {
		log.debug("takeDetectedFaceData,detectedFaceQueue size=" + trackededFaceQueue.size());
		return trackededFaceQueue.take();
	}

	private static FaceDetectionService _instance = null;

	private FaceDetectionService() {
	}

	public static synchronized FaceDetectionService getInstance() {
		if(_instance == null) _instance = new FaceDetectionService();
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
		
		
		if(!frameImageQueue.offer(frame)){
			frameImageQueue.poll();
			frameImageQueue.offer(frame);
		}
	}

	/**
	 * get detected face
	 * 
	 * @return
	 */
	private FaceData pollTrackedFaceData() {
		return trackededFaceQueue.poll();
	}

	private void beginFaceTrackThread() {
		FaceTrackTask trackTask = new FaceTrackTask();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		FaceDetectTask detectTask = new FaceDetectTask();
		ExecutorService executer = Executors.newCachedThreadPool();
		executer.execute(trackTask);
//		executer.execute(detectTask);
	}

	int frameCounter = 0;

	public void beginVideoCaptureAndTracking() throws VideoCaptureException {
		beginFaceTrackThread();

		final Video<MBFImage> video;
		if (Config.getInstance().getRoateCapture()==1)
			video = new RotationVideoCapture(640, 480);
		else
			video = new VideoCapture(640,480);

		video.setCurrentFrameIndex(10);
		VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay(video, videoPanel);
		vd.addVideoListener(new VideoDisplayListener<MBFImage>() {
			@Override
			public void beforeUpdate(MBFImage frame) {

				BufferedImage bi = ImageUtilities.createBufferedImageForDisplay(frame.clone());
				offerFrame(bi);
				
				FaceData face = pollTrackedFaceData();
				
				if (face != null) {
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
