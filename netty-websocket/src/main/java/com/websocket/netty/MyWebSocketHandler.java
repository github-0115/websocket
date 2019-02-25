package com.websocket.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

import java.util.Date;

/**
 * @author weixy
 * @title 处理/接收/响应客户端websocket请求的核心业务处理类
 * @description 请填写相关描述
 * @date 2019/02/20 11:17
 */
public class MyWebSocketHandler extends SimpleChannelInboundHandler<Object> {

    private WebSocketServerHandshaker handshaker;
    private static final String WEB_SOCKET_URL = "ws://localhost:8888/websocket";

    /**
     * 客户端与服务端连接的时候调用
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NettyConfig.channelGroup.add(ctx.channel());
        System.out.println("客户端与服务端连接开启...");
    }

    /**
     * 客户端与服务端断开连接的时候调用
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyConfig.channelGroup.remove(ctx.channel());
        System.out.println("客户端与服务端连接关闭...");
    }

    /**
     * 服务端接收客户端发送过来的数据结束之后调用
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 工程出现异常的时候调用
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 服务端处理客户端websocket请求的核心方法
     *
     * @param channelHandlerContext
     * @param object
     * @throws Exception
     */
    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
        if (object instanceof FullHttpRequest) {
            // 处理客户端向服务端发起http握手请求的业务
            handHttpRequest(channelHandlerContext, (FullHttpRequest) object);
        } else if (object instanceof WebSocketFrame) {
            // 处理websocket连接业务
            handWebsocketFrame(channelHandlerContext, (WebSocketFrame) object);
        }
    }

    /**
     * 处理客户端向服务端发起http握手请求的业务
     *
     * @param ctx
     * @param request
     */
    private void handHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        if (!request.decoderResult().isSuccess() || !("websocket").equals(request.headers().get("Upgrade"))) {
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(WEB_SOCKET_URL, null, false);
        handshaker = wsFactory.newHandshaker(request);
        if (null == handshaker) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), request);
        }
    }

    /**
     * 服务端向客户端响应消息
     *
     * @param ctx
     * @param request
     * @param response
     */
    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest request, DefaultFullHttpResponse response) {
        if (response.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(response.status().toString(), CharsetUtil.UTF_8);
            response.content().writeBytes(buf);
            buf.release();
        }
        // 服务端向客户端发送数据
        ChannelFuture channelFuture = ctx.channel().writeAndFlush(response);
        if (response.status().code() != 200) {
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * 处理客户端与服务端之间的websocket业务
     *
     * @param ctx
     * @param frame
     */
    private void handWebsocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // 判断是否是关闭websocket的指令
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), ((CloseWebSocketFrame) frame).retain());
        }
        // 判断是否是ping的消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 判断是否是二进制消息，如果是二进制消息，抛出异常
        if (!(frame instanceof TextWebSocketFrame)) {
            System.out.println("目前我们不支持二进制消息...");
            throw new RuntimeException("【" + this.getClass().getName() + "】不支持消息...");
        }
        // 返回应答消息
        // 获取客户端向服务端发送的消息
        String request = ((TextWebSocketFrame) frame).text();
        System.out.println("--->>>" + request);
        TextWebSocketFrame tws = new TextWebSocketFrame(new Date().toString() + ctx.channel().id() + "--->>>" + request);
        // 群发，服务端向每个连接上来的客户端发消息
        NettyConfig.channelGroup.writeAndFlush(tws);
    }
}
