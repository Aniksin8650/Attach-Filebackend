package com.example.attachfile.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static final DateTimeFormatter FRONTEND = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter ORACLE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");

    public static LocalDate fromFrontend(String date) {
        return (date == null || date.isBlank()) ? null : LocalDate.parse(date, FRONTEND);
    }

    public static String toFrontend(LocalDate date) {
        return (date == null) ? null : date.format(FRONTEND);
    }

    public static String toDDMMYYYY(LocalDate date) {
        return (date == null) ? null : date.format(ORACLE_FORMAT);
    }
}
