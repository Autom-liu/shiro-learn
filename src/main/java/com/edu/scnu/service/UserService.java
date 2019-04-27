package com.edu.scnu.service;


import com.edu.scnu.bean.User;

import java.util.Set;

public interface UserService {
    User findByUsername(String username);

    Set<String> getRolesByUsername(String username);

    Set<String> findPermissionByUsername(String username);
}
