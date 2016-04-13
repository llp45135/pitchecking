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

public class FaceLocationDetection {

	private Logger log = LoggerFactory.getLogger("FaceLocationDetection");
	private int sdkInitRet = -1;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FaceLocationDetection detecter = new FaceLocationDetection();
		byte[] imgBytes = CommUtil.getBytes("C:/DCZ/20160412/43040619900308255x.jpg");
		detecter.initJNIContext();
		detecter.detect(imgBytes);
		detecter.detect(imgBytes);
		detecter.detect(imgBytes);
	}

	public FaceLocationDetection() {
		JNative.setLoggingEnabled(true);
		/**
		 * 初始化SDK
		 */
		initJNIContext();


	}

	JNative jnative1 = null;
	JNative jnative2 = null;
	JNative jnative3 = null;

	int JNIFunctionHandle;

	private void initJNIContext() {
		try {
			log.debug("FGDetectInitSDK");
			jnative1 = new JNative("DetectSDK.dll", "FGDetectInitSDK");
			jnative1.setRetVal(Type.INT);
			jnative1.invoke();
			log.debug("FGDetectInitSDK: retval==" + jnative1.getRetVal());// 获取返回值
			this.sdkInitRet = jnative1.getRetValAsInt();
			if (this.sdkInitRet == 0) {
				Pointer handle = new Pointer(MemoryBlockFactory.createMemoryBlock(4));
				handle.setIntAt(0, 0);
				int i = 0;
				jnative2 = new JNative("DetectSDK.dll", "FGDetectCreateHandleEmpty"); // 创建缺省标准的检测句柄
				jnative2.setRetVal(Type.INT);
				jnative2.setParameter(i++, handle);
				jnative2.invoke();
				log.debug("FGDetectCreateHandleEmpty: retval==" + jnative2.getRetVal());// 获取返回值
				log.debug("handle==" + handle.getAsInt(0));

				JNIFunctionHandle = handle.getAsInt(0);

				handle.dispose();

				if (jnative2.getRetValAsInt() == 0) {
					log.debug("FGDetectImageFaceLocation");
					jnative3 = new JNative("DetectSDK.dll", "FGDetectImageFaceLocation"); // 获取图像检测评价值
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
	public FaceLocation detect(byte[] imgBytes) {
		String ret = "failue";

		FaceLocation faceLocation = null;
		try {

			long nowMils = Calendar.getInstance().getTimeInMillis();
			//
			int i = 0;

			//log.info("Begin to FGDetectImageFaceLocation.......................");
			Pointer pOrgResult = new Pointer(MemoryBlockFactory.createMemoryBlock(100));
			jnative3.setRetVal(Type.INT);
			jnative3.setParameter(i++, JNIFunctionHandle);
			jnative3.setParameter(i++, Type.STRING, imgBytes);
			jnative3.setParameter(i++, Type.INT, "" + imgBytes.length);
			jnative3.setParameter(i++, pOrgResult);
			jnative3.setParameter(i++, 0);
			jnative3.invoke();

			byte[] bytesResult = pOrgResult.getMemory();
			faceLocation = new FaceLocation();
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

			long usingTime = Calendar.getInstance().getTimeInMillis() - nowMils;
			//log.debug("Face Detected succ using " + usingTime + " ms......" + faceLocation.toString());

			// jnative1.dispose();
			// jnative2.dispose();
			// jnative3.dispose(); //注意：JNative1.3.2任意调用一个一次就可以了，不允许多次

			ret = "success";
		} catch (NativeException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} finally {
		}
		return faceLocation;
	}



}
