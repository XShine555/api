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

    @Autowired
    private com.musify.logging.CustomLogging logger;

    private static class CustomMapper implements RowMapper<User> {
        @Override
        public User mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setPasswordHash(rs.getString("password_hash"));
            user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return user;
        }
    }

    private final RowMapper<User> userRowMapper = new CustomMapper();

    public int create(User user) {
        logger.info(getClass().getSimpleName(), "create", "Creating user with username: " + user.getUsername());
        String sql = "INSERT INTO users (username, password_hash, image_path) VALUES (?, ?, ?)";
        int rowsAffected = jdbcTemplate.update(sql, user.getUsername(), user.getPasswordHash(), user.getImagePath());
        logger.info(getClass().getSimpleName(), "create", "User created successfully with username: " + user.getUsername() + ", rows affected: " + rowsAffected);
        return rowsAffected;
    }

    public Optional<User> findById(Long id) {
        logger.info(getClass().getSimpleName(), "findById", "Finding user by ID: " + id);
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, id);
        Optional<User> result = users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
        if (result.isPresent()) {
            logger.info(getClass().getSimpleName(), "findById", "User found with ID: " + id);
        } else {
            logger.warn(getClass().getSimpleName(), "findById", "No user found with ID: " + id);
        }
        return result;
    }

    public Optional<User> findByUsername(String username) {
        logger.info(getClass().getSimpleName(), "findByUsername", "Finding user by username: " + username);
        String sql = "SELECT * FROM users WHERE username = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, username);
        Optional<User> result = users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
        if (result.isPresent()) {
            logger.info(getClass().getSimpleName(), "findByUsername", "User found with username: " + username);
        } else {
            logger.warn(getClass().getSimpleName(), "findByUsername", "No user found with username: " + username);
        }
        return result;
    }

    public List<User> findAll() {
        logger.info(getClass().getSimpleName(), "findAll", "Retrieving all users");
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, userRowMapper);
        logger.info(getClass().getSimpleName(), "findAll", "Retrieved " + users.size() + " users");
        return users;
    }

    public int update(User user) {
        logger.info(getClass().getSimpleName(), "update", "Updating user with ID: " + user.getId());
        String sql = "UPDATE users SET username = ?, password_hash = ?, image_path = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, user.getUsername(), user.getPasswordHash(), user.getImagePath(), user.getId());
        logger.info(getClass().getSimpleName(), "update", "User updated successfully with ID: " + user.getId() + ", rows affected: " + rowsAffected);
        return rowsAffected;
    }

    public void deleteById(Long id) {
        logger.info(getClass().getSimpleName(), "deleteById", "Deleting user with ID: " + id);
        String sql = "DELETE FROM users WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        logger.info(getClass().getSimpleName(), "deleteById", "User deleted with ID: " + id + ", rows affected: " + rowsAffected);
    }

    public void deleteAll() {
        logger.info(getClass().getSimpleName(), "deleteAll", "Deleting all users");
        String sql = "DELETE FROM users";
        int rowsAffected = jdbcTemplate.update(sql);
        logger.info(getClass().getSimpleName(), "deleteAll", "All users deleted, rows affected: " + rowsAffected);
    }
}
