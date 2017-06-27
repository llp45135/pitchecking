package com.rxtec.pitchecking.db.mysql;

import java.text.ParseException;
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
import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.db.PitRecord;
import com.rxtec.pitchecking.device.BarUnsecurity;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.net.event.wharf.PhotoReceiptBean;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
import com.rxtec.pitchecking.task.ClearPITFaceFileJob;
import com.rxtec.pitchecking.utils.CalUtils;

public class PhotoReceiptSqlLoger implements Runnable {
	private Logger log = LoggerFactory.getLogger("PhotoReceiptSqlLoger");
	private static PhotoReceiptSqlLoger _instance = new PhotoReceiptSqlLoger();

	public static synchronized PhotoReceiptSqlLoger getInstance() {
		if (_instance == null)
			_instance = new PhotoReceiptSqlLoger();
		return _instance;
	}

	private PitRecordSqlDao pitRecordSqlDao;

	private PhotoReceiptSqlLoger() {
		log.info("初始化MySQL存储PhotoReceiptSqlLoger线程");
		pitRecordSqlDao = new PitRecordSqlDao("127.0.0.1");
		this.addQuartzJobs();
	}

	public void startThread() {
		ExecutorService executer = Executors.newSingleThreadExecutor();
		executer.execute(_instance);

	}

	/**
	 * 
	 * @param monthCount
	 */
	public void clearMysqlDatas(int monthCount) {
		String preDate = "";
		try {
			preDate = CalUtils.getPreSerivalDaysShort2(CalUtils.getStringDateShort2(), monthCount);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			log.error("clearMysqlDatas:", e);
		}
		String sqls = "delete from pit_phototable where pitDate<'" + preDate + "'";
		log.info("clearMysqlDatas:sqls = " + sqls);
		pitRecordSqlDao.deleteSQL(sqls);
	}

	/**
	 * 添加定时扫描人脸本地数据，定期删除，保留10天数据
	 * 
	 * @return
	 */
	private int addQuartzJobs() {
		int retVal = 1;
		try {
			String cronStr = "0 0 23 * * ?";
			SchedulerFactory sf = new StdSchedulerFactory();
			Scheduler sched = sf.getScheduler(); // 初始化调度器
			JobDetail job = JobBuilder.newJob(ClearPITFaceFileJob.class).withIdentity("ClearPITFaceFileJob", "pitcheckGroup").build();
			CronTrigger trigger = (CronTrigger) TriggerBuilder.newTrigger().withIdentity("ClearPITFaceFileTrigger", "pitcheckGroup")
					.withSchedule(CronScheduleBuilder.cronSchedule(cronStr)).build(); // 设置触发器
			Date ft = sched.scheduleJob(job, trigger); // 设置调度作业
			log.debug(job.getKey() + " has been scheduled to run at: " + ft + " and repeat based on expression: " + trigger.getCronExpression());
			sched.start(); // 开启调度任务，执行作业

		} catch (Exception ex) {
			retVal = 0;
			ex.printStackTrace();
		}
		return retVal;
	}

	public void offer(PhotoReceiptBean data) {
		photoReceiptQueue.add(data);
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

	private LinkedBlockingQueue<PhotoReceiptBean> photoReceiptQueue = new LinkedBlockingQueue<PhotoReceiptBean>();

	@Override
	public void run() {
		while (true) {
			try {
				PhotoReceiptBean pvd = photoReceiptQueue.take();

				String sqls = pitRecordSqlDao.createInsertPhotoReceiptSqlstr(pvd, pvd.getIdcardImagePath(), pvd.getFaceImagePath(), pvd.getFrameImagePath());
				log.info("createInsertPhotoReceiptSqlstr: sqls = " + sqls);
				if (sqls != null && !sqls.equals("")) {
					pitRecordSqlDao.insertSQL(sqls);
				}
			} catch (Exception e) {
				log.error("PitRecordSqlLoger run loop", e);
			}
		}

	}

}
