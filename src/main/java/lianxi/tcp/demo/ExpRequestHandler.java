package lianxi.tcp.demo;

import io.netty.channel.ChannelHandlerContext;
import lianxi.tcp.common.IMessageHandler;
import lianxi.tcp.common.MessageOutput;

public class ExpRequestHandler implements IMessageHandler<ExpRequest> {

	@Override
	public void handle(ChannelHandlerContext ctx, String requestId, ExpRequest message) {
		int base = message.getBase();
		int exp = message.getExp();
		long start = System.nanoTime();
		long res = 1;
		for (int i = 0; i < exp; i++) {
			res *= base;
		}
		System.out.println(ctx.channel().remoteAddress());
		long cost = System.nanoTime() - start;
		//响应输出
		ctx.writeAndFlush(new MessageOutput(requestId, "exp_res", new ExpResponse(res, cost)));
	}

}
