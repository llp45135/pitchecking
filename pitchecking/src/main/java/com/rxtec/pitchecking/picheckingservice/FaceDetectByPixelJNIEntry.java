package com.rxtec.pitchecking.picheckingservice;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import org.jfree.util.Log;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xvolks.jnative.JNative;
import org.xvolks.jnative.Type;
import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.pointers.Pointer;
import org.xvolks.jnative.pointers.memory.MemoryBlockFactory;

import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.ImageToolkit;

public class FaceDetectByPixelJNIEntry {

	JNative FGDetectInitSDK = null;
	JNative FGDetectCreateHandleFromFile = null;
	JNative FGDetectSImageQuality = null;
	JNative FGDetectSGetFacePoint = null;
	JNative FGDetectSGetResult = null;
	JNative FGDetectSGetAssess = null;
	JNative FGDetectImageFaceLocation = null;

	int JNIHandle;
	private Logger log = LoggerFactory.getLogger("FaceLocationDetection");

	public static void main(String[] args) {

		Runnable run1 = new Runnable() {

			@Override
			public void run() {

				while (true) {
					// TODO Auto-generated method stub

					try {
						BufferedImage bi = ImageIO.read(new File("C:/pitchecking/llp.jpg"));
						FaceData fd = new FaceData(bi);
						FaceDetectByPixelJNIEntry detecter = FaceDetectByPixelJNIEntry.getInstance();
						detecter.detectFaceImage(fd);
						System.out.println(fd.getFaceDetectedResult());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};

		ExecutorService executer = Executors.newCachedThreadPool();
		executer.execute(run1);

	}

	private static FaceDetectByPixelJNIEntry instance = null;

	public static synchronized FaceDetectByPixelJNIEntry getInstance() {
		if (instance == null) {
			instance = new FaceDetectByPixelJNIEntry();
		}

		return instance;
	}

	private FaceDetectByPixelJNIEntry() {
		JNative.setLoggingEnabled(false);
		/**
		 * 初始化SDK
		 */
		initJNIContext();

	}

	private int initJNIContext() {
		int result = 0;
		try {
			FGDetectInitSDK = new JNative("DetectSDK.dll", "FGDetectInitSDK");
			FGDetectInitSDK.setRetVal(Type.INT);
			FGDetectInitSDK.invoke();
			log.debug("FGDetectInitSDK:" + FGDetectInitSDK.getRetValAsInt());

			if (FGDetectInitSDK.getRetValAsInt() == 0) {

				Pointer handle = new Pointer(MemoryBlockFactory.createMemoryBlock(4));
				handle.setIntAt(0, 0);
				int i = 0;
				FGDetectImageFaceLocation = new JNative("DetectSDK.dll", "FGDetectImageFaceLocation");

				FGDetectCreateHandleFromFile = new JNative("DetectSDK.dll", "FGDetectCreateHandleFromFile");
				FGDetectCreateHandleFromFile.setRetVal(Type.INT);
				FGDetectCreateHandleFromFile.setParameter(i++, handle);
				FGDetectCreateHandleFromFile.setParameter(i++, Type.STRING, "c:/pitchecking/FaceDetection.flt");
				FGDetectCreateHandleFromFile.invoke();
				log.debug("FGDetectCreateHandleFromFile:" + FGDetectCreateHandleFromFile.getRetVal());
				JNIHandle = handle.getAsInt(0);
				handle.dispose();
				if (FGDetectCreateHandleFromFile.getRetValAsInt() == 0) {
					FGDetectSImageQuality = new JNative("DetectSDK.dll", "FGDetectSImageQuality");
					FGDetectSGetFacePoint = new JNative("DetectSDK.dll", "FGDetectSGetFacePoint");
					FGDetectSGetResult = new JNative("DetectSDK.dll", "FGDetectSGetResult");
					FGDetectSGetAssess = new JNative("DetectSDK.dll", "FGDetectSGetAssess");
					result = 1;
				} else
					result = -1;
			}

		} catch (NativeException e) {
			result = -1;
			log.error("FaceDetectionWithImageQualityJNIEntry failed!", e);
		} catch (IllegalAccessException e) {
			result = -1;
			log.error("FaceDetectionWithImageQualityJNIEntry failed!", e);
		}
		return result;
	}

	public void detectFaceLocation(FaceData fd) {

		int id, x = 0, y = 0, width = 0, height = 0;
		byte[] imgBytes = ImageToolkit.getImageBytes(fd.getFrame(), "jpeg");
		int i = 0;

		try {
			Pointer FaceLocationPointer = new Pointer(MemoryBlockFactory.createMemoryBlock(100));
			FGDetectImageFaceLocation.setRetVal(Type.INT);
			FGDetectImageFaceLocation.setParameter(i++, JNIHandle);
			FGDetectImageFaceLocation.setParameter(i++, Type.STRING, imgBytes);
			FGDetectImageFaceLocation.setParameter(i++, Type.INT, "" + imgBytes.length);
			FGDetectImageFaceLocation.setParameter(i++, FaceLocationPointer);
			FGDetectImageFaceLocation.setParameter(i++, 0);
			FGDetectImageFaceLocation.invoke();

			byte[] FaceLocationResultBytes = FaceLocationPointer.getMemory();
			i = 0;
			id = CommUtil.bytesToInt(FaceLocationResultBytes, i);

			i += 4;
			x = CommUtil.bytesToInt(FaceLocationResultBytes, i);

			i += 4;
			y = CommUtil.bytesToInt(FaceLocationResultBytes, i);

			i += 4;
			width = CommUtil.bytesToInt(FaceLocationResultBytes, i);

			i += 4;
			height = CommUtil.bytesToInt(FaceLocationResultBytes, i);

			FaceLocationPointer.dispose();
		} catch (NativeException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} finally {
		}
		fd.updateFaceLocation(x, y, width, height);

	}

	public void detectFaceImageQuality(FaceData fd) {
		FaceDetectedResult fdr = new FaceDetectedResult();
		byte[] imgBytes = ImageToolkit.getImageBytes(fd.getFrame(), "jpeg");

		long nowMils = Calendar.getInstance().getTimeInMillis();

		int i = 0;

		try {
			FGDetectSImageQuality.setRetVal(Type.INT);
			FGDetectSImageQuality.setParameter(i++, JNIHandle);
			FGDetectSImageQuality.setParameter(i++, Type.STRING, imgBytes);
			FGDetectSImageQuality.setParameter(i++, Type.INT, "" + imgBytes.length);
			FGDetectSImageQuality.invoke();

			// log.debug("FGDetectSImageQuality:"+FGDetectSImageQuality.getRetVal());
			if (FGDetectSImageQuality.getRetValAsInt() == 0) {

				// /*
				// * public struct _FGFacePoint {
				// * public int faceType; // 人脸类型 0:正面 * 1:左侧面 2:右侧面
				// * public int xleft; // 左眼在原始图像中的位置
				// * public int yleft;
				// * public int xright; // 右眼在原始图像中的位置
				// * public int yright;
				// * public int faceLeft; // 人脸左边缘的X坐标，-1表示未知
				// * public int faceRight; / * 人脸右边缘的X坐标，-1表示未知
				// * public int headLeft; // 人头左边缘的X坐标，-1表示未知
				// * public int headRight; // 人头右边缘的X坐标，-1表示未知
				// * public int headTop; // * 头顶的Y坐标，-1表示未知
				// * public int chinPos; // 下巴的Y坐标，-1表示未知
				// * public double skewAngle; // 平面内偏斜角度 -181表示未知 }
				// */

				Pointer FacePointPointer = new Pointer(MemoryBlockFactory.createMemoryBlock(100));

				i = 0;
				FGDetectSGetFacePoint.setRetVal(Type.INT);
				FGDetectSGetFacePoint.setParameter(i++, JNIHandle);
				FGDetectSGetFacePoint.setParameter(i++, FacePointPointer);
				FGDetectSGetFacePoint.invoke();

				// log.debug("FGDetectSImageQuality:"+FGDetectSGetFacePoint.getRetVal());

				byte[] FacePointOutBytes = FacePointPointer.getMemory();
				i = 0;

				fdr.setFaceType(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				fdr.setXleft(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				fdr.setYleft(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				fdr.setXright(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				fdr.setYright(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				fdr.setFaceLeft(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				fdr.setFaceRight(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				fdr.setHeadLeft(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				fdr.setHeadRight(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				fdr.setHeadTop(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				fdr.setChinPos(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				fdr.setSkewAngle(CommUtil.bytesToInt(FacePointOutBytes, i));
				FacePointOutBytes = null;
				FacePointPointer.dispose();

//				int x = 0, y = 0, width = 0, height = 0;
//				x=fdr.getHeadLeft();
//				y=fdr.getHeadTop();
//				width=fdr.getHeadRight() - fdr.getHeadLeft();
//				height=fdr.getChinPos() - fdr.getHeadTop();
//				
//				
//				fd.updateFaceLocation(x, y, width, height);
				

				//
				/*
				 * public struct _FG_SDetectResult { // 照片属性 public int
				 * faceCount; // 照片人脸数
				 * 
				 * // (-) 偏左，偏上 (+) 偏右，偏下 public float faceRoll; // 脸旋转角度 public
				 * float faceYaw; // 脸侧转角度 public float headPitch; // 头俯昂角度
				 * 
				 * public float faceUniform; // 脸部光线均匀性 public float
				 * faceHotspots; // 脸部高光 public float faceBlur; // 脸部模糊度 public
				 * float eyesOpen; // 眼睛开闭 public float eyesFrontal; // 眼睛正视前方
				 * public float faceExpression; // 表情自然 public float
				 * eyesGlasses; // 是否戴眼镜 };
				 */

				Pointer FaceResultPointer = new Pointer(MemoryBlockFactory.createMemoryBlock(100));

				i = 0;
				FGDetectSGetResult.setRetVal(Type.INT);
				FGDetectSGetResult.setParameter(i++, JNIHandle);
				FGDetectSGetResult.setParameter(i++, FaceResultPointer);
				FGDetectSGetResult.invoke();
				// log.debug("FGDetectSGetResult:"+FGDetectSGetResult.getRetVal());

				byte[] FaceResultBytes = FaceResultPointer.getMemory();
				i = 0;
				fdr.setFaceCount(CommUtil.bytesToInt(FaceResultBytes, i));
				i += 4;

				fdr.setFaceRoll(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				fdr.setFaceYaw(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				fdr.setHeadPitch(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				fdr.setFaceUniform(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				fdr.setFaceHotspots(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				fdr.setFaceBlur(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				fdr.setEyesOpen(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				fdr.setEyesFrontal(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				fdr.setFaceExpression(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				fdr.setEyesGlasses(CommUtil.bytesToFloat(FaceResultBytes, i));

				i += 4;
				FaceResultBytes = null;
				FaceResultPointer.dispose();

				Pointer FaceAssessPointer = new Pointer(MemoryBlockFactory.createMemoryBlock(100));

				i = 0;
				FGDetectSGetAssess.setRetVal(Type.INT);
				FGDetectSGetAssess.setParameter(i++, JNIHandle);
				FGDetectSGetAssess.setParameter(i++, FaceAssessPointer);
				FGDetectSGetAssess.invoke();
				// log.debug("FGDetectSGetAssess:"+FGDetectSGetAssess.getRetVal());

				byte[] FaceAssessBytes = FaceAssessPointer.getMemory();

				i = 0;
				fdr.setPass(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fdr.setHasface(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fdr.setEyesopen(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fdr.setFaceblur(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fdr.setHotspots(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fdr.setLightuniform(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fdr.setExpression(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fdr.setFacefrontal(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fdr.setEyesfrontal(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fdr.setHeadhigh(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fdr.setHeadlow(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fdr.setHeadleft(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fdr.setHeadright(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fdr.setLargehead(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fdr.setSmallhead(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fdr.setWearsglasses(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				FaceAssessBytes = null;
				FaceAssessPointer.dispose();
			}

		} catch (NativeException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} finally {
		}
		fd.setFaceDetectedResult(fdr);
		long usingTime = Calendar.getInstance().getTimeInMillis() - nowMils;
		// log.debug("detectFaceQuality using:" + usingTime + " ret=" + fdr);

	}

	public void detectFaceImage(FaceData fd) {
		long nowMils = Calendar.getInstance().getTimeInMillis();
		this.detectFaceLocation(fd);
		this.detectFaceImageQuality(fd);
		long usingTime = Calendar.getInstance().getTimeInMillis() - nowMils;
		log.debug("detectFaceImage using:" + usingTime + " ret=" + fd.getFaceDetectedResult());

	}

}
