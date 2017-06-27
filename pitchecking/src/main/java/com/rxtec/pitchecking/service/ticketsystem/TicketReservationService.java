package com.rxtec.pitchecking.service.ticketsystem;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xvolks.jnative.JNative;
import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.pointers.Pointer;
import org.xvolks.jnative.pointers.memory.MemoryBlockFactory;

import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.NumberUtil;

import org.xvolks.jnative.Type;

/**
 * 人脸识别接口：service interface
 * 描述核验闸机客票子程序与主控程序之间的接口，包括规定的动态库文件名和一套规定的动态库接口，接口文件以动态库的方式实现
 * 
 * @author ZhaoLin
 *
 */
public class TicketReservationService {
	private Logger log = LoggerFactory.getLogger("TicketReservationService");
	private static TicketReservationService _instance = new TicketReservationService();
	private String dllName = "C:\\afcdriver\\RSIVSRV.dll";
	// private String dllName = "RSIVSRV.dll";

	private JNative jnativeInit = null;
	private JNative jnativeSendDeviceInfo = null;
	private JNative jnativeTicketVerify = null;
	private JNative jnativeReceiptStatus = null;
	private JNative jnativeUnInit = null;

	private String initOutStr = null;
	private String sendDeviceInfoOutStr = null;
	private String ticketVerifyOutStr = null;
	private String receiptStatusOutStr = null;
	private String unInitOutStr = null;

	public String getInitOutStr() {
		return initOutStr;
	}

	public void setInitOutStr(String initOutStr) {
		this.initOutStr = initOutStr;
	}

	public String getSendDeviceInfoOutStr() {
		return sendDeviceInfoOutStr;
	}

	public void setSendDeviceInfoOutStr(String sendDeviceInfoOutStr) {
		this.sendDeviceInfoOutStr = sendDeviceInfoOutStr;
	}

	public String getTicketVerifyOutStr() {
		return ticketVerifyOutStr;
	}

	public void setTicketVerifyOutStr(String ticketVerifyOutStr) {
		this.ticketVerifyOutStr = ticketVerifyOutStr;
	}

	public String getReceiptStatusOutStr() {
		return receiptStatusOutStr;
	}

	public void setReceiptStatusOutStr(String receiptStatusOutStr) {
		this.receiptStatusOutStr = receiptStatusOutStr;
	}

	public String getUnInitOutStr() {
		return unInitOutStr;
	}

	public void setUnInitOutStr(String unInitOutStr) {
		this.unInitOutStr = unInitOutStr;
	}

	public static TicketReservationService getInstance() {
		return _instance;
	}

	private TicketReservationService() {
		// TODO Auto-generated constructor stub
		JNative.setLoggingEnabled(true);
		this.initJNative();
	}

	/**
	 * 
	 */
	private void initJNative() {
		// System.getProperty("java.library.path");
		// System.load(dllName);
		try {

			jnativeInit = new JNative(dllName, "Init");
			jnativeSendDeviceInfo = new JNative(dllName, "SendDeviceInfo");
			jnativeTicketVerify = new JNative(dllName, "TicketVerify");
			jnativeReceiptStatus = new JNative(dllName, "ReceiptStatus");
			jnativeUnInit = new JNative(dllName, "UnInit");
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("TicketReservationService initJNative:", e);
		} catch (Exception ex) {
			log.error("TicketReservationService initJNative:", ex);
		}
	}

