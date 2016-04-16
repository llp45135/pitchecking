package com.rxtec.pitchecking.device;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.event.IDCardReaderEvent;
import com.rxtec.pitchecking.device.event.IDeviceEvent;
import com.rxtec.pitchecking.device.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.FaceData;
import com.rxtec.pitchecking.picheckingservice.FaceDetectionService;
import com.rxtec.pitchecking.picheckingservice.IDCard;

/**
 * 事件处理任务
 * @author ZhaoLin
 *
 */
public class IDReaderEventTask implements Callable<FaceData> {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");

	IDCardReaderEvent event;

	public IDReaderEventTask(IDeviceEvent e) {
		event = (IDCardReaderEvent) e;
	}

	@Override
	public FaceData call() {
		// TODO 当接收到二代证读卡器事件时，后续处理
		log.debug("正在调用回调函数处理IDReaderEventTask==" + this.event);
		TicketCheckScreen.getInstance().offerEvent(
				new ScreenElementModifyEvent(1,ScreenCmdEnum.ShowBeginCheckFaceContent.getValue(),null));

		IDCard idcard = (IDCard)this.event.getData();
		log.debug("idcard number =="+idcard.getIdNo());
		
		ScreenElementModifyEvent semEvent = new ScreenElementModifyEvent(1, 1, 1);
		semEvent.setIdCard(idcard);
		TicketCheckScreen.getInstance().offerEvent(semEvent);
		FaceDetectionService.getInstance().beginCheckingFace(idcard);


		FaceData fd =  null;
		try {
			fd = FaceCheckingService.getInstance().pollPassFaceData();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			log.error("IDReaderEventTask call",e);
		}
		
		TicketCheckScreen.getInstance().offerEvent(
				new ScreenElementModifyEvent(1, ScreenCmdEnum.ShowFaceCheckResult.getValue(), fd));
		

		if(fd == null){
			TicketCheckScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(1, ScreenCmdEnum.showDefaultContent.getValue(), fd));
			DeviceEventListener.getInstance().setPitStatus(PITStatusEnum.FaceCheckedFailed.getValue());
			FaceDetectionService.getInstance().stopCheckingFace();
		}else{
			TicketCheckScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(1, ScreenCmdEnum.showDefaultContent.getValue(), fd));
			DeviceEventListener.getInstance().setPitStatus(PITStatusEnum.FaceChecked.getValue());
			FaceDetectionService.getInstance().stopCheckingFace();
		}

		return fd;
	}

}
