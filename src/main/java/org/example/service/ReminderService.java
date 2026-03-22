package org.example.service;

import org.example.model.Habit;
import org.example.model.User;
import org.example.repository.HabitRepository;
import org.example.repository.UserRepository;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ReminderService {
    private final HabitRepository habitRepo = new HabitRepository();
    private final UserRepository userRepo = new UserRepository();
    private final TelegramLongPollingBot bot;
    private final UserService userService = new UserService();

    public ReminderService(TelegramLongPollingBot bot) {
        this.bot = bot;
        startReminderChecker();
        System.out.println("✅ Сервис напоминаний запущен");
    }

    private void startReminderChecker() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkReminders();
            }
        }, 0, 60000);
    }

    private void checkReminders() {
        LocalTime now = LocalTime.now();
        String currentTime = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        System.out.println("🔍 Проверка напоминаний... Текущее время: " + currentTime);

        List<Habit> habits = habitRepo.findAllActive();
        System.out.println("📋 Всего активных привычек: " + habits.size());

        for (Habit habit : habits) {
            if (habit.getReminderTime() != null && !habit.getReminderTime().isEmpty()) {
                System.out.println("  - Привычка: " + habit.getName() +
                        ", время: " + habit.getReminderTime() +
                        ", userId: " + habit.getUserId());

                if (currentTime.equals(habit.getReminderTime())) {
                    System.out.println("⏰ Время совпало! Отправляем напоминание для: " + habit.getName());
                    sendReminder(habit);
                }
            }
        }
    }

    private void sendReminder(Habit habit) {
        // Сначала получаем пользователя по внутреннему ID из таблицы Users
        User user = userRepo.findById(habit.getUserId());
        if (user == null) {
            System.out.println("❌ Пользователь не найден по userId: " + habit.getUserId());
            return;
        }

        System.out.println("👤 Найден пользователь: telegramId=" + user.getTelegramId() +
                ", notificationsEnabled=" + user.isNotificationsEnabled() +
                ", username=" + user.getUsername());

        // Проверяем, включены ли напоминания у пользователя
        if (!user.isNotificationsEnabled()) {
            System.out.println("🔇 Напоминания отключены для пользователя: " + user.getTelegramId() +
                    " (привычка: " + habit.getName() + ")");
            return;
        }

        System.out.println("🔔 Напоминания ВКЛЮЧЕНЫ для пользователя: " + user.getTelegramId() +
                " (привычка: " + habit.getName() + ")");

        String message = String.format(
                "⏰ **Напоминание!**\n\n" +
                        "Пришло время выполнить привычку:\n" +
                        "📌 *%s*\n" +
                        "🏷️ Категория: %s\n\n" +
                        "✅ Отметьте выполнение командой /done",
                habit.getName(), habit.getCategory()
        );

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(user.getTelegramId()));
        sendMessage.setText(message);
        sendMessage.setParseMode("Markdown");

        try {
            bot.execute(sendMessage);
            System.out.println("📨 Напоминание отправлено пользователю " + user.getTelegramId() +
                    " для привычки: " + habit.getName());
        } catch (Exception e) {
            System.err.println("❌ Ошибка отправки: " + e.getMessage());
        }
    }
}