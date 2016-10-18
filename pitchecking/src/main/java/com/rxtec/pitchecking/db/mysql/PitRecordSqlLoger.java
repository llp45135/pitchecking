package com.rxtec.pitchecking.db.mysql;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.db.PitRecord;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;

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
		log.info("初始化MySQL存储Photos线程");
		pitRecordSqlDao = new PitRecordSqlDao();
	}

	public void startThread() {
		ExecutorService executer = Executors.newSingleThreadExecutor();
		executer.execute(_instance);

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
	// log.info("ClearExpirationData rows = " + dao.deleteRecords(dn));
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
