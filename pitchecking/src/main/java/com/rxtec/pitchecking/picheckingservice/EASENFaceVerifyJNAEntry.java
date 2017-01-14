package com.rxtec.pitchecking.picheckingservice;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.device.easen.EasenVerifyResult;
import com.rxtec.pitchecking.device.easen.Main.SDKMethod;
import com.sun.jna.Library;
import com.sun.jna.Native;

public class EASENFaceVerifyJNAEntry implements FaceVerifyInterface {
	private Logger log = LoggerFactory.getLogger("EASENFaceVerifyJNAEntry");

	private int initStatus = 0;

	@Override
	public int getInitStatus() {
		return initStatus;
	}

	public EASENFaceVerifyJNAEntry() {
		// TODO Auto-generated constructor stub
		this.init();
	}

	public enum SDK_ERROR {
		SDK_NO_ERROR, SDK_ACTIVATION_SERIAL_UNKNOWN, SDK_ACTIVATION_EXPIRED,

		SDK_NOT_INITIALIZED, SDK_ALREADY_INITIALIZED, SDK_NO_RES_FILE, SDK_BAD_THRESHOLD_XML, SDK_NOT_FACE_DETECTED,

		SDK_NONE_IDCARD_SET, SDK_BAD_PARAMETER, SDK_NOT_ID_PHOTO, SDK_DONGLE_ERROR
	}

	public interface SDKMethod extends Library {

		SDKMethod INSTANCE = (SDKMethod) Native.loadLibrary("idVerificationSDK.dll", SDKMethod.class);

		int getCurrentHWID(byte[] hwidBytes, int size);

		int setActivation(byte[] licenseBytes);

		int initializeSDK(byte[] pathBytes);

		int finalizeSDK();

		int setIDCardPhoto(byte[] idImgPtr, int width, int height);

		int match(byte[] idImgPtr, int width, int height, float[] matchinfgScore, int[] yesNo, float[] matchingTime,
				int[] faceX, int[] faceY, int[] faceWidth, int[] faceHeight);

		int detectFace(byte[] imgData, int width, int height, int[] faceX, int[] faceY, int[] faceWidth,
				int[] faceHeight);

		public final int SDK_NO_ERROR = 0;
		public final int SDK_ACTIVATION_SERIAL_UNKNOW = 1;
		public final int SDK_ACTIVATION_EXPIRED = 2;

		public final int SDK_NOT_INITIALIZED = 3;
		public final int SDK_ALREADY_INITIALIZED = 4;
		public final int SDK_NO_RES_FILE = 5;
		public final int SDK_BAD_THRESHOLD_XML = 6;
		public final int SDK_NOT_FACE_DETECTED = 7;

		public final int SDK_NONE_IDCARD_SET = 8;
		public final int SDK_BAD_PARAMETER = 9;
		public final int SDK_NOT_ID_PHOTO = 10;
		public final int SDK_DONGLE_ERROR = 11;

	}

	/**
	 * 
	 */
	private void init() {
		log.debug("config.path==" + Config.getInstance().getEasenConfigPath());
		int ret;
		byte[] licenseBytes = null;
		try {
			log.info("currentHwid==" + getCurrentHWID(256));

			File file = new File(Config.getInstance().getEasenActivationFile());
			log.info("getEasenActivationFile=="+Config.getInstance().getEasenActivationFile());
			FileInputStream fis = new FileInputStream(file);
			licenseBytes = new byte[(int) file.length() + 1];
			Arrays.fill(licenseBytes, (byte) 0);
			fis.read(licenseBytes);
			fis.close();

			ret = SDKMethod.INSTANCE.setActivation(licenseBytes);
			log.info("Activation ret = " + ret);
			if (ret == SDKMethod.SDK_NO_ERROR) {
				String strPath = Config.getInstance().getEasenConfigPath();
				byte[] path = strPath.getBytes("UTF-16LE");
				byte[] pathBytes = new byte[path.length + 2];
				Arrays.fill(pathBytes, (byte) 0);
				System.arraycopy(path, 0, pathBytes, 0, path.length);

				ret = SDKMethod.INSTANCE.initializeSDK(pathBytes);
				log.info("initializeSDK ret = " + ret);
				log.info("初始化Easen initializeSDK 成功!  ret: " + ret);// 获取返回值
				if (ret != SDKMethod.SDK_NO_ERROR) {
					log.error("初始化Easen initializeSDK Failed!");
				} else {
					initStatus = 1;
				}
			}else{
				log.error("Easen setActivation Failed!");
			}
		} catch (Exception e) {
			log.error("init:", e);
		}
	}

