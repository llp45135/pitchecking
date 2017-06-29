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
import com.rxtec.pitchecking.gui.faceocx.CallOcxFaceFrame;
import com.rxtec.pitchecking.gui.faceocx.OcxFaceFrameTwo;
import com.rxtec.pitchecking.gui.singledoor.SingleVerifyFrame;

/**
 * 人脸识别显示屏幕
 * 
 * @author lenovo
 *
 */
public class SingleOcxFaceScreen {

	private Logger log = LoggerFactory.getLogger("DeviceEventListener");
	private static SingleOcxFaceScreen _instance = new SingleOcxFaceScreen();

	 CallOcxFaceFrame ocxFaceFrame;
//	OcxFaceFrameTwo ocxFaceFrame;

//	public OcxFaceFrameTwo getOcxFaceFrame() {
//		return ocxFaceFrame;
//	}
//
//	public void setOcxFaceFrame(OcxFaceFrameTwo ocxFaceFrame) {
//		this.ocxFaceFrame = ocxFaceFrame;
//	}

	private SingleOcxFaceScreen() {

	}

	 public CallOcxFaceFrame getOcxFaceFrame() {
	 return ocxFaceFrame;
	 }
	
	 public void setOcxFaceFrame(CallOcxFaceFrame ocxFaceFrame) {
	 this.ocxFaceFrame = ocxFaceFrame;
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

			// callOcxFaceFrame.setVisible(true);
			// callOcxFaceFrame.setLocation(xOff, yOff);
			// callOcxFaceFrame.initActiveX();

			ocxFaceFrame.setVisible(true);
			ocxFaceFrame.setLocation(xOff, yOff);
			ocxFaceFrame.initActiveX();
		}
		// log.debug("当前的人脸检测屏位置：face.x=="+singleVerifyFrame.getBounds().x+",face.y=="+singleVerifyFrame.getBounds().y);
	}

	public static SingleOcxFaceScreen getInstance() {
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
			ocxFaceFrame.showBeginCheckFaceContent();
		} else if (e.getElementCmd() == ScreenCmdEnum.showFaceDefaultContent.getValue()) {
			ocxFaceFrame.showFaceDefaultContent();
		} else if (e.getElementCmd() == ScreenCmdEnum.ShowFaceDisplayFromTK.getValue()) {
			ocxFaceFrame.showFaceDisplayFromTK();
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
				ocxFaceFrame.showWaitInputContent(null, idCard, 1, 0);
			}
			if (ticket != null && idCard == null) {
				// log.debug("等待旅客刷身份证！");
				ocxFaceFrame.showWaitInputContent(ticket, null, 2, 0);
			}
		} else if (cmdType == ScreenCmdEnum.ShowTicketDefault.getValue()) {
			log.debug("收到ShowTicketDefault屏幕事件，重画屏幕");
			ocxFaceFrame.showTicketDefaultContent();
		} else if (cmdType == ScreenCmdEnum.ShowTicketVerifySucc.getValue()) {
			log.debug("收到ShowTicketVerifySucc屏幕事件，重画屏幕");
			String exMsg = "票证一致";
			ocxFaceFrame.showSuccWait("人脸核验中", "后面的旅客切勿刷证");
		} else if (cmdType == ScreenCmdEnum.ShowQRDeviceException.getValue()) {
			String exMsg = "二维码扫描器故障!";
			ocxFaceFrame.showExceptionContent(DeviceConfig.getInstance(), -1, exMsg);
		} else if (cmdType == ScreenCmdEnum.ShowIDDeviceException.getValue()) {
			String exMsg = "二代证读卡器故障!";
			ocxFaceFrame.showExceptionContent(DeviceConfig.getInstance(), -1, exMsg);
		} else if (cmdType == ScreenCmdEnum.ShowVersionFault.getValue()) {
			String exMsg = "软件版本错误!";
			ocxFaceFrame.showExceptionContent(DeviceConfig.getInstance(), -1, exMsg);
		} else if (cmdType == ScreenCmdEnum.ShowCamOpenException.getValue()) {
			String exMsg = "摄像头连接故障!";
			ocxFaceFrame.showExceptionContent(DeviceConfig.getInstance(), -1, exMsg);
		} else if (cmdType == Integer.parseInt("-04002")) {
			String errCode = "";
			ocxFaceFrame.showSuccWait(errCode, "公安接口初始化失败");
		} else if (cmdType == Integer.parseInt("-03002")) {
			String errCode = "";
			ocxFaceFrame.showSuccWait(errCode, "网络故障");
		} else if (cmdType == Integer.parseInt("-39650")) {
			String errCode = "";
			ocxFaceFrame.showSuccWait(errCode, "设备序列号不符");
		} else if (cmdType == Integer.parseInt("-39647")) {
			String errCode = "";
			ocxFaceFrame.showSuccWait("终端未初始化", "等待全局参数");
		} else if (cmdType == ScreenCmdEnum.ShowStopCheckFault.getValue()) {
			String errCode = "暂停服务";
			ocxFaceFrame.showSuccWait(errCode, "请走其他通道");
		} else if (cmdType == ScreenCmdEnum.showFailedIDCard.getValue()) {
			String errCode = "读二代证失败";
			ocxFaceFrame.showTKInfo(errCode, "请将二代证摆在验票区", 1);
		} else if (cmdType == ScreenCmdEnum.showFailedQRCode.getValue()) {
			String errCode = "无电子票或二维码模糊";
			ocxFaceFrame.showTKInfo(errCode, "请走人工通道", 1);
		} else if (cmdType == ScreenCmdEnum.showNoETicket.getValue()) {
			String errCode = "无电子票或二维码模糊";
			ocxFaceFrame.showTKInfo(errCode, "请走人工通道", 1);
		} else if (cmdType == ScreenCmdEnum.showInvalidTicketAndIDCard.getValue()) {
			String errCode = "票证不符";
			ocxFaceFrame.showTKInfo(errCode, "请核对是否本人票证", 1);
		} else if (cmdType == ScreenCmdEnum.showPassTime.getValue()) {
			String errCode = "已过检票时间";
			ocxFaceFrame.showTKInfo(errCode, "请到售票处改签", 1);
		} else if (cmdType == ScreenCmdEnum.showETicketPassTime.getValue()) {
			String errCode = "已过检票时间";
			ocxFaceFrame.showTKInfo(errCode, "请到售票处改签", 1);
		} else if (cmdType == ScreenCmdEnum.showNotInTime.getValue()) {
			String errCode = "未到检票时间";
			ocxFaceFrame.showTKInfo(errCode, "请稍候再来验票", 1);
		} else if (cmdType == ScreenCmdEnum.showETicketNotInTime.getValue()) {
			String errCode = "未到检票时间";
			ocxFaceFrame.showTKInfo(errCode, "请稍候再来验票", 1);
		} else if (cmdType == ScreenCmdEnum.showPassStation.getValue()) {
			String errCode = "越站乘车";
			ocxFaceFrame.showTKInfo(errCode, "请走人工通道", 1);
		} else if (cmdType == ScreenCmdEnum.showWrongStation.getValue()) {
			String errCode = "非本站乘车";
			ocxFaceFrame.showTKInfo(errCode, "请核对车票", 1);
		} else if (cmdType == ScreenCmdEnum.showRepeatCheck.getValue()) {
			String errCode = "重复验票";
			ocxFaceFrame.showTKInfo(errCode, "请换他人验票", 1);
		} else if (cmdType == ScreenCmdEnum.showConnTicketCenterFailed.getValue()) {
			String errCode = "内部服务器错误";
			ocxFaceFrame.showTKInfo(errCode, "请走人工通道", 1);
		} else if (cmdType == ScreenCmdEnum.showNoShipTicket.getValue()) {
			String errCode = "无电子船票";
			ocxFaceFrame.showTKInfo(errCode, "请走人工通道", 1);
		} else if (cmdType == ScreenCmdEnum.showNoInvalidShipTicket.getValue()) {
			String errCode = "船票不符";
			ocxFaceFrame.showTKInfo(errCode, "请走人工通道", 1);
		} else if (cmdType == -80220) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "系统认证失败", 1);
		} else if (cmdType == -80221) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "报文信息为空", 1);
		} else if (cmdType == -80222) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "固定报文长度不符", 1);
		} else if (cmdType == -80223) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "车票信息为空", 1);
		} else if (cmdType == -80224) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "检票模式无效", 1);
		} else if (cmdType == -80225) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "车站码无效", 1);
		} else if (cmdType == -80226) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "终端号或终端时间无效", 1);
		} else if (cmdType == -80227) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "查询车站检票业务定义失败", 1);
		} else if (cmdType == -80228) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "终端号无效", 1);
		} else if (cmdType == -80229) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "车站未开通电子票核验业务", 1);
		} else if (cmdType == -80230) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "售处号或票号无效", 1);
		} else if (cmdType == -80231) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "车票列车信息无效", 1);
		} else if (cmdType == -80232) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "实名信息无效", 1);
		} else if (cmdType == -80233) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "窗口号无效", 1);
		} else if (cmdType == -80234) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "结账日期无效", 1);
		} else if (cmdType == -80235) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "票种无效", 1);
		} else if (cmdType == -80236) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "票价无效", 1);
		} else if (cmdType == -80237) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "设备未定义", 1);
		} else if (cmdType == -80238) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "查询终端定义失败", 1);
		} else if (cmdType == -80239) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "闸机已停用", 1);
		} else if (cmdType == -80240) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "终端检票类型不符", 1);
		} else if (cmdType == -80241) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "终端电子票核验功能未开启", 1);
		} else if (cmdType == -80242) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "终端IP地址不符", 1);
		} else if (cmdType == -80243) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "查询终端检票规则参数失败", 1);
		} else if (cmdType == -80244) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "乘车站不符,禁止异地上车", 1);
		} else if (cmdType == -80245) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "核对列车开行状态失败", 1);
		} else if (cmdType == -80246) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "查询业务控制参数失败", 1);
		} else if (cmdType == -80247) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "未知错误", 1);
		} else if (cmdType == -80248) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "查询核验记录失败", 1);
		} else if (cmdType == -80249) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "未知错误", 1);
		} else if (cmdType == -80250) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "核验次数超限", 1);
		} else if (cmdType == -80251) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "查询列车检票站站序失败", 1);
		} else if (cmdType == -80252) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "查询到站站序失败", 1);
		} else if (cmdType == -80253) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "票不符,已到站", 1);
		} else if (cmdType == -80254) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "未知错误", 1);
		} else if (cmdType == -80255) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "越站乘车", 1);
		} else if (cmdType == -80256) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "未到进站时间", 1);
		} else if (cmdType == -80257) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "已过进站时间", 1);
		} else if (cmdType == -80258) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "查询到站名失败", 1);
		} else if (cmdType == -80259) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "查询中转站名失败", 1);
		} else if (cmdType == -80260) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "查询终端交易流水号失败", 1);
		} else if (cmdType == -80261) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "保存核验记录失败", 1);
		} else if (cmdType == -80262) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "票不符,列车不经过本站", 1);
		} else if (cmdType == -80263) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "获取电子票出错", 1);
		} else if (cmdType == -80264) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "解析电子票数据失败", 1);
		} else if (cmdType == -80265) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "售处号或票号无效", 1);
		} else if (cmdType == -80266) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "车票列车信息无效", 1);
		} else if (cmdType == -80267) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "实名信息无效", 1);
		} else if (cmdType == -80268) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "窗口号无效", 1);
		} else if (cmdType == -80269) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "结账日期无效", 1);
		} else if (cmdType == -80270) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "票种无效", 1);
		} else if (cmdType == -80271) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "票价无效", 1);
		} else if (cmdType == -80272) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "核对列车开行状态失败", 1);
		} else if (cmdType == -80273) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "查询核验记录失败", 1);
		} else if (cmdType == -80274) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "检票次数超限", 1);
		} else if (cmdType == -80275) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "未到进站时间", 1);
		} else if (cmdType == -80276) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "已过进站时间", 1);
		} else if (cmdType == -80277) {
			String errCode = "";
			ocxFaceFrame.showTKInfo(errCode, "无电子票信息", 1);
		} else {
			String errCode = String.valueOf(cmdType);
			ocxFaceFrame.showSuccWait(errCode, "未知错误!!");
		}
	}

	public void repainSingleVerifyFrame() {
		// singleVerifyFrame.showIDCardImage(null);
		ocxFaceFrame.getContentPane().repaint();
	}

	public ScreenElementModifyEvent getScreenEvent() {
		return null;
	}

}
