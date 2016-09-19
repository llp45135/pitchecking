package com.rxtec.pitchecking.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xvolks.jnative.JNative;
import org.xvolks.jnative.Type;
import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.pointers.Pointer;
import org.xvolks.jnative.pointers.memory.MemoryBlockFactory;

public class GateDevice {
	
	private Logger log = LoggerFactory.getLogger("GateDevice");
	private static GateDevice _instance = new GateDevice();
	private String dllName = "GAT_GTAg.dll";
	JNative jnativeGAT_Init = null;
	JNative jnativeGAT_Uninit = null;
	JNative jnativeGAT_SetMode = null;
	JNative jnativeGAT_SetLampAndBeepStatus = null;
	JNative jnativeGAT_Control = null;
	JNative jnativeGAT_GetStatus = null;
	

	public static GateDevice getInstance() {
		return _instance;
	}

	private GateDevice() {
		// TODO Auto-generated constructor stub
		JNative.setLoggingEnabled(false);
		this.initJnative();
	}

	private void initJnative() {

		try {
			jnativeGAT_Init = new JNative(dllName, "GAT_Init");
			jnativeGAT_Uninit = new JNative(dllName, "GAT_Uninit");
			jnativeGAT_SetMode = new JNative(dllName, "GAT_SetMode");
			jnativeGAT_SetLampAndBeepStatus = new JNative(dllName, "GAT_SetLampAndBeepStatus");
			jnativeGAT_Control = new JNative(dllName, "GAT_Control");
			jnativeGAT_GetStatus = new JNative(dllName, "GAT_GetStatus");

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
	public int GAT_Init(String pIn) {
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

			jnativeGAT_Init.setRetVal(Type.INT);
			jnativeGAT_Init.setParameter(i++, pIn);
			jnativeGAT_Init.setParameter(i++, pointerOut);
			jnativeGAT_Init.invoke();

			retval = jnativeGAT_Init.getRetValAsInt();
			log.debug("GAT_Init retval==" + retval);
			if (retval == 0) {

			}
			pointerIn.dispose();
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("GateDevice GAT_Init:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("GateDevice GAT_Init:", e);
		} catch (Exception e) {
			log.error("GateDevice GAT_Init:", e);
		}
		return retval;
	}
	
	/**
	 * 
	 * @param pIn
	 * @return
	 */
	public int GAT_Uninit(String pIn) {
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

			jnativeGAT_Uninit.setRetVal(Type.INT);
			jnativeGAT_Uninit.setParameter(i++, pIn);
			jnativeGAT_Uninit.setParameter(i++, pointerOut);
			jnativeGAT_Uninit.invoke();

			retval = jnativeGAT_Uninit.getRetValAsInt();
			log.debug("GAT_Uninit retval==" + retval);
			if (retval == 0) {

			}
			pointerIn.dispose();
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("GateDevice GAT_Uninit:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("GateDevice GAT_Uninit:", e);
		} catch (Exception e) {
			log.error("GateDevice GAT_Uninit:", e);
		}
		return retval;
	}
	
	/**
	 * 
	 * @param pIn
	 * @return
	 */
	public int GAT_SetMode(String pIn) {
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

			jnativeGAT_SetMode.setRetVal(Type.INT);
			jnativeGAT_SetMode.setParameter(i++, pIn);
			jnativeGAT_SetMode.setParameter(i++, pointerOut);
			jnativeGAT_SetMode.invoke();

			retval = jnativeGAT_SetMode.getRetValAsInt();
			log.debug("GAT_SetMode retval==" + retval);
			if (retval == 0) {

			}
			pointerIn.dispose();
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("GateDevice GAT_SetMode:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("GateDevice GAT_SetMode:", e);
		} catch (Exception e) {
			log.error("GateDevice GAT_SetMode:", e);
		}
		return retval;
	}
	
	/**
	 * 
	 * @param pIn
	 * @return
	 */
	public int GAT_Control(String pIn) {
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

			jnativeGAT_Control.setRetVal(Type.INT);
			jnativeGAT_Control.setParameter(i++, pIn);
			jnativeGAT_Control.setParameter(i++, pointerOut);
			jnativeGAT_Control.invoke();

			retval = jnativeGAT_Control.getRetValAsInt();
			log.debug("GAT_Control retval==" + retval);
			if (retval == 0) {

			}
			pointerIn.dispose();
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("GateDevice GAT_Control:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("GateDevice GAT_Control:", e);
		} catch (Exception e) {
			log.error("GateDevice GAT_Control:", e);
		}
		return retval;
	}
	
	public int GAT_GetStatus(String pIn) {
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

			jnativeGAT_GetStatus.setRetVal(Type.INT);
			jnativeGAT_GetStatus.setParameter(i++, pIn);
			jnativeGAT_GetStatus.setParameter(i++, pointerOut);
			jnativeGAT_GetStatus.invoke();

			retval = jnativeGAT_GetStatus.getRetValAsInt();
			log.debug("GAT_GetStatus retval==" + retval);
			if (retval == 0) {

			}
			pointerIn.dispose();
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("GateDevice GAT_GetStatus:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("GateDevice GAT_GetStatus:", e);
		} catch (Exception e) {
			log.error("GateDevice GAT_GetStatus:", e);
		}
		return retval;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
