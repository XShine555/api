package com.musify.repositories;

import com.musify.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static class CustomMapper implements RowMapper<User> {
        @Override
        public User mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setPasswordHash(rs.getString("password_hash"));
            user.setImagePath(rs.getString("image_path"));
            user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return user;
        }
    }

    private final RowMapper<User> userRowMapper = new CustomMapper();

    public int create(User user) {
        String sql = "INSERT INTO users (username, password_hash, image_path) VALUES (?, ?, ?)";
        int rowsAffected = jdbcTemplate.update(sql, user.getUsername(), user.getPasswordHash(), user.getImagePath());
        return rowsAffected;
    }

    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, id);
        Optional<User> result = users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
        return result;
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, username);
        Optional<User> result = users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
        return result;
    }

    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, userRowMapper);
        return users;
    }

    public int update(User user) {
        String sql = "UPDATE users SET username = ?, password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, user.getUsername(), user.getPasswordHash(), user.getId());
        return rowsAffected;
    }

    public int updateWithImage(User user) {
        String sql = "UPDATE users SET username = ?, password_hash = ?, image_path = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, user.getUsername(), user.getPasswordHash(), user.getImagePath(), user.getId());
        return rowsAffected;
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public void deleteAll() {
        String sql = "DELETE FROM users";
        jdbcTemplate.update(sql);
    }
}
