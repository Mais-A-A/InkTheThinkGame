package com.example.inkthethink.service;

import com.example.inkthethink.dto.MyAppUserDTO;
import com.example.inkthethink.dto.RoomDTO;
import com.example.inkthethink.mapper.RoomMapper;
import com.example.inkthethink.model.MyAppUser;
import com.example.inkthethink.model.RoomEntity;
import com.example.inkthethink.model.ScoreEntity;
import com.example.inkthethink.repository.MyAppUserRepository;
import com.example.inkthethink.repository.RoomRepository;
import com.example.inkthethink.repository.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);
    private static final String[] WORDS = {
            "apple", "train", "zebra", "cake", "piano", "mountain",
            "banana", "book", "car", "flower", "swim", "doctor",
            "guitar", "ocean", "chocolate", "rabbit", "house", "teacher",
            "kiwi", "dance", "lamp", "elephant", "forest", "football",
            "mango", "sofa", "yoga", "trophy", "cat", "newspaper",
            "orange", "river", "ice cream", "bird", "city", "notebook",
            "peach", "bicycle", "hero", "skirt", "moon", "adventure",
            "strawberry", "cherry", "truck", "violin", "forest", "holiday",
            "blueberry", "lamp", "game", "ghost", "mountain", "juice",
            "raspberry", "watermelon", "shirt", "team", "candy", "trip",
            "pineapple", "teacher", "paint", "football", "swim", "desert",
            "flower", "alien", "vacation", "dance", "cloud", "friend",
            "guitar", "castle", "medal", "cake", "rabbit", "computer",
            "fish", "sketch", "adventure", "bridge", "chocolate", "hiking",
            "tea", "skateboard", "balloon", "tiger", "cupcake", "celebration",
            "jacket", "drums", "exploration", "pencil", "sun", "zebra",
            "sculpture", "kite", "cup", "snow", "carpet", "puzzle",
            "robot", "tree", "tornado", "whale", "bicycle", "straw",
            "holiday", "jewelry", "whistle", "pasta", "calculator", "coconut"
    };
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private MyAppUserRepository myAppUserRepository;
    @Autowired
    private ScoreRepository scoreRepository;

    public void addRoom(RoomDTO room) {
        RoomEntity roomEntity = RoomMapper.toRoomEntity(room);
        roomRepository.save(roomEntity);
    }

    public RoomDTO getRoomDetails(String roomId) {
        RoomEntity roomEntity = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        return RoomMapper.toRoomDTO(roomEntity);
    }

    public void addPlayerToRoom(String roomId, String playerUsername) {
        System.out.println("Attempting to add user with username: " + playerUsername);

        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        MyAppUser player = myAppUserRepository.findByUsername(playerUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!room.getPlayers().contains(player)) {
            room.getPlayers().add(player);
            player.getRooms().add(room);

            ScoreEntity score = new ScoreEntity();
            score.setRoomId(roomId);
            score.setUserId(player.getUserId());
            score.setRoomScore(0);
            scoreRepository.save(score);

            roomRepository.save(room);
            myAppUserRepository.save(player);
        } else {
            System.out.println("Player already in the room: " + playerUsername);
        }
    }

    public boolean checkRoomExistence(String roomId, String newPlayer) {
        RoomDTO room = getRoomDetails(roomId);
        if (room != null) {
            List<MyAppUserDTO> players = room.getPlayers();

            System.out.println(players);
            System.out.println(players.size());
            System.out.println(players.size());
            System.out.println(room.getNumPlayers());

            if (players.size() < room.getNumPlayers()) {
                if (isValidPlayer(newPlayer)) {
                    addPlayerToRoom(roomId, newPlayer);
                    return true;
                } else {
                    System.out.println("Player not found: " + newPlayer);
                    return false;
                }
            } else {
                System.out.println("Room is full. Can't add player.");
            }
        } else {
            System.out.println("Room not found: " + roomId);
        }
        return false;
    }

    private boolean isValidPlayer(String playerName) {
        return myAppUserRepository.findByUsername(playerName).isPresent();
    }

    public int userScore(String playerName) {
        return myAppUserRepository.findByUsername(playerName).get().getTotalScore();
    }

    public void startGame(String roomId, Map<String, WebSocketSession> playerSessions) throws IOException, InterruptedException {
        System.out.println("hello world from start game");
        RoomDTO room = getRoomDetails(roomId);

        if (room != null && room.getCurrentRound() < room.getNumRounds()) {
            String drawer = selectRandomDrawer(roomId);
            String wordToDraw = selectRandomWord(roomId);
            room.setCurrentWord(wordToDraw);
            room.setCurrentDrawer(drawer);
            if (room.getCurrentRound() == 0) {
                for (MyAppUserDTO player : room.getPlayers()) {
                    WebSocketSession playerSession = playerSessions.get(player.getUsername());
                    if (playerSession != null && playerSession.isOpen()) {
                        try {
                            playerSession.sendMessage(new TextMessage("{\"type\":\"alert\",\"message\":\"" + "Game Started!\"}"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            Thread.sleep(5000);
            System.out.println(drawer + " is drawing the word: " + wordToDraw);

            WebSocketSession drawerSession = playerSessions.get(drawer);
            if (drawerSession != null && drawerSession.isOpen()) {
                String message = String.format("{\"type\":\"drawer\",\"word\":\"%s\"}", wordToDraw);
                drawerSession.sendMessage(new TextMessage("{\"type\":\"enableTools\"}"));
                try {
                    drawerSession.sendMessage(new TextMessage(message)); // Send message
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            for (MyAppUserDTO player : room.getPlayers()) {
                WebSocketSession playerSession = playerSessions.get(player.getUsername());
                if (playerSession != null && playerSession.isOpen()) {
                    try {
                        if (!player.getUsername().equals(drawer)) {
                            playerSession.sendMessage(new TextMessage("{\"type\":\"system\",\"message\":\"" + drawer + " is drawing now!\"}"));
                            playerSession.sendMessage(new TextMessage("{\"type\":\"enableChat\"}"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        startGameTimer(roomId, playerSessions);
    }

    private void startGameTimer(String roomId, Map<String, WebSocketSession> playerSessions) {
        int roundDurationInSeconds = 40;
        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            int remainingTime = roundDurationInSeconds;

            @Override
            public void run() {
                if (remainingTime > 0) {

                    System.out.println("Sending remaining time: " + remainingTime);

                    for (WebSocketSession session : playerSessions.values()) {
                        if (session.isOpen()) {
                            try {
                                session.sendMessage(new TextMessage("{\"type\":\"timer\",\"remainingTime\":\"" + remainingTime + "\"}"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    remainingTime--;
                } else {
                    timer.cancel();
                    try {
                        endRound(roomId, playerSessions);
                    } catch (Exception e) {

                        throw new RuntimeException(e);
                    }
                }
            }
        }, 0, 1000);
    }

    private void endRound(String roomId, Map<String, WebSocketSession> playerSessions) throws InterruptedException, IOException {
        RoomDTO room = getRoomDetails(roomId);
        if (room != null) {
            int curRound = CurrentRound(roomId);
            room.setCurrentRound(curRound);

            System.out.println(room.getCurrentRound());

            for (WebSocketSession session : playerSessions.values()) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage("{\"type\":\"system\",\"message\":\"Time's up! The round has ended.\"}"));
                        session.sendMessage(new TextMessage("{\"type\":\"restartAccess\"}"));
                        session.sendMessage(new TextMessage("{\"type\":\"disableChat\"}"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }


            String drawer = room.getCurrentDrawer();
            String word = room.getCurrentWord();
            WebSocketSession drawerSession = playerSessions.get(drawer);
            if (drawerSession != null && drawerSession.isOpen()) {
                String message = String.format("{\"type\":\"validation\",\"word\":\"%s\",\"roomId\":\"%s\"}", word, roomId);
                try {
                    drawerSession.sendMessage(new TextMessage(message));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            for (WebSocketSession session : playerSessions.values()) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage("{\"type\":\"clear\"}"));
                        session.sendMessage(new TextMessage("{\"type\":\"clearChat\"}"));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (room.getCurrentRound() >= room.getNumRounds()) {
                Thread.sleep(20000);
                addPoint(roomId, playerSessions);
                endGame(playerSessions);
            } else {
                Thread.sleep(10000);
                addPoint(roomId, playerSessions);
                startGame(roomId, playerSessions);
            }
        }
    }

    private void endGame(Map<String, WebSocketSession> playerSessions) {
        for (WebSocketSession session : playerSessions.values()) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage("{\"type\":\"system\",\"message\":\"The game has ended!\"}"));
                    session.sendMessage(new TextMessage("{\"type\":\"clear\"}"));
                    session.sendMessage(new TextMessage("{\"type\":\"clearChat\"}"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String selectRandomDrawer(String roomID) {
        RoomEntity room = roomRepository.findById(roomID)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        List<MyAppUser> players = room.getPlayers();

        if (players.isEmpty()) {
            throw new RuntimeException("No players in the room");
        }

        Random rand = new Random();
        int index = rand.nextInt(players.size());
        MyAppUser selectedDrawer = players.get(index);

        room.setCurrentDrawer(selectedDrawer.getUsername());

        roomRepository.save(room);

        return selectedDrawer.getUsername();
    }

    public String selectRandomWord(String roomID) {
        RoomEntity room = roomRepository.findById(roomID)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Random rand = new Random();
        int index = rand.nextInt(WORDS.length);
        String word = WORDS[index];
        room.setCurrentWord(word);
        roomRepository.save(room);
        return word;
    }

    public int CurrentRound(String roomID) {
        RoomEntity room = roomRepository.findById(roomID)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setCurrentRound(room.getCurrentRound() + 1);
        roomRepository.save(room);
        return room.getCurrentRound();
    }

    public void checkTheGuess(String roomId, String guess, String guessingPlayerUsername) {
        System.out.println("Received guess: " + guess + " from player: " + guessingPlayerUsername + " in room: " + roomId);

        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));


        if (guess.equals(room.getCurrentWord())) {
            if (room.getFirstCorrectGuesser() == null) {
                room.setFirstCorrectGuesser(guessingPlayerUsername);
                roomRepository.save(room);
                System.out.println("firstCorrectGuesser for room " + roomId + ": " + guessingPlayerUsername);
            }
        }
    }

    public void addPoint(String roomId, Map<String, WebSocketSession> playerSessions) {
        System.out.println("roomID form addint point: " + roomId);
        RoomEntity roomEn = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        List<MyAppUser> players = roomEn.getPlayers();
        String curDrawer = roomEn.getCurrentDrawer();

        for (MyAppUser player : players) {

            if (Objects.equals(player.getUsername(), roomEn.getFirstCorrectGuesser())) {
                System.out.println(player.getUsername());

                ScoreEntity score = scoreRepository.findByRoomIdAndUserId(roomId, player.getUserId());
                int updateScore = score.getRoomScore();
                score.setRoomScore(10 + updateScore);
                scoreRepository.save(score);

                int currentTotalScore = player.getTotalScore();
                player.setTotalScore(currentTotalScore + 10);
                myAppUserRepository.save(player);

            }
            if (Objects.equals(player.getUsername(), curDrawer)) {
                System.out.println(player.getUsername());
                ScoreEntity score = scoreRepository.findByRoomIdAndUserId(roomId, player.getUserId());
                int updateScore = score.getRoomScore();
                score.setRoomScore(roomEn.getCurAiPoints() + updateScore);
                scoreRepository.save(score);

                int currentTotalScore = player.getTotalScore();
                player.setTotalScore(currentTotalScore + roomEn.getCurAiPoints());
                myAppUserRepository.save(player);

            }
        }

        roomEn.setFirstCorrectGuesser(null);
        roomRepository.save(roomEn);

        String scoreboardMessage = getScoreboard(players, roomId);
        updateScoreboardForRoom(scoreboardMessage, playerSessions);
    }

    private String getScoreboard(List<MyAppUser> players, String roomId) {
        StringBuilder scoreboard = new StringBuilder();
        scoreboard.append("{\"type\":\"scoreboard\",\"players\":[");
        for (int i = 0; i < players.size(); i++) {
            MyAppUser player = players.get(i);
            ScoreEntity score = scoreRepository.findByRoomIdAndUserId(roomId, player.getUserId());
            scoreboard.append("{\"name\":\"")
                    .append(player.getUsername())
                    .append("\",\"score\":")
                    .append(score.getRoomScore())
                    .append("}");
            if (i < players.size() - 1) {
                scoreboard.append(",");
            }
        }
        scoreboard.append("]}");
        return scoreboard.toString();
    }

    public void updateScoreboardForRoom(String scoreboardMessage, Map<String, WebSocketSession> playerSessions) {
        for (WebSocketSession session : playerSessions.values()) {
            try {
                session.sendMessage(new TextMessage(scoreboardMessage));
            } catch (IOException e) {
                System.err.println("Failed to send scoreboard update to session: " + session.getId());
                e.printStackTrace();
            }
        }
    }

    public boolean ValidCred(String username, String email) {
        Optional<MyAppUser> myAppUse = myAppUserRepository.findByUsername(username);
        Optional<MyAppUser> myAppEmail = myAppUserRepository.findByEmail(email);
        if (myAppUse.isPresent() || myAppEmail.isPresent()) {
            return false;
        }
        return true;
    }


}