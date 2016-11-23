package com.rxtec.pitchecking.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.mq.RemoteMonitorPublisher;
import com.rxtec.pitchecking.mq.police.PITInfoPolicePublisher;
import com.rxtec.pitchecking.mqtt.ManualEventReceiverBroker;

public class PoliceSendingTask implements Runnable {
	private Logger log = LoggerFactory.getLogger("PoliceSendingTask");
	private String taskType = "";

	public PoliceSendingTask(String taskType) {
		this.taskType = taskType;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if(Config.getInstance().getIsUsePoliceMQ()==1){  //是否连接公安处MQ
			if (taskType.equals(DeviceConfig.GAT_MQ_Standalone_CLIENT)) {  //人脸比对进程
//				PITInfoPolicePublisher.getInstance().startService(1);
				PITInfoPolicePublisher.getInstance().startService(2);// 启动Event转发线程
			}
		}
	}

}
