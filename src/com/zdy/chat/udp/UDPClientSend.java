package com.zdy.chat.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClientSend {

	public static void main(String[] args) throws IOException {
		/*
		 * 向服务器端发送数据
		 */
		// 1.定义服务器的地址、端口号、数据
		InetAddress address = InetAddress.getByName("192.168.2.225");
		int port = 55901;
		byte[] data = "55901 55905590  55905590".getBytes();
		// 2/创建数据报，包含发送的数据信息
		DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
		// 3.创建DatagramSocket对象
		DatagramSocket socket = new DatagramSocket();
		// 4.向服务器发送数据报
		socket.send(packet);
		System.out.println("mySocket is " + socket.getLocalPort());
	}
	

}
