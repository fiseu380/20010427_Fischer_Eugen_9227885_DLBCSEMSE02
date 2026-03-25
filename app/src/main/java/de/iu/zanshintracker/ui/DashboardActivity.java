package de.iu.zanshintracker.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import de.iu.zanshintracker.R;
import de.iu.zanshintracker.data.PersistenceManager;

/**
 * This class handles the dashboard screen.
 * It shows the current mission and calculates the remaining days.
 */
public class DashboardActivity extends AppCompatActivity {

    // ===========================================================
    // 1. UI COMPONENTS
    // ===========================================================
    private TextView tvDashboardTitle;
    private Button btnDashboardNewGoal;
    private TextView tvDashboardGoal;
    private TextView tvDashboardCountdown;
    private int defaultTextColor;

    private LinearLayout llTargetSection;
    private LinearLayout llTimeSection;
    private ProgressBar pbTarget;
    private ProgressBar pbTime;
    private TextView tvProgressTargetLabel;
    private TextView tvProgressTargetNumbers;
    private TextView tvProgressTimeNumbers;
    private Button btnAddTargetProgress;

    // ===========================================================
    // 2. DATA
    // ===========================================================
    private PersistenceManager persistenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup for the full screen view
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // Handle the padding for the system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.clDashboardMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Initialize logic and UI
        persistenceManager = new PersistenceManager(this);
        initializeViews();

        // 2. Load and display goal data
        loadDashboardData();

        // 3. Button Click Listener for the progress button
        btnAddTargetProgress.setOnClickListener(v -> showAddProgressDialog());

