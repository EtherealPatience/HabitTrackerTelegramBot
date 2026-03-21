package org.example.handler;

import org.example.model.Habit;
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

            // Проверяем, в каком режиме мы находимся - создание или редактирование
            String state = CommandHandler.userStates.get(chatId);

            // Если в режиме редактирования
            if ("waiting_edit_value".equals(state)) {
                String editField = CommandHandler.tempData.get(chatId + 200000L);
                if ("category".equals(editField)) {
                    String habitIdStr = CommandHandler.tempData.get(chatId + 100000L);
                    if (habitIdStr != null) {
                        int habitId = Integer.parseInt(habitIdStr);
                        Habit habit = habitService.getHabitById(chatId, habitId);
                        if (habit != null) {
                            String result = habitService.updateCategory(habit, category);
                            CommandHandler.tempData.remove(chatId + 200000L);

                            // Обновляем сохраненные данные привычки
                            CommandHandler.tempData.put(chatId + 100001L, habit.getName());
                            CommandHandler.tempData.put(chatId + 100002L, habit.getCategory());
                            CommandHandler.tempData.put(chatId + 100003L, habit.getReminderTime());

                            message.setText(result + "\n\n" + getHabitDetailsForEdit(habit));
                            CommandHandler.userStates.put(chatId, "waiting_edit_field");
                            return message;
                        }
                    }
                }
            }

            // Если в режиме создания привычки
            String habitName = CommandHandler.tempData.get(originalChatId + 1000L);
            String habitTime = CommandHandler.tempData.get(originalChatId + 2000L);

            if (habitName == null || habitTime == null) {
                message.setText("❌ Ошибка: данные о привычке потеряны. Попробуйте /create заново\n\n" +
                        "habitName=" + habitName + ", habitTime=" + habitTime);
                return message;
            }

            CommandHandler.tempData.remove(originalChatId + 1000L);
            CommandHandler.tempData.remove(originalChatId + 2000L);

            String result = habitService.create(chatId, habitName, category, habitTime);
            message.setText(result);
        } else {
            message.setText("❌ Неизвестное действие");
        }

        return message;
    }

    // Вспомогательный метод для отображения деталей привычки
    private String getHabitDetailsForEdit(Habit habit) {
        return String.format(
                "✏️ **Редактирование привычки:**\n\n" +
                        "1️⃣ Название: %s\n" +
                        "2️⃣ Категория: %s\n" +
                        "3️⃣ Время: %s\n\n" +
                        "Введите номер поля, которое хотите изменить (1, 2 или 3), или 0 для завершения:",
                habit.getName(), habit.getCategory(), habit.getReminderTime()
        );
    }
}