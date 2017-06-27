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
		// log.debug("GraphicsDevice=="+gd);
		if (gd != null) {
			GraphicsConfiguration gc = gd.getDefaultConfiguration();
			int xOff = gc.getBounds().x;
			int yOff = gc.getBounds().y;
			ticketFrame.setVisible(true);
			ticketFrame.setLocation(xOff, yOff);
		}
		// log.debug("当前的人脸检测屏位置：face.x=="+faceFrame.getBounds().x+",face.y=="+faceFrame.getBounds().y);
	}

	public void initUINoFull(int screenNo) throws Exception {
		ticketFrame.setVisible(true);
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
				// ticketFrame.showWaitInputContent(ticket, null, 2, 0);
				String errCode = "";
				ticketFrame.showTKInfo(errCode, "还需要刷二代证", 1);
			}
		} else if (cmdType == ScreenCmdEnum.ShowTicketVerifyStationRuleFail.getValue()) {
			log.debug("收到ShowTicketVerifyStationRuleFail屏幕事件，重画屏幕");
			String msg = "非本站乘车,请核对始发站名！";
			if (Config.getInstance().getIsOutGateForCSQ() == 1) {
				msg = "非本站出站,请走人工通道！";
			}
			ticketFrame.showFailedContent(DeviceConfig.getInstance(), ticket, 4, 0, msg);
		} else if (cmdType == ScreenCmdEnum.ShowTicketVerifyTrainDateRuleFail.getValue()) {
			log.debug("收到ShowTicketVerifyTrainDateRuleFail屏幕事件，重画屏幕");
			String msg = "非当日乘车,请核对乘车日期！";
			if (Config.getInstance().getIsOutGateForCSQ() == 1) {
				msg = "乘车日期不符,请走人工通道！";
			}
			ticketFrame.showFailedContent(DeviceConfig.getInstance(), ticket, 4, 0, msg);
		} else if (e.getElementCmd() == ScreenCmdEnum.ShowTicketVerifyIDFail.getValue()) {
			log.debug("收到票证不相符屏幕事件，重画屏幕");
			// String msg = "票证不相符,请核对！";
			// ticketFrame.showFailedContent(DeviceConfig.getInstance(), ticket,
			// 4, 0, msg);
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "票证不符,请仔细核对", 1);
		} else if (cmdType == ScreenCmdEnum.ShowTicketVerifySucc.getValue()) {
			log.debug("收到ShowTicketVerifySucc屏幕事件，重画屏幕");
			ticketFrame.showTicketContent(DeviceConfig.getInstance(), ticket, 3, 2);
		} else if (cmdType == ScreenCmdEnum.ShowBeginCheckFaceContent.getValue()) {
			log.debug("收到ShowBeginCheckFaceContent屏幕事件，重画屏幕");
			ticketFrame.showSuccWait("人脸核验中", "后面的旅客切勿刷票证");
		} else if (cmdType == ScreenCmdEnum.ShowTicketDefaultCSQ.getValue()) {
			log.debug("收到ShowTicketDefaultCSQ屏幕事件，重画屏幕");
			ticketFrame.showSuccWait("票面朝下", "扫描车票二维码");
		} else if (cmdType == ScreenCmdEnum.ShowTicketSuccCSQ.getValue()) {
			log.debug("收到ShowTicketSuccCSQ屏幕事件，重画屏幕");
			ticketFrame.showSuccWait("", "请通过");
		} else if (cmdType == ScreenCmdEnum.ShowTicketDefault.getValue()) {
			log.debug("收到ShowTicketDefault屏幕事件，重画屏幕");
			ticketFrame.showDefaultContent();
		} else if (cmdType == ScreenCmdEnum.ShowQRDeviceException.getValue()) {
			String errCode = "二维码扫描器故障!";
			ticketFrame.showExceptionContent(DeviceConfig.getInstance(), -1, errCode);
		} else if (cmdType == ScreenCmdEnum.ShowIDDeviceException.getValue()) {
			String errCode = "二代证读卡器故障!";
			ticketFrame.showExceptionContent(DeviceConfig.getInstance(), -1, errCode);
		} else if (cmdType == ScreenCmdEnum.ShowVersionFault.getValue()) {
			String errCode = "软件版本错误!";
			ticketFrame.showExceptionContent(DeviceConfig.getInstance(), -1, errCode);
		} else if (cmdType == ScreenCmdEnum.ShowCamOpenException.getValue()) {
			String errCode = "摄像头连接故障!";
			ticketFrame.showExceptionContent(DeviceConfig.getInstance(), -1, errCode);
		} else if (cmdType == ScreenCmdEnum.ShowDoorGateExceptoin.getValue()) {
			String errCode = "门模块故障!";
			ticketFrame.showExceptionContent(DeviceConfig.getInstance(), -1, errCode);
		} else if (cmdType == Integer.parseInt("-04002")) {
			String errCode = "";
			ticketFrame.showSuccWait(errCode, "公安接口初始化失败");
		} else if (cmdType == Integer.parseInt("-03002")) {
			String errCode = "";
			ticketFrame.showSuccWait(errCode, "网络故障");
		} else if (cmdType == Integer.parseInt("-39650")) {
			String errCode = "";
			ticketFrame.showSuccWait(errCode, "设备序列号不符");
		} else if (cmdType == Integer.parseInt("-39647")) {
			String errCode = "";
			ticketFrame.showSuccWait(errCode, "终端未初始化,等待全局参数");
		} else if (cmdType == ScreenCmdEnum.ShowStopCheckFault.getValue()) {
			String errCode = "暂停服务";
			ticketFrame.showSuccWait(errCode, "请走其他通道");
		} else if (cmdType == ScreenCmdEnum.showFailedIDCard.getValue()) {
			String errCode = "读二代证失败";
			ticketFrame.showTKInfo(errCode, "请将二代证摆在验票区", 1);
		} else if (cmdType == ScreenCmdEnum.showFailedQRCode.getValue()) {
			String errCode = "无电子票或二维码模糊";
			ticketFrame.showTKInfo(errCode, "请走人工通道", 1);
		} else if (cmdType == ScreenCmdEnum.showNoETicket.getValue()) {
			String errCode = "无电子票或二维码模糊";
			ticketFrame.showTKInfo(errCode, "请走人工通道", 1);
		} else if (cmdType == ScreenCmdEnum.showInvalidTicketAndIDCard.getValue()) {
			String errCode = "票证不符";
			ticketFrame.showTKInfo(errCode, "请核对是否本人票证", 1);
		} else if (cmdType == ScreenCmdEnum.showETicketPassTime.getValue()) {
			String errCode = "已过检票时间";
			ticketFrame.showTKInfo(errCode, "请到售票处改签", 1);
			// ticketFrame.showFailedContent(DeviceConfig.getInstance(), ticket,
			// 4, 0, errCode);
		} else if (cmdType == ScreenCmdEnum.showETicketNotInTime.getValue()) {
			String errCode = "未到检票时间";
			String msg = "开车前" + DeviceConfig.getInstance().getNotStartCheckMinutes() + "分钟允许进站";
			ticketFrame.showTKInfo(errCode, msg, 1);
			// ticketFrame.showFailedContent(DeviceConfig.getInstance(), ticket,
			// 4, 0, errCode);
		} else if (cmdType == ScreenCmdEnum.showPassStation.getValue()) {
			String errCode = "越站乘车";
			ticketFrame.showTKInfo(errCode, "请走人工通道", 1);
		} else if (cmdType == ScreenCmdEnum.showWrongStation.getValue()) {
			String errCode = "非本站乘车";
			ticketFrame.showTKInfo(errCode, "请核对车票", 1);
		} else if (cmdType == ScreenCmdEnum.showPassTime.getValue()) {
			String errCode = "已过检票时间";
			ticketFrame.showTKInfo(errCode, "请到售票处改签", 1);
		} else if (cmdType == ScreenCmdEnum.showNotInTime.getValue()) {
			String errCode = "未到检票时间";
			ticketFrame.showTKInfo(errCode, "请稍候再来验票", 1);
		} else if (cmdType == ScreenCmdEnum.showRepeatCheck.getValue()) {
			String errCode = "重复验票";
			ticketFrame.showTKInfo(errCode, "请换他人验票", 1);
		} else if (cmdType == ScreenCmdEnum.showTrainStopped.getValue()) {
			String errCode = "列车已停运";
			ticketFrame.showTKInfo(errCode, "请到售票处改签", 1);
		} else if (cmdType == ScreenCmdEnum.showInvalidTrain.getValue()) {
			String errCode = "无效数据";
			ticketFrame.showTKInfo(errCode, "请核对车票", 1);
		} else if (cmdType == -80220) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "系统认证失败", 1);
		} else if (cmdType == -80221) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "报文信息为空", 1);
		} else if (cmdType == -80222) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "固定报文长度不符", 1);
		} else if (cmdType == -80223) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "车票信息为空", 1);
		} else if (cmdType == -80224) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "检票模式无效", 1);
		} else if (cmdType == -80225) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "车站码无效", 1);
		} else if (cmdType == -80226) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "终端号或终端时间无效", 1);
		} else if (cmdType == -80227) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "查询车站检票业务定义失败", 1);
		} else if (cmdType == -80228) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "终端号无效", 1);
		} else if (cmdType == -80229) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "车站未开通电子票核验业务", 1);
		} else if (cmdType == -80230) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "售处号或票号无效", 1);
		} else if (cmdType == -80231) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "车票列车信息无效", 1);
		} else if (cmdType == -80232) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "实名信息无效", 1);
		} else if (cmdType == -80233) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "窗口号无效", 1);
		} else if (cmdType == -80234) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "结账日期无效", 1);
		} else if (cmdType == -80235) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "票种无效", 1);
		} else if (cmdType == -80236) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "票价无效", 1);
		} else if (cmdType == -80237) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "设备未定义", 1);
		} else if (cmdType == -80238) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "查询终端定义失败", 1);
		} else if (cmdType == -80239) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "闸机已停用", 1);
		} else if (cmdType == -80240) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "终端检票类型不符", 1);
		} else if (cmdType == -80241) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "终端电子票核验功能未开启", 1);
		} else if (cmdType == -80242) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "终端IP地址不符", 1);
		} else if (cmdType == -80243) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "查询终端检票规则参数失败", 1);
		} else if (cmdType == -80244) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "乘车站不符,禁止异地上车", 1);
		} else if (cmdType == -80245) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "核对列车开行状态失败", 1);
		} else if (cmdType == -80246) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "查询业务控制参数失败", 1);
		} else if (cmdType == -80247) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "未知错误", 1);
		} else if (cmdType == -80248) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "查询核验记录失败", 1);
		} else if (cmdType == -80249) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "未知错误", 1);
		} else if (cmdType == -80250) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "核验次数超限", 1);
		} else if (cmdType == -80251) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "查询列车检票站站序失败", 1);
		} else if (cmdType == -80252) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "查询到站站序失败", 1);
		} else if (cmdType == -80253) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "票不符,已到站", 1);
		} else if (cmdType == -80254) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "未知错误", 1);
		} else if (cmdType == -80255) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "越站乘车", 1);
		} else if (cmdType == -80256) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "未到进站时间", 1);
		} else if (cmdType == -80257) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "已过进站时间", 1);
		} else if (cmdType == -80258) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "查询到站名失败", 1);
		} else if (cmdType == -80259) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "查询中转站名失败", 1);
		} else if (cmdType == -80260) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "查询终端交易流水号失败", 1);
		} else if (cmdType == -80261) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "保存核验记录失败", 1);
		} else if (cmdType == -80262) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "票不符,列车不经过本站", 1);
		} else if (cmdType == -80263) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "获取电子票出错", 1);
		} else if (cmdType == -80264) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "解析电子票数据失败", 1);
		} else if (cmdType == -80265) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "售处号或票号无效", 1);
		} else if (cmdType == -80266) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "车票列车信息无效", 1);
		} else if (cmdType == -80267) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "实名信息无效", 1);
		} else if (cmdType == -80268) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "窗口号无效", 1);
		} else if (cmdType == -80269) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "结账日期无效", 1);
		} else if (cmdType == -80270) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "票种无效", 1);
		} else if (cmdType == -80271) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "票价无效", 1);
		} else if (cmdType == -80272) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "核对列车开行状态失败", 1);
		} else if (cmdType == -80273) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "查询核验记录失败", 1);
		} else if (cmdType == -80274) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "检票次数超限", 1);
		} else if (cmdType == -80275) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "未到进站时间", 1);
		} else if (cmdType == -80276) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "已过进站时间", 1);
		} else if (cmdType == -80277) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "无电子票信息", 1);
		} else if (cmdType == -80281) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "纸质车票已取", 1);
		} else if (cmdType == -80002) {
			String errCode = "";
			ticketFrame.showTKInfo(errCode, "票证不一致,请核对", 1);
		}
	}

	public ScreenElementModifyEvent getScreenEvent() {
		return null;
	}

}
