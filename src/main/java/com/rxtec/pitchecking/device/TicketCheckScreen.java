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

import com.rxtec.pitchecking.device.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.gui.FaceCheckFrame;
import com.rxtec.pitchecking.gui.TicketCheckFrame;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.FaceTrackingService;

public class TicketCheckScreen {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");
	private static TicketCheckScreen _instance = new TicketCheckScreen();
	private ExecutorService executor = Executors.newCachedThreadPool();

	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	GraphicsDevice[] gs = ge.getScreenDevices();

	TicketCheckFrame ticketFrame = new TicketCheckFrame();
	FaceCheckFrame faceFrame = new FaceCheckFrame();

	private TicketCheckScreen() {
		startTicketScreenCustomer();
		ticketFrame.setUndecorated(true);
		ticketFrame.setVisible(true);
		//
		faceFrame.setUndecorated(true);
		faceFrame.setVisible(true);

		FaceTrackingService.getInstance().setVideoPanel(faceFrame.getVideoPanel());

		
		
	}

	public JPanel getVideoPanel(){
		return faceFrame.getVideoPanel();
	}
	
	
	public static TicketCheckScreen getInstance() {
		return _instance;
	}

	private LinkedBlockingQueue<ScreenElementModifyEvent> screenEventQueue = new LinkedBlockingQueue<ScreenElementModifyEvent>();

	public void offerEvent(ScreenElementModifyEvent e) {
		screenEventQueue.add(e);
	}

	public void startShow() throws InterruptedException {
		ScreenElementModifyEvent e = screenEventQueue.poll();
		/*
		 * 根据ScreenElementModifyEvent的screenType、elementType、
		 * elementCmd来决定屏幕的显示内容
		 */
		if (e != null) {
			if (e.getScreenType() == 0) {
				log.debug("收到Ticket屏幕事件，重画屏幕");

				ticketFrame.getContentPane().repaint();
				// gs[0].setFullScreenWindow(ticketFrame);
			} else if (e.getScreenType() == 1) {
				processEventByType(e);
			}
		}
	}
	
	
	private void processEventByType(ScreenElementModifyEvent e){
		if(e.getElementType() == 1){
			log.debug("收到Face屏幕事件，重画屏幕");
			ImageIcon icon = new ImageIcon(e.getIdCard().getCardImage());
			faceFrame.setIdcardBmp(icon);
			faceFrame.getContentPane().repaint();

		}else if(e.getElementType() == 2){
			float r = e.getFaceData().getFaceCheckResult();
			if(r>=0.7){
				faceFrame.updateFaceCheckResult(String.format("验证通过 %<2.2f", r));
				
			}else{
				faceFrame.updateFaceCheckResult(String.format("验证不通过 %<2.2f", r));

			}
			faceFrame.getContentPane().repaint();

		}
	}
	
	public void repainFaceFrame(){
		faceFrame.setIdcardBmp(null);
		faceFrame.getContentPane().repaint();
	}

	public ScreenElementModifyEvent getScreenEvent() {
		return null;
	}

	private void startTicketScreenCustomer() {
		executor.execute(new ScreenCustomer());
	}
}
