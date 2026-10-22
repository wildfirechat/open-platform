package cn.wildfirechat.app.shiro;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 会话反序列化白名单：已登录会话必须能读回来，白名单外的类型必须被拒绝。
 */
public class DBSessionDaoTest {

    private final DBSessionDao dao = new DBSessionDao();

    private Session roundTrip(Session session) {
        byte[] bytes = ReflectionTestUtils.invokeMethod(dao, "sessionToByte", session);
        return ReflectionTestUtils.invokeMethod(dao, "byteToSession", (Object) bytes);
    }

    @Test
    public void loggedInSessionCanBeRestored() {
        SimpleSession session = new SimpleSession("127.0.0.1");
        session.setId("login");
        session.setAttribute("count", 1L);
        session.setAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY,
                new SimplePrincipalCollection("admin", "passwordRealm"));
        session.setAttribute(DefaultSubjectContext.AUTHENTICATED_SESSION_KEY, Boolean.TRUE);

        Session restored = roundTrip(session);

        assertNotNull(restored, "已登录会话必须能反序列化");
        assertEquals(1L, restored.getAttribute("count"));
        assertEquals(Boolean.TRUE, restored.getAttribute(DefaultSubjectContext.AUTHENTICATED_SESSION_KEY));
        assertEquals("admin",
                ((SimplePrincipalCollection) restored.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY))
                        .getPrimaryPrincipal());
    }

    @Test
    public void sessionWithUnlistedClassIsRejected() {
        SimpleSession session = new SimpleSession("127.0.0.1");
        session.setId("bad");
        session.setAttribute("other", new TreeMap<String, String>());

        assertNull(roundTrip(session), "白名单外的类型必须被拒绝");
    }

    @Test
    public void emptySessionDataIsIgnored() {
        assertNull(ReflectionTestUtils.invokeMethod(dao, "byteToSession", (Object) new byte[0]));
    }
}
