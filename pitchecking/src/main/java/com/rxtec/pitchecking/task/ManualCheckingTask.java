package com.rxtec.pitchecking.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.mq.RemoteMonitorPublisher;
import com.rxtec.pitchecking.mqtt.ManualEventReceiverBroker;

public class ManualCheckingTask implements Runnable {
	private Logger log = LoggerFactory.getLogger("ManualCheckingTask");
	private String taskType = "";

	public ManualCheckingTask(String taskType) {
		this.taskType = taskType;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if (Config.getInstance().getIsUseManualMQ() == 1) {// 是否连人工窗
			if (taskType.equals(DeviceConfig.GAT_MQ_Standalone_CLIENT)) {
				RemoteMonitorPublisher.getInstance().startService(2);
				RemoteMonitorPublisher.getInstance().startService(3);// 启动Event转发线程
				ManualEventReceiverBroker.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT);
			}
			if (taskType.equals(DeviceConfig.GAT_MQ_Track_CLIENT)) {
				RemoteMonitorPublisher.getInstance().startService(1); // 启动整帧图片转发线程
				ManualEventReceiverBroker.getInstance(DeviceConfig.GAT_MQ_Track_CLIENT);
			}
		}
	}

}
