package com.rxtec.pitchecking.device;

import javax.swing.*;
import org.xvolks.jnative.JNative;
import org.xvolks.jnative.Type;
import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.pointers.Pointer;
import org.xvolks.jnative.pointers.memory.MemoryBlockFactory;

import com.rxtec.pitchecking.utils.CommUtil;

import java.awt.*;
import javax.imageio.ImageIO;
import java.io.*;
import java.util.StringTokenizer;
import java.awt.event.*;

public class IdentityInfoReader extends JFrame implements ActionListener {
	private static final long serialVersionUID = -8959055752439578441L;
	JLabel[] label = new JLabel[10];
	JButton showBmp;
	JTextField[] text = new JTextField[9];
	String[] labelItem = { "姓名：", "性别：", "民族：", "出生年月：", "地址：", "身份证号：", "签发机构：", "期限起始：", "期限终止：" };
	JNative openJN = null, findJN = null, selectJN = null, readJN = null, MngJN = null, BmpJN = null, closeJN = null;
	JButton readJB, exitJB;
	int iPort = 1001;
	int iIfOpen = 0;

	public IdentityInfoReader(String str) {
		super(str);
		System.loadLibrary("sdtapi");
		try {
			try {
				init();
			} catch (NativeException e) {
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} catch (IllegalAccessException e) {
		}
		label[0] = new JLabel("二代身份证信息");
		label[0].setBounds(200, 20, 95, 25);
		getContentPane().add(label[0]);
		for (int j = 1; j < labelItem.length + 1; j++) {
			label[j] = new JLabel(labelItem[j - 1]);
			label[j].setBounds(20, 30 + j * 30, 70, 25);
			getContentPane().add(label[j]);
		}
		for (int j = 0; j < labelItem.length; j++) {
			text[j] = new JTextField("");
			text[j].setBounds(95, 60 + j * 30, 250, 25);
			getContentPane().add(text[j]);
		}
		showBmp = new JButton();
		showBmp.setBounds(350, 60, 100, 130);
		exitJB = new JButton("退出");
		exitJB.setBounds(120, 400, 90, 26);
		readJB = new JButton("读身份证");
		readJB.setBounds(20, 400, 90, 26);
		readJB.addActionListener(this);
		exitJB.addActionListener(this);
		getContentPane().add(readJB);
		getContentPane().add(exitJB);
		getContentPane().setBackground(new Color(225, 238, 210));
		setLayout(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// setSize(500,500);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == readJB) {
			read();

		}
		if (event.getSource() == exitJB) {
			System.exit(0);
		}

	}

	public void init() throws NativeException, IllegalAccessException, UnsupportedEncodingException {
		closeJN = new JNative("sdtapi", "SDT_ClosePort");
		closeJN.setRetVal(org.xvolks.jnative.Type.INT);
		closeJN.setParameter(0, org.xvolks.jnative.Type.INT, "" + iPort);
		openJN = new JNative("sdtapi", "SDT_OpenPort");
		openJN.setRetVal(org.xvolks.jnative.Type.INT);
		int i = 0;
		openJN.setParameter(i++, org.xvolks.jnative.Type.INT, "" + iPort);
		openJN.invoke();
		if (openJN.getRetVal().equals("144"))
			System.out.println("端口已打开，请放置身份证！");
		else {
			closeJN.invoke();
			System.out.println("端口未打开！");
		}
	}

	/**
	 * 
	 */
	public void read() { // throws NativeException,IllegalAccessException,
							// UnsupportedEncodingException{
		try {
			try {
				int i = 0, j = 0, k = 0;
				String pucIIN = "", pucSN = "";
				Pointer chmsgPointer = new Pointer(MemoryBlockFactory.createMemoryBlock(1000));
				Pointer chlenPointer = new Pointer(MemoryBlockFactory.createMemoryBlock(256));
				Pointer phmsgPointer = new Pointer(MemoryBlockFactory.createMemoryBlock(1024 * 3));
				Pointer phlenPointer = new Pointer(MemoryBlockFactory.createMemoryBlock(1024));
				// Pointer pointer = new
				// Pointer(MemoryBlockFactory.createMemoryBlock(1024));

				findJN = new JNative("sdtapi", "SDT_StartFindIDCard");
				findJN.setRetVal(org.xvolks.jnative.Type.INT);
				findJN.setParameter(i++, org.xvolks.jnative.Type.INT, "" + iPort);
				findJN.setParameter(i++, pucIIN);
				findJN.setParameter(i++, org.xvolks.jnative.Type.INT, "" + iIfOpen);
				findJN.invoke();
				if (findJN.getRetVal().equals("159"))
					System.out.println("已找到二代身份证");
				else {
					closeJN.invoke();
				}
				selectJN = new JNative("sdtapi", "SDT_SelectIDCard");
				selectJN.setRetVal(org.xvolks.jnative.Type.INT);
				selectJN.setParameter(j++, org.xvolks.jnative.Type.INT, "" + iPort);
				selectJN.setParameter(j++, pucSN);
				selectJN.setParameter(j++, org.xvolks.jnative.Type.INT, "" + iIfOpen);
				selectJN.invoke();
				if (selectJN.getRetVal().equals("144"))
					System.out.println("已选择二代身份证");
				else {
					closeJN.invoke();
					init();
				}
				readJN = new JNative("sdtapi", "SDT_ReadBaseMsg");
				readJN.setRetVal(org.xvolks.jnative.Type.INT);
				readJN.setParameter(k++, org.xvolks.jnative.Type.INT, "" + iPort);
				readJN.setParameter(k++, chmsgPointer);
				readJN.setParameter(k++, chlenPointer);
				readJN.setParameter(k++, phmsgPointer);
				readJN.setParameter(k++, phlenPointer);
				readJN.setParameter(k++, org.xvolks.jnative.Type.INT, "" + iIfOpen);
				readJN.invoke();
				if (readJN.getRetVal().equals("144"))
					System.out.println("读取二代身份证成功！");
				else {
					closeJN.invoke();
					init();
				}
				int count = chlenPointer.getSize();
				byte[] byteArray = new byte[count + 2];
				for (i = 0; i < count; i++)
					byteArray[i + 2] = chmsgPointer.getAsByte(i);
				byteArray[0] = (byte) 0xff;
				byteArray[1] = (byte) 0xfe;
				String msg = new String(byteArray, "utf-16");
				StringTokenizer st = new StringTokenizer(msg);
				int hh = 0;
				String[] Info = new String[5];
				while (st.hasMoreElements()) {
					Info[hh++] = (String) st.nextElement();
					System.out.println(Info[hh - 1]);
				}
				text[0].setText(Info[0]);

				if (Info[1].charAt(0) == '1') {
					text[1].setText("男");
				} else if (Info[1].charAt(0) == '2')
					text[1].setText("女");
				char[] nationChar = new char[2];
				Info[1].getChars(1, 3, nationChar, 0);
				String nationStr = "";
				nationStr = String.valueOf(nationChar);
				if (nationStr.equals("01"))
					text[2].setText("汉");
				else if (nationStr.equals("02"))
					text[2].setText("蒙古族");
				else if (nationStr.equals("03"))
					text[2].setText("回族");
				else if (nationStr.equals("04"))
					text[2].setText("藏族");
				else if (nationStr.equals("05"))
					text[2].setText("维吾尔族");
				else if (nationStr.equals("06"))
					text[2].setText("苗族");
				else if (nationStr.equals("07"))
					text[2].setText("彝族");
				else if (nationStr.equals("08"))
					text[2].setText("壮族");
				else if (nationStr.equals("09"))
					text[2].setText("布依族");
				else if (nationStr.equals("10"))
					text[2].setText("朝鲜族");
				else if (nationStr.equals("11"))
					text[2].setText("满族");
				else if (nationStr.equals("12"))
					text[2].setText("侗族");
				else if (nationStr.equals("13"))
					text[2].setText("瑶族");
				else if (nationStr.equals("14"))
					text[2].setText("白族");
				else if (nationStr.equals("15"))
					text[2].setText("土家族");
				else if (nationStr.equals("16"))
					text[2].setText("哈尼族");
				else if (nationStr.equals("17"))
					text[2].setText("哈萨克族");
				else if (nationStr.equals("18"))
					text[2].setText("傣族");
				else if (nationStr.equals("19"))
					text[2].setText("黎族");
				else if (nationStr.equals("20"))
					text[2].setText("傈僳族");
				else if (nationStr.equals("21"))
					text[2].setText("佤族");
				else if (nationStr.equals("22"))
					text[2].setText("畲族");
				else if (nationStr.equals("23"))
					text[2].setText("高山族");
				else if (nationStr.equals("24"))
					text[2].setText("拉祜族");
				else if (nationStr.equals("25"))
					text[2].setText("水族");
				else if (nationStr.equals("26"))
					text[2].setText("东乡族");
				else if (nationStr.equals("27"))
					text[2].setText("纳西族");
				else if (nationStr.equals("28"))
					text[2].setText("景颇族");
				else if (nationStr.equals("29"))
					text[2].setText("柯尔克孜族");
				else if (nationStr.equals("30"))
					text[2].setText("土族");
				else if (nationStr.equals("31"))
					text[2].setText("达翰尔族");
				else if (nationStr.equals("32"))
					text[2].setText("仫佬族");
				else if (nationStr.equals("33"))
					text[2].setText("羌族");
				else if (nationStr.equals("34"))
					text[2].setText("布朗族");
				else if (nationStr.equals("35"))
					text[2].setText("撒拉族");
				else if (nationStr.equals("36"))
					text[2].setText("毛南族");
				else if (nationStr.equals("37"))
					text[2].setText("仡佬族");
				else if (nationStr.equals("38"))
					text[2].setText("锡伯族");
				else if (nationStr.equals("39"))
					text[2].setText("阿昌族");
				else if (nationStr.equals("40"))
					text[2].setText("普米族");
				else if (nationStr.equals("41"))
					text[2].setText("哈萨克族");
				else if (nationStr.equals("42"))
					text[2].setText("怒族");
				else if (nationStr.equals("43"))
					text[2].setText("乌孜别克族");
				else if (nationStr.equals("44"))
					text[2].setText("俄罗斯族");
				else if (nationStr.equals("45"))
					text[2].setText("鄂温克族");
				else if (nationStr.equals("46"))
					text[2].setText("德昂族");
				else if (nationStr.equals("47"))
					text[2].setText("保安族");
				else if (nationStr.equals("48"))
					text[2].setText("裕固族");
				else if (nationStr.equals("49"))
					text[2].setText("京族");
				else if (nationStr.equals("50"))
					text[2].setText("塔塔尔族");
				else if (nationStr.equals("51"))
					text[2].setText("独龙族");
				else if (nationStr.equals("52"))
					text[2].setText("鄂伦春族");
				else if (nationStr.equals("53"))
					text[2].setText("赫哲族");
				else if (nationStr.equals("54"))
					text[2].setText("门巴族");
				else if (nationStr.equals("55"))
					text[2].setText("珞巴族");
				else if (nationStr.equals("56"))
					text[2].setText("基诺族");
				else if (nationStr.equals("57"))
					text[2].setText("其它");
				else if (nationStr.equals("98"))
					text[2].setText("外国人入籍");
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
				text[3].setText(BirthyearStr + "年" + BirthmonthStr + "月" + BirthdateStr + "日");
				char[] addressChar = new char[Info[1].length() - 11];
				String addressStr = "";
				Info[1].getChars(11, Info[1].length(), addressChar, 0);
				addressStr = String.valueOf(addressChar);
				text[4].setText(addressStr);
				char[] INNChar = new char[18];
				Info[2].getChars(0, 18, INNChar, 0);
				String INNStr = "";
				INNStr = String.valueOf(INNChar);
				text[5].setText(INNStr);
				char[] issueChar = new char[Info[2].length() - 18];
				Info[2].getChars(18, Info[2].length(), issueChar, 0);
				String issueStr = "";
				issueStr = String.valueOf(issueChar);
				text[6].setText(issueStr);
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
				text[7].setText(startyearStr + "年" + startmonthStr + "月" + startdateStr + "日");
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
				text[8].setText(endyearStr + "年" + endmonthStr + "月" + enddateStr + "日");

				// 读相片数据
				int count1 = phlenPointer.getSize();
				byte[] byteArray1 = new byte[count1];
				for (i = 0; i < count1; i++)
					byteArray1[i] = phmsgPointer.getAsByte(i);
				try {
					File myFile = new File("zp.wlt");
					FileOutputStream out = new FileOutputStream(myFile);
					out.write(byteArray1, 0, count1 - 1);
				} catch (IOException t) {
					t.printStackTrace();
				}
				closeJN.invoke();
				// System.out.println(closeJN.getRetVal());
				int l = 0;
				System.loadLibrary("WltRS");
				BmpJN = new JNative("WltRS", "GetBmp");
				BmpJN.setRetVal(org.xvolks.jnative.Type.INT);
				BmpJN.setParameter(l++, "zp.wlt");
				BmpJN.setParameter(l++, 1);
				BmpJN.invoke();
				if (readJN.getRetVal().equals("144"))
					System.out.println("相片解码成功！");
				else
					System.out.println("相片解码不成功！");
				Image image = null;
				try {
					image = ImageIO.read(new File("zp.bmp"));
				} catch (IOException ex) {
				}
				ImageIcon icon = new ImageIcon(image);
				showBmp.setIcon(icon);
				getContentPane().add(showBmp);
				setVisible(true);
				chmsgPointer.dispose();
				chlenPointer.dispose();
				phmsgPointer.dispose();
				phlenPointer.dispose();
				// pointer.dispose();
			} catch (NativeException e) {
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} catch (IllegalAccessException e) {
		}
	}

	public static void main(String agrs[]) {
		IdentityInfoReader identityInfoReader = new IdentityInfoReader("二代身份证信息读取");
		identityInfoReader.setSize(500, 500);
	}
}
