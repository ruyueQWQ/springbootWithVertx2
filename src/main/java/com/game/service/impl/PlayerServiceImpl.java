package com.game.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.game.entity.Player;
import com.game.mapper.PlayerMapper;
import com.game.service.PlayerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class PlayerServiceImpl extends ServiceImpl<PlayerMapper, Player> implements PlayerService {

    @Override
    public Player login(String username, String password) {
        log.info("Login attempt for username: {}", username);
        QueryWrapper<Player> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username)
                   .eq("password", password);
        Player player = getOne(queryWrapper);
        if (player != null) {
            log.info("Login successful for player: {} (ID: {})", username, player.getId());
        } else {
            log.warn("Login failed for username: {}", username);
        }
        return player;
    }

    @Override
    public Player register(Player player) {
        log.info("Registering new player with username: {}", player.getUsername());
        player.setCreateTime(new Date());
        player.setLastLoginTime(new Date());
        player.setScore(0);
        save(player);
        log.info("Player registered successfully: {} (ID: {})", player.getUsername(), player.getId());
        return player;
    }

    @Override
    public void updateLastLoginTime(Long playerId) {
        log.debug("Updating last login time for player: {}", playerId);
        Player player = new Player();
        player.setId(playerId);
        player.setLastLoginTime(new Date());
        updateById(player);
    }
}