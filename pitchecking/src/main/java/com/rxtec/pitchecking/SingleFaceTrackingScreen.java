package com.rxtec.pitchecking;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.gui.FaceCheckFrame;
import com.rxtec.pitchecking.gui.VideoPanel;
import com.rxtec.pitchecking.gui.singledoor.SingleVerifyFrame;

/**
 * 人脸识别显示屏幕
 * 
 * @author lenovo
 *
 */
public class SingleFaceTrackingScreen {

	private Logger log = LoggerFactory.getLogger("DeviceEventListener");
	private static SingleFaceTrackingScreen _instance = new SingleFaceTrackingScreen();

	SingleVerifyFrame singleVerifyFrame;

	public SingleVerifyFrame getFaceFrame() {
		return singleVerifyFrame;
	}

	public void setFaceFrame(SingleVerifyFrame singleVerifyFrame) {
		this.singleVerifyFrame = singleVerifyFrame;
	}

	private SingleFaceTrackingScreen() {

	}

	public void initUI(int screenNo) throws Exception {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		GraphicsDevice gd = gs[screenNo];
		// log.debug("GraphicsDevice=="+gd);
		if (gd != null) {
			GraphicsConfiguration gc = gd.getDefaultConfiguration();
			int xOff = gc.getBounds().x;
			int yOff = gc.getBounds().y;
			singleVerifyFrame.setVisible(true);
			singleVerifyFrame.setLocation(xOff, yOff);
		}
		// log.debug("当前的人脸检测屏位置：face.x=="+singleVerifyFrame.getBounds().x+",face.y=="+singleVerifyFrame.getBounds().y);
	}

	public VideoPanel getVideoPanel() {
		return singleVerifyFrame.getVideoPanel();
	}

	public static SingleFaceTrackingScreen getInstance() {
		return _instance;
	}

	public void offerEvent(ScreenElementModifyEvent e) {
		if (e != null) {
			if (e.getScreenType() == 0) {
				processEventByTicketCmdType(e);
			} else {
				processEventByType(e);
			}
		}
	}

