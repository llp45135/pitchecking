package com.rxtec.pitchecking.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xvolks.jnative.JNative;
import org.xvolks.jnative.Type;
import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.pointers.Pointer;
import org.xvolks.jnative.pointers.memory.MemoryBlockFactory;

public class LightLevelControl {
	private Logger log = LoggerFactory.getLogger("LightLevelControl");
	private static LightLevelControl _instance;
	private String dllName = "HSTL_GZD.dll";
	JNative jnativeGetGZD = null;

	public static synchronized LightLevelControl getInstance() {
		if (_instance == null) {
			_instance = new LightLevelControl();
		}
		return _instance;
	}

	private LightLevelControl() {
		// TODO Auto-generated constructor stub
		JNative.setLoggingEnabled(true);
		try {
			jnativeGetGZD = new JNative(dllName, "GetGZD");
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("init LightLevelControl:", e);
		} catch (Exception ex) {
			log.error("init LightLevelControl:", ex);
		}
	}

	/**
	 * 获取光照度监测器的当前数值
	 * 
	 * @return
	 */
	public String GetGZD() {
		int lightLevel = -1;
		int retval = -1;
		Pointer pointerOut = null;
		try {

			pointerOut = new Pointer(MemoryBlockFactory.createMemoryBlock(4));
			int i = 0;

			jnativeGetGZD.setRetVal(Type.INT);
			jnativeGetGZD.setParameter(i++, pointerOut);
			jnativeGetGZD.invoke();

			retval = jnativeGetGZD.getRetValAsInt();
			log.debug("GetGZD retval==" + retval);
			if (retval == 0) {
				lightLevel = pointerOut.getAsInt(0);
			}
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("LightLevelControl GetGZD:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("LightLevelControl GetGZD:", e);
		} catch (Exception e) {
			log.error("LightLevelControl GetGZD:", e);
		}
		return String.valueOf(lightLevel);
	}

	public static void main(String[] args) {
		LightLevelControl gzdDevice = LightLevelControl.getInstance();
		System.out.println("当前光照度数值==" + gzdDevice.GetGZD());
	}

}
