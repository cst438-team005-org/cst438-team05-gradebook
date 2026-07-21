package com.cst438.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

@RestController
public class HealthCheckController {

    private final ApplicationContext appContext;

    public HealthCheckController(ApplicationContext appContext) {
        this.appContext = appContext;
    }

    /*
     * health check return ip address and pid of server
     */
    @GetMapping("/")
    public String healthCheck() {
        try {
            String ip = InetAddress.getLocalHost().toString();
            long pid = ProcessHandle.current().pid();
            return ip+" pid="+pid;
        } catch (UnknownHostException e) {
            return "unknown ip";
        }
    }
    /*
     * terminate the server
     */
    @GetMapping("/exit")
    public void exit() {
        SpringApplication.exit(appContext, () -> 1);
    }
}
