package com.rxtec.pitchecking.device;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xvolks.jnative.JNative;
import org.xvolks.jnative.Type;
import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.pointers.Pointer;
import org.xvolks.jnative.pointers.memory.MemoryBlockFactory;

import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.gui.FaceCheckFrame;
import com.rxtec.pitchecking.gui.TicketCheckFrame;
import com.rxtec.pitchecking.picheckingservice.FaceDetectByPixelJNIEntryClone;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.DateUtils;

/**
 * Syn_OpenPort Syn_SetMaxRFByte(m_Port, 80, 0); Syn_StartFindIDCard(m_Port,* *
 * pucIIN, 0); Syn_SelectIDCard(m_Port, pucSN, 0); Syn_ReadFPMsg(m_Port, 0, * *
 * &idcardData, szFPPath); Syn_ClosePort(m_Port);
 * 
 * @author ZhaoLin
 *
 */
public class IDCardDevice {
	private static Logger log = LoggerFactory.getLogger("IDCardDevice");
	private static IDCardDevice instance = null;
	private int port = -1;
	private static int bIfOpen = 0;

	private JNative findUSBReaderJNative = null;
	private JNative openPortJNative = null;
	private JNative closePortJNative = null;
	private JNative findIDCardJNative = null;
	private JNative selectIDCardJNative = null;
	private JNative readBaseMsgJNative = null;
	private JNative BmpJN = null;

	public static void main(String[] args) {

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		IDCardDevice device = IDCardDevice.getInstance();
		if (device.getPort() != 0) {
			TicketCheckFrame ticketFrame = new TicketCheckFrame();
//			frame.setVisible(true);
			gs[0].setFullScreenWindow(ticketFrame);
			
			while (true) {
				device.Syn_OpenPort();
				String findval = device.Syn_StartFindIDCard();
				if (findval.equals("0")) {
					String selectval = device.Syn_SelectIDCard();
					if (selectval.equals("0")) {
						IDCard idCard = new IDCard();
						idCard = device.Syn_ReadBaseMsg();
						if (idCard.getIdNo() != null) {
							ticketFrame.showWaitInputContent(null, idCard, 1);
							CommUtil.sleep(3000);
						}
					}
				}
				device.Syn_ClosePort();

				CommUtil.sleep(150);
			}
		}
	}

