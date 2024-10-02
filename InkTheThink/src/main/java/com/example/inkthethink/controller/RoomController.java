package com.example.inkthethink.controller;


import com.example.inkthethink.dto.Room;
import com.example.inkthethink.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PostMapping("/api/addRoom")
    public void addRoom(@RequestParam String roomId, @RequestParam String player, @RequestParam int numPlayers, @RequestParam int numRounds) {
        Room room = new Room(roomId, numPlayers, numRounds);
        room.addPlayer(player);
        roomService.addRoom(room);
    }

    @GetMapping("/api/checkRoom")
    public Map<String, Boolean> checkRoomExistence(@RequestParam String roomId, @RequestParam String newPlayer) {
        boolean isExist = roomService.checkRoomExistence(roomId, newPlayer);

        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", isExist);
        return response;  // Return the response as JSON

    }

    @GetMapping("/api/isFull")
    public Map<String, Boolean> isFull(@RequestParam String roomId) {
        Room room = roomService.getRoomDetails(roomId);
        Map<String, Boolean> response = new HashMap<>();
        if (room == null) {
            response.put("exists", false);
        }
        else {
            boolean isFull =  room.getPlayers().size() == room.getNumPlayers();
            response.put("exists", isFull);
        }
        return response;  // Return the response as JSON
    }

//    @GetMapping("/api/getRoomDetails")
//    public boolean getRoomDetails(@RequestParam String roomId) {
//        Room room = roomService.getRoomDetails(roomId);
//        if(room.getPlayers().size() == room.getNumPlayers()){
//            System.out.print("hello.");
//        }
//        return room.getPlayers().size() == room.getNumPlayers();
//    }

}
