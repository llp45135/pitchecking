package com.rxtec.pitchecking.picheckingservice.realsense;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.AudioPlayTask;
import com.rxtec.pitchecking.DeviceEventListener;
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.PITStatusEnum;
import com.rxtec.pitchecking.ScreenCmdEnum;
import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.TicketCheckScreen;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.device.SecondGateDevice;
import com.rxtec.pitchecking.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.gui.FaceCheckFrame;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.PITData;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.ImageToolkit;
import com.rxtec.pitchecking.picheckingservice.FaceDetectionService;
import com.rxtec.pitchecking.picheckingservice.IFaceTrackService;

public class RSFaceTrackingTestFrame {
	private static Logger log = LoggerFactory.getLogger("RSFaceTrackingTestFrame");

	static IFaceTrackService faceTrackService = RSFaceDetectionService.getInstance();

	public static void main(String[] args) throws InterruptedException {

		
		
		TestFaceVerifyScreen frame = TestFaceVerifyScreen.getInstance();
		frame.initUI();
//		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//		GraphicsDevice[] gs = ge.getScreenDevices();
//		
//		FaceCheckFrame frame = new FaceCheckFrame();
//		 gs[1].setFullScreenWindow(frame);

		RSFaceDetectionService rsft = RSFaceDetectionService.getInstance();
		rsft.setVideoPanel(frame.getVideoPanel());
		rsft.beginVideoCaptureAndTracking();


		FaceCheckingService.getInstance().beginFaceCheckerTask();

		IDCard idCard = createIDCard("C:/pitchecking/llp.jpg");
//		IDCard idCard = createIDCard("C:/pitchecking/zhaolin.jpg");
		

		while (true) {
			CommUtil.sleep(500);
			beginCheckFace(idCard);
		}

	}

	public static PITVerifyData beginCheckFace(IDCard idCard) {

		TestFaceVerifyScreen.getInstance().offerEvent(
				new ScreenElementModifyEvent(1, ScreenCmdEnum.ShowBeginCheckFaceContent.getValue(), null, null, null));

		faceTrackService.beginCheckingFace(idCard,new Ticket());

		long nowMils = Calendar.getInstance().getTimeInMillis();

		PITVerifyData fd = null;
		try {
			fd = FaceCheckingService.getInstance().pollPassFaceData();
		} catch (InterruptedException e) {
			log.error("IDReaderEventTask call", e);
		}

		if (fd == null) {
			long usingTime = Calendar.getInstance().getTimeInMillis() - nowMils;
			log.debug("pollPassFaceData, using " + usingTime + " value = null");
			faceTrackService.stopCheckingFace();

			log.debug("认证比对结果：picData==" + fd);

			TestFaceVerifyScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(1, ScreenCmdEnum.ShowFaceCheckFailed.getValue(), null, null, fd));
			TestFaceVerifyScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(1, ScreenCmdEnum.showFaceDefaultContent.getValue(), null, null, fd));

		} else {
			long usingTime = Calendar.getInstance().getTimeInMillis() - nowMils;
			log.debug("pollPassFaceData, using " + usingTime + " ms, value=" + fd.getVerifyResult());
			faceTrackService.stopCheckingFace();

			TestFaceVerifyScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(1, ScreenCmdEnum.ShowFaceCheckPass.getValue(), null, null, fd));
			TestFaceVerifyScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(1, ScreenCmdEnum.showFaceDefaultContent.getValue(), null, null, fd));
		}
		return fd;
	}

	private static IDCard createIDCard(String fn) {
		IDCard card = new IDCard();
		card.setIdNo("1234567890");
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(new File(fn));
		} catch (Exception e) {
			e.printStackTrace();
		}
		card.setCardImageBytes(ImageToolkit.getImageBytes(bi, "jpeg"));
		card.setCardImage(bi);
		card.setAge(44);
		return card;
	}
}
