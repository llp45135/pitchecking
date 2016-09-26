package com.rxtec.pitchecking;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.BarUnsecurity;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.device.TKQRDevice;
import com.rxtec.pitchecking.event.QRCodeReaderEvent;
import com.rxtec.pitchecking.gui.TicketCheckFrame;
import com.rxtec.pitchecking.utils.CalUtils;

public class BarCodeReader implements Runnable {
	private Logger log = LoggerFactory.getLogger("DeviceEventListener");
	private static BarCodeReader instance;
	private TKQRDevice tkqrDevice;
	private BarUnsecurity barUnsecurity = BarUnsecurity.getInstance();
	private int deviceStatus = Config.StartStatus;

	private TicketCheckFrame ticketFrame;

	public TicketCheckFrame getTicketFrame() {
		return ticketFrame;
	}

	public void setTicketFrame(TicketCheckFrame ticketFrame) {
		this.ticketFrame = ticketFrame;
	}

	public static synchronized BarCodeReader getInstance() {
		if (instance == null) {
			instance = new BarCodeReader();
		}
		return instance;
	}

	private BarCodeReader() {
		tkqrDevice = TKQRDevice.getInstance();
		int initRet = tkqrDevice.BAR_Init("3000");
		if (initRet == 0) {
			DeviceConfig.getInstance().setQrdeviceStatus(1);
		} else {
			DeviceConfig.getInstance().setQrdeviceStatus(0);
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		readBarCode(false);
	}

	/**
	 * 调用DLL接口方式读二维码
	 */
	private void readBarCode(boolean isReadData) {
		if (isReadData) {
			String barCode = tkqrDevice.BAR_ReadData("500");
			if (barCode != null && !barCode.equals("")) {
				String year = CalUtils.getStringDateShort2().substring(0, 4);
				Ticket ticket = null;
				try {
					ticket = barUnsecurity.buildTicket(barUnsecurity.uncompress(barCode, year));
					// 仅供测试用例
					ticket.setTrainDate(CalUtils.getStringDateShort2());
				} catch (NumberFormatException | UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					log.error("BarCodeReader buildBarCode:", e);
				}
				if (deviceStatus == Config.StartStatus && ticket != null) {
					if (DeviceConfig.getInstance().getVersionFlag() == 1) {// 以下为正式代码
						if (DeviceEventListener.getInstance().isDealDeviceEvent()) {
							QRCodeReaderEvent qrEvent = new QRCodeReaderEvent(Config.QRReaderEvent);
							qrEvent.setTicket(ticket);
							log.debug("offerDeviceEvent ticket");
							DeviceEventListener.getInstance().offerDeviceEvent(qrEvent);
						}
					} else {
						ticketFrame.showWaitInputContent(ticket, null, 2, 0); // 仅供测试用
					}
				}
			}
		} else {
			if (deviceStatus == Config.StartStatus) {
				String barCode = tkqrDevice.BAR_ReadData("500");
				if (barCode != null && !barCode.equals("")) {
					String year = CalUtils.getStringDateShort2().substring(0, 4);
					Ticket ticket = null;
					try {
						ticket = barUnsecurity.buildTicket(barUnsecurity.uncompress(barCode, year));
						// 仅供测试用例
						ticket.setTrainDate(CalUtils.getStringDateShort2());
					} catch (NumberFormatException | UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						log.error("BarCodeReader buildBarCode:", e);
					}
					if (ticket != null) {
						if (DeviceConfig.getInstance().getVersionFlag() == 1) {// 以下为正式代码
							if (DeviceEventListener.getInstance().isDealDeviceEvent()) {
								QRCodeReaderEvent qrEvent = new QRCodeReaderEvent(Config.QRReaderEvent);
								qrEvent.setTicket(ticket);
								log.debug("offerDeviceEvent ticket");
								DeviceEventListener.getInstance().offerDeviceEvent(qrEvent);
							}
						} else {
							ticketFrame.showWaitInputContent(ticket, null, 2, 0); // 仅供测试用
						}
					}
				}
			}
		}
	}

	public void start() {
		deviceStatus = Config.StartStatus;
	}

	public void stop() {
		deviceStatus = Config.StopStatus;
	}

	public static void main(String[] args) {
		BarCodeReader barCodeReader = BarCodeReader.getInstance();
		if (DeviceConfig.getInstance().getQrdeviceStatus() != DeviceConfig.qrDeviceSucc) {
			System.out.println("二维码扫描器异常,请联系维护人员!");
		} else {
			TicketCheckFrame ticketFrame = new TicketCheckFrame();
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice[] gs = ge.getScreenDevices();
			gs[0].setFullScreenWindow(ticketFrame);
			barCodeReader.setTicketFrame(ticketFrame);
			ScheduledExecutorService qrReaderScheduler = Executors.newScheduledThreadPool(1);
			qrReaderScheduler.scheduleWithFixedDelay(barCodeReader, 0, 100, TimeUnit.MILLISECONDS);
		}
	}

}
