package com.websocket.netty;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author weixy
 * @title 存储工厂的全局配置
 * @description 请填写相关描述
 * @date 2019/02/20 11:17
 */
public class NettyConfig {
    /**
     * 储存每一个客户端进来时的channel对象
     */
    public static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
}