package com.example.bg.config;

import com.example.bg.user.UserMapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

//    @Value("${spring.data.redis.password}")
//    private String redisPassword;

    @Value("${shiro.session.timeout}")
    private int sessionTimeout;

    // Redis管理器
    @Bean
    public RedisManager redisManager() {
        RedisManager manager = new RedisManager();
        manager.setHost(redisHost  + ":" + redisPort);
//        manager.setPassword(redisPassword);
        manager.setTimeout(2000);
        return manager;
    }

    // Redis会话存储
    @Bean
    public SessionDAO sessionDAO(RedisManager redisManager) {
        RedisSessionDAO dao = new RedisSessionDAO();
        dao.setRedisManager(redisManager);
        dao.setExpire(sessionTimeout);  // 使用配置的超时时间
        dao.setKeyPrefix("shiro_session:");
        return dao;
    }

    // 会话管理器
    @Bean
    public SessionManager sessionManager(SessionDAO sessionDAO) {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setSessionDAO(sessionDAO);
        sessionManager.setSessionIdUrlRewritingEnabled(false);
        sessionManager.setGlobalSessionTimeout(sessionTimeout  * 1000L); // 转为毫秒
        return sessionManager;
    }

    @Bean
    public MethodInvokingFactoryBean securityManagerInjector(@Lazy SecurityManager securityManager) {
        MethodInvokingFactoryBean bean = new MethodInvokingFactoryBean();
        bean.setStaticMethod("org.apache.shiro.SecurityUtils.setSecurityManager");
        bean.setArguments(new  Object[]{securityManager});
        return bean;
    }

    // 安全域（使用构造器注入Mapper）
    @Bean
    public UserRealm userRealm(UserMapper userMapper) {
        UserRealm realm = new UserRealm(userMapper);

        HashedCredentialsMatcher matcher = new HashedCredentialsMatcher();
        matcher.setHashAlgorithmName("SHA-256");
        matcher.setHashIterations(1024);
        realm.setCredentialsMatcher(matcher);

        return realm;
    }

    // 安全核心管理器
    @Bean
    public SecurityManager securityManager(UserRealm userRealm, SessionManager sessionManager) {
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
        manager.setRealm(userRealm);
        ThreadContext.bind(manager);//加上这句代码手动绑定
        manager.setSessionManager(sessionManager);
        SecurityUtils.setSecurityManager(manager);
        return manager;
    }

    // Shiro过滤器配置
    @Bean
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
        ShiroFilterFactoryBean filter = new ShiroFilterFactoryBean();
        filter.setSecurityManager(securityManager);

        Map<String, String> filterChain = new LinkedHashMap<>();
        filterChain.put("/user/login",  "anon");
        filterChain.put("/user/add",  "anon");
        filterChain.put("/**",  "authc");

        filter.setFilterChainDefinitionMap(filterChain);
        filter.setLoginUrl("/user/login");
        filter.setUnauthorizedUrl("/unauthorized");

        return filter;
    }
//    @Bean
//    @DependsOn("securityManager")
//    public MethodInvokingFactoryBean methodInvokingFactoryBean(SecurityManager securityManager) {
//        MethodInvokingFactoryBean factoryBean = new MethodInvokingFactoryBean();
//        factoryBean.setStaticMethod("org.apache.shiro.SecurityUtils.setSecurityManager");
//        factoryBean.setArguments(new  Object[]{securityManager});
//        return factoryBean;
//    }
}