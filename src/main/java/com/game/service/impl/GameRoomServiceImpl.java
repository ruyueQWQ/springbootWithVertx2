package com.game.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.game.entity.GameRoom;
import com.game.mapper.GameRoomMapper;
import com.game.service.GameRoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class GameRoomServiceImpl extends ServiceImpl<GameRoomMapper, GameRoom> implements GameRoomService {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int ROOM_CODE_LENGTH = 6;
    private final Random random = new Random();

    @Override
    public GameRoom createRoom(Long playerId) {
        log.info("Creating room for player: {}", playerId);
        GameRoom room = new GameRoom();
        String roomCode = generateRoomCode();
        room.setRoomCode(roomCode);
        room.setStatus(0); // 等待中
        room.setPlayer1Id(playerId);
        room.setCreateTime(new Date());
        save(room);
        log.info("Room created successfully: ID={}, code={}", room.getId(), roomCode);
        return room;
    }

    @Override
    public GameRoom joinRoom(String roomCode, Long playerId) {
        log.info("Player {} attempting to join room with code: {}", playerId, roomCode);
        QueryWrapper<GameRoom> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("room_code", roomCode)
                   .eq("status", 0); // 只查找等待中的房间
        GameRoom room = getOne(queryWrapper);
        
        if (room != null) {
            if (room.getPlayer1Id().equals(playerId)) {
                log.warn("Player {} trying to join their own room: {}", playerId, roomCode);
            } else {
                room.setPlayer2Id(playerId);
                updateById(room);
                log.info("Player {} successfully joined room: {} (ID: {})", playerId, roomCode, room.getId());
            }
        } else {
            log.warn("Room not found or not available: {}", roomCode);
        }
        
        return room;
    }

    @Override
    public boolean leaveRoom(String roomCode, Long playerId) {
        log.info("Player {} attempting to leave room: {}", playerId, roomCode);
        QueryWrapper<GameRoom> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("room_code", roomCode)
                   .and(qw -> qw.eq("player1_id", playerId).or().eq("player2_id", playerId));
        GameRoom room = getOne(queryWrapper);
        
        if (room != null && room.getStatus() == 0) { // 只有等待中的房间可以离开
            if (room.getPlayer1Id().equals(playerId)) {
                // 创建者离开，删除房间
                removeById(room.getId());
                log.info("Room {} deleted as creator (player {}) left", roomCode, playerId);
            } else {
                // 其他玩家离开，清空player2Id
                room.setPlayer2Id(null);
                updateById(room);
                log.info("Player {} left room {}, position available again", playerId, roomCode);
            }
            return true;
        } else if (room == null) {
            log.warn("Player {} trying to leave non-existent room: {}", playerId, roomCode);
        } else {
            log.warn("Player {} cannot leave room {} (not in waiting status)", playerId, roomCode);
        }
        
        return false;
    }

    @Override
    public List<GameRoom> getWaitingRooms() {
        log.debug("Retrieving list of waiting rooms");
        QueryWrapper<GameRoom> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 0) // 等待中
                   .isNull("player2_id"); // 还有空位
        List<GameRoom> rooms = list(queryWrapper);
        log.debug("Found {} waiting rooms with available positions", rooms.size());
        return rooms;
    }

    @Override
    public void startGame(Long roomId) {
        log.info("Starting game in room: {}", roomId);
        GameRoom room = new GameRoom();
        room.setId(roomId);
        room.setStatus(1); // 游戏中
        room.setStartTime(new Date());
        updateById(room);
        log.info("Game started in room: {}", roomId);
    }

    @Override
    public void endGame(Long roomId, Long winnerId) {
        log.info("Ending game in room: {}, winner: {}", roomId, winnerId);
        GameRoom room = new GameRoom();
        room.setId(roomId);
        room.setStatus(2); // 已结束
        room.setEndTime(new Date());
        updateById(room);
        log.info("Game ended in room: {}", roomId);
    }

    private String generateRoomCode() {
        StringBuilder sb = new StringBuilder(ROOM_CODE_LENGTH);
        for (int i = 0; i < ROOM_CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}