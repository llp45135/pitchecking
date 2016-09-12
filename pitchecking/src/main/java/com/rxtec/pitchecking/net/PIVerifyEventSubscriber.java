package com.rxtec.pitchecking.net;

import io.aeron.Aeron;
import io.aeron.Image;
import io.aeron.Subscription;
import io.aeron.driver.MediaDriver;
import io.aeron.logbuffer.FragmentHandler;
import io.aeron.protocol.HeaderFlyweight;

import org.agrona.CloseHelper;
import org.agrona.LangUtil;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.BusySpinIdleStrategy;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SigInt;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.IDCard;
import com.rxtec.pitchecking.Ticket;
import com.rxtec.pitchecking.net.event.EventHandler;
import com.rxtec.pitchecking.picheckingservice.FaceCheckingService;
import com.rxtec.pitchecking.picheckingservice.PITVerifyData;
import com.rxtec.pitchecking.picheckingservice.PITData;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * 接收铁科闸机主控程序送过来的人脸比对事件请求的订阅者
 * 接受的消息是JSON
 * 收到消息调用RSFaceTrackTask线程的 public void beginCheckingFace(IDCard idCard, Ticket ticket)
 * 
 * @author lenovo
 *
 */


public class PIVerifyEventSubscriber {
	private Logger log = LoggerFactory.getLogger("PIVerifyBeginEventSubscriber");

	private static final int STREAM_ID = Config.PIVerifyEvent_Begin_STREAM_ID;
	private static final String CHANNEL = Config.PIVerify_CHANNEL;

	public PIVerifyEventSubscriber() {
		log.debug("Subscribing to " + CHANNEL + " on stream Id " + STREAM_ID);
		final Aeron.Context ctx = new Aeron.Context().availableImageHandler(PIVerifyEventSubscriberUtils::printAvailableImage)
				.unavailableImageHandler(PIVerifyEventSubscriberUtils::printUnavailableImage);

		final FragmentHandler fragmentHandler = PIVerifyEventSubscriberUtils.processMessage(STREAM_ID);
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
			log.info("PIVerifyEventSubscriber connected,and begin Subscription...");
			log.debug("registrationId = " + subscription.registrationId());
			PIVerifyEventSubscriberUtils.subscriberLoop(fragmentHandler, 256, running).accept(subscription);
		}

	}

	public static void main(String[] args) {
		PIVerifyEventSubscriber s = new PIVerifyEventSubscriber();

	}

}

class PIVerifyEventSubscriberUtils {
	/**
	 * 闸机主控程序发送过来的事件处理器
	 */
	static EventHandler eventHandler = new EventHandler();
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
			Log.debug("running "+running.get());
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
//			final byte[] data = new byte[length];
//			buffer.getBytes(offset, data);
//			String jsonStr = "";
//			try {
//				jsonStr = new String(data,"UTF8");
//			} catch (UnsupportedEncodingException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			final String jsonStr = buffer.getStringWithoutLengthUtf8(offset, length);
			try {
				/**
				 * 处理闸机主控发送过来的事件
				 */
				eventHandler.InComeEventHandler(jsonStr);
			} catch (IOException e) {
				Log.error("EventHandler.InComeEventHandler", e);
			}
//			Log.debug(String.format("Message to stream %d from session %d (%d@%d) <<%s>>", streamId,
//					header.sessionId(), length, offset, jsonStr));
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
		Log.debug(String.format("Available image on %s streamId=%d sessionId=%d from %s",
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
		Log.debug(String.format("Unavailable image on %s streamId=%d sessionId=%d", subscription.channel(),
				subscription.streamId(), image.sessionId()));
	}

}
