package org.example.handler;

import org.example.service.HabitService;
import org.example.service.UserService;
import org.example.util.KeyboardBuilder;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class MessageHandler {
    private final UserService userService = new UserService();
    private final HabitService habitService = new HabitService();

    public SendMessage handle(Update update) {
        long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        // Проверяем состояние пользователя
        String state = CommandHandler.userStates.get(chatId);

        // Состояние 1: ожидание названия привычки
        if ("waiting_habit_name".equals(state)) {
            CommandHandler.tempData.put(chatId, "name:" + text);
            CommandHandler.userStates.put(chatId, "waiting_habit_time");
            message.setText("⏰ В какое время напоминать? (в формате ЧЧ:ММ, например 09:00)");
            return message;
        }

        // Состояние 2: ожидание времени
        if ("waiting_habit_time".equals(state)) {
            String data = CommandHandler.tempData.get(chatId);
            if (data == null || !data.startsWith("name:")) {
                CommandHandler.userStates.remove(chatId);
                message.setText("❌ Ошибка создания привычки. Попробуйте /create заново");
                return message;
            }

            String name = data.substring(5);

            // Проверяем формат времени
            if (!text.matches("([01]?[0-9]|2[0-3]):[0-5][0-9]")) {
                message.setText("❌ Неверный формат времени! Используйте ЧЧ:ММ, например 09:00");
                return message;
            }

            // Сохраняем название и время для последующего использования
            CommandHandler.tempData.put(chatId + 1000, name);
            CommandHandler.tempData.put(chatId + 2000, text);

            // Очищаем временные данные и состояние
            CommandHandler.tempData.remove(chatId);
            CommandHandler.userStates.remove(chatId);

            // Показываем выбор категории
            message.setText("📌 Выберите категорию для привычки \"" + name + "\":");
            message.setReplyMarkup(KeyboardBuilder.createCategoryKeyboard(chatId));
            return message;
        }

        // Обычное сообщение - помощь
        message.setText("❓ Неизвестная команда.\n\n" +
                "Доступные команды:\n" +
                "/start - начать работу\n" +
                "/create - создать привычку\n" +
                "/list - список привычек\n" +
                "/done - отметить выполнение\n" +
                "/stats - статистика");

        return message;
    }
}