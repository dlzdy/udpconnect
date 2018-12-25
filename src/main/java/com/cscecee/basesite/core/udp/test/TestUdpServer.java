package com.cscecee.basesite.core.udp.test;

import com.cscecee.basesite.core.udp.server.UdpServer;

public class TestUdpServer {

	public static void main(String[] args) {
		UdpServer server = new UdpServer(8800);
		server.start();
	}
}
