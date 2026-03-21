package org.example.handler;

import org.example.model.User;
import org.example.service.HabitService;
import org.example.service.UserService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

public class CommandHandler {
    private final UserService userService = new UserService();
    private final HabitService habitService = new HabitService();

    public static final Map<Long, String> userStates = new HashMap<>();
    public static final Map<Long, String> tempData = new HashMap<>();

    public SendMessage handle(Update update) {
        long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        // Регистрируем пользователя
        org.telegram.telegrambots.meta.api.objects.User tgUser = update.getMessage().getFrom();
        User user = new User();
        user.setTelegramId(tgUser.getId());
        user.setUsername(tgUser.getUserName());
        user.setFirstName(tgUser.getFirstName());
        userService.getOrCreate(user);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        // Проверяем состояние (для создания привычки)
        String state = userStates.get(chatId);

        if ("waiting_habit_name".equals(state)) {
            tempData.put(chatId, "name:" + text);
            userStates.put(chatId, "waiting_habit_time");
            message.setText("⏰ В какое время напоминать? (в формате ЧЧ:ММ, например 09:00)");
            return message;
        }

        if ("waiting_habit_time".equals(state)) {
            String data = tempData.get(chatId);
            if (data != null && data.startsWith("name:")) {
                String name = data.substring(5);

                if (!text.matches("([01]?[0-9]|2[0-3]):[0-5][0-9]")) {
                    message.setText("❌ Неверный формат времени! Используйте ЧЧ:ММ, например 09:00");
                    return message;
                }

                // Сохраняем для CallbackHandler
                tempData.put(chatId + 1000, name);
                tempData.put(chatId + 2000, text);
                tempData.remove(chatId);
                userStates.remove(chatId);

                message.setText("📌 Выберите категорию для привычки \"" + name + "\":");
                message.setReplyMarkup(org.example.util.KeyboardBuilder.createCategoryKeyboard(chatId));
                return message;
            }
        }

        // Обработка команд
        switch (text) {
            case "/start":
                message.setText("👋 Привет! Я бот-трекер привычек!\n\n" +
                        "📌 /create - создать новую привычку\n" +
                        "📋 /list - показать мои привычки\n" +
                        "✅ /done [ID] - отметить выполнение привычки\n" +
                        "📊 /stats - показать статистику\n\n" +
                        "Пример: /done 1");
                break;

            case "/create":
                userStates.put(chatId, "waiting_habit_name");
                message.setText("📝 Введите название привычки:");
                break;

            case "/list":
                String list = habitService.getList(chatId);
                message.setText(list);
                message.setParseMode("Markdown");
                break;

            case "/stats":
                String stats = habitService.getList(chatId);
                message.setText("📊 Статистика:\n\n" + stats);
                message.setParseMode("Markdown");
                break;

            default:
                if (text.startsWith("/done ")) {
                    try {
                        String[] parts = text.split(" ");
                        if (parts.length < 2) {
                            message.setText("❌ Используйте: /done [ID привычки]\n\n" +
                                    "Посмотреть ID можно в /list");
                        } else {
                            int habitId = Integer.parseInt(parts[1].trim());
                            String result = habitService.markDone(chatId, habitId);
                            message.setText(result);
                        }
                    } catch (NumberFormatException e) {
                        message.setText("❌ Неверный формат ID. Используйте число.\n\n" +
                                "Пример: /done 1");
                    }
                } else {
                    message.setText("❓ Неизвестная команда. Используйте /start");
                }
        }

        return message;
    }
}