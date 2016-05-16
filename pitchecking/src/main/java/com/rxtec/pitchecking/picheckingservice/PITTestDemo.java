package com.rxtec.pitchecking.picheckingservice;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.tracking.KLTHaarFaceTracker;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.device.DeviceEventListener;
import com.rxtec.pitchecking.device.PITStatusEnum;
import com.rxtec.pitchecking.device.ScreenCmdEnum;
import com.rxtec.pitchecking.device.TicketCheckScreen;
import com.rxtec.pitchecking.device.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.gui.FaceCheckFrame;
import com.rxtec.pitchecking.picheckingservice.realsense.RSFaceDetectionService;


public class PITTestDemo {

	private KLTHaarFaceTracker faceTracker = new KLTHaarFaceTracker( 40 );
	static IFaceTrackService faceTrackService = null; 

	

	public static void main(String[] args) {
		try {
			
			TicketCheckScreen screen = TicketCheckScreen.getInstance();
			screen.initUI();
			
			if(Config.getInstance().getVideoType() == Config.RealSenseVideo) 
				faceTrackService = RSFaceDetectionService.getInstance();
			else 
				faceTrackService = FaceDetectionService.getInstance();

			
			
			FaceCheckingService.getInstance().beginFaceCheckerTask();
			faceTrackService.setVideoPanel(screen.getVideoPanel());
			faceTrackService.beginVideoCaptureAndTracking();
//			screen.startShow();

			
		
			while(true){
				Thread.sleep(100);
				screen.offerEvent(
						new ScreenElementModifyEvent(1,ScreenCmdEnum.ShowBeginCheckFaceContent.getValue(),null));
				RSFaceDetectionService.getInstance().beginCheckingFace(createIDCard());
				PICData fd = FaceCheckingService.getInstance().pollPassFaceData();
				if(fd == null){
					TicketCheckScreen.getInstance().offerEvent(
							new ScreenElementModifyEvent(1, ScreenCmdEnum.ShowFaceCheckFailed.getValue(), fd));
					FaceDetectionService.getInstance().stopCheckingFace();
				}else{
					TicketCheckScreen.getInstance().offerEvent(
							new ScreenElementModifyEvent(1, ScreenCmdEnum.ShowFaceCheckPass.getValue(), fd));
					RSFaceDetectionService.getInstance().stopCheckingFace();

				}
				
			}

			
			
			
			
			
		} catch (final Exception e) {
			// an error occured
			JOptionPane.showMessageDialog(null, "Unable to open video.");
		}
	}
	
	private static IDCard createIDCard(){
		IDCard card = new IDCard();
		card.setIdNo("440111197209283012");
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(new File("C:/pitchecking/idcardtest.jpg"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		card.setCardImage(bi);
		return card;
	}
}
