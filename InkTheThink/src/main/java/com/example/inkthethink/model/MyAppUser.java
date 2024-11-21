package com.example.inkthethink.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;

import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Audited
@Table(name = "my_app_user", indexes = {
        @Index(name = "idx_username", columnList = "username"),
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_user_id", columnList = "userId")})

public class MyAppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String username;

    @Column(unique = true)
    private String email;

    private String password;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int totalScore;

    @ManyToMany(mappedBy = "players")
    private List<RoomEntity> rooms;
}

