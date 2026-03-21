package org.example.service;

import org.example.model.Habit;
import org.example.model.HabitLog;
import org.example.model.User;
import org.example.repository.HabitRepository;
import org.example.repository.HabitLogRepository;
import java.util.List;

public class HabitService {
    private final HabitRepository habitRepo = new HabitRepository();
    private final HabitLogRepository logRepo = new HabitLogRepository();
    private final UserService userService = new UserService();

    public String create(long chatId, String name, String category, String time) {
        User user = userService.getByTelegramId(chatId);
        if (user == null) {
            return "❌ Ошибка: пользователь не найден";
        }

        Habit h = new Habit();
        h.setUserId(user.getId());
        h.setName(name);
        h.setCategory(category);
        h.setReminderTime(time);
        habitRepo.save(h);
        return "✅ Привычка \"" + name + "\" создана!";
    }

    public String getList(long chatId) {
        User user = userService.getByTelegramId(chatId);
        if (user == null) {
            return "❌ Ошибка: пользователь не найден";
        }

        List<Habit> habits = habitRepo.findByUserId(user.getId());
        if (habits.isEmpty()) {
            return "📭 У вас нет привычек. Создайте через /create";
        }

        StringBuilder sb = new StringBuilder("📋 **Ваши привычки:**\n\n");
        for (int i = 0; i < habits.size(); i++) {
            Habit h = habits.get(i);
            HabitLog log = logRepo.findTodayLog(h.getId());
            String status = (log != null && "DONE".equals(log.getStatus())) ? "✅" : "⭕";
            sb.append(i+1).append(". ").append(status).append(" ")
                    .append(h.getName()).append(" (").append(h.getReminderTime()).append(")\n");
        }
        return sb.toString();
    }

    public String getHabitsForButtons(long chatId) {
        User user = userService.getByTelegramId(chatId);
        if (user == null) return "";

        List<Habit> habits = habitRepo.findByUserId(user.getId());
        StringBuilder sb = new StringBuilder();
        for (Habit h : habits) {
            sb.append(h.getId()).append(":").append(h.getName()).append(";");
        }
        return sb.toString();
    }

    public String markDone(long chatId, int habitId) {
        User user = userService.getByTelegramId(chatId);
        if (user == null) {
            return "❌ Ошибка: пользователь не найден";
        }

        Habit habit = habitRepo.findById(habitId);
        if (habit == null || habit.getUserId() != user.getId()) {
            return "❌ Привычка не найдена";
        }

        HabitLog log = logRepo.findTodayLog(habitId);
        if (log == null) {
            log = new HabitLog();
            log.setHabitId(habitId);
            log.setStatus("DONE");
            logRepo.save(log);
            return "✅ Отлично! Привычка \"" + habit.getName() + "\" отмечена!";
        } else if (!"DONE".equals(log.getStatus())) {
            logRepo.updateStatus(log.getId(), "DONE");
            return "✅ Отлично! Привычка \"" + habit.getName() + "\" отмечена!";
        } else {
            return "❌ Вы уже отметили эту привычку сегодня!";
        }
    }
}