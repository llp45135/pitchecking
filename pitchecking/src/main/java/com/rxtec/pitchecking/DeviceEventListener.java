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
import com.rxtec.pitchecking.utils.CommUtil;

public class DeviceEventListener implements Runnable {
	private VerifyFaceTask verifyFaceTask = new VerifyFaceTask();
	private static DeviceEventListener _instance = new DeviceEventListener();
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");
	private TicketVerify ticketVerifier = new TicketVerify();


	private DeviceEventListener() {
		try {
			startDevice();
		} catch (DeviceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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

	// public void takeDeviceEvent() throws InterruptedException {
	// // 队列满了需要处理Exception,注意！
	// log.debug("消费者准备消费event");
	// IDeviceEvent e = deviceEventQueue.take();
	// log.debug("消费者取到新的event==" + e + ",e.getEventType==" + e.getEventType());
	// this.processEvent(e);
	// }

	private void processEvent(IDeviceEvent e) {
		if (e.getEventType() == Config.QRReaderEvent && e.getData() != null) {
			/**
			 * 二维码读卡器读到数据
			 */
			// QRCodeEventTask qrtask = new QRCodeEventTask(e);
			// Future<Integer> result = executor.submit(qrtask);
			ticketVerifier.setTicket((Ticket) e.getData());

			verifyTicket();
		} else if (e.getEventType() == Config.IDReaderEvent && e.getData() != null) {
			/**
			 * 二代证读卡器读到了新数据
			 */
			ticketVerifier.setIdCard((IDCard) e.getData());
			verifyTicket();
		}
	}

	private void verifyTicket() {
		int ticketVerifyResult = ticketVerifier.verify();// 核验结果

		if (ticketVerifyResult == Config.TicketVerifySucc) { // 核验成功
			// GateDeviceManager.getInstance().openFirstDoor();
			
			IDReader.getInstance().stop();
			QRReader.getInstance().stop();
			
			log.debug("TicketVerifySucc$$$");
			TicketCheckScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0,
					ScreenCmdEnum.ShowTicketVerifySucc.getValue(), ticketVerifier.getTicket(), ticketVerifier.getIdCard(), null));
			 verifyFace(ticketVerifier.getIdCard());
//			CommUtil.sleep(5000);
			ticketVerifier.reset();
			log.debug("$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			IDReader.getInstance().start();
			QRReader.getInstance().start();
			log.debug("#############################");
			
		} else if (ticketVerifyResult == Config.TicketVerifyWaitInput) { // 等待票证验证数据
			if (!(ticketVerifier.getTicket() == null && ticketVerifier.getIdCard() == null)) {
				TicketCheckScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0,
						ScreenCmdEnum.ShowTicketVerifyWaitInput.getValue(), ticketVerifier.getTicket(), ticketVerifier.getIdCard(), null));
			}
		} else if (ticketVerifyResult == Config.TicketVerifyIDFail) { // 票证验证失败
			TicketCheckScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0,
					ScreenCmdEnum.ShowTicketVerifyIDFail.getValue(), ticketVerifier.getTicket(), ticketVerifier.getIdCard(), null));
			ticketVerifier.reset();
		} else if (ticketVerifyResult == Config.TicketVerifyStationRuleFail) { // 车票未通过车站业务规则
			TicketCheckScreen.getInstance().offerEvent(new ScreenElementModifyEvent(0,
					ScreenCmdEnum.ShowTicketVerifyStationRuleFail.getValue(), ticketVerifier.getTicket(), ticketVerifier.getIdCard(), null));
			ticketVerifier.reset();
		}
	}

	private void verifyFace(IDCard idCard) {
		PICData picData = verifyFaceTask.beginCheckFace(idCard);
//		if (picData != null) {
//			GateDeviceManager.getInstance().openThirdDoor();
//		} else {
//			GateDeviceManager.getInstance().openSecondDoor();
//		}
	}

	// 启动设备
	private void startDevice() throws DeviceException {
		log.debug("启动设备");
		// IDCardDevice.getInstance();
		IDReader idReader = IDReader.getInstance();
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleWithFixedDelay(idReader, 0, 150, TimeUnit.MILLISECONDS);
		QRDevice qrDevice = QRDevice.getInstance();
		// scheduler.scheduleWithFixedDelay(qrDevice, 0, 100,
		// TimeUnit.MILLISECONDS);
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
			log.error("DeviceEventListener", ex);
		}
	}
}
