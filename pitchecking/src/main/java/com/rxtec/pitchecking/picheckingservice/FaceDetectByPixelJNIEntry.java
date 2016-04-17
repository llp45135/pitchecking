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

public class FaceDetectByPixelJNIEntry implements IFaceDetect{

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
		IFaceDetect detecter = FaceDetectByPixelJNIEntry.getInstance();
		byte[] imgBytes = CommUtil.getBytes("C:/pitchecking/images/20160416/-707230729@07-05-13-916.jpg");
		byte[] imgBytes2 = CommUtil.getBytes("C:/pitchecking/images/20160416/-707230729@06-41-43-341.jpg");
		
		FaceDetectedResult r = new FaceDetectedResult();
		FaceDetectedResult r2 = new FaceDetectedResult();
		
		

		r.setImageBytes(imgBytes);
		r2.setImageBytes(imgBytes2);
		for (int i = 0; i < 200; i++) {
			detecter.detectFaceImage(r);
			detecter.detectFaceImage(r2);
			
			System.out.println(r);
			System.out.println(r2);

		}
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

	public void detectFaceLocation(FaceDetectedResult fd) {

		byte[] imgBytes = fd.getImageBytes();
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
			fd.setId(CommUtil.bytesToInt(FaceLocationResultBytes, i));

			i += 4;
			fd.setX(CommUtil.bytesToInt(FaceLocationResultBytes, i));

			i += 4;
			fd.setY(CommUtil.bytesToInt(FaceLocationResultBytes, i));

			i += 4;
			fd.setWidth(CommUtil.bytesToInt(FaceLocationResultBytes, i));

			i += 4;
			fd.setHeight(CommUtil.bytesToInt(FaceLocationResultBytes, i));

			i += 4;
			fd.setConfidence(CommUtil.bytesToFloat(FaceLocationResultBytes, i));

			i += 4;
			fd.setxFirstEye(CommUtil.bytesToInt(FaceLocationResultBytes, i));

			i += 4;
			fd.setyFirstEye(CommUtil.bytesToInt(FaceLocationResultBytes, i));

			i += 4;
			fd.setFirstConfidence(CommUtil.bytesToFloat(FaceLocationResultBytes, i));

			i += 4;
			fd.setxSecondEye(CommUtil.bytesToInt(FaceLocationResultBytes, i));

			i += 4;
			fd.setySecondEye(CommUtil.bytesToInt(FaceLocationResultBytes, i));

			i += 4;
			fd.setSecondConfidence(CommUtil.bytesToFloat(FaceLocationResultBytes, i));

			FaceLocationPointer.dispose();
		} catch (NativeException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} finally {
		}

	}

	public void detectFaceImageQuality(FaceDetectedResult fd) {
		byte[] imgBytes = fd.getImageBytes();
		
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

				fd.setFaceType(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				fd.setXleft(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				fd.setYleft(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				fd.setXright(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				fd.setYright(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				fd.setFaceLeft(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				fd.setFaceRight(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				fd.setHeadLeft(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				fd.setHeadRight(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				fd.setHeadTop(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				fd.setChinPos(CommUtil.bytesToInt(FacePointOutBytes, i));
				i += 4;
				fd.setSkewAngle(CommUtil.bytesToInt(FacePointOutBytes, i));
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
				fd.setFaceCount(CommUtil.bytesToInt(FaceResultBytes, i));
				i += 4;

				fd.setFaceRoll(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				fd.setFaceYaw(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				fd.setHeadPitch(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				fd.setFaceUniform(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				fd.setFaceHotspots(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				fd.setFaceBlur(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				fd.setEyesOpen(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				fd.setEyesFrontal(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				fd.setFaceExpression(CommUtil.bytesToFloat(FaceResultBytes, i));
				i += 4;

				fd.setEyesGlasses(CommUtil.bytesToFloat(FaceResultBytes, i));

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
				fd.setPass(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fd.setHasface(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fd.setEyesopen(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fd.setFaceblur(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fd.setHotspots(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fd.setLightuniform(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fd.setExpression(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fd.setFacefrontal(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fd.setEyesfrontal(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fd.setHeadhigh(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fd.setHeadlow(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fd.setHeadleft(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fd.setHeadright(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fd.setLargehead(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fd.setSmallhead(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
				fd.setWearsglasses(CommUtil.bytesToBoolean(FaceAssessBytes[i++]));
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
		log.debug("detectFaceQuality using:"+usingTime+" ret="+fd);

	}
	public void detectFaceImage(FaceDetectedResult result) {
		this.detectFaceLocation(result);
		this.detectFaceImageQuality(result);
	}


}
