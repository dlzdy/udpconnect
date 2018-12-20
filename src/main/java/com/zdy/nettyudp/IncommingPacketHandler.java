package com.zdy.nettyudp;

import java.net.InetAddress;

import akka.actor.ActorRef;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

public class IncommingPacketHandler extends SimpleChannelInboundHandler<DatagramPacket> {

	public IncommingPacketHandler(ActorRef parserServer) {

	}

	protected void messageReceived(ChannelHandlerContext channelHandlerContext, DatagramPacket packet)
			throws Exception {
		final InetAddress srcAddr = packet.sender().getAddress();
		final ByteBuf buf = packet.content();
		final int rcvPktLength = buf.readableBytes();
		final byte[] rcvPktBuf = new byte[rcvPktLength];
		buf.readBytes(rcvPktBuf);
		System.out.println("Inside incomming packet handler:" + srcAddr);

		//rcvPktProcessing(rcvPktBuf, rcvPktLength, srcAddr);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext arg0, DatagramPacket arg1) throws Exception {
		// TODO Auto-generated method stub

	}
}
