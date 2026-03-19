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
 * This class is the main screen of the app.
 * It handles the input fields, categories via Spinner, and a helper calculator.
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

        // Setup for the full screen view
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Handle the padding for the system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.clSetupMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            int paddingLeft = v.getPaddingLeft();
            int paddingTop = systemBars.top + v.getPaddingTop();
            int paddingRight = v.getPaddingRight();
            int paddingBottom = systemBars.bottom + v.getPaddingBottom();

            v.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

            return insets;
        });

        // 1. Initialize variables and UI components
        persistenceManager = new PersistenceManager(this);
        initializeViews();
        setupSpinner();

        // 2. Click-Listener for the deadline field (DatePicker)
        etSetupDeadline.setOnClickListener(v -> showDatePicker());

        // 3. Click-Listener for the helper text (Time Calculator)
        tvSetupHelper.setOnClickListener(v -> {
            String deadlineStr = etSetupDeadline.getText().toString();
            if (deadlineStr.isEmpty()) {
                Toast.makeText(this, R.string.msg_setup_deadline_missing, Toast.LENGTH_SHORT).show();
            } else {
                showCalculatorDialog(deadlineStr);
            }
        });

        // 4. Click-Listener for the save button
        btnSetupSave.setOnClickListener(v -> handleSaveAction());
    }

    /**
     * Finds all UI elements from the XML layout and links them to Java variables.
     */
    private void initializeViews() {
        etSetupGoal = findViewById(R.id.etSetupGoal);
        etSetupUnit = findViewById(R.id.etSetupUnit);
        etSetupTimeHours = findViewById(R.id.etSetupTimeHours);
        etSetupDeadline = findViewById(R.id.etSetupDeadline);
        spSetupCategory = findViewById(R.id.spSetupCategory);
        tvSetupHelper = findViewById(R.id.tvSetupHelper);
        btnSetupSave = findViewById(R.id.btnSetupSave);

        // Ensure etSetupDeadline is not focusable manually to protect the UX
        etSetupDeadline.setFocusable(false);
        etSetupDeadline.setClickable(true);
    }

    /**
     * Sets up the Spinner with options from the strings.xml array.
     */
    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.setup_sp_category_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSetupCategory.setAdapter(adapter);
    }

    /**
     * Configures and shows the DatePickerDialog with a minDate restriction.
     */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            String dateString = d + "." + (m + 1) + "." + y;
            etSetupDeadline.setText(dateString);
        }, year, month, day);

        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    /**
     * Shows a dialog to ask for daily work hours and calculates the total.
     */
    private void showCalculatorDialog(String deadlineStr) {
        final EditText etCalcInput = new EditText(this);
        etCalcInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        etCalcInput.setHint(R.string.calc_et_hint_hours);

        new AlertDialog.Builder(this).setTitle(R.string.calc_alert_title).setMessage(R.string.calc_alert_message).setView(etCalcInput).setPositiveButton(R.string.calc_btn_ok, (dialog, which) -> {
            String hoursStr = etCalcInput.getText().toString();
            if (!hoursStr.isEmpty()) {
                performCalculation(deadlineStr, hoursStr);
            }
        }).setNegativeButton(R.string.calc_btn_cancel, null).show();
    }

    /**
     * Calculates the total hours and updates fields with a feedback Toast.
     */
    private void performCalculation(String deadlineStr, String hoursPerDayStr) {
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d.M.yyyy");
            LocalDate deadlineDate = LocalDate.parse(deadlineStr, dtf);
            LocalDate today = LocalDate.now();

            long daysBetween = ChronoUnit.DAYS.between(today, deadlineDate);

            if (daysBetween >= 0) {
                long totalHours = daysBetween * Integer.parseInt(hoursPerDayStr);

                // For this logic, we fill the TimeHours field automatically
                etSetupTimeHours.setText(String.valueOf(totalHours));
                Toast.makeText(this, R.string.msg_setup_calc_updated, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.msg_setup_error_calc, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Reads all user inputs and prepares them for saving.
     */
    private void handleSaveAction() {
        // 1. Get input strings
        String goalStr = etSetupGoal.getText().toString();
        String unitStr = etSetupUnit.getText().toString();
        String timeStr = etSetupTimeHours.getText().toString();
        String deadlineStr = etSetupDeadline.getText().toString();
        String categoryStr = spSetupCategory.getSelectedItem().toString();

        // 2. Validation

        // Check if deadline is chosen
        if (deadlineStr.isEmpty()) {
            Toast.makeText(this, R.string.msg_setup_deadline_missing, Toast.LENGTH_SHORT).show();
            return;
        }

        // Main Rule: At least one target must be set
        if (goalStr.isEmpty() && timeStr.isEmpty()) {
            Toast.makeText(this, R.string.msg_setup_no_target, Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Prepare values for logic
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

        // 4. Save setup data via PersistenceManager
        persistenceManager.saveSetupData(categoryStr, goal, unitStr, deadlineStr, timeHours);

        // 5. Navigate to Dashboard and close setup
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}
