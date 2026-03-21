package org.example;

import org.example.config.BotConfig;
import org.example.handler.CommandHandler;
import org.example.handler.CallbackHandler;
import org.example.service.ReminderService;
import org.example.util.KeyboardBuilder;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class HabbitTrackerBot extends TelegramLongPollingBot {
    private final BotConfig config = new BotConfig();
    private final CommandHandler commandHandler = new CommandHandler();
    private final CallbackHandler callbackHandler = new CallbackHandler();
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

                // Проверяем состояние - ожидание времени (этот блок больше не нужен, так как MessageHandler обрабатывает)
                // Просто передаем все сообщения в CommandHandler и MessageHandler

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