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
	
	
	private byte[] serialObjToBytes(Object o) {
		byte[] buf = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(bos);
			oo.writeObject(o);
			buf = bos.toByteArray();
			oo.close();
			bos.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("serialObjToBytes",e);
		}

		return buf;
	}
	
	/**
	 * 每次将待验证的人脸放入FailedFace
	 * @param fd
	 */
	private void putFailedFace(PITVerifyData fd){
		FailedFace failedFace = new FailedFace();
		failedFace.setIdNo(fd.getIdNo());
		failedFace.setIpAddress(DeviceConfig.getInstance().getIpAddress());
		failedFace.setGateNo(DeviceConfig.getInstance().getGateNo());
		failedFace.setCardImage(fd.getIdCardImg());
		failedFace.setFaceImage(fd.getFaceImg());
		FaceCheckingService.getInstance().setFailedFace(failedFace);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			CommUtil.sleep(50);
			try {
				PITVerifyData data = FaceCheckingService.getInstance().takeFaceVerifyData();
				if (data == null)
					continue;
				byte[] buf = serialObjToBytes(data);
				if (buf == null)
					continue;
				this.putFailedFace(data);  //每次将待验证的人脸放入FailedFace 供active mq调用
				
				ptVerifySender.sendMessage(buf);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error("PTVerifyThread running loop failed",e);
			}
		}
	}

}
