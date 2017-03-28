package com.rxtec.pitchecking.picheckingservice;

import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jacob.com.Variant;
import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.db.MongoDB;
import com.rxtec.pitchecking.db.PitRecordLoger;
import com.rxtec.pitchecking.db.mysql.PitRecordSqlLoger;
import com.rxtec.pitchecking.mq.police.dao.PITInfoImgPath;
import com.rxtec.pitchecking.utils.CalUtils;
import com.rxtec.pitchecking.utils.CommUtil;

public class FaceImageLog {
	static Logger log = LoggerFactory.getLogger("FaceImageLog");

	public static void clearFaceLogs() {
		int days = Config.getInstance().getFaceLogRemainDays();
		SimpleDateFormat sFormat = new SimpleDateFormat("yyyyMMdd");

		Date today = new Date();
		String dn = sFormat.format(new Date(today.getTime() - days * 24 * 60 * 60 * 1000));

		File imgDir = new File(Config.getInstance().getImagesLogDir());

		if (imgDir.exists()) {
			File dirs[] = imgDir.listFiles();
			for (File d : dirs) {
				if (d.isDirectory()) {
					if (d.getName().compareTo(dn) < 0) {
						if (CommUtil.deleteDir(d)) {
							System.out.println("Remove face log dir " + d.getAbsolutePath());
						}
					}
				}
			}
		}

	}

	/**
	 * 将人脸数据存储在个公安处
	 * 
	 * @param fd
	 * @param usingTime
	 */
	public static void saveFaceDataToPoliceDsk(PITVerifyData fd, PITInfoImgPath imgPath) {
		try {
			String dirName = Config.getInstance().getImagesLogDir();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
			dirName += formatter.format(new Date());
			String trackedDir = dirName + "/Tracked";
			String passedDir = dirName + "/Passed";
			String failedDir = dirName + "/Failed";

			float result = fd.getVerifyResult();
			String vfFileName = "", fiFileName = "", idFileName = "";
			if (result >= Config.getInstance().getFaceCheckThreshold()) {
				vfFileName = getPoliceImageFileName("VF", passedDir, fd);
				fiFileName = getPoliceImageFileName("FA", passedDir, fd);
				idFileName = getPoliceImageFileName("ID", passedDir, fd);

				// log.debug("vfFileName==" + vfFileName);
				// log.debug("fiFileName==" + fiFileName);
				// log.debug("idFileName==" + idFileName);
				if (Config.getInstance().getIsSaveFaceImageToLocaldisk() == 1) {
					saveFrameImage(passedDir, vfFileName, fd);
					saveFaceImage(passedDir, fiFileName, fd);
					saveIDCardImage(passedDir, idFileName, fd);
				}
			} else if (result < Config.getInstance().getFaceCheckThreshold() && result > 0) {
				vfFileName = getPoliceImageFileName("VF", failedDir, fd);
				fiFileName = getPoliceImageFileName("FA", failedDir, fd);
				idFileName = getPoliceImageFileName("ID", failedDir, fd);

				// log.debug("vfFileName==" + vfFileName);
				// log.debug("fiFileName==" + fiFileName);
				// log.debug("idFileName==" + idFileName);
				if (Config.getInstance().getIsSaveFaceImageToLocaldisk() == 1) {
					saveFrameImage(failedDir, vfFileName, fd);
					saveFaceImage(failedDir, fiFileName, fd);
					saveIDCardImage(failedDir, idFileName, fd);
				}
			}

			if (imgPath != null) {
				imgPath.setVfImagePath(vfFileName);
				imgPath.setFdImagePath(fiFileName);
				imgPath.setIdImagePath(idFileName);
			}

			// offer进mongodb的处理队列
			if (Config.getInstance().getIsUseMongoDB() == 1) {
				// log.debug("offer mongoDB:vfFileName==" + vfFileName);
				// log.debug("offer mongoDB:fiFileName==" + fiFileName);
				// log.debug("offer mongoDB:idFileName==" + idFileName);

				fd.setFrameImg(vfFileName.getBytes());
				fd.setFaceImg(fiFileName.getBytes());
				fd.setIdCardImg(idFileName.getBytes());
				PitRecordLoger.getInstance().offer(fd);
				FaceVerifyServiceStatistics.getInstance().update(result, 600, fd.getFaceDistance());
			}
			// offer into MySQL的处理队列
			if (Config.getInstance().getIsUseMySQLDB() == 1) {
				if (result > 0) {
					// log.debug("offer MySQLDB:vfFileName==" + vfFileName);
					// log.debug("offer MySQLDB:fiFileName==" + fiFileName);
					// log.debug("offer MySQLDB:idFileName==" + idFileName);

					fd.setFrameImg(vfFileName.getBytes());
					fd.setFaceImg(fiFileName.getBytes());
					fd.setIdCardImg(idFileName.getBytes());
					PitRecordSqlLoger.getInstance().offer(fd);
					FaceVerifyServiceStatistics.getInstance().update(result, 600, fd.getFaceDistance());
				}
			}
		} catch (Exception ex) {
			log.error("saveFaceDataToDskAndMongoDB:", ex);
		}
	}

