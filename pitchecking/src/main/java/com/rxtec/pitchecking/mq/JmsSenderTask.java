package com.rxtec.pitchecking.mq;

import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rxtec.pitchecking.domain.FailedFace;
import com.rxtec.pitchecking.utils.CommUtil;

public class JmsSenderTask implements Runnable {
	private Logger log = LoggerFactory.getLogger("JmsSenderTask");
	private static JmsSenderTask _instance;
	private LinkedBlockingQueue<FailedFace> failedFaceQueue;
	private JmsSender jmsSender = null;

	public static synchronized JmsSenderTask getInstance() {
		if (_instance == null) {
			_instance = new JmsSenderTask();
		}
		return _instance;
	}

	private JmsSenderTask() {
		failedFaceQueue = new LinkedBlockingQueue<FailedFace>(1);
	}

	public void offerFailedFace(FailedFace failedFace) {
		// offer方法在添加元素时，如果发现队列已满无法添加的话，会直接返回false
		boolean flag = failedFaceQueue.offer(failedFace);
		if (flag) {
			log.debug("offer failedface succed!");
		} else {
			log.debug("offer failedface failed!");
		}
	}

	public FailedFace takeFailedFace() throws InterruptedException {
		return failedFaceQueue.take();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if (jmsSender == null) {
			try {
				jmsSender = new JmsSender();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				jmsSender = null;
				log.error("JmsSenderTask JmsSender 实例化失败");
			}
			if (jmsSender != null) {
				log.info("JmsSenderTask JmsSender 实例化已经成功");
			}
		}

		if (jmsSender != null) {
			FailedFace faceData = null;
			try {
				faceData = JmsSenderTask.getInstance().takeFailedFace();  // take若队列为空，发生阻塞，等待有元素
			} catch (InterruptedException e) {
				log.error("takeFailedFace失败");
			} 
			log.info("JmsSenderTask faceData==" + faceData);
			if (faceData != null) {
				try {
					jmsSender.sendMessage("map", "", faceData);
					log.debug("takeFailedFace sendMessage成功");
				} catch (Exception e) {
					log.error("takeFailedFace sendMessage失败");
				}
			}
		}
	}

}
