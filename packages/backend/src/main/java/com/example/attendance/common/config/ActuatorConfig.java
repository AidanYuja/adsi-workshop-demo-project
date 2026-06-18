package com.example.attendance.common.config;

import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class ActuatorConfig {

    @Bean
    public InfoContributor appInfoContributor() {
        return builder -> builder.withDetails(Map.of(
            "app", Map.of(
                "name", "attendance",
                "description", "Attendance Management System"
            )
        ));
    }
}