	/**
	 * 将人脸数据存盘和存mongodb
	 * 
	 * @param fd
	 */
	public static void saveFaceDataToDskAndMongoDB(PITVerifyData fd, int usingTime) {
		try {
			if ((Config.getInstance().getIsSaveFaceImageToLocaldisk() == 1)) {
				String dirName = Config.getInstance().getImagesLogDir();
				SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
				dirName += formatter.format(new Date());
				String trackedDir = dirName + "/Tracked";
				String passedDir = dirName + "/Passed";
				String failedDir = dirName + "/Failed";

				float result = fd.getVerifyResult();
				String vfFileName = "", fiFileName = "", idFileName = "";
				if (result >= Config.getInstance().getFaceCheckThreshold()) {
					vfFileName = getImageFileName("VF", passedDir, fd);
					fiFileName = getImageFileName("FI", passedDir, fd);
					idFileName = getImageFileName("ID", passedDir, fd);

					// log.debug("vfFileName==" + vfFileName);
					// log.debug("fiFileName==" + fiFileName);
					// log.debug("idFileName==" + idFileName);

					saveFrameImage(passedDir, vfFileName, fd);
					saveFaceImage(passedDir, fiFileName, fd);
					saveIDCardImage(passedDir, idFileName, fd);
				} else if (result < Config.getInstance().getFaceCheckThreshold() && result > 0) {
					vfFileName = getImageFileName("VF", failedDir, fd);
					fiFileName = getImageFileName("FI", failedDir, fd);
					idFileName = getImageFileName("ID", failedDir, fd);

					// log.debug("vfFileName==" + vfFileName);
					// log.debug("fiFileName==" + fiFileName);
					// log.debug("idFileName==" + idFileName);

					saveFrameImage(failedDir, vfFileName, fd);
					saveFaceImage(failedDir, fiFileName, fd);
					saveIDCardImage(failedDir, idFileName, fd);
				}
				// offer进mongodb的处理队列
				if (Config.getInstance().getIsUseMongoDB() == 1) {
					// log.debug("offer mongoDB:vfFileName==" + vfFileName);
					// log.debug("offer mongoDB:fiFileName==" + fiFileName);
					// log.debug("offer mongoDB:idFileName==" + idFileName);

					fd.setFrameImg(vfFileName.getBytes());
					fd.setFaceImg(fiFileName.getBytes());
					fd.setIdCardImg(idFileName.getBytes());
					PitRecordLoger.getInstance().offer(fd);
					FaceVerifyServiceStatistics.getInstance().update(result, usingTime, fd.getFaceDistance());
				}
			}
		} catch (Exception ex) {
			log.error("saveFaceDataToDskAndMongoDB:", ex);
		}
	}

	/**
	 * 
	 * @param fd
	 * @param usingTime
	 */
	public static void saveFaceDataToMySQL(PITVerifyData fd, int usingTime) {
		try {
			float result = fd.getVerifyResult();
			String vfFileName = "", fiFileName = "", idFileName = "";
			// offer into MySQL的处理队列
			if (Config.getInstance().getIsUseMySQLDB() == 1) {
				if (result > 0) {
					// log.debug("offer MySQLDB:vfFileName==" + vfFileName);
					// log.debug("offer MySQLDB:fiFileName==" + fiFileName);
					// log.debug("offer MySQLDB:idFileName==" + idFileName);

					fd.setFrameImg(vfFileName.getBytes());
					fd.setFaceImg(fiFileName.getBytes());
					fd.setIdCardImg(idFileName.getBytes());
					PitRecordSqlLoger.getInstance().offer(fd);
					FaceVerifyServiceStatistics.getInstance().update(result, usingTime, fd.getFaceDistance());
				}
			}
		} catch (Exception ex) {
			log.error("saveFaceDataToMySQL:", ex);
		}
	}

