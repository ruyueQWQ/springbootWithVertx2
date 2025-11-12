package com.game.tcp;

import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class GameSessionManager {
    // 玩家ID -> 网络连接
    private final Map<Long, NetSocket> playerSockets = new ConcurrentHashMap<>();
    // 网络连接 -> 玩家ID
    private final Map<NetSocket, Long> socketPlayers = new ConcurrentHashMap<>();
    // 玩家ID -> 当前所在房间ID
    private final Map<Long, Long> playerRooms = new ConcurrentHashMap<>();
    // 房间ID -> 房间内的玩家列表
    private final Map<Long, Map<Long, NetSocket>> roomPlayers = new ConcurrentHashMap<>();

    public void addSession(Long playerId, NetSocket socket) {
        log.info("Player {} connected, adding session", playerId);
        playerSockets.put(playerId, socket);
        socketPlayers.put(socket, playerId);
        log.debug("Session added successfully for player {}", playerId);
    }

    public void removeSession(NetSocket socket) {
        Long playerId = socketPlayers.remove(socket);
        if (playerId != null) {
            log.info("Removing session for player {}", playerId);
            playerSockets.remove(playerId);
            Long roomId = playerRooms.remove(playerId);
            if (roomId != null) {
                log.debug("Player {} left room {}", playerId, roomId);
                Map<Long, NetSocket> room = roomPlayers.get(roomId);
                if (room != null) {
                    room.remove(playerId);
                    if (room.isEmpty()) {
                        log.debug("Room {} is now empty, removing it", roomId);
                        roomPlayers.remove(roomId);
                    }
                }
            }
            log.info("Session removed successfully for player {}", playerId);
        }
    }

    public void joinRoom(Long playerId, Long roomId) {
        log.info("Player {} joining room {}", playerId, roomId);
        playerRooms.put(playerId, roomId);
        roomPlayers.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>())
                  .put(playerId, playerSockets.get(playerId));
        log.debug("Player {} joined room {} successfully", playerId, roomId);
    }

    public void leaveRoom(Long playerId) {
        log.info("Player {} leaving room", playerId);
        Long roomId = playerRooms.remove(playerId);
        if (roomId != null) {
            log.debug("Player {} left room {}", playerId, roomId);
            Map<Long, NetSocket> room = roomPlayers.get(roomId);
            if (room != null) {
                room.remove(playerId);
                if (room.isEmpty()) {
                    log.debug("Room {} is now empty, removing it", roomId);
                    roomPlayers.remove(roomId);
                }
            }
        }
    }

    public Long getPlayerId(NetSocket socket) {
        return socketPlayers.get(socket);
    }

    public NetSocket getPlayerSocket(Long playerId) {
        return playerSockets.get(playerId);
    }

    public Long getPlayerRoom(Long playerId) {
        return playerRooms.get(playerId);
    }

    public Map<Long, NetSocket> getRoomPlayers(Long roomId) {
        return roomPlayers.getOrDefault(roomId, new ConcurrentHashMap<>());
    }

    public boolean isPlayerOnline(Long playerId) {
        return playerSockets.containsKey(playerId);
    }
}