package com.edu.scnu.basic;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.*;

/**
 * @ClassName CustomRealm
 * @Description 使用自定义Realm
 * @Author Administrator
 * @Date 2019-04-27 14:49
 * @Version 1.0
 **/
public class CustomRealm extends AuthorizingRealm {

    private Map<String, String> userMap;

    private Map<String, Set<String>> roleMap;

    private Map<String, Set<String>> permissionMap;

    {
        super.setName("customRealm");
        userMap = new HashMap<>();
        userMap.put("Tom", "123456");

        roleMap = new HashMap<>();
        Set<String> roles = new HashSet<String>() {{
            add("admin");
            add("public");
        }};
        roleMap.put("Tom", roles);

        permissionMap = new HashMap<>();
        Set<String> permission = new HashSet<String>() {{
           add("user:add");
           add("user:delete");
        }};
        permissionMap.put("admin", permission);
    }


    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String username = (String) principals.getPrimaryPrincipal();
        Set<String> roles = this.getRoleByUsername(username);
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        authorizationInfo.setRoles(roles);
        Set<String> permissions = this.getPermissionByUsername(username);
        authorizationInfo.setStringPermissions(permissions);
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
        String password = this.getPasswordByUsername(username);
        if (password == null) {
            return null;
        }

        return new SimpleAuthenticationInfo("Tom", password, "customRealm");
    }

    private String getPasswordByUsername(String username) {
        return userMap.get(username);
    }

    private Set<String> getRoleByUsername(String username) {
        return roleMap.get(username);
    }

    private Set<String> getPermissionByUsername(String username) {
        Set<String> roles = this.getRoleByUsername(username);
        Set<String> result = new HashSet<>();
        for (String role : roles) {
            Set<String> permissions = permissionMap.get(role);
            if(permissions != null) {
                result.addAll(permissions);
            }
        }
        return result;
    }
}
