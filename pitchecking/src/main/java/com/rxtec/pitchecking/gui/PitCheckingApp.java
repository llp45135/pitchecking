package com.rxtec.pitchecking.gui;

import com.rxtec.pitchecking.device.DeviceEventListener;
import com.rxtec.pitchecking.device.TicketCheckScreen;

/**
 * 人证识别自动验票软件主入口
 * @author ZhaoLin
 *
 */
public class PitCheckingApp {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DeviceEventListener eventListener = DeviceEventListener.getInstance();
		TicketCheckScreen ticketCheckScreen = TicketCheckScreen.getInstance();
		//启动事件监听
		try {
			eventListener.startListenEvent();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
