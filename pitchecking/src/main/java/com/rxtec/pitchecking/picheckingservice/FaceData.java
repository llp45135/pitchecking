package com.rxtec.pitchecking.picheckingservice;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

import javax.imageio.ImageIO;

import org.jfree.util.Log;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.math.geometry.shape.Rectangle;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.ImageToolkit;

/**
 * 34567
 * 
 * @author lll
 *
 */

public class FaceData {
	private MBFImage frame = null;
	private MBFImage extractFrame = null;

	public MBFImage getExtractFrame() {
		return extractFrame;
	}

	public void setExtractFrame(MBFImage extractFrame) {
		this.extractFrame = extractFrame;
	}

	private FaceDetectedResult faceDetectedResult;

	public FaceDetectedResult getFaceDetectedResult() {
		return faceDetectedResult;
	}

	public void setFaceDetectedResult(FaceDetectedResult fdr) {
		this.faceDetectedResult = fdr;
		this.faceX = fdr.getX();
		this.faceY = fdr.getY();
		this.faceWidth = fdr.getWidth();
		this.faceHeight = fdr.getHeight();

	}

	public void updateFaceLocation() {
		this.faceX = faceDetectedResult.getX();
		this.faceY = faceDetectedResult.getY();
		this.faceWidth = faceDetectedResult.getWidth();
		this.faceHeight = faceDetectedResult.getHeight();
		if (faceWidth > 50 && faceHeight > 50)
			this.isDetectedFace = true;

	}

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
		extractFrame(fm);
		createTime = Calendar.getInstance().getTimeInMillis();
	}

	private void extractFrame(MBFImage fm) {
		int x = (int) (faceX * 0.8);
		int y = (int) (faceY * 0.8);
		int width = (int) (faceWidth * 1.2);
		int height = (int) (faceHeight * 1.3);

		extractFrame = frame.extractROI(x, y, width, height);

	}

	public FaceData(MBFImage fm, MBFImage extractFrame, FaceDetectedResult fdr) {
		this.frame = fm;
		this.extractFrame = extractFrame;
		this.faceDetectedResult = fdr;
	}

	public FaceData(MBFImage fm, FaceDetectedResult fdr) {
		this.faceDetectedResult = fdr;
		this.frame = fm;
		this.faceX = fdr.getX();
		this.faceY = fdr.getY();
		this.faceWidth = fdr.getWidth();
		this.faceHeight = fdr.getHeight();

		// this.faceX = fdr.getHeadLeft();
		// this.faceY = fdr.getHeadTop();
		// this.faceWidth = fdr.getHeadRight() - fdr.getHeadLeft();
		// this.faceHeight = fdr.getChinPos() - fdr.getHeadTop();

		if (this.faceWidth < 50)
			this.isDetectedFace = false;
		else {
			this.isDetectedFace = true;
			extractFrame(fm);
			fdr.setExtractImageBytes(getExtractFaceImageBytes(false));
		}
		this.faceDetectedResult = fdr;
		createTime = Calendar.getInstance().getTimeInMillis();
	}

	public Rectangle getFaceBounds() {
		return new Rectangle(faceX, faceY, faceWidth, faceHeight);
	}

	private int faceX;
	private int faceY;
	private int faceWidth;
	private int faceHeight;

	public byte[] getExtractFaceImageBytes(boolean isSaveToDisk) {
		if (this.faceWidth == 0 || this.faceHeight == 0)
			return null;

		if (extractFrame == null)
			this.extractFrame(this.frame);
		BufferedImage bi = ImageUtilities.createBufferedImageForDisplay(extractFrame);
		bi = ImageToolkit.scale(bi, 120, 140, true);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(bi, "JPEG", ImageIO.createImageOutputStream(os));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		if (isSaveToDisk) {
			saveExtractFaceImageToDisk(bi);
		}
		return os.toByteArray();
	}

	private void saveIDCardImageToDisk(String dirName) {
		if (idCard == null || idCard.getCardImage() == null || idCard.getIdNo() == null)
			return;
		String fn = dirName + idCard.getIdNo().hashCode() + ".jpg";
		
		Log.debug(fn);
		if(dirName == null){
			Log.error("saveIDCardImageToDisk failed,logDir is not exist");
			return;
		}
		BufferedImage bi = idCard.getCardImage();
		try {
			ImageIO.write(bi, "JPEG", ImageIO.createImageOutputStream(new File(fn)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.error("saveToImagesLogFile failed", e);
		}


	}

	private void saveExtractFaceImageToDisk(BufferedImage bi) {
		String dirName = Config.getInstance().getImagesLogDir();
		int ret = CommUtil.createDir(dirName);
		if (ret == 0 || ret == 1) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
			String toDayDir = formatter.format(new Date());
			ret = CommUtil.createDir(dirName + toDayDir);

			if (ret == 0 || ret == 1) {
				formatter = new SimpleDateFormat("hh-mm-ss-SSS");
				StringBuffer sb = new StringBuffer();
				sb.append(dirName);
				sb.append(toDayDir);
				sb.append("/");
				sb.append(idCard.getIdNo().hashCode());
				sb.append("@");
				sb.append(formatter.format(new Date()));
				sb.append(".jpg");
				String fn = sb.toString();
				try {
					ImageIO.write(bi, "JPEG", ImageIO.createImageOutputStream(new File(fn)));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.error("saveToImagesLogFile failed", e);
				}
			}
		}
	}

	
	
	public void saveVerifyFaceImageToDisk(String type) {
		String dirName = getFaceDataLogDir(type);
		if(dirName == null){
			Log.error("saveVerifyFaceImageToDisk failed,logDir is not exist");
			return;
		}
		saveIDCardImageToDisk(dirName);
		if (extractFrame == null)
			this.extractFrame(this.frame);
		BufferedImage bi = ImageUtilities.createBufferedImageForDisplay(extractFrame);
		SimpleDateFormat formatter = new SimpleDateFormat("hh-mm-ss-SSS");

		StringBuffer sb = new StringBuffer();
		sb.append(dirName);
		sb.append(idCard.getIdNo().hashCode());
		sb.append("@");
		sb.append(formatter.format(new Date()));
		sb.append("@");
		sb.append(String.format("%.2f", this.faceCheckResult));
		sb.append(".jpg");
		String fn = sb.toString();
		Log.debug(fn);

		try {
			ImageIO.write(bi, "JPEG", ImageIO.createImageOutputStream(new File(fn)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.error("saveToImagesLogFile failed", e);
		}
	}
	
	
	private String getFaceDataLogDir(String type){
		String dirName = Config.getInstance().getImagesLogDir();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		String toDayDir = formatter.format(new Date());
		String passedDir = "/Passed/";
		String failedDir = "/Failed/";
		String dnPassed = dirName + toDayDir + passedDir;
		String dnFailed = dirName + toDayDir + failedDir;
		
		int ret = CommUtil.createDir(dnPassed);
		if (ret != 0 && ret != 1) {
			dnPassed = null;
		}

		ret = CommUtil.createDir(dnFailed);
		if (ret != 0 && ret != 1) {
			dnFailed = null;
		}
		if(type.equals("Passed")) return dnPassed;
		else return dnFailed;

	}

	public BufferedImage getFaceImageBufferedImage() throws IOException {
		BufferedImage sourceImage = ImageUtilities.createBufferedImage(frame);
		BufferedImage result = ImageToolkit.cut(sourceImage, faceX, faceY, faceWidth, faceHeight);
		return result;

	}

	private boolean isDetectedFace = false;

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
