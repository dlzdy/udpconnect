package lianxi.tcp.common;

import java.util.List;

import com.alibaba.fastjson.JSON;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
//响应消息的编码器比较简单
@Sharable
public class MessageEncoder extends MessageToMessageEncoder<MessageOutput> {

	@Override
	protected void encode(ChannelHandlerContext ctx, MessageOutput msg, List<Object> out) throws Exception {
		ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer();
		writeStr(buf, msg.getRequestId());
		writeStr(buf, msg.getType());
		writeStr(buf, JSON.toJSONString(msg.getPayload()));
		out.add(buf);
	}

	private void writeStr(ByteBuf buf, String s) {
		buf.writeInt(s.length());
		buf.writeBytes(s.getBytes(Charsets.UTF8));
	}

}
