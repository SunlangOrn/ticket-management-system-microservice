package com.mentor.ticketservice.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(
        prefix = "tms.reservation",
        name = "expiry-scheduler-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class SchedulingConfig {
}
