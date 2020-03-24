package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author VvGnaK
 * @date 2020-03-14 10:05
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class LeyouAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeyouAuthApplication.class);
    }
}
