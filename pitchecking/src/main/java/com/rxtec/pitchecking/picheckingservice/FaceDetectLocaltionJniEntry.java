package com.rxtec.pitchecking.picheckingservice;

import java.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xvolks.jnative.JNative;
import org.xvolks.jnative.Type;
import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.pointers.Pointer;
import org.xvolks.jnative.pointers.memory.MemoryBlockFactory;

import com.rxtec.pitchecking.utils.CommUtil;

public class FaceDetectLocaltionJniEntry {

	private Logger log = LoggerFactory.getLogger("FaceLocationDetection");
	private int sdkInitRet = -1;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FaceDetectLocaltionJniEntry detecter = new FaceDetectLocaltionJniEntry();
		byte[] imgBytes = CommUtil.getBytes("C:/DCZ/20160412/43040619900308255x.jpg");
		FaceDetectedResult faceLocation = new FaceDetectedResult();
		faceLocation.setImageBytes(imgBytes);
		detecter.detectFaceLocation(faceLocation);
		detecter.detectFaceLocation(faceLocation);
		detecter.detectFaceLocation(faceLocation);
	}

	public FaceDetectLocaltionJniEntry() {
		JNative.setLoggingEnabled(false);
		/**
		 * 初始化SDK
		 */
		initJNIContext();


	}

	JNative jnativeInitFun = null;
	JNative jnativeCreateHandleFun = null;
	JNative jnativeDetectLocationFun = null;
	JNative jnativeDetectAssessFun = null;
	

	int JNIFunctionHandle;

	private void initJNIContext() {
		try {
			log.debug("FGDetectInitSDK");
			jnativeInitFun = new JNative("DetectSDK.dll", "FGDetectInitSDK");
			jnativeInitFun.setRetVal(Type.INT);
			jnativeInitFun.invoke();
			log.debug("FGDetectInitSDK: retval==" + jnativeInitFun.getRetVal());// 获取返回值
			this.sdkInitRet = jnativeInitFun.getRetValAsInt();
			if (this.sdkInitRet == 0) {
				Pointer handle = new Pointer(MemoryBlockFactory.createMemoryBlock(4));
				handle.setIntAt(0, 0);
				int i = 0;
				jnativeCreateHandleFun = new JNative("DetectSDK.dll", "FGDetectCreateHandleEmpty"); // 创建缺省标准的检测句柄
				jnativeCreateHandleFun.setRetVal(Type.INT);
				jnativeCreateHandleFun.setParameter(i++, handle);
				jnativeCreateHandleFun.invoke();
				log.debug("FGDetectCreateHandleEmpty: retval==" + jnativeCreateHandleFun.getRetVal());// 获取返回值
				log.debug("handle==" + handle.getAsInt(0));

				JNIFunctionHandle = handle.getAsInt(0);

				handle.dispose();

				if (jnativeCreateHandleFun.getRetValAsInt() == 0) {
					log.debug("FGDetectImageFaceLocation");
					jnativeDetectLocationFun = new JNative("DetectSDK.dll", "FGDetectImageFaceLocation"); // 人脸位置
				}
			}

		} catch (NativeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// @SuppressWarnings("deprecation")
	public void detectFaceLocation( FaceDetectedResult faceLocation) {
		byte[] imgBytes = faceLocation.getImageBytes();
		try {

			//
			int i = 0;

			Pointer pOrgResult = new Pointer(MemoryBlockFactory.createMemoryBlock(100));
			jnativeDetectLocationFun.setRetVal(Type.INT);
			jnativeDetectLocationFun.setParameter(i++, JNIFunctionHandle);
			jnativeDetectLocationFun.setParameter(i++, Type.STRING, imgBytes);
			jnativeDetectLocationFun.setParameter(i++, Type.INT, "" + imgBytes.length);
			jnativeDetectLocationFun.setParameter(i++, pOrgResult);
			jnativeDetectLocationFun.setParameter(i++, 0);
			jnativeDetectLocationFun.invoke();

			byte[] bytesResult = pOrgResult.getMemory();
			i = 0;
			faceLocation.setId(CommUtil.bytesToInt(bytesResult, i));

			i += 4;
			faceLocation.setX(CommUtil.bytesToInt(bytesResult, i));

			i += 4;
			faceLocation.setY(CommUtil.bytesToInt(bytesResult, i));

			i += 4;
			faceLocation.setWidth(CommUtil.bytesToInt(bytesResult, i));

			i += 4;
			faceLocation.setHeight(CommUtil.bytesToInt(bytesResult, i));

			i += 4;
			faceLocation.setConfidence(CommUtil.bytesToFloat(bytesResult, i));

			i += 4;
			faceLocation.setxFirstEye(CommUtil.bytesToInt(bytesResult, i));

			i += 4;
			faceLocation.setyFirstEye(CommUtil.bytesToInt(bytesResult, i));

			i += 4;
			faceLocation.setFirstConfidence(CommUtil.bytesToFloat(bytesResult, i));

			i += 4;
			faceLocation.setxSecondEye(CommUtil.bytesToInt(bytesResult, i));

			i += 4;
			faceLocation.setySecondEye(CommUtil.bytesToInt(bytesResult, i));

			i += 4;
			faceLocation.setSecondConfidence(CommUtil.bytesToFloat(bytesResult, i));

			pOrgResult.dispose();
			
			

			
			
		
			// jnative1.dispose();
			// jnative2.dispose();
			// jnative3.dispose(); //注意：JNative1.3.2任意调用一个一次就可以了，不允许多次
			
		} catch (NativeException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} finally {
		}
	}




}