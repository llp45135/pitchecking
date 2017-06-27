package com.rxtec.pitchecking.utils;
/**
 *  Title  新闻管理系统
 *  @version 1.0
 *  日期
 */

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.text.*;
import java.util.Calendar;
import java.util.TimeZone;

public class CalUtils {

	public CalUtils() {
	}

	/**
	 * yyyy-MM-dd HH:mm:ss
	 * 
	 * @return
	 */
	public static Date getNowDate() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		String dateString = formatter.format(currentTime);
		ParsePosition pos = new ParsePosition(8);
		Date currentTime_2 = formatter.parse(dateString, pos);
		return currentTime_2;
	}

	/**
	 * yyyy-MM-dd
	 * 
	 * @return
	 */
	public static Date getNowDateShort() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		String dateString = formatter.format(currentTime);
		ParsePosition pos = new ParsePosition(8);
		Date currentTime_2 = formatter.parse(dateString, pos);
		return currentTime_2;
	}

	/**
	 * yyyy-MM-dd HH:mm:ss
	 * 
	 * @return
	 */
	public static String getStringDate() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * yyyy-MM-dd HH:mm:ss.SSS
	 * 
	 * @return
	 */
	public static String getStringDateHaomiao() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * HHmm
	 * 
	 * @return
	 */
	public static String getStringTime() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("HHmm");
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * HHmmss
	 * 
	 * @return
	 */
	public static String getStringFullTime() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("HHmmss");
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * HHmmss.SSS
	 * 
	 * @return
	 */
	public static String getStringFullTimeHaomiao() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("HHmmss.SSS");
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * yyyy-MM-dd
	 * 
	 * @return
	 */
	public static String getStringDateShort() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * yyyyMMdd
	 * 
	 * @return
	 */
	public static String getStringDateShort2() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * yyyy-MM-dd EEEE
	 * 
	 * @return
	 */
	public static String getStringDateShort3() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd EEEE", java.util.Locale.CHINESE);
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * yyyyMMddHHmmss
	 * 
	 * @return
	 */
	public static String getStringDateLong() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		String dateString = formatter.format(currentTime);
		return dateString;

	}

	/**
	 * yyyy-MM-dd HH:mm:ss
	 * 
	 * @param strDate
	 * @return
	 */
	public static Date strToDate(String strDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
	}

	/**
	 * yyyy-MM-dd HH:mm:ss
	 * 
	 * @param dateDate
	 * @return
	 */
	public static String dateToStr(java.util.Date dateDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		String dateString = formatter.format(dateDate);
		return dateString;
	}

	public static String dateToStrShort(java.util.Date dateDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		String dateString = formatter.format(dateDate);
		return dateString;
	}

	public static Date strToBirthday(String strDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
	}

	public static Date getNow() {
		Date currentTime = new Date();
		return currentTime;
	}

	public static long getS(String strDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate.getTime();
	}

	public static Date getLastDate(long day) {
		Date date = new Date();
		long date_3_hm = date.getTime() - 3600000 * 34 * day;
		Date date_3_hm_date = new Date(date_3_hm);
		return date_3_hm_date;
	}

	/**
	 * 得到昨天的日期，格式：yyyy-mm-dd
	 * 
	 * @throws ParseException
	 * @return String
	 */
	public static String getYestodayShort() throws ParseException {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		String dateString = formatter.format(currentTime);

		Date today = formatter.parse(dateString);

		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		cal.add(Calendar.DAY_OF_YEAR, -1);
		String yestoday = String.valueOf(formatter.format(cal.getTime()));
		cal.roll(Calendar.DAY_OF_YEAR, 1);
		return yestoday;
	}

	/**
	 * 获取参数日期的前一天日期
	 * 
	 * @param dateString
	 *            String
	 * @throws ParseException
	 * @return String
	 */
	public static String getYestodayShort(String dateString) throws ParseException {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		Date today = formatter.parse(dateString);

		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		cal.add(Calendar.DAY_OF_YEAR, -1);
		String yestoday = String.valueOf(formatter.format(cal.getTime()));
		cal.roll(Calendar.DAY_OF_YEAR, 1);
		return yestoday;
	}

	/**
	 * 返回5天前的日期 yyyy-MM-dd
	 * 
	 * @param dateString
	 *            String
	 * @throws ParseException
	 * @return String
	 */
	public static String getPreSerivalDaysShort(String dateString, int k) throws ParseException {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		Date today = formatter.parse(dateString);

		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		cal.add(Calendar.DAY_OF_YEAR, -k);
		String preFiveDay = String.valueOf(formatter.format(cal.getTime()));
		cal.roll(Calendar.DAY_OF_YEAR, k);
		return preFiveDay;
	}

	/**
	 * 返回5天前的日期 yyyyMMdd
	 * 
	 * @param dateString
	 *            String
	 * @throws ParseException
	 * @return String
	 */
	public static String getPreSerivalDaysShort2(String dateString, int k) throws ParseException {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		Date today = formatter.parse(dateString);

		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		cal.add(Calendar.DAY_OF_YEAR, -k);
		String preFiveDay = String.valueOf(formatter.format(cal.getTime()));
		cal.roll(Calendar.DAY_OF_YEAR, k);
		return preFiveDay;
	}

	/**
	 * 得到明天的日期，格式：yyyy-mm-dd
	 * 
	 * @throws ParseException
	 * @return String
	 */
	public static String getTomrrowShort() throws ParseException {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		String dateString = formatter.format(currentTime);

		Date today = formatter.parse(dateString);

		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		cal.add(Calendar.DAY_OF_YEAR, 1);
		String tomrrow = String.valueOf(formatter.format(cal.getTime()));
		cal.roll(Calendar.DAY_OF_YEAR, -1);
		return tomrrow;
	}

	public static String getTomrrowShort(String dateString) throws ParseException {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		Date today = formatter.parse(dateString);

		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		cal.add(Calendar.DAY_OF_YEAR, 1);
		String tomrrow = String.valueOf(formatter.format(cal.getTime()));
		cal.roll(Calendar.DAY_OF_YEAR, -1);
		return tomrrow;
	}

	/**
	 * 判断时间date1是否在时间date2之前 时间格式 2005-4-21 16:16:34
	 * 
	 * @param date1
	 *            String
	 * @param date2
	 *            String
	 * @return boolean
	 */
	public static boolean isDateBefore(String date1, String date2) {
		try {
			DateFormat df = DateFormat.getDateTimeInstance();
			return df.parse(date1).before(df.parse(date2));
		} catch (ParseException e) {
			System.out.print("[SYS] " + e.getMessage());
			return false;
		}
	}

	/**
	 * 判断当前时间是否在时间date2之前 时间格式 2005-4-21 16:16:34
	 * 
	 * @param date2
	 *            String
	 * @return boolean
	 */
	public static boolean isDateBefore(String date2) {
		try {
			Date date1 = new Date();
			DateFormat df = DateFormat.getDateTimeInstance();
			return date1.before(df.parse(date2));
		} catch (ParseException e) {
			System.out.print("[SYS] " + e.getMessage());
			return false;
		}
	}

	/**
	 * 判断当前时间是否在时间date2之后 时间格式 2005-4-21 16:16:34
	 * 
	 * @param date2
	 *            String
	 * @return boolean
	 */
	public static boolean isDateAfter(String date2) {
		try {
			Date date1 = new Date();
			DateFormat df = DateFormat.getDateTimeInstance();
			return date1.after(df.parse(date2));
		} catch (ParseException e) {
			System.out.print("[SYS] " + e.getMessage());
			return false;
		}
	}

	public static Date getSqlDate(java.util.Date date) {
		return new Date(date.getTime());
	}

	/**
	 * * 获得某一日期的前一天 * @param date * @return Date
	 */
	public static Date getPreviousDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int day = calendar.get(Calendar.DATE);
		calendar.set(Calendar.DATE, day - 1);
		return getSqlDate(calendar.getTime());
	}

	/**
	 * * 获得某年某月第一天的日期 * @param year * @param month * @return Date
	 */
	public static Date getFirstDayOfMonth(int year, int month) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month - 1);
		calendar.set(Calendar.DATE, 1);
		return getSqlDate(calendar.getTime());
	}

	/**
	 * * 获得某年某月最后一天的日期 * @param year * @param month * @return Date
	 */
	public static Date getLastDayOfMonth(int year, int month) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DATE, 1);
		return getPreviousDate(getSqlDate(calendar.getTime()));
	}

	/**
	 * 得到上个月的今天日期
	 * 
	 * @return
	 * @throws ParseException
	 */
	public static String getLastMonthDay() throws ParseException {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		String dateString = formatter.format(currentTime);

		Date today = formatter.parse(dateString);

		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		cal.add(Calendar.MONTH, -1);
		String last_month_day = String.valueOf(formatter.format(cal.getTime()));
		cal.roll(Calendar.MONTH, 1);
		return last_month_day;
	}

	/**
	 * 得到下个月的今天日期
	 * 
	 * @return
	 * @throws ParseException
	 */
	public static String getNextMonthDay() throws ParseException {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		String dateString = formatter.format(currentTime);

		Date today = formatter.parse(dateString);

		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		cal.add(Calendar.MONTH, 1);
		String next_month_day = String.valueOf(formatter.format(cal.getTime()));
		cal.roll(Calendar.MONTH, -1);
		return next_month_day;
	}

	public static int getNowMonthDays(String nowMonth) {
		Calendar rightNow = Calendar.getInstance();
		SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM"); // 如果写成年月日的形式的话，要写小d，如："yyyy/MM/dd"
		try {
			rightNow.setTime(simpleDate.parse(nowMonth)); // 要计算你想要的月份，改变这里即可
		} catch (ParseException e) {
			e.printStackTrace();
		}
		int days = rightNow.getActualMaximum(Calendar.DAY_OF_MONTH);
		return days;
	}

	public static int getNowYearDays(String nowYear) {
		Calendar rightNow = Calendar.getInstance();
		SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy"); // 如果写成年月日的形式的话，要写小d，如："yyyy/MM/dd"
		try {
			rightNow.setTime(simpleDate.parse(nowYear)); // 要计算你想要的月份，改变这里即可
		} catch (ParseException e) {
			e.printStackTrace();
		}
		int days = rightNow.getActualMaximum(Calendar.DAY_OF_YEAR);
		return days;
	}

	/**
	 * 时间之间的天数 yyyyMMdd
	 * 
	 * @param dateBegin
	 * @param dateEnd
	 * @return
	 */
	public static int getCalcDateShort(String dateBegin, String dateEnd) {
		int distance = 0; // 时间之间的天数
		String db = dateBegin; // 开始日期
		String de = dateEnd; // 结束日期

		int strby = 0; // substring of begin date year 开始日期的年份
		int strbm = 0; // 开始日期的月份
		int strbd = 0; // 开始日期的日子
		int strey = 0;
		int strem = 0;
		int stred = 0;
		// 类型转换
		strby = Integer.parseInt(db.substring(0, 4));
		strbm = Integer.parseInt(db.substring(4, 6));
		strbd = Integer.parseInt(db.substring(6, 8));
		strey = Integer.parseInt(de.substring(0, 4));
		strem = Integer.parseInt(de.substring(4, 6));
		stred = Integer.parseInt(de.substring(6, 8));
		if (stred < strbd) {
			stred = stred + 30;
			strem = strem - 1;
		}

		if (strem < strbm) {
			strem = strem + 12;
			strey = strey - 1;
		}
		distance = (strey - strby) * 365 + (strem - strbm) * 30 + stred - strbd;
		return distance;
	}

	/**
	 * 时间之间的天数 yyyy-MM-dd
	 * 
	 * @param dateBegin
	 * @param dateEnd
	 * @return
	 */
	public static int getCalcDate(String dateBegin, String dateEnd) {
		int distance = 0; // 时间之间的天数
		String db = dateBegin; // 开始日期
		String de = dateEnd; // 结束日期

		int strby = 0; // substring of begin date year 开始日期的年份
		int strbm = 0; // 开始日期的月份
		int strbd = 0; // 开始日期的日子
		int strey = 0;
		int strem = 0;
		int stred = 0;
		// 类型转换
		strby = Integer.parseInt(db.substring(0, 4));
		strbm = Integer.parseInt(db.substring(5, 7));
		strbd = Integer.parseInt(db.substring(8, 10));
		strey = Integer.parseInt(de.substring(0, 4));
		strem = Integer.parseInt(de.substring(5, 7));
		stred = Integer.parseInt(de.substring(8, 10));
		if (stred < strbd) {
			stred = stred + 30;
			strem = strem - 1;
		}

		if (strem < strbm) {
			strem = strem + 12;
			strey = strey - 1;
		}
		distance = (strey - strby) * 365 + (strem - strbm) * 30 + stred - strbd;
		return distance;
	}

	/**
	 * 计算年龄，时间之间的年数 yyyy-MM-dd
	 * 
	 * @param dateBegin
	 * @param dateEnd
	 * @return
	 */
	public static int getAge(String dateBegin, String dateEnd) {
		int distance = 0; // 时间之间的天数
		String db = dateBegin; // 开始日期
		String de = dateEnd; // 结束日期

		int strby = 0; // substring of begin date year 开始日期的年份
		int strbm = 0; // 开始日期的月份
		int strbd = 0; // 开始日期的日子
		int strey = 0;
		int strem = 0;
		int stred = 0;
		// 类型转换
		strby = Integer.parseInt(db.substring(0, 4));
		strbm = Integer.parseInt(db.substring(5, 7));
		strbd = Integer.parseInt(db.substring(8, 10));
		strey = Integer.parseInt(de.substring(0, 4));
		strem = Integer.parseInt(de.substring(5, 7));
		stred = Integer.parseInt(de.substring(8, 10));
		if (stred < strbd) {
			stred = stred + 30;
			strem = strem - 1;
		}

		if (strem < strbm) {
			strem = strem + 12;
			strey = strey - 1;
		}
		distance = (strey - strby) * 365 + (strem - strbm) * 30 + stred - strbd;
		int age = distance / 365;
		return age;
	}

	/**
	 * 计算时间差 (时间单位,开始时间,结束时间) 调用方法 howLong("h","2007-08-09 10:22:26",
	 * "2007-08-09 20:21:30") ///9小时56分 返回9小时
	 */
	public static long howLong(String unit, String time1, String time2) throws ParseException {
		// 时间单位(如：不足1天(24小时) 则返回0)，开始时间，结束时间
		Date date1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(time1);
		Date date2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(time2);
		long ltime = date1.getTime() - date2.getTime() < 0 ? date2.getTime() - date1.getTime() : date1.getTime() - date2.getTime();
		if (unit.equals("ms")) {
			return ltime;
		} else if (unit.equals("s")) {
			return ltime / 1000;// 返回秒
		} else if (unit.equals("m")) {
			return ltime / 60000;// 返回分钟
		} else if (unit.equals("h")) {
			return ltime / 3600000;// 返回小时
		} else if (unit.equals("d")) {
			return ltime / 86400000;// 返回天数
		} else {
			return 0;
		}
	}

	/**
	 * @param args
	 * @throws ParseException
	 */
	public static void main(String[] args) throws ParseException {
		System.out.println("nowMonthDays==" + getNowMonthDays("2008-02"));
		System.out.println("nowYearDays==" + getNowYearDays("2009"));
		System.out.println("getAge==" + getCalcDate("2016-07-19", "2016-12-07"));
		System.out.println("getPreSerivalDaysShort==" + getPreSerivalDaysShort2(getStringDateShort2(), 90));
		System.out.println("hhmm==" + getStringFullTimeHaomiao());
		System.out.println("howlong:" + howLong("m", "2017-06-16 13:56:09.100", "2017-06-16 12:24:09.300"));
		System.out.println("getCalcDateShort==" + getCalcDateShort("20170624", "20170623"));

	}

}
