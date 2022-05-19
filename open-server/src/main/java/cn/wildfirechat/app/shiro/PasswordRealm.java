package cn.wildfirechat.app.shiro;


import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.jpa.User;
import cn.wildfirechat.app.jpa.UserRepository;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static cn.wildfirechat.app.RestResult.RestCode.SUCCESS;

@Service
public class PasswordRealm extends AuthorizingRealm {

    @Autowired
    private PasswordMatcher passwordMatcher;
    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    private void initMatcher() {
        setCredentialsMatcher(passwordMatcher);
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        if(!principalCollection.isEmpty()) {
            String account = (String)principalCollection.getPrimaryPrincipal();
            Optional<User> optionalUser = userRepository.findByAccount(account);

            if(optionalUser.isPresent()) {
                Set<String> stringSet = new HashSet<>();
                // TODO 改为基于角色授权，这样子的话，在数据库里面配置角色就行，权限的话，数据库里面没有权限，而这儿通过代码添加权限
                Set<String> roleSet = new HashSet<>();
                roleSet.add(optionalUser.get().getRole());
                info.setRoles(roleSet);

                if ("admin".equals(optionalUser.get().getRole())) {
                    stringSet.add("user:view");
                    stringSet.add("user:admin");
                } else if("user".equals(optionalUser.get().getRole())) {
                    stringSet.add("user:view");
                }
                info.setStringPermissions(stringSet);
            }
        }

        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        if(authenticationToken instanceof UsernamePasswordToken) {
            String account = (String) authenticationToken.getPrincipal();
            Optional<User> optionalUser = userRepository.findByAccount(account);
            if (optionalUser.isPresent()) {
                SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(account, optionalUser.get().getPasswordMd5(), getName());
                simpleAuthenticationInfo.setCredentialsSalt(ByteSource.Util.bytes(optionalUser.get().getSalt()));
                return simpleAuthenticationInfo;
            }
        }
        return null;
    }
}