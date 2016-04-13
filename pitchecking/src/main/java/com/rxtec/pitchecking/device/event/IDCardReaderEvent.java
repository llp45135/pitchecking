package com.rxtec.pitchecking.device.event;

public class IDCardReaderEvent implements IDeviceEvent {
	private IDCard idCard;

	// public IDCard getIdCard() {
	// return idCard;
	// }

	public void setIdCard(IDCard idCard) {
		this.idCard = idCard;
	}

	@Override
	public int getEventType() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public Object getData() {
		// TODO Auto-generated method stub
		return idCard;
	}

}
