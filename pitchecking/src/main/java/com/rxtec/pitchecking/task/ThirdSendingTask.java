package com.rxtec.pitchecking.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.mq.quickhigh.PITInfoQuickPublisher;

public class ThirdSendingTask implements Runnable {
	private Logger log = LoggerFactory.getLogger("ThirdSendingTask");
	private String taskType = "";

	public ThirdSendingTask(String taskType) {
		this.taskType = taskType;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if(Config.getInstance().getIsUseThirdMQ()==1){  //是否连接第三方MQ
			if (taskType.equals(DeviceConfig.GAT_MQ_Standalone_CLIENT)) {  //人脸比对进程
				PITInfoQuickPublisher.getInstance().startService(2);// 启动Event转发线程
			}
		}
	}

}
