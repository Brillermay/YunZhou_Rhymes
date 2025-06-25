package com.example.bg.config;

import com.example.bg.user.User;
import com.example.bg.user.UserMapper;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.dao.DataAccessException;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

public class UserRealm extends AuthorizingRealm {

    private final UserMapper userMapper;

    @Autowired
    public UserRealm(UserMapper userMapper) {
        this.userMapper  = userMapper;
    }

    // 认证逻辑
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(
            AuthenticationToken token) throws AuthenticationException {

        try {
            String username = (String) token.getPrincipal();
            User user = userMapper.findByUsername(username);

            if (user == null) {
                throw new UnknownAccountException("用户不存在"+username);
            }
            if ("locked".equals(user.getStatus()))  {
                throw new LockedAccountException("账户已锁定");
            }

            // 防御性检查
            if (user.getSalt()  == null || user.getPwd()  == null) {
                throw new AuthenticationException("用户凭证数据不完整");
            }

            return new SimpleAuthenticationInfo(
                    username,
                    user.getPwd(),
                    ByteSource.Util.bytes(user.getSalt()),
                    getName()
            );
        } catch (DataAccessException e) { // 捕获所有数据访问异常
            throw new AuthenticationException("数据库访问失败", e);
        }
    }

    // 授权逻辑
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return null;
    }
}