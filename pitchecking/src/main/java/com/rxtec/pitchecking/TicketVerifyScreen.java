package com.rxtec.pitchecking;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.AudioDevice;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.gui.FaceCheckFrame;
import com.rxtec.pitchecking.gui.TicketCheckFrame;
import com.rxtec.pitchecking.gui.VideoPanel;
import com.rxtec.pitchecking.gui.ticketgui.TicketVerifyFrame;

/**
 * 单独管理第一块屏的屏幕事件
 * 
 * @author ZhaoLin
 *
 */
public class TicketVerifyScreen {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");
	private static TicketVerifyScreen _instance;

//	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//	GraphicsDevice[] gs = ge.getScreenDevices();

	TicketVerifyFrame ticketFrame;

	public TicketVerifyFrame getTicketFrame() {
		return ticketFrame;
	}

	public void setTicketFrame(TicketVerifyFrame ticketFrame) {
		this.ticketFrame = ticketFrame;
	}

	private TicketVerifyScreen() {

	}
	
	public static synchronized TicketVerifyScreen getInstance() {
		if (_instance == null) {
			_instance = new TicketVerifyScreen();
		}
		return _instance;
	}

//	public void initUI() {
//		gs[DeviceConfig.getInstance().getTicketScreen()].setFullScreenWindow(ticketFrame);
//		// ticketFrame.setUndecorated(true);
//		// ticketFrame.setVisible(true);
//
//	}
	
	public void initUI(int screenNo) throws Exception {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		GraphicsDevice gd = gs[screenNo];
		// log.info("GraphicsDevice=="+gd);
		if (gd != null) {
			GraphicsConfiguration gc = gd.getDefaultConfiguration();
			int xOff = gc.getBounds().x;
			int yOff = gc.getBounds().y;
			ticketFrame.setVisible(true);
			ticketFrame.setLocation(xOff, yOff);
		}
		// log.info("当前的人脸检测屏位置：face.x=="+faceFrame.getBounds().x+",face.y=="+faceFrame.getBounds().y);
	}

	private LinkedBlockingQueue<ScreenElementModifyEvent> screenEventQueue = new LinkedBlockingQueue<ScreenElementModifyEvent>();

	public void offerEvent(ScreenElementModifyEvent e) {
		if (e != null) {
			if (e.getScreenType() == 0) {
				processEventByTicketCmdType(e);
			}
		}
	}

	/**
	 * 车票屏幕事件处理
	 * 
	 * @param e
	 */
	private void processEventByTicketCmdType(ScreenElementModifyEvent e) {
		int cmdType = e.getElementCmd();
		Ticket ticket = e.getTicket();
		IDCard idCard = e.getIdCard();

		if (cmdType == ScreenCmdEnum.ShowTicketVerifyWaitInput.getValue()) {
			if (ticket == null && idCard != null) {
				// log.debug("等待旅客扫描车票！");
				ticketFrame.showWaitInputContent(null, idCard, 1, 0);
			}
			if (ticket != null && idCard == null) {
				// log.debug("等待旅客刷身份证！");
				ticketFrame.showWaitInputContent(ticket, null, 2, 0);
			}
		} else if (cmdType == ScreenCmdEnum.ShowTicketVerifyStationRuleFail.getValue()) {
			log.debug("收到ShowTicketVerifyStationRuleFail屏幕事件，重画屏幕");
			String msg = "非本站乘车,请核对始发站名！";
			ticketFrame.showFailedContent(DeviceConfig.getInstance(), ticket, 4, 0, msg);
		} else if (cmdType == ScreenCmdEnum.ShowTicketVerifyTrainDateRuleFail.getValue()) {
			log.debug("收到ShowTicketVerifyTrainDateRuleFail屏幕事件，重画屏幕");
			String msg = "非当日乘车,请核对乘车日期！";
			ticketFrame.showFailedContent(DeviceConfig.getInstance(), ticket, 4, 0, msg);
		} else if (e.getElementCmd() == ScreenCmdEnum.ShowTicketVerifyIDFail.getValue()) {
			log.debug("收到ShowTicketVerifyIDFail屏幕事件，重画屏幕");
			String msg = "票证不相符,请核对！";
			ticketFrame.showFailedContent(DeviceConfig.getInstance(), ticket, 4, 0, msg);
		} else if (cmdType == ScreenCmdEnum.ShowTicketVerifySucc.getValue()) {
			log.debug("收到ShowTicketVerifySucc屏幕事件，重画屏幕");
			ticketFrame.showTicketContent(DeviceConfig.getInstance(), ticket, 3, 1);
		} else if (cmdType == ScreenCmdEnum.ShowTicketDefault.getValue()) {
			log.debug("收到ShowTicketDefault屏幕事件，重画屏幕");
			ticketFrame.showDefaultContent();
		} else if (cmdType == ScreenCmdEnum.ShowQRDeviceException.getValue()) {
			String exMsg = "二维码扫描器故障!";
			ticketFrame.showExceptionContent(DeviceConfig.getInstance(), -1, exMsg);
		} else if (cmdType == ScreenCmdEnum.ShowIDDeviceException.getValue()) {
			String exMsg = "二代证读卡器故障!";
			ticketFrame.showExceptionContent(DeviceConfig.getInstance(), -1, exMsg);
		} else if (cmdType == ScreenCmdEnum.ShowVersionFault.getValue()) {
			String exMsg = "软件版本错误!";
			ticketFrame.showExceptionContent(DeviceConfig.getInstance(), -1, exMsg);
		} else if (cmdType == ScreenCmdEnum.ShowCamOpenException.getValue()) {
			String exMsg = "摄像头连接故障!";
			ticketFrame.showExceptionContent(DeviceConfig.getInstance(), -1, exMsg);
		}else if (cmdType == ScreenCmdEnum.ShowStopCheckFault.getValue()) {
			String exMsg = "暂停服务";
			ticketFrame.showSuccWait(exMsg, "请走其他通道");
		}
	}

	public ScreenElementModifyEvent getScreenEvent() {
		return null;
	}

}
