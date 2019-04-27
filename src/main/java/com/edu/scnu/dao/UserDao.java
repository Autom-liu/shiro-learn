package com.edu.scnu.dao;

import com.edu.scnu.bean.User;

import java.util.List;

public interface UserDao {
    List<User> findByUsername(String username);

    List<String> findRolesByUsername(String username);

    List<String> findPermissionByRole(String role);
}
