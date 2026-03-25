package de.iu.zanshintracker.data;

import android.content.Context;
import android.content.SharedPreferences;

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
    private static final String KEY_TARGET_PROGRESS = "user_target_progress";

    // ===========================================================
    // 2. VARIABLES
    // ===========================================================
    private final SharedPreferences prefs;

    /**
     * Constructor for the PersistenceManager.
     *
     * @param context The context needed to access SharedPreferences.
     */
    public PersistenceManager(Context context) {
        // Use private mode so only this app can access the data
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
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
        // 1. Open the editor to change values
        SharedPreferences.Editor editor = prefs.edit();

        // 2. Put all values into the editor
        editor.putString(KEY_CATEGORY, category);
        editor.putInt(KEY_GOAL_AMOUNT, goalAmount);
        editor.putString(KEY_GOAL_UNIT, goalUnit);
        editor.putString(KEY_DEADLINE, deadline);
        editor.putInt(KEY_TIME_HOURS, timeHours);

        // 3. Reset target progress
        editor.putInt(KEY_TARGET_PROGRESS, 0);

        // 4. Save everything
        editor.apply();
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
     * Adds new progress to the current target progress and saves it.
     *
     * @param addedAmount The amount the user just completed.
     */
    public void addTargetProgress(int addedAmount) {
        int current = getCurrentTargetProgress();
        prefs.edit().putInt(KEY_TARGET_PROGRESS, current + addedAmount).apply();
    }

    /**
     * Returns the currently achieved target progress.
     *
     * @return The current progress (default 0).
     */
    public int getCurrentTargetProgress() {
        return prefs.getInt(KEY_TARGET_PROGRESS, 0);
    }

    /**
     * Clears all saved setup data to start a new goal.
     */
    public void clearSetupData() {
        prefs.edit().clear().apply();
    }
}
