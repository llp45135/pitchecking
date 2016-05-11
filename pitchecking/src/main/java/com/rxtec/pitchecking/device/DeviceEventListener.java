package com.rxtec.pitchecking.device;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.event.IDeviceEvent;
import com.rxtec.pitchecking.device.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.picheckingservice.PICData;
import com.rxtec.pitchecking.picheckingservice.VerifyFaceTask;

public class DeviceEventListener implements Runnable{

	private static DeviceEventListener _instance = new DeviceEventListener();
	private ExecutorService executor = Executors.newCachedThreadPool();
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");

	private DeviceEventListener() {
		try {
			startDevice();
		} catch (DeviceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

//		 try {
//		 startListenEvent();
//		 } catch (InterruptedException e) {
//		 // TODO Auto-generated catch block
//		 e.printStackTrace();
//		 }
	}

	public static DeviceEventListener getInstance() {
		return _instance;
	}

	private LinkedBlockingQueue<IDeviceEvent> deviceEventQueue = new LinkedBlockingQueue<IDeviceEvent>(20);

	public void offerDeviceEvent(IDeviceEvent e) {
		// 队列满了需要处理Exception,注意！
//		log.debug("生产者准备生产event");
		deviceEventQueue.offer(e);
	}

	public void takeDeviceEvent() throws InterruptedException {
		// 队列满了需要处理Exception,注意！
		log.debug("消费者准备消费event");
		IDeviceEvent e = deviceEventQueue.take();
		log.debug("消费者取到新的event==" + e + ",e.getEventType==" + e.getEventType());

		this.processEvent(e);
	}

//	public void startListenEvent() throws InterruptedException {
//		while (true) {
//			// 如果没有新事件将一直阻塞等待到新设备事件
//			
//			log.debug("消费者准备消费event");
//			IDeviceEvent e = deviceEventQueue.take();
//			log.debug("消费者取到新的event==" + e + ",e.getEventType==" + e.getEventType());
//
//			this.processEvent(e);
//		}
//	}

	private void processEvent(IDeviceEvent e) {
		if (e.getEventType() == 0) {
			/**
			 * 二维码读卡器读到数据
			 */
			QRCodeEventTask qrtask = new QRCodeEventTask(e);
			Future<Integer> result = executor.submit(qrtask);
		} else if (e.getEventType() == DeviceEventTypeEnum.ReadIDCard.getValue()) {
			
			
			/**
			 * 二代证读卡器读到了新数据
			 */
			VerifyFaceTask idtask = new VerifyFaceTask(e);
			executor.submit(idtask);

		}
	}

	// 启动设备
	private void startDevice() throws DeviceException {
		log.debug("启动设备");
		
		IDCardDevice.getInstance();

		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleWithFixedDelay(new IDReader(), 0, 100, TimeUnit.MILLISECONDS);
		scheduler.scheduleWithFixedDelay(new QRReader(), 0, 100, TimeUnit.MILLISECONDS);
		


	}

	
	private  int pitStatus = -1;

	public int getPitStatus() {
		return pitStatus;
	}

	public void setPitStatus(int pitStatus) {
		this.pitStatus = pitStatus;
	}

	@Override
	public void run() {
//		log.debug("消费者准备消费event");
		IDeviceEvent e;
		try {
			e = deviceEventQueue.take();
//			log.debug("消费者取到新的event==" + e + ",e.getEventType==" + e.getEventType());

			this.processEvent(e);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
	
}
