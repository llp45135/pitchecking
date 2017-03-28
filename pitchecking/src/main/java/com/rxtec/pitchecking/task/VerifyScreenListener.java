package com.rxtec.pitchecking.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.TicketVerifyScreen;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.mbean.ProcessUtil;
import com.rxtec.pitchecking.mqtt.GatCtrlSenderBroker;

public class VerifyScreenListener implements Runnable {
	private Logger log = LoggerFactory.getLogger("FaceScreenListener");
	private Logger mainlog = LoggerFactory.getLogger("DeviceEventListener");

	private static VerifyScreenListener _instance;
	private int screenNo = 0;

	public int getScreenNo() {
		return screenNo;
	}

	public void setScreenNo(int screenNo) {
		this.screenNo = screenNo;
	}

	public static synchronized VerifyScreenListener getInstance() {
		if (_instance == null) {
			_instance = new VerifyScreenListener();
		}
		return _instance;
	}

	private VerifyScreenListener() {
		mainlog.debug("初始化VerifyScreen位置监控");
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		String pidStr = ProcessUtil.getCurrentProcessID();
//		ProcessUtil.writeHeartbeat(pidStr,Config.getInstance().getTicketVerifyHeartFile()); // 写心跳日志
		GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Verify_CLIENT).sendMessage(DeviceConfig.EventTopic,
				DeviceConfig.getInstance().getHeartStr(pidStr, "V"));
		
		int frameX = TicketVerifyScreen.getInstance().getTicketFrame().getX();
//		log.debug("frameX=="+frameX);
		if (frameX == 0) {
			for (int i = 0; i < 3; i++) {
				try {
//					log.debug("Start 重置VerifyScreen的位置，恢复至第" + this.screenNo + "块屏");
					TicketVerifyScreen.getInstance().initUI(this.screenNo);
					TicketVerifyScreen.getInstance().repainFaceFrame();
				} catch (Exception ex) {
					log.error("重置VerifyScreen的位置失败!再次重置...");
					continue;
				}
//				log.debug("重置VerifyScreen的位置成功!");
				break;
			}
		}

	}

}
