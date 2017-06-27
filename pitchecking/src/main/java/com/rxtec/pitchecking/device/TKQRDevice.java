package com.rxtec.pitchecking.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xvolks.jnative.JNative;
import org.xvolks.jnative.Type;
import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.pointers.Pointer;
import org.xvolks.jnative.pointers.memory.MemoryBlockFactory;

public class TKQRDevice {
	private Logger log = LoggerFactory.getLogger("TKQRDevice");
	// private String dllName = "Bar_QRCode.dll";
	private String dllName = "BAR_RXTa.dll";
	JNative jnativeBAR_Init = null;
	JNative jnativeBAR_Uninit = null;
	JNative jnativeBAR_ReadData = null;
	JNative jnativeBAR_GetStatus = null;
	JNative jnativeBAR_GetErrMessage = null;
	private static TKQRDevice _instance = new TKQRDevice();
	
	private int statusCode = -1;



	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public static TKQRDevice getInstance() {
		return _instance;
	}

	private TKQRDevice() {
		// TODO Auto-generated constructor stub
		JNative.setLoggingEnabled(false);
		this.initJnative();
	}

	private void initJnative() {

		try {
			jnativeBAR_Init = new JNative(dllName, "BAR_Init");
			jnativeBAR_Uninit = new JNative(dllName, "BAR_Uninit");
			jnativeBAR_ReadData = new JNative(dllName, "BAR_ReadData");
			jnativeBAR_GetStatus = new JNative(dllName, "BAR_GetStatus");
			jnativeBAR_GetErrMessage = new JNative(dllName, "BAR_GetErrMessage");

		} catch (NativeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param pIn
	 * @return
	 */
	public int BAR_Init(String pIn) {
		int retval = -1;
		Pointer pointerIn = null;
		Pointer pointerOut = null;

		try {
			pointerIn = new Pointer(MemoryBlockFactory.createMemoryBlock(8));
			byte[] pointerInArray = new byte[8];
			byte[] pInArray = pIn.getBytes();
			for (int i = 0; i < pInArray.length; i++) {
				pointerInArray[i] = pInArray[i];
			}
			pointerIn.setMemory(pointerInArray);

			pointerOut = new Pointer(MemoryBlockFactory.createMemoryBlock(8));
			int i = 0;

			jnativeBAR_Init.setRetVal(Type.INT);
			jnativeBAR_Init.setParameter(i++, pointerIn);
			jnativeBAR_Init.setParameter(i++, pointerOut);
			jnativeBAR_Init.invoke();

			retval = jnativeBAR_Init.getRetValAsInt();
			log.debug("BAR_Init retval==" + retval);
			if (retval == 0) {

			}
			pointerIn.dispose();
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("TKQRDevice BAR_Init:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("TKQRDevice BAR_Init:", e);
		} catch (Exception e) {
			log.error("TKQRDevice BAR_Init:", e);
		}
		return retval;
	}

	/**
	 * 
	 * @param pIn
	 * @return
	 */
	public int BAR_Uninit(String pIn) {
		int retval = -1;
		Pointer pointerIn = null;
		Pointer pointerOut = null;

		try {
			pointerIn = new Pointer(MemoryBlockFactory.createMemoryBlock(8));
			byte[] pointerInArray = new byte[8];
			byte[] pInArray = pIn.getBytes();
			for (int i = 0; i < pInArray.length; i++) {
				pointerInArray[i] = pInArray[i];
			}
			pointerIn.setMemory(pointerInArray);

			pointerOut = new Pointer(MemoryBlockFactory.createMemoryBlock(8));
			int i = 0;

			jnativeBAR_Uninit.setRetVal(Type.INT);
			jnativeBAR_Uninit.setParameter(i++, pointerIn);
			jnativeBAR_Uninit.setParameter(i++, pointerOut);
			jnativeBAR_Uninit.invoke();

			retval = jnativeBAR_Init.getRetValAsInt();
			log.debug("BAR_Uninit retval==" + retval);
			if (retval == 0) {

			}
			pointerIn.dispose();
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("TKQRDevice BAR_Uninit:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("TKQRDevice BAR_Uninit:", e);
		} catch (Exception e) {
			log.error("TKQRDevice BAR_Uninit:", e);
		}
		return retval;
	}

	/**
	 * 读二维码数据
	 * 
	 * @param pIn
	 * @return
	 */
	public String BAR_ReadData(String pIn) {
		String barCode = "";
		int retval = -1;
		Pointer pointerIn = null;
		Pointer pointerOut = null;
		Pointer pointerOutLen = null;
		try {
			pointerIn = new Pointer(MemoryBlockFactory.createMemoryBlock(8));
			byte[] pointerInArray = new byte[8];
			byte[] pInArray = pIn.getBytes();
			for (int i = 0; i < pInArray.length; i++) {
				pointerInArray[i] = pInArray[i];
			}
			pointerIn.setMemory(pointerInArray);

			pointerOut = new Pointer(MemoryBlockFactory.createMemoryBlock(256));
			pointerOutLen = new Pointer(MemoryBlockFactory.createMemoryBlock(4));
			int i = 0;

			jnativeBAR_ReadData.setRetVal(Type.INT);
			jnativeBAR_ReadData.setParameter(i++, pointerIn);
			jnativeBAR_ReadData.setParameter(i++, pointerOut);
			jnativeBAR_ReadData.setParameter(i++, pointerOutLen);
			jnativeBAR_ReadData.invoke();

			retval = jnativeBAR_ReadData.getRetValAsInt();
			 log.debug("BAR_ReadData retval==" + retval);
			if (retval == 0) {
				int bar_len = pointerOutLen.getAsInt(0);

				byte[] barArray = new byte[bar_len];
				for (int k = 0; k < barArray.length; k++) {
					barArray[k] = pointerOut.getAsByte(k);
				}
				barCode = new String(barArray);
				log.debug("barCode==" + barCode);
			}
			pointerIn.dispose();
			pointerOut.dispose();
			pointerOutLen.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("TKQRDevice BAR_Uninit:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("TKQRDevice BAR_Uninit:", e);
		} catch (Exception e) {
			log.error("TKQRDevice BAR_Uninit:", e);
		}
		return barCode;
	}

	/**
	 * 
	 * @param pIn
	 * @return
	 */
	public int BAR_GetStatus(String pIn) {
		int retval = -1;
		Pointer pointerIn = null;
		Pointer pointerOut = null;

		try {
			pointerIn = new Pointer(MemoryBlockFactory.createMemoryBlock(8));
			byte[] pointerInArray = new byte[8];
			byte[] pInArray = pIn.getBytes();
			for (int i = 0; i < pInArray.length; i++) {
				pointerInArray[i] = pInArray[i];
			}
			pointerIn.setMemory(pointerInArray);

			pointerOut = new Pointer(MemoryBlockFactory.createMemoryBlock(8));
			int i = 0;

			jnativeBAR_GetStatus.setRetVal(Type.INT);
			jnativeBAR_GetStatus.setParameter(i++, pointerIn);
			jnativeBAR_GetStatus.setParameter(i++, pointerOut);
			jnativeBAR_GetStatus.invoke();

			retval = jnativeBAR_GetStatus.getRetValAsInt();
			log.debug("BAR_GetStatus retval==" + retval);
			if (retval == 0) {

			}
			pointerIn.dispose();
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("TKQRDevice BAR_GetStatus:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("TKQRDevice BAR_GetStatus:", e);
		} catch (Exception e) {
			log.error("TKQRDevice BAR_GetStatus:", e);
		}
		return retval;
	}

	/**
	 * 
	 * @return
	 */
	public String BAR_GetErrMessage() {
		String errcode = "";
		String retval = "";

		Pointer pointerOut = null;
		try {

			pointerOut = new Pointer(MemoryBlockFactory.createMemoryBlock(8));
			int i = 0;

			jnativeBAR_GetErrMessage.setRetVal(Type.INT);
			jnativeBAR_GetErrMessage.setParameter(i++, pointerOut);
			jnativeBAR_GetErrMessage.invoke();

			retval = jnativeBAR_GetErrMessage.getRetVal();
//			log.info("statusCode=="+this.statusCode);
			log.debug("jnativeBAR_GetErrMessage retval==" + retval);
//			if(retval!=this.statusCode){
//				log.info("%%%%%%ERROR%%%%");
//			}
			
			
//			if (retval == 0) {
				errcode = new String(pointerOut.getMemory());
				log.debug("errcode==" + errcode);
//			}
				
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("TKQRDevice BAR_GetErrMessage:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("TKQRDevice BAR_GetErrMessage:", e);
		} catch (Exception e) {
			log.error("TKQRDevice BAR_GetErrMessage:", e);
		}
		return retval;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TKQRDevice qrd = TKQRDevice.getInstance();
		int initRet = qrd.BAR_Init("3000");
		while (initRet == 0) {
			qrd.BAR_ReadData("500");

//			qrd.BAR_GetErrMessage();
		}
	}

}
