package de.iu.zanshintracker.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    private TextView tvDashboardGoal;
    private TextView tvDashboardCountdown;

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

        // Setup for the full screen view (Consistent with MainActivity)
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

        // 2. Load and display the mission data
        loadDashboardData();

        // 3. Button Click Listener for the progress button
        btnAddTargetProgress.setOnClickListener(v -> showAddProgressDialog());
    }

    /**
     * Finds all UI elements from the XML layout and links them to Java variables.
     */
    private void initializeViews() {
        tvDashboardTitle = findViewById(R.id.tvDashboardTitle);
        tvDashboardGoal = findViewById(R.id.tvDashboardGoal);
        tvDashboardCountdown = findViewById(R.id.tvDashboardCountdown);

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
     * Loads the stored setup data and calculates the countdown.
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

        // 4. Progress Logic
        int currentTargetProgress = persistenceManager.getCurrentTargetProgress();
        int currentTimeProgress = 0;

        // Target (Amount) Logic
        if (goalAmount > 0) {
            llTargetSection.setVisibility(View.VISIBLE);

            String targetLabel = getString(R.string.dashboard_tv_progress_target_label, goalUnit);
            tvProgressTargetLabel.setText(targetLabel);

            pbTarget.setMax(goalAmount);
            pbTarget.setProgress(currentTargetProgress);

            String targetNumbers = getString(R.string.dashboard_tv_progress_numbers, currentTargetProgress, goalAmount);
            tvProgressTargetNumbers.setText(targetNumbers);
        } else {
            llTargetSection.setVisibility(View.GONE);
        }

        // Time (Hours) Logic
        if (timeHours > 0) {
            llTimeSection.setVisibility(View.VISIBLE);

            pbTime.setMax(timeHours);
            pbTime.setProgress(currentTimeProgress);

            String timeNumbers = getString(R.string.dashboard_tv_progress_numbers, currentTimeProgress, timeHours);
            tvProgressTimeNumbers.setText(timeNumbers);
        } else {
            llTimeSection.setVisibility(View.GONE);
        }
    }


    /**
     * Calculates the days left until the deadline and updates the UI.
     *
     * @param deadlineStr The target date string.
     */
    private void calculateCountdown(String deadlineStr) {
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d.M.yyyy");
            LocalDate deadlineDate = LocalDate.parse(deadlineStr, dtf);
            LocalDate today = LocalDate.now();

            long daysLeft = ChronoUnit.DAYS.between(today, deadlineDate);

            if (daysLeft >= 0) {
                String daysLeftText = getString(R.string.dashboard_tv_days_left, (int) daysLeft);
                tvDashboardCountdown.setText(daysLeftText);
            } else {
                tvDashboardCountdown.setText(R.string.dashboard_tv_deadline_reached);
            }
        } catch (Exception e) {
            tvDashboardCountdown.setText(R.string.dashboard_tv_deadline_unknown);
        }
    }

    /**
     * Shows a dialog allowing the user to enter new progress manually.
     */
    private void showAddProgressDialog() {
        final EditText etInput = new EditText(this);
        etInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        etInput.setHint(R.string.dashboard_btn_alert_hint);

        new AlertDialog.Builder(this).setTitle(R.string.dashboard_btn_alert_title).setMessage(R.string.dashboard_btn_alert_message).setView(etInput).setPositiveButton(R.string.dashboard_btn_alert_add, (dialog, which) -> {
            String inputStr = etInput.getText().toString();
            if (!inputStr.isEmpty()) {
                int addedAmount = Integer.parseInt(inputStr);

                // Save the new progress
                persistenceManager.addTargetProgress(addedAmount);

                // Reload the whole dashboard to instantly update bars and text!
                loadDashboardData();
            }
        }).setNegativeButton(R.string.dashboard_btn_alert_cancel, null).show();
    }
}
