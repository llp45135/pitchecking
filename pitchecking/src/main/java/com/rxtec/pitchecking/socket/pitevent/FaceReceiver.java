package com.rxtec.pitchecking.socket.pitevent;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.internal.formatter.comment.CommentFormatterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
import com.rxtec.pitchecking.utils.BASE64Util;
import com.rxtec.pitchecking.utils.CommUtil;

public class FaceReceiver implements Runnable {
	private Logger log = LoggerFactory.getLogger("FaceReceiver");
	private static FaceReceiver _instance = new FaceReceiver();
	private ServerSocket serverSocket;

	private FaceReceiver() {
		// TODO Auto-generated constructor stub
		try {
			serverSocket = new ServerSocket(Config.getInstance().getFaceReceiveSocketPort());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
	}

	public static synchronized FaceReceiver getInstance() {
		if (_instance == null)
			_instance = new FaceReceiver();
		return _instance;
	}

	public void startSubscribing() {
		ExecutorService executer = Executors.newCachedThreadPool();
		executer.execute(this);
		executer.shutdown();

		// ScheduledExecutorService scheduler =
		// Executors.newScheduledThreadPool(1);
		// scheduler.scheduleWithFixedDelay(this, 0, 50, TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		log.info("Start FaceReceiver By Socket!LocalSocketAddress = " + serverSocket.getLocalSocketAddress());
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

				if (fd.getIdCardImg() != null && fd.getIdCardImg().length > 0) {
					fd.setIdCardImgByBase64(BASE64Util.encryptBASE64(fd.getIdCardImg()));
				}
				if (fd.getFaceImg() != null && fd.getFaceImg().length > 0) {
					fd.setFaceImgByBase64(BASE64Util.encryptBASE64(fd.getFaceImg()));
				}
				if (fd.getFrameImg() != null && fd.getFrameImg().length > 0) {
					fd.setFrameImgByBase64(BASE64Util.encryptBASE64(fd.getFrameImg()));
				}

				// log.info("收到数据,fd.getIdNo = " + fd.getIdNo() + ",fd.usetime =
				// " + fd.getUseTime() + ",faceImg.length = "
				// + fd.getFaceImg().length + ",frameImg = " +
				// fd.getFrameImg().length);
				// if (fd.getIdCardImg() != null) {
				// log.info("fd.idcardImg.length = " +
				// fd.getIdCardImg().length);
				// } else {
				// log.info("fd.idcardImg.length = null");
				// }
				/**
				 * 收到第一个后置摄像头送来的人脸时，首先做一次清理
				 */
				// log.info("DeviceConfig.getInstance().isInTracking()=="+DeviceConfig.getInstance().isInTracking());
				if (DeviceConfig.getInstance().isInTracking()) {
					if (!FaceCheckingService.getInstance().isReceiveBehindCameraFace() && fd.getCameraPosition() == 2) {
						FaceCheckingService.getInstance().setReceiveBehindCameraFace(true);
						FaceCheckingService.getInstance().resetFaceDataQueue();
						// Log.debug("收到第一个后置摄像头送来的人脸时，首先做一次清理");
					}
					// log.info("检脸状态下，收到人脸,fd.CameraPosition=="+fd.getCameraPosition());
					FaceCheckingService.getInstance().offerFaceVerifyData(fd); // 加入待验证队列
				} else {
					if (FaceCheckingService.getInstance().isDealNoneTrackFace() && fd.getCameraFaceMode() == 2) {
						// log.info("非检脸状态下，再次收到人脸");
						FaceCheckingService.getInstance().offerFaceVerifyData(fd); // 加入待验证队列
					}
				}

				data = null;
				ois.close();
				bais.close();
				dis.close();

				socket.close();
			} catch (Exception e) {
				log.error("publishArrived", e);
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

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Config.getInstance();
		FaceReceiver.getInstance().startSubscribing();
	}
}