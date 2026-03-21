package org.example.util;

import org.example.model.Habit;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.util.ArrayList;
import java.util.List;

public class KeyboardBuilder {

    // Метод принимает только chatId, без длинного названия
    public static InlineKeyboardMarkup createCategoryKeyboard(long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        String[][] categories = {
                {"🏃 Спорт", "СПОРТ"},
                {"📚 Учеба", "УЧЕБА"},
                {"🥗 Здоровье", "ЗДОРОВЬЕ"},
                {"🧘 Медитация", "МЕДИТАЦИЯ"},
                {"✏️ Другое", "ДРУГОЕ"}
        };

        for (String[] cat : categories) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(cat[0]);
            // Передаем только категорию и chatId, без длинного названия
            btn.setCallbackData("cat_" + cat[1] + "_" + chatId);
            row.add(btn);
            rows.add(row);
        }

        markup.setKeyboard(rows);
        return markup;
    }

    public static InlineKeyboardMarkup createHabitsKeyboard(List<Habit> habits) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Habit habit : habits) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton btn = new InlineKeyboardButton();
            String shortName = habit.getName().length() > 30 ?
                    habit.getName().substring(0, 27) + "..." : habit.getName();
            btn.setText("✅ " + shortName);
            btn.setCallbackData("done_" + habit.getId());
            row.add(btn);
            rows.add(row);
        }

        markup.setKeyboard(rows);
        return markup;
    }
}