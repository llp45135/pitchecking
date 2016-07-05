package com.rxtec.pitchecking;

import com.rxtec.pitchecking.device.AudioDevice;
import com.rxtec.pitchecking.device.DeviceConfig;

public class AudioPlayTask implements Runnable {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		AudioDevice.getInstance().play(DeviceConfig.cameraWav);
	}

}
