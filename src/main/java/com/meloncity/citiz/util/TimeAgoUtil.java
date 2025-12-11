package com.meloncity.citiz.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeAgoUtil {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

    public static String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, now);

        long seconds = duration.getSeconds();
        long minutes = seconds / 60;
        long hours = seconds / 3600;
        long days = seconds / 86400;

        // 1일 미만
        if (days < 1) {
            if (seconds < 60) return "방금 전";
            if (minutes < 60) return minutes + "분 전";
            return hours + "시간 전";
        }

        // 일주일 미만
        if (days < 7) {
            return days + "일 전";
        }

        // 1주 이상 → yyyy년 MM월 dd일 형태로 표시
        return dateTime.format(DATE_FORMATTER);
    }
}
