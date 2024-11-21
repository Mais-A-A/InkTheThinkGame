package com.example.inkthethink.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DrawingRequest {
    private String drawing;
    private String word;
    private String roomID;
}
