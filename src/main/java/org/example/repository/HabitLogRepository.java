package org.example.repository;

import org.example.model.HabitLog;
import org.example.config.DatabaseConfig;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HabitLogRepository {

    public HabitLog findTodayLog(int habitId) {
        String sql = "SELECT * FROM HabitLogs WHERE habitId = ? AND logDate = ?";
        LocalDate today = LocalDate.now();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, habitId);
            stmt.setDate(2, Date.valueOf(today));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                HabitLog log = new HabitLog();
                log.setId(rs.getInt("id"));
                log.setHabitId(rs.getInt("habitId"));
                log.setLogDate(rs.getDate("logDate").toLocalDate());
                log.setStatus(rs.getString("status"));

                Timestamp ts = rs.getTimestamp("loggedAt");
                if (ts != null) {
                    log.setLoggedAt(ts.toLocalDateTime());
                }
                return log;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка findTodayLog: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void save(HabitLog log) {
        String sql = "INSERT INTO HabitLogs (habitId, logDate, status, loggedAt) VALUES (?,?,?,?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, log.getHabitId());
            stmt.setDate(2, Date.valueOf(log.getLogDate()));
            stmt.setString(3, log.getStatus());
            stmt.setTimestamp(4, Timestamp.valueOf(log.getLoggedAt()));
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                log.setId(keys.getInt(1));
            }
            System.out.println("✅ Лог добавлен для habitId: " + log.getHabitId());
        } catch (SQLException e) {
            System.err.println("Ошибка save log: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateStatus(int id, String status) {
        String sql = "UPDATE HabitLogs SET status=? WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            stmt.executeUpdate();
            System.out.println("✅ Статус обновлен для лога: " + id);
        } catch (SQLException e) {
            System.err.println("Ошибка updateStatus: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Добавь этот метод в конец класса HabitLogRepository

    public void delete(int id) {
        String sql = "DELETE FROM HabitLogs WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("🗑️ Лог удален, id: " + id);
        } catch (SQLException e) {
            System.err.println("Ошибка delete log: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Добавь этот метод в HabitLogRepository.java

    public List<HabitLog> findByHabitIdAndDateRange(int habitId, LocalDate startDate, LocalDate endDate) {
        List<HabitLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM HabitLogs WHERE habitId = ? AND logDate BETWEEN ? AND ? ORDER BY logDate DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, habitId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                HabitLog log = new HabitLog();
                log.setId(rs.getInt("id"));
                log.setHabitId(rs.getInt("habitId"));
                log.setLogDate(rs.getDate("logDate").toLocalDate());
                log.setStatus(rs.getString("status"));

                Timestamp ts = rs.getTimestamp("loggedAt");
                if (ts != null) {
                    log.setLoggedAt(ts.toLocalDateTime());
                }
                logs.add(log);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка findByHabitIdAndDateRange: " + e.getMessage());
            e.printStackTrace();
        }
        return logs;
    }
}