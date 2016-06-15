package com.rxtec.pitchecking;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.DeviceException;
import com.rxtec.pitchecking.device.GateDeviceManager;
import com.rxtec.pitchecking.device.IDCardDevice;
import com.rxtec.pitchecking.device.QRDevice;
import com.rxtec.pitchecking.event.IDeviceEvent;
import com.rxtec.pitchecking.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.picheckingservice.PICData;

public class DeviceEventListener implements Runnable {

	private static DeviceEventListener _instance = new DeviceEventListener();
	private ExecutorService executor = Executors.newCachedThreadPool();
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");
	private TicketVerify ticketVerifier = new TicketVerify();

	private DeviceEventListener() {
		try {
			startDevice();
		} catch (DeviceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// try {
		// startListenEvent();
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	public static DeviceEventListener getInstance() {
		return _instance;
	}

	private LinkedBlockingQueue<IDeviceEvent> deviceEventQueue = new LinkedBlockingQueue<IDeviceEvent>(20);

	public void offerDeviceEvent(IDeviceEvent e) {
		// 队列满了需要处理Exception,注意！
		// log.debug("生产者准备生产event");
		deviceEventQueue.offer(e);
	}

	public void takeDeviceEvent() throws InterruptedException {
		// 队列满了需要处理Exception,注意！
		log.debug("消费者准备消费event");
		IDeviceEvent e = deviceEventQueue.take();
		log.debug("消费者取到新的event==" + e + ",e.getEventType==" + e.getEventType());

		this.processEvent(e);
	}


	private void processEvent(IDeviceEvent e) {
		if (e.getEventType() == Config.QRReaderEvent && e.getData() != null) {
			/**
			 * 二维码读卡器读到数据
			 */
			QRCodeEventTask qrtask = new QRCodeEventTask(e);
			Future<Integer> result = executor.submit(qrtask);
			ticketVerifier.setTicket((Ticket)e.getData());
			
			verifyTicket();
		} else if (e.getEventType() == Config.IDReaderEvent && e.getData() != null) {
			/**
			 * 二代证读卡器读到了新数据
			 */
			ticketVerifier.setIdCard((IDCard)e.getData());
			verifyTicket();
		}
	}
	
	private void verifyTicket(){
		if(ticketVerifier.verify() == Config.TicketVerifySucc){
			GateDeviceManager.getInstance().openFirstDoor();
			IDReader.getInstance().stop();
			QRReader.getInstance().stop();
			verifyFace(ticketVerifier.getIdCard());
			ticketVerifier.reset();
			IDReader.getInstance().start();
			QRReader.getInstance().start();
		}else{
			
		}
	}
	
	
	private void verifyFace(IDCard idCard){
		VerifyFaceTask idtask = new VerifyFaceTask(idCard);
		Future<PICData> future =  executor.submit(idtask);
		try {
			PICData picData = future.get();
			if(picData != null){
				GateDeviceManager.getInstance().openThirdDoor();
			}else{
				GateDeviceManager.getInstance().openSecondDoor();
			}
		} catch (InterruptedException | ExecutionException e) {
			log.error("verifyFace",e);
		}
		
	}
	
	

	// 启动设备
	private void startDevice() throws DeviceException {
		log.debug("启动设备");

		IDCardDevice.getInstance();

		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleWithFixedDelay(new IDReader(), 0, 100, TimeUnit.MILLISECONDS);
		QRDevice qrDevice = QRDevice.getInstance();
		scheduler.scheduleWithFixedDelay(qrDevice, 0, 100, TimeUnit.MILLISECONDS);

	}

	private int pitStatus = -1;

	public int getPitStatus() {
		return pitStatus;
	}

	public void setPitStatus(int pitStatus) {
		this.pitStatus = pitStatus;
	}

	@Override
	public void run() {
		// log.debug("消费者准备消费event");
		IDeviceEvent e;
		try {
			e = deviceEventQueue.take();
			this.processEvent(e);
		} catch (InterruptedException ex) {
			log.error("DeviceEventListener",ex);
		}

	}

}
