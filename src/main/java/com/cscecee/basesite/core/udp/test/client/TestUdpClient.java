package com.cscecee.basesite.core.udp.test.client;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.cscecee.basesite.core.udp.client.UdpClient;

import lianxi.tcp.client.RPCClient;
import lianxi.tcp.client.RPCException;
import lianxi.tcp.demo.DemoClient;
import lianxi.tcp.demo.ExpRequest;
import lianxi.tcp.demo.ExpResponse;

public class TestUdpClient {
	private final static Logger logger = LoggerFactory.getLogger(TestUdpClient.class);

	private UdpClient client;

	public TestUdpClient(UdpClient client) {
		this.client = client;
		this.client.register("time", new TimeRequestHandler());

	}
	public Object fib(int n) {
		Object result = client.send("fib", n);
		return  result ;
	}

	//ExpResponse
	public Object exp(int base, int exp) {
		Object result = client.send("exp", JSON.toJSONString(new ExpRequest(base, exp)));
		return  result ;
		//return (ExpResponse) client.send("exp", new ExpRequest(base, exp));
	}
//RPC客户端要链接远程IP端口，并注册服务输出类(RPC响应类)，
// 然后分别调用20次斐波那契服务和指数服务，输出结果

	public static void main(String[] args) throws InterruptedException {
		UdpClient client = new UdpClient("localhost", 8800, UUID.randomUUID().toString().replaceAll("-", ""));
		TestUdpClient testClient = new TestUdpClient(client);

		for (int i = 0; i < 30; i++) {
			try {
				System.out.printf("fib(%d) = %s\n", i, (testClient.fib(i)+""));
				Thread.sleep(100);
			} catch (RPCException e) {
				i--; // retry
			}
		}
		Thread.sleep(3000);
		for (int i = 0; i < 30; i++) {
			try {
				String strJsonObj = testClient.exp(2, i) + "";
				ExpResponse expResp = JSON.parseObject(strJsonObj, ExpResponse.class);
				if (expResp != null) {
					System.out.printf("exp2(%d) = %d cost=%dns\n", i, expResp.getValue(), expResp.getCostInNanos());
				}else {
					System.err.println("null");
				}
				Thread.sleep(100);
			} catch (RPCException e) {
				i--; // retry
			}
		}

		client.close();
	}
}
