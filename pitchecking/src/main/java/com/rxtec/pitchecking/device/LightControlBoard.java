package com.rxtec.pitchecking.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xvolks.jnative.JNative;
import org.xvolks.jnative.Type;
import org.xvolks.jnative.exceptions.NativeException;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.utils.CommUtil;

/**
 * 错误码 错误值 标识符 错误描述 0 _CB_SUCCESS 函数执行成功;
 * 
 * -4 _CB_FAILURE 一般性错误; -13 _CB_VFY_INV_PARAMETER 参数错误; -17
 * _CB_LICENSE_MISMATCH 授权不匹配; -264 _CB_COM_NOT_OPEN 端口未打开
 * 
 * @author ZhaoLin
 *
 */
public class LightControlBoard {
	private static Logger log = LoggerFactory.getLogger("LightControlBoard");

	private static LightControlBoard _instance = new LightControlBoard();

	private JNative Cb_InitJnative = null;
	private JNative Cb_ExitJnative = null;
	private JNative Cb_OpenComJnative = null;
	private JNative Cb_ScanAndOpenComJnative = null;
	private JNative Cb_CloseComJnative = null;
	private JNative Cb_IsComOpenJnative = null;
	private JNative Cb_LightUnitOnJnative = null;
	private JNative Cb_LightUnitOffJnative = null;
	private JNative Cb_EnableShortPressJnative = null;

	public static LightControlBoard getInstance() {
		return _instance;
	}

