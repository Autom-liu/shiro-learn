package com.edu.scnu.service.impl;

import com.edu.scnu.bean.User;
import com.edu.scnu.dao.UserDao;
import com.edu.scnu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @ClassName UserServiceImpl
 * @Description 用户相关服务实现类
 * @Author Administrator
 * @Date 2019-04-27 22:21
 * @Version 1.0
 **/
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;


    @Override
    public User findByUsername(String username) {
        List<User> users = userDao.findByUsername(username);
        if(CollectionUtils.isEmpty(users)) {
            return null;
        }
        return users.get(0);
    }

    @Override
    public Set<String> getRolesByUsername(String username) {
        List<String> roles = userDao.findRolesByUsername(username);

        return new HashSet<>(roles);
    }

    @Override
    public Set<String> findPermissionByUsername(String username) {
        List<String> roles = userDao.findRolesByUsername(username);
        Set<String> result = new HashSet<>();
        for (String role : roles ) {
            List<String> permissions = userDao.findPermissionByRole(role);
            if(!CollectionUtils.isEmpty(permissions)) {
                result.addAll(permissions);
            }
        }
        return result;
    }
}
