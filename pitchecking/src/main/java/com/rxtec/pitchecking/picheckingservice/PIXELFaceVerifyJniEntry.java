package com.rxtec.pitchecking.picheckingservice;

import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xvolks.jnative.JNative;
import org.xvolks.jnative.Type;
import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.pointers.Pointer;
import org.xvolks.jnative.pointers.memory.MemoryBlockFactory;

public class PIXELFaceVerifyJniEntry implements FaceVerifyInterface {

	
	private int initStatus = 0;
	
	public int getInitStatus() {
		return initStatus;
	}


	public PIXELFaceVerifyJniEntry(String DLLName) {
		String filePath = Thread.currentThread().getContextClassLoader().getResource("").toString();
		this.DLLName = filePath.substring(6, filePath.length()) + DLLName;
		initJNIContext();
	}

	JNative jnativeInitFun = null;
	JNative jnativeVerifyFun = null;
	JNative jnativeExitFun = null;
	private Logger log = LoggerFactory.getLogger("FaceAuthentication");
	String DLLName = "";
	DecimalFormat df=(DecimalFormat)NumberFormat.getInstance(); 

	private void initJNIContext() {

		try {
			jnativeInitFun = new JNative(DLLName, "PS_InitFaceIDSDK");
			jnativeInitFun.setRetVal(Type.INT);
			jnativeInitFun.invoke();
			log.info("初始化 PS_InitFaceIDSDK 成功!  ret: " + jnativeInitFun.getRetVal());// 获取返回值
			jnativeVerifyFun = new JNative(DLLName, "PS_VerifyImage");
		} catch (NativeException e) {
			log.error("初始化 PS_InitFaceIDSDK 失败！", e);
			initStatus = -1;
		} catch (IllegalAccessException e) {
			log.error("初始化 PS_InitFaceIDSDK 失败!", e);
			initStatus = -1;
		} finally {
		}

		df.setMaximumFractionDigits(2);
		initStatus = 1;

	}

	public void clearJNIContext() {
		try {
			jnativeExitFun = new JNative(DLLName, "PS_ExitFaceIDSDK");
			jnativeExitFun.setRetVal(Type.INT);
			jnativeExitFun.invoke();
			log.debug("PS_ExitFaceIDSDK ret: " + jnativeExitFun.getRetVal());// 获取返回值
			jnativeExitFun.dispose(); // 注意：JNative1.3.2任意调用一个一次就可以了，不允许多次
		} catch (NativeException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} finally {
		}
	}

	public float verify(byte[] faceImgBytes, byte[] idCardBytes) {

		float result = 0;

		long nowMils = Calendar.getInstance().getTimeInMillis();
		Pointer aArrIntInputf = null;
		try {

			aArrIntInputf = new Pointer(MemoryBlockFactory.createMemoryBlock(8));
			aArrIntInputf.setFloatAt(0, -1);
			int i = 0;
			// log.debug("aArrIntInputf: " +
			// aArrIntInputf.getAsFloat(0));//获取返回值

			jnativeVerifyFun.setRetVal(Type.INT);
			jnativeVerifyFun.setParameter(i++, Type.STRING, faceImgBytes);
			jnativeVerifyFun.setParameter(i++, Type.INT, "" + faceImgBytes.length);
			jnativeVerifyFun.setParameter(i++, Type.STRING, idCardBytes);
			jnativeVerifyFun.setParameter(i++, Type.INT, "" + idCardBytes.length);
			jnativeVerifyFun.setParameter(i++, aArrIntInputf);
			jnativeVerifyFun.invoke();

			// 获取输出参数值（验证分数值）
			result = aArrIntInputf.getAsFloat(0);
			aArrIntInputf.dispose();
//			long usingTime = Calendar.getInstance().getTimeInMillis() - nowMils;
//			log.debug("Using " + usingTime + " ms, value=" + df.format(result));

		} catch (NativeException e) {
			log.error("FaceVerifyJniEntry verify failed!", e);
		} catch (IllegalAccessException e) {
			log.error("FaceVerifyJniEntry verify failed!", e);
		} finally {
			try {
				if (aArrIntInputf != null) {
					aArrIntInputf.dispose();
				}
			} catch (NativeException e) {
				log.error("FaceVerifyJniEntry verify failed!", e);
			}
		}
		return result;
	}


	@Override
	public int setIDCardPhoto(BufferedImage idCardImg) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public float verify(byte[] faceImgBytes) {
		// TODO Auto-generated method stub
		return 0;
	}


}
