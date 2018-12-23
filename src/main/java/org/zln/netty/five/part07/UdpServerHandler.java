package org.zln.netty.five.part07;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by sherry on 16/11/7.
 */
public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final String[] DICT = {
            "只要功夫深,铁杵磨成针"
            , "旧时王谢堂前燕,飞入寻常百姓家"
            , "洛阳亲友如相问,一片冰心在玉壶"
            , "一寸光阴一寸金,寸金难买寸光阴"
            , "老骥伏枥,志在千里.烈士暮年,壮心不已!"
    };

    /**
     * 日志
     */
    private Logger logger = LoggerFactory.getLogger(UdpServerHandler.class);

    @Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
      String req = msg.content().toString(CharsetUtil.UTF_8);
        logger.debug("收到的请求:" + req);
        if ("谚语字典查询?".equals(req)) {
            ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("谚语查询结果:" + nextQueue(), CharsetUtil.UTF_8), msg.sender()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        logger.error(cause.getMessage(),cause);
    }

    private String nextQueue() {
        //ThreadLocalRandom 线程安全随机类
        int quoteId = ThreadLocalRandom.current().nextInt(DICT.length);
        return DICT[quoteId];
    }




}
