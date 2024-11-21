package com.example.inkthethink.dto;

import lombok.*;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyAppUserDTO {

    private Long userId;

    private String username;

    private String email;

    private String password;

    private List<RoomDTO> rooms;
}
