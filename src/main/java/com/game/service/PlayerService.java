package com.game.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.game.entity.Player;

public interface PlayerService extends IService<Player> {
    Player login(String username, String password);
    Player register(Player player);
    void updateLastLoginTime(Long playerId);
}