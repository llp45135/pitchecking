package com.rxtec.pitchecking.picheckingservice;

import java.util.Calendar;

import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xvolks.jnative.JNative;
import org.xvolks.jnative.Type;
import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.pointers.Pointer;
import org.xvolks.jnative.pointers.memory.MemoryBlockFactory;

import com.rxtec.pitchecking.utils.CommUtil;

public class FaceDetectImageQualityJNIEntry {

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
		FaceDetectImageQualityJNIEntry detecter = FaceDetectImageQualityJNIEntry.getInstance();
		byte[] imgBytes = CommUtil.getBytes("C:/pitchecking/images/20160416/1544565937@02-44-33-040.jpg");
		byte[] imgBytes2 = CommUtil.getBytes("C:/pitchecking/images/20160416/1544565937@02-45-26-482.jpg");
		
		FaceDetectedResult r = new FaceDetectedResult();
		FaceDetectedResult r2 = new FaceDetectedResult();

		r.setImageBytes(imgBytes);
		r2.setImageBytes(imgBytes2);
		detecter.detectFaceLocation(imgBytes);
		for (int i = 0; i < 20; i++) {
			detecter.detectFaceQuality(imgBytes);
			detecter.detectFaceQuality(imgBytes2);
			
			System.out.println(r);
			System.out.println(r2);

		}
	}

	private static FaceDetectImageQualityJNIEntry instance = null;

	public static synchronized FaceDetectImageQualityJNIEntry getInstance() {
		if (instance == null) {
			instance = new FaceDetectImageQualityJNIEntry();
		}

		return instance;
	}

	private FaceDetectImageQualityJNIEntry() {
		JNative.setLoggingEnabled(false);
		/**
		 * 初始化SDK
		 */
		initJNIContext();

	}

	private int initJNIContext() {
		int result = 0;
		try {
			FGDetectInitSDK = new JNative("DetectSDKClone.dll", "FGDetectInitSDK");
			FGDetectInitSDK.setRetVal(Type.INT);
			FGDetectInitSDK.invoke();
			log.debug("FGDetectInitSDK:" + FGDetectInitSDK.getRetValAsInt());

			if (FGDetectInitSDK.getRetValAsInt() == 0) {

				Pointer handle = new Pointer(MemoryBlockFactory.createMemoryBlock(4));
				handle.setIntAt(0, 0);
				int i = 0;
				FGDetectImageFaceLocation = new JNative("DetectSDKClone.dll", "FGDetectImageFaceLocation");

				FGDetectCreateHandleFromFile = new JNative("DetectSDKClone.dll", "FGDetectCreateHandleFromFile");
				FGDetectCreateHandleFromFile.setRetVal(Type.INT);
				FGDetectCreateHandleFromFile.setParameter(i++, handle);
				FGDetectCreateHandleFromFile.setParameter(i++, Type.STRING, "c:/pitchecking/FaceDetection.flt");
				FGDetectCreateHandleFromFile.invoke();
				log.debug("FGDetectCreateHandleFromFile:" + FGDetectCreateHandleFromFile.getRetVal());
				JNIHandle = handle.getAsInt(0);
				handle.dispose();
				if (FGDetectCreateHandleFromFile.getRetValAsInt() == 0) {
					FGDetectSImageQuality = new JNative("DetectSDKClone.dll", "FGDetectSImageQuality");
					FGDetectSGetFacePoint = new JNative("DetectSDKClone.dll", "FGDetectSGetFacePoint");
					FGDetectSGetResult = new JNative("DetectSDKClone.dll", "FGDetectSGetResult");
					FGDetectSGetAssess = new JNative("DetectSDKClone.dll", "FGDetectSGetAssess");
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

	public FaceDetectedResult detectFaceLocation(byte[] imgBytes) {

		FaceDetectedResult result = new FaceDetectedResult();
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
			result.setId(CommUtil.bytesToInt(FaceLocationResultBytes, i));

			i += 4;
			result.setX(CommUtil.bytesToInt(FaceLocationResultBytes, i));

			i += 4;
			result.setY(CommUtil.bytesToInt(FaceLocationResultBytes, i));

			i += 4;
			result.setWidth(CommUtil.bytesToInt(FaceLocationResultBytes, i));

			i += 4;
			result.setHeight(CommUtil.bytesToInt(FaceLocationResultBytes, i));

			i += 4;
			result.setConfidence(CommUtil.bytesToFloat(FaceLocationResultBytes, i));

			i += 4;
			result.setxFirstEye(CommUtil.bytesToInt(FaceLocationResultBytes, i));

			i += 4;
			result.setyFirstEye(CommUtil.bytesToInt(FaceLocationResultBytes, i));

			i += 4;
			result.setFirstConfidence(CommUtil.bytesToFloat(FaceLocationResultBytes, i));

			i += 4;
			result.setxSecondEye(CommUtil.bytesToInt(FaceLocationResultBytes, i));

			i += 4;
			result.setySecondEye(CommUtil.bytesToInt(FaceLocationResultBytes, i));

			i += 4;
			result.setSecondConfidence(CommUtil.bytesToFloat(FaceLocationResultBytes, i));

			FaceLocationPointer.dispose();
		} catch (NativeException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} finally {
		}

		return result;
	}

	public FaceDetectedResult detectFaceQuality(byte[] imgBytes) {
		
		long nowMils = Calendar.getInstance().getTimeInMillis();

		FaceDetectedResult result = new FaceDetectedResult();
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

				result.setFaceType(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				result.setXleft(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				result.setYleft(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				result.setXright(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				result.setYright(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				result.setFaceLeft(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				result.setFaceRight(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				result.setHeadLeft(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				result.setHeadRight(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				result.setHeadTop(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				result.setChinPos(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				result.setSkewAngle(CommUtil.bytesToInt(FacePointOutBytes, i));
				FacePointOutBytes = null;
				FacePointPointer.dispose();

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
				result.setFaceCount(CommUtil.bytesToInt(FaceResultBytes, i));
				i += 4;

				result.setFaceRoll(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				result.setFaceYaw(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				result.setHeadPitch(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				result.setFaceUniform(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				result.setFaceHotspots(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				result.setFaceBlur(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				result.setEyesOpen(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				result.setEyesFrontal(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				result.setFaceExpression(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				result.setEyesGlasses(CommUtil.bytesToFloat(FaceResultBytes, i));

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
				result.setPass(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				result.setHasface(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				result.setEyesopen(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				result.setFaceblur(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				result.setHotspots(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				result.setLightuniform(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				result.setExpression(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				boolean b = CommUtil.bytesToBoolean(FaceAssessBytes[i++]);
				log.debug("setFacefrontal:"+b);
				result.setFacefrontal(b);
				
				result.setEyesfrontal(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				result.setHeadhigh(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				result.setHeadlow(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				result.setHeadleft(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				result.setHeadright(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				result.setLargehead(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				result.setSmallhead(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				result.setWearsglasses(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				FaceAssessBytes = null;
				FaceAssessPointer.dispose();
			}

		} catch (NativeException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} finally {
		}
		long usingTime = Calendar.getInstance().getTimeInMillis() - nowMils;
		log.debug("detectFaceQuality using:"+usingTime+" ret="+result);
		return result;

	}

}
