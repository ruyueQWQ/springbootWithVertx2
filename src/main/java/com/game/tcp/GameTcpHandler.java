package com.game.tcp;

import com.game.protobuf.GameProto;
import com.game.service.GameRoomService;
import com.game.service.PlayerService;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
@Slf4j
@Component
public class GameTcpHandler implements Handler<NetSocket> {

    @Autowired
    private GameSessionManager sessionManager;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private GameRoomService gameRoomService;

    @Override
    public void handle(NetSocket socket) {
        log.info("New connection established from {}", socket.remoteAddress());
        // 读取消息前的处理
        socket.handler(buffer -> {
            try {
                log.debug("Received message with length: {}", buffer.length());
                // 解析Protobuf消息
                GameProto.GameMessage message = GameProto.GameMessage.parseFrom(buffer.getBytes());
                log.info("Processing message type: {}", message.getType());
                handleMessage(socket, message);
            } catch (Exception e) {
                log.error("Error parsing message: {}", e.getMessage());
                sendErrorMessage(socket, GameProto.ErrorCode.INVALID_REQUEST, "消息格式错误");
            }
        });

        // 连接关闭时的处理
        socket.closeHandler(v -> {
            log.info("Connection closed from {}", socket.remoteAddress());
            sessionManager.removeSession(socket);
        });

        // 连接异常时的处理
        socket.exceptionHandler(e -> {
            log.error("Connection error from {}: {}", socket.remoteAddress(), e.getMessage());
            sessionManager.removeSession(socket);
        });
    }

    private void handleMessage(NetSocket socket, GameProto.GameMessage message) {
        switch (message.getType()) {
            case LOGIN_REQUEST:
                handleLogin(socket, message.getLoginRequest());
                break;
            case REGISTER_REQUEST:
                handleRegister(socket, message.getRegisterRequest());
                break;
            case CREATE_ROOM_REQUEST:
                handleCreateRoom(socket, message.getCreateRoomRequest());
                break;
            case JOIN_ROOM_REQUEST:
                handleJoinRoom(socket, message.getJoinRoomRequest());
                break;
            case LEAVE_ROOM_REQUEST:
                handleLeaveRoom(socket, message.getLeaveRoomRequest());
                break;
            case LIST_ROOMS_REQUEST:
                handleListRooms(socket);
                break;
            case START_GAME_REQUEST:
                handleStartGame(socket, message.getStartGameRequest());
                break;
            case MOVE_REQUEST:
                log.info("Move request from {}", socket.remoteAddress());
                handleMove(socket, message.getMoveRequest());
                break;
            default:
                sendErrorMessage(socket, GameProto.ErrorCode.INVALID_REQUEST, "未知消息类型");
        }
    }

