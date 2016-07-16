package com.rxtec.pitchecking.picheckingservice;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.utils.CommUtil;

public class FaceVerifyJniEntryTest implements Runnable {

	FaceVerifyJniEntry jni = new FaceVerifyJniEntry(Config.FaceVerifyDLLName);
	
//	FaceVerifyMicroPJNIEntry jni = new FaceVerifyMicroPJNIEntry("C:/maven/git/pitchecking/micropattern/MPALLibFaceRecFInf.dll");
	
	byte[] img1, img2;
	DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();

	public FaceVerifyJniEntryTest() {
		IDCard c1 = createIDCard("C:/pitchecking/B1.jpg");
		IDCard c2 = createIDCard("C:/pitchecking/B2.jpg");

		img1 = c1.getImageBytes();
		img2 = c2.getImageBytes();

		df.setMaximumFractionDigits(2);
	}

	@Override
	public void run() {
		while (true) {
			CommUtil.sleep(500);
			long nowMils = Calendar.getInstance().getTimeInMillis();

			float result = jni.verify(img1, img2);

			long usingTime = Calendar.getInstance().getTimeInMillis() - nowMils;
			System.out.println("Using " + usingTime + " ms, value=" + df.format(result));
		}

	}

	public static void main(String[] args) {

		ExecutorService executer = Executors.newCachedThreadPool();
		FaceVerifyJniEntryTest task1 = new FaceVerifyJniEntryTest();
		executer.execute(task1);
		if (Config.getInstance().getFaceVerifyThreads() == 2) {
			FaceVerifyJniEntryTest task2 = new FaceVerifyJniEntryTest();
			executer.execute(task2);
		}
		executer.shutdown();

	}

	private static IDCard createIDCard(String fn) {
		IDCard card = new IDCard();
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(new File(fn));
		} catch (Exception e) {
			e.printStackTrace();
		}

		card.setCardImage(bi);
		return card;
	}
}
