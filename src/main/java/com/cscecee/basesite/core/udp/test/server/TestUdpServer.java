package com.cscecee.basesite.core.udp.test.server;

import com.cscecee.basesite.core.udp.server.UdpServer;



public class TestUdpServer {

	UdpServer server;
	public TestUdpServer(UdpServer server ) {
		this.server = server;
	}
//	public Object time() {
//		Object result = server.send("time", "");
//		return  result ;
//	}
	public static void main(String[] args) {
		UdpServer server = new UdpServer(8800);
		server.register("fib", new FibRequestHandler());
		server.register("exp", new ExpRequestHandler());
		TestUdpServer testServer = new TestUdpServer(server);
		//testServer.time();
		server.start();
	}
}
