package com.game.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class GameRoom {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String roomCode;
    private Integer status; // 0: 等待中, 1: 游戏中, 2: 已结束
    private Long player1Id;
    private Long player2Id;
    private Date createTime;
    private Date startTime;
    private Date endTime;
}