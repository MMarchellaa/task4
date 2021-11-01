package com.example.demo;

import lombok.Data;


import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
@Data
@Table
public class User{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String name;

    @Column
    private LocalDateTime lastTimeClickedBroButton;

    @Column
    private LocalDateTime lastTimeClickedSisButton;

    public User() {
    }

    public User(String name) {
        this.name = name;
    }
}
