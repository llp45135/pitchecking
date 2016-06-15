package com.rxtec.pitchecking.event;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.IDCard;

public class IDCardReaderEvent implements IDeviceEvent {

	
	int eventType = Config.IDReaderEvent;
	

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
