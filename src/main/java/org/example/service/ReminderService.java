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

    public ReminderService(TelegramLongPollingBot bot) {
        this.bot = bot;
        startReminderChecker();
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

        List<Habit> habits = habitRepo.findAllActive();

        for (Habit habit : habits) {
            if (habit.getReminderTime() != null && currentTime.equals(habit.getReminderTime())) {
                sendReminder(habit);
            }
        }
    }

    private void sendReminder(Habit habit) {
        User user = userRepo.findByTelegramId(habit.getUserId());
        if (user == null) return;

        String message = String.format(
                "⏰ **Напоминание!**\n\n" +
                        "Пришло время выполнить привычку:\n" +
                        "📌 *%s*\n" +
                        "🏷️ Категория: %s\n\n" +
                        "Нажми /done чтобы отметить выполнение",
                habit.getName(), habit.getCategory()
        );

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(user.getTelegramId()));
        sendMessage.setText(message);
        sendMessage.setParseMode("Markdown");

        try {
            bot.execute(sendMessage);
            System.out.println("📨 Напоминание отправлено: " + habit.getName());
        } catch (Exception e) {
            System.err.println("❌ Ошибка отправки напоминания: " + e.getMessage());
        }
    }
}