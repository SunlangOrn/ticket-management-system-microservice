package com.mentor.ticketservice.reservation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "tms.reservation")
public record ReservationProperties(@DefaultValue("10m") Duration hold) { }
