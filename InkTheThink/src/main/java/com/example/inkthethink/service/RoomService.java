package com.example.inkthethink.service;

import com.example.inkthethink.dto.Room;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RoomService {

    //private Set<String> rooms = new HashSet<>();
    private Map<String, Room> roomMap = new HashMap<>();

    public void addRoom(Room room) {
        roomMap.put(room.getRoomId(), room);
    }

    public Room getRoomDetails(String roomId) {
        return roomMap.get(roomId); // Return room details or null if room doesn't exist
    }

    public boolean checkRoomExistence(String roomId, String newPlayer) {
        Room room = getRoomDetails(roomId);
        if(room != null){
            List<String> players = room.getPlayers();
            if(players.size() < room.getNumPlayers()){
                room.addPlayer(newPlayer);
                return true;
            }
        }
        return false;
    }
}