package com.rxtec.pitchecking.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.rxtec.pitchecking.IDCard;

/**
 * 身份证验证的工具（支持5位或18位省份证） 身份证号码结构： 17位数字和1位校验码：6位地址码数字，8位生日数字，3位出生时间顺序号，1位校验码。
 * 地址码（前6位）：表示对象常住户口所在县（市、镇、区）的行政区划代码，按GB/T2260的规定执行。 出生日期码，（第七位
 * 至十四位）：表示编码对象出生年、月、日，按GB按GB/T7408的规定执行，年、月、日代码之间不用分隔符。
 * 顺序码（第十五位至十七位）：表示在同一地址码所标示的区域范围内，对同年、同月、同日出生的人编订的顺序号， 顺序码的奇数分配给男性，偶数分配给女性。
 * 校验码（第十八位数）： 十七位数字本体码加权求和公式 s = sum(Ai*Wi), i = 0,,16，先对前17位数字的权求和；
 * Ai:表示第i位置上的身份证号码数字值.Wi:表示第i位置上的加权因.Wi: 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2；
 * 计算模 Y = mod(S, 11) 通过模得到对应的校验码 Y: 0 1 2 3 4 5 6 7 8 9 10 校验码: 1 0 X 9 8 7 6 5
 * 4 3 2
 */
public class IDCardUtil {
	final static Map<Integer, String> zoneNum = new HashMap<Integer, String>();
	static {
		zoneNum.put(11, "北京");
		zoneNum.put(12, "天津");
		zoneNum.put(13, "河北");
		zoneNum.put(14, "山西");
		zoneNum.put(15, "内蒙古");
		zoneNum.put(21, "辽宁");
		zoneNum.put(22, "吉林");
		zoneNum.put(23, "黑龙江");
		zoneNum.put(31, "上海");
		zoneNum.put(32, "江苏");
		zoneNum.put(33, "浙江");
		zoneNum.put(34, "安徽");
		zoneNum.put(35, "福建");
		zoneNum.put(36, "江西");
		zoneNum.put(37, "山东");
		zoneNum.put(41, "河南");
		zoneNum.put(42, "湖北");
		zoneNum.put(43, "湖南");
		zoneNum.put(44, "广东");
		zoneNum.put(45, "广西");
		zoneNum.put(46, "海南");
		zoneNum.put(50, "重庆");
		zoneNum.put(51, "四川");
		zoneNum.put(52, "贵州");
		zoneNum.put(53, "云南");
		zoneNum.put(54, "西藏");
		zoneNum.put(61, "陕西");
		zoneNum.put(62, "甘肃");
		zoneNum.put(63, "青海");
		zoneNum.put(64, "新疆");
		zoneNum.put(71, "台湾");
		zoneNum.put(81, "香港");
		zoneNum.put(82, "澳门");
		zoneNum.put(91, "外国");
	}

	final static Map<String, String> nationMap = new HashMap<String, String>();
	static {
		nationMap.put("01", "汉");
		nationMap.put("02", "蒙古");
		nationMap.put("03", "回");
		nationMap.put("04", "藏");
		nationMap.put("05", "维吾尔");
		nationMap.put("06", "苗");
		nationMap.put("07", "彝");
		nationMap.put("08", "壮");
		nationMap.put("09", "布依");
		nationMap.put("10", "朝鲜");
		nationMap.put("11", "满");
		nationMap.put("12", "侗");
		nationMap.put("13", "瑶");
		nationMap.put("14", "白");
		nationMap.put("15", "土家");
		nationMap.put("16", "哈尼");
		nationMap.put("17", "哈萨克");
		nationMap.put("18", "傣");
		nationMap.put("19", "黎");
		nationMap.put("20", "傈僳");
		nationMap.put("21", "佤");
		nationMap.put("22", "畲");
		nationMap.put("23", "高山");
		nationMap.put("24", "拉祜");
		nationMap.put("25", "水");
		nationMap.put("26", "东乡");
		nationMap.put("27", "纳西");
		nationMap.put("28", "景颇");
		nationMap.put("29", "柯尔克孜");
		nationMap.put("30", "土");
		nationMap.put("31", "达斡尔");
		nationMap.put("32", "仫佬");
		nationMap.put("33", "羌");
		nationMap.put("34", "布朗");
		nationMap.put("35", "撒拉");
		nationMap.put("36", "毛南");
		nationMap.put("37", "仡佬");
		nationMap.put("38", "锡伯");
		nationMap.put("39", "阿昌");
		nationMap.put("40", "普米");
		nationMap.put("41", "塔吉克");
		nationMap.put("42", "怒");
		nationMap.put("43", "乌孜别克");
		nationMap.put("44", "俄罗斯");
		nationMap.put("45", "鄂温克");
		nationMap.put("46", "德昂");
		nationMap.put("47", "保安");
		nationMap.put("48", "裕固");
		nationMap.put("49", "京");
		nationMap.put("50", "塔塔尔");
		nationMap.put("51", "独龙");
		nationMap.put("52", "鄂伦春");
		nationMap.put("53", "赫哲");
		nationMap.put("54", "门巴");
		nationMap.put("55", "珞巴");
		nationMap.put("56", "基诺");
	}

