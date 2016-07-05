package com.rxtec.pitchecking.mq;

import java.io.File;

import javax.jms.JMSException;

import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.domain.FailedFace;
import com.rxtec.pitchecking.utils.CommUtil;

public class RunSender {
	public static void main(String[] args) throws JMSException, Exception {
		JmsSender sender = new JmsSender();
//		JmsReceiver receiver = new JmsReceiver();

		int i = 0;
		while (true) {
			
			 i++;
			// sender.sendMessage("text",String.valueOf(i)+" 这是一条jms信息!");
			// sender.close();
			Thread.sleep(20*1000);
			String msgType = "map";
			String msgStr = "";
			FailedFace failedFace = new FailedFace();
			failedFace.setIdNo(CommUtil.getRandomUUID());
			failedFace.setIpAddress(DeviceConfig.getInstance().getIpAddress());
			failedFace.setGateNo("01");
			
			File image = null; 
			image = new File("zp.jpg");
			failedFace.setCardImage(CommUtil.getBytesFromFile(image));
			
			File faceImage = new File("zhao.jpg");
			failedFace.setFaceImage(CommUtil.getBytesFromFile(faceImage));
			
			sender.sendMessage(msgType, msgStr, failedFace);
			System.out.println("已经发送第"+String.valueOf(i)+"条msg!!");
			Thread.sleep(20 * 1000);
			
			i++;
			failedFace.setIdNo(CommUtil.getRandomUUID());
			failedFace.setIpAddress("192.168.0.202");
			failedFace.setGateNo("02");
			faceImage = new File("lin.jpg");
			failedFace.setFaceImage(CommUtil.getBytesFromFile(faceImage));
			
			sender.sendMessage(msgType, msgStr, failedFace);
			System.out.println("已经发送第"+String.valueOf(i)+"条msg!!");
		}
	}
}
