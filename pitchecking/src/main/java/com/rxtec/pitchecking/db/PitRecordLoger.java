package com.rxtec.pitchecking.db;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;

public class PitRecordLoger implements Runnable {
	private Logger log = LoggerFactory.getLogger("PitRecordLoger");
	private static PitRecordLoger _instance = new PitRecordLoger();
	public static synchronized PitRecordLoger getInstance() {
		if (_instance == null)
			_instance = new PitRecordLoger();
		return _instance;
	}
	private PitRecordDAO dao;
	
	private PitRecordLoger(){
		log.debug("初始化MongoDB存储Photos线程");
		dao = new PitRecordDAO();
	}
	public void startThread(){
		ExecutorService executer = Executors.newSingleThreadExecutor();
		executer.execute(_instance);

	}
	public void offer(PITVerifyData data){
		faceVerifyDataQueue.add(data);
	}
	
	
	public void clearExpirationData(){
		int days = Config.getInstance().getFaceLogRemainDays();
		SimpleDateFormat sFormat = new SimpleDateFormat("yyyyMMdd");

		Date today = new Date();
		String dn = sFormat.format(new Date(today.getTime() - days * 24 * 60 * 60 * 1000));
		log.debug("ClearExpirationData rows = " + dao.deleteRecords(dn));
		
		
	}
	
	
	
	private LinkedBlockingQueue<PITVerifyData> faceVerifyDataQueue = new LinkedBlockingQueue<PITVerifyData>();
	@Override
	public void run() {
		while(true){
			try {
				PITVerifyData pvd = faceVerifyDataQueue.take();
				PitRecord rec = new PitRecord(pvd);
				dao.save(rec);
			} catch (Exception e) {
				log.error("PitRecordLoger run loop",e);
			}
		}
		
	}
	
}
