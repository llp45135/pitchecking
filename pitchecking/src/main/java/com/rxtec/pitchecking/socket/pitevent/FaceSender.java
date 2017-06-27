package com.rxtec.pitchecking.socket.pitevent;

import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.ImageToolkit;

public class FaceSender {
	private Logger log = LoggerFactory.getLogger("FaceSender");
	private static FaceSender _instance = new FaceSender();
	// private InetAddress address = null;

	private FaceSender() {
	}

	public static synchronized FaceSender getInstance() {
		if (_instance == null)
			_instance = new FaceSender();
		return _instance;
	}

	/**
	 * send by udp
	 * 
	 * @param buf
	 */
	// public void sendFaceDataByDdp(byte[] buf) {
	// DatagramPacket packet = null;
	// DatagramSocket socket = null;
	// try {
	// socket = new DatagramSocket(); // 4.向服务器端发送数据包新
	// // socket.setSendBufferSize(256*1024);
	// log.info("buf.length==" + buf.length);
	// packet = new DatagramPacket(buf, buf.length, address, PORT); //
	// 3.创建datagramsocket,
	// socket.send(packet);
	// buf = null;
	// packet = null;
	// socket.close();
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// log.error("sendFaceData failed", e);
	// } finally {
	// if (packet != null) {
	// packet = null;
	// }
	// if (socket != null) {
	// socket.close();
	// }
	// }
	// }

	/**
	 * sned by tcp
	 * 
	 * @param buf
	 */
	public void sendFaceDataByTcp(byte[] buf) {
		Socket socket = null;
		DataOutputStream dos = null;
		try {
			socket = new Socket("localhost", Config.getInstance().getFaceReceiveSocketPort());
//			log.info("buf.length==" + buf.length);
			// 向服务器端发送数据
			dos = new DataOutputStream(socket.getOutputStream());
			dos.write(buf);
			dos.flush();
			dos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("sendFaceDataByTcp:", e);
		} finally {
			try {
				if (dos != null)
					dos.close();
				if (socket != null)
					socket.close();
			} catch (IOException e) {
				socket = null;
				log.error("sendFaceDataByTcp finally 异常:", e.getMessage());
			}
		}
	}

	/**
	 * 
	 * @param fn
	 * @return
	 */
	private static IDCard createIDCard(String fn) {
		IDCard card = new IDCard();
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(new File(fn));
		} catch (Exception e) {
			e.printStackTrace();
		}

		card.setCardImageBytes(ImageToolkit.getImageBytes(bi, "JPEG"));
		return card;
	}

	/**
	 * 
	 * @param fn
	 * @return
	 */
	private static byte[] createFace(String fn) {
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(new File(fn));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ImageToolkit.getImageBytes(bi, "JPEG");
	}

	/**
	 * 
	 * @param fn
	 * @return
	 */
	private static byte[] createFrame(String fn) {
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(new File(fn));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ImageToolkit.getImageBytes(bi, "JPEG");
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(new FaceSendThread()); // 启动人脸发布 By socket

		for (int i = 0; i < 50; i++) {
			CommUtil.sleep(100);
			IDCard idcard = createIDCard("zhao.jpg");
			PITVerifyData pvd = new PITVerifyData();
			idcard.setIdNo("52020319791214111" + i);
			idcard.setAge(36);

			pvd.setUseTime(i);
			pvd.setTicket(new Ticket());

			if (idcard != null) {
				// pvd.setIdCard(idcard);
				// pvd.setIdCardImg(idcard.getCardImageBytes());
				// pvd.setIdNo(idcard.getIdNo());
				// pvd.setPersonName(idcard.getPersonName());
				// pvd.setAge(idcard.getAge());
				// pvd.setGender(idcard.getGender());
			}
			pvd.setFaceImg(createFace("llp.jpg"));
			pvd.setFrameImg(createFrame("zp.jpg"));

			FaceCheckingService.getInstance().offerFaceVerifyData(pvd);

			FaceCheckingService.getInstance().setIdcard(idcard);
			System.out.println("$$$$$$$$$$$$$" + i);

		}
	}
}
