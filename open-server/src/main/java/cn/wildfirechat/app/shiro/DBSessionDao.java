package cn.wildfirechat.app.shiro;

import cn.wildfirechat.app.jpa.ShiroSession;
import cn.wildfirechat.app.jpa.ShiroSessionRepository;
import com.google.gson.Gson;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DBSessionDao implements SessionDAO {
    /**
     * 会话反序列化只允许这些类型，避免读到库里的恶意字节流时执行任意代码。
     * SimplePrincipalCollection 是 Shiro 登录后写入会话的身份信息；SavedRequest 是未登录访问受限接口时 Shiro 记下的原请求；
     * Number 是 Long/Integer 等包装类的父类，反序列化时也会被校验。
     */
    private static final Set<String> ALLOWED_CLASSES = new HashSet<>(Arrays.asList(
            "org.apache.shiro.session.mgt.SimpleSession",
            "org.apache.shiro.subject.SimplePrincipalCollection",
            "org.apache.shiro.web.util.SavedRequest",
            "java.lang.Number",
            "java.util.HashMap",
            "java.util.LinkedHashMap",
            "java.util.HashSet",
            "java.util.LinkedHashSet",
            "java.util.ArrayList",
            "java.util.Date",
            "java.lang.String",
            "java.lang.Long",
            "java.lang.Integer",
            "java.lang.Short",
            "java.lang.Byte",
            "java.lang.Character",
            "java.lang.Float",
            "java.lang.Double",
            "java.lang.Boolean"
    ));

    private Map<Object, Session> sessionMap = new ConcurrentHashMap<>();

    @Autowired
    private ShiroSessionRepository shiroSessionRepository;

    @Override
    public Serializable create(Session session) {
        String sessionId = UUID.randomUUID().toString().replaceAll("-", "");
        ((SimpleSession) session).setId(sessionId);
        return sessionId;
    }

    @Override
    public Session readSession(Serializable sessionId) throws UnknownSessionException {
//        return sessionMap.get(sessionId);
        ShiroSession shiroSession = shiroSessionRepository.findById((String) sessionId).orElse(null);
        if (shiroSession != null) {
            Session session = byteToSession(shiroSession.getSessionData());
            return session;
        }
        return null;
    }

    @Override
    public void update(Session session) throws UnknownSessionException {
        byte[] bb = sessionToByte(session);
        ShiroSession shiroSession = new ShiroSession((String)session.getId(), bb);
//        sessionMap.put(session.getId(), session);
        shiroSessionRepository.save(shiroSession);
    }

    @Override
    public void delete(Session session) {
        sessionMap.remove(session.getId());
    }

    @Override
    public Collection<Session> getActiveSessions() {
        return sessionMap.values();
    }

    // convert session object to byte, then store it to redis
    private byte[] sessionToByte(Session session){
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] bytes = null;
        try {
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(session);
            bytes = bo.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    // restore session
    private Session byteToSession(byte[] bytes){
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try (ObjectInputStream in = new SafeObjectInputStream(new ByteArrayInputStream(bytes))) {
            Object object = in.readObject();
            if (object instanceof SimpleSession) {
                return (SimpleSession) object;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 只允许会话需要的类型，其它类一律拒绝。
     * 库里的 session_data 一旦被写入恶意字节流，默认的 ObjectInputStream 会直接反序列化并执行 gadget。
     */
    private static final class SafeObjectInputStream extends ObjectInputStream {
        SafeObjectInputStream(InputStream in) throws IOException {
            super(in);
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            String name = desc.getName();
            if (name.startsWith("[")) {
                // 数组只放行基本类型数组、String[]、Object[]
                if (name.length() == 2
                        || "[Ljava.lang.String;".equals(name)
                        || "[Ljava.lang.Object;".equals(name)) {
                    return super.resolveClass(desc);
                }
                throw new InvalidClassException("Unauthorized array class", name);
            }
            if (!ALLOWED_CLASSES.contains(name)) {
                throw new InvalidClassException("Unauthorized deserialization class", name);
            }
            return super.resolveClass(desc);
        }
    }
}
