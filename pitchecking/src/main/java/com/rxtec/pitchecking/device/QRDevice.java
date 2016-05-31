package com.rxtec.pitchecking.device;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xvolks.jnative.JNative;
import org.xvolks.jnative.Type;
import org.xvolks.jnative.exceptions.NativeException;
import org.xvolks.jnative.pointers.Pointer;
import org.xvolks.jnative.pointers.memory.MemoryBlockFactory;

import com.rxtec.pitchecking.device.event.IDeviceEvent;
import com.rxtec.pitchecking.device.event.QRCodeReaderEvent;
import com.vguang.VguangApi;

public class QRDevice implements Runnable {
	private Log log = LogFactory.getLog("QRDevice");
	private static Log logger = LogFactory.getLog("QRDevice");
	private static QRDevice _instance = new QRDevice();
	private JNative qrDeviceJNative = null;

	public static void main(String[] args) {

		QRDevice qrDeviceTest = QRDevice.getInstance();
		// ExecutorService executor = Executors.newCachedThreadPool();
		// executor.execute(qrDeviceTest);

		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleWithFixedDelay(qrDeviceTest, 0, 100, TimeUnit.MILLISECONDS);

	}

	private QRDevice() {
		JNative.setLoggingEnabled(true);
		initQRDevice();
	}

	public static QRDevice getInstance() {
		return _instance;
	}

	private LinkedBlockingQueue<IDeviceEvent> deviceEventQueue = new LinkedBlockingQueue<IDeviceEvent>(20);

	public void offerDeviceEvent(IDeviceEvent e) {
		// 队列满了需要处理Exception,注意！
		// log.debug("生产者准备生产event");
		deviceEventQueue.offer(e);
	}

	public Ticket doTicket(String instr, String year) throws NumberFormatException, UnsupportedEncodingException {
		byte[] outStrArray = uncompress(instr, year);
		// log.debug("outStrArray.length==" + outStrArray.length);
		Ticket ticket = null;
		// log.debug("outStrArray.length=="+outStrArray.length);
		ticket = buildTicket(outStrArray);
		// ticket.printTicket();
		return ticket;
	}

