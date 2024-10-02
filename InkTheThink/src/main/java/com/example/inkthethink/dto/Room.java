package com.example.inkthethink.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class Room {

    private String roomId;
    private int numPlayers;
    private int numRounds;
    private List<String> players;

    public Room(String roomId, int numPlayers, int numRounds) {
        this.roomId = roomId;
        this.numPlayers = numPlayers;
        this.numRounds = numRounds;
        this.players = new ArrayList<>();
    }

    public void addPlayer(String player) {
        players.add(player);
    }
}
