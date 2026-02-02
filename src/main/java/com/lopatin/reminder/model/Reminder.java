package com.lopatin.reminder.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reminder")
@AllArgsConstructor
@NoArgsConstructor
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter
    @Column(name = "title", nullable = false)
    private String title;

    @Getter
    @Column(name = "description", length = 4096)
    private String description;

    @Getter
    @Column(name = "remind", nullable = false)
    private LocalDateTime remind;

    @Getter
    @Column(name = "user_id", nullable = false)
    private int user_id;
}




