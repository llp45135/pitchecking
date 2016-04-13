package com.rxtec.pitchecking.device.event;

public interface IDeviceEvent {
	int getEventType();
	void setEventType(int arg);
	Object getData();	
	
}
