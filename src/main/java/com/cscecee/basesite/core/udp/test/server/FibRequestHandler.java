package com.cscecee.basesite.core.udp.test.server;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.cscecee.basesite.core.udp.common.Charsets;
import com.cscecee.basesite.core.udp.common.IMessageHandler;
import com.cscecee.basesite.core.udp.common.MessageOutput;
import com.cscecee.basesite.core.udp.server.UdpServerHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;


//斐波那契和指数计算处理
public class FibRequestHandler implements IMessageHandler {

	private final static Logger logger = LoggerFactory.getLogger(FibRequestHandler.class);
	
	private List<Long> fibs = new ArrayList<>();

	{
		fibs.add(1L); // fib(0) = 1
		fibs.add(1L); // fib(1) = 1
	}

	@Override
	public void handle(ChannelHandlerContext ctx, InetSocketAddress sender, String requestId, Object payload) {
		int n = Integer.valueOf(payload + "");
		for (int i = fibs.size(); i < n + 1; i++) {
			long value = fibs.get(i - 2) + fibs.get(i - 1);
			fibs.add(value);
		}
		
		ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer();
		String fromId = "0";
		writeStr(buf, fromId );
		writeStr(buf, requestId);
		writeStr(buf, "fib_res");//****
		writeStr(buf, fibs.get(n) + "");
		//响应输出
		logger.debug("send fib_res>>>>>" + fibs.get(n));
		ctx.writeAndFlush(new DatagramPacket(buf, sender));
	}
	private void writeStr(ByteBuf buf, String s) {
		buf.writeInt(s.length());
		buf.writeBytes(s.getBytes(Charsets.UTF8));
	}
}