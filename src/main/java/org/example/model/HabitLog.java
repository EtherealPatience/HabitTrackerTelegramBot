package org.example.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class HabitLog {
    private int id;
    private int habitId;
    private LocalDate logDate = LocalDate.now();
    private String status = "PENDING";
    private LocalDateTime loggedAt = LocalDateTime.now();
}