	private LightControlBoard() {
		try {
			Cb_InitJnative = new JNative("ControlBoardSDK.dll", "Cb_InitSDK");
			Cb_ExitJnative = new JNative("ControlBoardSDK.dll", "Cb_ExitSDK");
			Cb_OpenComJnative = new JNative("ControlBoardSDK.dll", "Cb_OpenCom");
			Cb_ScanAndOpenComJnative = new JNative("ControlBoardSDK.dll", "Cb_ScanAndOpenCom");
			Cb_CloseComJnative = new JNative("ControlBoardSDK.dll", "Cb_CloseCom");
			Cb_IsComOpenJnative = new JNative("ControlBoardSDK.dll", "Cb_IsComOpen");
			Cb_LightUnitOnJnative = new JNative("ControlBoardSDK.dll", "Cb_LightUnitOn");
			Cb_LightUnitOffJnative = new JNative("ControlBoardSDK.dll", "Cb_LightUnitOff");
			Cb_EnableShortPressJnative = new JNative("ControlBoardSDK.dll", "Cb_EnableShortPress");
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 点亮摄像头补光灯
	 * 
	 * @return
	 */
	public int startLED() {
		// LightControlBoard cb = LightControlBoard.getInstance();
		if (this.Cb_InitSDK() == 0) {
			if (this.Cb_OpenCom(DeviceConfig.getInstance().getCameraLEDPort()) == 0) {
				if (this.Cb_LightUnitOff(DeviceConfig.CameraLEDUnit, DeviceConfig.CameraLEDLevel) != 0) {
					return 0;
				}
				CommUtil.sleep(1000);
				if (this.Cb_LightUnitOn(DeviceConfig.CameraLEDUnit, DeviceConfig.CameraLEDLevel) != 0) {
					return 0;
				}
			} else {
				return 0;
			}
		} else {
			return 0;
		}
		return 1;
	}

	/**
	 * 初始化API，程序开始时调用一次
	 * 
	 * @return
	 */
	public int Cb_InitSDK() {
		int retval = -1;
		try {
			int i = 0;
			Cb_InitJnative.setParameter(i, "");
			Cb_InitJnative.setRetVal(Type.INT);
			Cb_InitJnative.invoke();
			retval = Cb_InitJnative.getRetValAsInt();
			log.debug("Cb_InitJnative:retval==" + retval);// 获取返回值
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retval;
	}

	/**
	 * 卸载 API，释放资源，程序结束时调用一次
	 */
	public void Cb_ExitSDK() {
		try {
			// Cb_ExitJnative.setRetVal(Type.INT);
			Cb_ExitJnative.invoke();
			// retval = Cb_InitJnative.getRetValAsInt();
			// log.debug("Cb_ExitJnative : retval==" + retval);// 获取返回值
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 函数：int __stdcall Cb_OpenCom(int nCom); 参数：int nCom [in] 端口号
	 * 返回：成功为0，其它为错误值见 错误码 说明：打开指定端口。
	 * 
	 * @param port
	 * @return
	 */
	public int Cb_OpenCom(int port) {
		int retval = -1;
		try {
			int i = 0;
			Cb_OpenComJnative.setParameter(i++, port);
			Cb_OpenComJnative.setRetVal(Type.INT);
			Cb_OpenComJnative.invoke();
			retval = Cb_OpenComJnative.getRetValAsInt();
			log.debug("Cb_OpenComJnative : retval==" + retval);// 获取返回值
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retval;
	}

	/**
	 * 函数：int __stdcall Cb_ScanAndOpenCom(); 参数：无 返回：成功为0，其它为错误值见 错误码
	 * 说明：自动扫描端口并打开第一个可用端口
	 */
	public int Cb_ScanAndOpenCom() {
		int retval = -1;
		try {
			Cb_ScanAndOpenComJnative.setRetVal(Type.INT);
			Cb_ScanAndOpenComJnative.invoke();
			retval = Cb_ScanAndOpenComJnative.getRetValAsInt();
			log.debug("Cb_ScanAndOpenComJnative : retval==" + retval);// 获取返回值
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retval;
	}

	/**
	 * 函数：int __stdcall Cb_IsComOpen(bool *pOpen); 参数：bool *pOpen [out] 是否已打开端口
	 * 返回：成功为0，其它为错误值见 错误码 说明：指示是否已打开端口。
	 */
	public int Cb_IsComOpen() {
		int retval = -1;
		try {
			String isOpen = "";
			int i = 0;
			Cb_IsComOpenJnative.setParameter(i++, isOpen);
			Cb_IsComOpenJnative.setRetVal(Type.INT);
			Cb_IsComOpenJnative.invoke();
			retval = Cb_IsComOpenJnative.getRetValAsInt();
			log.debug("Cb_IsComOpenJnative : retval==" + retval);// 获取返回值
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retval;
	}

	/**
	 * 函数：int __stdcall Cb_CloseCom(); 参数：无 返回：成功为0，其它为错误值见 错误码 说明：关闭端口，释放资源。
	 */
	public int Cb_CloseCom() {
		int retval = -1;
		try {
			Cb_CloseComJnative.setRetVal(Type.INT);
			Cb_CloseComJnative.invoke();
			retval = Cb_CloseComJnative.getRetValAsInt();
			log.debug("Cb_CloseComJnative : retval==" + retval);// 获取返回值
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retval;
	}

	/**
	 * 函数：int __stdcall Cb_LightUnitOn(int unit, int level); 参数：int unit,
	 * [in]灯组号，有效值为0~1 int level [in]亮度值，-1到31 返回：成功为0，其它为错误值见 错误码 说明：设置灯组亮度。
	 */
	public int Cb_LightUnitOn(int lightUnit, int lightLevel) {
		int retval = -1;
		try {
			int i = 0;
			Cb_LightUnitOnJnative.setParameter(i++, lightUnit);
			Cb_LightUnitOnJnative.setParameter(i++, lightLevel);
			Cb_LightUnitOnJnative.setRetVal(Type.INT);
			Cb_LightUnitOnJnative.invoke();
			retval = Cb_LightUnitOnJnative.getRetValAsInt();
			log.debug("Cb_LightUnitOnJnative : retval==" + retval);// 获取返回值
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retval;
	}

	/**
	 * 函数：int __stdcall Cb_LightUnitOff(int unit); 参数：int unit, [in]灯组号，有效值为0~1
	 * 返回：成功为0，其它为错误值见 错误码 说明：关闭灯组
	 */
	public int Cb_LightUnitOff(int lightUnit, int lightLevel) {
		int retval = -1;
		try {
			int i = 0;
			Cb_LightUnitOffJnative.setParameter(i++, lightUnit);
			Cb_LightUnitOffJnative.setRetVal(Type.INT);
			Cb_LightUnitOffJnative.invoke();
			retval = Cb_LightUnitOffJnative.getRetValAsInt();
			log.debug("Cb_LightUnitOffJnative : retval==" + retval);// 获取返回值
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retval;
	}

	/**
	 * 函数：int __stdcall Cb_EnableShortPress(); 参数：无 返回：成功为0，其它为错误值见 错误码
	 * 说明：打开控制板的短按控制开关。
	 * 
	 */
	public int Cb_EnableShortPress() {
		int retval = -1;
		try {
			Cb_EnableShortPressJnative.setRetVal(Type.INT);
			Cb_EnableShortPressJnative.invoke();
			retval = Cb_EnableShortPressJnative.getRetValAsInt();
			log.debug("Cb_EnableShortPressJnative : retval==" + retval);// 获取返回值
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retval;
	}

	public static void main(String[] args) {
		LightControlBoard cb = new LightControlBoard();
		if (cb.Cb_InitSDK() == 0) {
			if (cb.Cb_OpenCom(DeviceConfig.getInstance().getCameraLEDPort()) == 0) {
				// cb.Cb_LightUnitOn(0, 30);
				cb.Cb_LightUnitOff(0, 30);
			}
		}

	}

}
