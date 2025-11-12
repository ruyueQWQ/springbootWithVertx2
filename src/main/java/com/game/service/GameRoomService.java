package com.game.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.game.entity.GameRoom;

import java.util.List;

public interface GameRoomService extends IService<GameRoom> {
    GameRoom createRoom(Long playerId);
    GameRoom joinRoom(String roomCode, Long playerId);
    boolean leaveRoom(String roomCode, Long playerId);
    List<GameRoom> getWaitingRooms();
    void startGame(Long roomId);
    void endGame(Long roomId, Long winnerId);
}