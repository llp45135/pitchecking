package com.rxtec.pitchecking.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * 通用工具类
 * 
 * @author ZhaoLin
 *
 */
public class CommUtil {
	private static Logger log = LoggerFactory.getLogger("CommUtil");

	public static void main(String[] args) {
		// String srcfile = "D:/eclipse/workspace/IDCard.bmp";
		// String dstFile = "D:/eclipse/workspace/idcardtest.jpg";
		// bmpTojpg(srcfile, dstFile);

		String aa = "20119850326广东省茂名市电白县水东镇人民街175号之一时代名苑商住小区A幢1505440923198503264625电白县公安局";
		String ss = "20119870101湖北省黄石市下陆区团城山街道杭州西路71号怡安花园3栋1单元2702室35042019630511002X黄石市公安局下陆分局";
		String dd = "10119870408广州市海珠区南永安直街4号104房";
		String cc = "35042019630511002X永安市公安局";
		// String reg = "^.*\\d{18}.*$";
		// if (ss.matches(reg)) {
		// // TODO
		// System.out.println(ss);
		// }
		System.out.println("idcardNo==" + CommUtil.getIdCardNoFromInfo(aa));
		try {
			System.out.println("" + CommUtil.round(2, (float) 0.685325));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(hexstringToInt("f007"));
		
		try {
			System.out.println("intToBytes: 0 = "+CommUtil.bytesToHexString(intToBytes(6837)));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 判断字符串什么编码类型
	 * 
	 * @param str
	 * @return
	 */
	public static String getEncoding(String str) {
		String encode = "GB2312";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s = encode;
				return s;
			}
		} catch (Exception exception) {
		}
		encode = "ISO-8859-1";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s1 = encode;
				return s1;
			}
		} catch (Exception exception1) {
		}
		encode = "UTF-8";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s2 = encode;
				return s2;
			}
		} catch (Exception exception2) {
		}
		encode = "GBK";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s3 = encode;
				return s3;
			}
		} catch (Exception exception3) {
		}
		return "";
	}

	public static String getStringDateShort() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	public static String getStringDateShort2() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		TimeZone timeZoneChina = TimeZone.getTimeZone("Asia/Shanghai"); // 获取时区
		formatter.setTimeZone(timeZoneChina); // 设置系统时区
		String dateString = formatter.format(currentTime);
		return dateString;
	}

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
	 * 
	 */
	public static float round(int scale, float pIn) throws Exception {
		float retval = 0f;
		BigDecimal b = new BigDecimal(pIn);
		retval = b.setScale(scale, BigDecimal.ROUND_HALF_UP).floatValue();
		return retval;
	}

	/**
	 * 以行为单位读取文件，常用于读面向行的格式化文件
	 * 
	 * @param fileName
	 *            文件名
	 */
	public static void readFileByLines(String fileName) {
		File file = new File(fileName);
		BufferedReader reader = null;
		try {
			System.out.println("以行为单位读取文件内容，一次读一整行：");
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 1;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				// 显示行号
				System.out.println("line " + line + ": " + tempString);
				line++;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
	}
	
	
	/**
	 * 将字符串写成文件
	 * 
	 * @param fileName
	 * @param fileContent
	 * @return
	 */
	public static boolean writeFileContent(String fileName, String fileContent) {
		try {
			File logFile = new File(fileName);
			if (!logFile.exists()) {
				logFile.createNewFile();
			}
			FileWriter fw = new FileWriter(logFile);
			// log.debug("pidStr==" + pidStr);
			fw.write(fileContent);
			fw.flush();
			fw.close();
			return true;

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
	}

	/**
	 * 判断字符串中是否含有身份证号的前面17个连续数字
	 * 
	 * @param str
	 * @return
	 */
	public static String getIdCardNoFromInfo(String str) {
		String result = "";
		try {
			Pattern p = Pattern.compile("\\d{17}");
			Matcher m = p.matcher(str);
			if (m.find()) {
				result = m.group();
				int i = str.indexOf(result);
				// System.out.println("i=="+i);
				for (int k = i; k <= (i + 18); k++) {
					result = str.substring(k, k + 18);
					// System.out.println("数字起始位:" + k + " result==" + result);
					if (IDCardUtil.isIDCard(result)) {
						break;
					} else
						continue;
				}
			}
		} catch (Exception ex) {
			result = "";
			log.error("getIdCardNoFromInfo:", ex);
		}
		return result;

	}

	@SuppressWarnings("restriction")
	public static void bmpTojpg(String file, String dstFile) {
		try {
			FileInputStream in = new FileInputStream(file);
			Image TheImage = read(in);
			int wideth = TheImage.getWidth(null);
			int height = TheImage.getHeight(null);
			BufferedImage tag = new BufferedImage(wideth, height, BufferedImage.TYPE_INT_RGB);
			tag.getGraphics().drawImage(TheImage, 0, 0, wideth, height, null);
			FileOutputStream out = new FileOutputStream(dstFile);
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			encoder.encode(tag);
			in.close();
			out.close();
		} catch (Exception e) {
			log.error("bmpTojpg:", e);
		}
	}

	public static int constructInt(byte[] in, int offset) {
		int ret = ((int) in[offset + 3] & 0xff);
		ret = (ret << 8) | ((int) in[offset + 2] & 0xff);
		ret = (ret << 8) | ((int) in[offset + 1] & 0xff);
		ret = (ret << 8) | ((int) in[offset + 0] & 0xff);
		return (ret);
	}

	public static int constructInt3(byte[] in, int offset) {
		int ret = 0xff;
		ret = (ret << 8) | ((int) in[offset + 2] & 0xff);
		ret = (ret << 8) | ((int) in[offset + 1] & 0xff);
		ret = (ret << 8) | ((int) in[offset + 0] & 0xff);
		return (ret);
	}

	public static long constructLong(byte[] in, int offset) {
		long ret = ((long) in[offset + 7] & 0xff);
		ret |= (ret << 8) | ((long) in[offset + 6] & 0xff);
		ret |= (ret << 8) | ((long) in[offset + 5] & 0xff);
		ret |= (ret << 8) | ((long) in[offset + 4] & 0xff);
		ret |= (ret << 8) | ((long) in[offset + 3] & 0xff);
		ret |= (ret << 8) | ((long) in[offset + 2] & 0xff);
		ret |= (ret << 8) | ((long) in[offset + 1] & 0xff);
		ret |= (ret << 8) | ((long) in[offset + 0] & 0xff);
		return (ret);
	}

	public static double constructDouble(byte[] in, int offset) {
		long ret = constructLong(in, offset);
		return (Double.longBitsToDouble(ret));
	}

	public static short constructShort(byte[] in, int offset) {
		short ret = (short) ((short) in[offset + 1] & 0xff);
		ret = (short) ((ret << 8) | (short) ((short) in[offset + 0] & 0xff));
		return (ret);
	}

	static class BitmapHeader {
		public int iSize, ibiSize, iWidth, iHeight, iPlanes, iBitcount, iCompression, iSizeimage, iXpm, iYpm, iClrused, iClrimp;

		// 读取bmp文件头信息
		public void read(FileInputStream fs) throws IOException {
			final int bflen = 14;
			byte bf[] = new byte[bflen];
			fs.read(bf, 0, bflen);
			final int bilen = 40;
			byte bi[] = new byte[bilen];
			fs.read(bi, 0, bilen);
			iSize = constructInt(bf, 2);
			ibiSize = constructInt(bi, 2);
			iWidth = constructInt(bi, 4);
			iHeight = constructInt(bi, 8);
			iPlanes = constructShort(bi, 12);
			iBitcount = constructShort(bi, 14);
			iCompression = constructInt(bi, 16);
			iSizeimage = constructInt(bi, 20);
			iXpm = constructInt(bi, 24);
			iYpm = constructInt(bi, 28);
			iClrused = constructInt(bi, 32);
			iClrimp = constructInt(bi, 36);
		}
	}

	public static Image read(FileInputStream fs) {
		try {
			BitmapHeader bh = new BitmapHeader();
			bh.read(fs);
			if (bh.iBitcount == 24) {
				return (readImage24(fs, bh));
			}
			if (bh.iBitcount == 32) {
				return (readImage32(fs, bh));
			}
			fs.close();
		} catch (IOException e) {
			log.error("bmpTojpg:", e);
		} catch (Exception e) {
			log.error("bmpTojpg:", e);
		}
		return (null);
	}

	// 24位
	protected static Image readImage24(FileInputStream fs, BitmapHeader bh) throws IOException {
		Image image;
		if (bh.iSizeimage == 0) {
			bh.iSizeimage = ((((bh.iWidth * bh.iBitcount) + 31) & ~31) >> 3);
			bh.iSizeimage *= bh.iHeight;
		}
		int npad = (bh.iSizeimage / bh.iHeight) - bh.iWidth * 3;
		int ndata[] = new int[bh.iHeight * bh.iWidth];
		byte brgb[] = new byte[(bh.iWidth + npad) * 3 * bh.iHeight];
		fs.read(brgb, 0, (bh.iWidth + npad) * 3 * bh.iHeight);
		int nindex = 0;
		for (int j = 0; j < bh.iHeight; j++) {
			for (int i = 0; i < bh.iWidth; i++) {
				ndata[bh.iWidth * (bh.iHeight - j - 1) + i] = constructInt3(brgb, nindex);
				nindex += 3;
			}
			nindex += npad;
		}
		image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(bh.iWidth, bh.iHeight, ndata, 0, bh.iWidth));
		fs.close();
		return (image);
	}

	// 32位
	protected static Image readImage32(FileInputStream fs, BitmapHeader bh) throws IOException {
		Image image;
		int ndata[] = new int[bh.iHeight * bh.iWidth];
		byte brgb[] = new byte[bh.iWidth * 4 * bh.iHeight];
		fs.read(brgb, 0, bh.iWidth * 4 * bh.iHeight);
		int nindex = 0;
		for (int j = 0; j < bh.iHeight; j++) {
			for (int i = 0; i < bh.iWidth; i++) {
				ndata[bh.iWidth * (bh.iHeight - j - 1) + i] = constructInt3(brgb, nindex);
				nindex += 4;
			}
		}
		image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(bh.iWidth, bh.iHeight, ndata, 0, bh.iWidth));
		fs.close();
		return (image);
	}

	/**
	 * 获得指定文件的byte数组
	 */
	public static byte[] getBytes(String fileName) {
		byte[] buffer = null;
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(fileName));
			int length = bis.available();
			buffer = new byte[length];
			ByteArrayOutputStream byteaos = new ByteArrayOutputStream(length);
			int bytes;
			while ((bytes = bis.read(buffer)) != -1) {
				byteaos.write(buffer, 0, bytes);
			}
			buffer = byteaos.toByteArray();
			bis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer;
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
		value = (int) ((src[offset] & 0xFF) | ((src[offset + 1] & 0xFF) << 8) | ((src[offset + 2] & 0xFF) << 16) | ((src[offset + 3] & 0xFF) << 24));
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

	public static void wirtedisk(byte[] array, int size, String filename) {
		if (array != null) {
			try {
				java.io.OutputStream out = new FileOutputStream(new File(filename));
				out.write(array, 0, size);
				out.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static boolean bytesToBoolean(byte src) {

		if (src == 0)
			return false;
		else
			return true;
	}

	/**
	 * 递归删除目录
	 * 
	 * @param dir
	 * @return
	 */
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// 目录此时为空，可以删除
		return dir.delete();
	}

	/**
	 * 创建目录
	 * 
	 * @param destDirName
	 * @return
	 */
	public static int createDir(String destDirName) {
		File dir = new File(destDirName);
		if (dir.exists()) {
			// System.out.println("创建目录" + destDirName + "失败，目标目录已经存在");
			return 0;
		}
		if (!destDirName.endsWith(File.separator)) {
			destDirName = destDirName + File.separator;
		}
		// 创建目录
		if (dir.mkdirs()) {
			// System.out.println("创建目录" + destDirName + "成功！");
			return 1;
		} else {
			// System.out.println("创建目录" + destDirName + "失败！");
			return -1;
		}
	}

	/**
	 * Sleep for timeout msecs. Returns when timeout has elapsed or thread was
	 * interrupted
	 */
	public static void sleep(long timeout) {
		try {
			Thread.sleep(timeout);
		} catch (Throwable e) {
		}
	}

	public static void sleep(long timeout, int nanos) {
		try {
			Thread.sleep(timeout, nanos);
		} catch (Throwable e) {
		}
	}

	/**
	 * On most UNIX systems, the minimum sleep time is 10-20ms. Even if we
	 * specify sleep(1), the thread will sleep for at least 10-20ms. On Windows,
	 * sleep() seems to be implemented as a busy sleep, that is the thread never
	 * relinquishes control and therefore the sleep(x) is exactly x ms long.
	 */
	public static void sleep(long msecs, boolean busy_sleep) {
		if (!busy_sleep) {
			sleep(msecs);
			return;
		}

		long start = System.currentTimeMillis();
		long stop = start + msecs;

		while (stop > start) {
			start = System.currentTimeMillis();
		}
	}

	/** Returns a random value in the range [1 - range] */
	public static long random(long range) {
		return (long) ((Math.random() * 100000) % range) + 1;
	}

	/**
	 * 将指定字符串src，以每两个字符分割转换为16进制形式 如："2B44EFD9" --> byte[]{0x2B, 0x44, 0xEF,
	 * 0xD9}
	 * 
	 * @param src
	 *            String
	 * @return byte[]
	 */
	public static byte[] hexStringToBytes(String src) {
		byte[] tmp = src.getBytes();
		int len = tmp.length / 2;
		byte[] ret = new byte[len];

		for (int i = 0; i < len; i++) {
			ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
		}
		return ret;
	}

	/**
	 * 将两个ASCII字符合成一个字节； 如："EF"--> 0xEF
	 * 
	 * @param src0
	 *            byte
	 * @param src1
	 *            byte
	 * @return byte
	 */
	public static byte uniteBytes(byte src0, byte src1) {
		byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 })).byteValue();
		_b0 = (byte) (_b0 << 4);
		byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 })).byteValue();
		byte ret = (byte) (_b0 ^ _b1);
		return ret;
	}

	/**
	 * 生成命令尾 出了0x10,其他各字节值相加
	 */
	public static String tail(String xh, String cc1, String cc2) {
		String retStr = "";
		int i = Integer.parseInt(xh, 16);
		int j = Integer.parseInt(cc1, 16);
		int k = Integer.parseInt(cc2, 16);
		int result = i + j + k + 5;
		retStr = Integer.toHexString(result).toString().toUpperCase();
		if (retStr.length() == 1) {
			retStr = "0" + retStr;
		}
		return retStr;
	}

	/**
	 * 生成18位随机数
	 * 
	 * @return
	 */
	public static String getRandomUUID() {
		// 1、创建时间戳
		java.util.Date dateNow = new java.util.Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String dateNowStr = dateFormat.format(dateNow);
		StringBuffer sb = new StringBuffer(dateNowStr);

		// 2、创建随机对象
		Random rd = new Random();

		// 3、产生4位随机数
		String n = "";
		int rdGet; // 取得随机数

		do {
			rdGet = Math.abs(rd.nextInt()) % 10 + 48; // 产生48到57的随机数(0-9的键位值)
			// rdGet=Math.abs(rd.nextInt())%26+97; //产生97到122的随机数(a-z的键位值)
			char num1 = (char) rdGet;
			String dd = Character.toString(num1);
			n += dd;
		} while (n.length() < 4);// 假如长度小于4
		sb.append(n);

		// 4、返回唯一码
		return sb.toString();
	}

	/**
	 * 图片到byte数组
	 * 
	 * @param path
	 * @return
	 */
	public static byte[] image2byte(String path) {
		byte[] data = null;
		FileImageInputStream input = null;
		ByteArrayOutputStream output = null;
		try {
			input = new FileImageInputStream(new File(path));
			output = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int numBytesRead = 0;
			while ((numBytesRead = input.read(buf)) != -1) {
				output.write(buf, 0, numBytesRead);
			}
			data = output.toByteArray();
			if (output != null) {
				output.close();
				output = null;
			}
			if (input != null) {
				input.close();
				input = null;
			}
		} catch (FileNotFoundException ex1) {
			ex1.printStackTrace();
		} catch (IOException ex1) {
			ex1.printStackTrace();
		} finally {
			try {
				if (output != null) {
					output.close();
					output = null;
				}
				if (input != null) {
					input.close();
					input = null;
				}
			} catch (IOException ex1) {
				ex1.printStackTrace();
			}
		}
		return data;
	}

	/**
	 * byte数组到图片
	 * 
	 * @param data
	 * @param path
	 */
	public static void byte2image(byte[] data, String path) {
		if (data.length < 3 || path.equals(""))
			return;
		FileImageOutputStream imageOutput = null;
		try {
			imageOutput = new FileImageOutputStream(new File(path));
			imageOutput.write(data, 0, data.length);
			if (imageOutput != null) {
				imageOutput.close();
				imageOutput = null;
			}
			log.debug("Make Picture success,Please find image in " + path);
		} catch (Exception ex) {
			log.error("Exception: " + ex);
			ex.printStackTrace();
		} finally {
			try {
				if (imageOutput != null) {
					imageOutput.close();
					imageOutput = null;
				}
			} catch (Exception ex) {
				log.error("Exception: " + ex);
				ex.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param f
	 * @return
	 */
	public static byte[] getBytesFromFile(File f) {
		if (f == null) {
			return null;
		}
		try {
			FileInputStream stream = new FileInputStream(f);
			ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
			byte[] b = new byte[1000];
			int n;
			while ((n = stream.read(b)) != -1)
				out.write(b, 0, n);
			stream.close();
			out.close();
			return out.toByteArray();
		} catch (Exception e) {
			log.error("CommUtil getBytesFromFile:", e);
		}
		return null;
	}

	/**
	 * 
	 * @param cardImage
	 * @return
	 */
	public static byte[] getImageBytesFromImageBuffer(BufferedImage cardImage) {
		ByteArrayOutputStream output = null;
		byte[] buff = null;
		try {
			output = new ByteArrayOutputStream();
			ImageIO.write(cardImage, "JPEG", ImageIO.createImageOutputStream(output));
			buff = output.toByteArray();
			if (output != null) {
				output.close();
				output = null;
			}
		} catch (Exception e) {
			log.error("CommUtil ImageIO.write error!", e);
		} finally {
			try {
				if (output != null) {
					output.close();
					output = null;
				}
			} catch (Exception e) {
				log.error("CommUtil ImageIO.write error!", e);
			}
		}

		return buff;
	}

	/**
	 * 
	 * @param b
	 * @param outputFile
	 * @return
	 */
	public static File getFileFromBytes(byte[] b, String outputFile) {
		BufferedOutputStream stream = null;
		File file = null;
		try {
			file = new File(outputFile);
			FileOutputStream fstream = new FileOutputStream(file);
			stream = new BufferedOutputStream(fstream);
			stream.write(b);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		return file;
	}

	/**
	 * 将int数值转换为占四个字节的byte数组，本方法适用于(低位在前，高位在后)的顺序。 和bytesToInt（）配套使用
	 * 
	 * @param value
	 *            要转换的int值
	 * @return byte数组
	 */
	public static byte[] intToBytes(int value) {
		byte[] src = new byte[4];
		src[3] = (byte) ((value >> 24) & 0xFF);
		src[2] = (byte) ((value >> 16) & 0xFF);
		src[1] = (byte) ((value >> 8) & 0xFF);
		src[0] = (byte) (value & 0xFF);
		return src;
	}

	/**
	 * 
	 * @param o
	 * @return
	 */
	public static byte[] serialObjToBytes(Object o) {
		byte[] buf = null;
		ByteArrayOutputStream bos = null;
		ObjectOutputStream oos = null;
		try {
			bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(o);
			buf = bos.toByteArray();
			if (oos != null) {
				oos.close();
				oos = null;
			}
			if (bos != null) {
				bos.close();
				bos = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("serialObjToBytes", e);
		} finally {
			try {
				if (oos != null) {
					oos.close();
					oos = null;
				}
				if (bos != null) {
					bos.close();
					bos = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.error("serialObjToBytes", e);
			}
		}

		return buf;
	}

	/*
	 * Convert byte[] to hex
	 * string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
	 * 
	 * @param src byte[] data
	 * 
	 * @return hex string
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	/**
	 * 
	 */
	public static int hexstringToInt(String hexstr) {
		int value = 0;
		if (hexstr == null || hexstr.length() <= 0) {
			return 0;
		}

		String ss = "";
		for (int i = 0; i < hexstr.length(); i = i + 2) {
			ss = hexstr.substring(i, i + 2) + ss;
		}
		// System.out.println("ss = " + ss);
		value = Integer.parseInt(ss, 16);
		return value;
	}
}
