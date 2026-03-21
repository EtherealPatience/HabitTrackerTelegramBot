package org.example.repository;

import org.example.model.Habit;
import org.example.config.DatabaseConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HabitRepository {

    public List<Habit> findByUserId(int userId) {
        List<Habit> list = new ArrayList<>();
        String sql = "SELECT * FROM Habits WHERE userId = ? AND isActive = true ORDER BY createdAt DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Habit h = new Habit();
                h.setId(rs.getInt("id"));
                h.setUserId(rs.getInt("userId"));
                h.setName(rs.getString("name"));
                h.setCategory(rs.getString("category"));
                h.setReminderTime(rs.getString("reminderTime"));

                Timestamp ts = rs.getTimestamp("createdAt");
                if (ts != null) {
                    h.setCreatedAt(ts.toLocalDateTime());
                }

                h.setActive(rs.getBoolean("isActive"));
                list.add(h);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка findByUserId: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public Habit findById(int id) {
        String sql = "SELECT * FROM Habits WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Habit h = new Habit();
                h.setId(rs.getInt("id"));
                h.setUserId(rs.getInt("userId"));
                h.setName(rs.getString("name"));
                h.setCategory(rs.getString("category"));
                h.setReminderTime(rs.getString("reminderTime"));

                Timestamp ts = rs.getTimestamp("createdAt");
                if (ts != null) {
                    h.setCreatedAt(ts.toLocalDateTime());
                }

                h.setActive(rs.getBoolean("isActive"));
                return h;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка findById: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void save(Habit habit) {
        String sql = "INSERT INTO Habits (userId, name, category, reminderTime, createdAt, isActive) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, habit.getUserId());
            stmt.setString(2, habit.getName());
            stmt.setString(3, habit.getCategory());
            stmt.setString(4, habit.getReminderTime());
            stmt.setTimestamp(5, Timestamp.valueOf(habit.getCreatedAt()));
            stmt.setBoolean(6, habit.isActive());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                habit.setId(keys.getInt(1));
            }
            System.out.println("✅ Привычка добавлена: " + habit.getName());
        } catch (SQLException e) {
            System.err.println("Ошибка save habit: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Habit> findAllActive() {
        List<Habit> list = new ArrayList<>();
        String sql = "SELECT * FROM Habits WHERE isActive = true";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Habit h = new Habit();
                h.setId(rs.getInt("id"));
                h.setUserId(rs.getInt("userId"));
                h.setName(rs.getString("name"));
                h.setCategory(rs.getString("category"));
                h.setReminderTime(rs.getString("reminderTime"));
                h.setActive(rs.getBoolean("isActive"));
                list.add(h);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка findAllActive: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    // Добавь эти методы в конец класса HabitRepository

    public void update(Habit habit) {
        String sql = "UPDATE Habits SET name=?, category=?, reminderTime=? WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, habit.getName());
            stmt.setString(2, habit.getCategory());
            stmt.setString(3, habit.getReminderTime());
            stmt.setInt(4, habit.getId());
            stmt.executeUpdate();
            System.out.println("✅ Привычка обновлена: " + habit.getName());
        } catch (SQLException e) {
            System.err.println("Ошибка update: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM Habits WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("🗑️ Привычка удалена, id: " + id);
        } catch (SQLException e) {
            System.err.println("Ошибка delete: " + e.getMessage());
            e.printStackTrace();
        }
    }
}