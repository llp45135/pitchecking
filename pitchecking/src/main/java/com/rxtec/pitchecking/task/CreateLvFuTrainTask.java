package com.rxtec.pitchecking.task;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.rxtec.pitchecking.db.sqlserver.PSDataService;

public class CreateLvFuTrainTask implements Job {

	public CreateLvFuTrainTask() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		PSDataService.getInstance().writeTrainListToTxt();
	}

}
