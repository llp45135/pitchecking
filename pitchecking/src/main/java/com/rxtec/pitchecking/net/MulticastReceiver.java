package com.rxtec.pitchecking.net;

import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jfree.util.Log;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rxtec.pitchecking.Config;
import com.rxtec.pitchecking.utils.JsonUtils;


public class MulticastReceiver implements Runnable {
	public  void testReceive() throws Exception {
		InetAddress group = InetAddress.getByName(Config.getInstance().getMulticastAddress());// 组播地址
		int port = Config.getInstance().getMulticastPort();
		MulticastSocket msr = null;// 创建组播套接字
		try {
			msr = new MulticastSocket(port);
			msr.joinGroup(group);// 加入连接
			byte[] buffer = new byte[1024*64];
			System.out.println("接收数据包启动！(启动时间: " + new Date() + ")");

			while (true) {
				// 建立一个指定缓冲区大小的数据包
				DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
				msr.receive(dp);
				// 解码组播数据包
				System.out.println("data receive length=" + dp.getLength());
				
				ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());

				String s = new String(dp.getData());
				PITInfoJson clone_of_bob =(PITInfoJson)JsonUtils.deserialize(s, PITInfoJson.class);
				
				System.out.println(clone_of_bob);
				
				Thread.sleep(500);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (msr != null) {
				try {
					msr.leaveGroup(group);
					msr.close();
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}
		}
	}
	
	public MulticastReceiver(){
		ExecutorService executor = Executors.newCachedThreadPool();
		executor.execute(this);

	}

	@Override
	public void run() {
		try {
			testReceive() ;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
