package com.cscecee.basesite.core.udp.test;

import com.cscecee.basesite.core.udp.client.UdpClient;

import lianxi.tcp.client.RPCClient;
import lianxi.tcp.client.RPCException;
import lianxi.tcp.demo.DemoClient;
import lianxi.tcp.demo.ExpRequest;
import lianxi.tcp.demo.ExpResponse;

public class TestUdpClient {

	private UdpClient client;

	public TestUdpClient(UdpClient client) {
		this.client = client;
		this.client.register("fib_res", new FibRespHandler());
		this.client.register("exp_res", new ExpRespHandler());

	}

//
//	public ExpResponse exp(int base, int exp) {
//		return (ExpResponse) client.send("exp", new ExpRequest(base, exp));
//	}
//RPC客户端要链接远程IP端口，并注册服务输出类(RPC响应类)，
// 然后分别调用20次斐波那契服务和指数服务，输出结果

	public static void main(String[] args) throws InterruptedException {
		UdpClient client = new UdpClient("localhost", 8888);
		TestUdpClient testClient = new TestUdpClient(client);
		for (int i = 0; i < 30; i++) {
			try {
				System.out.printf("fib(%d) = %d\n", i, client.send("fib", i));
				Thread.sleep(100);
			} catch (RPCException e) {
				i--; // retry
			}
		}
//		Thread.sleep(3000);
//		for (int i = 0; i < 30; i++) {
//			try {
//				ExpResponse res = testClient.exp(2, i);
//				Thread.sleep(100);
//				System.out.printf("exp2(%d) = %d cost=%dns\n", i, res.getValue(), res.getCostInNanos());
//			} catch (RPCException e) {
//				i--; // retry
//			}
//		}

		client.close();
	}
}
