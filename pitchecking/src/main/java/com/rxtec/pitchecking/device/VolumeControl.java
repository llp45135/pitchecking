package com.rxtec.pitchecking.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xvolks.jnative.JNative;
import org.xvolks.jnative.Type;
import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.pointers.Pointer;
import org.xvolks.jnative.pointers.memory.MemoryBlockFactory;

import com.rxtec.pitchecking.utils.CommUtil;

/**
 * 声道控制
 * @author ZhaoLin
 *
 */
public class VolumeControl {
	private Logger log = LoggerFactory.getLogger("VolumeControl");

	private static VolumeControl _instance;
	private String dllName = "SetVolume.dll";
	private JNative jnativeSetDevice = null;
	private JNative jnativeLeftClose = null;
	private JNative jnativeRightClose = null;
	private JNative jnativeAllOpen = null;

	public static synchronized VolumeControl getInstance() {
		if (_instance == null) {
			_instance = new VolumeControl();
		}
		return _instance;
	}

	private VolumeControl() {
		try {
//			jnativeSetDevice = new JNative(dllName, "SetDevice ");
			jnativeLeftClose = new JNative(dllName, "LeftClose");
			jnativeRightClose = new JNative(dllName, "RightClose");
			jnativeAllOpen = new JNative(dllName, "AllOpen");
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("init VolumeControl:", e);
		} catch(Exception ex){
			log.error("init VolumeControl:", ex);
		}
	}
	
	/**
	 * 初始化
	 * @param pIn
	 * @return
	 */
	public int SetDevice(int pIn) {
		int retval = -1;

		try {
			int i = 0;

			jnativeSetDevice.setRetVal(Type.INT);
			jnativeSetDevice.setParameter(i++, pIn);
			jnativeSetDevice.invoke();

			retval = jnativeSetDevice.getRetValAsInt();
			log.info("jnativeSetDevice retval==" + retval);
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("VolumeControl SetDevice:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("VolumeControl SetDevice:", e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("VolumeControl SetDevice:", e);
		}
		return retval;
	}
	
	/**
	 * 左声道关闭
	 * @return
	 */
	public int LeftClose() {
		int retval = -1;

		try {
			int i = 0;

			jnativeLeftClose.setRetVal(Type.INT);
			jnativeLeftClose.invoke();

			retval = jnativeLeftClose.getRetValAsInt();
			log.info("jnativeLeftClose retval==" + retval);
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("VolumeControl LeftClose:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("VolumeControl LeftClose:", e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("VolumeControl LeftClose:", e);
		}
		return retval;
	}
	
	public int RightClose() {
		int retval = -1;

		try {
			int i = 0;

			jnativeRightClose.setRetVal(Type.INT);
			jnativeRightClose.invoke();

			retval = jnativeRightClose.getRetValAsInt();
			log.info("jnativeRightClose retval==" + retval);
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("VolumeControl RightClose:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("VolumeControl RightClose:", e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("VolumeControl RightClose:", e);
		}
		return retval;
	}
	
	
	public int AllOpen() {
		int retval = -1;

		try {
			int i = 0;

			jnativeAllOpen.setRetVal(Type.INT);
			jnativeAllOpen.invoke();

			retval = jnativeAllOpen.getRetValAsInt();
			log.info("jnativeAllOpen retval==" + retval);
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("VolumeControl AllOpen:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("VolumeControl AllOpen:", e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("VolumeControl AllOpen:", e);
		}
		return retval;
	}
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		VolumeControl vcc = VolumeControl.getInstance();
//		vcc.SetDevice(0);
		vcc.LeftClose();
		CommUtil.sleep(3000);
		vcc.RightClose();
		CommUtil.sleep(3000);
		vcc.AllOpen();
	}

}
