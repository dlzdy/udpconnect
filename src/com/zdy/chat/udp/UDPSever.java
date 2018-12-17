package com.zdy.chat.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Hashtable;

public class UDPSever {
	public static void main(String[] args) throws IOException {
		Hashtable<String, DatagramPacket> udpMap = new Hashtable<String, DatagramPacket>();
		DatagramSocket socket = new DatagramSocket(8800);
		System.out.println("服务器即将启动，等待客户端的连接");
		byte[] data = new byte[1024];// 创建字节数组，指定接收的数据包的大小
		int count = 0;
		while (true) {
			DatagramPacket packet = new DatagramPacket(data, data.length);
			socket.receive(packet);
			if(!udpMap.containsKey(packet.getSocketAddress().toString())) {
				udpMap.put(packet.getSocketAddress().toString(), packet);
				// 此方法在接收到数据报之前会一直阻塞
				// 4.读取数据
				count++;
				System.out.println("client count is " + count);
				UDPServerThread UDPThread = new UDPServerThread(socket, packet, data);
				UDPThread.start();
			}
		}
	}
}
