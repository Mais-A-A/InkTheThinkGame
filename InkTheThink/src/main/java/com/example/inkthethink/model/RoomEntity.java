package com.example.inkthethink.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Audited
@Table(name = "room_entity", indexes = {
        @Index(name = "idx_room_id_room_entity", columnList = "roomID"),
})
public class RoomEntity {

    @Id
    private String roomID;

    private int numOfPlayers;

    private int numOfRounds;

    private String currentDrawer;

    private String currentWord;

    private String firstCorrectGuesser;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int curAiPoints = 0;

    private int currentRound;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "player_room",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<MyAppUser> players = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
