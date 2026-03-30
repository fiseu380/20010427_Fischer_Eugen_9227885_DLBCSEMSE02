package de.iu.zanshintracker.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import de.iu.zanshintracker.model.ProgressEntry;

/**
 * Manages the persistence of setup data using SharedPreferences.
 * This class handles saving and loading the user's project settings.
 */
public class PersistenceManager {

    // ===========================================================
    // 1. CONSTANTS
    // ===========================================================
    private static final String PREF_NAME = "ZanshinTrackerPrefs";
    private static final String KEY_CATEGORY = "user_category";
    private static final String KEY_GOAL_AMOUNT = "user_goal_amount";
    private static final String KEY_GOAL_UNIT = "user_goal_unit";
    private static final String KEY_DEADLINE = "user_deadline";
    private static final String KEY_TIME_HOURS = "user_time_hours";
    private static final String KEY_PROGRESS_HISTORY = "user_progress_history";
    private static final String KEY_TRACKED_TIME_MINUTES = "user_tracked_time_minutes";

    // Pomodoro Timer State & Settings
    private static final String KEY_POMO_RUNNING = "pomo_running";
    private static final String KEY_POMO_END_TIME = "pomo_end_time";
    private static final String KEY_POMO_IS_WORK = "pomo_is_work";
    private static final String KEY_POMO_CUR_CYCLE = "pomo_cur_cycle";
    private static final String KEY_POMO_TOT_CYCLES = "pomo_tot_cycles";
    private static final String KEY_POMO_WORK_MS = "pomo_work_ms";
    private static final String KEY_POMO_BREAK_MS = "pomo_break_ms";
    private static final String KEY_POMO_WORKED_MIN = "pomo_worked_min";
    private static final String KEY_POMO_DEF_WORK = "pomo_def_work";
    private static final String KEY_POMO_DEF_BREAK = "pomo_def_break";
    private static final String KEY_POMO_DEF_CYCLES = "pomo_def_cycles";

    // ===========================================================
    // 2. VARIABLES
    // ===========================================================
    private final SharedPreferences prefs;
    private final Gson gson;

