package com.rxtec.pitchecking.net;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.picheckingservice.PITData;

public class MulticastSender implements Runnable {

	private MulticastSocket mss = null;
	private InetAddress group = null;
	private int port;
	private Logger log = LoggerFactory.getLogger("MulticastSender");
	private static MulticastSender instance;
	private boolean isOK = false;

	private LinkedBlockingQueue<PITData> PITDataQueue = new LinkedBlockingQueue<PITData>(20);

	private MulticastSender() {
		try {
			initMulticastSocket();
			isOK = true;
		} catch (Exception e) {
			log.error("initMulticastSocket failed", e);
			isOK = false;
		}
		ExecutorService executor = Executors.newCachedThreadPool();
		executor.execute(this);

	}

	public static synchronized MulticastSender getInstance() {
		if (instance == null) {
			instance = new MulticastSender();
		}
		return instance;
	}

	private void initMulticastSocket() throws Exception {
		group = InetAddress.getByName(Config.getInstance().getMulticastAddress());// 组播地址
		port = Config.getInstance().getMulticastPort();
		mss = new MulticastSocket(port);
		mss.joinGroup(group);
		log.debug("MulticastSender init......");
	}

	public void send(PITData pitData) {
		PITDataQueue.offer(pitData);
	}

	private void sendAction(PITData pitData) {
		if (!isOK)
			return;
		if (pitData == null) {
			log.error("PITData == null");
			return;
		}
		try {
			String buffer = PitinfoUPDBuilder.buildPITDataJsonBytes(pitData);
			if (buffer != null && buffer.length() > 0) {
				
				DatagramPacket dp = new DatagramPacket(buffer.getBytes(), buffer.getBytes().length, group, port);
				mss.send(dp);
				log.debug("发送数据包给 " + group + ":" + port);
			}
		} catch (Exception e) {
			log.error("PitinfoUPDBuilder.buildPITDataJsonBytes failed",e);
			return;
		}

	}

	@Override
	public void run() {

		while (true) {
			try {
				PITData d = PITDataQueue.take();
				if (d != null)
					sendAction(d);
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void main(String s[]) throws IOException, InterruptedException {
		MulticastSender ms = new MulticastSender();

		BufferedImage idImg = ImageIO.read(new File("C:/pitchecking/idcardtest.jpg"));
		BufferedImage frame = ImageIO.read(new File("C:/pitchecking/llp.jpg"));

		PITData pitData = new PITData(frame);
		IDCard idCard = new IDCard();
		idCard.setCardImage(idImg);
		idCard.setAge(40);
		idCard.setIdNo("440111197209283012");
		idCard.setGender(1);
		pitData.setIdCard(idCard);
		pitData.setFaceCheckResult(0.6f);
		
		MulticastReceiver r = new MulticastReceiver();

		while (true) {
			ms.send(pitData);
			Thread.sleep(1000);
		}

	}



}
