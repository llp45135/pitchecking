package com.rxtec.pitchecking.device;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xvolks.jnative.JNative;
import org.xvolks.jnative.Type;
import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.pointers.Pointer;
import org.xvolks.jnative.pointers.memory.MemoryBlockFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.FaceTrackingScreen;
import com.rxtec.pitchecking.gui.FaceCheckFrame;
import com.rxtec.pitchecking.mqtt.MqttReceiverBroker;
import com.rxtec.pitchecking.mqtt.MqttSenderBroker;
import com.rxtec.pitchecking.net.event.CAMOpenBean;
import com.rxtec.pitchecking.utils.CommUtil;

public class CAMDevice {
	private Logger log = LoggerFactory.getLogger("CAMDevice");
	private static CAMDevice _instance = new CAMDevice();
	private String dllName = "CAM_RXTa.dll";
	private JNative jnativeCAM_Open = null;
	private JNative jnativeCAM_Close = null;
	private JNative jnativeCAM_Notify = null;
	private JNative jnativeCAM_GetPhotoInfo = null;
	private JNative jnativeCAM_ScreenDisplay = null;

	public static CAMDevice getInstance() {
		return _instance;
	}

	private CAMDevice() {
		try {
			jnativeCAM_Open = new JNative(dllName, "CAM_Open");
			jnativeCAM_Close = new JNative(dllName, "CAM_Close");
			jnativeCAM_Notify = new JNative(dllName, "CAM_Notify");
			jnativeCAM_GetPhotoInfo = new JNative(dllName, "CAM_GetPhotoInfo");
			jnativeCAM_ScreenDisplay = new JNative(dllName, "CAM_ScreenDisplay");
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("init CAMDevice:", e);
		} catch (Exception ex) {
			log.error("init CAMDevice:", ex);
		}
	}

