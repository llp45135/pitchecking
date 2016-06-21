package com.rxtec.pitchecking;

import java.util.Calendar;
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
import com.rxtec.pitchecking.picheckingservice.PITData;
import com.rxtec.pitchecking.picheckingservice.realsense.RSFaceDetectionService;
import com.rxtec.pitchecking.task.RunningStatus;

/**
 * 事件处理任务
 * @author ZhaoLin
 *
 */
public class VerifyFaceTask{
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");

	IDCardReaderEvent event;
	IFaceTrackService faceTrackService = null; 
	public VerifyFaceTask() {
		if(Config.getInstance().getVideoType() == Config.RealSenseVideo) 
			faceTrackService = RSFaceDetectionService.getInstance();
		else 
			faceTrackService = FaceDetectionService.getInstance();
	}

	public PITData beginCheckFace(IDCard idCard) {
		TicketCheckScreen.getInstance().offerEvent(
				new ScreenElementModifyEvent(1, ScreenCmdEnum.ShowBeginCheckFaceContent.getValue(), null, null, null));

		
		ScreenElementModifyEvent semEvent = new ScreenElementModifyEvent(1, ScreenCmdEnum.showIDCardImage.getValue(),
				null, null, null);
		semEvent.setIdCard(idCard);
		TicketCheckScreen.getInstance().offerEvent(semEvent);
		
		faceTrackService.beginCheckingFace(idCard);

		long nowMils = Calendar.getInstance().getTimeInMillis();

		PITData fd =  null;
		try {
			fd = FaceCheckingService.getInstance().pollPassFaceData();
		} catch (InterruptedException e) {
			log.error("IDReaderEventTask call",e);
		}
		

		if(fd == null){
			long usingTime = Calendar.getInstance().getTimeInMillis() - nowMils;
			log.debug("pollPassFaceData, using " + usingTime +" value = null");
			faceTrackService.stopCheckingFace();
			TicketCheckScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(1, ScreenCmdEnum.ShowFaceCheckFailed.getValue(), null, null, fd));
			TicketCheckScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(1, ScreenCmdEnum.showDefaultContent.getValue(), null, null, fd));

			DeviceEventListener.getInstance().setPitStatus(PITStatusEnum.FaceCheckedFailed.getValue());
			
		}else{
			long usingTime = Calendar.getInstance().getTimeInMillis() - nowMils;
			log.debug("pollPassFaceData, using " + usingTime + " ms, value=" + fd.getFaceCheckResult());
			faceTrackService.stopCheckingFace();
			TicketCheckScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(1, ScreenCmdEnum.ShowFaceCheckPass.getValue(), null, null, fd));
			TicketCheckScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(1, ScreenCmdEnum.showDefaultContent.getValue(), null, null, fd));
			DeviceEventListener.getInstance().setPitStatus(PITStatusEnum.FaceChecked.getValue());
		}

		return fd;
	}

}
