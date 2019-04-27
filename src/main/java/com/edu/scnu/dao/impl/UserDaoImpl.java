package com.edu.scnu.dao.impl;

import com.edu.scnu.bean.User;
import com.edu.scnu.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @ClassName UserDaoImpl
 * @Description TODO
 * @Author Administrator
 * @Date 2019-04-27 22:05
 * @Version 1.0
 **/
@Component
public class UserDaoImpl implements UserDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<User> findByUsername(String username) {
        String sql = "select username, password from t_user where username = ?";
        return jdbcTemplate.query(sql, new String[]{username}, new RowMapper<User>() {
            @Override
            public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                User user = new User();
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                return user;
            }
        });
    }

    @Override
    public List<String> findRolesByUsername(String username) {
        String sql = "select role_name from t_user_role where username = ?";
        return jdbcTemplate.queryForList(sql, String.class, username);
    }

    @Override
    public List<String> findPermissionByRole(String role) {
        String sql = "select permission from t_role_permission where role_name = ?";
        return jdbcTemplate.queryForList(sql, String.class, role);
    }
}
