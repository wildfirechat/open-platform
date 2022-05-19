package cn.wildfirechat.app.shiro;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.jpa.User;
import cn.wildfirechat.app.jpa.UserRepository;
import cn.wildfirechat.pojos.InputOutputUserInfo;
import cn.wildfirechat.sdk.UserAdmin;
import cn.wildfirechat.sdk.model.IMResult;
import cn.wildfirechat.sdk.utilities.AdminHttpUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class PasswordMatcher implements CredentialsMatcher {

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        if (token instanceof UsernamePasswordToken && info instanceof SimpleAuthenticationInfo) {
            UsernamePasswordToken passwordToken = (UsernamePasswordToken) token;
            String pwd = new String(passwordToken.getPassword());
            SimpleAuthenticationInfo authenticationInfo = (SimpleAuthenticationInfo)info;
            String salt = new String(authenticationInfo.getCredentialsSalt().getBytes());

            String md5 = new Base64().encodeToString(DigestUtils.getDigest("MD5").digest((pwd + salt).getBytes(StandardCharsets.UTF_8)));
            if(md5.equals(authenticationInfo.getCredentials())) {
                return true;
            }
        }
        return false;
    }
}
