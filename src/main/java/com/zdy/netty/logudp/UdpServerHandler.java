package com.zdy.netty.logudp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import org.apache.log4j.Logger;

/**
 * 	 <B>说	明<B/>:
 * 
 * @author 作者名：冯龙淼
 * 		   E-mail：fenglongmiao@vrvmail.com.cn
 * 
 * @version 版   本  号：1.0.<br/>
 *          创建时间：2018年1月8日 上午10:09:47
 */
public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket>{

	private static final Logger logger = Logger.getLogger(UdpServerHandler.class);
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg)
			throws Exception {
		// 接受client的消息
		logger.info("开始接收来自client的数据");
		final ByteBuf buf = msg.content();
    	int readableBytes = buf.readableBytes();
    	byte[] content = new byte[readableBytes];
	    buf.readBytes(content);
	    String clientMessage = new String(content,"UTF-8");
		logger.info("clientMessage is: "+clientMessage);
		if(clientMessage.contains("UdpServer")){
			ctx.writeAndFlush(new DatagramPacket(Unpooled.wrappedBuffer("helloClient".getBytes()),msg.sender()));
		}
	}

}
