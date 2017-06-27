package com.rxtec.pitchecking.socket.pitevent;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
import com.rxtec.pitchecking.utils.CommUtil;

public class FaceResultReceiver implements Runnable {
	private Logger log = LoggerFactory.getLogger("FaceResultReceiver");
	private static FaceResultReceiver _instance = new FaceResultReceiver();
	private ServerSocket serverSocket;

	private FaceResultReceiver() {
		try {
			serverSocket = new ServerSocket(Config.getInstance().getFaceResultReceiveSocketPort());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
	}

	public static synchronized FaceResultReceiver getInstance() {
		if (_instance == null)
			_instance = new FaceResultReceiver();
		return _instance;
	}

	public void startSubscribing() {
		ExecutorService executer = Executors.newCachedThreadPool();
		executer.execute(this);
		executer.shutdown();
		
//		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//		scheduler.scheduleWithFixedDelay(this, 0, 50, TimeUnit.MILLISECONDS);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		log.info("Start FaceResultReceiver By Socket!");
		Socket socket = null;
		while (true) {
			CommUtil.sleep(50);
			try {
				socket = serverSocket.accept();
			} catch (Exception e) {
				log.error("serverSocket.accept", e);
			}

			DataInputStream dis = null;
			ByteArrayInputStream bais = null;
			ObjectInputStream ois = null;
			try {
				socket.setSendBufferSize(64 * 1024);
				socket.setReceiveBufferSize(64 * 1024);

				dis = new DataInputStream(socket.getInputStream());

				byte[] data = new byte[256 * 1024]; // 一次读取一个byte

				int nIdx = 0;
				int nTotalLen = data.length;
				int nReadLen = 0;

				while (nIdx < nTotalLen) {
					nReadLen = dis.read(data, nIdx, nTotalLen - nIdx);

					if (nReadLen > 0) {
						nIdx = nIdx + nReadLen;
					} else {
						break;
					}
				}

				bais = new ByteArrayInputStream(data);
				ois = new ObjectInputStream(bais);

				PITVerifyData fd = (PITVerifyData) ois.readObject();

				if (fd.getVerifyResult() >= Config.getInstance().getFaceCheckThreshold()) {
					// 比对成功的offer到成功队列
					log.info("收到比对成功的结果!verifyResult = "+fd.getVerifyResult());
					FaceCheckingService.getInstance().offerPassFaceData(fd);
				} else {
					// 比对失败的offer到失败队列
					FaceCheckingService.getInstance().offerFailedFaceData(fd);
				}

				data = null;
				ois.close();
				bais.close();
				dis.close();
				
				socket.close();
			} catch (IOException | ClassNotFoundException e) {
				Log.error("publishArrived", e);
			} finally {
				try {
					if (ois != null)
						ois.close();
					if (bais != null)
						bais.close();
					if (dis != null)
						dis.close();
					if (socket != null)
						socket.close();
				} catch (Exception e) {
					log.error("publishArrived", e);
				}
			}
		}
	}

}
