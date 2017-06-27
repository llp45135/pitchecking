package com.rxtec.pitchecking.socket.pitevent;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.rxtec.pitchecking.picheckingservice.PITVerifyData;

public class Server {
	public static final int PORT = 8888;// 监听的端口号
	private ServerSocket serverSocket;

	public static void main(String[] args) {
		System.out.println("服务器启动...\n");
		Server server = new Server();
		server.init();
	}

	public void init() {
		try {
			serverSocket = new ServerSocket(PORT);
			Socket socket = null;
			while (true) {
				// 一旦有堵塞, 则表示服务器与客户端获得了连接
				socket = serverSocket.accept();
				// 处理这次连接
				DataInputStream dis = null;
				ByteArrayInputStream bais = null;
				ObjectInputStream ois = null;
				try {
					socket.setSendBufferSize(64 * 1024);
					socket.setReceiveBufferSize(64 * 1024);
					// 装饰流BufferedReader封装输入流（接收客户端的流）
					// BufferedInputStream bis = new
					// BufferedInputStream(socket.getInputStream());

					dis = new DataInputStream(socket.getInputStream());

					byte[] data = new byte[256 * 1024]; // 一次读取一个byte

					int nIdx = 0;
					int nTotalLen = data.length;
					int nReadLen = 0;

					while (nIdx < nTotalLen) {
						nReadLen = dis.read(data, nIdx, nTotalLen - nIdx);

						if (nReadLen > 0) {
							nIdx = nIdx + nReadLen;
						} else {
							break;
						}
					}

					bais = new ByteArrayInputStream(data);
					ois = new ObjectInputStream(bais);
					PITVerifyData fd = (PITVerifyData) ois.readObject();
					System.out.println("收到数据,fd.getIdNo = " + fd.getIdNo() + ",fd.usetime = " + fd.getUseTime()
							+ ",faceImg.length = " + fd.getFaceImg().length + ",frameImg = " + fd.getFrameImg().length);
					if (fd.getIdCardImg() != null) {
						System.out.println("fd.idcardImg.length = " + fd.getIdCardImg().length);
					} else {
						System.out.println("fd.idcardImg.length = null");
					}

					ois.close();
					bais.close();
					dis.close();

					socket.close();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (ois != null)
						ois.close();
					if (bais != null)
						bais.close();
					if (dis != null)
						dis.close();
					if (socket != null)
						socket.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void doSomething(String ret) {
		System.out.println(ret);
	}

	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	public static String BytesHexString(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret += hex.toUpperCase();
		}
		return ret;
	}

}
