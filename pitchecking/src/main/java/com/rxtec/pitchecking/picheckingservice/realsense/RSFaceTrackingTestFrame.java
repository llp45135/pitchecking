package com.rxtec.pitchecking.picheckingservice.realsense;

import javax.swing.JOptionPane;

import com.rxtec.pitchecking.ScreenCmdEnum;
import com.rxtec.pitchecking.TicketCheckScreen;
import com.rxtec.pitchecking.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.PITData;
import com.rxtec.pitchecking.picheckingservice.FaceDetectionService;

public class RSFaceTrackingTestFrame {
	public static void main(String[] args) {
		try {
			
			TicketCheckScreen screen = TicketCheckScreen.getInstance();
			screen.initUI();
			
			RSFaceDetectionService rsft = RSFaceDetectionService.getInstance();
			rsft.setVideoPanel(screen.getVideoPanel());
			rsft.beginVideoCaptureAndTracking();
			
			screen.startShow();

			
//			FaceCheckingService.getInstance().beginFaceCheckerTask();
//			FaceDetectionService.getInstance().setVideoPanel(screen.getVideoPanel());
//			FaceDetectionService.getInstance().beginVideoCaptureAndTracking();
//			
//			//FaceCheckingService.getInstance().beginFaceQualityDetecterTask();
			
		
//			while(true){
//				Thread.sleep(100);
//				screen.offerEvent(
//						new ScreenElementModifyEvent(1,ScreenCmdEnum.ShowBeginCheckFaceContent.getValue(),null));
//				FaceDetectionService.getInstance().beginCheckingFace(createIDCard());
//				FaceData fd = FaceCheckingService.getInstance().pollPassFaceData();
//				if(fd == null){
//					TicketCheckScreen.getInstance().offerEvent(
//							new ScreenElementModifyEvent(1, ScreenCmdEnum.ShowFaceCheckFailed.getValue(), fd));
//					FaceDetectionService.getInstance().stopCheckingFace();
//				}else{
//					TicketCheckScreen.getInstance().offerEvent(
//							new ScreenElementModifyEvent(1, ScreenCmdEnum.ShowFaceCheckPass.getValue(), fd));
//					FaceDetectionService.getInstance().stopCheckingFace();
//
//				}
//				
//			}

			 
			
			
			
			
		} catch (final Exception e) {
			// an error occured
			JOptionPane.showMessageDialog(null, "Unable to open video.");
		}
	}

}
