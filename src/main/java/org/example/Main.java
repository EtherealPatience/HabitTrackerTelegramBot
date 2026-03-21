package org.example;

import org.example.config.DatabaseConfig;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {
        try {
            // Проверяем подключение к БД
            DatabaseConfig.getConnection();
            System.out.println("✅ Подключение к БД успешно!");

            // Запускаем бота
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new HabbitTrackerBot());
            System.out.println("✅ Бот успешно запущен!");
            System.out.println("🤖 Имя бота: @habitTrackerKursachBot");

        } catch (Exception e) {
            System.err.println("❌ Ошибка запуска: " + e.getMessage());
            e.printStackTrace();
        }
    }
}