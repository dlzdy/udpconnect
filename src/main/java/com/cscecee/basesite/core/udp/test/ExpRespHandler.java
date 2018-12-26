package com.cscecee.basesite.core.udp.test;

import java.net.InetSocketAddress;

import com.alibaba.fastjson.JSON;
import com.cscecee.basesite.core.udp.common.Charsets;
import com.cscecee.basesite.core.udp.common.IMessageHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;


public class ExpRespHandler implements IMessageHandler<ExpRequest> {

	@Override
	public void handle(ChannelHandlerContext ctx, InetSocketAddress sender, String requestId, ExpRequest message) {
		int base = message.getBase();
		int exp = message.getExp();
		long start = System.nanoTime();
		long res = 1;
		for (int i = 0; i < exp; i++) {
			res *= base;
		}
		long cost = System.nanoTime() - start;
		//响应输出
		
		ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer();
		String fromId = "0";
		writeStr(buf, fromId );
		writeStr(buf, requestId);
		writeStr(buf, "exp_res");//****
		writeStr(buf, JSON.toJSONString(new ExpResponse(res, cost)));
		ctx.writeAndFlush(new DatagramPacket(buf, sender));
//		ctx.writeAndFlush(new MessageOutput(requestId, "exp_res", new ExpResponse(res, cost)));
	}
	private void writeStr(ByteBuf buf, String s) {
		buf.writeInt(s.length());
		buf.writeBytes(s.getBytes(Charsets.UTF8));
	}
}
