package org.example.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class User {
    private int id;
    private long telegramId;
    private String username;
    private String firstName;
    private LocalDateTime registeredDate = LocalDateTime.now();
    private LocalDateTime lastActive = LocalDateTime.now();
    private boolean notificationsEnabled = true;
}
