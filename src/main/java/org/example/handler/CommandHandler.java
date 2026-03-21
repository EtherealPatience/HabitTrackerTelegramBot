package org.example.handler;

import org.example.model.Habit;
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

        // Проверяем состояние
        String state = userStates.get(chatId);


        // Состояние: ожидание названия привычки (создание)
        if ("waiting_habit_name".equals(state)) {
            tempData.put(chatId, "name:" + text);
            userStates.put(chatId, "waiting_habit_time");
            message.setText("⏰ В какое время напоминать? (в формате ЧЧ:ММ, например 09:00)");
            return message;
        }

        if ("waiting_undone_number".equals(state)) {
            try {
                int index = Integer.parseInt(text.trim());
                String result = habitService.markUndone(chatId, index);
                userStates.remove(chatId);
                message.setText(result);
            } catch (NumberFormatException e) {
                message.setText("❌ Неверный формат. Введите номер привычки (число)");
            }
            return message;
        }

        // Состояние: ожидание времени (создание)
        if ("waiting_habit_time".equals(state)) {
            String data = tempData.get(chatId);
            if (data != null && data.startsWith("name:")) {
                String name = data.substring(5);

                if (!text.matches("([01]?[0-9]|2[0-3]):[0-5][0-9]")) {
                    message.setText("❌ Неверный формат времени! Используйте ЧЧ:ММ, например 09:00");
                    return message;
                }

                tempData.put(chatId + 1000L, name);
                tempData.put(chatId + 2000L, text);
                tempData.remove(chatId);
                userStates.remove(chatId);

                message.setText("📌 Выберите категорию для привычки \"" + name + "\":");
                message.setReplyMarkup(org.example.util.KeyboardBuilder.createCategoryKeyboard(chatId));
                return message;
            }
        }

        // Состояние: ожидание номера привычки для /done
        if ("waiting_done_number".equals(state)) {
            try {
                int index = Integer.parseInt(text.trim());
                String result = habitService.markDoneByIndex(chatId, index);
                userStates.remove(chatId);
                message.setText(result);
            } catch (NumberFormatException e) {
                message.setText("❌ Неверный формат. Введите номер привычки (число)");
            }
            return message;
        }

        // Состояние: ожидание номера привычки для /delete
        if ("waiting_delete_number".equals(state)) {
            try {
                int index = Integer.parseInt(text.trim());
                Habit habit = habitService.getHabitByIndex(chatId, index);
                if (habit == null) {
                    userStates.remove(chatId);
                    message.setText("❌ Привычка не найдена");
                } else {
                    String result = habitService.deleteHabit(habit);
                    userStates.remove(chatId);
                    message.setText(result);
                }
            } catch (NumberFormatException e) {
                message.setText("❌ Неверный формат. Введите номер привычки (число)");
            }
            return message;
        }

        // Состояние: ожидание номера привычки для /edit
        if ("waiting_edit_choice".equals(state)) {
            try {
                int index = Integer.parseInt(text.trim());
                Habit habit = habitService.getHabitByIndex(chatId, index);
                if (habit == null) {
                    userStates.remove(chatId);
                    message.setText("❌ Привычка не найдена");
                } else {
                    tempData.put(chatId + 100000L, String.valueOf(habit.getId()));
                    tempData.put(chatId + 100001L, habit.getName());
                    tempData.put(chatId + 100002L, habit.getCategory());
                    tempData.put(chatId + 100003L, habit.getReminderTime());
                    userStates.put(chatId, "waiting_edit_field");
                    message.setText(getHabitDetailsForEdit(habit));
                }
            } catch (NumberFormatException e) {
                message.setText("❌ Неверный формат. Введите номер привычки (число)");
            }
            return message;
        }

        // Состояние: ожидание выбора поля для редактирования
        if ("waiting_edit_field".equals(state)) {
            String habitIdStr = tempData.get(chatId + 100000L);
            if (habitIdStr == null) {
                userStates.remove(chatId);
                message.setText("❌ Ошибка. Попробуйте /edit заново");
                return message;
            }

            int habitId = Integer.parseInt(habitIdStr);
            int choice;
            try {
                choice = Integer.parseInt(text.trim());
            } catch (NumberFormatException e) {
                message.setText("❌ Введите число (1, 2, 3 или 0 для завершения)");
                return message;
            }

            if (choice == 0) {
                // Завершаем редактирование
                tempData.remove(chatId + 100000L);
                tempData.remove(chatId + 100001L);
                tempData.remove(chatId + 100002L);
                tempData.remove(chatId + 100003L);
                userStates.remove(chatId);
                message.setText("✅ Редактирование завершено!");
                return message;
            }

            if (choice == 1) {
                tempData.put(chatId + 200000L, "name");
                userStates.put(chatId, "waiting_edit_value");
                message.setText("📝 Введите новое название для привычки (или 0 для пропуска):");
            } else if (choice == 2) {
                tempData.put(chatId + 200000L, "category");
                userStates.put(chatId, "waiting_edit_value");
                message.setText("🏷️ Выберите новую категорию (или 0 для пропуска):");
                message.setReplyMarkup(org.example.util.KeyboardBuilder.createCategoryKeyboard(chatId));
                return message;
            } else if (choice == 3) {
                tempData.put(chatId + 200000L, "time");
                userStates.put(chatId, "waiting_edit_value");
                message.setText("⏰ Введите новое время (формат ЧЧ:ММ, например 09:00) или 0 для пропуска:");
            } else {
                message.setText("❌ Неверный выбор. Введите 1, 2, 3 или 0 для завершения");
            }
            return message;
        }

        // Состояние: ожидание нового значения
        if ("waiting_edit_value".equals(state)) {
            String habitIdStr = tempData.get(chatId + 100000L);
            String editField = tempData.get(chatId + 200000L);

            if (habitIdStr == null || editField == null) {
                userStates.remove(chatId);
                message.setText("❌ Ошибка. Попробуйте /edit заново");
                return message;
            }

            int habitId = Integer.parseInt(habitIdStr);
            Habit habit = habitService.getHabitById(chatId, habitId);
            if (habit == null) {
                userStates.remove(chatId);
                message.setText("❌ Привычка не найдена");
                return message;
            }

            String result = "";

            // Если пользователь ввел 0 - пропускаем
            if ("0".equals(text.trim())) {
                result = "⏭️ Поле пропущено";
            } else if ("name".equals(editField)) {
                result = habitService.updateName(habit, text);
            } else if ("category".equals(editField)) {
                result = habitService.updateCategory(habit, text);
            } else if ("time".equals(editField)) {
                result = habitService.updateTime(habit, text);
            } else {
                result = "❌ Ошибка";
            }

            // Очищаем временные данные по полю
            tempData.remove(chatId + 200000L);

            // Обновляем сохраненные данные привычки
            tempData.put(chatId + 100001L, habit.getName());
            tempData.put(chatId + 100002L, habit.getCategory());
            tempData.put(chatId + 100003L, habit.getReminderTime());

            // Показываем обновленные данные и снова предлагаем выбрать поле
            message.setText(result + "\n\n" + getHabitDetailsForEdit(habit));
            userStates.put(chatId, "waiting_edit_field");
            return message;
        }

        // Обработка команд
        switch (text) {
            case "/start":
            case "/help":
                message.setText("👋 Привет! Я бот-трекер привычек!\n\n" +
                        "📌 /create - создать новую привычку\n" +
                        "📋 /list - показать мои привычки\n" +
                        "✏️ /edit - редактировать привычку\n" +
                        "🗑️ /delete - удалить привычку\n" +
                        "✅ /done - отметить выполнение привычки\n" +
                        "🔄 /undone - отменить выполнение привычки\n" +
                        "📊 /stats - показать статистику\n"+
                        "❓ /help - показать это сообщение");
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
                String stats = habitService.getStatistics(chatId);
                message.setText("📊 Статистика:\n\n" + stats);
                message.setParseMode("Markdown");
                break;

            case "/done":
                String doneList = habitService.getHabitsListForChoice(chatId, "done");
                if (doneList.contains("У вас нет привычек")) {
                    message.setText(doneList);
                } else {
                    userStates.put(chatId, "waiting_done_number");
                    message.setText(doneList);
                }
                break;

            case "/delete":
                String deleteList = habitService.getHabitsListForChoice(chatId, "delete");
                if (deleteList.contains("У вас нет привычек")) {
                    message.setText(deleteList);
                } else {
                    userStates.put(chatId, "waiting_delete_number");
                    message.setText(deleteList);
                }
                break;

            case "/edit":
                String editList = habitService.getHabitsListForChoice(chatId, "edit");
                if (editList.contains("У вас нет привычек")) {
                    message.setText(editList);
                } else {
                    userStates.put(chatId, "waiting_edit_choice");
                    message.setText(editList);
                }
                break;

            case "/undone":
                String undoneList = habitService.getHabitsListForUndone(chatId);
                // Проверяем, есть ли выполненные привычки
                if (undoneList.contains("нет выполненных привычек") || undoneList.contains("У вас нет привычек")) {
                    // Если нет выполненных привычек, просто показываем сообщение без ожидания ввода
                    message.setText(undoneList);
                    message.setParseMode("Markdown");
                } else {
                    // Если есть выполненные привычки, переходим в состояние ожидания
                    userStates.put(chatId, "waiting_undone_number");
                    message.setText(undoneList);
                    message.setParseMode("Markdown");
                }
                break;

            default:
                message.setText("❓ Неизвестная команда. Используйте /start");
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