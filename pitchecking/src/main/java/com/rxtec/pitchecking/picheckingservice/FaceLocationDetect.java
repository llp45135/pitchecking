package com.rxtec.pitchecking.picheckingservice;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xvolks.jnative.JNative;
import org.xvolks.jnative.Type;
import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.pointers.Pointer;
import org.xvolks.jnative.pointers.memory.MemoryBlockFactory;

import com.rxtec.pitchecking.event.FaceLocation;
import com.rxtec.pitchecking.utils.CommUtil;

public class FaceLocationDetect {

	public static FaceLocationDetect instance = null;
	private Logger log = LoggerFactory.getLogger("FaceLocationDetect");
	private int sdkInitRet = -1;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// test();
		FaceLocationDetect.getInstance().duibi();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FaceLocationDetect.getInstance().duibi();
	}

	private FaceLocationDetect() {
		JNative.setLoggingEnabled(true);
		/**
		 * 初始化SDK
		 */
		JNative jnative1 = null;
		try {
			log.info("FGDetectInitSDK");
			jnative1 = new JNative("DetectSDK.dll", "FGDetectInitSDK");
			jnative1.setRetVal(Type.INT);
			jnative1.invoke();
			log.info("FGDetectInitSDK: retval==" + jnative1.getRetVal());// 获取返回值
			this.sdkInitRet = jnative1.getRetValAsInt();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static synchronized FaceLocationDetect getInstance() {
		if (instance == null) {
			instance = new FaceLocationDetect();
		}
		return instance;
	}

	// @SuppressWarnings("deprecation")
	public FaceLocation duibi() {
		String ret = "failue";

		JNative jnative2 = null;
		JNative jnative3 = null;
		FaceLocation faceLocation = null;
		try {

			//
			if (this.sdkInitRet == 0) {
				Pointer handle = new Pointer(MemoryBlockFactory.createMemoryBlock(4));
				handle.setIntAt(0, 0);
				int i = 0;
				jnative2 = new JNative("DetectSDK.dll", "FGDetectCreateHandleEmpty"); // 创建缺省标准的检测句柄
				jnative2.setRetVal(Type.INT);
				jnative2.setParameter(i++, handle);
				jnative2.invoke();
				log.info("FGDetectCreateHandleEmpty: retval==" + jnative2.getRetVal());// 获取返回值
				log.info("handle==" + handle.getAsInt(0));

				int iHandle = handle.getAsInt(0);

				handle.dispose();

				if (jnative2.getRetValAsInt() == 0) {
					log.info("FGDetectImageFaceLocation");
					Pointer pOrgResult = new Pointer(MemoryBlockFactory.createMemoryBlock(100));
					byte[] imgBytes = CommUtil.getBytes("D:/eclipse/workspace/ljx.jpg");
					i = 0;
					jnative3 = new JNative("DetectSDK.dll", "FGDetectImageFaceLocation"); // 获取图像检测评价值
					jnative3.setRetVal(Type.INT);
					jnative3.setParameter(i++, iHandle);
					jnative3.setParameter(i++, Type.STRING, imgBytes);
					jnative3.setParameter(i++, Type.INT, "" + imgBytes.length);
					jnative3.setParameter(i++, pOrgResult);
					jnative3.setParameter(i++, 0);
					jnative3.invoke();
					log.info("FGDetectImageFaceLocation: retval==" + jnative3.getRetVal());// 获取返回值
					log.info("Result Info:");

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

					log.info("x==" + String.valueOf(faceLocation.getX()));
					log.info("y==" + String.valueOf(faceLocation.getY()));
					log.info("width==" + String.valueOf(faceLocation.getWidth()));
					log.info("height==" + String.valueOf(faceLocation.getHeight()));

				}
			}

//			jnative1.dispose();
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
