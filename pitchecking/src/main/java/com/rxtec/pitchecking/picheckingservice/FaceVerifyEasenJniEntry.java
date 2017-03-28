package com.rxtec.pitchecking.picheckingservice;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xvolks.jnative.JNative;
import org.xvolks.jnative.Type;
import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.pointers.Pointer;
import org.xvolks.jnative.pointers.memory.MemoryBlockFactory;

public class FaceVerifyEasenJniEntry {
	private Logger log = LoggerFactory.getLogger("FaceVerifyEasenJniEntry");
	String DLLName = "";
	JNative getCurrentHWIDJnative = null;
	JNative setActivationJnative = null;
	JNative initializeSDKJnative = null;
	JNative setIDCardPhotoJnative = null;
	JNative matchJnative = null;
	JNative detectFaceJnative = null;
	JNative finalizeSDKJnative = null;

	DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();

	public FaceVerifyEasenJniEntry(String DLLName) {
		// TODO Auto-generated constructor stub
		this.DLLName = DLLName;
		initJNIContext();
	}

	private void initJNIContext() {

		try {
			log.debug("DLLName=="+DLLName);
			getCurrentHWIDJnative = new JNative(DLLName, "getCurrentHWID");
			setActivationJnative = new JNative(DLLName, "setActivation");
			initializeSDKJnative = new JNative(DLLName, "initializeSDK");
			setIDCardPhotoJnative = new JNative(DLLName, "setIDCardPhoto");
			matchJnative = new JNative(DLLName, "match");
			detectFaceJnative = new JNative(DLLName, "detectFace");
			finalizeSDKJnative = new JNative(DLLName, "finalizeSDK");
			log.debug("initJNIContext complete!");
		} catch (NativeException e) {
			log.error("FaceVerifyEasenJniEntry initJNIContext failed!", e);
		}

		df.setMaximumFractionDigits(2);
	}

