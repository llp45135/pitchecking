package com.rxtec.pitchecking.socket.pitevent;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.rxtec.pitchecking.utils.CommUtil;

/**
 * 
 * @author ZhaoLin
 *
 */
public class UDPClient {

	public UDPClient() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws Exception {

		while (true) {
			CommUtil.sleep(500);
			/*
			 * 
			 * 发送请求信息
			 * 
			 */
			// 1.定义服务器的地址，端口号 、数据
			InetAddress address = InetAddress.getByName("localhost");
			int port = 8889;
			byte[] data = "用户名：admin；密码：456".getBytes();
			// 2.定义数据包datagrampacket, 发送数据信息
			DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
			// 3.创建datagramsocket,
			DatagramSocket socket = new DatagramSocket();
			// 4.向服务器端发送数据包新
			socket.send(packet);

			/*
			 * 
			 * 接收请求信息
			 * 
			 */
			// // 1.创建数据报 datagrampacket，用于接收响应数据。
			// byte[] data2 = new byte[1024];
			// DatagramPacket packet2 = new DatagramPacket(data2, data2.length);
			// // 2.接收服务器响应的数据
			// socket.receive(packet2);
			// // 读取数据
			// String reply = new String(data2, 0, packet2.getLength());
			// System.out.println("我是客户端》服务端响应：" + reply);
			// // 关闭资源
			socket.close();
		}
	}
}
