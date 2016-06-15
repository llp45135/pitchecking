package com.rxtec.pitchecking.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 闸门控制
 * @author lenovo
 *
 */
public class GateDeviceManager {
	private Logger log = LoggerFactory.getLogger("GateDeviceManager");

	private static GateDeviceManager instance;

	public static synchronized GateDeviceManager getInstance() {
		if (instance == null) {
			instance = new GateDeviceManager();
		}
		return instance;
	}

	
	public int openFirstDoor(){
		log.debug("openFirstDoor");
		return 0;
	}
	
	//电磁开关侧门
	public int openSecondDoor(){
		log.debug("openSecondDoor");
		return 0;
	}

	public int openThirdDoor(){
		log.debug("openThirdDoor");
		return 0;
	}

}
