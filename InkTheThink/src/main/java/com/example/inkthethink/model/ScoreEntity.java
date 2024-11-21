package com.example.inkthethink.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Audited
@Table(name = "score_entity", indexes = {
        @Index(name = "idx_room_id_score_entity", columnList = "room_id"),
        @Index(name = "idx_user_id_score_entity", columnList = "user_id")
})

public class ScoreEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomId;

    private Long userId;

    private int roomScore;
}
