package de.iu.zanshintracker.logic;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for time and date calculations.
 * Separated from UI for clean architecture and easy unit testing.
 */
public class TimeCalculator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("d.M.yyyy");

    /**
     * Parses a date string into a LocalDate object.
     */
    public static LocalDate parseDate(String dateStr) throws DateTimeParseException {
        return LocalDate.parse(dateStr, FORMATTER);
    }

    /**
     * Calculates the days between a start date and an end date.
     */
    public static long calculateDaysBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Calculates the total hours required based on days and daily commitment.
     */
    public static long calculateTotalHours(long days, int hoursPerDay) {
        if (days < 0 || hoursPerDay < 0) {
            return 0; // Prevent negative calculations
        }
        return days * hoursPerDay;
    }

    // ===========================================================
    // POMODORO TIMER SECTION
    // ===========================================================

    /**
     * Converts minutes to milliseconds.
     */
    public static long minutesToMillis(long minutes) {
        return minutes * 60 * 1000;
    }

    /**
     * Converts milliseconds to full minutes.
     */
    public static int millisToMinutes(long millis) {
        return (int) (millis / 1000 / 60);
    }

    /**
     * Formats milliseconds into a MM:SS string.
     */
    public static String formatMillisToMMSS(long millis) {
        int minutes = (int) (millis / 1000) / 60;
        int seconds = (int) (millis / 1000) % 60;
        return String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
}

