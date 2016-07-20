package com.rxtec.pitchecking.mq;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;

import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.domain.FailedFace;
import com.rxtec.pitchecking.utils.CommUtil;

public class RunSender {
	public static void main(String[] args) {
//		ExecutorService executer = Executors.newCachedThreadPool();
//		JmsSenderTask jmsSenderTask = JmsSenderTask.getInstance();
//		executer.execute(jmsSenderTask);
		
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleWithFixedDelay(JmsSenderTask.getInstance(), 0, 100, TimeUnit.MILLISECONDS);

		for (int i = 0; i < 2; i++) {
			CommUtil.sleep(10000);
			FailedFace failedFace = new FailedFace();
			failedFace.setGateNo("01");
			failedFace.setIpAddress(DeviceConfig.getInstance().getIpAddress());
			failedFace.setIdNo("111111111111111111");
			File image = null;
			image = new File("zp.jpg");
			failedFace.setCardImage(CommUtil.getBytesFromFile(image));

			File faceImage = new File("zhao.jpg");
			failedFace.setFaceImage(CommUtil.getBytesFromFile(faceImage));
			JmsSenderTask.getInstance().offerFailedFace(failedFace);
		}
	}
}
