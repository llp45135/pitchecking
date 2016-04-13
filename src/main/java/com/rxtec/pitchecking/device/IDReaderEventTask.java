package com.rxtec.pitchecking.device;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.event.IDCardReaderEvent;
import com.rxtec.pitchecking.device.event.IDeviceEvent;
import com.rxtec.pitchecking.device.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.FaceData;
import com.rxtec.pitchecking.picheckingservice.FaceTrackingService;
import com.rxtec.pitchecking.picheckingservice.IDCard;

/**
 * 事件处理任务
 * @author ZhaoLin
 *
 */
public class IDReaderEventTask implements Callable<Integer> {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");

	IDCardReaderEvent event;

	public IDReaderEventTask(IDeviceEvent e) {
		event = (IDCardReaderEvent) e;
	}

	@Override
	public Integer call() throws Exception {
		// TODO 当接收到二代证读卡器事件时，后续处理
		log.debug("正在调用回调函数处理IDReaderEventTask==" + this.event);
		IDCard idcard = (IDCard)this.event.getData();
		log.debug("idcard number =="+idcard.getIdNo());
		
		ScreenElementModifyEvent semEvent = new ScreenElementModifyEvent(1, 1, 1);
		semEvent.setIdCard(idcard);
		TicketCheckScreen.getInstance().offerEvent(semEvent);
		FaceTrackingService.getInstance().beginCheckingFace(idcard);


		return null;
	}

}
