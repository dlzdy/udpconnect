package com.zdy.netty.logudp;

import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.junit.Test;


/**
 * 	 <B>说	明<B/>:
 * 
 * @author 作者名：冯龙淼
 * 		   E-mail：fenglongmiao@vrvmail.com.cn
 * 
 * @version 版   本  号：1.0.<br/>
 *          创建时间：2018年1月8日 上午10:25:21
 */
public class UdpMainTest {

	private static final Logger logger = Logger.getLogger(UdpMainTest.class);
	
	private static final String host = "192.168.133.72";
	
	private static final int port = 888;
	
	public static void main(String[] args) {
		try {
			UdpLogServer.getInstance().start(host, port);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void logPushSendTest() throws Exception{
		LogPushUdpClient.getInstance().start();
		int i = 0;
		while( i < 10){
			LogPushUdpClientHandler.sendMessage(new String("你好UdpServer"), new InetSocketAddress(host,port));
		    logger.info(i+" client send message is: 你好UdpServer");
		    i++;
		}
	}
	
}
