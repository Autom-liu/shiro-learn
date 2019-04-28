package com.edu.scnu.web.shiro;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.session.mgt.WebSessionKey;

import javax.servlet.ServletRequest;
import java.io.Serializable;

/**
 * @ClassName CustomSessionManager
 * @Description TODO
 * @Author Administrator
 * @Date 2019-04-28 17:11
 * @Version 1.0
 **/
public class CustomSessionManager extends DefaultWebSessionManager {

    @Override
    protected Session retrieveSession(SessionKey sessionKey) throws UnknownSessionException {
        Serializable sessionId = getSessionId(sessionKey);
        ServletRequest request = null;
        Session session = null;
        if(sessionKey instanceof WebSessionKey) {
            request = ((WebSessionKey) sessionKey).getServletRequest();
        }

        if(request != null && sessionId != null) {
            session = (Session) request.getAttribute(sessionId.toString());
            if (session == null) {
                session = super.retrieveSession(sessionKey);
                request.setAttribute(sessionId.toString(), session);
            }
        }

        return session;
    }
}
