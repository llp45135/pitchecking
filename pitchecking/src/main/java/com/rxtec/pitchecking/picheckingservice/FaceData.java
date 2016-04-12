package com.rxtec.pitchecking.picheckingservice;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Comparator;

import javax.imageio.ImageIO;

import org.apache.tools.ant.types.resources.selectors.Date;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.math.geometry.shape.Rectangle;

import com.rxtec.pitchecking.utils.ImageToollit;



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

	public float imageQuality;

	public FaceData(MBFImage fm, DetectedFace fc) {
		this.frame = fm;
		this.face = fc;
		this.faceX = (int) fc.getBounds().x;
		this.faceY = (int) fc.getBounds().y;
		this.faceWidth = (int) fc.getBounds().width;
		this.faceHeight = (int) fc.getBounds().height;
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
		BufferedImage sourceImage = ImageUtilities.createBufferedImage(frame);
		BufferedImage result = ImageToollit.cut(sourceImage, faceX, faceY, faceWidth, faceHeight);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(result, "bmp", ImageIO.createImageOutputStream(os));
		return os.toByteArray();

	}

	public BufferedImage getFaceImageBufferedImage() throws IOException {
		BufferedImage sourceImage = ImageUtilities.createBufferedImage(frame);
		BufferedImage result = ImageToollit.cut(sourceImage, faceX, faceY, faceWidth, faceHeight);
		return result;

	}

}

class FaceDataComparator implements Comparator {
	public int compare(Object obj1, Object obj2) {
		return 0;
	}
}
