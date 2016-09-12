package com.rxtec.pitchecking.net;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.agrona.BufferUtil;
import org.agrona.concurrent.UnsafeBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.device.DeviceConfig;
import com.rxtec.pitchecking.domain.FailedFace;
import com.rxtec.pitchecking.net.event.EventHandler;
import com.rxtec.pitchecking.net.event.PIVerifyEventBean;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
import com.rxtec.pitchecking.utils.CommUtil;
import com.rxtec.pitchecking.utils.ImageToolkit;

import io.aeron.Aeron;
import io.aeron.Publication;

public class PIVerifyEventPublisher {

	private Logger log = LoggerFactory.getLogger("PTVerifyPublisher");
	private static final int STREAM_ID = Config.PIVerifyEvent_Begin_STREAM_ID;
	private static final String CHANNEL = Config.PIVerify_CHANNEL;
	private static final long LINGER_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(5);
	private static final UnsafeBuffer BUFFER = new UnsafeBuffer(BufferUtil.allocateDirectAligned(1024 * 128, 32));

	private EventHandler eventHandler = new EventHandler();
	private Publication publication;

	private static PIVerifyEventPublisher _instance = new PIVerifyEventPublisher();

	private PIVerifyEventPublisher() {
		initAeronContext();
	}

	public static synchronized PIVerifyEventPublisher getInstance() {
		if (_instance == null)
			_instance = new PIVerifyEventPublisher();
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

		} catch (Exception ex) {
			log.error("initAeronContext", ex);
		}

	}

	public void publish(PIVerifyEventBean b) {
		CommUtil.sleep(50);
		try {
			String json = eventHandler.OutputEventToJson(b);
			if(json == null) return;
			
			BUFFER.putStringWithoutLengthUtf8(0, json);
			final long result = publication.offer(BUFFER, 0, json.length());

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
				// log.info("Send Face verify request message succ!");
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("PTVerifyPublisher running loop failed", e);
		}
	}



	public static void main(String[] args) throws IOException {

		PIVerifyEventPublisher p = PIVerifyEventPublisher.getInstance();
		for (int i = 0; i < 1; i++) {
			PIVerifyEventBean b = buildPIVerifyEventBean();
			p.publish(b);
		}

	}
	
	ObjectMapper mapper = new ObjectMapper(); 
	
	private static PIVerifyEventBean buildPIVerifyEventBean() throws IOException{
		PIVerifyEventBean b = new PIVerifyEventBean();
		b.setAge(1);
		b.setEventDirection(1);
		b.setEventName("CAM_Notify");
		b.setGender(1);
        BufferedImage bi = ImageIO.read(new File("C:/pitchecking/llp.jpg"));
		b.setIdPhoto(ImageToolkit.getImageBytes(bi, "jpeg"));
		b.setPersonName("");
		b.setTicket(new Ticket());
		b.setUuid("111");
		b.setDelaySeconds(0);
		return b;
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