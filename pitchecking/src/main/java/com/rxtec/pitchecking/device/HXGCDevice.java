package com.rxtec.pitchecking.device;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xvolks.jnative.JNative;
import org.xvolks.jnative.Type;
import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.pointers.Pointer;
import org.xvolks.jnative.pointers.memory.MemoryBlockFactory;

import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.IDReader;
import com.rxtec.pitchecking.gui.TicketCheckFrame;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.CalUtils;

public class HXGCDevice {
	private static Logger log = LoggerFactory.getLogger("IDCardDevice");
	private static HXGCDevice instance = null;
	private int iPort = 1001;
	private int iIfOpen = 0;
	private JNative openJN = null;
	private JNative findJN = null;
	private JNative selectJN = null;
	private JNative readJN = null;
	private JNative BmpJN = null;
	private JNative closeJN = null;

	public static synchronized HXGCDevice getInstance() {
		if (instance == null) {
			instance = new HXGCDevice();
		}
		return instance;
	}

	private HXGCDevice() {
		try {
			// System.loadLibrary("sdtapi");
			// System.loadLibrary("WltRS");
			this.init();			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("IDCardDevice:" + e);
			try {
				log.info("自动注销计算机...");
				// Runtime.getRuntime().exec(Config.AutoLogonCmd);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	/**
	 * 华旭金卡二代证读卡器初始化
	 * 
	 * @throws NativeException
	 * @throws IllegalAccessException
	 * @throws UnsupportedEncodingException
	 */
	private void init() throws NativeException, IllegalAccessException, UnsupportedEncodingException {
		closeJN = new JNative("sdtapi.dll", "SDT_ClosePort");
		openJN = new JNative("sdtapi.dll", "SDT_OpenPort");
		findJN = new JNative("sdtapi.dll", "SDT_StartFindIDCard");
		selectJN = new JNative("sdtapi.dll", "SDT_SelectIDCard");
		readJN = new JNative("sdtapi.dll", "SDT_ReadBaseMsg");
		BmpJN = new JNative("WltRS.dll", "GetBmp");
	}

	/**
	 * 
	 * @return
	 */
	public String Syn_ClosePort() {
		String retval = "";
		try {
			closeJN.setRetVal(org.xvolks.jnative.Type.INT);
			closeJN.setParameter(0, org.xvolks.jnative.Type.INT, "" + iPort);
			closeJN.invoke();
			retval = closeJN.getRetVal();
		} catch (IllegalAccessException | NativeException e) {
			// TODO Auto-generated catch block
			log.error("IDCardDevice  SDT_ClosePort:" + e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("IDCardDevice  SDT_ClosePort:" + e);
		}
		return retval;
	}

	/**
	 * 
	 * @return
	 */
	public String Syn_OpenPort() {
		String retval = "";
		if (this.iPort != 0) {
			try {
				int i = 0;
				openJN.setParameter(i, iPort);
				openJN.setRetVal(Type.INT);
				openJN.invoke();
				retval = openJN.getRetVal();
//				log.debug("SDT_OpenPort:retval==" + retval);// 获取返回值
				if (openJN.getRetVal().equals("144")) {
//					log.debug("端口已打开，请放置身份证！");
					retval = "0";
				}else{
					log.error("SDT_OpenPort:retval==" + retval);// 获取返回值
				}
			} catch (NativeException e) {
				// TODO Auto-generated catch block
				log.error("IDCardDevice  SDT_OpenPort:" + e);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				log.error("IDCardDevice  SDT_OpenPort:" + e);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error("IDCardDevice  SDT_OpenPort:" + e);
			}
		}
		return retval;
	}

	/**
	 * 
	 * @return
	 */
	public String Syn_StartFindIDCard() {
		String retval = "";
		try {
			int i = 0;
			// log.debug("port==" + String.valueOf(port));
			// Pointer pointer = new
			// Pointer(MemoryBlockFactory.createMemoryBlock(4));
			String pucIIN = "";
			findJN.setRetVal(org.xvolks.jnative.Type.INT);
			findJN.setParameter(i++, org.xvolks.jnative.Type.INT, "" + iPort);
			findJN.setParameter(i++, pucIIN);
			findJN.setParameter(i++, org.xvolks.jnative.Type.INT, "" + iIfOpen);
			findJN.invoke();

			retval = findJN.getRetVal();
			// log.debug("SDT_StartFindIDCard: retval==" + retval);// 获取返回值
			// log.debug("SDT_StartFindIDCard: pucIIN==" + pucIIN);// 获取返回值
			if (retval.equals("159")) {
				log.debug("已找到二代身份证");
				retval = "0";
			}
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("IDCardDevice  SDT_ClosePort:" + e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("IDCardDevice  SDT_ClosePort:" + e);
		} catch (Exception e) {
			log.error("IDCardDevice  SDT_ClosePort:" + e);
		}
		return retval;
	}

	/**
	 * 
	 * @return
	 */
	public String Syn_SelectIDCard() {
		String retval = "";
		try {
			int i = 0;

			// Pointer pointer = new
			// Pointer(MemoryBlockFactory.createMemoryBlock(4));
			String pucSN = "";
			selectJN.setRetVal(org.xvolks.jnative.Type.INT);
			selectJN.setParameter(i++, org.xvolks.jnative.Type.INT, "" + iPort);
			selectJN.setParameter(i++, pucSN);
			selectJN.setParameter(i++, org.xvolks.jnative.Type.INT, "" + iIfOpen);
			selectJN.invoke();

			retval = selectJN.getRetVal();
			// log.debug("SDT_SelectIDCard: retval==" + retval);// 获取返回值
			// log.debug("SDT_SelectIDCard: pucSN==" + pucSN);// 获取返回值
			if (retval.equals("144")) {
				log.debug("已选择二代身份证");
				retval = "0";
			}

		} catch (NativeException e) {
			// TODO Auto-generated catch block
			log.error("IDCardDevice  SDT_SelectIDCard:" + e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			log.error("IDCardDevice  SDT_SelectIDCard:" + e);
		} catch (Exception e) {
			log.error("IDCardDevice  SDT_SelectIDCard:" + e);
		}
		return retval;
	}

	/**
	 * 
	 * @return
	 */
	public IDCard Syn_ReadBaseMsg() {
		String retval = "";
		IDCard synIDCard = null;
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

			readJN.setRetVal(org.xvolks.jnative.Type.INT);
			readJN.setParameter(i++, org.xvolks.jnative.Type.INT, "" + iPort);
			readJN.setParameter(i++, chmsgPointer);
			readJN.setParameter(i++, chlenPointer);
			readJN.setParameter(i++, phmsgPointer);
			readJN.setParameter(i++, phlenPointer);
			readJN.setParameter(i++, org.xvolks.jnative.Type.INT, "" + iIfOpen);
			readJN.invoke();
			retval = readJN.getRetVal();
			if (retval.equals("144")) {
				// log.debug("读取二代身份证成功！");

				synIDCard = new IDCard();
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
					log.debug(Info[hh - 1]);
				}
				// log.debug("姓名：" + Info[0]);
				synIDCard.setPersonName(Info[0]); // set personName
				// log.debug("性别+国籍+种族+生日字符串长度==" + Info[1].length());
				if (Info[1].charAt(0) == '1') {
					synIDCard.setGender(1);
					synIDCard.setGenderCH("男");// set gender
					// log.debug("性别：" + "男");
				} else if (Info[1].charAt(0) == '2') {
					synIDCard.setGender(2);
					synIDCard.setGenderCH("女");// set gender
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
				String birthday = BirthyearStr + "-" + BirthmonthStr + "-" + BirthdateStr;
				String today = CalUtils.getStringDateShort();
				// log.debug("birthday==" + birthday);
				int personAge = CalUtils.getAge(birthday, today);
				synIDCard.setAge(personAge); // set age
				log.debug("出生年月：" + BirthyearStr + "年" + BirthmonthStr + "月" + BirthdateStr + "日" + ",personAge=="
						+ personAge);
				char[] addressChar = new char[Info[1].length() - 11];
				String addressStr = "";
				Info[1].getChars(11, Info[1].length(), addressChar, 0);
				addressStr = String.valueOf(addressChar);
				// log.debug("住址：" + addressStr);

				String IDCardNoStr = "";
				IDCardNoStr = CommUtil.getIdCardNoFromInfo(Info[1]); // 首先判断info1中是否已经包含身份证号
				if (IDCardNoStr.equals("")) {
					char[] INNChar = new char[18];
					// log.debug("身份证号字符串长度==" + Info[2].length());
					log.debug("身份证号字符串info[2]==" + Info[2]);
					Info[2].getChars(0, 18, INNChar, 0);

					IDCardNoStr = String.valueOf(INNChar);
					log.debug("从info[2]中读取身份证号：" + IDCardNoStr);
					synIDCard.setIdNo(IDCardNoStr); // setIdNo
				} else {
					synIDCard.setIdNo(IDCardNoStr);
					log.debug("从info[1]中读取身份证号：" + IDCardNoStr);
				}
				// 以下代码未使用，先屏蔽
				// char[] issueChar = new char[Info[2].length() - 18];
				// Info[2].getChars(18, Info[2].length(), issueChar, 0);
				// String issueStr = "";
				// issueStr = String.valueOf(issueChar);
				// log.debug("签发机关：" + issueStr);
				// char[] startyearChar = new char[4];
				// Info[3].getChars(0, 4, startyearChar, 0);
				// String startyearStr = "";
				// startyearStr = String.valueOf(startyearChar);
				// char[] startmonthChar = new char[2];
				// Info[3].getChars(4, 6, startmonthChar, 0);
				// String startmonthStr = "";
				// startmonthStr = String.valueOf(startmonthChar);
				// char[] startdateChar = new char[2];
				// Info[3].getChars(6, 8, startdateChar, 0);
				// String startdateStr = "";
				// startdateStr = String.valueOf(startdateChar);
				// char[] endyearChar = new char[4];
				// Info[3].getChars(8, 12, endyearChar, 0);
				// String endyearStr = "";
				// endyearStr = String.valueOf(endyearChar);
				// char[] endmonthChar = new char[2];
				// Info[3].getChars(12, 14, endmonthChar, 0);
				// String endmonthStr = "";
				// endmonthStr = String.valueOf(endmonthChar);
				// char[] enddateChar = new char[2];
				// Info[3].getChars(14, 16, enddateChar, 0);
				// String enddateStr = "";
				// enddateStr = String.valueOf(enddateChar);
				if (IDCardNoStr != null && IDCardNoStr.length() == 18) {
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

					/**
					 * Syn_GetBmp本函数用于将wlt文件解码成bmp文件
					 * 参数2：阅读设备通讯接口类型（1—RS-232C，2—USB）
					 */
					int j = 0;
					String bmpretval = "";

					BmpJN.setRetVal(org.xvolks.jnative.Type.INT);
					BmpJN.setParameter(j++, "zp.wlt");
					BmpJN.setParameter(j++, 2);
					BmpJN.invoke();
					bmpretval = BmpJN.getRetVal();
					log.debug("GetBmp: bmpretval==" + bmpretval);// 获取返回值

					CommUtil.bmpTojpg("zp.bmp", "zp.jpg");

					File idcardFile = new File("zp.jpg");
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
					boolean delFlag = idcardFile.delete();
					log.debug("二代证jpg照片删除 flag=" + delFlag + ",二代证bmp照片删除 flag=" + (new File("zp.bmp")).delete());
					if (synIDCard.getCardImage() != null && synIDCard.getCardImageBytes() != null)
						log.debug("完整读取二代证全部信息成功！");
				} else {
					synIDCard = null;
					log.debug("读取二代证信息失败！未截取到身份证号:" + IDCardNoStr);
				}
			} else {
				log.debug("读取二代证信息失败！请重试!!retval==" + retval);
			}

			chmsgPointer.dispose();
			chlenPointer.dispose();
			phmsgPointer.dispose();
			phlenPointer.dispose();
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			synIDCard = null;
			log.error("IDCardDevice  SDT_ReadBaseMsg:", e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			synIDCard = null;
			log.error("IDCardDevice  SDT_ReadBaseMsg:", e);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			synIDCard = null;
			log.error("IDCardDevice  SDT_ReadBaseMsg:", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			synIDCard = null;
			log.error("IDCardDevice  SDT_ReadBaseMsg:", e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			synIDCard = null;
			log.error("IDCardDevice  SDT_ReadBaseMsg:", e);
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
}
