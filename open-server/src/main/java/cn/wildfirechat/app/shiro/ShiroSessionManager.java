package cn.wildfirechat.app.shiro;

import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;
import org.h2.util.StringUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.Serializable;

public class ShiroSessionManager extends DefaultWebSessionManager {

    private static final String AUTHORIZATION = "authToken";

    private static final String REFERENCED_SESSION_ID_SOURCE = "Stateless request";

    public ShiroSessionManager(){
        super();
    }

    @Override
    protected Serializable getSessionId(ServletRequest request, ServletResponse response){
        // 用解码并规范化后的应用内路径（与 Shiro 过滤器链匹配时一致），
        // 避免 //api/login、/./api/login 这类写法绕过下面的登录路径判断。
        String path = WebUtils.getPathWithinApplication(WebUtils.toHttp(request));

        // 登录请求必须新建会话：不能复用请求里携带的旧 authToken / Cookie。
        // 否则 subject.login() 会把新登录的身份写进旧会话，session ID 不轮换，
        // 攻击者事先拿到的 token 在受害者登录后就变成了受害者的已登录会话（会话固定）。
        if (isLoginPath(path)) {
            return null;
        }

        String id = WebUtils.toHttp(request).getHeader(AUTHORIZATION);
        if(StringUtils.isNullOrEmpty(id)){
            //如果没有携带id参数则按照父类的方式在cookie进行获取
            return super.getSessionId(request, response);
        }else{
            //如果请求头中有 authToken 则其值为sessionId
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE,REFERENCED_SESSION_ID_SOURCE);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID,id);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID,Boolean.TRUE);
            return id;
        }
    }

    /**
     * 需要强制换新会话的登录入口：管理后台登录（/login、/api/login）和客户端 authCode 登录（/api/user_login）。
     */
    static boolean isLoginPath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return "/login".equals(path)
                || "/api/login".equals(path)
                || "/api/user_login".equals(path);
    }
}
