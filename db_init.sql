-- 创建数据库
CREATE DATABASE IF NOT EXISTS game_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE game_db;

-- 创建玩家表
CREATE TABLE IF NOT EXISTS player (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '玩家ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(50) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) NOT NULL COMMENT '昵称',
    score INT DEFAULT 0 COMMENT '分数',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    last_login_time DATETIME NOT NULL COMMENT '最后登录时间',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家表';

-- 创建游戏房间表
CREATE TABLE IF NOT EXISTS game_room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '房间ID',
    room_code VARCHAR(10) NOT NULL UNIQUE COMMENT '房间码',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '房间状态：0-等待中，1-游戏中，2-已结束',
    player1_id BIGINT NOT NULL COMMENT '玩家1 ID',
    player2_id BIGINT DEFAULT NULL COMMENT '玩家2 ID',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    start_time DATETIME DEFAULT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    INDEX idx_room_code (room_code),
    INDEX idx_status (status),
    INDEX idx_player1_id (player1_id),
    INDEX idx_player2_id (player2_id),
    FOREIGN KEY (player1_id) REFERENCES player(id),
    FOREIGN KEY (player2_id) REFERENCES player(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏房间表';

-- 插入测试数据
INSERT INTO player (username, password, nickname, score, create_time, last_login_time)
VALUES 
('player1', '123456', '玩家1', 0, NOW(), NOW()),
('player2', '123456', '玩家2', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE username=username;