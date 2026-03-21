package org.example.handler;

import org.example.service.HabitService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class CallbackHandler {
    private final HabitService habitService = new HabitService();

    public SendMessage handle(Update update) {
        String data = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        if (data.startsWith("done_")) {
            int habitId = Integer.parseInt(data.split("_")[1]);
            String result = habitService.markDone(chatId, habitId);
            message.setText(result);

        } else if (data.startsWith("cat_")) {
            String[] parts = data.split("_");
            String category = parts[1];
            long originalChatId = Long.parseLong(parts[2]);

            // Получаем название и время из временного хранилища
            String habitName = CommandHandler.tempData.get(originalChatId + 1000);
            String habitTime = CommandHandler.tempData.get(originalChatId + 2000);

            if (habitName == null || habitTime == null) {
                message.setText("❌ Ошибка: данные о привычке потеряны. Попробуйте /create заново\n\n" +
                        "habitName=" + habitName + ", habitTime=" + habitTime);
                return message;
            }

            // Очищаем временные данные
            CommandHandler.tempData.remove(originalChatId + 1000);
            CommandHandler.tempData.remove(originalChatId + 2000);

            String result = habitService.create(chatId, habitName, category, habitTime);
            message.setText(result);
        } else {
            message.setText("❌ Неизвестное действие");
        }

        return message;
    }
}