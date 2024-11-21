package com.example.inkthethink.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RoomRequest {
    private String roomId;
    private String player;
    private int numPlayers;
    private int numRounds;
}
