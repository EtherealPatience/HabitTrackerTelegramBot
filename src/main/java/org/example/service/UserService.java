package org.example.service;

import org.example.model.User;
import org.example.repository.UserRepository;

public class UserService {
    private final UserRepository userRepo = new UserRepository();

    public User getOrCreate(User user) {
        User existingUser = userRepo.findByTelegramId(user.getTelegramId());
        if (existingUser == null) {
            user.setRegisteredDate(java.time.LocalDateTime.now());
            user.setLastActive(java.time.LocalDateTime.now());
            userRepo.save(user);
            System.out.println("🆕 Новый пользователь: " + user.getTelegramId());
            return user;
        } else {
            existingUser.setLastActive(java.time.LocalDateTime.now());
            existingUser.setUsername(user.getUsername());
            existingUser.setFirstName(user.getFirstName());
            userRepo.save(existingUser);
            return existingUser;
        }
    }

    public User getByTelegramId(long id) {
        return userRepo.findByTelegramId(id);
    }

    // Метод для отключения напоминаний
    // Метод для отключения напоминаний
    // Метод для отключения напоминаний
    public String muteNotifications(long chatId) {
        User user = getByTelegramId(chatId);
        if (user == null) {
            return "❌ Пользователь не найден";
        }

        System.out.println("🔇 Попытка отключить напоминания для пользователя: " + chatId);
        System.out.println("   Имя пользователя: " + user.getUsername());
        System.out.println("   Текущее значение notificationsEnabled: " + user.isNotificationsEnabled());

        user.setNotificationsEnabled(false);
        userRepo.save(user);

        // Проверяем, сохранилось ли
        User checkUser = getByTelegramId(chatId);
        System.out.println("   После сохранения: " + checkUser.isNotificationsEnabled());
        System.out.println("   Сохранено для telegramId: " + checkUser.getTelegramId());

        return "🔇 **Напоминания ВЫКЛЮЧЕНЫ**\n\n" +
                "Вы больше не будете получать уведомления о привычках.\n" +
                "Чтобы снова включить, используйте /unmute или нажмите кнопку 🔔 /unmute.";
    }

    // Метод для включения напоминаний
    public String unmuteNotifications(long chatId) {
        User user = getByTelegramId(chatId);
        if (user == null) {
            return "❌ Пользователь не найден";
        }

        System.out.println("🔔 Попытка включить напоминания для пользователя: " + chatId);
        System.out.println("   Текущее значение notificationsEnabled: " + user.isNotificationsEnabled());

        user.setNotificationsEnabled(true);
        userRepo.save(user);

        // Проверяем, сохранилось ли
        User checkUser = getByTelegramId(chatId);
        System.out.println("   После сохранения: " + checkUser.isNotificationsEnabled());

        return "🔔 **Напоминания ВКЛЮЧЕНЫ**\n\n" +
                "Теперь вы снова будете получать уведомления о привычках!\n" +
                "Чтобы отключить, используйте /mute или нажмите кнопку 🔇 /mute.";
    }

    // Проверка, включены ли напоминания
    public boolean isNotificationsEnabled(long chatId) {
        User user = getByTelegramId(chatId);
        if (user == null) {
            return true; // По умолчанию включены
        }
        return user.isNotificationsEnabled();
    }
}