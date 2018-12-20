package com.zdy.nettyudp;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UdpClient {
    public static void main(String args[]){

        byte[] buf = new byte[256];
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
            System.out.println(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        buf= "hello".getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 9956);
        try {
            socket.send(packet);
            System.out.println(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