	/**
	 * 初始化二维码扫描器
	 */
	private void initQRDevice() {
		log.debug("初始化二维码扫描器...");
		try {
			qrDeviceJNative = new JNative("BAR2unsecurity.dll", "uncompress");
		} catch (NativeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 应用设置
		applySetting();
		// 打开设备
		VguangApi.openDevice();
	}

	/**
	 * 解析二维码数据为车票票面信息
	 * 
	 * @param ticketStr
	 * @return
	 * @throws NumberFormatException
	 * @throws UnsupportedEncodingException
	 */
	public Ticket buildTicket(byte[] ticketArray) throws NumberFormatException, UnsupportedEncodingException {
		Ticket ticket = null;
		if (ticketArray.length == 117) {
			ticket = new Ticket();
			String ticketStr = new String(ticketArray, "gbk");

			ticket.setTicketNo(ticketStr.substring(0, 7));
			ticket.setFromStationCode(ticketStr.substring(7, 10));
			ticket.setEndStationCode(ticketStr.substring(10, 13));
			ticket.setChangeStationCode(ticketStr.substring(13, 16));
			String trainCodeStr = ticketStr.substring(16, 24).trim();
			if (trainCodeStr.length() > 5) {
				ticket.setTrainCode(
						trainCodeStr.substring(0, 1) + String.valueOf(Integer.parseInt(trainCodeStr.substring(1))));
			} else {
				ticket.setTrainCode(String.valueOf(Integer.parseInt(trainCodeStr)));
			}
			ticket.setCoachNo(ticketStr.substring(24, 26));
			ticket.setSeatCode(ticketStr.substring(26, 27));
			ticket.setTicketType(ticketStr.substring(27, 29));
			ticket.setSeatNo(ticketStr.substring(29, 33));
			ticket.setTicketPrice(Integer.parseInt(ticketStr.substring(33, 38)));
			byte[] trainDateArray = new byte[8];
			for (int i = 0; i < 8; i++) {
				trainDateArray[i] = ticketArray[i + 38];
			}
			ticket.setTrainDate(new String(trainDateArray));
			ticket.setChangeFlag(ticketStr.substring(46, 47));
			ticket.setTicketSourceCenter(ticketStr.substring(47, 49));
			ticket.setBzsFlag(ticketStr.substring(49, 50));
			ticket.setSaleOfficeNo(ticketStr.substring(50, 57));
			ticket.setSaleWindowNo(ticketStr.substring(57, 60));
			ticket.setSaleDate(ticketStr.substring(60, 68));
			ticket.setCardType(ticketStr.substring(68, 70));
			ticket.setCardNo(ticketStr.substring(72, 90));
			;
			byte[] passengerNameArray = new byte[20];
			for (int i = 0; i < 20; i++) {
				passengerNameArray[i] = ticketArray[i + 90];
			}
			String ss = new String(passengerNameArray, "gbk");
			ticket.setPassengerName(ss.trim());
			byte[] specialArray = new byte[7];
			for (int i = 0; i < 7; i++) {
				specialArray[i] = ticketArray[i + 110];
			}
			ticket.setSpecialStr(new String(specialArray, "gbk"));
		}

		return ticket;
	}

	/**
	 * 二维码数据还原
	 * 
	 * @param instr
	 * @param year
	 * @return
	 */
	public byte[] uncompress(String instr, String year) {
		byte[] outStrArray = new byte[117];
		try {
			String retval = "-1";
			int i = 0;

			Pointer ticketStrPointer = new Pointer(MemoryBlockFactory.createMemoryBlock(117));
			qrDeviceJNative.setParameter(i++, instr);
			qrDeviceJNative.setParameter(i++, ticketStrPointer);
			qrDeviceJNative.setParameter(i++, Type.LONG, year);
			qrDeviceJNative.setRetVal(Type.LONG);
			qrDeviceJNative.invoke();

			retval = qrDeviceJNative.getRetVal();

			log.debug("uncompress: retval==" + retval);// 获取返回值

			if (retval.equals("0")) {
				// outStr = ticketStrPointer.getAsString();
				// outStr = new String(outStr.getBytes("UTF-8"),"GBK");
				outStrArray = ticketStrPointer.getMemory();
				log.debug("uncompress：outStrArray.length==" + outStrArray.length);
				String ticketStr = new String(outStrArray, "gbk");
				log.debug("uncompress: ticketArray==" + ticketStr + "##");
			}
			ticketStrPointer.dispose();

		} catch (NativeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return outStrArray;
	}

	/**
	 * 应用设置
	 */
	private void applySetting() {
		// 设置QR状态
		VguangApi.setQRable(true);
		// 设置DM状态
		VguangApi.setDMable(true);
		// 设置Bar状态
		VguangApi.setBarcode(true);

		// 设置解码间隔时间，单位毫秒
		VguangApi.setDeodeIntervalTime(300);

		// 设置自动休眠状态
		VguangApi.setAI(false);
		int aiLimit = 20;
		if (aiLimit < 1 || aiLimit > 64) {
			aiLimit = 20;
		}
		// 设置自动休眠灵敏度
		VguangApi.setAISensitivity(aiLimit);
		// 设置自动休眠响应时间，单位秒
		VguangApi.setAIResponseTime(30);

		// 设置扬声器状态
		VguangApi.setBeepable(true);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		// try {
		// // Thread.sleep(200);
		//
		// log.debug("QRDevice 准备从阻塞队列取Device事件");
		// IDeviceEvent e;
		// e = deviceEventQueue.take();
		// log.debug("QRDevice 从阻塞队列取到新的event==" + e);
		// if (e != null) {
		// log.debug("QRDevice e.hashCode==" + e.hashCode());
		// QRCodeReaderEvent qrcode = (QRCodeReaderEvent) e;
		// Ticket ticket = (Ticket) qrcode.getData();
		// ticket.printTicket();
		// }
		// } catch (InterruptedException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
	}
}
