package com.bharat.SpendLens.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

@Service
public class DateZone {

    /**
     * Convert IST start of day (00:00:00) to UTC Instant
     */
    public Instant convertToInstant(String date) {
        LocalDate localDate = LocalDate.parse(date);
        return localDate.atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant();
    }

    /**
     * Convert IST end of day (23:59:59.999999999) to UTC Instant
     */
    public Instant convertToInstantEndOfDay(String date) {
        LocalDate localDate = LocalDate.parse(date);
        return localDate.atTime(LocalTime.MAX)
                .atZone(ZoneId.of("Asia/Kolkata"))
                .toInstant();
    }
}
