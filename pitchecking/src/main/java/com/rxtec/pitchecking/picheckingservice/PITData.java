package com.rxtec.pitchecking.picheckingservice;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.ImageToolkit;

/**
 * 
 * @author llp
 *
 */

public class PITData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8362192728485137547L;

	private IDCard idCard;
	private Ticket ticket;
	private BufferedImage frame = null;
	private BufferedImage faceImage = null;
	private FaceLocation faceLocation = null;
	private long createTime;
	public float faceCheckResult = 0;
	private float faceDistance = 0;
	private boolean isDetectedFace = false;
	private String pitStation;
	private float facePosePitch;
	private float facePoseRoll;
	private float facePoseYaw;

	public Ticket getTicket() {
		return ticket;
	}

	public void setTicket(Ticket ticket) {
		this.ticket = ticket;
	}

	public BufferedImage getFrame() {
		return frame;
	}

	public void setFrame(BufferedImage frame) {
		this.frame = frame;
	}

	public FaceLocation getFaceLocation() {
		return faceLocation;
	}

	public class FaceLocation {
		private int x;
		private int y;
		private int width;
		private int height;

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}

		public int getWidth() {
			return width;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public int getHeight() {
			return height;
		}

		public void setHeight(int height) {
			this.height = height;
		}

		public Rectangle getFaceBounds() {
			return new Rectangle(x, y, width, height);
		}

		public void setLocation(int x, int y, int w, int h) {
			this.x = x;
			this.y = y;
			this.width = w;
			this.height = h;
		}

		@Override
		public String toString() {
			return "x=" + x + " y=" + y + " width=" + width + " height=" + height;
		}

	}

	public BufferedImage getFaceImage() {
		return faceImage;
	}

	public void setFaceImage(BufferedImage bi) {
		this.faceImage = bi;
	}

	private FaceDetectedResult faceDetectedResult;

	public FaceDetectedResult getFaceDetectedResult() {
		return faceDetectedResult;
	}

	public void setFaceDetectedResult(FaceDetectedResult fdr) {
		this.faceDetectedResult = fdr;
	}

	public IDCard getIdCard() {
		return idCard;
	}

	public void setIdCard(IDCard idCard) {
		this.idCard = idCard;
	}

	public long getCreateTime() {
		return createTime;
	}

	public float getFaceCheckResult() {
		return faceCheckResult;
	}

	public void setFaceCheckResult(float faceCheckResult) {
		this.faceCheckResult = faceCheckResult;

	}

	public float getFaceDistance() {
		return faceDistance;
	}

	public void setFaceDistance(float faceDistance) {
		this.faceDistance = faceDistance;
	}

	private void extractFaceImage(BufferedImage fm) {
		int x = (int) (faceLocation.getX() * 0.85);
		int y = (int) (faceLocation.getY() * 0.85);
		int width = (int) (faceLocation.getWidth() * 1.15);
		int height = (int) (faceLocation.getHeight() * 1.15);
		faceImage = fm.getSubimage(x, y, width, height);
	}

	public PITData(BufferedImage frame) {
		this.frame = frame;
		this.faceLocation = new FaceLocation();
	}

	public void updateFaceLocation(int x, int y, int width, int height) {
		if (width > 50 && height > 50) {
			this.isDetectedFace = true;
			this.faceLocation.setX(x);
			this.faceLocation.setY(y);
			this.faceLocation.setWidth(width);
			this.faceLocation.setHeight(height);
			if (frame != null)
				extractFaceImage(frame);
			createTime = Calendar.getInstance().getTimeInMillis();
		}
	}

	public byte[] getExtractFaceImageBytes() {
		if (!isDetectedFace || faceImage == null)
			return null;

		// BufferedImage bi = ImageToolkit.scale(faceImage, 120, 140, true);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(faceImage, "JPEG", ImageIO.createImageOutputStream(os));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return os.toByteArray();
	}

	public void saveFaceDataToDsk() {
		String dirName = Config.getInstance().getImagesLogDir();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		dirName += formatter.format(new Date());
		String trackedDir = dirName + "/Tracked";
		String passedDir = dirName + "/Passed";
		String failedDir = dirName + "/Failed";

		if (faceCheckResult == 0) {
			saveFrameImage(trackedDir);
			saveFaceImage(trackedDir);
		} else if (faceCheckResult >= Config.getInstance().getFaceCheckThreshold()) {
			saveFrameImage(passedDir);
			saveFaceImage(passedDir);
			saveIDCardImage(passedDir);
		} else {
			saveFrameImage(failedDir);
			saveFaceImage(failedDir);
			saveIDCardImage(failedDir);

		}
	}

	private void saveFrameImage(String dirName) {
		if (frame == null)
			return;
		int ret = CommUtil.createDir(dirName);
		if (ret == 0 || ret == 1) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss-SSS");

			StringBuffer sb = new StringBuffer();
			sb.append(dirName);
			sb.append("/VF");
			if (idCard != null)
				sb.append(idCard.getIdNo().hashCode());
			else
				sb.append("--");
			sb.append("@");
			sb.append(formatter.format(new Date()));
			sb.append(".jpg");
			String fn = sb.toString();
			try {
				ImageIO.write(frame, "JPEG", ImageIO.createImageOutputStream(new File(fn)));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.error("saveToImagesLogFile failed", e);
			}
		}
	}

	private void saveFaceImage(String dirName) {
		if (faceImage == null)
			return;
		int ret = CommUtil.createDir(dirName);
		if (ret == 0 || ret == 1) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss-SSS");
			DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
			df.setMaximumFractionDigits(2);
			StringBuffer sb = new StringBuffer();
			sb.append(dirName);
			sb.append("/FI");
			if (idCard != null)
				sb.append(idCard.getIdNo().hashCode());
			else
				sb.append("--");
			sb.append("@");
			sb.append(formatter.format(new Date()));
			sb.append("$");
			sb.append(df.format(faceCheckResult));
			sb.append(".jpg");
			String fn = sb.toString();
			try {
				ImageIO.write(faceImage, "JPEG", ImageIO.createImageOutputStream(new File(fn)));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.error("saveToImagesLogFile failed", e);
			}
		}
	}

	private void saveIDCardImage(String dirName) {
		if (idCard == null || idCard.getCardImage() == null || idCard.getIdNo() == null)
			return;
		BufferedImage bi = idCard.getCardImage();

		int ret = CommUtil.createDir(dirName);
		if (ret == 0 || ret == 1) {
			StringBuffer sb = new StringBuffer();
			sb.append(dirName);
			sb.append("/ID");
			sb.append(idCard.getIdNo().hashCode());
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

	public boolean isDetectedFace() {
		return isDetectedFace;
	}

	public void setDetectedFace(boolean isDetectedFace) {
		this.isDetectedFace = isDetectedFace;
	}

	public String getPitStation() {
		return pitStation;
	}

	public void setPitStation(String pitStation) {
		this.pitStation = pitStation;
	}

	public float getFacePosePitch() {
		return facePosePitch;
	}

	public void setFacePosePitch(float facePosePitch) {
		this.facePosePitch = facePosePitch;
	}

	public float getFacePoseRoll() {
		return facePoseRoll;
	}

	public void setFacePoseRoll(float facePoseRoll) {
		this.facePoseRoll = facePoseRoll;
	}

	public float getFacePoseYaw() {
		return facePoseYaw;
	}

	public void setFacePoseYaw(float facePoseYaw) {
		this.facePoseYaw = facePoseYaw;
	}

	@Override
	public String toString() {
		return "VerifyResult=" + this.faceCheckResult;
	}

}

class FaceDataComparator implements Comparator {
	public int compare(Object obj1, Object obj2) {
		return 0;
	}
}