	final static int[] PARITYBIT = { '1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2' };
	final static int[] POWER_LIST = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };

	/**
	 * 身份证验证
	 * 
	 * @param s
	 *            号码内容
	 * @return 是否有效 null和"" 都是false
	 */
	public static boolean isIDCard(String certNo) {
		if (certNo == null || (certNo.length() != 15 && certNo.length() != 18))
			return false;
		final char[] cs = certNo.toUpperCase().toCharArray();
		// 校验位数
		int power = 0;
		for (int i = 0; i < cs.length; i++) {
			if (i == cs.length - 1 && cs[i] == 'X')
				break;// 最后一位可以 是X或x
			if (cs[i] < '0' || cs[i] > '9')
				return false;
			if (i < cs.length - 1) {
				power += (cs[i] - '0') * POWER_LIST[i];
			}
		}

		// 校验区位码
		if (!zoneNum.containsKey(Integer.valueOf(certNo.substring(0, 2)))) {
			return false;
		}

		// 校验年份
		String year = certNo.length() == 15 ? getIdcardCalendar() + certNo.substring(6, 8) : certNo.substring(6, 10);

		final int iyear = Integer.parseInt(year);
		if (iyear < 1900 || iyear > Calendar.getInstance().get(Calendar.YEAR))
			return false;// 1900年的PASS，超过今年的PASS

		// 校验月份
		String month = certNo.length() == 15 ? certNo.substring(8, 10) : certNo.substring(10, 12);
		final int imonth = Integer.parseInt(month);
		if (imonth < 1 || imonth > 12) {
			return false;
		}

		// 校验天数
		String day = certNo.length() == 15 ? certNo.substring(10, 12) : certNo.substring(12, 14);
		final int iday = Integer.parseInt(day);
		if (iday < 1 || iday > 31)
			return false;

		// 校验"校验码"
		if (certNo.length() == 15)
			return true;
		return cs[cs.length - 1] == PARITYBIT[power % 11];
	}

	private static int getIdcardCalendar() {
		GregorianCalendar curDay = new GregorianCalendar();
		int curYear = curDay.get(Calendar.YEAR);
		int year2bit = Integer.parseInt(String.valueOf(curYear).substring(2));
		return year2bit;
	}

	/**
	 * 
	 * @param nationCode
	 * @return
	 */
	public static String getIDNationCH(String nationCode) {
		return nationMap.get(nationCode);
	}

	/**
	 * 测试用例，手动创建身份证
	 * 
	 * @param fn
	 * @return
	 */
	public static IDCard createIDCard(String fn) {
		IDCard idCard = null;		
		File idcardFile = new File(fn);

		BufferedImage idCardImage = null;
		try {
			idCardImage = ImageIO.read(idcardFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (idCardImage != null) {
			idCard = new IDCard();
			idCard.setIdNo("520203197912141118");
			idCard.setPersonName("赵林");
			idCard.setAge(37);
			idCard.setIDBirth("19791214");
			idCard.setGender(1);
			idCard.setIDNation("01");
			idCard.setIDDwelling("广州市白云路28号");
			idCard.setIDEfficb("20121001");
			idCard.setIDEffice("20221001");
			idCard.setIDIssue("广州市公安局");
			byte[] idCardImageBytes = null;
			idCardImageBytes = CommUtil.getImageBytesFromImageBuffer(idCardImage);
			if (idCardImageBytes != null)
				idCard.setCardImageBytes(idCardImageBytes);
		}
		return idCard;
	}

	public static void main(String[] args) {
		boolean mark = isIDCard("520203197912141118");
		System.out.println(mark);
		System.out.println("" + getIDNationCH("03"));
	}

}
