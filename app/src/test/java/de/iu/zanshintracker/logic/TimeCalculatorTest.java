package de.iu.zanshintracker.logic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.time.LocalDate;

/**
 * Unit tests for the TimeCalculator logic.
 */
public class TimeCalculatorTest {

    @Test
    public void testCalculateDaysBetween_futureDate() {
        LocalDate today = LocalDate.of(2026, 11, 1);
        LocalDate deadline = LocalDate.of(2026, 11, 11);

        long daysLeft = TimeCalculator.calculateDaysBetween(today, deadline);

        assertEquals(10, daysLeft);
    }

    @Test
    public void testCalculateDaysBetween_pastDate() {
        LocalDate today = LocalDate.of(2026, 10, 10);
        LocalDate deadline = LocalDate.of(2026, 10, 5);

        long daysLeft = TimeCalculator.calculateDaysBetween(today, deadline);

        assertEquals(-5, daysLeft);
    }

    @Test
    public void testCalculateTotalHours_validInput() {
        long totalHours = TimeCalculator.calculateTotalHours(10, 5);
        assertEquals(50, totalHours);
    }

    @Test
    public void testCalculateTotalHours_negativeDays() {
        long totalHours = TimeCalculator.calculateTotalHours(-5, 5);
        assertEquals(0, totalHours);
    }
}