package org.example.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Habit {
    private int id;
    private int userId;
    private String name;
    private String category;
    private String reminderTime;
    private LocalDateTime createdAt = LocalDateTime.now();
    private boolean isActive = true;
}
