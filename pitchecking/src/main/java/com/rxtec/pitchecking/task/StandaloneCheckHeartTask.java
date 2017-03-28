package com.rxtec.pitchecking.task;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.mbean.ProcessUtil;
import com.rxtec.pitchecking.mqtt.GatCtrlSenderBroker;

public class StandaloneCheckHeartTask implements Job {
	private Logger log = LoggerFactory.getLogger("FaceScreenListener");

	public StandaloneCheckHeartTask() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		String pidStr = ProcessUtil.getCurrentProcessID();
		// ProcessUtil.writeHeartbeat(pidStr,
		// Config.getInstance().getStandaloneCheckHeartFile()); // 写心跳日志
		GatCtrlSenderBroker.getInstance(DeviceConfig.GAT_MQ_Standalone_CLIENT).sendMessage(DeviceConfig.EventTopic,
				DeviceConfig.getInstance().getHeartStr(pidStr, "A"));
		// log.debug("写StandaloneCheckHeartTask心跳");
	}

}
