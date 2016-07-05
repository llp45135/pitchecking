package com.rxtec.pitchecking.net;

import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.agrona.BufferUtil;
import org.agrona.concurrent.UnsafeBuffer;

import com.rxtec.pitchecking.IDReader;
import com.rxtec.pitchecking.utils.CommUtil;

import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.driver.MediaDriver;

public class FrameImagePublisher implements Runnable {

	private static final int STREAM_ID = 10;
	private static final String CHANNEL = "aeron:udp?endpoint=224.0.0.1:40123";
	private static final long LINGER_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(5);
	private static final UnsafeBuffer BUFFER = new UnsafeBuffer(BufferUtil.allocateDirectAligned(1024*64, 256));

	private Queue<FrameImage> frameImageQueue;
	private Publication publication;

	private static FrameImagePublisher _instance = new FrameImagePublisher();

	private FrameImagePublisher() {
		frameImageQueue = new LinkedList<FrameImage>();
		initAeron();
	}

	public static synchronized FrameImagePublisher getInstance() {
		if (_instance == null)
			_instance = new FrameImagePublisher();
		return _instance;
	}

	public void offerImage(FrameImage img) {
		if (img != null)
			frameImageQueue.offer(img);
	}

	private void initAeron() {
		final MediaDriver driver = MediaDriver.launch();

		final Aeron.Context ctx = new Aeron.Context();
		ctx.aeronDirectoryName(driver.aeronDirectoryName());

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

		}

	}

	@Override
	public void run() {
		while (true) {
			CommUtil.sleep(50);
			try {
				FrameImage img = frameImageQueue.poll();
				if(img == null) continue;
                System.out.println("offering--> "+ img.getImageBytes().length+" bytes" );
				BUFFER.putBytes(0, img.getImageBytes());
				final long result = publication.offer(BUFFER, 0, img.getImageBytes().length);

				if (result < 0L) {
					if (result == Publication.BACK_PRESSURED) {
						System.out.println("  Offer failed due to back pressure");
					} else if (result == Publication.NOT_CONNECTED) {
						System.out.println("  Offer failed because publisher is not yet connected to subscriber");
					} else if (result == Publication.ADMIN_ACTION) {
						System.out.println("  Offer failed because of an administration action in the system");
					} else if (result == Publication.CLOSED) {
						System.out.println("  Offer failed publication is closed");
					} else {
						System.out.println("  Offer failed due to unknown reason");
					}
				} else {
					System.out.println("   yay!");
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
