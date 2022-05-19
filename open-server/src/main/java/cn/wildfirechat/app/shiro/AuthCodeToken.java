package cn.wildfirechat.app.shiro;

import org.apache.shiro.authc.AuthenticationToken;

public class AuthCodeToken implements AuthenticationToken {
    public String appId;
    public int appType;
    public String authCode;

    public AuthCodeToken() {
    }

    public AuthCodeToken(String appId, int appType, String authCode) {
        this.appId = appId;
        this.appType = appType;
        this.authCode = authCode;
    }

    @Override
    public Object getPrincipal() {
        return appId;
    }

    @Override
    public Object getCredentials() {
        return authCode;
    }
}
