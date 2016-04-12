package com.rxtec.pitchecking.device;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.event.IDeviceEvent;
import com.rxtec.pitchecking.device.event.QRCodeReaderEvent;
import com.rxtec.pitchecking.device.event.ScreenElementModifyEvent;

public class QRCodeEventTask implements Callable<Integer> {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");
	QRCodeReaderEvent event;

	public QRCodeEventTask(IDeviceEvent e) {
		this.event = (QRCodeReaderEvent) e;
	}

	@Override
	public Integer call() throws Exception {
		// TODO Auto-generated method stub
		// TODO 当接收到二维码读卡器事件时，后续处理
		log.debug("正在调用回调函数处理QRCodeEventTask==" + this.event);

		ScreenElementModifyEvent semEvent = new ScreenElementModifyEvent(0, 1, 1);
		TicketCheckScreen.getInstance().offerEvent(semEvent);
		return null;
	}

}
