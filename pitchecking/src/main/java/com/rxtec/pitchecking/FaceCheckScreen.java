package com.rxtec.pitchecking;

import java.util.concurrent.LinkedBlockingQueue;

import com.rxtec.pitchecking.event.ScreenElementModifyEvent;

public class FaceCheckScreen {
	private static FaceCheckScreen _instance = new FaceCheckScreen();

	private FaceCheckScreen() {
	}

	public static FaceCheckScreen getInstance() {
		return _instance;
	}

	private LinkedBlockingQueue<ScreenElementModifyEvent> screenEventQueue = new LinkedBlockingQueue<ScreenElementModifyEvent>();

	public void offerEvent(ScreenElementModifyEvent e) {
		screenEventQueue.add(e);
	}

	private void startShow() throws InterruptedException {
		while (true) {
			ScreenElementModifyEvent e = screenEventQueue.take();
			/*
			 * 根据ScreenElementModifyEvent的screenType、elementType、
			 * elementCmd来决定屏幕的显示内容
			 */
		}
	}

	public ScreenElementModifyEvent getScreenEvent() {
		return null;
	}

}
