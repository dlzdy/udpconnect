package com.zdy.chat.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServerThread extends Thread {
	DatagramSocket socket = null;
	DatagramPacket packet = null;
	byte[] data = null;

	public UDPServerThread(DatagramSocket socket, DatagramPacket packet, byte[] data) {
		this.socket = socket;
		this.packet = packet;
		this.data = data;
	}

	public void run(){
		while(true) {
        /*
         * 接收客户端数据
         */ 
            String info=new String(data, 0, packet.getLength());
            if(info!=null){
            	System.out.println("我是服务器，客户端说："+info);
            }               
            /*
             * 向客户端响应数据
             * */  
            //1.定义客户端的地址、端口号、数据
            InetAddress address=packet.getAddress();
            int port=packet.getPort();
            byte[] data2="欢迎你！".getBytes();
            //2.创建数据报，包含响应的数据信息
            DatagramPacket packet2=new DatagramPacket(data2, data2.length, address, port);
            //3.响应客户端
            try {
                socket.send(packet2);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
}
