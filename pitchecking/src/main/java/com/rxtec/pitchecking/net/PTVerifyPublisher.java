package com.rxtec.pitchecking.net;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.agrona.BufferUtil;
import org.agrona.concurrent.UnsafeBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.IDReader;
import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.domain.FailedFace;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
import com.rxtec.pitchecking.picheckingservice.PITData;
import com.rxtec.pitchecking.utils.CommUtil;

import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.driver.MediaDriver;

/**
 * 主程序-待验证人脸(公布者)
 * @author ZhaoLin
 *
 */
public class PTVerifyPublisher implements Runnable {

	private Logger log = LoggerFactory.getLogger("PTVerifyPublisher");
	private static final int STREAM_ID = Config.PIVerify_Send_STREAM_ID;
	private static final String CHANNEL = Config.PIVerify_CHANNEL;
	private static final long LINGER_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(5);
	private static final UnsafeBuffer BUFFER = new UnsafeBuffer(BufferUtil.allocateDirectAligned(1024 * 128, 32));

	private Publication publication;

	private static PTVerifyPublisher _instance = new PTVerifyPublisher();

	private PTVerifyPublisher() {
		initAeronContext();
	}

	public static synchronized PTVerifyPublisher getInstance() {
		if (_instance == null)
			_instance = new PTVerifyPublisher();
		return _instance;
	}


	private void initAeronContext() {

		final Aeron.Context ctx = new Aeron.Context();

		// Connect a new Aeron instance to the media driver and create a
		// publication on
		// the given channel and stream ID.
		// The Aeron and Publication classes implement "AutoCloseable" and will
		// automatically
		// clean up resources when this try block is finished
		try {
			final Aeron aeron = Aeron.connect(ctx);
			publication = aeron.addPublication(CHANNEL, STREAM_ID);

			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(this);

		} catch (Exception ex) {
			log.error("initAeronContext",ex);
		}

	}

	@Override
	public void run() {
		while (true) {
			CommUtil.sleep(50);
			try {
				PITVerifyData data = FaceCheckingService.getInstance().takeFaceVerifyData();
				if (data == null)
					continue;
				byte[] buf = serialObjToBytes(data);
				if (buf == null)
					continue;
				this.putFailedFace(data);  //每次将待验证的人脸放入FailedFace
				log.debug("FaceVerifyData serial obj bytes = " + buf.length + " BUFFER.capacity = "+BUFFER.capacity());
				BUFFER.putBytes(0, buf);
				final long result = publication.offer(BUFFER, 0, buf.length);

				if (result < 0L) {
					if (result == Publication.BACK_PRESSURED) {
						log.error("  Offer failed due to back pressure");
					} else if (result == Publication.NOT_CONNECTED) {
						log.error("  Offer failed because publisher is not yet connected to subscriber");
					} else if (result == Publication.ADMIN_ACTION) {
						log.error("  Offer failed because of an administration action in the system");
					} else if (result == Publication.CLOSED) {
						log.error("  Offer failed publication is closed");
					} else {
						log.error("  Offer failed due to unknown reason");
					}
				} else {
//					log.info("Send Face verify request message succ!");
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error("PTVerifyPublisher running loop failed",e);
			}
		}
	}
	
	/**
	 * 每次将待验证的人脸放入FailedFace
	 * @param fd
	 */
	private void putFailedFace(PITVerifyData fd){
		FailedFace failedFace = new FailedFace();
		failedFace.setIdNo(CommUtil.getRandomUUID());
		failedFace.setIpAddress(DeviceConfig.getInstance().getIpAddress());
		failedFace.setGateNo(DeviceConfig.getInstance().getGateNo());
		failedFace.setCardImage(fd.getIdCardImg());
		failedFace.setFaceImage(fd.getFaceImg());
		FaceCheckingService.getInstance().setFailedFace(failedFace);
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

	public static void main(String[] args) {

		for (int i = 0; i < 100; i++) {
			PITVerifyData d = new PITVerifyData();
			d.setIdNo("1234567890");
			IDCard c1 = createIDCard("C:/pitchecking/B1.jpg");
			d.setFaceImg(c1.getImageBytes());
			d.setIdCardImg(c1.getImageBytes());
			d.setTicket(new Ticket());
			FaceCheckingService.getInstance().offerFaceVerifyData(d);
		}

	}
	
	private static IDCard createIDCard(String fn) {
		IDCard card = new IDCard();
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(new File(fn));
		} catch (Exception e) {
			e.printStackTrace();
		}

		card.setCardImage(bi);
		return card;
	}

}
