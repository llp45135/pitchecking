package com.rxtec.pitchecking.device.event;

import com.rxtec.pitchecking.picheckingservice.IDCard;

public class IDCardReaderEvent implements IDeviceEvent {

	
	int eventType;
	
	public IDCardReaderEvent(int t){
		eventType = t;
	}


	private IDCard idCard;
	public void setIdCard(IDCard idCard) {
		this.idCard = idCard;
	}

	@Override
	public int getEventType() {
		// TODO Auto-generated method stub
		return eventType;
	}

	@Override
	public Object getData() {
		// TODO Auto-generated method stub
		return idCard;
	}

}
