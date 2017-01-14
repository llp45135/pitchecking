package com.rxtec.pitchecking.task;

import java.util.Date;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.utils.CommUtil;

public class AutoLogonJob implements Job {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");

	@Override
	public void execute(JobExecutionContext jobContext) throws JobExecutionException {
		// TODO Auto-generated method stub
		try {
			log.info("闸机定时注销任务,开始执行！ By " + jobContext.getJobDetail().getJobClass());
//			Runtime.getRuntime().exec(Config.AutoLogonCmd);
			Runtime.getRuntime().exec("shutdown -l");
		} catch (Exception ex) {
			log.error("AutoLogonJob:", ex);
		}
	}

	public static void main(String[] args) {
		try {
			SchedulerFactory sf = new StdSchedulerFactory();
			Scheduler sched = sf.getScheduler(); // 初始化调度器
			JobDetail job = JobBuilder.newJob(AutoLogonJob.class).withIdentity("autoLogonJob", "pitcheckGroup").build();
			CronTrigger trigger = (CronTrigger) TriggerBuilder.newTrigger()
					.withIdentity("autoLogonTrigger", "pitcheckGroup")
					.withSchedule(CronScheduleBuilder.cronSchedule("0/20 * * * * ?")).build(); // 设置触发器
																								// 每20秒执行一次
			Date ft = sched.scheduleJob(job, trigger); // 设置调度作业
			System.out.println(job.getKey() + " has been scheduled to run at: " + ft
					+ " and repeat based on expression: " + trigger.getCronExpression());
			sched.start(); // 开启调度任务，执行作业

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
