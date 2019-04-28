package com.edu.scnu.web.shiro;

import com.edu.scnu.bean.User;
import com.edu.scnu.service.UserService;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @ClassName CustomRealm
 * @Description 使用自定义Realm
 * @Author Administrator
 * @Date 2019-04-27 14:49
 * @Version 1.0
 **/
@Component
public class CustomRealm extends AuthorizingRealm {

    @Autowired
    private UserService userService;

    public CustomRealm() {
        super.setName("customRealm");
    }


    /**
     * @Author Autom
     * @Description 获取用户的角色和权限的相关逻辑，提供shiro去校验
     * @Date 15:39 2019-04-27
     * @Param [principals]
     * @return org.apache.shiro.authz.AuthorizationInfo
     **/
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        String username = (String) principals.getPrimaryPrincipal();
        Set<String> roles = userService.getRolesByUsername(username);
        System.out.println("从数据库中拿到数据....");
        authorizationInfo.setRoles(roles);
        Set<String> permission = userService.findPermissionByUsername(username);
        authorizationInfo.setStringPermissions(permission);
        return authorizationInfo;
    }

    /**
     * @Author Autom
     * @Description 认证相关逻辑
     * @Date 14:58 2019-04-27
     * @Param [token]  包含主体认证信息
     * @return org.apache.shiro.authc.AuthenticationInfo  使用SimpleAuthenticationInfo构造返回
     **/
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String username = (String) token.getPrincipal();
        User user = userService.findByUsername(username);
        if (user == null) {
            return null;
        }
        // 返回全局唯一的realm名称即可
        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(user.getUsername(), user.getPassword(), this.getClass().getName());
        return authenticationInfo;
    }

}