package com.example.inkthethink.repository;


import com.example.inkthethink.model.ScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScoreRepository extends JpaRepository<ScoreEntity, Long> {
    ScoreEntity findByRoomIdAndUserId(String roomId, Long userId);
}