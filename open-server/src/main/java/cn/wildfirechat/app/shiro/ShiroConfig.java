package cn.wildfirechat.app.shiro;


import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.ShiroHttpSession;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    @Autowired
    DBSessionDao dbSessionDao;

    @Value("${wfc.all_client_support_ssl}")
    private boolean All_Client_Support_SSL;

    @Bean(name = "shiroFilter")
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        shiroFilterFactoryBean.setLoginUrl("/login");
        shiroFilterFactoryBean.setUnauthorizedUrl("/notRole");
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();

        // <!-- authc:所有url都必须认证通过才可以访问; anon:所有url都都可以匿名访问-->
        filterChainDefinitionMap.put("/index.html", "anon");
        filterChainDefinitionMap.put("/js/*", "anon");
        filterChainDefinitionMap.put("/fonts/*", "anon");
        filterChainDefinitionMap.put("/img/*", "anon");
        filterChainDefinitionMap.put("/css/*", "anon");
        filterChainDefinitionMap.put("/", "anon");
        filterChainDefinitionMap.put("/work.html", "anon");


        filterChainDefinitionMap.put("/api/login", "anon");
        filterChainDefinitionMap.put("/api/user_login", "anon");
        filterChainDefinitionMap.put("/api/account", "perms[user:view]");
        filterChainDefinitionMap.put("/api/application/create", "perms[user:admin]");
        filterChainDefinitionMap.put("/api/application/get/*", "anon");
        filterChainDefinitionMap.put("/api/application/list", "anon");
        filterChainDefinitionMap.put("/api/application/del", "perms[user:admin]");
        filterChainDefinitionMap.put("/api/application/update", "perms[user:admin]");
        filterChainDefinitionMap.put("/api/application/list_global", "anon");
        filterChainDefinitionMap.put("/api/application/list_foreground", "anon");
        filterChainDefinitionMap.put("/api/application/list_background", "perms[user:admin]");

        //主要这行代码必须放在所有权限设置的最后，不然会导致所有 url 都被拦截 剩余的都需要认证
        filterChainDefinitionMap.put("/**", "login");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        shiroFilterFactoryBean.getFilters().put("login", new JsonAuthLoginFilter());
        shiroFilterFactoryBean.getFilters().put("perms", new WfPermissionsAuthorizationFilter());
        return shiroFilterFactoryBean;

    }

    @Bean
    public SecurityManager securityManager() {
        DefaultWebSecurityManager defaultSecurityManager = new DefaultWebSecurityManager();
        defaultSecurityManager.setRealms(Arrays.asList(passwordRealm, authCodeRealm));
        ShiroSessionManager sessionManager = new ShiroSessionManager();
        sessionManager.setGlobalSessionTimeout(Long.MAX_VALUE);
        sessionManager.setSessionDAO(dbSessionDao);

        Cookie cookie = new SimpleCookie(ShiroHttpSession.DEFAULT_SESSION_ID_NAME);
        if (All_Client_Support_SSL) {
            cookie.setSameSite(Cookie.SameSiteOptions.NONE);
            cookie.setSecure(true);
        } else {
            cookie.setSameSite(null);
        }
        cookie.setMaxAge(Integer.MAX_VALUE);
        sessionManager.setSessionIdCookie(cookie);
        sessionManager.setSessionIdCookieEnabled(true);
        sessionManager.setSessionIdUrlRewritingEnabled(true);

        defaultSecurityManager.setSessionManager(sessionManager);
        SecurityUtils.setSecurityManager(defaultSecurityManager);
        return defaultSecurityManager;
    }

    @Autowired
    private PasswordRealm passwordRealm;


    @Autowired
    private AuthCodeRealm authCodeRealm;
}