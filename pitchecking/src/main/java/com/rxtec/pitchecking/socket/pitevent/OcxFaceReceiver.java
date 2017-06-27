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
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.mq.RemoteMonitorPublisher;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
import com.rxtec.pitchecking.utils.BASE64Util;
import com.rxtec.pitchecking.utils.CalUtils;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.ImageToolkit;

public class OcxFaceReceiver implements Runnable {
	private Logger log = LoggerFactory.getLogger("OcxFaceReceiver");
	private static OcxFaceReceiver _instance = new OcxFaceReceiver();
	private ServerSocket serverSocket;

	private OcxFaceReceiver() {
		// TODO Auto-generated constructor stub
		try {
			serverSocket = new ServerSocket(Config.getInstance().getOcxFaceReceiveSocketPort());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("", e);
		}
	}

	public static synchronized OcxFaceReceiver getInstance() {
		if (_instance == null)
			_instance = new OcxFaceReceiver();
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
		log.info("Start OcxFaceReceiver By Socket!LocalSocketAddress = " + serverSocket.getLocalSocketAddress());
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

				byte[] totalArray = new byte[4]; // 总数据长度
				System.arraycopy(data, 1, totalArray, 0, 4);
				String totalStr = CommUtil.bytesToHexString(totalArray);
				int total = CommUtil.hexstringToInt(totalStr);
				// log.info(totalStr + ",数据总长 = " + total);

				byte[] angleArray = new byte[2]; // 人脸角度
				System.arraycopy(data, 5, angleArray, 0, 2);
				String angleStr = CommUtil.bytesToHexString(angleArray);
				// log.info("角度 = " + CommUtil.hexstringToInt(angleStr));

				byte[] frameArray = new byte[4]; // 半身照数据长度
				System.arraycopy(data, 7, frameArray, 0, 4);
				String frameStr = CommUtil.bytesToHexString(frameArray);
				int frameLength = CommUtil.hexstringToInt(frameStr);
				// log.info(frameStr + ",现场照总长 = " + frameLength);

				byte[] faceArray = new byte[4]; // 人脸照数据长度
				System.arraycopy(data, 11, faceArray, 0, 4);
				String faceStr = CommUtil.bytesToHexString(faceArray);
				int faceLength = CommUtil.hexstringToInt(faceStr);
				// log.info(faceStr + ",人脸照总长 = " + faceLength);

				int tt = faceLength + frameLength + 10;
				// log.info("ll = " + tt);
				if (tt == total) {
					byte[] framePhotoArray = new byte[frameLength];
					System.arraycopy(data, 15, framePhotoArray, 0, frameLength);
					// CommUtil.byte2image(framePhotoArray,
					// "D:/pitchecking/images/frame" +
					// CalUtils.getStringFullTimeHaomiao() + ".jpg");

					byte[] facePhotoArray = new byte[faceLength];
					System.arraycopy(data, 15 + frameLength, facePhotoArray, 0, faceLength);
					// CommUtil.byte2image(facePhotoArray,
					// "D:/pitchecking/images/face" +
					// CalUtils.getStringFullTimeHaomiao() + ".jpg");

					PITVerifyData fd = new PITVerifyData();

					fd.setPitStation(DeviceConfig.getInstance().getBelongStationCode());
					fd.setFaceImg(facePhotoArray);
					fd.setFrameImg(framePhotoArray);
					fd.setFaceImgByBase64(BASE64Util.encryptBASE64(facePhotoArray));
					fd.setFrameImgByBase64(BASE64Util.encryptBASE64(framePhotoArray));
					fd.setPitDate(CalUtils.getStringDateShort2());
					fd.setPitTime(CalUtils.getStringDate());
					fd.setFaceQuality(Integer.parseInt(angleStr, 16));

					if (FaceCheckingService.getInstance().getIdcard() != null) {
						IDCard idcard = FaceCheckingService.getInstance().getIdcard();
						fd.setIdNo(idcard.getIdNo());
						fd.setAge(idcard.getAge());
						fd.setPersonName(idcard.getPersonName());
						fd.setGender(idcard.getGender());
						fd.setIdBirth(idcard.getIDBirth());
						fd.setIdNation(idcard.getIDNation());
						fd.setIdDwelling(idcard.getIDDwelling());
						fd.setIdEfficb(idcard.getIDEfficb());
						fd.setIdEffice(idcard.getIDEffice());
						fd.setIdIssue(idcard.getIDIssue());
						
						fd.setIdCardImg(idcard.getCardImageBytes());
						fd.setIdCardImgByBase64(BASE64Util.encryptBASE64(idcard.getCardImageBytes()));
						fd.setIdCard(idcard);
					}

					FaceCheckingService.getInstance().offerFaceVerifyData(fd); // 加入待验证队列

					if (Config.getInstance().getIsUseManualMQ() == 1 && Config.getInstance().getIsSendFrame() == 1) {
						RemoteMonitorPublisher.getInstance().offerFrameData(framePhotoArray);
					}
				}

				// bais = new ByteArrayInputStream(data);
				// ois = new ObjectInputStream(bais);

				// PITVerifyData fd = (PITVerifyData) ois.readObject();

				/**
				 * 收到第一个后置摄像头送来的人脸时，首先做一次清理
				 */
				// log.info("DeviceConfig.getInstance().isInTracking()=="+DeviceConfig.getInstance().isInTracking());
				// if (DeviceConfig.getInstance().isInTracking()) {
				// if
				// (!FaceCheckingService.getInstance().isReceiveBehindCameraFace()
				// && fd.getCameraPosition() == 2) {
				// FaceCheckingService.getInstance().setReceiveBehindCameraFace(true);
				// FaceCheckingService.getInstance().resetFaceDataQueue();
				// // Log.debug("收到第一个后置摄像头送来的人脸时，首先做一次清理");
				// }
				// //
				// log.info("检脸状态下，收到人脸,fd.CameraPosition=="+fd.getCameraPosition());
				// FaceCheckingService.getInstance().offerFaceVerifyData(fd); //
				// 加入待验证队列
				// } else {
				// if (FaceCheckingService.getInstance().isDealNoneTrackFace()
				// && fd.getCameraFaceMode() == 2) {
				// // log.info("非检脸状态下，再次收到人脸");
				// FaceCheckingService.getInstance().offerFaceVerifyData(fd); //
				// 加入待验证队列
				// }
				// }

				data = null;
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
		OcxFaceReceiver.getInstance().startSubscribing();
	}
}