	/**
	 * 将人脸数据存盘
	 * 
	 * @param fd
	 */
	public static void saveFaceDataToDsk(PITVerifyData fd) {
		if ((Config.getInstance().getIsSaveFaceImageToLocaldisk() == 1)) {
			String dirName = Config.getInstance().getImagesLogDir();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
			dirName += formatter.format(new Date());
			String trackedDir = dirName + "/Tracked";
			String passedDir = dirName + "/Passed";
			String failedDir = dirName + "/Failed";

			float result = fd.getVerifyResult();
			if (result >= Config.getInstance().getFaceCheckThreshold()) {
				saveFrameImage(passedDir, fd);
				saveFaceImage(passedDir, fd);
				saveIDCardImage(passedDir, fd);
			} else if (result < Config.getInstance().getFaceCheckThreshold() && result > 0) {
				saveFrameImage(failedDir, fd);
				saveFaceImage(failedDir, fd);
				saveIDCardImage(failedDir, fd);
			}
		}
	}

	/**
	 * a)?身份证(身份证号码进行加密处理)：身份证号码.bmp，存放目录名“Id_card”
	 * b)?图像照片(分辨率不低于1920*1080，全身图像)：身份证号码_1.jpg、身份证号码_2.jpg、身份证号码_3.jpg，存放目录名“
	 * Photo”
	 * 
	 * @param fd
	 */
	public static void saveFaceDataToDskByTK(PITVerifyData fd) {
		// log.info("准备存储照片saveFaceDataToDskByTK");
		String dirName = Config.getInstance().getImagesLogDir();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		dirName += formatter.format(new Date());

		String idcardDir = dirName + "/Id_card/";
		String photoDir = dirName + "/Photo/";
		String faceDir = dirName + "/Faces/";

		float result = fd.getVerifyResult();
		saveFrameImageByTK(photoDir, fd);
		saveIDCardImageByTK(idcardDir, fd);
		saveFaceImageByTK(faceDir, fd);
	}

