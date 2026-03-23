package com.lopatin.reminder.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "reminder")
@AllArgsConstructor
@NoArgsConstructor
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 4096)
    private String description;

    @Column(name = "remind", nullable = false)
    private LocalDateTime remind;

    @Column(name = "user_id", nullable = false)
    private UUID user_id;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReminderStatus status;
}




