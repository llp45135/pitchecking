package com.rxtec.pitchecking;

import com.rxtec.pitchecking.device.FirstGateDevice;
import com.rxtec.pitchecking.device.SecondGateDevice;

public class GATStatusQuery implements Runnable {
	private static GATStatusQuery instance;
	
	public static synchronized GATStatusQuery getInstance() {
		if (instance == null) {
			instance = new GATStatusQuery();
		}
		return instance;
	}
	
	private GATStatusQuery(){
		
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		this.readGATStatus();
	}
	
	private void readGATStatus(){
		FirstGateDevice.getInstance().getGateStatus();
		SecondGateDevice.getInstance().getGateStatus();
	}

}