	/**
	 * 
	 * @param nSize
	 * @return
	 */
	public String getCurrentHWID(int nSize) {
		String szID = "";
		SDKMethod sdkMethod = SDKMethod.INSTANCE;
		byte[] hwidBytes = new byte[nSize];

		try {
			sdkMethod.getCurrentHWID(hwidBytes, nSize);
			szID = fromByteArray(hwidBytes);
		} catch (Exception e) {
			log.error("getCurrentHWID:", e);
		}

		return szID;
	}

	/**
	 * 处理身份证照片
	 * 
	 * @param idCardImg
	 * @param nWidth
	 * @param nHeight
	 */
	public int setIDCardPhoto(BufferedImage idCardImg) {
		log.info("starting setIDCardPhoto...");
		int retval = -1;
		byte[] pixels = ((DataBufferByte) idCardImg.getRaster().getDataBuffer()).getData();
		byte[] rgbPixels = ConvertToEngineBuffer(pixels, idCardImg.getWidth(), idCardImg.getHeight());

		retval = SDKMethod.INSTANCE.setIDCardPhoto(rgbPixels, idCardImg.getWidth(), idCardImg.getHeight());
		log.info("retval==" + retval);
		if (retval == SDKMethod.SDK_NO_ERROR) {
			log.info("setIDCardPhoto succfully!");
		} else if (retval == SDKMethod.SDK_NOT_FACE_DETECTED) {
			log.info("Face Detection Failed!");
		} else if (retval == SDKMethod.SDK_NOT_ID_PHOTO) {
			log.info("Not ID Photo!!");
		}
		return retval;
	}

	/**
	 * 做setIDCardPhoto来处理过的身份证照片和现场照上的人脸比对
	 * 
	 * @param faceImg
	 * @param verifyResult
	 * @return
	 */
	public float verify(byte[] faceImgBytes) {
		log.info("starting match with easen...");
		float result = 0;
		int ret = -1;
		BufferedImage faceImg = ByteToBufferedImg(faceImgBytes);
		byte[] pixels = ((DataBufferByte) faceImg.getRaster().getDataBuffer()).getData();
		byte[] rgbPixels = ConvertToEngineBuffer(pixels, faceImg.getWidth(), faceImg.getHeight());

		float[] matchingScore = new float[1];
		int[] yesNo = new int[1];
		float[] matchingTime = new float[1];
		int[] faceX = new int[1];
		int[] faceY = new int[1];
		int[] faceWidth = new int[1];
		int[] faceHeight = new int[1];
		ret = SDKMethod.INSTANCE.match(rgbPixels, faceImg.getWidth(), faceImg.getHeight(), matchingScore, yesNo,
				matchingTime, faceX, faceY, faceWidth, faceHeight);
		log.info("retval==" + ret);
		if (ret == SDKMethod.SDK_NO_ERROR) {
			log.info("match succfully!");
			result = matchingScore[0] / 100;
			log.info("verifyResult==" + result + ",matchTime==" + matchingTime[0]);
			// verifyResult.setMatchingScore(matchingScore);
			// verifyResult.setYesNo(yesNo);
			// verifyResult.setMatchingTime(matchingTime);
			// verifyResult.setFaceX(faceX);
			// verifyResult.setFaceY(faceY);
			// verifyResult.setFaceWidth(faceWidth);
			// verifyResult.setFaceHeight(faceHeight);

		} else if (ret == SDKMethod.SDK_NOT_FACE_DETECTED) {
			log.info("Face Detection Failed!");
		} else if (ret == SDKMethod.SDK_NONE_IDCARD_SET) {
			log.info("None ID Photo Set!");
		}

		return result;
	}

