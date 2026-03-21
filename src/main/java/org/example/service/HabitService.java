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

    // Получить список привычек для выбора (с номерами)
    public String getHabitsListForChoice(long chatId, String action) {
        User user = userService.getByTelegramId(chatId);
        if (user == null) {
            return "❌ Ошибка: пользователь не найден";
        }

        List<Habit> habits = habitRepo.findByUserId(user.getId());
        if (habits.isEmpty()) {
            return "📭 У вас нет привычек. Создайте через /create";
        }

        StringBuilder sb = new StringBuilder();
        if ("edit".equals(action)) {
            sb.append("✏️ **Выберите привычку для редактирования:**\n\n");
        } else if ("delete".equals(action)) {
            sb.append("🗑️ **Выберите привычку для удаления:**\n\n");
        } else if ("done".equals(action)) {
            sb.append("✅ **Выберите привычку для отметки выполнения:**\n\n");
        } else {
            sb.append("📋 **Выберите привычку:**\n\n");
        }

        for (int i = 0; i < habits.size(); i++) {
            Habit h = habits.get(i);

            // Получаем статус выполнения на сегодня
            HabitLog log = logRepo.findTodayLog(h.getId());
            String statusIcon;
            if (log != null && "DONE".equals(log.getStatus())) {
                statusIcon = "✅";  // Выполнено
            } else {
                statusIcon = "⭕";  // Не выполнено
            }

            sb.append(i + 1).append(". ").append(statusIcon).append(" ")
                    .append(h.getName()).append(" (").append(h.getReminderTime()).append(")\n");
        }

        if ("done".equals(action)) {
            sb.append("\n📝 Введите номер привычки, которую хотите отметить:");
        } else {
            sb.append("\n📝 Введите номер привычки:");
        }
        return sb.toString();
    }

    // Получить привычку по индексу (из списка)
    public Habit getHabitByIndex(long chatId, int index) {
        User user = userService.getByTelegramId(chatId);
        if (user == null) return null;

        List<Habit> habits = habitRepo.findByUserId(user.getId());
        if (habits.isEmpty() || index < 1 || index > habits.size()) {
            return null;
        }
        return habits.get(index - 1);
    }

    // Редактирование: получить текущие данные привычки для отображения
    public String getHabitDetailsForEdit(Habit habit) {
        return String.format(
                "✏️ **Редактирование привычки:**\n\n" +
                        "1️⃣ Название: %s\n" +
                        "2️⃣ Категория: %s\n" +
                        "3️⃣ Время: %s\n\n" +
                        "Введите номер поля, которое хотите изменить (1, 2 или 3), или 0 для завершения:",
                habit.getName(), habit.getCategory(), habit.getReminderTime()
        );
    }

    // Редактирование: обновить название
    public String updateName(Habit habit, String newName) {
        String oldName = habit.getName();
        habit.setName(newName);
        habitRepo.update(habit);
        return "✅ Название изменено с \"" + oldName + "\" на \"" + newName + "\"";
    }

    // Редактирование: обновить категорию
    public String updateCategory(Habit habit, String newCategory) {
        String oldCategory = habit.getCategory();
        habit.setCategory(newCategory);
        habitRepo.update(habit);
        return "✅ Категория изменена с \"" + oldCategory + "\" на \"" + newCategory + "\"";
    }

    // Редактирование: обновить время
    public String updateTime(Habit habit, String newTime) {
        if (!newTime.matches("([01]?[0-9]|2[0-3]):[0-5][0-9]")) {
            return "❌ Неверный формат времени! Используйте ЧЧ:ММ, например 09:00";
        }
        String oldTime = habit.getReminderTime();
        habit.setReminderTime(newTime);
        habitRepo.update(habit);
        return "✅ Время изменено с \"" + oldTime + "\" на \"" + newTime + "\"";
    }

    // Удаление привычки
    public String deleteHabit(Habit habit) {
        String habitName = habit.getName();
        habitRepo.delete(habit.getId());
        return "🗑️ Привычка \"" + habitName + "\" удалена!";
    }

    public String getHabitsListForDone(long chatId) {
        User user = userService.getByTelegramId(chatId);
        if (user == null) {
            return "❌ Ошибка: пользователь не найден";
        }

        List<Habit> habits = habitRepo.findByUserId(user.getId());
        if (habits.isEmpty()) {
            return "📭 У вас нет привычек. Создайте через /create";
        }

        StringBuilder sb = new StringBuilder("📋 **Выберите привычку для отметки:**\n\n");
        for (int i = 0; i < habits.size(); i++) {
            Habit h = habits.get(i);
            sb.append(i + 1).append(". ").append(h.getName())
                    .append(" (").append(h.getReminderTime()).append(")\n");
        }
        sb.append("\n📝 Введите номер привычки:");
        return sb.toString();
    }

    public String markDoneByIndex(long chatId, int index) {
        Habit habit = getHabitByIndex(chatId, index);
        if (habit == null) {
            return "❌ Привычка не найдена";
        }
        return markDone(chatId, habit.getId());
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

    // Добавь этот метод в HabitService

    public Habit getHabitById(long chatId, int habitId) {
        User user = userService.getByTelegramId(chatId);
        if (user == null) return null;

        Habit habit = habitRepo.findById(habitId);
        if (habit == null || habit.getUserId() != user.getId()) {
            return null;
        }
        return habit;
    }

    // Метод для получения списка привычек с отображением статуса (для undone)
    // Метод для получения списка привычек с отображением статуса (для undone)
    public String getHabitsListForUndone(long chatId) {
        User user = userService.getByTelegramId(chatId);
        if (user == null) {
            return "❌ Ошибка: пользователь не найден";
        }

        List<Habit> habits = habitRepo.findByUserId(user.getId());
        if (habits.isEmpty()) {
            return "📭 У вас нет привычек. Создайте через /create";
        }

        StringBuilder sb = new StringBuilder("🔄 **Выберите привычку для отмены выполнения:**\n\n");

        int completedCount = 0;

        for (int i = 0; i < habits.size(); i++) {
            Habit h = habits.get(i);

            // Получаем статус выполнения на сегодня
            HabitLog log = logRepo.findTodayLog(h.getId());
            boolean isDone = (log != null && "DONE".equals(log.getStatus()));

            if (isDone) {
                completedCount++;
                sb.append(i + 1).append(". ✅ **").append(h.getName()).append("**");
                sb.append(" — выполнено сегодня\n");
            } else {
                sb.append(i + 1).append(". ⭕ ").append(h.getName());
                sb.append(" (").append(h.getReminderTime()).append(")");
                sb.append(" — не выполнено\n");
            }
        }

        sb.append("\n📊 **Статистика на сегодня:**\n");
        sb.append("✅ Выполнено: ").append(completedCount).append("\n");
        sb.append("⭕ Не выполнено: ").append(habits.size() - completedCount).append("\n\n");

        if (completedCount == 0) {
            sb.append("⚠️ У вас нет выполненных привычек сегодня! Нечего отменять.\n");
            return sb.toString();
        }

        sb.append("📝 Введите номер привычки, выполнение которой нужно отменить:");
        return sb.toString();
    }

    // Метод для отмены выполнения привычки
    public String markUndone(long chatId, int index) {
        User user = userService.getByTelegramId(chatId);
        if (user == null) {
            return "❌ Ошибка: пользователь не найден";
        }

        List<Habit> habits = habitRepo.findByUserId(user.getId());
        if (habits.isEmpty()) {
            return "📭 У вас нет привычек";
        }

        if (index < 1 || index > habits.size()) {
            return "❌ Неверный номер. Введите число от 1 до " + habits.size();
        }

        Habit habit = habits.get(index - 1);

        // Проверяем, выполнена ли привычка сегодня
        HabitLog log = logRepo.findTodayLog(habit.getId());

        if (log == null || !"DONE".equals(log.getStatus())) {
            return "❌ Привычка \"" + habit.getName() + "\" сегодня не была отмечена как выполненная!";
        }

        // Удаляем запись о выполнении
        if (log.getId() != 0) {
            // Если есть запись в БД, удаляем её
            logRepo.delete(log.getId());
        } else {
            // Если по каким-то причинам записи нет, просто возвращаем ошибку
            return "❌ Ошибка: не удалось найти запись о выполнении";
        }

        return "🔄 Отлично! Выполнение привычки \"" + habit.getName() + "\" отменено. Привычка снова ожидает выполнения!";
    }
}