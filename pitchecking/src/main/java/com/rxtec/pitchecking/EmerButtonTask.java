package com.rxtec.pitchecking;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.SecondGateDevice;
import com.rxtec.pitchecking.domain.EmerButtonEvent;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.DateUtils;

public class EmerButtonTask implements Runnable {
	private Logger log = LoggerFactory.getLogger("EmerButtonTask");
	private static EmerButtonTask _instance;

	private LinkedBlockingQueue<EmerButtonEvent> emerButtonQueue;

	public static synchronized EmerButtonTask getInstance() {
		if (_instance == null) {
			_instance = new EmerButtonTask();
		}
		return _instance;
	}

	private EmerButtonTask() {
		emerButtonQueue = new LinkedBlockingQueue<EmerButtonEvent>(1);
	}

	public void offerEmerButtonEvent(EmerButtonEvent emerButtonEvent) {
		// offer方法在添加元素时，如果发现队列已满无法添加的话，会直接返回false
		emerButtonQueue.offer(emerButtonEvent);
	}

	public EmerButtonEvent takeEmerButtonEvent() throws InterruptedException {
		return emerButtonQueue.take();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

			try {
				EmerButtonEvent embe = EmerButtonTask.getInstance().takeEmerButtonEvent(); // take若队列为空，发生阻塞，等待有元素
				if (embe != null) {
					log.debug("求助按钮事件被触发，打开紧急电磁门。触发时间：" + embe.getEventTime());
					SecondGateDevice.getInstance().openSecondDoor();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public static void main(String args[]) {
		ExecutorService executor = Executors.newCachedThreadPool();
		// 求助按钮事件处理
		executor.execute(EmerButtonTask.getInstance());

		for (int i = 0; i < 11; i++) {
			CommUtil.sleep(3000);

			EmerButtonEvent embEvent = new EmerButtonEvent();
			embEvent.setEventNo("01");
			embEvent.setEventTime(DateUtils.getStringDateHaomiao());
			EmerButtonTask.getInstance().offerEmerButtonEvent(embEvent);
		}
	}

}
