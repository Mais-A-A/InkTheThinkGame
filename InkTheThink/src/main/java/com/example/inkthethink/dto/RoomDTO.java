package com.example.inkthethink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Setter
@Getter
@AllArgsConstructor
@Builder
public class RoomDTO {

    private String roomId;

    private int numPlayers;

    private int numRounds;

    private List<MyAppUserDTO> players;

    private int currentRound;

    private String currentDrawer;

    private String currentWord;

    public RoomDTO(String roomId, int numPlayers, int numRounds) {
        this.roomId = roomId;
        this.numPlayers = numPlayers;
        this.numRounds = numRounds;
        this.currentRound = 0;
        this.currentDrawer = "";
        this.currentWord = "";

    }


}
