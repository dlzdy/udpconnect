package lianxi.tcp.demo;

import java.util.ArrayList;
import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import lianxi.tcp.common.IMessageHandler;
import lianxi.tcp.common.MessageOutput;

//斐波那契和指数计算处理
public class FibRequestHandler implements IMessageHandler<Integer> {

	private List<Long> fibs = new ArrayList<>();

	{
		fibs.add(1L); // fib(0) = 1
		fibs.add(1L); // fib(1) = 1
	}

	@Override
	public void handle(ChannelHandlerContext ctx, String requestId, Integer n) {
		for (int i = fibs.size(); i < n + 1; i++) {
			long value = fibs.get(i - 2) + fibs.get(i - 1);
			fibs.add(value);
		}
		//响应输出
		ctx.writeAndFlush(new MessageOutput(requestId, "fib_res", fibs.get(n)));
	}

}