package com.rxtec.pitchecking.picheckingservice.realsense;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.ScreenCmdEnum;
import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.TicketCheckScreen;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.gui.FaceCheckFrame;
import com.rxtec.pitchecking.gui.VideoPanel;

public class TestFaceVerifyScreen {

	private Logger log = LoggerFactory.getLogger("TicketCheckScreen");
	private static TestFaceVerifyScreen _instance = new TestFaceVerifyScreen();

	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	GraphicsDevice[] gs = ge.getScreenDevices();

	FaceCheckFrame faceFrame = new FaceCheckFrame();

	private TestFaceVerifyScreen() {

	}

	public void initUI() {

		gs[1].setFullScreenWindow(faceFrame);
		// ticketFrame.setUndecorated(true);
		// ticketFrame.setVisible(true);

		// gs[1].setFullScreenWindow(faceFrame);
		// if(gs.length>1)
		// gs[1].setFullScreenWindow(ticketFrame);
		// else
		// gs[0].setFullScreenWindow(ticketFrame);
		// faceFrame.setUndecorated(true);
		// faceFrame.setVisible(true);

	}

	public VideoPanel getVideoPanel() {
		return faceFrame.getVideoPanel();
	}

	public static TestFaceVerifyScreen getInstance() {
		return _instance;
	}

	private LinkedBlockingQueue<ScreenElementModifyEvent> screenEventQueue = new LinkedBlockingQueue<ScreenElementModifyEvent>();

	public void offerEvent(ScreenElementModifyEvent e) {
		if (e != null) {
			if (e.getScreenType() == 0) {
//				processEventByTicketCmdType(e);
			} else if (e.getScreenType() == 1) {
				processEventByType(e);
			}
		}
	}

	public void startShow() throws InterruptedException {

		// while(true){
		// ScreenElementModifyEvent e = screenEventQueue.take();
		// /*
		// * 根据ScreenElementModifyEvent的screenType、elementType、
		// * elementCmd来决定屏幕的显示内容
		// */
		// if (e != null) {
		// if (e.getScreenType() == 0) {
		// log.debug("收到Ticket屏幕事件，重画屏幕");
		//
		// ticketFrame.getContentPane().repaint();
		// // gs[0].setFullScreenWindow(ticketFrame);
		// } else if (e.getScreenType() == 1) {
		// processEventByType(e);
		// }
		// }
		// }

	}


	private void processEventByType(ScreenElementModifyEvent e) {
		// if (e.getElementCmd() == ScreenCmdEnum.showIDCardImage.getValue()) {
		// // log.debug("收到Face屏幕事件，重画屏幕");
		// ImageIcon icon = new ImageIcon(e.getIdCard().getCardImage());
		// faceFrame.showIDCardImage(icon);
		//
		// } else
		if (e.getElementCmd() == ScreenCmdEnum.ShowBeginCheckFaceContent.getValue()) {
			faceFrame.showBeginCheckFaceContent();

		} else if (e.getElementCmd() == ScreenCmdEnum.ShowFaceCheckPass.getValue()) {
			faceFrame.showFaceCheckPassContent();
			;

		} else if (e.getElementCmd() == ScreenCmdEnum.showFaceDefaultContent.getValue()) {
			faceFrame.showDefaultContent();

		} else if (e.getElementCmd() == ScreenCmdEnum.ShowFaceCheckFailed.getValue()) {
			faceFrame.showCheckFailedContent();
		}
	}

	public void repainFaceFrame() {
		// faceFrame.showIDCardImage(null);
		faceFrame.getContentPane().repaint();
	}

	public ScreenElementModifyEvent getScreenEvent() {
		return null;
	}

}