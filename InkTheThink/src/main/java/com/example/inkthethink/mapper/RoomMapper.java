package com.example.inkthethink.mapper;

import com.example.inkthethink.dto.MyAppUserDTO;
import com.example.inkthethink.dto.RoomDTO;
import com.example.inkthethink.model.RoomEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RoomMapper {
    public static RoomEntity toRoomEntity(RoomDTO room) {
        return RoomEntity.builder()
                .roomID(room.getRoomId())
                .numOfPlayers(room.getNumPlayers())
                .numOfRounds(room.getNumRounds())
                .currentDrawer(room.getCurrentDrawer())
                .currentWord(room.getCurrentWord())
                .currentRound(room.getCurrentRound())
                .players(new ArrayList<>()) // Initialize players if needed
                .build();
    }

    public static RoomDTO toRoomDTO(RoomEntity roomEntity) {

        List<MyAppUserDTO> playerDTOs = roomEntity.getPlayers().stream()
                .map(UserMapper::toMyAppUserDTO) // Use the mapper to convert each MyAppUser to MyAppUserDTO
                .toList();

        return RoomDTO.builder()
                .roomId(roomEntity.getRoomID())
                .numPlayers(roomEntity.getNumOfPlayers())
                .numRounds(roomEntity.getNumOfRounds())
                .currentDrawer(roomEntity.getCurrentDrawer())
                .currentWord(roomEntity.getCurrentWord())
                .players(playerDTOs)
                .currentRound(roomEntity.getCurrentRound())// Convert players as needed
                .build();
    }
}
