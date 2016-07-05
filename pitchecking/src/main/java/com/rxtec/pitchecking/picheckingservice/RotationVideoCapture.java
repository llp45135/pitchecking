package com.rxtec.pitchecking.picheckingservice;

import java.awt.image.BufferedImage;
import java.util.List;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.transform.ProjectionProcessor;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

/**
 * 旋转视频流图像90°
 * @author lll
 *
 */
public class RotationVideoCapture extends VideoCapture{

	public RotationVideoCapture(int width, int height,Device d) throws VideoCaptureException {
		super(width, height, d);
		// TODO Auto-generated constructor stub
	}

	@Override 
	public MBFImage getCurrentFrame() {
		MBFImage currentFrame = super.getCurrentFrame();
		BufferedImage bi = rotate90DX(ImageUtilities.createBufferedImage(currentFrame));
		MBFImage rotatedImage = ImageUtilities.createMBFImage(bi,false);
		return rotatedImage;
	}
	@Override 
	public MBFImage getNextFrame() {
		MBFImage nextFrame = super.getNextFrame();
		BufferedImage bi = rotate90DX(ImageUtilities.createBufferedImage(nextFrame));
		MBFImage rotatedImage = ImageUtilities.createMBFImage(bi,false);
		
		return rotatedImage;
	}
	
	public BufferedImage rotate90DX(BufferedImage bi)
	{
	    int width = bi.getWidth();
	    int height = bi.getHeight();

	    BufferedImage biFlip = new BufferedImage(height, width, bi.getType());

	    for(int i=0; i<width; i++)
	        for(int j=0; j<height; j++)
	            biFlip.setRGB(height-1-j, width-1-i, bi.getRGB(i, j));

	    return biFlip;
	}

	public BufferedImage rotate90SX(BufferedImage bi)
	{
	    int width = bi.getWidth();
	    int height = bi.getHeight();

	    BufferedImage biFlip = new BufferedImage(height, width, bi.getType());

	    for(int i=0; i<width; i++)
	        for(int j=0; j<height; j++)
	            biFlip.setRGB(j, i, bi.getRGB(i, j));

	    return biFlip;
	}
}
