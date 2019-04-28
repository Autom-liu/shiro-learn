package com.edu.scnu.web.shiro;

import com.edu.scnu.util.RedisUtil;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.SerializationUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @ClassName RedisSession
 * @Description 重写基于Redis的会话管理
 * @Author Administrator
 * @Date 2019-04-28 14:17
 * @Version 1.0
 **/
public class RedisSession extends AbstractSessionDAO {

    @Autowired
    private RedisUtil redisUtil;

    private final String SESSION_PREFIX = "session:";

    private byte[] getKey(String key) {
        return (SESSION_PREFIX + key).getBytes();
    }

    private void saveSession(Session session) {
        if(session != null && session.getId() != null) {
            byte[] key = getKey(session.getId().toString());
            byte[] value = SerializationUtils.serialize(session);
            redisUtil.set(key, value);
            redisUtil.expire(key, 600);
        }
    }

    @Override
    protected Serializable doCreate(Session session) {
        System.out.println("doCreate...");
        Serializable sessionId = generateSessionId(session);
        assignSessionId(session, sessionId);
        saveSession(session);
        return sessionId;
    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
        System.out.println("doReadSession...");
        System.out.println(sessionId.toString());
        if(sessionId == null) {
            return null;
        }
        byte[] key = getKey(sessionId.toString());
        byte[] value = redisUtil.get(key);
        return (Session) SerializationUtils.deserialize(value);
    }

    @Override
    public void update(Session session) throws UnknownSessionException {
        System.out.println("update...");
        saveSession(session);
    }

    @Override
    public void delete(Session session) {
        System.out.println("delete...");
        if(session == null || session.getId() == null) {
            return;
        }
        byte[] key = getKey(session.getId().toString());
        redisUtil.del(key);
    }

    @Override
    public Collection<Session> getActiveSessions() {
        System.out.println("getActiveSessions...");
        Set<byte[]> keys = redisUtil.keys(SESSION_PREFIX);
        Set<Session> sessions = new HashSet<>();
        if(CollectionUtils.isEmpty(keys)) {
            return sessions;
        }

        for (byte[] key : keys) {
            Session session = (Session) SerializationUtils.deserialize(redisUtil.get(key));
            sessions.add(session);
        }
        return sessions;
    }
}
