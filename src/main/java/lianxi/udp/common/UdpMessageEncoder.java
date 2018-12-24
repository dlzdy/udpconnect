package lianxi.udp.common;

import java.net.InetSocketAddress;
import java.util.List;

import com.alibaba.fastjson.JSON;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import lianxi.tcp.common.Charsets;
import lianxi.tcp.common.MessageOutput;

//响应消息的编码器比较简单
@Sharable
public class UdpMessageEncoder extends MessageToMessageEncoder<MessageOutput> {

	private final InetSocketAddress remoteAddress;

	public UdpMessageEncoder(InetSocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, MessageOutput msg, List<Object> out) throws Exception {
		ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer();
		// requestId
		writeStr(buf, msg.getRequestId());
		// type
		writeStr(buf, msg.getType());
		// payload,json格式
		writeStr(buf, JSON.toJSONString(msg.getPayload()));
		// 创建DatagramPacket实例并添加到结果列表
		out.add(new DatagramPacket(buf, remoteAddress));
	}

	private void writeStr(ByteBuf buf, String s) {
		buf.writeInt(s.length());
		buf.writeBytes(s.getBytes(Charsets.UTF8));
	}

}
