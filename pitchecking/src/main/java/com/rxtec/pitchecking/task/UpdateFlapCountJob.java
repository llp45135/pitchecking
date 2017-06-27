package com.rxtec.pitchecking.task;

import org.jfree.util.Log;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.DeviceEventListener;
import com.rxtec.pitchecking.device.smartmonitor.MonitorXMLUtil;

public class UpdateFlapCountJob implements Job {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");

	public UpdateFlapCountJob() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		// 更新监控状态
		log.info("开始定时更新开门次数");
		MonitorXMLUtil.updateFlapTotalForMonitor(Config.getInstance().getStatusInfoXMLPath(), DeviceEventListener.getInstance().getFlapCount());
	}

}
