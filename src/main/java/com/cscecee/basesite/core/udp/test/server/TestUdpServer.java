package com.cscecee.basesite.core.udp.test.server;

import com.cscecee.basesite.core.udp.server.UdpServer333;



public class TestUdpServer {

	UdpServer333 server;
	public TestUdpServer(UdpServer333 server ) {
		this.server = server;
	}
//	public Object time() {
//		Object result = server.send("time", "");
//		return  result ;
//	}
	public static void main(String[] args) {
		UdpServer333 server = new UdpServer333(8800);
		server.register("fib", new FibRequestHandler());
		server.register("exp", new ExpRequestHandler());
		TestUdpServer testServer = new TestUdpServer(server);
		//testServer.time();
		server.start();
	}
}
