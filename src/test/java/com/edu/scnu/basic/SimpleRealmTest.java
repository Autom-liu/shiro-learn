package com.edu.scnu.basic;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.subject.Subject;
import org.junit.Before;
import org.junit.Test;

/**
 * @ClassName SimpleRealmTest
 * @Description Shiro框架中 SimpleAccountRealm 的使用
 * @Author Autom
 * @Date 2019-04-27 13:45
 * @Version 1.0
 **/
public class SimpleRealmTest {

    private SimpleAccountRealm simpleRealm = new SimpleAccountRealm();

    @Before
    public void initRealm() {
        simpleRealm.addAccount("Tom", "123456", "admin", "public");
    }

    @Test
    public void testAuthentication() {
        // 1. 构建securityManager 环境
        DefaultSecurityManager securityManager = new DefaultSecurityManager();
        // 将realm添加到环境中
        securityManager.setRealm(simpleRealm);

        // 2. 主体提交认证请求
        SecurityUtils.setSecurityManager(securityManager);
        Subject subject = SecurityUtils.getSubject();

        UsernamePasswordToken token = new UsernamePasswordToken("Tom", "123456");
        // 3. 主体登录
        subject.login(token);
        // 4. 校验主体是否登录
        System.out.println("subject.isAuthenticated: " + subject.isAuthenticated());

        // 5. 退出操作
        subject.logout();

        System.out.println("subject.isAuthenticated after logout : " + subject.isAuthenticated());
    }

    @Test
    public void testPrivilege() {
        // 1. 构建securityManager 环境
        DefaultSecurityManager securityManager = new DefaultSecurityManager();
        // 将realm添加到环境中
        securityManager.setRealm(simpleRealm);

        // 2. 主体提交认证请求
        SecurityUtils.setSecurityManager(securityManager);
        Subject subject = SecurityUtils.getSubject();

        UsernamePasswordToken token = new UsernamePasswordToken("Tom", "123456");
        // 3. 主体登录
        subject.login(token);
        // 4. 校验主体是否有权限
        try {

            subject.checkRole("admin0");
        } catch (UnauthorizedException e) {
            System.out.println(e.getMessage());
        }

    }

}
