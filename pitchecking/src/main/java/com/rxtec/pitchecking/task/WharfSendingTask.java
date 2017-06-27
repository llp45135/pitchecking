package com.rxtec.pitchecking.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.mq.wharf.PITInfoWharfPublisher;

public class WharfSendingTask implements Runnable {
	private Logger log = LoggerFactory.getLogger("WharfSendingTask");
	private String taskType = "";

	public WharfSendingTask(String taskType) {
		this.taskType = taskType;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if(Config.getInstance().getIsUseWharfMQ()==1){  //是否连接桂林MQ
			if (taskType.equals(DeviceConfig.GAT_MQ_Standalone_CLIENT)) {  //人脸比对进程
				PITInfoWharfPublisher.getInstance().startService(2);// 启动Event转发线程
			}
		}
	}

}
