package com.rxtec.pitchecking.net;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.agrona.LangUtil;
import org.agrona.concurrent.BusySpinIdleStrategy;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SigInt;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.FaceVerifyData;
import com.rxtec.pitchecking.picheckingservice.PITData;

import io.aeron.Aeron;
import io.aeron.Image;
import io.aeron.Subscription;
import io.aeron.logbuffer.FragmentHandler;

/**
 * 人脸比对结果-订阅者
 * @author ZhaoLin
 *
 */
public class PIVerifyResultSubscriber implements Runnable{

	private Logger log = LoggerFactory.getLogger("PIVerifyResultSubscriber");

	private static final int STREAM_ID = Config.PIVerify_Receive_STREAM_ID;
	private static final String CHANNEL = Config.PIVerify_CHANNEL;
	
	private static PIVerifyResultSubscriber _instance = new PIVerifyResultSubscriber();
	public static synchronized PIVerifyResultSubscriber getInstance() {
		if (_instance == null)
			_instance = new PIVerifyResultSubscriber();
		return _instance;
	}
	
	public void startSubscribing(){
		ExecutorService executer = Executors.newCachedThreadPool();
		executer.execute(this);

	}

	public static void main(String[] args) {
		PIVerifyResultSubscriber.getInstance().startSubscribing();

	}

	@Override
	public void run() {
		log.debug("Subscribing to " + CHANNEL + " on stream Id " + STREAM_ID);

		final Aeron.Context ctx = new Aeron.Context().availableImageHandler(ResultSubscriberUtils::printAvailableImage)
				.unavailableImageHandler(ResultSubscriberUtils::printUnavailableImage);

		final FragmentHandler fragmentHandler = ResultSubscriberUtils.processMessage(STREAM_ID);  //处理message
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
			ResultSubscriberUtils.subscriberLoop(fragmentHandler, 256, running).accept(subscription);

			log.info("PIVerifyEventSubscriber Shutting down...");
		}
		
	}

}

class ResultSubscriberUtils {
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
		final IdleStrategy idleStrategy = new BusySpinIdleStrategy();

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

	/**
	 * 处理人脸比对单独进程传回来的FaceVerifyData消息
	 * @param streamId
	 * @return
	 */
	public static FragmentHandler processMessage(final int streamId) {
		return (buffer, offset, length, header) -> {
			final byte[] data = new byte[length];
			buffer.getBytes(offset, data);

			try {
				ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
				FaceVerifyData fd = (FaceVerifyData)ois.readObject();
				PITData pd = new PITData(null);
				pd.setFaceCheckResult(fd.getVerifyResult());
				FaceCheckingService.getInstance().offerPassFaceData(pd);
			} catch (IOException | ClassNotFoundException e) {
				Log.error("processMessage",e);
			}

			Log.debug(String.format("Message to stream %d from session %d (%d@%d) <<%s>>", streamId,
					header.sessionId(), length, offset, new String(data)));
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