	private IDCardDevice() {
		JNative.setLoggingEnabled(false);
		try {
			findUSBReaderJNative = new JNative("SynIDCardAPI.dll", "Syn_FindUSBReader");
			openPortJNative = new JNative("SynIDCardAPI.dll", "Syn_OpenPort");
			closePortJNative = new JNative("SynIDCardAPI.dll", "Syn_ClosePort");
			findIDCardJNative = new JNative("SynIDCardAPI.dll", "Syn_StartFindIDCard");
			selectIDCardJNative = new JNative("SynIDCardAPI.dll", "Syn_SelectIDCard");
			readBaseMsgJNative = new JNative("SynIDCardAPI.dll", "Syn_ReadBaseMsg");
			BmpJN = new JNative("WltRS.dll", "GetBmp");
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.port = Integer.parseInt(Syn_FindUSBReader());
		this.setPort(port);
		// if (this.port != 0) {
		// String openResult = Syn_OpenPort();
		// }

	}

	public static synchronized IDCardDevice getInstance() {
		if (instance == null) {
			instance = new IDCardDevice();
		}
		return instance;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getBIfOpen() {
		return bIfOpen;
	}

	/**
	 * Syn_FindUSBReader 自动寻找USB读卡器。 int Syn_FindUSBReader (); 返回值： 0 未找到 其他
	 * 1001～1016USB
	 * 
	 * @param port
	 * @return
	 */
	public String Syn_FindUSBReader() {
		String retval = "";
		try {
			findUSBReaderJNative.setRetVal(Type.INT);
			findUSBReaderJNative.invoke();
			retval = findUSBReaderJNative.getRetVal();
			log.info("Syn_FindUSBReader:retval==" + retval);// 获取返回值
			if (retval.equals("0")) {
				DeviceConfig.getInstance().setIdDeviceStatus(Integer.parseInt(retval));
			} else {
				DeviceConfig.getInstance().setIdDeviceStatus(DeviceConfig.idDeviceSucc);
			}
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retval;
	}

	public String Syn_OpenPort() {
		String retval = "";
		if (this.port != 0) {
			try {
				int i = 0;
				openPortJNative.setParameter(i, port);
				openPortJNative.setRetVal(Type.INT);
				openPortJNative.invoke();
				retval = openPortJNative.getRetVal();
				// log.debug("Syn_OpenPort:retval==" + retval);// 获取返回值
			} catch (NativeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return retval;
	}

	public String Syn_ClosePort() {
		String retval = "";
		try {
			int i = 0;
			closePortJNative.setParameter(i, port);
			closePortJNative.setRetVal(Type.INT);
			closePortJNative.invoke();
			retval = closePortJNative.getRetVal();
			// log.debug("Syn_ClosePort:retval==" + retval);// 获取返回值
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
	 * Syn_SetMaxRFByte 设置射频适配器最大通信字节数 int Syn_SetMaxRFByte ( int iPort,
	 * unsigned char ucByte, int bIfOpen );
	 * 
	 */
	// public String Syn_SetMaxRFByte(int port, String ucByte, int bIfOpen) {
	//
	// JNative jnative = null;
	// String retval = "";
	// try {
	// int i = 0;
	//
	// jnative = new JNative("SynIDCardAPI.dll", "Syn_SetMaxRFByte");
	// jnative.setParameter(i++, port);
	// jnative.setParameter(i++, ucByte);
	// jnative.setParameter(i++, bIfOpen);
	// jnative.setRetVal(Type.INT);
	// jnative.invoke();
	// retval = jnative.getRetVal();
	// log.debug("Syn_SetMaxRFByte: retval==" + retval);// 获取返回值
	// } catch (NativeException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IllegalAccessException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// return retval;
	// }

	/**
	 * Syn_StartFindIDCard 开始找卡
	 */
	public String Syn_StartFindIDCard() {
		String retval = "";
		try {
			int i = 0;
			// log.debug("port==" + String.valueOf(port));
			// Pointer pointer = new
			// Pointer(MemoryBlockFactory.createMemoryBlock(4));
			String pucIIN = "";
			findIDCardJNative.setParameter(i++, port);
			findIDCardJNative.setParameter(i++, pucIIN);
			findIDCardJNative.setParameter(i++, bIfOpen);
			findIDCardJNative.setRetVal(Type.INT);
			findIDCardJNative.invoke();

			retval = findIDCardJNative.getRetVal();
			// log.debug("Syn_StartFindIDCard: retval==" + retval);// 获取返回值
			// log.debug("Syn_StartFindIDCard: pucIIN==" + pucIIN);// 获取返回值
			if (retval.equals("0")) {
				log.debug("已找到二代身份证");
			}
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
	 * Syn_SelectIDCard 选卡
	 */
	public String Syn_SelectIDCard() {
		String retval = "";
		try {
			int i = 0;

			// Pointer pointer = new
			// Pointer(MemoryBlockFactory.createMemoryBlock(4));
			String pucSN = "";
			selectIDCardJNative.setParameter(i++, port);
			selectIDCardJNative.setParameter(i++, pucSN);
			selectIDCardJNative.setParameter(i++, bIfOpen);
			selectIDCardJNative.setRetVal(Type.INT);
			selectIDCardJNative.invoke();

			retval = selectIDCardJNative.getRetVal();
			// log.debug("Syn_SelectIDCard: retval==" + retval);// 获取返回值
			// log.debug("Syn_SelectIDCard: pucSN==" + pucSN);// 获取返回值
			if (retval.equals("0")) {
				// log.debug("已选择二代身份证");
			}

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
	 * Syn_ReadBaseMsg 读取身份证内基本信息区域信息。 int Syn_ReadBaseMsg ( int iPort， unsigned
	 * char * pucCHMsg， unsigned int * puiCHMsgLen， unsigned char * pucPHMsg，
	 * unsigned int * puiPHMsgLen, int iIfOpen );
	 */
	public IDCard Syn_ReadBaseMsg() {
		String retval = "";
		IDCard synIDCard = new IDCard();
		Pointer chmsgPointer = null;
		Pointer chlenPointer = null;
		Pointer phmsgPointer = null;
		Pointer phlenPointer = null;
		try {
			int i = 0;

			chmsgPointer = new Pointer(MemoryBlockFactory.createMemoryBlock(500));
			chlenPointer = new Pointer(MemoryBlockFactory.createMemoryBlock(256));
			phmsgPointer = new Pointer(MemoryBlockFactory.createMemoryBlock(1024 * 3));
			phlenPointer = new Pointer(MemoryBlockFactory.createMemoryBlock(1024));

			readBaseMsgJNative.setParameter(i++, port);
			readBaseMsgJNative.setParameter(i++, chmsgPointer);
			readBaseMsgJNative.setParameter(i++, chlenPointer);
			readBaseMsgJNative.setParameter(i++, phmsgPointer);
			readBaseMsgJNative.setParameter(i++, phlenPointer);
			readBaseMsgJNative.setParameter(i++, bIfOpen);
			readBaseMsgJNative.setRetVal(Type.INT);
			readBaseMsgJNative.invoke();
			retval = readBaseMsgJNative.getRetVal();
			if (retval.equals("0")) {
				// log.debug("读取二代身份证成功！");

				int count = chlenPointer.getSize();
				byte[] byteArray = new byte[count + 2];
				for (int k = 0; k < count; k++) {
					byteArray[k + 2] = chmsgPointer.getAsByte(k);
				}
				byteArray[0] = (byte) 0xff;
				byteArray[1] = (byte) 0xfe;
				String msg = new String(byteArray, "utf-16");
				StringTokenizer st = new StringTokenizer(msg);
				int hh = 0;
				String[] Info = new String[5];
				while (st.hasMoreElements()) {
					Info[hh++] = (String) st.nextElement();
					// log.debug(Info[hh - 1]);
				}
				// log.debug("姓名：" + Info[0]);
				synIDCard.setPersonName(Info[0]);   //set personName
				if (Info[1].charAt(0) == '1') {
					synIDCard.setGender(1);
					synIDCard.setGenderCH("男");//set gender
					// log.debug("性别：" + "男");
				} else if (Info[1].charAt(0) == '2') {
					synIDCard.setGender(2);
					synIDCard.setGenderCH("女");//set gender
					// log.debug("性别：" + "女");
				}
				char[] nationChar = new char[2];
				Info[1].getChars(1, 3, nationChar, 0);
				String nationStr = "";
				nationStr = String.valueOf(nationChar);
				// if (nationStr.equals("01"))
				//// log.debug("汉");
				// else if (nationStr.equals("02"))
				// log.debug("蒙古族");
				// else if (nationStr.equals("03"))
				// log.debug("回族");
				// else if (nationStr.equals("04"))
				// log.debug("藏族");
				// else if (nationStr.equals("05"))
				// log.debug("维吾尔族");
				// else if (nationStr.equals("06"))
				// log.debug("苗族");
				// else if (nationStr.equals("07"))
				// log.debug("彝族");
				// else if (nationStr.equals("08"))
				// log.debug("壮族");
				// else if (nationStr.equals("09"))
				// log.debug("布依族");
				// else if (nationStr.equals("10"))
				// log.debug("朝鲜族");
				// else if (nationStr.equals("11"))
				// log.debug("满族");
				// else if (nationStr.equals("12"))
				// log.debug("侗族");
				// else if (nationStr.equals("13"))
				// log.debug("瑶族");
				// else if (nationStr.equals("14"))
				// log.debug("白族");
				// else if (nationStr.equals("15"))
				// log.debug("土家族");
				// else if (nationStr.equals("16"))
				// log.debug("哈尼族");
				// else if (nationStr.equals("17"))
				// log.debug("哈萨克族");
				// else if (nationStr.equals("18"))
				// log.debug("傣族");
				// else if (nationStr.equals("19"))
				// log.debug("黎族");
				// else if (nationStr.equals("20"))
				// log.debug("傈僳族");
				// else if (nationStr.equals("21"))
				// log.debug("佤族");
				// else if (nationStr.equals("22"))
				// log.debug("畲族");
				// else if (nationStr.equals("23"))
				// log.debug("高山族");
				// else if (nationStr.equals("24"))
				// log.debug("拉祜族");
				// else if (nationStr.equals("25"))
				// log.debug("水族");
				// else if (nationStr.equals("26"))
				// log.debug("东乡族");
				// else if (nationStr.equals("27"))
				// log.debug("纳西族");
				// else if (nationStr.equals("28"))
				// log.debug("景颇族");
				// else if (nationStr.equals("29"))
				// log.debug("柯尔克孜族");
				// else if (nationStr.equals("30"))
				// log.debug("土族");
				// else if (nationStr.equals("31"))
				// log.debug("达翰尔族");
				// else if (nationStr.equals("32"))
				// log.debug("仫佬族");
				// else if (nationStr.equals("33"))
				// log.debug("羌族");
				// else if (nationStr.equals("34"))
				// log.debug("布朗族");
				// else if (nationStr.equals("35"))
				// log.debug("撒拉族");
				// else if (nationStr.equals("36"))
				// log.debug("毛南族");
				// else if (nationStr.equals("37"))
				// log.debug("仡佬族");
				// else if (nationStr.equals("38"))
				// log.debug("锡伯族");
				// else if (nationStr.equals("39"))
				// log.debug("阿昌族");
				// else if (nationStr.equals("40"))
				// log.debug("普米族");
				// else if (nationStr.equals("41"))
				// log.debug("哈萨克族");
				// else if (nationStr.equals("42"))
				// log.debug("怒族");
				// else if (nationStr.equals("43"))
				// log.debug("乌孜别克族");
				// else if (nationStr.equals("44"))
				// log.debug("俄罗斯族");
				// else if (nationStr.equals("45"))
				// log.debug("鄂温克族");
				// else if (nationStr.equals("46"))
				// log.debug("德昂族");
				// else if (nationStr.equals("47"))
				// log.debug("保安族");
				// else if (nationStr.equals("48"))
				// log.debug("裕固族");
				// else if (nationStr.equals("49"))
				// log.debug("京族");
				// else if (nationStr.equals("50"))
				// log.debug("塔塔尔族");
				// else if (nationStr.equals("51"))
				// log.debug("独龙族");
				// else if (nationStr.equals("52"))
				// log.debug("鄂伦春族");
				// else if (nationStr.equals("53"))
				// log.debug("赫哲族");
				// else if (nationStr.equals("54"))
				// log.debug("门巴族");
				// else if (nationStr.equals("55"))
				// log.debug("珞巴族");
				// else if (nationStr.equals("56"))
				// log.debug("基诺族");
				// else if (nationStr.equals("57"))
				// log.debug("其它");
				// else if (nationStr.equals("98"))
				// log.debug("外国人入籍");
				String BirthyearStr = "";
				char[] BirthyearChar = new char[4];
				Info[1].getChars(3, 7, BirthyearChar, 0);
				BirthyearStr = String.valueOf(BirthyearChar);
				String BirthmonthStr = "";
				char[] BirthmonthChar = new char[2];
				Info[1].getChars(7, 9, BirthmonthChar, 0);
				BirthmonthStr = String.valueOf(BirthmonthChar);
				String BirthdateStr = "";
				char[] BirthdateChar = new char[2];
				Info[1].getChars(9, 11, BirthdateChar, 0);
				BirthdateStr = String.valueOf(BirthdateChar);
				String birthday = BirthyearStr+"-"+BirthmonthStr+"-"+BirthdateStr;
				String today = DateUtils.getStringDateShort();
				synIDCard.setAge(DateUtils.getAge(birthday, today));  //set age
				// log.debug("出生年月：" + BirthyearStr + "年" + BirthmonthStr + "月"
				// + BirthdateStr + "日");
				char[] addressChar = new char[Info[1].length() - 11];
				String addressStr = "";
				Info[1].getChars(11, Info[1].length(), addressChar, 0);
				addressStr = String.valueOf(addressChar);
				// log.debug("住址：" + addressStr);
				char[] INNChar = new char[18];
				Info[2].getChars(0, 18, INNChar, 0);
				String IDCardNoStr = "";
				IDCardNoStr = String.valueOf(INNChar);
				// log.debug("身份证号：" + IDCardNoStr);
				synIDCard.setIdNo(IDCardNoStr); // setIdNo
				char[] issueChar = new char[Info[2].length() - 18];
				Info[2].getChars(18, Info[2].length(), issueChar, 0);
				String issueStr = "";
				issueStr = String.valueOf(issueChar);
				// log.debug("签发机关：" + issueStr);
				char[] startyearChar = new char[4];
				Info[3].getChars(0, 4, startyearChar, 0);
				String startyearStr = "";
				startyearStr = String.valueOf(startyearChar);
				char[] startmonthChar = new char[2];
				Info[3].getChars(4, 6, startmonthChar, 0);
				String startmonthStr = "";
				startmonthStr = String.valueOf(startmonthChar);
				char[] startdateChar = new char[2];
				Info[3].getChars(6, 8, startdateChar, 0);
				String startdateStr = "";
				startdateStr = String.valueOf(startdateChar);
				// log.debug(startyearStr + "年" + startmonthStr + "月" +
				// startdateStr + "日");
				char[] endyearChar = new char[4];
				Info[3].getChars(8, 12, endyearChar, 0);
				String endyearStr = "";
				endyearStr = String.valueOf(endyearChar);
				char[] endmonthChar = new char[2];
				Info[3].getChars(12, 14, endmonthChar, 0);
				String endmonthStr = "";
				endmonthStr = String.valueOf(endmonthChar);
				char[] enddateChar = new char[2];
				Info[3].getChars(14, 16, enddateChar, 0);
				String enddateStr = "";
				enddateStr = String.valueOf(enddateChar);
				// log.debug(endyearStr + "年" + endmonthStr + "月" + enddateStr +
				// "日");

				// 读相片数据
				int count1 = phlenPointer.getSize();
				byte[] byteArray1 = new byte[count1];
				for (int k = 0; k < count1; k++)
					byteArray1[k] = phmsgPointer.getAsByte(k);
				try {
					File myFile = new File("zp.wlt");
					FileOutputStream out = new FileOutputStream(myFile);
					out.write(byteArray1, 0, count1 - 1);
				} catch (IOException t) {
					t.printStackTrace();
				}

				String bmpretval = "";

				i = 0;
				BmpJN.setParameter(i++, "zp.wlt");
				BmpJN.setParameter(i++, 2);
				BmpJN.invoke();
				BmpJN.setRetVal(Type.INT);
				BmpJN.invoke();
				bmpretval = BmpJN.getRetVal();
				// log.debug("GetBmp: bmpretval==" + bmpretval);// 获取返回值

				CommUtil.bmpTojpg("zp.bmp", "zp.jpg");

				BufferedImage image = null;
				image = ImageIO.read(new File("zp.jpg"));
				synIDCard.setCardImage(image);   //set cardImage

				// log.debug("相片解码成功！");
			} else {
				log.debug("相片解码不成功！请重试!!");
			}

			chmsgPointer.dispose();
			chlenPointer.dispose();
			phmsgPointer.dispose();
			phlenPointer.dispose();

			// JNative BmpJN = null;
			// String bmpretval = "";
			//
			// i = 0;
			// BmpJN = new JNative("WltRS.dll", "GetBmp");
			// BmpJN.setParameter(i++, "zp.wlt");
			// BmpJN.setParameter(i++, 2);
			// BmpJN.invoke();
			// BmpJN.setRetVal(Type.INT);
			// BmpJN.invoke();
			// bmpretval = BmpJN.getRetVal();
			// log.debug("GetBmp: bmpretval==" + bmpretval);// 获取返回值
			// if (retval.equals("0"))
			// log.debug("相片解码成功！");
			// else
			// log.debug("相片解码不成功！请重试!!");
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (chmsgPointer != null) {
					chmsgPointer.dispose();
				}
				if (chlenPointer != null) {
					chlenPointer.dispose();
				}
				if (phmsgPointer != null) {
					phmsgPointer.dispose();
				}
				if (phlenPointer != null) {
					phlenPointer.dispose();
				}
			} catch (NativeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return synIDCard;
	}

	/**
	 * Syn_GetBmp 本函数用于将wlt文件解码成bmp文件
	 */
	public String GetBmp(int port, String readRet) {

		String retval = "";
		try {
			int i = 0;

			BmpJN.setParameter(i++, "zp.wlt");
			BmpJN.setParameter(i++, 2);
			BmpJN.invoke();
			BmpJN.setRetVal(Type.INT);
			BmpJN.invoke();
			retval = BmpJN.getRetVal();

			// log.debug("GetBmp: retval==" + retval);// 获取返回值
			if (readRet.equals("0"))
				log.debug("相片解码成功！");
			else
				log.debug("相片解码不成功！请重试!!");
			// Image image = null;
			// try {
			// image = ImageIO.read(new File("zp.bmp"));
			// } catch (IOException ex) {
			// ex.printStackTrace();
			// }
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retval;
	}

	// /**
	// * Syn_ReadMsg本函数用于读取身份证中的基本信息和照片信息，并按设置转化信息和照片 int Syn_ReadMsg( int
	// iPort,
	// * int iIfOpen, IDCardData *pIDCardData );
	// */
	// public String Syn_ReadMsg(int port, int bIfOpen) {
	//
	// JNative jnative = null;
	// String retval = "";
	// try {
	// int i = 0;
	//
	// Pointer pointer = new Pointer(MemoryBlockFactory.createMemoryBlock(100));
	//
	// jnative = new JNative("SynIDCardAPI.dll", "Syn_ReadMsg");
	// jnative.setParameter(i++, port);
	// jnative.setParameter(i++, bIfOpen);
	// jnative.setParameter(i++, pointer);
	//
	// jnative.setRetVal(Type.INT);
	// jnative.invoke();
	// retval = jnative.getRetVal();
	// byte[] bytesResult = pointer.getMemory();
	// // String bytesResult = pointer.getAsString();
	// // for (int k = 0; k < bytesResult.length; k = k + 2) {
	// // System.out.println(bytesToInt(bytesResult, k));
	// // }
	//
	// log.debug("Syn_ReadMsg: retval==" + retval);// 获取返回值
	// log.debug("Syn_ReadMsg: Msg==" + bytesToHexString(bytesResult));// 获取返回值
	//
	// pointer.dispose();
	// } catch (NativeException e) {
	// // TODO Auto-generated catch block
	// System.out.println(e);
	// e.printStackTrace();
	// } catch (IllegalAccessException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// System.out.println(e);
	// } catch (Exception e) {
	// System.out.println(e);
	// }
	// return retval;
	// }
	//
	// /**
	// * Syn_ReadFPMsg本函数用于读取身份证中的基本信息和照片信息，并按设置转化信息和照片 int Syn_ReadFPMsg( int
	// * iPort, int iIfOpen, IDCardData *pIDCardData, char * cFPhotoName );
	// */
	// public String Syn_ReadFPMsg(int port, int bIfOpen) {
	//
	// JNative jnative = null;
	// String retval = "";
	// try {
	// int i = 0;
	//
	// Pointer pointer = new Pointer(MemoryBlockFactory.createMemoryBlock(200));
	// Pointer finger = new Pointer(MemoryBlockFactory.createMemoryBlock(500));
	// String pucSN = "";
	// jnative = new JNative("SynIDCardAPI.dll", "Syn_ReadFPMsg");
	// jnative.setParameter(i++, port);
	// jnative.setParameter(i++, bIfOpen);
	// jnative.setParameter(i++, pointer);
	// jnative.setParameter(i++, finger);
	//
	// jnative.setRetVal(Type.INT);
	// jnative.invoke();
	//
	// byte[] bytesResult = pointer.getMemory();
	// // String bytesResult = pointer.getAsString();
	// byte[] fingerResult = finger.getMemory();
	// retval = jnative.getRetVal();
	// pointer.dispose();
	// finger.dispose();
	//
	// log.debug("Syn_ReadFPMsg: retval==" + retval);// 获取返回值
	// log.debug("Syn_ReadFPMsg: bytesResult==" +
	// bytesToHexString(bytesResult));// 获取返回值
	// log.debug("fingerResult==" + bytesToHexString(fingerResult));// 获取返回值
	// } catch (NativeException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IllegalAccessException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// return retval;
	// }

	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			System.out.println(v);
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	/**
	 * Convert hex string to byte[]
	 * 
	 * @param hexString
	 *            the hex string
	 * @return byte[]
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	/**
	 * Convert char to byte
	 * 
	 * @param c
	 *            char
	 * @return byte
	 */
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	/**
	 * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和和intToBytes（）配套使用
	 * 
	 * @param src
	 *            byte数组
	 * @param offset
	 *            从数组的第offset位开始
	 * @return int数值
	 */
	public static int bytesToInt(byte[] src, int offset) {
		int value;
		value = (int) ((src[offset] & 0xFF) | ((src[offset + 1] & 0xFF) << 8) | ((src[offset + 2] & 0xFF) << 16)
				| ((src[offset + 3] & 0xFF) << 24));
		return value;
	}

	public static float bytesToFloat(byte[] src, int offset) {
		return Float.intBitsToFloat(bytesToInt(src, offset));
	}

	/**
	 * public static Float bytesToFloat(byte[] src, int offset) { return
	 * Float.intBitsToFloat(bytesToInt(src, offset)); }
	 * 
	 * @param b
	 * @param offset
	 * @return
	 */

	public static double bytesToDouble(byte[] b, int offset) {
		long l;
		l = b[offset + 0];
		l &= 0xff;
		l |= ((long) b[offset + 1] << 8);
		l &= 0xffff;
		l |= ((long) b[offset + 2] << 16);
		l &= 0xffffff;
		l |= ((long) b[offset + 3] << 24);
		l &= 0xffffffffl;
		l |= ((long) b[offset + 4] << 32);
		l &= 0xffffffffffl;
		l |= ((long) b[offset + 5] << 40);
		l &= 0xffffffffffffl;
		l |= ((long) b[offset + 6] << 48);
		l &= 0xffffffffffffffl;
		l |= ((long) b[offset + 7] << 56);
		return Double.longBitsToDouble(l);
	}

}
