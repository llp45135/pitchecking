package com.rxtec.pitchecking;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.event.IDCardReaderEvent;
import com.rxtec.pitchecking.event.IDeviceEvent;
import com.rxtec.pitchecking.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.FaceDetectionService;
import com.rxtec.pitchecking.picheckingservice.IFaceTrackService;
import com.rxtec.pitchecking.picheckingservice.PICData;
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
	IDCard idCard = null;
	public VerifyFaceTask(IDCard idCard) {
		this.idCard = idCard;
		if(Config.getInstance().getVideoType() == Config.RealSenseVideo) 
			faceTrackService = RSFaceDetectionService.getInstance();
		else 
			faceTrackService = FaceDetectionService.getInstance();
	}

	@Override
	public PICData call() {
		TicketCheckScreen.getInstance().offerEvent(
				new ScreenElementModifyEvent(1,ScreenCmdEnum.ShowBeginCheckFaceContent.getValue(),null));

		
		ScreenElementModifyEvent semEvent = new ScreenElementModifyEvent(1, 1, 1);
		semEvent.setIdCard(idCard);
		TicketCheckScreen.getInstance().offerEvent(semEvent);
		
		faceTrackService.beginCheckingFace(idCard);


		PICData fd =  null;
		try {
			fd = FaceCheckingService.getInstance().pollPassFaceData();
		} catch (InterruptedException e) {
			log.error("IDReaderEventTask call",e);
		}
		

		if(fd == null){
			TicketCheckScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(1, ScreenCmdEnum.ShowFaceCheckFailed.getValue(), fd));
			TicketCheckScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(1, ScreenCmdEnum.showDefaultContent.getValue(), fd));

			DeviceEventListener.getInstance().setPitStatus(PITStatusEnum.FaceCheckedFailed.getValue());
			
			faceTrackService.stopCheckingFace();
		}else{
			TicketCheckScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(1, ScreenCmdEnum.ShowFaceCheckPass.getValue(), fd));
			TicketCheckScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(1, ScreenCmdEnum.showDefaultContent.getValue(), fd));
			DeviceEventListener.getInstance().setPitStatus(PITStatusEnum.FaceChecked.getValue());
			faceTrackService.stopCheckingFace();
		}

		return fd;
	}

}
