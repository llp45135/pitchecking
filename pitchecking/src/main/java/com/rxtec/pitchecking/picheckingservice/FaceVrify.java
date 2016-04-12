package com.rxtec.pitchecking.picheckingservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xvolks.jnative.JNative;
import org.xvolks.jnative.Type;
import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.pointers.Pointer;
import org.xvolks.jnative.pointers.memory.MemoryBlockFactory;

import com.rxtec.pitchecking.utils.CommUtil;

/**
 * 
 * @author ZhaoLin
 *
 */
public class FaceVrify {
	private Logger log = LoggerFactory.getLogger("FaceLocationDetect");
	private static FaceVrify instance = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JNative.setLoggingEnabled(true);
		// test();
		byte[] bytecp = CommUtil.getBytes("face1.jpg");
		byte[] byteicp = CommUtil.getBytes("face2.jpg");
		FaceVrify.getInstance().duibi(bytecp, byteicp);
	}

	private FaceVrify() {

	}

	public static synchronized FaceVrify getInstance() {
		if (instance == null) {
			instance = new FaceVrify();
		}
		return instance;
	}

	// @SuppressWarnings("deprecation")
	public String duibi(byte[] dfaceBtyeArray, byte[] idcardByteArray) {
		String ret = "failue";
		JNative jnative1 = null;
		JNative jnative2 = null;
		JNative jnative3 = null;
		try {
			log.debug("PS_InitFaceIDSDK");
			jnative1 = new JNative("FaceVRISDK.dll", "PS_InitFaceIDSDK");
			jnative1.setRetVal(Type.INT);
			jnative1.invoke();
			log.debug("PS_InitFaceIDSDK retval==" + jnative1.getRetVal());// 获取返回值

			Pointer aArrIntInputf = new Pointer(MemoryBlockFactory.createMemoryBlock(8));
			aArrIntInputf.setFloatAt(0, -1);
			int i = 0;
			log.debug("验证初始值："+String.valueOf(aArrIntInputf.getAsFloat(0)));// 获取初参数

			log.debug("PS_VerifyImage");
			jnative2 = new JNative("FaceVRISDK.dll", "PS_VerifyImage");
			jnative2.setRetVal(Type.INT);
			jnative2.setParameter(i++, Type.STRING, dfaceBtyeArray);
			jnative2.setParameter(i++, Type.INT, "" + dfaceBtyeArray.length);
			jnative2.setParameter(i++, Type.STRING, idcardByteArray);
			jnative2.setParameter(i++, Type.INT, "" + idcardByteArray.length);
			jnative2.setParameter(i++, aArrIntInputf);
			jnative2.invoke();
			// 打印函数返回值
			log.debug("PS_VerifyImage retval==" + jnative2.getRetValAsInt());// 获取返回值
			// 获取输出参数值（验证分数值）
			log.debug("验证分数值:"+String.valueOf(aArrIntInputf.getAsFloat(0)));
			aArrIntInputf.dispose();

			jnative3 = new JNative("FaceVRISDK.dll", "PS_ExitFaceIDSDK");
			jnative3.setRetVal(Type.INT);
			jnative3.invoke();
			log.debug("PS_ExitFaceIDSDK retval==" + jnative3.getRetVal());// 获取返回值

			// jnative1.dispose();
			// jnative2.dispose();
			jnative3.dispose(); // 注意：JNative1.3.2任意调用一个一次就可以了，不允许多次

			ret = "success";
		} catch (NativeException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} finally {
		}
		return ret;
	}
}
