package com.rxtec.pitchecking.net;

import io.aeron.Aeron;
import io.aeron.Image;
import io.aeron.Subscription;
import io.aeron.driver.MediaDriver;
import io.aeron.logbuffer.FragmentHandler;
import io.aeron.logbuffer.Header;
import io.aeron.protocol.HeaderFlyweight;

import org.agrona.CloseHelper;
import org.agrona.DirectBuffer;
import org.agrona.LangUtil;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.BusySpinIdleStrategy;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SigInt;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.net.event.EventHandler;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
import com.rxtec.pitchecking.picheckingservice.PITData;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * 订阅由人脸检测进程发过来的人脸比对请求
 * 通过Aeron订阅比对请求，将比对请求插入独立人脸比对进程（FaceCheckingService）的等待比对队列
 * 
 *
 */
public class PIVerifySubscriber {
	private Logger log = LoggerFactory.getLogger("PIVerifyEventSubscriber");

	private static final int STREAM_ID = Config.PIVerify_Begin_STREAM_ID;
	private static final String CHANNEL = Config.PIVerify_CHANNEL;
	

	public PIVerifySubscriber() {
		log.debug("Subscribing to " + CHANNEL + " on stream Id " + STREAM_ID);
		final Aeron.Context ctx = new Aeron.Context().availableImageHandler(PIVerifySubscriberUtils::printAvailableImage)
				.unavailableImageHandler(PIVerifySubscriberUtils::printUnavailableImage);

		final FragmentHandler fragmentHandler = PIVerifySubscriberUtils.processMessage(STREAM_ID);
		final AtomicBoolean running = new AtomicBoolean(true);

		// Register a SIGINT handler for graceful shutdown.
		SigInt.register(() -> running.set(false));

		// Create an Aeron instance using the configured Context and create a
		// Subscription on that instance that subscribes to the configured
		// channel and stream ID.
		// The Aeron and Subscription classes implement "AutoCloseable" and will
		// automatically
		// clean up resources when this try block is finished
		try (final Aeron aeron = Aeron.connect(ctx);
				final Subscription subscription = aeron.addSubscription(CHANNEL, STREAM_ID)) {
			PIVerifySubscriberUtils.subscriberLoop(fragmentHandler, 256, running).accept(subscription);

			log.info("PIVerifyEventSubscriber Shutting down...");
		}

	}

	public static void main(String[] args) {
		PIVerifySubscriber s = new PIVerifySubscriber();

	}

}

class PIVerifySubscriberUtils {
	
	/**
	 * Return a reusable, parameterised event loop that calls a default idler
	 * when no messages are received
	 *
	 * @param fragmentHandler
	 *            to be called back for each message.
	 * @param limit
	 *            passed to {@link Subscription#poll(FragmentHandler, int)}
	 * @param running
	 *            indication for loop
	 * @return loop function
	 */
	public static Consumer<Subscription> subscriberLoop(final FragmentHandler fragmentHandler, final int limit,
			final AtomicBoolean running) {
//		final IdleStrategy idleStrategy = new BusySpinIdleStrategy();
		final IdleStrategy idleStrategy = new BackoffIdleStrategy(1, 1, 1, 1);
		return subscriberLoop(fragmentHandler, limit, running, idleStrategy);
	}

	/**
	 * Return a reusable, parameterized event loop that calls and idler when no
	 * messages are received
	 *
	 * @param fragmentHandler
	 *            to be called back for each message.
	 * @param limit
	 *            passed to {@link Subscription#poll(FragmentHandler, int)}
	 * @param running
	 *            indication for loop
	 * @param idleStrategy
	 *            to use for loop
	 * @return loop function
	 */
	public static Consumer<Subscription> subscriberLoop(final FragmentHandler fragmentHandler, final int limit,
			final AtomicBoolean running, final IdleStrategy idleStrategy) {
		return (subscription) -> {
			try {
				while (running.get()) {
					idleStrategy.idle(subscription.poll(fragmentHandler, limit));
				}
			} catch (final Exception ex) {
				LangUtil.rethrowUnchecked(ex);
			}
		};
	}

	public static FragmentHandler processMessage(final int streamId) {
		return (buffer, offset, length, header) -> {
			final byte[] data = new byte[length];
			buffer.getBytes(offset, data);

			try {
				ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
				PITVerifyData fd = (PITVerifyData)ois.readObject();
				FaceCheckingService.getInstance().offerFaceVerifyData(fd);  //加入待验证队列
				Log.info("Receive request message : " + fd);
			} catch (IOException | ClassNotFoundException e) {
				Log.error("processMessage",e);
			}

		};
	}
	

	/**
	 * Print the information for an available image to stdout.
	 *
	 * @param image
	 *            that has been created
	 */
	public static void printAvailableImage(final Image image) {
		final Subscription subscription = image.subscription();
		System.out.println(String.format("Available image on %s streamId=%d sessionId=%d from %s",
				subscription.channel(), subscription.streamId(), image.sessionId(), image.sourceIdentity()));
	}

	/**
	 * Print the information for an unavailable image to stdout.
	 *
	 * @param image
	 *            that has gone inactive
	 */
	public static void printUnavailableImage(final Image image) {
		final Subscription subscription = image.subscription();
		System.out.println(String.format("Unavailable image on %s streamId=%d sessionId=%d", subscription.channel(),
				subscription.streamId(), image.sessionId()));
	}

}
