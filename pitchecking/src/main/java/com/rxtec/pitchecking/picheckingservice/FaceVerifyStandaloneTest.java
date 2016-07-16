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
import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.utils.CommUtil;

public class FaceVerifyStandaloneTest implements Runnable {

	byte[] img1, img2;
	DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();

	public FaceVerifyStandaloneTest() {
		IDCard c1 = createIDCard("C:/pitchecking/B1.jpg");
		IDCard c2 = createIDCard("C:/pitchecking/B2.jpg");

		img1 = c1.getImageBytes();
		img2 = c2.getImageBytes();

		df.setMaximumFractionDigits(2);
		
		FaceCheckingService.getInstance().beginFaceCheckerTask();
	}

	@Override
	public void run() {
		while (true) {
			CommUtil.sleep(500);
			long nowMils = Calendar.getInstance().getTimeInMillis();

			PITVerifyData faceData = new PITVerifyData();
			faceData.setFrameImg(img1);
			faceData.setFaceImg(img1);
			faceData.setIdCardImg(img2);
			faceData.setIdNo("123456");
			faceData.setPersonName("李立平");
			faceData.setAge(12);
			faceData.setGender(1);
			Ticket t = new Ticket();
			t.setTrainCode("D001");
			t.setTrainDate("20160701");
			t.setFromStationCode("GGQ");
			t.setEndStationCode("SZQ");
			t.setCoachNo("01");
			t.setSeatCode("0001");
			faceData.setTicket(t);
			FaceCheckingService.getInstance().offerFaceVerifyData(faceData);

			long usingTime = Calendar.getInstance().getTimeInMillis() - nowMils;
		}

	}

	public static void main(String[] args) {

		ExecutorService executer = Executors.newCachedThreadPool();
		FaceVerifyStandaloneTest task1 = new FaceVerifyStandaloneTest();
		executer.execute(task1);
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