    /**
     * Constructor for the PersistenceManager.
     *
     * @param context The context needed to access SharedPreferences.
     */
    public PersistenceManager(Context context) {
        // Use private mode so only this app can access the data
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    /**
     * Saves the complete setup configuration to the storage.
     *
     * @param category   The chosen project category.
     * @param goalAmount The target amount (e.g. 500).
     * @param goalUnit   The unit for the goal (e.g. words).
     * @param deadline   The target date as a string.
     * @param timeHours  The total time commitment in hours.
     */
    public void saveSetupData(String category, int goalAmount, String goalUnit, String deadline, int timeHours) {
        // 1. Save all setup values and clear history
        prefs.edit()
                .putString(KEY_CATEGORY, category)
                .putInt(KEY_GOAL_AMOUNT, goalAmount)
                .putString(KEY_GOAL_UNIT, goalUnit)
                .putString(KEY_DEADLINE, deadline)
                .putInt(KEY_TIME_HOURS, timeHours)
                .putString(KEY_PROGRESS_HISTORY, "[]")
                .apply();
    }

    /**
     * Returns the saved project category.
     *
     * @return The category string (default empty).
     */
    public String getCategory() {
        return prefs.getString(KEY_CATEGORY, "");
    }

    /**
     * Returns the saved goal amount.
     *
     * @return The goal as an integer (default 0).
     */
    public int getGoalAmount() {
        return prefs.getInt(KEY_GOAL_AMOUNT, 0);
    }

    /**
     * Returns the saved unit for the goal.
     *
     * @return The unit string (default empty).
     */
    public String getGoalUnit() {
        return prefs.getString(KEY_GOAL_UNIT, "");
    }

    /**
     * Returns the saved deadline.
     *
     * @return The deadline string (default empty).
     */
    public String getDeadline() {
        return prefs.getString(KEY_DEADLINE, "");
    }

    /**
     * Returns the saved time commitment.
     *
     * @return The hours as an integer (default 0).
     */
    public int getTimeHours() {
        return prefs.getInt(KEY_TIME_HOURS, 0);
    }

    /**
     * Retrieves the full history of progress entries.
     */
    public List<ProgressEntry> getHistory() {
        String json = prefs.getString(KEY_PROGRESS_HISTORY, "[]");
        Type type = new TypeToken<ArrayList<ProgressEntry>>() {
        }.getType();
        return gson.fromJson(json, type);
    }                

    /**
     * Adds a new entry to the history with the current timestamp.
     */
    public void addTargetProgress(int addedAmount) {
        List<ProgressEntry> history = getHistory();

        ProgressEntry newEntry = new ProgressEntry(System.currentTimeMillis(), addedAmount);
        history.add(newEntry);

        String json = gson.toJson(history);
        prefs.edit().putString(KEY_PROGRESS_HISTORY, json).apply();
    }

    /**
     * Calculates total progress by summing up all entries from history.
     */
    public int getCurrentTargetProgress() {
        List<ProgressEntry> history = getHistory();
        int total = 0;
        for (ProgressEntry entry : history) {
            total += entry.getAmount();
        }
        return total;
    }

    /**
     * Clears all saved setup data to start a new goal.
     */
    public void clearSetupData() {
        prefs.edit().clear().apply();
    }

    /**
     * Deletes entry from the history based on its list index.
     *
     * @param index The position of the item to delete.
     */
    public void deleteProgressEntry(int index) {
        List<ProgressEntry> history = getHistory();

        // Safety check to prevent app crashes
        if (index >= 0 && index < history.size()) {
            history.remove(index);

            // Save the updated list back to SharedPreferences
            String json = gson.toJson(history);
            prefs.edit().putString(KEY_PROGRESS_HISTORY, json).apply();
        }
    }

    /**
     * Adds worked minutes from the Pomodoro timer to the total tracked time.
     *
     * @param minutes The amount of minutes focused.
     */
    public void addTrackedTime(int minutes) {
        int currentMinutes = getTrackedTimeMinutes();
        prefs.edit().putInt(KEY_TRACKED_TIME_MINUTES, currentMinutes + minutes).apply();
    }

    /**
     * Returns the total tracked time in minutes.
     *
     * @return The tracked time (default 0).
     */
    public int getTrackedTimeMinutes() {
        return prefs.getInt(KEY_TRACKED_TIME_MINUTES, 0);
    }


    /**
     * Saves the exact state of the Pomodoro timer when the app goes into the background.
     */
    public void savePomodoroState(boolean isRunning, long endTime, boolean isWorkPhase,
                                  int currentCycle, int totalCycles, long workTimeMs,
                                  long breakTimeMs, int totalWorkedMin) {
        prefs.edit()
                .putBoolean(KEY_POMO_RUNNING, isRunning)
                .putLong(KEY_POMO_END_TIME, endTime)
                .putBoolean(KEY_POMO_IS_WORK, isWorkPhase)
                .putInt(KEY_POMO_CUR_CYCLE, currentCycle)
                .putInt(KEY_POMO_TOT_CYCLES, totalCycles)
                .putLong(KEY_POMO_WORK_MS, workTimeMs)
                .putLong(KEY_POMO_BREAK_MS, breakTimeMs)
                .putInt(KEY_POMO_WORKED_MIN, totalWorkedMin)
                .apply();
    }

    /**
     * Saves the default user settings for the Pomodoro timer.
     */
    public void savePomodoroSettings(int work, int breakTime, int cycles) {
        prefs.edit()
                .putInt(KEY_POMO_DEF_WORK, work)
                .putInt(KEY_POMO_DEF_BREAK, breakTime)
                .putInt(KEY_POMO_DEF_CYCLES, cycles)
                .apply();
    }

    /**
     * Retrieves the saved default work time in minutes.
     *
     * @return The saved work time, or 30 as fallback.
     */
    public int getPomodoroDefWork() {
        return prefs.getInt(KEY_POMO_DEF_WORK, 30);
    }

    /**
     * Retrieves the saved default break time in minutes.
     *
     * @return The saved break time, or 5 as fallback.
     */
    public int getPomodoroDefBreak() {
        return prefs.getInt(KEY_POMO_DEF_BREAK, 5);
    }

    /**
     * Retrieves the saved default number of cycles.
     *
     * @return The saved cycles amount, or 1 as fallback.
     */
    public int getPomodoroDefCycles() {
        return prefs.getInt(KEY_POMO_DEF_CYCLES, 1);
    }

    // ===========================================================
    // 3. GETTER POMODORO STATE
    // ===========================================================

    /**
     * Checks if the Pomodoro timer was running when the app went into the background.
     *
     * @return true if the timer was running, false otherwise.
     */
    public boolean isPomodoroRunning() {
        return prefs.getBoolean(KEY_POMO_RUNNING, false);
    }

    /**
     * Returns the saved end time of the active phase.
     *
     * @return The end time in milliseconds (default 0).
     */
    public long getPomodoroEndTime() {
        return prefs.getLong(KEY_POMO_END_TIME, 0);
    }

    /**
     * Checks whether the saved phase is a work phase or a break phase.
     *
     * @return true if work phase, false if break phase.
     */
    public boolean isPomodoroWorkPhase() {
        return prefs.getBoolean(KEY_POMO_IS_WORK, true);
    }

    /**
     * Returns the current cycle number of the saved Pomodoro session.
     *
     * @return The current cycle (default 1).
     */
    public int getPomodoroCurrentCycle() {
        return prefs.getInt(KEY_POMO_CUR_CYCLE, 1);
    }

    /**
     * Returns the total number of cycles of the saved Pomodoro session.
     *
     * @return The total cycles (default 1).
     */
    public int getPomodoroTotalCycles() {
        return prefs.getInt(KEY_POMO_TOT_CYCLES, 1);
    }

    /**
     * Returns the saved work phase duration in milliseconds.
     *
     * @return The work time in milliseconds (default 0).
     */
    public long getPomodoroWorkTimeMs() {
        return prefs.getLong(KEY_POMO_WORK_MS, 0);
    }

    /**
     * Returns the saved break phase duration in milliseconds.
     *
     * @return The break time in milliseconds (default 0).
     */
    public long getPomodoroBreakTimeMs() {
        return prefs.getLong(KEY_POMO_BREAK_MS, 0);
    }

    /**
     * Returns the minutes already worked in the current Pomodoro session.
     *
     * @return The worked minutes (default 0).
     */
    public int getPomodoroWorkedMinutes() {
        return prefs.getInt(KEY_POMO_WORKED_MIN, 0);
    }
}
