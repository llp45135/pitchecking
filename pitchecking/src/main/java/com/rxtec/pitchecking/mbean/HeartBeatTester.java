package com.rxtec.pitchecking.mbean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.utils.CommUtil;

public class HeartBeatTester implements Runnable{

	String pid;
	public HeartBeatTester(){
		pid = ProcessUtil.getCurrentProcessID();
	}
	@Override
	public void run() {
		while(true){
			CommUtil.sleep(1000);
//			ProcessUtil.writeHeartbeat(pid,Config.getInstance().getHeartBeatLogFile());
			
		}
		
	}
	
	public static void main(String[] args) {
		HeartBeatTester h = new HeartBeatTester();
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(h);
	}

}
