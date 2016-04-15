package com.rxtec.pitchecking.device.event;

import com.rxtec.pitchecking.device.DeviceEventTypeEnum;

public interface IDeviceEvent {
	int getEventType();
	Object getData();	
	
}
