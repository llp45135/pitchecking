package com.rxtec.pitchecking.socket.pitevent;

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

public class FaceSendThread implements Runnable {
	Logger log = LoggerFactory.getLogger("FaceSendThread");
	private FaceSender faceSender;

	public FaceSendThread() {
		// TODO Auto-generated constructor stub
		faceSender = FaceSender.getInstance();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	/**
	 * 
	 * @param o
	 * @return
	 */
	private byte[] serialObjToBytes(Object obj) {
		byte[] bytes = null;
		try {
			// object to bytearray
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(bo);
			oo.writeObject(obj);

			bytes = bo.toByteArray();

			bo.close();
			oo.close();
		} catch (Exception e) {
			System.out.println("translation" + e.getMessage());
			e.printStackTrace();
		}
		return bytes;
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
					PITVerifyData data = FaceCheckingService.getInstance().takeFaceVerifyData();
//					log.info("takeFaceVerifyData 送进比对进程!!!!!!!!!!"+data.getUseTime());
					byte[] buf = serialObjToBytes(data);
					if (buf == null)
						continue;
					this.putFailedFace(data); // 每次将待验证的人脸放入FailedFace 供active
												// mq调用
					// log.info("data.getIdcard=="+data.getIdCard());
					faceSender.sendFaceDataByTcp(buf);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error("PTVerifyThread running loop failed", e);
			}
		}
	}

}
