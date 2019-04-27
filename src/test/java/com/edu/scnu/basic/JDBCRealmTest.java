package com.edu.scnu.basic;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.subject.Subject;
import org.junit.Before;
import org.junit.Test;

/**
 * @ClassName JDBCRealmTest
 * @Description Shiro框架中 JDBCRealm 的使用
 *      数据库中新建的表：
 *          users :   id | username | password
 *          user_roles:  id | username | role_name
 *          roles_permission: id | role_name | permission
 * @Author Autom
 * @Date 2019-04-27 14:22
 * @Version 1.0
 **/
public class JDBCRealmTest {

    private DruidDataSource druidDataSource;
    private JdbcRealm jdbcRealm;

    {
        druidDataSource = new DruidDataSource();
        druidDataSource.setUrl("jdbc:mysql://192.168.56.101:3306/test?useSSL=false");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("123456");

        jdbcRealm = new JdbcRealm();
    }

    @Before
    public void initRealm() {
        jdbcRealm.setDataSource(druidDataSource);
        jdbcRealm.setPermissionsLookupEnabled(true);
        String userSql = "select password from t_user where username = ?";
        jdbcRealm.setAuthenticationQuery(userSql);

        String roleSql = "select role_name from t_user_role where user_id = ?";
        jdbcRealm.setUserRolesQuery(roleSql);
    }

    @Test
    public void testAuthentication() {
        // 1. 构建securityManager 环境
        DefaultSecurityManager securityManager = new DefaultSecurityManager();
        // 将realm添加到环境中（就这个地方不一样了）
        securityManager.setRealm(jdbcRealm);

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
        securityManager.setRealm(jdbcRealm);

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
