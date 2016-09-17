package com.rxtec.pitchecking.device;

import java.io.UnsupportedEncodingException;

import org.xvolks.jnative.JNative;
import org.xvolks.jnative.Type;
import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.pointers.Pointer;
import org.xvolks.jnative.pointers.memory.MemoryBlockFactory;

public class TKICCDevcie {

	private String dllName = "ICC_EWTa.dll";
	JNative jnativeICCInit = null;
	JNative jnativeICCClose = null;
	JNative jnativeICCGetVersInfo = null;

	public TKICCDevcie() {
		JNative.setLoggingEnabled(true);
		this.initJnative();
	}

	private void initJnative() {

		try {
			jnativeICCInit = new JNative(dllName, "ICC_Init");
			jnativeICCClose = new JNative(dllName, "ICC_Close");
			jnativeICCGetVersInfo = new JNative(dllName, "ICC_GetVersInfo");
			
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void InitIcc() {
		String retval = "";
		Pointer pointerIn = null;
		Pointer pointerOut = null;

		try {
			pointerIn = new Pointer(MemoryBlockFactory.createMemoryBlock(8));
			pointerIn.setMemory(new byte[8]);
			pointerOut = new Pointer(MemoryBlockFactory.createMemoryBlock(8));
			int i = 0;

			jnativeICCInit.setRetVal(Type.INT);
			jnativeICCInit.setParameter(i++, pointerIn);
			jnativeICCInit.setParameter(i++, pointerOut);
			jnativeICCInit.invoke();

			retval = jnativeICCInit.getRetVal();
			System.out.println("InitIcc pointerOut==" + new String(pointerOut.getMemory(), "utf-16") );
			System.out.println("InitIcc retval==" + retval);
			pointerIn.dispose();
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	public void ICC_GetVersInfo() {
		int retval = -1;
		String pIn = "";
		Pointer pointerOut = null;
		Pointer pOutLen = null;

		try {
			pointerOut = new Pointer(MemoryBlockFactory.createMemoryBlock(1024));
			pOutLen = new Pointer(MemoryBlockFactory.createMemoryBlock(8));
			int i = 0;
			jnativeICCGetVersInfo.setRetVal(Type.INT);
			jnativeICCGetVersInfo.setParameter(i++, pointerOut);
			jnativeICCGetVersInfo.setParameter(i++, pOutLen);
			jnativeICCGetVersInfo.invoke();

			retval = jnativeICCGetVersInfo.getRetValAsInt();
			System.out.println("ICC_GetVersInfo pOUt==" + pointerOut.getAsString());
			System.out.println("ICC_GetVersInfo retval==" + retval);
			pointerOut.dispose();
			pOutLen.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void CloseIcc() {
		int retval = -1;
		String pIn = "";
		String pOut = "";
		Pointer pointerOut = null;

		try {
			pointerOut = new Pointer(MemoryBlockFactory.createMemoryBlock(8));
			int i = 0;
			jnativeICCClose.setRetVal(Type.INT);
			jnativeICCClose.setParameter(i++, pIn);
			jnativeICCClose.setParameter(i++, pointerOut);
			jnativeICCClose.invoke();

			retval = jnativeICCClose.getRetValAsInt();
			System.out.println("CloseIcc pOUt==" + pointerOut.getAsString());
			System.out.println("CloseIcc retval==" + retval);
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		TKICCDevcie icc = new TKICCDevcie();
		icc.InitIcc();
		icc.ICC_GetVersInfo();
		icc.CloseIcc();
	}
}
