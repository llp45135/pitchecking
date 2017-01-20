package com.rxtec.pitchecking.device;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xvolks.jnative.JNative;
import org.xvolks.jnative.Type;
import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.pointers.Pointer;
import org.xvolks.jnative.pointers.memory.MemoryBlockFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.IDCard;

import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.CalUtils;

public class TKIDCDevice {
	private Logger log = LoggerFactory.getLogger("IDCardDevice");
	// private String dllName = "IDC_EWTa.dll";
	private String dllName = "ICC_EWTa.dll";
	JNative jnativeIDC_Init = null;
	JNative jnativeIDC_FetchCard = null;
	JNative jnativeIDC_StopFetchCard = null;
	JNative jnativeIDC_GetSerial = null;
	JNative jnativeIDC_ReadIDCardInfo = null;
	JNative jnativeBmp = null;
	static TKIDCDevice _instance = new TKIDCDevice();

	public static TKIDCDevice getInstance() {
		return _instance;
	}

	private TKIDCDevice() {
		JNative.setLoggingEnabled(false);
		this.initJnative();
		this.IDC_Init();
	}

	/**
	 * 初始化jnative
	 */
	private void initJnative() {

		try {
			jnativeIDC_Init = new JNative(dllName, "IDC_Init");
			jnativeIDC_FetchCard = new JNative(dllName, "IDC_FetchCard");
			jnativeIDC_StopFetchCard = new JNative(dllName, "IDC_StopFetchCard");
			jnativeIDC_GetSerial = new JNative(dllName, "IDC_GetSerial");
			jnativeIDC_ReadIDCardInfo = new JNative(dllName, "IDC_ReadIDCardInfo");
			jnativeBmp = new JNative("WltRS.dll", "GetBmp");

		} catch (NativeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 对二代居民身份证识读单元进行初始化设置
	 */
	public void IDC_Init() {
		String retval = "";
		Pointer pointerOut = null;

		try {
			pointerOut = new Pointer(MemoryBlockFactory.createMemoryBlock(4 * 4 + 128 + 128));
			int i = 0;

			jnativeIDC_Init.setRetVal(Type.INT);
			jnativeIDC_Init.setParameter(i++, pointerOut);
			jnativeIDC_Init.invoke();

			retval = jnativeIDC_Init.getRetVal();
			log.debug("IDC_Init retval==" + retval);
			if (retval.equals("0")) {
				byte[] iLogicCode = new byte[4];
				for (int k = 0; k < 4; k++) {
					iLogicCode[k] = pointerOut.getAsByte(k);
				}
				log.debug("iLogicCode==" + CommUtil.bytesToInt(iLogicCode, 0));

				byte[] iPhyCode = new byte[4];
				for (int k = 0; k < 4; k++) {
					iPhyCode[k] = pointerOut.getAsByte(k + 4);
				}
				log.debug("iPhyCode==" + CommUtil.bytesToInt(iPhyCode, 0));

				byte[] iHandle = new byte[4];
				for (int k = 0; k < 4; k++) {
					iHandle[k] = pointerOut.getAsByte(k + 4 + 4);
				}
				log.debug("iHandle==" + CommUtil.bytesToInt(iHandle, 0));

				byte[] iType = new byte[4];
				for (int k = 0; k < 4; k++) {
					iType[k] = pointerOut.getAsByte(k + 4 + 4);
				}
				log.debug("iType==" + CommUtil.bytesToInt(iType, 0));

				byte[] acDevReturn = new byte[128];
				for (int k = 0; k < 128; k++) {
					acDevReturn[k] = pointerOut.getAsByte(k + 4 + 4 + 4);
				}
				log.debug("acDevReturn==" + new String(acDevReturn));

				byte[] acReserve = new byte[128];
				for (int k = 0; k < 128; k++) {
					acReserve[k] = pointerOut.getAsByte(k + 4 + 4 + 4 + 128);
				}
				log.debug("acReserve==" + new String(acReserve));
			}
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("TKIDCDevice IDC_Init:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("TKIDCDevice IDC_Init:", e);
		} catch (Exception e) {
			log.error("TKIDCDevice IDC_Init:", e);
		}
	}

	/**
	 * 读取二代居民身份证识读单元序列号
	 */
	public void IDC_GetSerial() {
		String retval = "";
		Pointer pointerOut = null;

		try {
			pointerOut = new Pointer(MemoryBlockFactory.createMemoryBlock(32));
			int i = 0;

			jnativeIDC_GetSerial.setRetVal(Type.INT);
			jnativeIDC_GetSerial.setParameter(i++, pointerOut);
			jnativeIDC_GetSerial.invoke();

			retval = jnativeIDC_GetSerial.getRetVal();
			log.debug("IDC_GetSerial retval==" + retval);
			if (retval.equals("0")) {
				byte[] cSerialNo = new byte[32];
				for (int k = 0; k < 32; k++) {
					cSerialNo[k] = pointerOut.getAsByte(k);
				}
				log.debug("cSerialNo==" + new String(cSerialNo));
			}
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("TKIDCDevice IDC_GetSerial:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("TKIDCDevice IDC_GetSerial:", e);
		} catch (Exception e) {
			log.error("TKIDCDevice IDC_GetSerial:", e);
		}
	}

	/**
	 * 在设定时间内寻找是否有二代居民身份证进入部件读取有效范围
	 * 
	 * @param iTimeOut
	 * @return
	 */
	public String IDC_FetchCard(int iTimeOut) {
		String retval = "-1";
		Pointer pointerCardType = null;
		Pointer pointerOut = null;

		try {
			pointerCardType = new Pointer(MemoryBlockFactory.createMemoryBlock(4));
			pointerOut = new Pointer(MemoryBlockFactory.createMemoryBlock(4 * 4 + 128 + 128));
			int i = 0;

			jnativeIDC_FetchCard.setRetVal(Type.INT);
			jnativeIDC_FetchCard.setParameter(i++, iTimeOut);
			jnativeIDC_FetchCard.setParameter(i++, pointerCardType);
			jnativeIDC_FetchCard.setParameter(i++, pointerOut);
			jnativeIDC_FetchCard.invoke();

			retval = jnativeIDC_FetchCard.getRetVal();
//			log.debug("IDC_FetchCard retval==" + retval);
			if (retval.equals("0")) {
				log.debug("已经寻到二代证");
//				byte[] iCardType = new byte[4];
//				for (int k = 0; k < 4; k++) {
//					iCardType[k] = pointerCardType.getAsByte(k);
//				}
//				log.debug("iCardType==" + CommUtil.bytesToInt(iCardType, 0));
//
//				byte[] iLogicCode = new byte[4];
//				for (int k = 0; k < 4; k++) {
//					iLogicCode[k] = pointerOut.getAsByte(k);
//				}
//				log.debug("iLogicCode==" + CommUtil.bytesToInt(iLogicCode, 0));
//
//				byte[] iPhyCode = new byte[4];
//				for (int k = 0; k < 4; k++) {
//					iPhyCode[k] = pointerOut.getAsByte(k + 4);
//				}
//				log.debug("iPhyCode==" + CommUtil.bytesToInt(iPhyCode, 0));
//
//				byte[] iHandle = new byte[4];
//				for (int k = 0; k < 4; k++) {
//					iHandle[k] = pointerOut.getAsByte(k + 4 + 4);
//				}
//				log.debug("iHandle==" + CommUtil.bytesToInt(iHandle, 0));
//
//				byte[] iType = new byte[4];
//				for (int k = 0; k < 4; k++) {
//					iType[k] = pointerOut.getAsByte(k + 4 + 4);
//				}
//				log.debug("iType==" + CommUtil.bytesToInt(iType, 0));
//
//				byte[] acDevReturn = new byte[128];
//				for (int k = 0; k < 128; k++) {
//					acDevReturn[k] = pointerOut.getAsByte(k + 4 + 4 + 4);
//				}
//				log.debug("acDevReturn==" + new String(acDevReturn));
//
//				byte[] acReserve = new byte[128];
//				for (int k = 0; k < 128; k++) {
//					acReserve[k] = pointerOut.getAsByte(k + 4 + 4 + 4 + 128);
//				}
//				log.debug("acReserve==" + new String(acReserve));
			}
			pointerCardType.dispose();
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("TKIDCDevice IDC_FetchCard:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("TKIDCDevice IDC_FetchCard:", e);
		} catch (Exception e) {
			log.error("TKIDCDevice IDC_FetchCard:", e);
		}
		return retval;
	}

	/**
	 * 停止二代居民身份证识读单元寻找是否有二代居民身份证进入部件读取有效范围
	 */
	public void IDC_StopFetchCard() {
		String retval = "";
		Pointer pointerOut = null;

		try {
			pointerOut = new Pointer(MemoryBlockFactory.createMemoryBlock(4 * 4 + 128 + 128));
			int i = 0;

			jnativeIDC_StopFetchCard.setRetVal(Type.INT);
			jnativeIDC_StopFetchCard.setParameter(i++, pointerOut);
			jnativeIDC_StopFetchCard.invoke();

			retval = jnativeIDC_StopFetchCard.getRetVal();
			log.debug("jnativeIDC_StopFetchCard retval==" + retval);
			if (retval.equals("0")) {
				byte[] iLogicCode = new byte[4];
				for (int k = 0; k < 4; k++) {
					iLogicCode[k] = pointerOut.getAsByte(k);
				}
				log.debug("iLogicCode==" + CommUtil.bytesToInt(iLogicCode, 0));

				byte[] iPhyCode = new byte[4];
				for (int k = 0; k < 4; k++) {
					iPhyCode[k] = pointerOut.getAsByte(k + 4);
				}
				log.debug("iPhyCode==" + CommUtil.bytesToInt(iPhyCode, 0));

				byte[] iHandle = new byte[4];
				for (int k = 0; k < 4; k++) {
					iHandle[k] = pointerOut.getAsByte(k + 4 + 4);
				}
				log.debug("iHandle==" + CommUtil.bytesToInt(iHandle, 0));

				byte[] iType = new byte[4];
				for (int k = 0; k < 4; k++) {
					iType[k] = pointerOut.getAsByte(k + 4 + 4);
				}
				log.debug("iType==" + CommUtil.bytesToInt(iType, 0));

				byte[] acDevReturn = new byte[128];
				for (int k = 0; k < 128; k++) {
					acDevReturn[k] = pointerOut.getAsByte(k + 4 + 4 + 4);
				}
				log.debug("acDevReturn==" + new String(acDevReturn));

				byte[] acReserve = new byte[128];
				for (int k = 0; k < 128; k++) {
					acReserve[k] = pointerOut.getAsByte(k + 4 + 4 + 4 + 128);
				}
				log.debug("acReserve==" + new String(acReserve));
			}
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("TKIDCDevice IDC_StopFetchCard:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("TKIDCDevice IDC_StopFetchCard:", e);
		} catch (Exception e) {
			log.error("TKIDCDevice IDC_StopFetchCard:", e);
		}
	}

	/**
	 * 根据指定的类型读取二代居民身份证信息。 输入参数： iType 读取二代居民身份证信息类型。
	 * 1：读取除照片外的文本信息，包括二代证卡号、姓名、性别、名族、出生日期、有效期限、住址 2: 读取二代居民身份证所有信息 输出参数：
	 * p_psCardInfo 保存二代居民身份证信息。 p_psStatus 保存状态信息。 返回值： int 0：成功 1：失败
	 * 
	 * @param iType
	 * @return
	 */
	public IDCard IDC_ReadIDCardInfo(int iType) {
		String retval = "-1";
		Pointer pointerCardInfo = null;
		Pointer pointerOut = null;
		IDCard synIDCard = null;
		try {
			pointerCardInfo = new Pointer(
					MemoryBlockFactory.createMemoryBlock(30 + 2 + 4 + 16 + 70 + 36 + 30 + 16 + 16 + 70 + 1024));
			pointerOut = new Pointer(MemoryBlockFactory.createMemoryBlock(4 * 4 + 128 + 128));
			int i = 0;

			jnativeIDC_ReadIDCardInfo.setRetVal(Type.INT);
			jnativeIDC_ReadIDCardInfo.setParameter(i++, iType);
			jnativeIDC_ReadIDCardInfo.setParameter(i++, pointerCardInfo);
			jnativeIDC_ReadIDCardInfo.setParameter(i++, pointerOut);
			jnativeIDC_ReadIDCardInfo.invoke();

			retval = jnativeIDC_ReadIDCardInfo.getRetVal();
			log.debug("IDC_ReadIDCardInfo retval==" + retval);
			if (retval.equals("0")) {
				synIDCard = new IDCard();

				byte[] IDName = new byte[30];
				for (int k = 0; k < 30; k++) {
					IDName[k] = pointerCardInfo.getAsByte(k);
				}
				String personName = new String(IDName, "GBK");
				log.debug("IDName==" + personName);
				synIDCard.setPersonName(personName); // set personName

				byte[] IDSex = new byte[2];
				for (int k = 0; k < 2; k++) {
					IDSex[k] = pointerCardInfo.getAsByte(k + 30);
				}
				String gender = new String(IDSex, "GBK");
//				log.debug("IDSex==" + gender);
				if (gender.equals("1")) {
					synIDCard.setGender(1);
					synIDCard.setGenderCH("男");// set gender
				} else if (gender.equals("2")) {
					synIDCard.setGender(2);
					synIDCard.setGenderCH("女");// set gender
				}

				byte[] IDNation = new byte[4];
				for (int k = 0; k < 4; k++) {
					IDNation[k] = pointerCardInfo.getAsByte(k + 30 + 2);
				}
//				log.debug("IDNation==" + new String(IDNation, "GBK"));

				byte[] IDBirth = new byte[16];
				for (int k = 0; k < IDBirth.length; k++) {
					IDBirth[k] = pointerCardInfo.getAsByte(k + 30 + 2 + 4);
				}
				String birthstr = new String(IDBirth, "GBK");
//				log.debug("IDBirth==" + birthstr);
				String birthday = birthstr.substring(0, 4) + "-" + birthstr.substring(4, 6) + "-"
						+ birthstr.substring(6, 8);
				String today = CalUtils.getStringDateShort();
				// log.debug("birthday==" + birthday);
				int personAge = CalUtils.getAge(birthday, today);
				synIDCard.setAge(personAge); // set age
//				log.debug("出生年月：" + birthstr.substring(0, 4) + "年" + birthstr.substring(4, 6) + "月"
//						+ birthstr.substring(6, 8) + "日" + ",personAge==" + personAge);

				byte[] IDDwelling = new byte[70];
				for (int k = 0; k < IDDwelling.length; k++) {
					IDDwelling[k] = pointerCardInfo.getAsByte(k + 30 + 2 + 4 + 16);
				}
//				log.debug("IDDwelling==" + new String(IDDwelling, "GBK"));

				byte[] IDCode = new byte[36];
				for (int k = 0; k < IDCode.length; k++) {
					IDCode[k] = pointerCardInfo.getAsByte(k + 30 + 2 + 4 + 16 + 70);
				}
				String IDCardNoStr = new String(IDCode, "GBK");
//				log.debug("IDCode==" + IDCardNoStr);
				synIDCard.setIdNo(IDCardNoStr.trim());

				byte[] IDIssue = new byte[30];
				for (int k = 0; k < IDIssue.length; k++) {
					IDIssue[k] = pointerCardInfo.getAsByte(k + 30 + 2 + 4 + 16 + 70 + 36);
				}
//				log.debug("IDIssue==" + new String(IDIssue, "GBK"));

				byte[] IDEfficb = new byte[16];
				for (int k = 0; k < IDEfficb.length; k++) {
					IDEfficb[k] = pointerCardInfo.getAsByte(k + 30 + 2 + 4 + 16 + 70 + 36 + 30);
				}
//				log.debug("IDEfficb==" + new String(IDEfficb, "GBK"));

				byte[] IDEffice = new byte[16];
				for (int k = 0; k < IDEfficb.length; k++) {
					IDEffice[k] = pointerCardInfo.getAsByte(k + 30 + 2 + 4 + 16 + 70 + 36 + 30 + 16);
				}
//				log.debug("IDEffice==" + new String(IDEffice, "GBK"));

				byte[] IDNewAddr = new byte[70];
				for (int k = 0; k < IDNewAddr.length; k++) {
					IDNewAddr[k] = pointerCardInfo.getAsByte(k + 30 + 2 + 4 + 16 + 70 + 36 + 30 + 16 + 16);
				}
//				log.debug("IDNewAddr==" + new String(IDNewAddr, "GBK"));

				byte[] IDPhoto = new byte[1024];
				for (int k = 0; k < IDPhoto.length; k++) {
					IDPhoto[k] = pointerCardInfo.getAsByte(k + 30 + 2 + 4 + 16 + 70 + 36 + 30 + 16 + 16 + 70);
				}
				try {
					File myFile = new File("zp.wlt");
					FileOutputStream out = new FileOutputStream(myFile);
					out.write(IDPhoto, 0, 1024 - 1);
				} catch (IOException t) {
					t.printStackTrace();
				}

				/**
				 * Syn_GetBmp本函数用于将wlt文件解码成bmp文件 参数2：阅读设备通讯接口类型（1—RS-232C，2—USB）
				 */
				int j = 0;
				String bmpretval = "";

				jnativeBmp.setRetVal(org.xvolks.jnative.Type.INT);
				jnativeBmp.setParameter(j++, "zp.wlt");
				jnativeBmp.setParameter(j++, 1);
				jnativeBmp.invoke();
				bmpretval = jnativeBmp.getRetVal();
				log.debug("GetBmp: bmpretval==" + bmpretval);// 获取返回值

				CommUtil.bmpTojpg("zp.bmp", "zp.jpg");

				String photoFile = "zp.jpg";
				log.debug("photoFile==" + photoFile);
				File idcardFile = new File(photoFile);
				
				BufferedImage idCardImage = null;
				idCardImage = ImageIO.read(idcardFile);
				if (idCardImage != null) {
					log.debug("完整读取二代证照片成功！");
					synIDCard.setCardImage(idCardImage); // set cardImage
					byte[] idCardImageBytes = null;
					idCardImageBytes = CommUtil.getImageBytesFromImageBuffer(idCardImage);
					if (idCardImageBytes != null)
						synIDCard.setCardImageBytes(idCardImageBytes);
				}
				// boolean delFlag = idcardFile.delete();
				//

				byte[] iLogicCode = new byte[4];
				for (int k = 0; k < 4; k++) {
					iLogicCode[k] = pointerOut.getAsByte(k);
				}
				log.debug("iLogicCode==" + CommUtil.bytesToInt(iLogicCode, 0));

				byte[] iPhyCode = new byte[4];
				for (int k = 0; k < 4; k++) {
					iPhyCode[k] = pointerOut.getAsByte(k + 4);
				}
				log.debug("iPhyCode==" + CommUtil.bytesToInt(iPhyCode, 0));

				byte[] iHandle = new byte[4];
				for (int k = 0; k < 4; k++) {
					iHandle[k] = pointerOut.getAsByte(k + 4 + 4);
				}
				log.debug("iHandle==" + CommUtil.bytesToInt(iHandle, 0));

				byte[] errType = new byte[4];
				for (int k = 0; k < 4; k++) {
					errType[k] = pointerOut.getAsByte(k + 4 + 4);
				}
				log.debug("iType==" + CommUtil.bytesToInt(errType, 0));

				byte[] acDevReturn = new byte[128];
				for (int k = 0; k < 128; k++) {
					acDevReturn[k] = pointerOut.getAsByte(k + 4 + 4 + 4);
				}
				log.debug("acDevReturn==" + new String(acDevReturn));

				byte[] acReserve = new byte[128];
				for (int k = 0; k < 128; k++) {
					acReserve[k] = pointerOut.getAsByte(k + 4 + 4 + 4 + 128);
				}
				log.debug("acReserve==" + new String(acReserve));
			}
			pointerCardInfo.dispose();
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("TKIDCDevice IDC_ReadIDCardInfo:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated IDC_ReadIDCardInfo block
			log.error("TKIDCDevice IDC_ReadIDCardInfo:", e);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			log.error("TKIDCDevice IDC_ReadIDCardInfo:", e);
		} catch (Exception e) {
			log.error("TKIDCDevice IDC_ReadIDCardInfo:", e);
		}
		return synIDCard;
	}

	public static void main(String[] args) {

		TKIDCDevice idc = TKIDCDevice.getInstance();
		// idc.IDC_Init();
		// idc.IDC_GetSerial();
		while (true) {
			CommUtil.sleep(200);
			idc.IDC_FetchCard(3000);
			idc.IDC_ReadIDCardInfo(2);
		}
	}
}
