package cn.rpc.lite.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Rpc服务器启动类
 */
@SpringBootApplication
@ComponentScan("cn.rpc.lite")
public class RpcBootstrap {

    public static void main(String[] args) {
        SpringApplication.run(RpcBootstrap.class, args);
    }

}
