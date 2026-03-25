package de.iu.zanshintracker.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import java.util.Calendar;

import de.iu.zanshintracker.R;
import de.iu.zanshintracker.data.PersistenceManager;

/**
 * Main screen of the app.
 * Handles user input, category selection, and the time calculator.
 */
public class MainActivity extends AppCompatActivity {

    // ===========================================================
    // 1. UI COMPONENTS
    // ===========================================================
    private EditText etSetupGoal;
    private EditText etSetupUnit;
    private EditText etSetupTimeHours;
    private EditText etSetupDeadline;
    private Spinner spSetupCategory;
    private TextView tvSetupHelper;
    private Button btnSetupSave;

    // ===========================================================
    // 2. DATA
    // ===========================================================
    private PersistenceManager persistenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Initialize PersistenceManager
        persistenceManager = new PersistenceManager(this);

        // 2. Automatic Routing
        if (routeToDashboardIfGoalExists()) {
            return; // Stop execution if routed
        }

        // 3. Setup full screen view
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 4. Handle system bar padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.clSetupMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 5. Initialize UI components
        initializeViews();
        setupSpinner();

        // 6. Set up Click Listeners
        etSetupDeadline.setOnClickListener(v -> showDatePicker());

        tvSetupHelper.setOnClickListener(v -> {
            String deadlineStr = etSetupDeadline.getText().toString();
            if (deadlineStr.isEmpty()) {
                Toast.makeText(this, R.string.msg_setup_deadline_missing, Toast.LENGTH_SHORT).show();
            } else {
                showCalculatorDialog(deadlineStr);
            }
        });

        btnSetupSave.setOnClickListener(v -> handleSaveAction());
    }

    /**
     * Checks if a goal exists and routes to the Dashboard.
     *
     * @return true if routed, false otherwise.
     */
    private boolean routeToDashboardIfGoalExists() {
        // 1. Check if deadline is saved
        if (!persistenceManager.getDeadline().isEmpty()) {

            // 2. Navigate to Dashboard
            Intent intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);

            // 3. Close setup
            finish();
            return true;
        }
        return false;
    }

    /**
     * Links XML layout elements to Java variables.
     */
    private void initializeViews() {
        etSetupGoal = findViewById(R.id.etSetupGoal);
        etSetupUnit = findViewById(R.id.etSetupUnit);
        etSetupTimeHours = findViewById(R.id.etSetupTimeHours);
        etSetupDeadline = findViewById(R.id.etSetupDeadline);
        spSetupCategory = findViewById(R.id.spSetupCategory);
        tvSetupHelper = findViewById(R.id.tvSetupHelper);
        btnSetupSave = findViewById(R.id.btnSetupSave);

        // Disable manual text input for the DatePicker
        etSetupDeadline.setFocusable(false);
        etSetupDeadline.setClickable(true);
    }

    /**
     * Sets up the category Spinner.
     */
    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.setup_sp_category_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSetupCategory.setAdapter(adapter);
    }

    /**
     * Shows a DatePickerDialog.
     */
    private void showDatePicker() {
        // 1. Get current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // 2. Build dialog
        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            String dateString = d + "." + (m + 1) + "." + y;
            etSetupDeadline.setText(dateString);
        }, year, month, day);

        // 3. Prevent selecting past dates
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    /**
     * Shows a dialog to calculate total hours.
     *
     * @param deadlineStr The chosen deadline.
     */
    private void showCalculatorDialog(String deadlineStr) {
        final EditText etCalcInput = new EditText(this);
        etCalcInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        etCalcInput.setHint(R.string.setup_dialog_calc_hint);

        new AlertDialog.Builder(this)
                .setTitle(R.string.setup_dialog_calc_title)
                .setMessage(R.string.setup_dialog_calc_message)
                .setView(etCalcInput)
                .setPositiveButton(R.string.setup_dialog_calc_ok, (dialog, which) -> {
                    String hoursStr = etCalcInput.getText().toString();
                    if (!hoursStr.isEmpty()) {
                        performCalculation(deadlineStr, hoursStr);
                    }
                })
                .setNegativeButton(R.string.setup_dialog_calc_cancel, null)
                .show();
    }

    /**
     * Calculates total hours and updates the UI.
     *
     * @param deadlineStr The chosen deadline.
     * @param hoursPerDayStr Daily work hours.
     */
    private void performCalculation(String deadlineStr, String hoursPerDayStr) {
        try {
            // 1. Parse dates
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d.M.yyyy");
            LocalDate deadlineDate = LocalDate.parse(deadlineStr, dtf);
            LocalDate today = LocalDate.now();

            long daysBetween = ChronoUnit.DAYS.between(today, deadlineDate);

            // 2. Calculate and update UI
            if (daysBetween >= 0) {
                long totalHours = daysBetween * Integer.parseInt(hoursPerDayStr);

                etSetupTimeHours.setText(String.valueOf(totalHours));
                Toast.makeText(this, R.string.msg_setup_calc_updated, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.msg_setup_error_calc, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Validates inputs and saves data.
     */
    private void handleSaveAction() {
        // 1. Read inputs
        String goalStr = etSetupGoal.getText().toString();
        String unitStr = etSetupUnit.getText().toString();
        String timeStr = etSetupTimeHours.getText().toString();
        String deadlineStr = etSetupDeadline.getText().toString();
        String categoryStr = spSetupCategory.getSelectedItem().toString();

        // 2. Check deadline
        if (deadlineStr.isEmpty()) {
            Toast.makeText(this, R.string.msg_setup_deadline_missing, Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Ensure at least one target is set
        if (goalStr.isEmpty() && timeStr.isEmpty()) {
            Toast.makeText(this, R.string.msg_setup_no_target, Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Parse inputs to integers
        int goal = 0;
        if (!goalStr.isEmpty()) {
            try {
                goal = Integer.parseInt(goalStr);
            } catch (NumberFormatException e) {
                etSetupGoal.setError(getString(R.string.msg_setup_error_numbers));
                return;
            }
        }

        int timeHours = 0;
        if (!timeStr.isEmpty()) {
            try {
                timeHours = Integer.parseInt(timeStr);
            } catch (NumberFormatException e) {
                etSetupTimeHours.setError(getString(R.string.msg_setup_error_numbers));
                return;
            }
        }

        // 5. Save data
        persistenceManager.saveSetupData(categoryStr, goal, unitStr, deadlineStr, timeHours);

        // 6. Navigate to Dashboard
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}