	/**
	 * 函数参数: pInput:
	 * 设备序列号（闸机厂家名称简称（4位）设备型号（8位）设备唯一序列号（32位）主控程序版本号（8位））。注：每项位数不足左补0。 pOut:
	 * 错误信息（见表一）。 函数功能： 对闸机设备初始化。 返回值： 0：成功 1：失败
	 * 
	 * @param factory
	 * @param deviceType
	 * @param deviceSerial
	 * @param softVersion
	 * @param pOutStr
	 * @return
	 */
	public int Init(String factory, String deviceType, String deviceSerial, String softVersion) {
		int retval = -1;
		Pointer pointerIn = null;
		Pointer pointerOut = null;
		this.initOutStr = null;
		try {
			pointerIn = new Pointer(MemoryBlockFactory.createMemoryBlock(52));

			byte[] initArray = new byte[52];
			byte[] factoryArray = factory.getBytes();
			for (int i = 0; i < factoryArray.length; i++) {
				initArray[i] = factoryArray[i];
			}

			byte[] deviceTypeArray = deviceType.getBytes();
			for (int i = 0; i < deviceTypeArray.length; i++) {
				initArray[i + 4] = deviceTypeArray[i];
			}

			byte[] deviceSerialArray = deviceSerial.getBytes();
			for (int i = 0; i < deviceSerialArray.length; i++) {
				initArray[i + 12] = deviceSerialArray[i];
			}

			byte[] softVersionArray = softVersion.getBytes();
			for (int i = 0; i < softVersionArray.length; i++) {
				initArray[i + 44] = softVersionArray[i];
			}

			pointerIn.setMemory(initArray);

			pointerOut = new Pointer(MemoryBlockFactory.createMemoryBlock(60));
			pointerOut.zeroMemory();

			int i = 0;
			jnativeInit.setRetVal(Type.INT);
			jnativeInit.setParameter(i++, pointerIn);
			jnativeInit.setParameter(i++, pointerOut);
			jnativeInit.invoke();

			retval = jnativeInit.getRetValAsInt();
			log.info("Init retval==" + retval);
			initOutStr = new String(pointerOut.getMemory(), "GBK");
			log.info("Init pOutStr = " + initOutStr);

			pointerIn.dispose();
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("TicketReservationService Init:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("TicketReservationService Init:", e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("TicketReservationService Init:", e);
		}
		return retval;
	}

	/**
	 * 函数功能： 定时上送设备状态，建议间隔1小时，状态发生变化实时上送。 函数参数：
	 * pInput:设备主控状态（0正常1异常）&通道模式（0正常1常开模式）&读卡器状态（0正常1异常）&门状态（0正常1异常）&摄像头（0正常1异常
	 * ）&二维码扫描器（0正常1异常） pOut: 错误信息（见表一）。 返回值： 0：成功 1：失败
	 * 
	 * @param pInput
	 * @param pOutStr
	 * @return
	 */
	public int SendDeviceInfo(String pInput) {
		int retval = -1;
		Pointer pointerOut = null;
		this.sendDeviceInfoOutStr = null;
		try {

			pointerOut = new Pointer(MemoryBlockFactory.createMemoryBlock(60));
			pointerOut.zeroMemory();

			int i = 0;
			jnativeSendDeviceInfo.setRetVal(Type.INT);
			jnativeSendDeviceInfo.setParameter(i++, pInput);
			jnativeSendDeviceInfo.setParameter(i++, pointerOut);
			jnativeSendDeviceInfo.invoke();

			retval = jnativeSendDeviceInfo.getRetValAsInt();
			log.info("SendDeviceInfo retval==" + retval);
			sendDeviceInfoOutStr = new String(pointerOut.getMemory(), "GBK");
			log.info("SendDeviceInfo pOutStr = " + sendDeviceInfoOutStr);

			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("TicketReservationService SendDeviceInfo:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("TicketReservationService SendDeviceInfo:", e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("TicketReservationService SendDeviceInfo:", e);
		}
		return retval;
	}

	/**
	 * 函数功能： 车票二维码信息或电子客票信息与身份证信息进行校验。 参数： pTicketInfo:票面信息, pInfo: 证件信息(全信息)
	 * pType: 核验类型(0: 票身份证/1: 电子票/3:票台胞证/4:票护照) pOut: 返回信息（见表一）。 返回值： 0：成功 1：失败
	 * 
	 * @param pTicketInfo
	 * @param tidCardInfo
	 * @param pType
	 * @param pOutStr
	 * @return
	 */
	public int TicketVerify(String pTicketInfo, TIDCardInfo tidCardInfo, String pType) {
		int retval = -1;
		Pointer pointerTicketInfo = null;
		Pointer pointerIdCard = null;
		Pointer pointerType = null;
		Pointer pointerOut = null;
		this.ticketVerifyOutStr = null;
		try {
			log.info("pTicketInfo = " + pTicketInfo + ",pTicketInfo.length = " + pTicketInfo.getBytes().length);
			pointerTicketInfo = new Pointer(MemoryBlockFactory.createMemoryBlock(pTicketInfo.length()));
			pointerTicketInfo.setMemory(pTicketInfo.getBytes());

			/**
			 * 
			 */
			pointerIdCard = new Pointer(MemoryBlockFactory.createMemoryBlock(30 + 2 + 4 + 16 + 70 + 36 + 30 + 16 + 16 + 70 + 1024));
			byte[] idcardArray = new byte[30 + 2 + 4 + 16 + 70 + 36 + 30 + 16 + 16 + 70 + 1024];

			for (int i = 0; i < 30; i++) {
				idcardArray[i] = tidCardInfo.getIDName()[i];
			}
			// log.info("IDName = " + new String(tidCardInfo.getIDName()) +
			// "#");
			for (int i = 0; i < 2; i++) {
				idcardArray[i + 30] = tidCardInfo.getIDSex()[i];
			}
			// log.info("IDSex = " + new String(tidCardInfo.getIDSex()) + "#");
			for (int i = 0; i < 4; i++) {
				idcardArray[i + 30 + 2] = tidCardInfo.getIDNation()[i];
			}
			// log.info("IDNation = " + new String(tidCardInfo.getIDNation()) +
			// "#");
			for (int i = 0; i < 16; i++) {
				idcardArray[i + 30 + 2 + 4] = tidCardInfo.getIDBirth()[i];
			}
			// log.info("IDBirth = " + new String(tidCardInfo.getIDBirth()) +
			// "#");
			for (int i = 0; i < 70; i++) {
				idcardArray[i + 30 + 2 + 4 + 16] = tidCardInfo.getIDDwelling()[i];
			}
			// log.info("IDDwelling = " + new
			// String(tidCardInfo.getIDDwelling()) + "#");
			for (int i = 0; i < 36; i++) {
				idcardArray[i + 30 + 2 + 4 + 16 + 70] = tidCardInfo.getIDCode()[i];
			}
			// log.info("IDCode = " + new String(tidCardInfo.getIDCode()) +
			// "#");
			for (int i = 0; i < 30; i++) {
				idcardArray[i + 30 + 2 + 4 + 16 + 70 + 36] = tidCardInfo.getIDIssue()[i];
			}
			// log.info("IDCode = " + new String(tidCardInfo.getIDCode()) +
			// "#");
			for (int i = 0; i < 16; i++) {
				idcardArray[i + 30 + 2 + 4 + 16 + 70 + 36 + 30] = tidCardInfo.getIDEfficb()[i];
			}
			// log.info("IDEfficb = " + new String(tidCardInfo.getIDEfficb()) +
			// "#");
			for (int i = 0; i < 16; i++) {
				idcardArray[i + 30 + 2 + 4 + 16 + 70 + 36 + 30 + 16] = tidCardInfo.getIDEffice()[i];
			}
			// log.info("IDEffice = " + new String(tidCardInfo.getIDEffice()) +
			// "#");
			for (int i = 0; i < 70; i++) {
				idcardArray[i + 30 + 2 + 4 + 16 + 70 + 36 + 30 + 16 + 16] = tidCardInfo.getIDNewAddr()[i];
			}
			// log.info("IDNewAddr = " + new String(tidCardInfo.getIDNewAddr())
			// + "#");
			for (int i = 0; i < 1024; i++) {
				idcardArray[i + 30 + 2 + 4 + 16 + 70 + 36 + 30 + 16 + 16 + 70] = tidCardInfo.getIDPhoto()[i];
			}

			String idName = new String(tidCardInfo.getIDName());
			String idSex = new String(tidCardInfo.getIDSex());
			String idNation = new String(tidCardInfo.getIDNation());
			String idBirth = new String(tidCardInfo.getIDBirth());
			String idDwelling = new String(tidCardInfo.getIDDwelling());
			String idCode = new String(tidCardInfo.getIDCode());
			String idIssue = new String(tidCardInfo.getIDIssue());
			String idEfficb = new String(tidCardInfo.getIDEfficb());
			String idEffice = new String(tidCardInfo.getIDEffice());
			String idNewAddr = new String(tidCardInfo.getIDNewAddr());
			String idPhoto = new String(tidCardInfo.getIDPhoto());
			log.info("入参==" + new String(pTicketInfo.getBytes()) + "," + new String(pType.getBytes()) + "," + idName + idSex + idNation + idBirth + idDwelling + idCode
					+ idIssue + idEfficb + idEffice + idNewAddr);

			pointerIdCard.zeroMemory();
			pointerIdCard.setMemory(idcardArray);

			//
			pointerType = new Pointer(MemoryBlockFactory.createMemoryBlock(1));
			pointerType.setMemory(pType.getBytes());

			//
			pointerOut = new Pointer(MemoryBlockFactory.createMemoryBlock(60));
			pointerOut.zeroMemory();

			int i = 0;
			jnativeTicketVerify.setRetVal(Type.INT);
			jnativeTicketVerify.setParameter(i++, pointerTicketInfo);
			jnativeTicketVerify.setParameter(i++, pointerIdCard);
			jnativeTicketVerify.setParameter(i++, pointerType);
			jnativeTicketVerify.setParameter(i++, pointerOut);
			jnativeTicketVerify.invoke();

			retval = jnativeTicketVerify.getRetValAsInt();
			log.info("TicketVerify retval==" + retval);
			ticketVerifyOutStr = new String(pointerOut.getMemory(), "GBK");
			log.info("TicketVerify pOutStr = " + ticketVerifyOutStr);
			pointerTicketInfo.dispose();
			pointerIdCard.dispose();
			pointerType.dispose();
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("TicketReservationService TicketVerify:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("TicketReservationService TicketVerify:", e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("TicketReservationService TicketVerify:", e);
		}
		return retval;
	}

	/**
	 * 函数功能： 票证人核验后，返回开门状态。 入参： pStatus: 状态码（4位）+证件号 状态码： 0000：票证人核验通过，开门成功
	 * 0001：票证人核验通过，开门失败 0002：票证不通过(人脸通过，不开门) 0003：人脸不通过(票证通过, 不开门)
	 * 0004：票证不通过，人脸不通过(不开门) 9999：其他，不开门
	 * 
	 * pPhoto: 摄像头抓拍的多张图像，顺序排列（根据长度取图像），第一张是头像照片，照片格式为JPG。 iPhotoLen:
	 * 摄像头抓怕每张图像的长度，长度为0代表没有图像，最多10张。 出参： pOut: 返回信息（见表一）。 返回值： 0：成功 1：失败
	 * 
	 * @param pStatus
	 * @param pPhoto
	 * @param iPhotoLen
	 * @param pOutStr
	 * @return
	 */
	public int ReceiptStatus(String pStatus, byte[] pPhoto, int[] iPhotoLen) {
		int retval = -1;
		Pointer pointerStatus = null;
		Pointer pointerPhoto = null;
		Pointer pointerPhotoLen = null;
		Pointer pointerOut = null;
		this.receiptStatusOutStr = null;
		try {
			log.info("pStatus = " + pStatus);
			pointerStatus = new Pointer(MemoryBlockFactory.createMemoryBlock(22));
			pointerStatus.setMemory(pStatus.getBytes());

			int totalLen = 0; // 照片总长度
			int photoCount = iPhotoLen.length; // 照片张数
			log.info("照片张数 = " + photoCount);
			for (int i = 0; i < photoCount; i++) {
				log.info("第" + (i + 1) + "张照片长度 = " + iPhotoLen[i]);
				totalLen = totalLen + iPhotoLen[i];
			}
			log.info("照片总长度 = " + totalLen);

			pointerPhoto = new Pointer(MemoryBlockFactory.createMemoryBlock(totalLen));
			pointerPhoto.setMemory(pPhoto);

			pointerPhotoLen = new Pointer(MemoryBlockFactory.createMemoryBlock(4 * photoCount + 4 * (10 - photoCount)));
			byte[] photoLenArray = new byte[4 * photoCount + 4 * (10 - photoCount)];
			for (int i = 0; i < photoCount; i++) {
				byte[] tt = CommUtil.intToBytes(iPhotoLen[i]);
				for (int k = 0; k < 4; k++) {
					photoLenArray[i * 4 + k] = tt[k];
				}
			}

			for (int i = photoCount; i < 10; i++) {
				byte[] tt = CommUtil.intToBytes(0);
				for (int k = 0; k < 4; k++) {
					photoLenArray[i * 4 + k] = tt[k];
				}
			}
			pointerPhotoLen.setMemory(photoLenArray);

			pointerOut = new Pointer(MemoryBlockFactory.createMemoryBlock(60));
			pointerOut.zeroMemory();

			int i = 0;
			jnativeReceiptStatus.setRetVal(Type.INT);
			jnativeReceiptStatus.setParameter(i++, pointerStatus);
			jnativeReceiptStatus.setParameter(i++, pointerPhoto);
			jnativeReceiptStatus.setParameter(i++, pointerPhotoLen);
			jnativeReceiptStatus.setParameter(i++, pointerOut);
			jnativeReceiptStatus.invoke();

			retval = jnativeReceiptStatus.getRetValAsInt();
			log.info("ReceiptStatus retval==" + retval);

			receiptStatusOutStr = new String(pointerOut.getMemory(), "GBK");
			log.info("ReceiptStatus pOutStr = " + receiptStatusOutStr);
			pointerStatus.dispose();
			pointerPhoto.dispose();
			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("TicketReservationService ReceiptStatus:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("TicketReservationService ReceiptStatus:", e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("TicketReservationService ReceiptStatus:", e);
		}
		return retval;
	}

	/**
	 * 函数参数: pOut: 错误信息（见表一）。 函数功能： 释放动态库资源。 返回值： 0：成功 1：失败
	 * 
	 * @param pOutStr
	 * @return
	 */
	public int UnInit() {
		int retval = -1;
		Pointer pointerOut = null;
		this.unInitOutStr = null;
		try {
			pointerOut = new Pointer(MemoryBlockFactory.createMemoryBlock(60));
			pointerOut.zeroMemory();
			int i = 0;

			jnativeUnInit.setRetVal(Type.INT);
			jnativeUnInit.setParameter(i++, pointerOut);
			jnativeUnInit.invoke();

			retval = jnativeUnInit.getRetValAsInt();
			log.info("UnInit retval==" + retval);

			unInitOutStr = new String(pointerOut.getMemory(), "GBK");
			log.info("UnInit pOutStr = " + unInitOutStr);

			pointerOut.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("TicketReservationService UnInit:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("TicketReservationService UnInit:", e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("TicketReservationService UnInit:", e);
		}
		return retval;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TicketReservationService TRService = TicketReservationService.getInstance();
		String pOutStr = "";
		int initRet = TRService.Init("RXTa", "0JWAG-GT", "00000000000000001111111111111111", "20170523");

		pOutStr = "";
		TRService.SendDeviceInfo("011010");

		pOutStr = "";
		TIDCardInfo tidCardInfo = new TIDCardInfo();
		byte[] IDName = "赵林".getBytes();
		tidCardInfo.setIDName(IDName);
		byte[] IDSex = "01".getBytes();
		tidCardInfo.setIDSex(IDSex);

		// TRService.TicketVerify("", tidCardInfo, "0", pOutStr);

		String ss = "20170523";
		System.out.println(ss.getBytes().length);
		System.out.println(CommUtil.bytesToHexString(ss.getBytes()));
	}

}
