package com.rxtec.pitchecking.device;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.jfree.util.Log;
import org.openimaj.video.capture.VideoCaptureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.device.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.gui.FaceCheckFrame;
import com.rxtec.pitchecking.gui.TicketCheckFrame;
import com.rxtec.pitchecking.gui.VideoPanel;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.FaceDetectionService;

public class TicketCheckScreen {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");
	private static TicketCheckScreen _instance = new TicketCheckScreen();
	private ExecutorService executor = Executors.newCachedThreadPool();

	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	GraphicsDevice[] gs = ge.getScreenDevices();

	TicketCheckFrame ticketFrame = new TicketCheckFrame();
	FaceCheckFrame faceFrame = new FaceCheckFrame();

	private TicketCheckScreen() {
	
	}
	
	
	public void initUI(){
//		gs[0].setFullScreenWindow(faceFrame);
//		gs[1].setFullScreenWindow(ticketFrame);
//		ticketFrame.setUndecorated(true);
//		ticketFrame.setVisible(true);		
		
		gs[1].setFullScreenWindow(faceFrame);
		gs[0].setFullScreenWindow(ticketFrame);
//		faceFrame.setUndecorated(true);
//		faceFrame.setVisible(true);
		
	}

	public VideoPanel getVideoPanel(){
		return faceFrame.getVideoPanel();
	}
	
	
	public static TicketCheckScreen getInstance() {
		return _instance;
	}

	private LinkedBlockingQueue<ScreenElementModifyEvent> screenEventQueue = new LinkedBlockingQueue<ScreenElementModifyEvent>();

	public void offerEvent(ScreenElementModifyEvent e) {
		if (e != null) {
			if (e.getScreenType() == 0) {
				log.debug("收到Ticket屏幕事件，重画屏幕");	
				Ticket ticket = e.getTicket();
				
				ticketFrame.showTicketContent(Config.getInstance(), ticket);
			} else if (e.getScreenType() == 1) {
				processEventByType(e);
			}
		}
	}

	public void startShow() throws InterruptedException {
		
//		while(true){
//			ScreenElementModifyEvent e = screenEventQueue.take();
//			/*
//			 * 根据ScreenElementModifyEvent的screenType、elementType、
//			 * elementCmd来决定屏幕的显示内容
//			 */
//			if (e != null) {
//				if (e.getScreenType() == 0) {
//					log.debug("收到Ticket屏幕事件，重画屏幕");
//
//					ticketFrame.getContentPane().repaint();
//					// gs[0].setFullScreenWindow(ticketFrame);
//				} else if (e.getScreenType() == 1) {
//					processEventByType(e);
//				}
//			}
//		}

	}
	
	

	
	
	private void processEventByType(ScreenElementModifyEvent e){
		if(e.getElementType() == 1){
			log.debug("收到Face屏幕事件，重画屏幕");
			ImageIcon icon = new ImageIcon(e.getIdCard().getCardImage());
			faceFrame.showIDCardImage(icon);

		}else if(e.getElementCmd() == ScreenCmdEnum.ShowBeginCheckFaceContent.getValue()){
			faceFrame.showBeginCheckFaceContent();

		}else if(e.getElementCmd() == ScreenCmdEnum.ShowFaceCheckPass.getValue()){
			faceFrame.showFaceCheckPassContent();;

		}else if(e.getElementCmd() == ScreenCmdEnum.showDefaultContent.getValue()){
			faceFrame.showDefaultContent();

		}else if(e.getElementCmd() == ScreenCmdEnum.ShowFaceCheckFailed.getValue()){
			faceFrame.showCheckFailedContent();
		}
	}
	
	public void repainFaceFrame(){
		faceFrame.showIDCardImage(null);
		faceFrame.getContentPane().repaint();
	}

	public ScreenElementModifyEvent getScreenEvent() {
		return null;
	}


}
