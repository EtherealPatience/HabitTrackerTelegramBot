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
}