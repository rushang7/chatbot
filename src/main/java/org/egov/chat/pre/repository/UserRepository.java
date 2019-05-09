package org.egov.chat.pre.repository;

import org.egov.chat.pre.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String insertUserQuery = "INSERT INTO eg_chat_user (user_id, mobile_number, auth_token, " +
            "refresh_token, user_info, expires_at) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String updateDetailsForUserQuery = "UPDATE eg_chat_user SET auth_token=?, refresh_token=?, " +
            "user_info=?, expires_at=? WHERE user_id=?";

    private static final String selectUserForMobileNumberQuery = "SELECT * FROM eg_chat_user WHERE mobile_number=?";

    public int insertUser(User user) {
        return jdbcTemplate.update(insertUserQuery,
                user.getUserId(),
                user.getMobileNumber(),
                user.getAuthToken(),
                user.getRefreshToken(),
                user.getUserInfo(),
                user.getExpiresAt());
    }

    public int updateUserDetails(User user) {
        return jdbcTemplate.update(updateDetailsForUserQuery,
                user.getAuthToken(),
                user.getRefreshToken(),
                user.getUserInfo(),
                user.getExpiresAt(),
                user.getUserId());
    }

    public User getUserForMobileNumber(String mobileNumber) {
        try {
            return jdbcTemplate.queryForObject(selectUserForMobileNumberQuery, new Object[]{mobileNumber},
                    new BeanPropertyRowMapper<>(User.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

}
