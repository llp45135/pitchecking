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

import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.device.DeviceException;
import com.rxtec.pitchecking.device.FirstGateDevice;
import com.rxtec.pitchecking.device.SecondGateDevice;
import com.rxtec.pitchecking.device.IDCardDevice;
import com.rxtec.pitchecking.device.LightControlBoard;
import com.rxtec.pitchecking.device.QRDevice;
import com.rxtec.pitchecking.event.IDeviceEvent;
import com.rxtec.pitchecking.event.ScreenElementModifyEvent;
import com.rxtec.pitchecking.mq.JmsReceiver;
import com.rxtec.pitchecking.picheckingservice.PITData;
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
		deviceEventQueue.offer(e);
	}

	private void processEvent(IDeviceEvent e) {
		if (e.getEventType() == Config.QRReaderEvent && e.getData() != null) {
			/**
			 * 二维码读卡器读到数据
			 */
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
			// 打开第一道门
//			FirstGateDevice.getInstance().openFirstDoor();
			// 停止寻卡
			this.setDeviceReader(false);

			log.debug("票证核验通过，停止寻卡，开始人证比对");
			TicketCheckScreen.getInstance()
					.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketVerifySucc.getValue(),
							ticketVerifier.getTicket(), ticketVerifier.getIdCard(), null));
			// 开始进行人脸检测和比对
			verifyFace(ticketVerifier.getIdCard());

			ticketVerifier.reset();

			// 开始寻卡
			this.setDeviceReader(true);
			log.debug("人证比对完成，开始寻卡");

		} else if (ticketVerifyResult == Config.TicketVerifyWaitInput) { // 等待票证验证数据
			if (!(ticketVerifier.getTicket() == null && ticketVerifier.getIdCard() == null)) {
				TicketCheckScreen.getInstance()
						.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketVerifyWaitInput.getValue(),
								ticketVerifier.getTicket(), ticketVerifier.getIdCard(), null));
			}
		} else if (ticketVerifyResult == Config.TicketVerifyIDFail) { // 票证验证失败
			TicketCheckScreen.getInstance()
					.offerEvent(new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketVerifyIDFail.getValue(),
							ticketVerifier.getTicket(), ticketVerifier.getIdCard(), null));
			ticketVerifier.reset();
		} else if (ticketVerifyResult == Config.TicketVerifyStationRuleFail) { // 车票未通过车站业务规则
			TicketCheckScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowTicketVerifyStationRuleFail.getValue(),
							ticketVerifier.getTicket(), ticketVerifier.getIdCard(), null));
			ticketVerifier.reset();
		}
	}

	private void verifyFace(IDCard idCard) {
		PITData picData = verifyFaceTask.beginCheckFace(idCard);
	}

	// 启动设备
	private void startDevice() throws DeviceException {
		log.debug("启动设备");
		if (this.startIDDevice() != 1) {
//			FirstGateDevice.getInstance().LightEntryCross();
			return;
		}
		if (this.startQRDevice() != 1) {
//			FirstGateDevice.getInstance().LightEntryCross();
			return;
		}
//		if (this.startGateDevice() != 1) {
//			FirstGateDevice.getInstance().LightEntryCross();
//			return;
//		}
//		if (this.startLED() != 1) {
//			FirstGateDevice.getInstance().LightEntryCross();
//			return;
//		}

		// this.startMQReceiver();

		ScheduledExecutorService idReaderScheduler = Executors.newScheduledThreadPool(1);
		idReaderScheduler.scheduleWithFixedDelay(IDReader.getInstance(), 0, 150, TimeUnit.MILLISECONDS);

		ExecutorService executor = Executors.newCachedThreadPool();
		executor.execute(QRReader.getInstance());
		//求助按钮事件处理
//		executor.execute(EmerButtonTask.getInstance());
		// 启动成功点亮绿色通行箭头
//		FirstGateDevice.getInstance().LightEntryArrow();
	}

	/**
	 * 
	 * @return
	 */
	private int startLED() {
		LightControlBoard cb = new LightControlBoard();
		if (cb.Cb_InitSDK() == 0) {
			if (cb.Cb_OpenCom(Config.getInstance().getLightBoardComm()) == 0) {
				if (cb.Cb_LightUnitOn(0, 30) != 0) {
					return 0;
				}
			} else {
				return 0;
			}
		} else {
			return 0;
		}
		return 1;
	}

	/**
	 * 启动通行控制板
	 * 
	 * @return
	 */
	private int startGateDevice() {
		try {
			FirstGateDevice.getInstance().connect(DeviceConfig.getInstance().getGateCrtlPort());
			SecondGateDevice.getInstance().connect(DeviceConfig.getInstance().getGteCrtlSecondPort());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	/**
	 * 启动activemq
	 */
	private void startMQReceiver() {
		JmsReceiver receiver = new JmsReceiver();
		try {
			receiver.receiveMessage();
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			log.error("DeviceEventListener startMQReceiver" + ex);
		}
	}

	private int startIDDevice() {
		IDReader idReader = IDReader.getInstance();
		// log.debug("getIdDeviceStatus=="+DeviceConfig.getInstance().getIdDeviceStatus());
		if (DeviceConfig.getInstance().getIdDeviceStatus() != DeviceConfig.idDeviceSucc) {
			log.debug("二代证读卡器异常,请联系维护人员!");
			TicketCheckScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowIDDeviceException.getValue(), null, null, null));
			setDeviceReader(false);
			return 0;
		}
		return 1;
	}

	private int startQRDevice() {
		QRReader qrReader = QRReader.getInstance();
		CommUtil.sleep(1000);
		log.debug("getQrdeviceStatus==" + DeviceConfig.getInstance().getQrdeviceStatus());
		if (DeviceConfig.getInstance().getQrdeviceStatus() != DeviceConfig.qrDeviceSucc) {
			log.debug("二维码扫描器异常,请联系维护人员!");
			TicketCheckScreen.getInstance().offerEvent(
					new ScreenElementModifyEvent(0, ScreenCmdEnum.ShowQRDeviceException.getValue(), null, null, null));

			setDeviceReader(false);
			return 0;
		}
		return 1;
	}

	private int pitStatus = -1;

	public int getPitStatus() {
		return pitStatus;
	}

	public void setPitStatus(int pitStatus) {
		this.pitStatus = pitStatus;
	}

	/**
	 * 读卡器是否开始寻卡
	 * 
	 * @param isRead
	 */
	public void setDeviceReader(boolean isRead) {
		if (isRead) {
			log.debug("读卡器开始寻卡");
			IDReader.getInstance().start();
			QRReader.getInstance().start();
		} else {
			log.debug("读卡器停止寻卡");
			IDReader.getInstance().stop();
			QRReader.getInstance().stop();
		}

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