        // 4. Button Click Listener to reset and start a new goal
        btnDashboardNewGoal.setOnClickListener(v -> handleNewGoalAction());
    }

    /**
     * Finds all UI elements from the XML layout and links them to Java variables.
     */
    private void initializeViews() {
        // 1. Header Section
        tvDashboardTitle = findViewById(R.id.tvDashboardTitle);
        tvDashboardGoal = findViewById(R.id.tvDashboardGoal);
        tvDashboardCountdown = findViewById(R.id.tvDashboardCountdown);
        btnDashboardNewGoal = findViewById(R.id.btnDashboardNewGoal);

        // Save the default system text color for the countdown alarm logic
        defaultTextColor = tvDashboardCountdown.getCurrentTextColor();

        // 2. Progress Section
        llTargetSection = findViewById(R.id.llTargetSection);
        llTimeSection = findViewById(R.id.llTimeSection);
        pbTarget = findViewById(R.id.pbTarget);
        pbTime = findViewById(R.id.pbTime);
        tvProgressTargetLabel = findViewById(R.id.tvProgressTargetLabel);
        tvProgressTargetNumbers = findViewById(R.id.tvProgressTargetNumbers);
        tvProgressTimeNumbers = findViewById(R.id.tvProgressTimeNumbers);
        btnAddTargetProgress = findViewById(R.id.btnAddTargetProgress);
    }

    /**
     * * Loads the stored setup data, updates text fields, and configures the progress bars.
     */
    private void loadDashboardData() {
        // 1. Get data from the persistence layer
        String category = persistenceManager.getCategory();
        String deadlineStr = persistenceManager.getDeadline();
        int goalAmount = persistenceManager.getGoalAmount();
        String goalUnit = persistenceManager.getGoalUnit();
        int timeHours = persistenceManager.getTimeHours();

        // 2. Update the Goal text
        String goalText = getString(R.string.dashboard_tv_goal_label, category);
        tvDashboardGoal.setText(goalText);

        // 3. Calculate and set the countdown
        calculateCountdown(deadlineStr);

        // 4. Retrieve current progress state
        int currentTargetProgress = persistenceManager.getCurrentTargetProgress();
        int currentTimeProgress = 0; // Placeholder for future time tracking implementation

        // 5. Setup Target (Amount) UI Logic
        if (goalAmount > 0) {
            llTargetSection.setVisibility(View.VISIBLE);

            String targetLabel = getString(R.string.dashboard_tv_progress_target_label, goalUnit);
            tvProgressTargetLabel.setText(targetLabel);

            pbTarget.setMax(goalAmount);
            pbTarget.setProgress(currentTargetProgress);

            String targetNumbers = getString(R.string.dashboard_tv_progress_numbers, currentTargetProgress, goalAmount);
            tvProgressTargetNumbers.setText(targetNumbers);
        } else {
            // Hide if no target amount
            llTargetSection.setVisibility(View.GONE);
        }

        // 6. Setup Time (Hours) UI Logic
        if (timeHours > 0) {
            llTimeSection.setVisibility(View.VISIBLE);

            pbTime.setMax(timeHours);
            pbTime.setProgress(currentTimeProgress);

            String timeNumbers = getString(R.string.dashboard_tv_progress_numbers, currentTimeProgress, timeHours);
            tvProgressTimeNumbers.setText(timeNumbers);
        } else {
            // Hide if no time was set
            llTimeSection.setVisibility(View.GONE);
        }
    }
    /**
     * Calculates the days left until the deadline and updates the UI with visual alarm.
     * @param deadlineStr The target date string.
     */
    private void calculateCountdown(String deadlineStr) {
        try {
            // 1. Parse dates and calculate the difference in days
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d.M.yyyy");
            LocalDate deadlineDate = LocalDate.parse(deadlineStr, dtf);
            LocalDate today = LocalDate.now();

            long daysLeft = ChronoUnit.DAYS.between(today, deadlineDate);

            // 2. Update UI based on remaining time
            if (daysLeft >= 0) {
                String daysLeftText = getString(R.string.dashboard_tv_days_left, (int) daysLeft);
                tvDashboardCountdown.setText(daysLeftText);

                // 3. Visual Alarm: Red if 3 days or less, default system color otherwise
                if (daysLeft <= 3) {
                    tvDashboardCountdown.setTextColor(Color.RED);
                } else {
                    tvDashboardCountdown.setTextColor(defaultTextColor);
                }
            } else {
                tvDashboardCountdown.setText(R.string.dashboard_tv_deadline_reached);
                // Keeps red if deadline is reached
                tvDashboardCountdown.setTextColor(Color.RED);
            }
        } catch (Exception e) {
            // Fallback if date parsing fails
            tvDashboardCountdown.setText(R.string.dashboard_tv_deadline_unknown);
            tvDashboardCountdown.setTextColor(defaultTextColor);
        }
    }

    /**
     * Shows a dialog allowing the user to enter new progress manually.
     */
    private void showAddProgressDialog() {
        // 1. Create input field for the dialog
        final EditText etInput = new EditText(this);
        etInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        etInput.setHint(R.string.dashboard_dialog_progress_hint);

        // 2. Build and show the dialog
        new AlertDialog.Builder(this)
                .setTitle(R.string.dashboard_dialog_progress_title)
                .setMessage(R.string.dashboard_dialog_progress_message)
                .setView(etInput)
                .setPositiveButton(R.string.dashboard_dialog_progress_add, (dialog, which) -> {
            String inputStr = etInput.getText().toString();

            // 3. Handle input
            if (!inputStr.isEmpty()) {
                int addedAmount = Integer.parseInt(inputStr);

                // Save the new progress
                persistenceManager.addTargetProgress(addedAmount);

                // Reload dashboard to update UI
                loadDashboardData();
                //Feedback for progress
                Toast.makeText(DashboardActivity.this, R.string.msg_dashboard_progress_added, Toast.LENGTH_SHORT).show();
            } else {
                //Feedback for empty progress
                Toast.makeText(DashboardActivity.this, R.string.msg_dashboard_progress_empty, Toast.LENGTH_SHORT).show();
            }
        })
                .setNegativeButton(R.string.dashboard_dialog_progress_cancel, null)
                .show();
    }

    /**
     * shows a confirmation dialog to reset current progress and start a new goal.
     */
    private void handleNewGoalAction() {
        new AlertDialog.Builder(this).setTitle(R.string.dashboard_dialog_reset_title).setMessage(R.string.dashboard_dialog_reset_message).setPositiveButton(R.string.dashboard_dialog_reset_confirm, (dialog, which) -> {
            // 1. Clear saved data
            persistenceManager.clearSetupData();

            // 2. Feedback for deletion
            Toast.makeText(this, R.string.msg_dashboard_goal_deleted, Toast.LENGTH_SHORT).show();


            // 3. Navigate to MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

            // 4. Close Dashboard
            finish();
        }).setNegativeButton(R.string.dashboard_dialog_reset_cancel, null).show();
    }
}
