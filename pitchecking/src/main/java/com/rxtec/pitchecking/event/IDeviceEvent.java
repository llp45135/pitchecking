package com.rxtec.pitchecking.event;

import com.rxtec.pitchecking.DeviceEventTypeEnum;

public interface IDeviceEvent {
	int getEventType();
	Object getData();	
	
}