	/**
	 * 打开摄像头设备
	 */
	public int CAM_Open(int[] pIn) {
		int retval = -1;
		Pointer pointerIn = null;
		Pointer pointerOut = null;

		try {
			pointerIn = new Pointer(MemoryBlockFactory.createMemoryBlock(128));

			byte[] P_T_REGION = new byte[7 * 4];
			for (int i = 0; i < 7; i++) {
				byte[] ss = CommUtil.intToBytes(pIn[i]);
				for (int k = 0; k < 4; k++) {
					P_T_REGION[i * 4 + k] = ss[k];
				}
			}

			pointerIn.zeroMemory();
			pointerIn.setMemory(P_T_REGION);

			pointerOut = new Pointer(MemoryBlockFactory.createMemoryBlock(8));
			pointerOut.zeroMemory();
			int i = 0;

			jnativeCAM_Open.setRetVal(Type.INT);
			jnativeCAM_Open.setParameter(i++, pointerIn);
			jnativeCAM_Open.setParameter(i++, pointerOut);
			jnativeCAM_Open.invoke();

			retval = jnativeCAM_Open.getRetValAsInt();
			log.debug("CAM_Open retval==" + retval);
			pointerIn.dispose();
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("CAMDevice CAM_Open:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("CAMDevice CAM_Open:", e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("CAMDevice CAM_Open:", e);
		}
		return retval;
	}

	/**
	 * 关闭摄像头设备
	 * 
	 * @param pIn
	 */
	public void CAM_Close(byte[] pIn) {
		String retval = "";
		Pointer pointerIn = null;
		Pointer pointerOut = null;

		try {
			pointerIn = new Pointer(MemoryBlockFactory.createMemoryBlock(28));
			pointerIn.setMemory(pIn);
			pointerOut = new Pointer(MemoryBlockFactory.createMemoryBlock(8));
			int i = 0;

			jnativeCAM_Close.setRetVal(Type.INT);
			jnativeCAM_Close.setParameter(i++, pointerIn);
			jnativeCAM_Close.setParameter(i++, pointerOut);
			jnativeCAM_Close.invoke();

			retval = jnativeCAM_Close.getRetVal();
			log.debug("CAM_Close retval==" + retval);
			pointerIn.dispose();
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("CAMDevice CAM_Close:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("CAMDevice CAM_Close:", e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("CAMDevice CAM_Close:", e);
		}
	}

	/**
	 * 通知摄像头设备执行某项操作，不等待执行结果
	 * 
	 * @param iFlag
	 * @param uuidStr
	 * @param photoDir
	 */
	public int CAM_Notify(int iFlag, String uuidStr, String photoDir) {
		int retval = -1;
		Pointer pointerIn = null;
		Pointer pointerOut = null;

		try {
			pointerIn = new Pointer(MemoryBlockFactory.createMemoryBlock(36 + 1024));

			byte[] notifyInfo = new byte[36 + 1024];
			byte[] UUID = uuidStr.getBytes();
			for (int i = 0; i < UUID.length; i++) {
				notifyInfo[i] = UUID[i];
			}

			byte[] IDPhoto = photoDir.getBytes();
			for (int i = 0; i < IDPhoto.length; i++) {
				notifyInfo[i + 36] = IDPhoto[i];
			}

			pointerIn.setMemory(notifyInfo);
			pointerOut = new Pointer(MemoryBlockFactory.createMemoryBlock(1024));
			int i = 0;

			jnativeCAM_Notify.setRetVal(Type.INT);
			jnativeCAM_Notify.setParameter(i++, iFlag);
			jnativeCAM_Notify.setParameter(i++, pointerIn);
			jnativeCAM_Notify.setParameter(i++, pointerOut);
			jnativeCAM_Notify.invoke();

			retval = jnativeCAM_Notify.getRetValAsInt();
			log.debug("CAM_Notify retval==" + retval);
			pointerIn.dispose();
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("CAMDevice CAM_Notify:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("CAMDevice CAM_Notify:", e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("CAMDevice CAM_Notify:", e);
		}
		return retval;
	}

	/**
	 * 获取身份证照片的比对结果 输入参数： pIn:唯一识别码（UUID） iDelay:延时时间(秒)，函数必须在延时到iDelay时间到后返回
	 * 输出参数： pOut：设备返回的图片信息， 结构体tIDPhotoInfo typedef struct { int
	 * iResult;//相似度0-100 char UUID[36]; //唯一识别码 int iPhotoLen1; char
	 * Photo1[100000]; //photo int iPhotoLen2; char Photo2[100000]; //photo int
	 * iPhotoLen3; char Photo3[100000]; //photo }tIDPhotoInfo;
	 * 
	 */
	public int CAM_GetPhotoInfo(String uuidStr, int iDelay) {
		int retval = -1;
		Pointer pointerUUID = null;
		Pointer pointerOut = null;
		log.debug("CAM_GetPhotoInfo 开始等待==");
		try {
			pointerUUID = new Pointer(MemoryBlockFactory.createMemoryBlock(36));

			byte[] uuidArray = new byte[36];
			byte[] uuidStr_array = uuidStr.getBytes();
			for (int i = 0; i < uuidStr_array.length; i++) {
				uuidArray[i] = uuidStr_array[i];
			}
			pointerUUID.setMemory(uuidArray);

			pointerOut = new Pointer(
					MemoryBlockFactory.createMemoryBlock(4 + 36 + 4 + 100000 + 4 + 100000 + 4 + 100000));

			pointerOut.zeroMemory();

			// log.debug("size=="+pointerOut.getSize());

			int i = 0;

			jnativeCAM_GetPhotoInfo.setRetVal(Type.INT);
			jnativeCAM_GetPhotoInfo.setParameter(i++, pointerUUID);
			jnativeCAM_GetPhotoInfo.setParameter(i++, pointerOut);
			jnativeCAM_GetPhotoInfo.setParameter(i++, iDelay);
			jnativeCAM_GetPhotoInfo.invoke();

			retval = jnativeCAM_GetPhotoInfo.getRetValAsInt();
			log.debug("CAM_GetPhotoInfo retval==" + retval);

			if (retval == 0) {
				// byte[] iResult = new byte[4];
				// for (int k = 0; k < 4; k++) {
				// iResult[k] = pointerOut.getAsByte(k);
				// }
				// log.debug("iResult==" + CommUtil.bytesToInt(iResult, 0));
				//
				// byte[] uuid = new byte[36];
				// for (int k = 0; k < 36; k++) {
				// uuid[k] = pointerOut.getAsByte(k + 4);
				// }
				// log.debug("uuid==" + new String(uuid));
				//
				// byte[] iPhotoLen1 = new byte[4];
				// for (int k = 0; k < 4; k++) {
				// iPhotoLen1[k] = pointerOut.getAsByte(k + 4 + 36);
				// }
				// int len1 = CommUtil.bytesToInt(iPhotoLen1, 0);
				// log.debug("iPhotoLen1==" + len1);
				//
				// byte[] Photo1 = new byte[len1];
				// for (int k = 0; k < len1; k++) {
				// Photo1[k] = pointerOut.getAsByte(k + 4 + 36 + 4);
				// }
				// // CommUtil.byte2image(Photo1, "D:/maven/git/a1.jpg");
				// Photo1 = null;
				// // log.debug("Photo1==" + new String(Photo1));
				// byte[] iPhotoLen2 = new byte[4];
				// for (int k = 0; k < 4; k++) {
				// iPhotoLen2[k] = pointerOut.getAsByte(k + 4 + 36 + 4 +
				// 100000);
				// }
				// int len2 = CommUtil.bytesToInt(iPhotoLen2, 0);
				// log.debug("iPhotoLen2==" + len2);
				//
				// byte[] Photo2 = new byte[len2];
				// for (int k = 0; k < len2; k++) {
				// Photo2[k] = pointerOut.getAsByte(k + 4 + 36 + 4 + 100000 +
				// 4);
				// }
				// // CommUtil.byte2image(Photo2, "D:/maven/git/a2.jpg");
				// Photo2 = null;
				//
				// byte[] iPhotoLen3 = new byte[4];
				// for (int k = 0; k < 4; k++) {
				// iPhotoLen3[k] = pointerOut.getAsByte(k + 4 + 36 + 4 + 100000
				// + 4 + 100000);
				// }
				// int len3 = CommUtil.bytesToInt(iPhotoLen3, 0);
				// log.debug("iPhotoLen3==" + len3);
				//
				// byte[] Photo3 = new byte[len3];
				// for (int k = 0; k < len3; k++) {
				// Photo3[k] = pointerOut.getAsByte(k + 4 + 36 + 4 + 100000 + 4
				// + 100000 + 4);
				// }
				// CommUtil.byte2image(Photo3,
				// "C:/maven/git/pitchecking/a3.jpg");
				// Photo3 = null;
			}
			pointerUUID.dispose();
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("CAMDevice CAM_GetPhotoInfo:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("CAMDevice CAM_GetPhotoInfo:", e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("CAMDevice CAM_GetPhotoInfo:", e);
		}
		return retval;
	}

	/**
	 * 
	 * @param pIn
	 * @param timeout
	 * @return
	 */
	public int CAM_ScreenDisplay(String pIn, int timeout) {
		int retval = -1;
		Pointer pointerIn = null;
		Pointer pointerOut = null;
		try {
			pointerOut = new Pointer(MemoryBlockFactory.createMemoryBlock(8));

			pointerOut.zeroMemory();

			// log.debug("size=="+pointerOut.getSize());

			int i = 0;

			jnativeCAM_ScreenDisplay.setRetVal(Type.INT);
			jnativeCAM_ScreenDisplay.setParameter(i++, pIn);
			jnativeCAM_ScreenDisplay.setParameter(i++, timeout);
			jnativeCAM_ScreenDisplay.setParameter(i++, pointerOut);
			jnativeCAM_ScreenDisplay.invoke();

			retval = jnativeCAM_ScreenDisplay.getRetValAsInt();
			log.debug("CAM_ScreenDisplay retval==" + retval);

			if (retval == 0) {

			}
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("CAMDevice CAM_ScreenDisplay:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("CAMDevice CAM_ScreenDisplay:", e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("CAMDevice CAM_ScreenDisplay:", e);
		}
		return retval;
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Logger log = LoggerFactory.getLogger("DeviceEventListener");
		MqttReceiverBroker mqtt = MqttReceiverBroker
				.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT + Config.getInstance().getCameraNum());
		FaceTrackingScreen faceTrackingScreen = FaceTrackingScreen.getInstance();
		FaceCheckFrame faceCheckFrame = new FaceCheckFrame();
		faceTrackingScreen.setFaceFrame(faceCheckFrame);
		boolean initUIFlag = false;
		try {
			faceTrackingScreen.initUI(DeviceConfig.getInstance().getFaceScreen());
			initUIFlag = true;
		} catch (Exception ex) {
			log.error("PITrackingApp:", ex);
		}
		if (!initUIFlag) {
			try {
				faceTrackingScreen.initUI(0);
				initUIFlag = true;
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				log.error("PITrackingApp:", ex);
			}
		}
		if (initUIFlag) {
			faceTrackingScreen.getFaceFrame().showDefaultContent();
		} else {
			return;
		}

		// while (true) {
		// ObjectMapper mapper = new ObjectMapper();
		// CAMOpenBean camOpenBean = new CAMOpenBean();
		// camOpenBean.setEventDirection(1);
		// camOpenBean.setThreshold(65);
		// camOpenBean.setTimeout(3000);
		// try {
		// String camOpenRequestJson = mapper.writeValueAsString(camOpenBean);
		// MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendMessage("pub_topic",
		// camOpenRequestJson);
		// } catch (JsonProcessingException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// CommUtil.sleep(100);
		// if(MqttSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).getCamOpenRetval()==0){
		// break;
		// }
		// CommUtil.sleep(2000);
		// }

		CAMDevice cam = CAMDevice.getInstance();

		int[] region = { 0, 0, 640, 480, 77, 1, 3000 };
		int openRet = cam.CAM_Open(region);
		if (openRet == 0) {
			String uuidStr = "520203197912141118";
			String IDPhoto_str = "zp.jpg";
			int notify = cam.CAM_Notify(1, uuidStr, IDPhoto_str);
			if (notify == 0) {
				int getPhotoRet = cam.CAM_GetPhotoInfo(uuidStr, 20);
				System.out.println("getPhotoRet==" + getPhotoRet);
				if (getPhotoRet == 0) {
					int ss = cam.CAM_ScreenDisplay("验证失败#请从边门离开", 3);
					System.out.println("ss==" + ss);
				}
			}
		}

	}
}