	@Override
	public float verify(byte[] faceImgBytes, byte[] idCardBytes) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * 
	 * @param byteArray
	 * @return
	 */
	public static BufferedImage ByteToBufferedImg(byte[] byteArray) {
		BufferedImage image = null;
		try {
			ByteArrayInputStream in = new ByteArrayInputStream(byteArray); // 将b作为输入流；
			image = ImageIO.read(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // 将in作为输入流，读取图片存入image中，而这里in可以为ByteArrayInputStream();
		return image;
	}

	/**
	 * 
	 * @param array
	 * @return
	 */
	public static String fromByteArray(byte[] array) {
		int realLen = 0;

		for (int i = 0; i < array.length; i++) {
			if (array[i] != 0)
				realLen++;
			else
				break;
		}

		byte[] realStr = new byte[realLen];
		System.arraycopy(array, 0, realStr, 0, realLen);

		return new String(realStr);
	}

	/**
	 * 
	 * @param pixels
	 * @param width
	 * @param height
	 * @return
	 */
	public static byte[] ConvertToEngineBuffer(byte[] pixels, int width, int height) {
		byte[] dist = new byte[pixels.length];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				dist[(y * width + x) * 3 + 0] = pixels[(width * y + x) * 3 + 2];
				dist[(y * width + x) * 3 + 1] = pixels[(width * y + x) * 3 + 1];
				dist[(y * width + x) * 3 + 2] = pixels[(width * y + x) * 3 + 0];
			}
		}

		return dist;
	}

	public static void main(String[] args) {

		// EASENFaceVerifyJNAEntry easenVerify = new EASENFaceVerifyJNAEntry();
		// System.out.println("getInitStatus==" + easenVerify.getInitStatus());
		// System.out.println("" + easenVerify.getCurrentHWID(256));
		//
		// try {
		// File file1 = new File("zp.jpg");// new
		// // File("D:/pitchecking/images/ID230103198206231618.jpg");
		// BufferedImage srcImg1 = ImageIO.read(file1);
		// easenVerify.setIDCardPhoto(srcImg1);
		//
		// File faceFile1 = new
		// File("D:/pitchecking/images/FA230103198206231618@2016-11-21
		// 04-08-05-700$0.83.jpg");
		// BufferedImage faceImg1 = ImageIO.read(faceFile1);
		// EasenVerifyResult verifyResult1 = new EasenVerifyResult();
		// float result = 0;
		// result = easenVerify.verify(faceImg1);
		// System.out.println("result==" + result);
		// // System.out.println("getMatchingScore==" +
		// // verifyResult1.getMatchingScore()[0]);
		// // System.out.println("getYesNo==" + verifyResult1.getYesNo()[0]);
		// // System.out.println("getMatchingTime==" +
		// // verifyResult1.getMatchingTime()[0]);
		// // System.out.println("getFaceX==" + verifyResult1.getFaceX()[0]);
		// // System.out.println("getFaceY==" + verifyResult1.getFaceY()[0]);
		// // System.out.println("getFaceWidth==" +
		// // verifyResult1.getFaceWidth()[0]);
		// // System.out.println("getFaceHeight==" +
		// // verifyResult1.getFaceHeight()[0]);
		// //
		// System.out.println("---------------------------------------------");
		// File file2 = new
		// File("D:/pitchecking/images/ID430404197202242067.jpg");
		// BufferedImage srcImg2 = ImageIO.read(file2);
		// easenVerify.setIDCardPhoto(srcImg2);
		//
		// File faceFile2 = new
		// File("D:/pitchecking/images/VF430404197202242067@2016-11-21
		// 04-08-23-846.jpg");
		// BufferedImage faceImg2 = ImageIO.read(faceFile2);
		// EasenVerifyResult verifyResult2 = new EasenVerifyResult();
		// result = easenVerify.verify(faceImg2);
		// System.out.println("result==" + result);
		// // System.out.println("getMatchingScore==" +
		// // verifyResult2.getMatchingScore()[0]);
		// // System.out.println("getYesNo==" + verifyResult2.getYesNo()[0]);
		// // System.out.println("getMatchingTime==" +
		// // verifyResult2.getMatchingTime()[0]);
		// // System.out.println("getFaceX==" + verifyResult2.getFaceX()[0]);
		// // System.out.println("getFaceY==" + verifyResult2.getFaceY()[0]);
		// // System.out.println("getFaceWidth==" +
		// // verifyResult2.getFaceWidth()[0]);
		// // System.out.println("getFaceHeight==" +
		// // verifyResult2.getFaceHeight()[0]);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

}
