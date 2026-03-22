package org.example.util;

import org.example.model.Habit;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import java.util.ArrayList;
import java.util.List;

public class KeyboardBuilder {

    // Создание главной клавиатуры с кнопками (ReplyKeyboard)
    public static ReplyKeyboardMarkup createMainMenu() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        // Первый ряд
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("/create"));
        row1.add(new KeyboardButton("/list"));
        row1.add(new KeyboardButton("/stats"));

        // Второй ряд
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("/done"));
        row2.add(new KeyboardButton("/undone"));
        row2.add(new KeyboardButton("/edit"));

        // Третий ряд
        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("/delete"));
        row3.add(new KeyboardButton("/export"));
        row3.add(new KeyboardButton("/help"));

        // Четвертый ряд - кнопки уведомлений
        KeyboardRow row4 = new KeyboardRow();
        row4.add(new KeyboardButton("🔇 /mute"));
        row4.add(new KeyboardButton("🔔 /unmute"));

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        return keyboardMarkup;
    }

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