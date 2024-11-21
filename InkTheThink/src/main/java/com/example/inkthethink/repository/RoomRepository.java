package com.example.inkthethink.repository;

import com.example.inkthethink.model.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<RoomEntity, String> {
}
