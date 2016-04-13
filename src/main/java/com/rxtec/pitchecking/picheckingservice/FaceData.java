package com.rxtec.pitchecking.picheckingservice;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Comparator;

import javax.imageio.ImageIO;

import org.apache.tools.ant.types.resources.selectors.Date;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.math.geometry.shape.Rectangle;

import com.rxtec.pitchecking.utils.ImageToolkit;



/**
 * 34567
 * @author lll
 *
 */

public class FaceData {
	private MBFImage frame = null;

	public MBFImage getFrame() {
		return frame;
	}

	public void setFrame(MBFImage frame) {
		this.frame = frame;
	}

	private DetectedFace face;

	public DetectedFace getFace() {
		return face;
	}

	public void setFace(DetectedFace face) {
		this.face = face;
	}

	private IDCard idCard;

	public IDCard getIdCard() {
		return idCard;
	}

	public void setIdCard(IDCard idCard) {
		this.idCard = idCard;
	}

	private long createTime;

	public long getCreateTime() {
		return createTime;
	}

	public float faceCheckResult;

	public float getFaceCheckResult() {
		return faceCheckResult;
	}

	public void setFaceCheckResult(float faceCheckResult) {
		this.faceCheckResult = faceCheckResult;
	}

	public FaceData(MBFImage fm, DetectedFace fc) {
		this.frame = fm;
		this.face = fc;
		this.faceX = (int) fc.getBounds().x;
		this.faceY = (int) fc.getBounds().y;
		this.faceWidth = (int) fc.getBounds().width;
		this.faceHeight = (int) fc.getBounds().height;
		createTime = Calendar.getInstance().getTimeInMillis();
	}
	
	public FaceData(MBFImage fm, FaceDetectedData fl) {
		this.frame = fm;
		this.faceX = fl.getX();
		this.faceY =  fl.getY();
		this.faceWidth = fl.getWidth();
		this.faceHeight = fl.getHeight();
		if(fl.getWidth() == 0) this.isDetectedFace = false;
		else this.isDetectedFace = true;
		createTime = Calendar.getInstance().getTimeInMillis();
	}

	public Rectangle getFaceBounds() {
		return new Rectangle(faceX, faceY, faceWidth, faceHeight);
	}

	private int faceX;
	private int faceY;
	private int faceWidth;
	private int faceHeight;
	
	
	public byte[] getFaceImageByteArray() throws IOException {
		MBFImage extractFrame = frame.extractROI(faceX, faceY, faceWidth, faceHeight);
		BufferedImage result = ImageUtilities.createBufferedImageForDisplay(extractFrame);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(result, "JPEG", ImageIO.createImageOutputStream(os));
		
//		String fn = "C:/DCZ/20160412/out/"+this.faceX+".jpg";
//		ImageIO.write(result, "JPEG", ImageIO.createImageOutputStream(new File(fn)));
		
		
		return os.toByteArray();

	}

	public BufferedImage getFaceImageBufferedImage() throws IOException {
		BufferedImage sourceImage = ImageUtilities.createBufferedImage(frame);
		BufferedImage result = ImageToolkit.cut(sourceImage, faceX, faceY, faceWidth, faceHeight);
		return result;

	}
	
	private  boolean isDetectedFace = false;

	public boolean isDetectedFace() {
		return isDetectedFace;
	}

	public void setDetectedFace(boolean isDetectedFace) {
		this.isDetectedFace = isDetectedFace;
	}

}

class FaceDataComparator implements Comparator {
	public int compare(Object obj1, Object obj2) {
		return 0;
	}
}
