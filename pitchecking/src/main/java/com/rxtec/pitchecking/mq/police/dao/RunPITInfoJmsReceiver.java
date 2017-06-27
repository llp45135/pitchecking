package com.rxtec.pitchecking.mq.police.dao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.JMSException;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.db.mysql.PhotoReceiptSqlLoger;
import com.rxtec.pitchecking.db.mysql.PitRecordSqlLoger;

public class RunPITInfoJmsReceiver {

	public static void main(String[] args) throws JMSException, Exception {
		// TODO Auto-generated method stub

		if (Config.getInstance().getIsUseMySQLDB() == 1) {
			if (Config.getInstance().getIsUseWharfMQ() == 1) {
				PhotoReceiptSqlLoger.getInstance().startThread();
			} else {
				PitRecordSqlLoger.getInstance().startThread();
			}
		}

		ExecutorService executer = Executors.newCachedThreadPool();
		PITInfoJmsReceiverTask jmsReceiverTask = new PITInfoJmsReceiverTask();
		executer.execute(jmsReceiverTask);
	}

}
