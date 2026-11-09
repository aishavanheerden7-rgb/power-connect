package com.powerconnect;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PowerConnectApplication {

    public static void main(String[] args) {
        // All times in the app are Lusaka time, wherever the server runs
        TimeZone.setDefault(TimeZone.getTimeZone("Africa/Lusaka"));
        SpringApplication.run(PowerConnectApplication.class, args);
    }
}
