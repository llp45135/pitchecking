package com.rxtec.pitchecking.db.mysql;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.db.PitRecord;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
import com.rxtec.pitchecking.task.ClearPITFaceFileJob;

public class PitRecordSqlLoger implements Runnable {
	private Logger log = LoggerFactory.getLogger("PitRecordLoger");
	private static PitRecordSqlLoger _instance = new PitRecordSqlLoger();

	public static synchronized PitRecordSqlLoger getInstance() {
		if (_instance == null)
			_instance = new PitRecordSqlLoger();
		return _instance;
	}

	private PitRecordSqlDao pitRecordSqlDao;

	private PitRecordSqlLoger() {
		log.debug("初始化MySQL存储Photos线程");
		pitRecordSqlDao = new PitRecordSqlDao("127.0.0.1");
		this.addQuartzJobs();
	}

	public void startThread() {
		ExecutorService executer = Executors.newSingleThreadExecutor();
		executer.execute(_instance);

	}

	/**
	 * 添加定时扫描人脸本地数据，定期删除，保留10天数据
	 * @return
	 */
	private int addQuartzJobs() {
		int retVal = 1;
		try {
			String cronStr = "0 0 23 * * ?";
			SchedulerFactory sf = new StdSchedulerFactory();
			Scheduler sched = sf.getScheduler(); // 初始化调度器
			JobDetail job = JobBuilder.newJob(ClearPITFaceFileJob.class)
					.withIdentity("ClearPITFaceFileJob", "pitcheckGroup").build();
			CronTrigger trigger = (CronTrigger) TriggerBuilder.newTrigger()
					.withIdentity("ClearPITFaceFileTrigger", "pitcheckGroup")
					.withSchedule(CronScheduleBuilder.cronSchedule(cronStr)).build(); // 设置触发器
			Date ft = sched.scheduleJob(job, trigger); // 设置调度作业
			log.debug(job.getKey() + " has been scheduled to run at: " + ft + " and repeat based on expression: "
					+ trigger.getCronExpression());
			sched.start(); // 开启调度任务，执行作业

		} catch (Exception ex) {
			retVal = 0;
			ex.printStackTrace();
		}
		return retVal;
	}

	public void offer(PITVerifyData data) {
		faceVerifyDataQueue.add(data);
	}

	// public void clearExpirationData(){
	// int days = Config.getInstance().getFaceLogRemainDays();
	// SimpleDateFormat sFormat = new SimpleDateFormat("yyyyMMdd");
	//
	// Date today = new Date();
	// String dn = sFormat.format(new Date(today.getTime() - days * 24 * 60 * 60
	// * 1000));
	// log.debug("ClearExpirationData rows = " + dao.deleteRecords(dn));
	//
	//
	// }

	private LinkedBlockingQueue<PITVerifyData> faceVerifyDataQueue = new LinkedBlockingQueue<PITVerifyData>();

	@Override
	public void run() {
		while (true) {
			try {
				PITVerifyData pvd = faceVerifyDataQueue.take();
				PitFaceRecord rec = new PitFaceRecord(pvd);
				String sqls = pitRecordSqlDao.createInsertSqlstr(rec);
				if (sqls != null && !sqls.equals("")) {
					pitRecordSqlDao.insertSQL(sqls);
				}
			} catch (Exception e) {
				log.error("PitRecordSqlLoger run loop", e);
			}
		}

	}

}
