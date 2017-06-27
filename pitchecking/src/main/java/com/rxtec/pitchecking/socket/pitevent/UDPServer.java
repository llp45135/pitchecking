package com.rxtec.pitchecking.socket.pitevent;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.rxtec.pitchecking.utils.CommUtil;

/**
 * 
 * @author ZhaoLin
 *
 */
public class UDPServer {

	public UDPServer() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws IOException {
		/*
		 * 服务器端接收客户端信息。
		 */
		// 1.创建服务端的datagramsocket，指定端口
		DatagramSocket socket = new DatagramSocket(8889);

		while (true) {
			// CommUtil.sleep(50);

			// socket = new DatagramSocket(8888);
			// 2.创建数据报datagrampacket，用于接收客户端发送的数据
			byte[] data = new byte[1024];// 创建字节数组，接受指定数据包的大小

			DatagramPacket packet = new DatagramPacket(data, data.length);

			// 3.接收客户端发送的数据信息
			socket.receive(packet);// 此方法在接受数据报之前会一直阻塞
			// 4.读取数据
			String info = new String(data, 0, packet.getLength());// 转为数组
			System.out.println("我是服务器，客户端说：" + info);
			data = null;

			/*
			 * =============================== 向客户端响应
			 */
			// 1.定义客户端的地址，端口号，数据。
//			InetAddress address = packet.getAddress();
//			int port = packet.getPort();
//			byte[] data2 = "欢迎你".getBytes();
//			// 2.创建数据报datagrampacket,包含响应的数据
//			DatagramPacket packet2 = new DatagramPacket(data2, data2.length, address, port);
//			// 3.响应客户端
//			socket.send(packet2);
//			data2 = null;
			// socket.close();
		}

	}

}
