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

	// GraphicsEnvironment ge =
	// GraphicsEnvironment.getLocalGraphicsEnvironment();
	// GraphicsDevice[] gs = ge.getScreenDevices();

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

	// public void initUI() {
	// gs[DeviceConfig.getInstance().getTicketScreen()].setFullScreenWindow(ticketFrame);
	// // ticketFrame.setUndecorated(true);
	// // ticketFrame.setVisible(true);
	//
	// }

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
	
	public void repainFaceFrame() {
		// faceFrame.showIDCardImage(null);
		ticketFrame.getContentPane().repaint();
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
			ticketFrame.showTicketContent(DeviceConfig.getInstance(), ticket, 3, 2);
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
		} else if (cmdType == ScreenCmdEnum.ShowStopCheckFault.getValue()) {
			String exMsg = "暂停服务";
			ticketFrame.showSuccWait(exMsg, "请走其他通道");
		} else if (cmdType == ScreenCmdEnum.showFailedIDCard.getValue()) {
			String exMsg = "读二代证失败";
			ticketFrame.showTKInfo(exMsg, "请将二代证摆在验票区", 1);
		} else if (cmdType == ScreenCmdEnum.showFailedQRCode.getValue()) {
			String exMsg = "无电子票或二维码模糊";
			ticketFrame.showTKInfo(exMsg, "请走人工通道", 1);
		} else if (cmdType == ScreenCmdEnum.showNoETicket.getValue()) {
			String exMsg = "无电子票或二维码模糊";
			ticketFrame.showTKInfo(exMsg, "请走人工通道", 1);
		} else if (cmdType == ScreenCmdEnum.showInvalidTicketAndIDCard.getValue()) {
			String exMsg = "票证不符";
			ticketFrame.showTKInfo(exMsg, "请核对是否本人票证", 1);
		} else if (cmdType == ScreenCmdEnum.showPassTime.getValue()) {
			String exMsg = "已过检票时间";
			ticketFrame.showTKInfo(exMsg, "请到售票处改签", 1);
		} else if (cmdType == ScreenCmdEnum.showETicketPassTime.getValue()) {
			String exMsg = "已过检票时间";
			ticketFrame.showTKInfo(exMsg, "请到售票处改签", 1);
		} else if (cmdType == ScreenCmdEnum.showNotInTime.getValue()) {
			String exMsg = "未到检票时间";
			ticketFrame.showTKInfo(exMsg, "请稍候再来验票", 1);
		} else if (cmdType == ScreenCmdEnum.showETicketNotInTime.getValue()) {
			String exMsg = "未到检票时间";
			ticketFrame.showTKInfo(exMsg, "请稍候再来验票", 1);
		} else if (cmdType == ScreenCmdEnum.showPassStation.getValue()) {
			String exMsg = "越站乘车";
			ticketFrame.showTKInfo(exMsg, "请走人工通道", 1);
		} else if (cmdType == ScreenCmdEnum.showWrongStation.getValue()) {
			String exMsg = "非本站乘车";
			ticketFrame.showTKInfo(exMsg, "请核对车票", 1);
		}
	}

	public ScreenElementModifyEvent getScreenEvent() {
		return null;
	}

}
