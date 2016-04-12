package com.rxtec.pitchecking.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * 通用工具
 * @author ZhaoLin
 *
 */
public class CommUtil {
	/**
	 * 获得指定文件的byte数组
	 */
	public static byte[] getBytes(String  fileName) {
		byte[] buffer = null;
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(fileName));
			int length = bis.available();
			buffer = new byte[length];
			ByteArrayOutputStream byteaos = new ByteArrayOutputStream(length);
			int bytes;
			while((bytes=bis.read(buffer))!=-1){
					byteaos.write(buffer, 0, bytes);
				}
				buffer=byteaos.toByteArray();
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
		value = (int) ((src[offset] & 0xFF) 
				| ((src[offset+1] & 0xFF)<<8) 
				| ((src[offset+2] & 0xFF)<<16) 
				| ((src[offset+3] & 0xFF)<<24));
		return value;
	}
	
	public static float bytesToFloat(byte[] src, int offset) {
		return Float.intBitsToFloat(bytesToInt(src, offset));
	}
	
	/**
	 * public static Float bytesToFloat(byte[] src, int offset) {
		return Float.intBitsToFloat(bytesToInt(src, offset));
	}
	 * @param b
	 * @param offset
	 * @return
	 */
	
	public static double bytesToDouble(byte[] b, int offset) {
		long l; 
	     l = b[offset+0]; 
	     l &= 0xff; 
	     l |= ((long) b[offset+1] << 8); 
	     l &= 0xffff; 
	     l |= ((long) b[offset+2] << 16); 
	     l &= 0xffffff; 
	     l |= ((long) b[offset+3] << 24); 
	     l &= 0xffffffffl; 
	     l |= ((long) b[offset+4] << 32); 
	     l &= 0xffffffffffl; 
	     l |= ((long) b[offset+5] << 40); 
	     l &= 0xffffffffffffl; 
	     l |= ((long) b[offset+6] << 48); 
	     l &= 0xffffffffffffffl; 
	     l |= ((long) b[offset+7] << 56); 
	     return Double.longBitsToDouble(l); 
	}
	
	public static void wirtedisk(byte[] array, int size, String filename){
		if(array!=null){
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
}
