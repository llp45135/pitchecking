package com.rxtec.pitchecking.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rxtec.pitchecking.TicketVerifyScreen;
import com.rxtec.pitchecking.device.DeviceConfig;

public class GuideScreenListener implements Runnable {
	private Logger log = LoggerFactory.getLogger("FaceScreenListener");
	private Logger mainlog = LoggerFactory.getLogger("DeviceEventListener");

	private static GuideScreenListener _instance;
	private int screenNo = 0;

	public int getScreenNo() {
		return screenNo;
	}

	public void setScreenNo(int screenNo) {
		this.screenNo = screenNo;
	}

	public static synchronized GuideScreenListener getInstance() {
		if (_instance == null) {
			_instance = new GuideScreenListener();
		}
		return _instance;
	}

	private GuideScreenListener() {
		mainlog.info("初始化GuideScreen位置监控");
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		int frameX = TicketVerifyScreen.getInstance().getTicketFrame().getX();
//		log.debug("frameX=="+frameX);
		if (frameX != 0) {
			for (int i = 0; i < 3; i++) {
				try {
//					log.debug("Start 重置GuideScreen的位置，恢复至第" + this.screenNo + "块屏");
					TicketVerifyScreen.getInstance().initUI(this.screenNo);
					TicketVerifyScreen.getInstance().repainFaceFrame();
				} catch (Exception ex) {
					log.error("重置GuideScreen的位置失败!再次重置...");
					continue;
				}
//				log.debug("重置GuideScreen的位置成功!");
				break;
			}
		}

	}

}
