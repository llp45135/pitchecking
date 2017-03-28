package com.rxtec.pitchecking.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.mq.RemoteMonitorPublisher;
import com.rxtec.pitchecking.mq.police.PITInfoPolicePublisher;
import com.rxtec.pitchecking.mqtt.ClosePCEventSenderBroker;
import com.rxtec.pitchecking.mqtt.ManualEventReceiverBroker;

public class ClosePCSendingTask implements Runnable {
	private Logger log = LoggerFactory.getLogger("ClosePCSendingTask");
	private String taskType = "";

	public ClosePCSendingTask(String taskType) {
		this.taskType = taskType;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if (Config.getInstance().getIsSendClosePCCmd() == 1) { // 是否需要向其他闸机发送同步关机命令
			if (taskType.equals(DeviceConfig.GAT_MQ_PidProtect_CLIENT)) { // 人脸比对进程
				ClosePCEventSenderBroker.getInstance(DeviceConfig.GAT_MQ_PidProtect_CLIENT);
			}
		}
	}

}
