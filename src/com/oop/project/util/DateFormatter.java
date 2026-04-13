package com.oop.project.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter {
    private static final SimpleDateFormat fullFormat = new SimpleDateFormat("dd/mm/yyyy hh:mm:ss");
    private static final SimpleDateFormat shortFormat = new SimpleDateFormat("dd/mm/yyyy");

    public static String formatDateTime(Timestamp timestamp) {
        if (timestamp == null) return "N/A";
        return fullFormat.format(timestamp);
    }

    public static String formatDate(Date date) {
        if (date == null) return "N/A";
        return shortFormat.format(date);
    }

    public static Date parseDate(String dateStr) {
        try {
            return shortFormat.parse(dateStr);
        } catch (ParseException e) {
            return null; 
        }
    }
    
    public static Timestamp getCurrentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }
}