	/**
	 * 人脸识别屏幕事件处理
	 * 
	 * @param e
	 */
	private void processEventByType(ScreenElementModifyEvent e) {
		if (e.getElementCmd() == ScreenCmdEnum.ShowBeginCheckFaceContent.getValue()) {
			singleVerifyFrame.showBeginCheckFaceContent();
		} else if (e.getElementCmd() == ScreenCmdEnum.ShowFaceCheckPass.getValue()) {
			singleVerifyFrame.showFaceCheckPassContent();
		} else if (e.getElementCmd() == ScreenCmdEnum.showFaceDefaultContent.getValue()) {
			singleVerifyFrame.showFaceDefaultContent();
		} else if (e.getElementCmd() == ScreenCmdEnum.ShowFaceCheckFailed.getValue()) {
			singleVerifyFrame.showCheckFailedContent();
		} else if (e.getElementCmd() == ScreenCmdEnum.ShowFaceDisplayFromTK.getValue()) {
			singleVerifyFrame.showFaceDisplayFromTK();
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
				singleVerifyFrame.showWaitInputContent(null, idCard, 1, 0);
			}
			if (ticket != null && idCard == null) {
				// log.debug("等待旅客刷身份证！");
				singleVerifyFrame.showWaitInputContent(ticket, null, 2, 0);
			}
		} else if (cmdType == ScreenCmdEnum.ShowTicketVerifyStationRuleFail.getValue()) {
			log.debug("收到ShowTicketVerifyStationRuleFail屏幕事件，重画屏幕");
			String msg = "非本站乘车,请核对始发站名！";
			singleVerifyFrame.showFailedContent(DeviceConfig.getInstance(), ticket, 4, 0, msg);
		} else if (cmdType == ScreenCmdEnum.ShowTicketVerifyTrainDateRuleFail.getValue()) {
			log.debug("收到ShowTicketVerifyTrainDateRuleFail屏幕事件，重画屏幕");
			String msg = "非当日乘车,请核对乘车日期！";
			singleVerifyFrame.showFailedContent(DeviceConfig.getInstance(), ticket, 4, 0, msg);
		} else if (e.getElementCmd() == ScreenCmdEnum.ShowTicketVerifyIDFail.getValue()) {
			log.debug("收到ShowTicketVerifyIDFail屏幕事件，重画屏幕");
			String msg = "票证不相符,请核对！";
			singleVerifyFrame.showFailedContent(DeviceConfig.getInstance(), ticket, 4, 0, msg);
		} else if (cmdType == ScreenCmdEnum.ShowTicketVerifySucc.getValue()) {
			log.debug("收到ShowTicketVerifySucc屏幕事件，重画屏幕");
			singleVerifyFrame.showTicketContent(DeviceConfig.getInstance(), ticket, 3, 2);
		} else if (cmdType == ScreenCmdEnum.ShowTicketDefault.getValue()) {
			log.debug("收到ShowTicketDefault屏幕事件，重画屏幕");
			singleVerifyFrame.showTicketDefaultContent();
		} else if (cmdType == ScreenCmdEnum.ShowQRDeviceException.getValue()) {
			String exMsg = "二维码扫描器故障!";
			singleVerifyFrame.showExceptionContent(DeviceConfig.getInstance(), -1, exMsg);
		} else if (cmdType == ScreenCmdEnum.ShowIDDeviceException.getValue()) {
			String exMsg = "二代证读卡器故障!";
			singleVerifyFrame.showExceptionContent(DeviceConfig.getInstance(), -1, exMsg);
		} else if (cmdType == ScreenCmdEnum.ShowVersionFault.getValue()) {
			String exMsg = "软件版本错误!";
			singleVerifyFrame.showExceptionContent(DeviceConfig.getInstance(), -1, exMsg);
		} else if (cmdType == ScreenCmdEnum.ShowCamOpenException.getValue()) {
			String exMsg = "摄像头连接故障!";
			singleVerifyFrame.showExceptionContent(DeviceConfig.getInstance(), -1, exMsg);
		} else if (cmdType == ScreenCmdEnum.ShowStopCheckFault.getValue()) {
			String exMsg = "暂停服务";
			singleVerifyFrame.showSuccWait(exMsg, "请走其他通道");
		} else if (cmdType == ScreenCmdEnum.showFailedIDCard.getValue()) {
			String exMsg = "读二代证失败";
			singleVerifyFrame.showTKInfo(exMsg, "请将二代证摆在验票区", 1);
		} else if (cmdType == ScreenCmdEnum.showFailedQRCode.getValue()) {
			String exMsg = "无电子票或二维码模糊";
			singleVerifyFrame.showTKInfo(exMsg, "请走人工通道", 1);
		} else if (cmdType == ScreenCmdEnum.showNoETicket.getValue()) {
			String exMsg = "无电子票或二维码模糊";
			singleVerifyFrame.showTKInfo(exMsg, "请走人工通道", 1);
		} else if (cmdType == ScreenCmdEnum.showInvalidTicketAndIDCard.getValue()) {
			String exMsg = "票证不符";
			singleVerifyFrame.showTKInfo(exMsg, "请核对是否本人票证", 1);
		} else if (cmdType == ScreenCmdEnum.showPassTime.getValue()) {
			String exMsg = "已过检票时间";
			singleVerifyFrame.showTKInfo(exMsg, "请到售票处改签", 1);
		} else if (cmdType == ScreenCmdEnum.showETicketPassTime.getValue()) {
			String exMsg = "已过检票时间";
			singleVerifyFrame.showTKInfo(exMsg, "请到售票处改签", 1);
		} else if (cmdType == ScreenCmdEnum.showNotInTime.getValue()) {
			String exMsg = "未到检票时间";
			singleVerifyFrame.showTKInfo(exMsg, "请稍候再来验票", 1);
		} else if (cmdType == ScreenCmdEnum.showETicketNotInTime.getValue()) {
			String exMsg = "未到检票时间";
			singleVerifyFrame.showTKInfo(exMsg, "请稍候再来验票", 1);
		} else if (cmdType == ScreenCmdEnum.showPassStation.getValue()) {
			String exMsg = "越站乘车";
			singleVerifyFrame.showTKInfo(exMsg, "请走人工通道", 1);
		} else if (cmdType == ScreenCmdEnum.showWrongStation.getValue()) {
			String exMsg = "非本站乘车";
			singleVerifyFrame.showTKInfo(exMsg, "请核对车票", 1);
		} else if (cmdType == ScreenCmdEnum.showRepeatCheck.getValue()) {
			String exMsg = "重复验票";
			singleVerifyFrame.showTKInfo(exMsg, "请换他人验票", 1);
		}
	}

	public void repainSingleVerifyFrame() {
		// singleVerifyFrame.showIDCardImage(null);
		singleVerifyFrame.getContentPane().repaint();
	}

	public ScreenElementModifyEvent getScreenEvent() {
		return null;
	}

}
