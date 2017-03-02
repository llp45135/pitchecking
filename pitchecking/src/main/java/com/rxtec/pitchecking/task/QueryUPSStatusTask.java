package com.rxtec.pitchecking.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.device.UPSDevice;
import com.rxtec.pitchecking.utils.CommUtil;

public class QueryUPSStatusTask implements Job {
	private Log log = LogFactory.getLog("UPSDevice");
	private UPSDevice upsDevice = UPSDevice.getInstance();

	public QueryUPSStatusTask() {
		// TODO Auto-generated constructor stub
		
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		if (!upsDevice.isClosePC()) {
			upsDevice.queryUPSStatus();
		}

		if (upsDevice.isClosePC() && !upsDevice.isCloseUPS()) {
			upsDevice.setCloseUPS(true);
			upsDevice.closeUPS();
			log.debug("已发出关闭UPS命令");
		}
	}

}