	/**
	 * 获取设备ID
	 * @param nSize
	 * @return
	 */
	public String getCurrentHWID(int nSize) {
		String hwid = "";
		int retval = -1;
		Pointer pointerOut = null;
		try {
			pointerOut = new Pointer(MemoryBlockFactory.createMemoryBlock(nSize));
			int i = 0;

			getCurrentHWIDJnative.setRetVal(Type.INT);
			getCurrentHWIDJnative.setParameter(i++, pointerOut);
			getCurrentHWIDJnative.setParameter(i++, nSize);
			getCurrentHWIDJnative.invoke();

			retval = getCurrentHWIDJnative.getRetValAsInt();
			log.debug("GetCurrentHWID retval==" + retval);
			if (retval == 0) {
				byte[] barArray = new byte[nSize];
				for (int k = 0; k < barArray.length; k++) {
					barArray[k] = pointerOut.getAsByte(k);
				}
				hwid = new String(barArray);
				log.debug("hwid==" + hwid);
			}
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("FaceVerifyEasenJniEntry GetCurrentHWID:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("FaceVerifyEasenJniEntry GetCurrentHWID:", e);
		} catch (Exception e) {
			log.error("FaceVerifyEasenJniEntry GetCurrentHWID:", e);
		}

		return hwid;
	}
	
	/**
	 * 从厂家获取的activation key来验证
	 * @param szActivationKey
	 * @return
	 */
	public int setActivation(String szActivationKey){
		int retval = -1;
		try {
			int i = 0;

			setActivationJnative.setRetVal(Type.INT);
			setActivationJnative.setParameter(i++, szActivationKey);
			setActivationJnative.invoke();

			retval = setActivationJnative.getRetValAsInt();
			log.debug("SetActivation retval==" + retval);
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("FaceVerifyEasenJniEntry SetActivationJnative:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("FaceVerifyEasenJniEntry SetActivation:", e);
		} catch (Exception e) {
			log.error("FaceVerifyEasenJniEntry SetActivation:", e);
		}
		return retval;
	}
	
	/**
	 * 初始化SDK
	 * @param wszFolderPath
	 * @return
	 */
	public int initializeSDK(String wszFolderPath){
		int retval = -1;
		try {
			int i = 0;

			initializeSDKJnative.setRetVal(Type.INT);
			initializeSDKJnative.setParameter(i++, wszFolderPath);
			initializeSDKJnative.invoke();

			retval = initializeSDKJnative.getRetValAsInt();
			log.debug("initializeSDK retval==" + retval);
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("FaceVerifyEasenJniEntry initializeSDK:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("FaceVerifyEasenJniEntry initializeSDK:", e);
		} catch (Exception e) {
			log.error("FaceVerifyEasenJniEntry initializeSDK:", e);
		}
		return retval;
	}
	
	/**
	 * 处理身份证照片
	 * @param pbIDCardPhoto
	 * @param nWidth
	 * @param nHeight
	 * @return
	 */
	public int setIDCardPhoto(String pbIDCardPhoto,int nWidth,int nHeight){
		int retval = -1;
		try {
			int i = 0;

			setIDCardPhotoJnative.setRetVal(Type.INT);
			setIDCardPhotoJnative.setParameter(i++, pbIDCardPhoto);
			setIDCardPhotoJnative.setParameter(i++, nWidth);
			setIDCardPhotoJnative.setParameter(i++, nHeight);
			setIDCardPhotoJnative.invoke();

			retval = setIDCardPhotoJnative.getRetValAsInt();
			log.debug("setIDCardPhoto retval==" + retval);
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("FaceVerifyEasenJniEntry setIDCardPhoto:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("FaceVerifyEasenJniEntry setIDCardPhoto:", e);
		} catch (Exception e) {
			log.error("FaceVerifyEasenJniEntry setIDCardPhoto:", e);
		}
		return retval;
	} 
	
	/**
	 * 结束SDK使用
	 * @return
	 */
	public int finalizeSDK(){
		int retval = -1;
		try {
			int i = 0;

			finalizeSDKJnative.setRetVal(Type.INT);
			finalizeSDKJnative.invoke();

			retval = finalizeSDKJnative.getRetValAsInt();
			log.debug("finalizeSDK retval==" + retval);
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("FaceVerifyEasenJniEntry finalizeSDK:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("FaceVerifyEasenJniEntry finalizeSDK:", e);
		} catch (Exception e) {
			log.error("FaceVerifyEasenJniEntry finalizeSDK:", e);
		}
		return retval;
	}
	
	/**
	 * 做setIDCardPhoto来处理过的身份证照片和现场照上的人脸比对
	 * @param pbSceneImage
	 * @param nWidth
	 * @param nHeight
	 * @return
	 */
	public int match(String pbSceneImage,int nWidth,int nHeight) {
		String hwid = "";
		int retval = -1;
		Pointer pointerOut = null;
		try {
//			pointerOut = new Pointer(MemoryBlockFactory.createMemoryBlock(nSize));
			int i = 0;

			matchJnative.setRetVal(Type.INT);
			matchJnative.setParameter(i++, pbSceneImage);
			matchJnative.setParameter(i++, nWidth);
			matchJnative.setParameter(i++, nHeight);
			matchJnative.invoke();

			retval = matchJnative.getRetValAsInt();
			log.debug("match retval==" + retval);
			if (retval == 0) {
//				byte[] barArray = new byte[nSize];
//				for (int k = 0; k < barArray.length; k++) {
//					barArray[k] = pointerOut.getAsByte(k);
//				}
//				hwid = new String(barArray);
//				log.debug("hwid==" + hwid);
			}
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("FaceVerifyEasenJniEntry match:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("FaceVerifyEasenJniEntry match:", e);
		} catch (Exception e) {
			log.error("FaceVerifyEasenJniEntry match:", e);
		}

		return retval;
	}
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String DllName = "idVerificationSDK.dll";
		FaceVerifyEasenJniEntry verifyEasen = new  FaceVerifyEasenJniEntry(DllName);
		String hwid = verifyEasen.getCurrentHWID(256);
		String szActivationKey = "jbixDib8x1obPOSParJI9DxcuHFb/jjwV85yD6TxFpnegdYObZSqmp9LbKgXYZS/gGp7iZ6USsTJlnNh2s0nef4AVJKe3xsXpRPhcEE7eDOp/Wugg/HWvjltpTa0g7r17iRVBqD0tOBQS9SbzeETAoRJNNdOkHNYcJR80dryPJ3sb6hYvanpYvduOFgQgaePlXEmZxb664xJU2c5U5jPGhNg3WhKBarLt5QJjXIbYQ0uSXDdWCdog1cK3XRVLuU/yKM1WRGF/LJbDNhDwZ/FUPHRxYalI6iZ+0JC2F66aZdM6XdgxnUDxk1DSOoSO40NzGEi0cFzMViK1wzYM+zklw==";
		verifyEasen.setActivation(szActivationKey);
		verifyEasen.initializeSDK("D:/maven/git/pitchecking/");
	}

}
