package sb.websocket;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

/**
 * @author weixy
 * @version 2019-02-23
 * @deprecated 因为websocket的session和我们常用的httpsession不一样，所有我们要转换一下，部分场景会用到httpsession
 **/
public class GetHttpSessionConfigurator extends Configurator {

    private Logger logger = Logger.getLogger(GetHttpSessionConfigurator.class);

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {

        HttpSession httpSession = (HttpSession) request.getHttpSession();
        if (httpSession != null) {
            sec.getUserProperties().put(HttpSession.class.getName(), httpSession);
        } else {
            logger.error("GetHttpSessionConfigurator modifyHandshake httpSession = null");
        }
    }
}
