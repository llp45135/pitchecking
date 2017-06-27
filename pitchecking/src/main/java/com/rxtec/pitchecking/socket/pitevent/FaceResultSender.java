package com.rxtec.pitchecking.socket.pitevent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;

public class FaceResultSender {

	private Logger log = LoggerFactory.getLogger("FaceResultSender");
	private static FaceResultSender _instance = new FaceResultSender();

	private FaceResultSender() {
	}

	public static synchronized FaceResultSender getInstance() {
		if (_instance == null)
			_instance = new FaceResultSender();
		return _instance;
	}

	/**
	 * 
	 * @param data
	 * @return
	 */
	public boolean publishResult(PITVerifyData data) {
		if (data == null)
			return false;

		byte[] buf = serialObjToBytes(data);
		if (buf == null)
			return false;

		Socket socket = null;
		DataOutputStream dos = null;
		try {
			socket = new Socket("localhost", Config.getInstance().getFaceResultReceiveSocketPort());
			// 向服务器端发送数据
			dos = new DataOutputStream(socket.getOutputStream());
			dos.write(buf);
			dos.flush();
			dos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("FaceResultSender publishResult failed", e);
		} finally {
			try {
				if (dos != null)
					dos.close();
				if (socket != null)
					socket.close();
			} catch (Exception e) {
				socket = null;
				log.error("客户端 finally 异常:", e);
			}
		}

		return true;
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

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
