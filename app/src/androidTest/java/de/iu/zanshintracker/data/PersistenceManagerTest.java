package de.iu.zanshintracker.data;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import de.iu.zanshintracker.model.ProgressEntry;

/**
 * Instrumented test for the PersistenceManager.
 * Checks if setup data and progress are correctly saved, updated, and cleared.
 */
@RunWith(AndroidJUnit4.class)
public class PersistenceManagerTest {

    private PersistenceManager persistenceManager;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        persistenceManager = new PersistenceManager(context);

        // This clears data before each test to ensure a clean slate!
        persistenceManager.clearSetupData();
    }

    @Test
    public void testSaveAndReadAllData() {
        // 1. Arrange
        String testCategory = "Writing thesis";
        int testGoalAmount = 500;
        String testGoalUnit = "pages";
        String testDeadline = "24.12.2025";
        int testTimeHours = 40;

        // 2. Act
        persistenceManager.saveSetupData(testCategory, testGoalAmount, testGoalUnit, testDeadline, testTimeHours);

        // 3. Assert
        Assert.assertEquals(testCategory, persistenceManager.getCategory());
        Assert.assertEquals(testGoalAmount, persistenceManager.getGoalAmount());
        Assert.assertEquals(testGoalUnit, persistenceManager.getGoalUnit());
        Assert.assertEquals(testDeadline, persistenceManager.getDeadline());
        Assert.assertEquals(testTimeHours, persistenceManager.getTimeHours());

        // 4. Assert history is completely empty after a fresh setup
        Assert.assertEquals(0, persistenceManager.getCurrentTargetProgress());
        Assert.assertTrue(persistenceManager.getHistory().isEmpty());
    }

    @Test
    public void testAddTargetProgress() {
        // 1. Arrange: Start with a clean setup
        persistenceManager.saveSetupData("Test", 100, "pages", "01.01.2030", 0);

        // 2. Act: Add progress twice
        persistenceManager.addTargetProgress(20);
        persistenceManager.addTargetProgress(30);

        // 3. Assert: 0 + 20 + 30 = 50
        Assert.assertEquals(50, persistenceManager.getCurrentTargetProgress());

        // 4. Assert History List
        List<ProgressEntry> history = persistenceManager.getHistory();
        Assert.assertEquals(2, history.size());
        Assert.assertEquals(20, history.get(0).getAmount());
        Assert.assertEquals(30, history.get(1).getAmount());
    }

    @Test
    public void testDeleteProgressEntry() {
        // 1. Arrange: Add two entries
        persistenceManager.saveSetupData("Test", 100, "pages", "01.01.2030", 0);
        persistenceManager.addTargetProgress(20);
        persistenceManager.addTargetProgress(30);

        // 2. Act: Delete the first entry
        persistenceManager.deleteProgressEntry(0);

        // 3. Assert History List
        List<ProgressEntry> history = persistenceManager.getHistory();
        Assert.assertEquals(1, history.size()); // Only one left
        Assert.assertEquals(30, history.get(0).getAmount()); // The "30" is now at the first position

        // 4. Assert Total Progress 30 expected
        Assert.assertEquals(30, persistenceManager.getCurrentTargetProgress());
    }

    @Test
    public void testClearSetupData() {
        // 1. Arrange: Save some data and progress
        persistenceManager.saveSetupData("Test", 100, "pages", "01.01.2030", 0);
        persistenceManager.addTargetProgress(50);

        // 2. Act: Clear everything
        persistenceManager.clearSetupData();

        // 3. Assert: Everything must be back to default values
        Assert.assertEquals("", persistenceManager.getCategory());
        Assert.assertEquals(0, persistenceManager.getGoalAmount());
        Assert.assertEquals("", persistenceManager.getDeadline());
        Assert.assertEquals(0, persistenceManager.getCurrentTargetProgress());
        Assert.assertTrue(persistenceManager.getHistory().isEmpty());
    }
}