    private void handleLogin(NetSocket socket, GameProto.LoginRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());
        com.game.entity.Player player = playerService.login(request.getUsername(), request.getPassword());
        if (player != null) {
            // 更新最后登录时间
            playerService.updateLastLoginTime(player.getId());
            
            // 创建会话
            sessionManager.addSession(player.getId(), socket);
            
            log.info("Login successful for player: {} (ID: {})", player.getUsername(), player.getId());
            // 发送登录成功响应
            GameProto.PlayerInfo playerInfo = GameProto.PlayerInfo.newBuilder()
                    .setId(player.getId())
                    .setUsername(player.getUsername())
                    .setNickname(player.getNickname())
                    .setScore(player.getScore())
                    .build();
            
            GameProto.LoginResponse response = GameProto.LoginResponse.newBuilder()
                    .setCode(GameProto.ErrorCode.SUCCESS)
                    .setMessage("登录成功")
                    .setPlayerInfo(playerInfo)
                    .build();
            
            sendMessage(socket, GameProto.MessageType.LOGIN_RESPONSE, response);
        } else {
            log.warn("Login failed for username: {}", request.getUsername());
            sendErrorMessage(socket, GameProto.ErrorCode.USERNAME_PASSWORD_ERROR, "用户名或密码错误");
        }
    }

    private void handleRegister(NetSocket socket, GameProto.RegisterRequest request) {
        log.info("Registration attempt for username: {}", request.getUsername());
        // 检查用户名是否已存在
        com.game.entity.Player existingPlayer = playerService.login(request.getUsername(), request.getPassword());
        if (existingPlayer == null) {
            // 创建新玩家
            com.game.entity.Player player = new com.game.entity.Player();
            player.setUsername(request.getUsername());
            player.setPassword(request.getPassword());
            player.setNickname(request.getNickname());
            
            player = playerService.register(player);
            log.info("Registration successful for player: {} (ID: {})", player.getUsername(), player.getId());
            
            // 发送注册成功响应
            GameProto.PlayerInfo playerInfo = GameProto.PlayerInfo.newBuilder()
                    .setId(player.getId())
                    .setUsername(player.getUsername())
                    .setNickname(player.getNickname())
                    .setScore(player.getScore())
                    .build();
            
            GameProto.RegisterResponse response = GameProto.RegisterResponse.newBuilder()
                    .setCode(GameProto.ErrorCode.SUCCESS)
                    .setMessage("注册成功")
                    .setPlayerInfo(playerInfo)
                    .build();
            
            sendMessage(socket, GameProto.MessageType.REGISTER_RESPONSE, response);
        } else {
            log.warn("Registration failed: username already exists: {}", request.getUsername());
            sendErrorMessage(socket, GameProto.ErrorCode.USERNAME_EXISTS, "用户名已存在");
        }
    }

    private void handleCreateRoom(NetSocket socket, GameProto.CreateRoomRequest request) {
        Long playerId = request.getPlayerId();
        log.info("Player {} requesting to create room", playerId);
        if (sessionManager.isPlayerOnline(playerId)) {
            com.game.entity.GameRoom room = gameRoomService.createRoom(playerId);
            sessionManager.joinRoom(playerId, room.getId());
            log.info("Room created successfully with ID: {}, room code: {}", room.getId(), room.getRoomCode());
            
            // 构建房间信息
            GameProto.RoomInfo roomInfo = buildRoomInfo(room);
            
            GameProto.CreateRoomResponse response = GameProto.CreateRoomResponse.newBuilder()
                    .setCode(GameProto.ErrorCode.SUCCESS)
                    .setMessage("房间创建成功")
                    .setRoomInfo(roomInfo)
                    .build();
            
            sendMessage(socket, GameProto.MessageType.CREATE_ROOM_RESPONSE, response);
        } else {
            sendErrorMessage(socket, GameProto.ErrorCode.INVALID_REQUEST, "玩家未登录");
        }
    }

    private void handleJoinRoom(NetSocket socket, GameProto.JoinRoomRequest request) {
        Long playerId = request.getPlayerId();
        log.info("Player {} requesting to join room with code: {}", playerId, request.getRoomCode());
        if (sessionManager.isPlayerOnline(playerId)) {
            com.game.entity.GameRoom room = gameRoomService.joinRoom(request.getRoomCode(), playerId);
            if (room != null) {
                sessionManager.joinRoom(playerId, room.getId());
                
                // 构建房间信息
                GameProto.RoomInfo roomInfo = buildRoomInfo(room);
                
                // 发送给加入的玩家
                GameProto.JoinRoomResponse response = GameProto.JoinRoomResponse.newBuilder()
                        .setCode(GameProto.ErrorCode.SUCCESS)
                        .setMessage("加入房间成功")
                        .setRoomInfo(roomInfo)
                        .build();
                sendMessage(socket, GameProto.MessageType.JOIN_ROOM_RESPONSE, response);
                
                // 通知房间内其他玩家
                notifyRoomPlayers(room.getId(), GameProto.MessageType.GAME_STATE_UPDATE, null);
                log.info("Player {} joined room {} successfully", playerId, room.getId());
            } else {
                log.warn("Room not found or full: {}", request.getRoomCode());
                sendErrorMessage(socket, GameProto.ErrorCode.ROOM_NOT_FOUND, "房间不存在或已满");
            }
        } else {
            sendErrorMessage(socket, GameProto.ErrorCode.INVALID_REQUEST, "玩家未登录");
        }
    }

    private void handleLeaveRoom(NetSocket socket, GameProto.LeaveRoomRequest request) {
        Long playerId = request.getPlayerId();
        log.info("Player {} requesting to leave room: {}", playerId, request.getRoomCode());
        if (sessionManager.isPlayerOnline(playerId)) {
            boolean success = gameRoomService.leaveRoom(request.getRoomCode(), playerId);
            if (success) {
                sessionManager.leaveRoom(playerId);
                
                GameProto.LeaveRoomResponse response = GameProto.LeaveRoomResponse.newBuilder()
                        .setCode(GameProto.ErrorCode.SUCCESS)
                        .setMessage("离开房间成功")
                        .build();
                sendMessage(socket, GameProto.MessageType.LEAVE_ROOM_RESPONSE, response);
                log.info("Player {} left room successfully", playerId);
            } else {
                log.warn("Failed to leave room for player: {}", playerId);
                sendErrorMessage(socket, GameProto.ErrorCode.INVALID_REQUEST, "离开房间失败");
            }
        } else {
            sendErrorMessage(socket, GameProto.ErrorCode.INVALID_REQUEST, "玩家未登录");
        }
    }

    private void handleListRooms(NetSocket socket) {
        GameProto.ListRoomsResponse.Builder responseBuilder = GameProto.ListRoomsResponse.newBuilder()
                .setCode(GameProto.ErrorCode.SUCCESS)
                .setMessage("获取房间列表成功");
        
        for (com.game.entity.GameRoom room : gameRoomService.getWaitingRooms()) {
            responseBuilder.addRooms(buildRoomInfo(room));
        }
        
        sendMessage(socket, GameProto.MessageType.LIST_ROOMS_RESPONSE, responseBuilder.build());
    }

    private void handleStartGame(NetSocket socket, GameProto.StartGameRequest request) {
        Long playerId = request.getPlayerId();
        log.info("Player {} requesting to start game in room: {}", playerId, request.getRoomId());
        if (sessionManager.isPlayerOnline(playerId)) {
            gameRoomService.startGame(request.getRoomId());
            log.info("Game started in room: {}", request.getRoomId());
            
            GameProto.StartGameResponse response = GameProto.StartGameResponse.newBuilder()
                    .setCode(GameProto.ErrorCode.SUCCESS)
                    .setMessage("游戏开始")
                    .build();
            sendMessage(socket, GameProto.MessageType.START_GAME_RESPONSE, response);
            
            // 通知房间内所有玩家游戏开始
            notifyRoomPlayers(request.getRoomId(), GameProto.MessageType.START_GAME_RESPONSE, response);
        } else {
            sendErrorMessage(socket, GameProto.ErrorCode.INVALID_REQUEST, "玩家未登录");
        }
    }

    private void handleMove(NetSocket socket, GameProto.MoveRequest request) {
        Long playerId = request.getPlayerId();
        log.debug("Player {} move request: position({},{}) in room: {}", 
                  playerId, request.getX(), request.getY(), request.getRoomId());
        if (sessionManager.isPlayerOnline(playerId)) {
            // 创建位置更新消息
            GameProto.GameStateUpdate.Builder stateUpdateBuilder = GameProto.GameStateUpdate.newBuilder()
                    .setRoomId(request.getRoomId());
            
            GameProto.PlayerPosition position = GameProto.PlayerPosition.newBuilder()
                    .setPlayerId(playerId)
                    .setX(request.getX())
                    .setY(request.getY())
                    .build();
            
            stateUpdateBuilder.addPlayers(position);
            
            // 广播给房间内所有玩家
            notifyRoomPlayers(request.getRoomId(), GameProto.MessageType.GAME_STATE_UPDATE, stateUpdateBuilder.build());
        }
    }

    private GameProto.RoomInfo buildRoomInfo(com.game.entity.GameRoom room) {
        GameProto.RoomInfo.Builder builder = GameProto.RoomInfo.newBuilder()
                .setId(room.getId())
                .setRoomCode(room.getRoomCode())
                .setStatus(room.getStatus());
        
        // 添加玩家信息
        if (room.getPlayer1Id() != null) {
            com.game.entity.Player player1 = playerService.getById(room.getPlayer1Id());
            if (player1 != null) {
                builder.setPlayer1(GameProto.PlayerInfo.newBuilder()
                        .setId(player1.getId())
                        .setUsername(player1.getUsername())
                        .setNickname(player1.getNickname())
                        .setScore(player1.getScore())
                        .build());
            }
        }
        
        if (room.getPlayer2Id() != null) {
            com.game.entity.Player player2 = playerService.getById(room.getPlayer2Id());
            if (player2 != null) {
                builder.setPlayer2(GameProto.PlayerInfo.newBuilder()
                        .setId(player2.getId())
                        .setUsername(player2.getUsername())
                        .setNickname(player2.getNickname())
                        .setScore(player2.getScore())
                        .build());
            }
        }
        
        return builder.build();
    }

    private void notifyRoomPlayers(Long roomId, GameProto.MessageType messageType, Object messageBody) {
        Map<Long, NetSocket> players = sessionManager.getRoomPlayers(roomId);
        log.debug("Notifying {} players in room {} about message type: {}", 
                  players.size(), roomId, messageType);
        for (NetSocket playerSocket : players.values()) {
            sendMessage(playerSocket, messageType, messageBody);
        }
    }

    private void sendMessage(NetSocket socket, GameProto.MessageType messageType, Object messageBody) {
        GameProto.GameMessage.Builder messageBuilder = GameProto.GameMessage.newBuilder()
                .setType(messageType);
        log.info("send message: {}", messageBuilder.build());

        // 根据消息类型设置相应的消息体
        switch (messageType) {
            case LOGIN_RESPONSE:
                messageBuilder.setLoginResponse((GameProto.LoginResponse) messageBody);
                break;
            case REGISTER_RESPONSE:
                messageBuilder.setRegisterResponse((GameProto.RegisterResponse) messageBody);
                break;
            case CREATE_ROOM_RESPONSE:
                messageBuilder.setCreateRoomResponse((GameProto.CreateRoomResponse) messageBody);
                break;
            case JOIN_ROOM_RESPONSE:
                messageBuilder.setJoinRoomResponse((GameProto.JoinRoomResponse) messageBody);
                break;
            case LEAVE_ROOM_RESPONSE:
                messageBuilder.setLeaveRoomResponse((GameProto.LeaveRoomResponse) messageBody);
                break;
            case LIST_ROOMS_RESPONSE:
                messageBuilder.setListRoomsResponse((GameProto.ListRoomsResponse) messageBody);
                break;
            case START_GAME_RESPONSE:
                messageBuilder.setStartGameResponse((GameProto.StartGameResponse) messageBody);
                break;
            case GAME_STATE_UPDATE:
                messageBuilder.setGameStateUpdate((GameProto.GameStateUpdate) messageBody);
                break;
            case ERROR:
                messageBuilder.setError((GameProto.ErrorMessage) messageBody);
                break;
        }
        
        // 序列化并发送消息
        byte[] bytes = messageBuilder.build().toByteArray();
        socket.write(Buffer.buffer(bytes));
    }

    private void sendErrorMessage(NetSocket socket, GameProto.ErrorCode errorCode, String message) {
        GameProto.ErrorMessage errorMessage = GameProto.ErrorMessage.newBuilder()
                .setCode(errorCode)
                .setMessage(message)
                .build();
        log.info("send error message: {}", errorMessage);
        sendMessage(socket, GameProto.MessageType.ERROR, errorMessage);
    }
}