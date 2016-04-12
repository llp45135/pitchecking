package com.rxtec.pitchecking.picheckingservice;

import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.transform.ProjectionProcessor;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

/**
 * 旋转视频流图像90°
 * @author lll
 *
 */
public class RotationVideoCapture extends VideoCapture{

	public RotationVideoCapture(int width, int height) throws VideoCaptureException {
		super(width, height);
		// TODO Auto-generated constructor stub
	}

	@Override 
	public MBFImage getCurrentFrame() {
		MBFImage rotatedImage = super.getCurrentFrame();
		rotatedImage = ProjectionProcessor.project(rotatedImage, TransformUtilities.rotationMatrix(90));
		return rotatedImage;
	}
	@Override 
	public MBFImage getNextFrame() {
		MBFImage rotatedImage = super.getNextFrame();
		rotatedImage = ProjectionProcessor.project(rotatedImage, TransformUtilities.rotationMatrix(90));
		return rotatedImage;
	}
}
