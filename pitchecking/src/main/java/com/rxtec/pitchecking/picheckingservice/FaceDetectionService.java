package com.rxtec.pitchecking.picheckingservice;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.facedetect.Face;
import com.rxtec.pitchecking.facedetect.FaceDetect;
import com.rxtec.pitchecking.gui.VideoPanel;
import com.rxtec.pitchecking.utils.ImageToolkit;

public class FaceDetectionService implements IFaceTrackService {
	private Logger log = LoggerFactory.getLogger("FaceTrackingService");

	private JPanel videoPanel = null;

	public void setVideoPanel(JPanel videoPanel) {
		this.videoPanel = videoPanel;
	}

	private FaceDetect faceDetecter = new FaceDetect();

	private LinkedBlockingQueue<BufferedImage> frameImageQueue = new LinkedBlockingQueue<BufferedImage>(3);

	private LinkedBlockingQueue<PITData> trackededFaceQueue = new LinkedBlockingQueue<PITData>(3);

	private LinkedBlockingQueue<PITData> waitForDetectedFaceQueue = new LinkedBlockingQueue<PITData>(3);

	public LinkedBlockingQueue<PITData> getDetectedFaceQueue() {
		return trackededFaceQueue;
	}

	public BufferedImage takeFrameImage() throws InterruptedException {
		return frameImageQueue.take();
	}

	public void offerWaitForDetectedFaceData(PITData fd) {
		if (!waitForDetectedFaceQueue.offer(fd)) {
			waitForDetectedFaceQueue.poll();
			waitForDetectedFaceQueue.offer(fd);
		}

	}

	public PITData takeWaitForDetectedFaceData() throws InterruptedException {
		// log.debug("takeWaitForDetectedFaceData,waitForDetectedFaceQueue
		// size=" + waitForDetectedFaceQueue.size());
		return waitForDetectedFaceQueue.take();
	}

	public void offerTrackedFaceData(PITData fd) {
		if (!trackededFaceQueue.offer(fd)) {
			trackededFaceQueue.poll();
			trackededFaceQueue.offer(fd);
		}

	}

	public PITData takeTrackedFaceData() throws InterruptedException {
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
	private PITData pollTrackedFaceData() {
		return trackededFaceQueue.poll();
	}

	
	MBFImage currentFrame;
	
	
	private Device getDefaultDevice(){
		List<Device> devices = VideoCapture.getVideoDevices();
		Device device = null;
		for(Device d : devices){
			String name = d.getNameStr();
			if(name.equals(Config.UVCCameraName)){
				device = d;
				break;
			}
		}
		return device;

	}
	
	public void beginVideoCaptureAndTracking() {
		MFFaceTrackTask.startTracking();

		Device device = getDefaultDevice();
		if(device == null){
			log.error("!!!!!! Not found camera device!!!!!!!");
			return;
		}
		Video<MBFImage> video = null;
		try {
			if (Config.getInstance().getRoateCapture() == 1)
				video = new RotationVideoCapture(Config.FrameHeigh, Config.FrameWidth, device);
			else
				video = new VideoCapture(Config.FrameWidth, Config.FrameHeigh, device);
		} catch (VideoCaptureException e) {
			log.error("beginVideoCaptureAndTracking", e);
			;
		}

		video.setCurrentFrameIndex(10);
		VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay(video, videoPanel);
		vd.addVideoListener(new VideoDisplayListener<MBFImage>() {
			@Override
			public void beforeUpdate(MBFImage frame) {
				currentFrame = frame;
				BufferedImage bi = ImageUtilities.createBufferedImageForDisplay(frame.clone());
				offerFrame(bi);

				PITData faceData = pollTrackedFaceData();

				if (faceData != null) {
					faceData.setIdCard(currentIDCard);
//					frame.drawShape(faceData.getFaceLocation().getFaceBounds(), RGBColour.GREEN);
					
					FaceCheckingService.getInstance().offerDetectedFaceData(faceData);

				
				}
			}

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {
			}
		});
	}
	
	
	
	
	
	public void drawFaceLocation(Face[] faces){
		if(currentFrame != null){
			for(Face face : faces){
				Rectangle rect = new Rectangle(face.getX(), face.getY(), face.getWidth(), face.getHeight());
				currentFrame.drawShape(rect, RGBColour.RED);
			}
		}
	}

//	public void beginVideoCaptureAndTracking() {
//		Video<MBFImage> video = null;
//		try {
//			if (Config.getInstance().getRoateCapture() == 1)
//				video = new RotationVideoCapture(640, 480);
//			else
//				video = new VideoCapture(640, 480);
//		} catch (VideoCaptureException e) {
//			log.error("beginVideoCaptureAndTracking", e);
//			;
//		}
//		video.setCurrentFrameIndex(10);
//		VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay(video, videoPanel);
//
//		vd.addVideoListener(new VideoDisplayListener<MBFImage>() {
//			@Override
//			public void beforeUpdate(MBFImage frame) {
//
//				detectFaceLocation(frame);
//				try {
//					Thread.sleep(0);
//				} catch (InterruptedException e) {
//					log.error("InterruptedException",e);
//				}
//			}
//
//			@Override
//			public void afterUpdate(VideoDisplay<MBFImage> display) {
//			}
//		});
//	}
//
//	int frameCounter = 0;
//
//	private void detectFaceLocation(MBFImage frame) {
//
//		frameCounter++;
//		if (frameCounter >= Config.getInstance().getVideoCaptureFrequency()) {
//			frameCounter = 0;
//
//			BufferedImage frameImage = ImageUtilities.createBufferedImageForDisplay(frame.clone());
//			if (frameImage != null) {
//				BufferedImage grayImage = ImageToolkit.toGrayImage(frameImage);
//				byte[] pixels = ((DataBufferByte) grayImage.getRaster().getDataBuffer()).getData();
//
//				if (pixels != null) {
//					long nowMils = Calendar.getInstance().getTimeInMillis();
//					Face[] faces = faceDetecter.frontal(pixels, frameImage.getHeight(), frameImage.getWidth());
//					long usingTime = Calendar.getInstance().getTimeInMillis() - nowMils;
//					log.debug("faceDetecter.frontal, using " + usingTime + " ms" + " detect faces=" + faces.length);
//					for (Face face : faces) {
//						PICData fd = new PICData(frameImage);
//						fd.updateFaceLocation(face.getX(), face.getY(), face.getWidth(), face.getHeight());
//						fd.setDetectedFace(true);
//						if (fd.isDetectedFace()) {
//							fd.setIdCard(currentIDCard);
//							frame.drawShape(fd.getFaceLocation().getFaceBounds(), RGBColour.RED);
//							FaceCheckingService.getInstance().offerDetectedFaceData(fd);
//						}
//					}
//				}
//			}
//		}
//	}

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
