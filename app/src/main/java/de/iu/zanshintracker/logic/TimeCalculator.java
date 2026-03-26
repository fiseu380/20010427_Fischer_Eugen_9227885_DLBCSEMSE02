package de.iu.zanshintracker.logic;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class TimeCalculator {

    /**
     * Utility class for time and date calculations.
     * Separated from UI for clean architecture and easy unit testing.
     */

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
}

