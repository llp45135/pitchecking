package com.rxtec.pitchecking.task;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.rxtec.pitchecking.device.DeviceEventListener;
import com.rxtec.pitchecking.device.PITStatusEnum;
import com.rxtec.pitchecking.device.ScreenCmdEnum;
import com.rxtec.pitchecking.device.TicketCheckScreen;
import com.rxtec.pitchecking.device.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.picheckingservice.FaceData;
import com.rxtec.pitchecking.picheckingservice.FaceDetectionService;

public class RunningStatus {

	private final Lock idReaderLock = new ReentrantLock();
	private final Condition idReaderCondition = idReaderLock.newCondition();
	private final Lock qrReaderLock = new ReentrantLock();
	private final Condition qrReaderCondition = qrReaderLock.newCondition();
	private  int runningStatus = -1;

	public int getRunningStatus() {
		return runningStatus;
	}

	public void setRunningStatus(int runningStatus) {
		this.runningStatus = runningStatus;
	}

	public Lock getIdReaderLock() {
		return idReaderLock;
	}

	public Condition getIdReaderCondition() {
		return idReaderCondition;
	}

	public Lock getQrReaderLock() {
		return qrReaderLock;
	}

	public Condition getQrReaderCondition() {
		return qrReaderCondition;
	}

	
	
	private static RunningStatus instance = null;
	public static synchronized RunningStatus getInstance(){
		if(instance == null) instance = new RunningStatus();
		return instance;
	}
	
	private RunningStatus(){
	}
	
	

	public void signalIDReader(){
		if (idReaderLock.tryLock()) {
			try {
				idReaderCondition.signal();
			} finally {
				idReaderLock.unlock();
			}
		}
	}
	
}
