package org.example;

import org.example.config.BotConfig;
import org.example.handler.CommandHandler;
import org.example.handler.CallbackHandler;
import org.example.service.HabitService;
import org.example.service.ReminderService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.io.ByteArrayInputStream;

public class HabbitTrackerBot extends TelegramLongPollingBot {
    private final BotConfig config = new BotConfig();
    private final CommandHandler commandHandler = new CommandHandler();
    private final CallbackHandler callbackHandler = new CallbackHandler();
    private final HabitService habitService = new HabitService();
    private final ReminderService reminderService;

    public HabbitTrackerBot() {
        this.reminderService = new ReminderService(this);
        System.out.println("✅ Бот инициализирован");
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public String getBotUsername() {
        return config.getUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String text = update.getMessage().getText();
                long chatId = update.getMessage().getChatId();

                // Обработка команды /export
                if (text.equals("/export")) {
                    byte[] excelData = habitService.exportToExcel(chatId);
                    if (excelData == null) {
                        SendMessage msg = new SendMessage();
                        msg.setChatId(String.valueOf(chatId));
                        msg.setText("❌ Нет данных для экспорта. У вас нет привычек или произошла ошибка.");
                        execute(msg);
                    } else {
                        SendDocument document = new SendDocument();
                        document.setChatId(String.valueOf(chatId));
                        document.setDocument(new InputFile(new ByteArrayInputStream(excelData), "statistics.xlsx"));
                        document.setCaption("📊 Статистика привычек за неделю");
                        execute(document);
                    }
                    return;
                }

                // Проверяем состояние - ожидание времени
                String state = CommandHandler.userStates.get(chatId);
                System.out.println("Текущее состояние: " + state + " для чата: " + chatId);

                if ("waiting_habit_time".equals(state)) {
                    String data = CommandHandler.tempData.get(chatId);
                    System.out.println("Данные tempData: " + data);

                    if (data != null && data.startsWith("name:")) {
                        String name = data.substring(5);
                        CommandHandler.tempData.remove(chatId);
                        CommandHandler.userStates.remove(chatId);

                        // Проверяем формат времени
                        if (!text.matches("([01]?[0-9]|2[0-3]):[0-5][0-9]")) {
                            SendMessage msg = new SendMessage();
                            msg.setChatId(String.valueOf(chatId));
                            msg.setText("❌ Неверный формат времени! Используйте ЧЧ:ММ, например 09:00");
                            execute(msg);
                            return;
                        }

                        // Сохраняем название и время во временное хранилище
                        CommandHandler.tempData.put(chatId + 1000L, name);
                        CommandHandler.tempData.put(chatId + 2000L, text);
                        CommandHandler.tempData.remove(chatId);
                        CommandHandler.userStates.remove(chatId);

                        // Показываем выбор категории
                        SendMessage msg = new SendMessage();
                        msg.setChatId(String.valueOf(chatId));
                        msg.setText("📌 Выберите категорию для привычки \"" + name + "\":");
                        InlineKeyboardMarkup markup = org.example.util.KeyboardBuilder.createCategoryKeyboard(chatId);
                        msg.setReplyMarkup(markup);
                        execute(msg);
                        return;
                    }
                }

                // Обрабатываем команды через CommandHandler
                SendMessage response = commandHandler.handle(update);
                if (response != null) {
                    execute(response);
                }

            } else if (update.hasCallbackQuery()) {
                SendMessage response = callbackHandler.handle(update);
                if (response != null) {
                    execute(response);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка обработки update: " + e.getMessage());
            e.printStackTrace();
        }
    }
}