package com.edu.scnu.basic;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.subject.Subject;
import org.junit.Before;
import org.junit.Test;

/**
 * @ClassName iniRealmTest
 * @Description Shiro框架中 iniRealmTest 的使用
 * @Author Autom
 * @Date 2019-04-27 14:06
 * @Version 1.0
 **/
public class iniRealmTest {

    private IniRealm iniRealm = null;

    @Before
    public void initRealm() {
        iniRealm = new IniRealm("classpath:user.ini");
    }

    @Test
    public void testAuthentication() {
        // 1. 构建securityManager 环境
        DefaultSecurityManager securityManager = new DefaultSecurityManager();
        // 将realm添加到环境中（就这个地方不一样了）
        securityManager.setRealm(iniRealm);

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
        securityManager.setRealm(iniRealm);

        // 2. 主体提交认证请求
        SecurityUtils.setSecurityManager(securityManager);
        Subject subject = SecurityUtils.getSubject();

        UsernamePasswordToken token = new UsernamePasswordToken("Tom", "123456");
        // 3. 主体登录
        subject.login(token);
        // 4. 校验主体是否有权限
        try {

            subject.checkRole("admin");

            subject.checkPermission("user:add");

            subject.checkPermission("user:update");
        } catch (UnauthorizedException e) {
            System.out.println(e.getMessage());
        }
    }
}
