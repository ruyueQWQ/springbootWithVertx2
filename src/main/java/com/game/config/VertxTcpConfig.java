package com.game.config;

import com.game.tcp.GameTcpHandler;
import io.vertx.core.Vertx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
public class VertxTcpConfig {

    @Autowired
    private GameTcpHandler gameTcpHandler;

    @Value("${game.tcp.port}")
    private int tcpPort;

    private Vertx vertx;

    @PostConstruct
    public void init() {
        vertx = Vertx.vertx();
        
        // 创建TCP服务器
        vertx.createNetServer()
             .connectHandler(gameTcpHandler)
             .listen(tcpPort, result -> {
                 if (result.succeeded()) {
                     System.out.println("TCP服务器启动成功，监听端口: " + tcpPort);
                 } else {
                     System.err.println("TCP服务器启动失败: " + result.cause().getMessage());
                 }
             });
    }

    @PreDestroy
    public void destroy() {
        if (vertx != null) {
            vertx.close();
            System.out.println("TCP服务器已关闭");
        }
    }

    @Bean
    public Vertx vertx() {
        return Vertx.vertx();
    }
}