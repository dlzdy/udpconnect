package com.zdy.chat.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient {

	public static void main(String[] args) throws IOException {
		// 3.创建DatagramSocket对象
		final DatagramSocket socket = new DatagramSocket();
		new Thread() {
			public void run() {
				try {
					// 1.定义服务器的地址、端口号、数据
					InetAddress address = InetAddress.getByName("192.168.2.225");
					int port = 8800;
					byte[] data = "user：admin;password：123".getBytes();
					while (true) {
						/*
						 * 向服务器端发送数据
						 */

						// 2/创建数据报，包含发送的数据信息
						DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
						// 4.向服务器发送数据报
						socket.send(packet);
						System.out.println("mySocket is " + socket.getLocalPort());
						Thread.sleep(10000);

					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}.start();
		/*
		 * 接收服务器端响应的数据
		 */
		while (true) {
			// 1.创建数据报，用于接收服务器端响应的数据
			byte[] data2 = new byte[1024];// 创建字节数组，指定接收的数据包的大小
			DatagramPacket packet2 = new DatagramPacket(data2, data2.length);
			// 2.接收服务器响应数据
			socket.receive(packet2);
			// 3.读取数据
			String reply = new String(data2, 0, packet2.getLength());
			System.out.println("I am client，server say：" + reply);
		}
		// 4.关闭资源*/
		// socket.close();
	}

}
