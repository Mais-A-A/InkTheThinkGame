package com.example.inkthethink.mapper;

import com.example.inkthethink.dto.MyAppUserDTO;
import com.example.inkthethink.dto.RoomDTO;
import com.example.inkthethink.model.MyAppUser;
import com.example.inkthethink.model.RoomEntity;

import java.util.ArrayList;

public class UserMapper {

    public static MyAppUser toMyAppUser(MyAppUserDTO userDTO) {
        return MyAppUser.builder()
                .username(userDTO.getUsername())
                .password(userDTO.getPassword())
                .email(userDTO.getEmail())
                .build();
    }

    public static MyAppUserDTO toMyAppUserDTO(MyAppUser user) {
        return MyAppUserDTO.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .email(user.getEmail())
                .build();
    }

}
