package de.iu.zanshintracker.data;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test for the PersistenceManager.
 * It checks if all setup data is correctly saved and loaded from SharedPreferences.
 */
@RunWith(AndroidJUnit4.class)
public class PersistenceManagerTest {

    private PersistenceManager persistenceManager;

    @Before
    public void setUp() {
        // 1. Get the context from the Android system
        Context context = ApplicationProvider.getApplicationContext();
        
        // 2. Initialize the manager before each test
        persistenceManager = new PersistenceManager(context);
    }

    @Test
    public void testSaveAndReadAllData() {
        // 1. Arrange: Prepare test values for all 5 fields
        String testCategory = "Writing thesis";
        int testGoalAmount = 500;
        String testGoalUnit = "pages";
        String testDeadline = "24.12.2025";
        int testTimeHours = 40;

        // 2. Act: Save all data at once
        persistenceManager.saveSetupData(testCategory, testGoalAmount, testGoalUnit, testDeadline, testTimeHours);

        // 3. Assert: Check if every single value was saved correctly
        Assert.assertEquals("Category should match", testCategory, persistenceManager.getCategory());
        Assert.assertEquals("Goal amount should match", testGoalAmount, persistenceManager.getGoalAmount());
        Assert.assertEquals("Goal unit should match", testGoalUnit, persistenceManager.getGoalUnit());
        Assert.assertEquals("Deadline should match", testDeadline, persistenceManager.getDeadline());
        Assert.assertEquals("Time hours should match", testTimeHours, persistenceManager.getTimeHours());
    }

    @Test
    public void testSaveWithEmptyValues() {
        // 1. Arrange: Use empty/zero values
        String testCategory = "";
        int testGoalAmount = 0;
        String testGoalUnit = "";
        String testDeadline = "";
        int testTimeHours = 0;

        // 2. Act: Save the empty data
        persistenceManager.saveSetupData(testCategory, testGoalAmount, testGoalUnit, testDeadline, testTimeHours);

        // 3. Assert: Check if it still returns the empty/default values without crashing
        Assert.assertEquals("", persistenceManager.getCategory());
        Assert.assertEquals(0, persistenceManager.getGoalAmount());
        Assert.assertEquals("", persistenceManager.getGoalUnit());
        Assert.assertEquals("", persistenceManager.getDeadline());
        Assert.assertEquals(0, persistenceManager.getTimeHours());
    }
}
