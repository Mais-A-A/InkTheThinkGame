package com.example.inkthethink.controller;

import com.example.inkthethink.request.RoomRequest;
import com.example.inkthethink.service.*;
import java.util.*;

import com.example.inkthethink.request.DrawingRequest;
import com.example.inkthethink.dto.RoomDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.Base64;
@RestController
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private OpenAiService openAiService;

    @Autowired
    private StorageService storageService;

    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);

    @PostMapping("/api/addRoom")
    public ResponseEntity<?> addRoom(@RequestBody RoomRequest roomRequest) {
        System.out.println(roomRequest.getPlayer());
        System.out.println("hello from lama");
        RoomDTO room = new RoomDTO(roomRequest.getRoomId(), roomRequest.getNumPlayers(), roomRequest.getNumRounds());
        try {
            roomService.addRoom(room);
            roomService.addPlayerToRoom(roomRequest.getRoomId(), roomRequest.getPlayer());
            // Return a successful response with the room details
            return ResponseEntity.ok(room); // Return the room details as a JSON response
        } catch (Exception e) {
            // Handle the exception and return an error message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage())); // Return an error as JSON
        }
    }

    @GetMapping("/api/checkRoom")
    public ResponseEntity<Map<String, Boolean>> checkRoomExistence(@RequestParam String roomId, @RequestParam String newPlayer) {
        Map<String, Boolean> response = new HashMap<>();
        try {
            boolean isExist = roomService.checkRoomExistence(roomId, newPlayer);
            response.put("exists", isExist);

            if (isExist) {
                return ResponseEntity.ok(response); // HTTP 200 OK
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // HTTP 404 Not Found
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log the error for debugging
            response.put("exists", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // HTTP 500 Internal Server Error
        }
    }

    @GetMapping("/api/isFull")
    public Map<String, Boolean> isFull(@RequestParam String roomId) {
        RoomDTO room = roomService.getRoomDetails(roomId);
        Map<String, Boolean> response = new HashMap<>();
        if (room == null) {
            response.put("exists", false);
        } else {
            boolean isFull = room.getPlayers().size() == room.getNumPlayers();
            response.put("exists", isFull);
            // start rounds
        }
        return response;  // Return the response as JSON
    }

    @PostMapping("/api/guess")
    public ResponseEntity<?> checkGuess(@RequestParam String roomId, @RequestParam String guess, @RequestParam String username) {
        try {
            System.out.println(guess);
            System.out.println(roomId);
            System.out.println(username);
            roomService.checkTheGuess(roomId, guess, username);
            return ResponseEntity.ok("Guess processed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/api/validateDrawing")
    public int validateDrawing(@RequestBody DrawingRequest drawingRequest) throws Exception {
        try {
            String base64Drawing = drawingRequest.getDrawing();
            String prompt = drawingRequest.getWord();
            String roomID = drawingRequest.getRoomID();

            // Convert Base64 to MultipartFile
            MultipartFile multipartFile = convertBase64ToMultipartFile(base64Drawing, "drawing.png", "image/png");

            // Upload the file to S3
            String imageUrl = storageService.uploadFile(multipartFile);
            System.out.println(imageUrl);

            if (imageUrl == null) {
                throw new Exception("Image upload failed.");
            }

            int response = openAiService.callOpenAi(imageUrl, prompt, roomID);
            logger.info("OpenAI response: {}", response);

            return response;

        } catch (Exception e) {
            logger.error("Error validating drawing", e);
            throw e;
        }
    }

    public MultipartFile convertBase64ToMultipartFile(String base64, String fileName, String contentType) {
        if (base64.contains(",")) {
            base64 = base64.split(",")[1];
        }
        byte[] decodedBytes = Base64.getDecoder().decode(base64);
        return new Base64MultipartFile(decodedBytes, fileName, contentType);
    }

    @GetMapping("/valid_cred")
    public ResponseEntity<?> ValidCred(@RequestParam String username,@RequestParam String email) {
        boolean response = roomService.ValidCred(username,email);
        if(response){
            System.out.println("hi from controlleer" +response);
            return ResponseEntity.ok("exisits in database ");
        }
        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/user-score")
    public int ValidCred(@RequestParam String username) {
        return roomService.userScore(username);
    }


}