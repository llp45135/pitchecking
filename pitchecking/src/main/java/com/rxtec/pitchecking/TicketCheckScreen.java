package com.rxtec.pitchecking;

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

import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.gui.FaceCheckFrame;
import com.rxtec.pitchecking.gui.TicketCheckFrame;
import com.rxtec.pitchecking.gui.VideoPanel;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.FaceDetectionService;

public class TicketCheckScreen {
	private Logger log = LoggerFactory.getLogger("TicketCheckScreen");
	private static TicketCheckScreen _instance = new TicketCheckScreen();

	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	GraphicsDevice[] gs = ge.getScreenDevices();

	TicketCheckFrame ticketFrame = new TicketCheckFrame();
	FaceCheckFrame faceFrame = new FaceCheckFrame();

	private TicketCheckScreen() {

	}

	public void initUI() {
		
		 gs[DeviceConfig.getInstance().getFaceScreen()].setFullScreenWindow(faceFrame);
		 gs[DeviceConfig.getInstance().getTicketScreen()].setFullScreenWindow(ticketFrame);
		// ticketFrame.setUndecorated(true);
		// ticketFrame.setVisible(true);

//		gs[1].setFullScreenWindow(faceFrame);
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

	public static TicketCheckScreen getInstance() {
		return _instance;
	}

	private LinkedBlockingQueue<ScreenElementModifyEvent> screenEventQueue = new LinkedBlockingQueue<ScreenElementModifyEvent>();

	public void offerEvent(ScreenElementModifyEvent e) {
		if (e != null) {
			if (e.getScreenType() == 0) {
				processEventByTicketCmdType(e);
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

	/**
	 * 车票屏幕事件处理
	 * 
	 * @param e
	 */
	private void processEventByTicketCmdType(ScreenElementModifyEvent e) {
		if (e.getElementCmd() == ScreenCmdEnum.ShowTicketVerifyWaitInput.getValue()) {
			if (e.getTicket() == null) {
				// log.debug("等待旅客扫描车票！");
				ticketFrame.showWaitInputContent(e.getTicket(), e.getIdCard(), 1);
			} else if (e.getIdCard() == null) {
				// log.debug("等待旅客刷身份证！");
				ticketFrame.showWaitInputContent(e.getTicket(), e.getIdCard(), 2);
			}
		} else if (e.getElementCmd() == ScreenCmdEnum.ShowTicketVerifyStationRuleFail.getValue()) {
			log.debug("收到ShowTicketVerifyStationRuleFail屏幕事件，重画屏幕");
			String msg = "车票不符合乘车条件！";
			Ticket ticket = e.getTicket();
			ticketFrame.showFailedContent(DeviceConfig.getInstance(), ticket, 4, msg);
		} else if (e.getElementCmd() == ScreenCmdEnum.ShowTicketVerifyIDFail.getValue()) {
			log.debug("收到ShowTicketVerifyIDFail屏幕事件，重画屏幕");
			String msg = "车票与身份证不相符！";
			Ticket ticket = e.getTicket();
			ticketFrame.showFailedContent(DeviceConfig.getInstance(), ticket, 4, msg);
		} else if (e.getElementCmd() == ScreenCmdEnum.ShowTicketVerifySucc.getValue()) {
			Ticket ticket = e.getTicket();
			ticketFrame.showTicketContent(DeviceConfig.getInstance(), ticket, 3);
		} else if (e.getElementCmd() == ScreenCmdEnum.ShowTicketDefault.getValue()) {
			ticketFrame.showDefaultContent();
		} else if (e.getElementCmd() == ScreenCmdEnum.ShowQRDeviceException.getValue()) {
			String exMsg = "二维码扫描器故障，请联系维护人员!";
			ticketFrame.showExceptionContent(DeviceConfig.getInstance(), -1, exMsg);
		} else if (e.getElementCmd() == ScreenCmdEnum.ShowIDDeviceException.getValue()) {
			String exMsg = "二代证读卡器故障，请联系维护人员!";
			ticketFrame.showExceptionContent(DeviceConfig.getInstance(), -1, exMsg);
		}
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

		} else if (e.getElementCmd() == ScreenCmdEnum.showDefaultContent.getValue()) {
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
