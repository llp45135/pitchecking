package com.rxtec.pitchecking.mqtt.pitevent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.domain.FailedFace;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
import com.rxtec.pitchecking.utils.CommUtil;

public class PTVerifyThread implements Runnable {
	Logger log = LoggerFactory.getLogger("PTVerifyThread");
	private PTVerifySender ptVerifySender;

	public PTVerifyThread(String pidname) {
		// TODO Auto-generated constructor stub
		ptVerifySender = PTVerifySender.getInstance(pidname);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	/**
	 * 
	 * @param o
	 * @return
	 */
	private byte[] serialObjToBytes(Object o) {
		byte[] buf = null;
		ByteArrayOutputStream bos = null;
		ObjectOutputStream oos = null;
		try {
			bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(o);
			buf = bos.toByteArray();
			if (oos != null) {
				oos.close();
				oos = null;
			}
			if (bos != null) {
				bos.close();
				bos = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("serialObjToBytes", e);
		} finally {
			try {
				if (oos != null) {
					oos.close();
					oos = null;
				}
				if (bos != null) {
					bos.close();
					bos = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.error("serialObjToBytes", e);
			}
		}

		return buf;
	}

	/**
	 * 每次将待验证的人脸放入FailedFace
	 * 
	 * @param fd
	 */
	private void putFailedFace(PITVerifyData fd) {
//		FailedFace failedFace = new FailedFace();
//		failedFace.setIdNo(fd.getIdNo());
//		failedFace.setIpAddress(DeviceConfig.getInstance().getIpAddress());
//		failedFace.setGateNo(DeviceConfig.getInstance().getGateNo());
//		failedFace.setCardImage(fd.getIdCardImg());
//		failedFace.setFaceImage(fd.getFaceImg());
		FaceCheckingService.getInstance().setFailedFace(fd);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			CommUtil.sleep(50);
			try {
				// log.info("getIdcard=="+FaceCheckingService.getInstance().getIdcard()+",getIdCardPhotoRet=="+FaceCheckingService.getInstance().getIdCardPhotoRet());
				if (FaceCheckingService.getInstance().getIdcard() != null) {
					// log.info("takeFaceVerifyData 送进比对进程!!!!!!!!!!");
					PITVerifyData data = FaceCheckingService.getInstance().takeFaceVerifyData();
					if (data == null)
						continue;
					byte[] buf = serialObjToBytes(data);
					if (buf == null)
						continue;
					this.putFailedFace(data); // 每次将待验证的人脸放入FailedFace 供active
												// mq调用
					// log.info("data.getIdcard=="+data.getIdCard());
					ptVerifySender.sendMessage(buf);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error("PTVerifyThread running loop failed", e);
			}
		}
	}

}
