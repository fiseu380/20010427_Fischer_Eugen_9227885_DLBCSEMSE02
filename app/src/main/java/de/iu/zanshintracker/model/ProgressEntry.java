package de.iu.zanshintracker.model;

/**
 * Model class representing a single progress entry.
 * Holds the data for the time it was created and the amount added.
 */
public class ProgressEntry {
    // ===========================================================
    // 1. VARIABLES
    // ===========================================================
    private final long timestamp;
    private final int amount;

    // ===========================================================
    // 2. CONSTRUCTOR
    // ===========================================================
    /**
     * Creates a new ProgressEntry.
     *
     * @param timestamp The exact time the progress was added (in milliseconds).
     * @param amount    The amount of progress added.
     */
    public ProgressEntry(long timestamp, int amount) {
        this.timestamp = timestamp;
        this.amount = amount;
    }

    // ===========================================================
    // 3. GETTER METHODS
    // ===========================================================
    public long getTimestamp() {
        return timestamp;
    }

    public int getAmount() {
        return amount;
    }
}
