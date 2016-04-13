package com.rxtec.pitchecking.picheckingservice;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Calendar;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xvolks.jnative.JNative;
import org.xvolks.jnative.Type;
import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.pointers.Pointer;
import org.xvolks.jnative.pointers.memory.MemoryBlockFactory;

public class FaceVerifyJniEntry {
	
	public FaceVerifyJniEntry(){
		initJNIContext();
	}
	JNative jnativeInitFun = null;
	JNative jnativeVerifyFun = null;
	JNative jnativeExitFun = null;
	private Logger log = LoggerFactory.getLogger("FaceAuthentication");
	
	
	private void initJNIContext(){
		try {
			jnativeInitFun = new JNative("FaceVRISDK.dll","PS_InitFaceIDSDK");
			jnativeInitFun.setRetVal(Type.INT); 
			jnativeInitFun.invoke(); 
			log.debug("PS_InitFaceIDSDK ret: " + jnativeInitFun.getRetVal());//获取返回值
			jnativeVerifyFun = new JNative("FaceVRISDK.dll","PS_VerifyImage");
		}catch (NativeException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}  finally {   
		}


	}
	
	public void clearJNIContext(){
		try{
			jnativeExitFun = new JNative("FaceVRISDK.dll","PS_ExitFaceIDSDK");
			jnativeExitFun.setRetVal(Type.INT); 
			jnativeExitFun.invoke(); 
			log.debug("PS_ExitFaceIDSDK ret: " + jnativeExitFun.getRetVal());//获取返回值
			jnativeExitFun.dispose(); //注意：JNative1.3.2任意调用一个一次就可以了，不允许多次
		}catch (NativeException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}  finally {   
		}
	}
	
	public float verify(byte[] frameBytes, byte[] idCardBytes) {
		float result = 0;
		
		long nowMils = Calendar.getInstance().getTimeInMillis();
		
		try {

			Pointer aArrIntInputf = new Pointer(MemoryBlockFactory.createMemoryBlock(8));
			aArrIntInputf.setFloatAt(0, -1);
			int i = 0; 
			log.debug("aArrIntInputf: " + aArrIntInputf.getAsFloat(0));//获取返回值

			jnativeVerifyFun.setRetVal(Type.INT); 
			jnativeVerifyFun.setParameter(i++, Type.STRING,frameBytes); 
			jnativeVerifyFun.setParameter(i++, Type.INT,""+frameBytes.length); 
			jnativeVerifyFun.setParameter(i++, Type.STRING,idCardBytes); 
			jnativeVerifyFun.setParameter(i++, Type.INT,""+idCardBytes.length); 
			jnativeVerifyFun.setParameter(i++, aArrIntInputf);
			jnativeVerifyFun.invoke();
			//打印函数返回值 
			//log.debug("PS_VerifyImage retCode=" + jnative3.getRetValAsInt());//获取返回值

			//获取输出参数值（验证分数值）
			result = aArrIntInputf.getAsFloat(0);
			aArrIntInputf.dispose();
			long usingTime = Calendar.getInstance().getTimeInMillis() - nowMils;
			log.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!FaceChecking succ, using " + usingTime + " ms, value=" + result);
			
			
		} catch (NativeException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}  finally {   
		}
		return result;
	}
	
	
	public static void main(String[] args) {

		FaceVerifyJniEntry v = new FaceVerifyJniEntry();
		IDCard c1 = createIDCard("C:/DCZ/20160412/43040619900308255x.jpg");
		IDCard c2 = createIDCard("C:/DCZ/20160412/out.jpg");
				
		v.verify(c1.getImageBytes(), c2.getImageBytes());
		v.verify(c1.getImageBytes(), c2.getImageBytes());
		v.verify(c1.getImageBytes(), c2.getImageBytes());
		v.verify(c1.getImageBytes(), c2.getImageBytes());
		v.verify(c1.getImageBytes(), c2.getImageBytes());
		v.verify(c1.getImageBytes(), c2.getImageBytes());

	}

	
	private static IDCard createIDCard(String fn){
		IDCard card = new IDCard();
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(new File(fn));
		} catch (Exception e) {
			e.printStackTrace();
		}

		card.setCardImage(bi);
		return card;
	}
}
