package sb.websocket;

import javax.websocket.EndpointConfig;
import javax.websocket.Session;

/**
 * @author wxy
 * @version 2019-02-23
 * @deprecated websocket业务接口，抽一些共用的方法
 **/
public interface WebSocketServer {
    /**
     * 连接建立成功调用的方法
     *
     * @param session session 对象
     */
    public void onOpen(Session session, EndpointConfig config);

    /**
     * 断开连接方法
     */
    public void onClose(Session session);

    /**
     * 收到客户端消息后调用的方法
     *
     * @param session session 对象
     * @param message 返回客户端的消息
     */
    public void onMessage(Session session, String message);

    /**
     * 发生异常时触发的方法
     *
     * @param session   session 对象
     * @param throwable 抛出的异常
     */
    public void onError(Session session, Throwable throwable);

    /**
     * 向单个客户端发送消息
     *
     * @param session session 对象
     * @param message 发送给客户端的消息
     */
    public void sendMessage(Session session, String message);

    /**
     * 向所有在线用户群发消息
     *
     * @param message 发送给客户端的消息
     */
    public void batchSendMessage(String message);
}