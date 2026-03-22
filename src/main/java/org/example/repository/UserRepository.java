package org.example.repository;

import org.example.model.User;
import org.example.config.DatabaseConfig;
import java.sql.*;
import java.time.LocalDateTime;

public class UserRepository {

    public User findByTelegramId(long telegramId) {
        String sql = "SELECT * FROM Users WHERE telegramId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, telegramId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setTelegramId(rs.getLong("telegramId"));
                u.setUsername(rs.getString("username"));
                u.setFirstName(rs.getString("firstName"));

                Timestamp regTs = rs.getTimestamp("registeredDate");
                if (regTs != null) {
                    u.setRegisteredDate(regTs.toLocalDateTime());
                }

                Timestamp lastTs = rs.getTimestamp("lastActive");
                if (lastTs != null) {
                    u.setLastActive(lastTs.toLocalDateTime());
                }

                // Добавь эту строку!
                u.setNotificationsEnabled(rs.getBoolean("notificationsEnabled"));

                return u;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка findByTelegramId: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void save(User user) {
        if (findByTelegramId(user.getTelegramId()) == null) {
            insert(user);
        } else {
            update(user);
        }
    }

    private void insert(User user) {
        String sql = "INSERT INTO Users (telegramId, username, firstName, registeredDate, lastActive, notificationsEnabled) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, user.getTelegramId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getFirstName());
            stmt.setTimestamp(4, Timestamp.valueOf(user.getRegisteredDate()));
            stmt.setTimestamp(5, Timestamp.valueOf(user.getLastActive()));
            stmt.setBoolean(6, user.isNotificationsEnabled());  // Это поле должно сохраняться!
            stmt.executeUpdate();
            System.out.println("✅ Пользователь добавлен: " + user.getTelegramId() +
                    ", notificationsEnabled=" + user.isNotificationsEnabled());
        } catch (SQLException e) {
            System.err.println("Ошибка insert: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void update(User user) {
        String sql = "UPDATE Users SET username=?, firstName=?, lastActive=?, notificationsEnabled=? WHERE telegramId=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getFirstName());
            stmt.setTimestamp(3, Timestamp.valueOf(user.getLastActive()));
            stmt.setBoolean(4, user.isNotificationsEnabled());  // Это поле должно сохраняться!
            stmt.setLong(5, user.getTelegramId());
            stmt.executeUpdate();
            System.out.println("✅ Пользователь обновлен: " + user.getTelegramId() +
                    ", notificationsEnabled=" + user.isNotificationsEnabled());
        } catch (SQLException e) {
            System.err.println("Ошибка update: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Добавь этот метод в конец класса UserRepository

    public User findById(int id) {
        String sql = "SELECT * FROM Users WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setTelegramId(rs.getLong("telegramId"));
                u.setUsername(rs.getString("username"));
                u.setFirstName(rs.getString("firstName"));

                Timestamp regTs = rs.getTimestamp("registeredDate");
                if (regTs != null) {
                    u.setRegisteredDate(regTs.toLocalDateTime());
                }

                Timestamp lastTs = rs.getTimestamp("lastActive");
                if (lastTs != null) {
                    u.setLastActive(lastTs.toLocalDateTime());
                }

                // ЭТА СТРОКА БЫЛА ПРОПУЩЕНА!
                u.setNotificationsEnabled(rs.getBoolean("notificationsEnabled"));

                return u;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка findById: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}