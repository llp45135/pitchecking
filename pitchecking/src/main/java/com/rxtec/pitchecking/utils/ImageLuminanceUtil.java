package com.rxtec.pitchecking.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.SafeArray;
import com.jacob.com.Variant;

public class ImageLuminanceUtil {
	private static ImageLuminanceUtil _instance;
	private Dispatch test = null;

	public static synchronized ImageLuminanceUtil getInstance() {
		if (_instance == null) {
			_instance = new ImageLuminanceUtil();
		}
		return _instance;
	}

	private ImageLuminanceUtil() {
		test = new ActiveXComponent("ImageStatisticsDLL.ImageStatistics");
	}

	/**
	 * 调用C#动态库，获取图像的光亮度
	 * 
	 * @param image
	 * @return
	 */
	public Variant getLuminanceResult(BufferedImage image) {
		// Dispatch test = new
		// ActiveXComponent("ImageStatisticsDLL.ImageStatistics");
		Variant result = null;
		try {
			// FileInputStream fin = new FileInputStream(fileName);
			// byte[] buf = new byte[fin.available()];
			// fin.read(buf);

			// BufferedImage image = ImageIO.read(new File(fileName));
			byte[] buf = CommUtil.getImageBytesFromImageBuffer(image); // getImageBytes(image,
																		// "jpg");

			SafeArray sArray = new SafeArray(Variant.VariantByte, buf.length);
			sArray.fromByteArray(buf);
			Variant vt = new Variant();
			vt.putSafeArrayRef(sArray);
			result = Dispatch.call(test, "Luminance", vt);
			// System.out.println("Luminance Result = " + result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 
	 * @param img
	 * @param type
	 * @return
	 */
	public static byte[] getImageBytes(BufferedImage img, String type) {

		byte[] buff = null;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			ImageIO.write(img, type, ImageIO.createImageOutputStream(output));
			buff = output.toByteArray();
		} catch (Exception e) {
			buff = null;
			e.printStackTrace();
		}
		return buff;
	}

	@SuppressWarnings("static-access")
	public static void main(String[] args) {

		String fileName = "D:/pitchecking/image.jpg";
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File(fileName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		float  luminanceResult = ImageLuminanceUtil.getInstance().getLuminanceResult(image).getFloat();

		System.out.println(CalUtils.getStringDateHaomiao() + " Luminance Result = " + luminanceResult);
		System.out.println(CalUtils.getStringDateHaomiao() + " Luminance Result = " + luminanceResult);

	}

}
