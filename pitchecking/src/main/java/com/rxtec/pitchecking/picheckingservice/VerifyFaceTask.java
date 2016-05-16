package com.rxtec.pitchecking.picheckingservice;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.device.DeviceEventListener;
import com.rxtec.pitchecking.device.PITStatusEnum;
import com.rxtec.pitchecking.device.ScreenCmdEnum;
import com.rxtec.pitchecking.device.TicketCheckScreen;
import com.rxtec.pitchecking.device.event.IDCardReaderEvent;
import com.rxtec.pitchecking.device.event.IDeviceEvent;
import com.rxtec.pitchecking.device.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.picheckingservice.realsense.RSFaceDetectionService;
import com.rxtec.pitchecking.task.RunningStatus;

/**
 * 事件处理任务
 * @author ZhaoLin
 *
 */
public class VerifyFaceTask implements Callable<PICData> {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");

	IDCardReaderEvent event;
	IFaceTrackService faceTrackService = null; 

	public VerifyFaceTask(IDeviceEvent e) {
		event = (IDCardReaderEvent) e;
		if(Config.getInstance().getVideoType() == Config.RealSenseVideo) 
			faceTrackService = RSFaceDetectionService.getInstance();
		else 
			faceTrackService = FaceDetectionService.getInstance();
	}

	@Override
	public PICData call() {
		// TODO 当接收到二代证读卡器事件时，后续处理
//		log.debug("正在调用回调函数处理IDReaderEventTask==" + this.event);
		TicketCheckScreen.getInstance().offerEvent(
				new ScreenElementModifyEvent(1,ScreenCmdEnum.ShowBeginCheckFaceContent.getValue(),null));

		IDCard idcard = (IDCard)this.event.getData();
//		log.debug("idcard number =="+idcard.getIdNo());
		
		ScreenElementModifyEvent semEvent = new ScreenElementModifyEvent(1, 1, 1);
		semEvent.setIdCard(idcard);
		TicketCheckScreen.getInstance().offerEvent(semEvent);
		
		RunningStatus.getInstance().getIdReaderLock();
		faceTrackService.beginCheckingFace(idcard);


		PICData fd =  null;
		try {
			fd = FaceCheckingService.getInstance().pollPassFaceData();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			log.error("IDReaderEventTask call",e);
		}
		

		if(fd == null){
			TicketCheckScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(1, ScreenCmdEnum.ShowFaceCheckFailed.getValue(), fd));
			TicketCheckScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(1, ScreenCmdEnum.showDefaultContent.getValue(), fd));

			DeviceEventListener.getInstance().setPitStatus(PITStatusEnum.FaceCheckedFailed.getValue());
			
			faceTrackService.stopCheckingFace();
			RunningStatus.getInstance().getIdReaderCondition().signal();
			
		}else{
			TicketCheckScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(1, ScreenCmdEnum.ShowFaceCheckPass.getValue(), fd));
			TicketCheckScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(1, ScreenCmdEnum.showDefaultContent.getValue(), fd));
			DeviceEventListener.getInstance().setPitStatus(PITStatusEnum.FaceChecked.getValue());
			faceTrackService.stopCheckingFace();
			RunningStatus.getInstance().getIdReaderCondition().signal();

		}

		return fd;
	}

}