	/**
	 * 写通道帧图像
	 * 
	 * @param dirName
	 * @param fd
	 */
	private static void saveFrameImage(String dirName, PITVerifyData fd) {
		if (fd == null || fd.getFrameImg() == null)
			return;
		int ret = CommUtil.createDir(dirName);
		if (ret == 0 || ret == 1) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss-SSS");

			StringBuffer sb = new StringBuffer();
			sb.append(dirName);
			sb.append("/VF");
			if (fd.getIdNo() != null)
				sb.append(fd.getIdNo().hashCode());
			else
				sb.append("--");
			sb.append("@");
			sb.append(formatter.format(new Date()));
			sb.append(".jpg");
			String fn = sb.toString();
			DataOutputStream out;
			if (fd.getFaceImg() != null) {
				try {
					out = new DataOutputStream(new FileOutputStream(fn));
					out.write(fd.getFrameImg());
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 图像照片(分辨率不低于1920*1080，全身图像)：身份证号码_1.jpg、身份证号码_2.jpg、身份证号码_3.jpg，存放目录名“
	 * Photo”
	 * 
	 * @param dirName
	 * @param fd
	 */
	private static void saveFrameImageByTK(String dirName, PITVerifyData fd) {
		if (fd == null || fd.getFrameImg() == null)
			return;
		int ret = CommUtil.createDir(dirName);
		if (ret == 0 || ret == 1) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss-SSS");

			String fn = "";
			for (int k = 1; k <= 3; k++) {
				StringBuffer sb = new StringBuffer();
				sb.append(dirName);
				sb.append(fd.getIdNo().hashCode());
				sb.append("_");
				sb.append(k);
				sb.append(".jpg");
				fn = sb.toString();
				if (!new File(fn).exists()) {
					break;
				}
			}
			if (fn == null || fn.equals("")) {
				return;
			}
			DataOutputStream out;
			if (fd.getFaceImg() != null) {
				try {
					out = new DataOutputStream(new FileOutputStream(fn));
					out.write(fd.getFrameImg());
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 
	 * @param dirName
	 * @param fd
	 */
	private static void saveFaceImageByTK(String dirName, PITVerifyData fd) {
		if (fd == null || fd.getFaceImg() == null)
			return;
		int ret = CommUtil.createDir(dirName);
		if (ret == 0 || ret == 1) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss-SSS");

			String fn = "";
			for (int k = 1; k <= 3; k++) {
				StringBuffer sb = new StringBuffer();
				sb.append(dirName);
				sb.append(fd.getIdNo().hashCode());
				sb.append("_");
				sb.append(k);
				sb.append(".jpg");
				fn = sb.toString();
				if (!new File(fn).exists()) {
					break;
				}
			}
			if (fn == null || fn.equals("")) {
				return;
			}
			DataOutputStream out;
			if (fd.getFaceImg() != null) {
				try {
					out = new DataOutputStream(new FileOutputStream(fn));
					out.write(fd.getFaceImg());
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 
	 * @param dirName
	 * @param fileName
	 * @param fd
	 */
	private static void saveFrameImage(String dirName, String fileName, PITVerifyData fd) {
		if (fd == null || fd.getFrameImg() == null)
			return;
		int ret = CommUtil.createDir(dirName);
		if (ret == 0 || ret == 1) {
			String fn = fileName;
			DataOutputStream out;
			if (fd.getFaceImg() != null) {
				try {
					out = new DataOutputStream(new FileOutputStream(fn));
					out.write(fd.getFrameImg());
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 写人脸图像
	 * 
	 * @param dirName
	 * @param fd
	 */
	private static void saveFaceImage(String dirName, PITVerifyData fd) {
		if (fd == null || fd.getFaceImg() == null)
			return;
		int ret = CommUtil.createDir(dirName);
		if (ret == 0 || ret == 1) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss-SSS");
			DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
			df.setMaximumFractionDigits(2);
			StringBuffer sb = new StringBuffer();
			sb.append(dirName);
			sb.append("/FI");
			if (fd.getIdNo() != null)
				sb.append(fd.getIdNo().hashCode());
			else
				sb.append("--");
			sb.append("@");
			sb.append(formatter.format(new Date()));
			sb.append("$");
			sb.append(df.format(fd.getVerifyResult()));
			sb.append(".jpg");
			String fn = sb.toString();

			DataOutputStream out;
			if (fd.getFaceImg() != null) {
				try {
					out = new DataOutputStream(new FileOutputStream(fn));
					out.write(fd.getFaceImg());
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 
	 * @param dirName
	 * @param fileName
	 * @param fd
	 */
	private static void saveFaceImage(String dirName, String fileName, PITVerifyData fd) {
		if (fd == null || fd.getFaceImg() == null)
			return;
		int ret = CommUtil.createDir(dirName);
		if (ret == 0 || ret == 1) {
			String fn = fileName;

			DataOutputStream out;
			if (fd.getFaceImg() != null) {
				try {
					out = new DataOutputStream(new FileOutputStream(fn));
					out.write(fd.getFaceImg());
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 写身份证照片
	 * 
	 * @param dirName
	 * @param fd
	 */
	private static void saveIDCardImage(String dirName, PITVerifyData fd) {
		if (fd == null || fd.getIdCardImg() == null)
			return;

		int ret = CommUtil.createDir(dirName);
		if (ret == 0 || ret == 1) {
			StringBuffer sb = new StringBuffer();
			sb.append(dirName);
			sb.append("/ID");
			sb.append(fd.getIdNo().hashCode());
			sb.append(".jpg");
			String fn = sb.toString();
			DataOutputStream out;
			if (fd.getFaceImg() != null) {
				try {
					out = new DataOutputStream(new FileOutputStream(fn));
					out.write(fd.getIdCardImg());
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 按中铁程测试统一路径存放身份证照片 身份证(身份证号码进行加密处理)：身份证号码.bmp，存放目录名“Id_card”
	 * 
	 * @param dirName
	 * @param fd
	 */
	private static void saveIDCardImageByTK(String dirName, PITVerifyData fd) {
		if (fd == null || fd.getIdCardImg() == null)
			return;

		int ret = CommUtil.createDir(dirName);
		if (ret == 0 || ret == 1) {
			StringBuffer sb = new StringBuffer();
			sb.append(dirName);
			sb.append(fd.getIdNo().hashCode());
			sb.append(".bmp");
			String fn = sb.toString();
			DataOutputStream out;
			if (fd.getFaceImg() != null) {
				try {
					out = new DataOutputStream(new FileOutputStream(fn));
					out.write(fd.getIdCardImg());
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 
	 * @param dirName
	 * @param fileName
	 * @param fd
	 */
	private static void saveIDCardImage(String dirName, String fileName, PITVerifyData fd) {
		if (fd == null || fd.getIdCardImg() == null)
			return;

		int ret = CommUtil.createDir(dirName);
		if (ret == 0 || ret == 1) {
			String fn = fileName;
			DataOutputStream out;
			if (fd.getFaceImg() != null) {
				try {
					out = new DataOutputStream(new FileOutputStream(fn));
					out.write(fd.getIdCardImg());
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 
	 * @param imageFlag
	 * @param dirName
	 * @param fd
	 * @return
	 */
	public static String getImageFileName(String imageFlag, String dirName, PITVerifyData fd) {
		String fileName = "";
		if (imageFlag.equals("VF")) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss-SSS");
			StringBuffer sb = new StringBuffer();
			sb.append(dirName);
			sb.append("/VF");
			if (fd.getIdNo() != null)
				sb.append(fd.getIdNo().hashCode());
			else
				sb.append("--");
			sb.append("@");
			sb.append(formatter.format(new Date()));
			sb.append(".jpg");
			fileName = sb.toString();
		} else if (imageFlag.equals("FI")) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss-SSS");
			DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
			df.setMaximumFractionDigits(2);
			StringBuffer sb = new StringBuffer();
			sb.append(dirName);
			sb.append("/FI");
			if (fd.getIdNo() != null)
				sb.append(fd.getIdNo().hashCode());
			else
				sb.append("--");
			sb.append("@");
			sb.append(formatter.format(new Date()));
			sb.append("$");
			sb.append(df.format(fd.getVerifyResult()));
			sb.append(".jpg");
			fileName = sb.toString();
		} else if (imageFlag.equals("ID")) {
			StringBuffer sb = new StringBuffer();
			sb.append(dirName);
			sb.append("/ID");
			sb.append(fd.getIdNo().hashCode());
			sb.append(".jpg");
			fileName = sb.toString();
		} else {
			fileName = "";
		}
		return fileName;
	}

	/**
	 * 公安处文件名取名
	 * 
	 * @param imageFlag
	 * @param dirName
	 * @param fd
	 * @return
	 */
	public static String getPoliceImageFileName(String imageFlag, String dirName, PITVerifyData fd) {
		String fileName = "";
		if (imageFlag.equals("VF")) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss-SSS");
			StringBuffer sb = new StringBuffer();
			sb.append(dirName);
			sb.append("/VF");
			if (fd.getIdNo() != null)
				sb.append(fd.getIdNo());
			else
				sb.append("--");
			sb.append("@");
			sb.append(formatter.format(new Date()));
			sb.append(".jpg");
			fileName = sb.toString();
		} else if (imageFlag.equals("FA")) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss-SSS");
			DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
			df.setMaximumFractionDigits(2);
			StringBuffer sb = new StringBuffer();
			sb.append(dirName);
			sb.append("/FA");
			if (fd.getIdNo() != null)
				sb.append(fd.getIdNo());
			else
				sb.append("--");
			sb.append("@");
			sb.append(formatter.format(new Date()));
			sb.append("$");
			sb.append(df.format(fd.getVerifyResult()));
			sb.append(".jpg");
			fileName = sb.toString();
		} else if (imageFlag.equals("ID")) {
			StringBuffer sb = new StringBuffer();
			sb.append(dirName);
			sb.append("/ID");
			sb.append(fd.getIdNo());
			sb.append(".jpg");
			fileName = sb.toString();
		} else {
			fileName = "";
		}
		return fileName;
	}

	/**
	 * 
	 * @param dirName
	 * @param frameImage
	 * @param luminance
	 */
	public static void saveImageFromFrame(String dirName, BufferedImage frameImage, float luminance) {
		if (frameImage == null)
			return;
		int ret = CommUtil.createDir(dirName);
		if (ret == 0 || ret == 1) {
			StringBuffer sb = new StringBuffer();
			sb.append(dirName);
			sb.append("/VF-");
			sb.append("" + luminance);
			sb.append("@");
			sb.append(CalUtils.getStringDateShort());
			sb.append(" ");
			sb.append(CalUtils.getStringFullTimeHaomiao());
			sb.append(".jpg");
			String fn = sb.toString();
			DataOutputStream out;
			try {
				out = new DataOutputStream(new FileOutputStream(fn));
				out.write(CommUtil.getImageBytesFromImageBuffer(frameImage));
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		// FaceImageLog.clearFaceLogs();
		// System.out.println(FaceImageLog.getImageFileName("VF", dirName, fd));
	}
}
