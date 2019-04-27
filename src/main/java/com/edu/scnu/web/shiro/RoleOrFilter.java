package com.edu.scnu.web.shiro;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * @ClassName RoleOrFilter
 * @Description 角色匹配类
 * @Author Administrator
 * @Date 2019-04-27 23:07
 * @Version 1.0
 **/
@Component
public class RoleOrFilter extends AuthorizationFilter {

    /**
    * @Author Autom
    * @Description 用于判断角色是否匹配的过滤器
    * @Date 23:08 2019-04-27
    * @Param  request
     * @Param response
     * @Param mappedValue  配置的值
    * @return boolean
    **/
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        Subject subject = getSubject(request, response);
        String[] roles = (String[]) mappedValue;
        if(roles == null || roles.length == 0) {
            return true;
        }
        for (String role: roles) {
            if (subject.hasRole(role)) {
                return true;
            }
        }
        return false;
    }
}
