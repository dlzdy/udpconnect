package lianxi.udp.common;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.ReplayingDecoder;
import lianxi.tcp.common.Charsets;
import lianxi.tcp.common.MessageInput;
import lianxi.tcp.common.MessageOutput;
//消息解码器
//使用Netty的ReplayingDecoder实现。简单起见，这里没有使用checkpoint去优化性能了，感兴趣的话读者
// 可以参考一下我之前在公众号里发表的相关文章，将checkpoint相关的逻辑自己添加进去

public class UdpMessageDecoder extends MessageToMessageDecoder<DatagramPacket> {

	@Override
	protected void decode(ChannelHandlerContext ctx, DatagramPacket datagramPacket, List<Object> out) throws Exception {
		 //得到DatagramPacket数据内容
        ByteBuf in = datagramPacket.content();

		String requestId = readStr(in);
		String type = readStr(in);
		String payload = readStr(in);
		//创建MessageInput实例
        MessageInput input = new MessageInput(type, requestId, payload);
        out.add(input);
		
	}


	private String readStr(ByteBuf in) {
		int len = in.readInt();
		if (len < 0 || len > (1 << 20)) {
			throw new DecoderException("string too long len=" + len);
		}
		byte[] bytes = new byte[len];
		in.readBytes(bytes);
		return new String(bytes, Charsets.UTF8);
	}


}
