package de.iu.zanshintracker.ui;

import android.app.Activity;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import de.iu.zanshintracker.R;
import de.iu.zanshintracker.data.PersistenceManager;
import de.iu.zanshintracker.logic.TimeCalculator;

/**
 * This class handles the logic for the Pomodoro Timer Widget on the dashboard.
 * It manages the countdown, cycles, and saves the tracked time.
 * It also supports background execution via timestamp calculation.
 */
public class PomodoroWidget {

    // ===========================================================
    // 1. DEPENDENCIES & CALLBACKS
    // ===========================================================
    private final Activity activity;
    private final PersistenceManager persistenceManager;
    private final Runnable onProgressUpdated;

    // ===========================================================
    // 2. UI COMPONENTS
    // ===========================================================
    private TextView tvTimerDisplay;
    private TextView tvCycleStatus;
    private TextView tvCycleCount;
    private Button btnStart;
    private ImageButton btnRestart;
    private ImageButton btnSettings;

    private EditText etWorkTime;
    private EditText etBreakTime;
    private EditText etCycles;
    private androidx.constraintlayout.widget.Group groupTimer;
    private androidx.constraintlayout.widget.Group groupSettings;

    // ===========================================================
    // 3. TIMER STATE
    // ===========================================================
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private boolean isWorkPhase = true;
    private int currentCycle = 1;
    private int totalCycles = 1;
    private long timeLeftInMillis;
    private long workTimeInMillis;
    private long breakTimeInMillis;
    private int totalWorkedMinutesSession = 0;

    /**
     * Initializes the Pomodoro Widget logic.
     */
    public PomodoroWidget(Activity activity, View rootView, Runnable onProgressUpdated) {
        this.activity = activity;
        this.persistenceManager = new PersistenceManager(activity);
        this.onProgressUpdated = onProgressUpdated;

        // 1. Initialize logic and UI
        initializeViews(rootView);

        // 2. Set Click Listeners
        btnStart.setOnClickListener(v -> startTimerSequence());
        btnRestart.setOnClickListener(v -> stopAndSave());
    }

    private void initializeViews(View rootView) {
        // 1. Map Timer UI
        tvTimerDisplay = rootView.findViewById(R.id.tvTimerDisplay);
        tvCycleStatus = rootView.findViewById(R.id.tvCycleStatus);
        tvCycleCount = rootView.findViewById(R.id.tvCycleCount);
        btnStart = rootView.findViewById(R.id.btnPomodoroStart);
        btnRestart = rootView.findViewById(R.id.btnPomodoroRestart);
        btnSettings = rootView.findViewById(R.id.btnPomodoroSettings);

        // 2. Map Settings UI
        etWorkTime = rootView.findViewById(R.id.etWorkTime);
        etBreakTime = rootView.findViewById(R.id.etBreakTime);
        etCycles = rootView.findViewById(R.id.etCycles);
        groupTimer = rootView.findViewById(R.id.groupTimer);
        groupSettings = rootView.findViewById(R.id.groupSettings);

        // 3. Load initial timer display from persistence
        long savedWorkMillis = TimeCalculator.minutesToMillis(persistenceManager.getPomodoroDefWork());
        tvTimerDisplay.setText(TimeCalculator.formatMillisToMMSS(savedWorkMillis));

        // 4. Set Click Listener for UI toggle
        btnSettings.setOnClickListener(v -> toggleSettingsView());
    }

    /**
     * Toggles between the Timer view and the Settings view with XML Groups.
     */
    private void toggleSettingsView() {
        if (groupSettings.getVisibility() == View.GONE) {
            // 1. Show settings, hide timer
            groupTimer.setVisibility(View.GONE);
            groupSettings.setVisibility(View.VISIBLE);

            // 2. Load saved values into input fields
            etWorkTime.setText(String.valueOf(persistenceManager.getPomodoroDefWork()));
            etBreakTime.setText(String.valueOf(persistenceManager.getPomodoroDefBreak()));
            etCycles.setText(String.valueOf(persistenceManager.getPomodoroDefCycles()));
        } else {
            // 3. Prevent crash if user leaves inputs empty
            String workStr = etWorkTime.getText().toString();
            String breakStr = etBreakTime.getText().toString();
            String cyclesStr = etCycles.getText().toString();

            if (workStr.isEmpty() || breakStr.isEmpty() || cyclesStr.isEmpty()) {
                Toast.makeText(activity, R.string.msg_pomodoro_fill_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            // 4. Save inputs to database
            try {
                int work = Integer.parseInt(workStr);
                int breakTime = Integer.parseInt(breakStr);
                int cycles = Integer.parseInt(cyclesStr);
                persistenceManager.savePomodoroSettings(work, breakTime, cycles);

                // 5. Show timer, hide settings
                groupSettings.setVisibility(View.GONE);
                groupTimer.setVisibility(View.VISIBLE);

                // 6. Update visual timer if not running
                if (!isTimerRunning) {
                    long updatedTimeMillis = TimeCalculator.minutesToMillis(work);
                    tvTimerDisplay.setText(TimeCalculator.formatMillisToMMSS(updatedTimeMillis));
                }
            } catch (NumberFormatException e) {
                Toast.makeText(activity, R.string.msg_setup_error_numbers, Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Reads settings from the database and prepares the timer for the first cycle.
     */
    private void startTimerSequence() {
        // 1. Prevent multiple starts
        if (isTimerRunning) {
            return;
        }
        // 2. Read values from persistence manager
        int workMins = persistenceManager.getPomodoroDefWork();
        int breakMins = persistenceManager.getPomodoroDefBreak();
        totalCycles = persistenceManager.getPomodoroDefCycles();

        // 3. Convert to milliseconds
        workTimeInMillis = TimeCalculator.minutesToMillis(workMins);
        breakTimeInMillis = TimeCalculator.minutesToMillis(breakMins);

        // 4. Lock UI elements during run
        btnStart.setVisibility(View.INVISIBLE);
        btnSettings.setVisibility(View.INVISIBLE);

        // 5. Reset state
        currentCycle = 1;
        isWorkPhase = true;
        totalWorkedMinutesSession = 0;

        startPhase();
    }

    /**
     * Resets the UI components to their default state.
     */
    private void resetUI() {
        isTimerRunning = false;

        // 1. Reload dynamic timer display
        long savedWorkMillis = TimeCalculator.minutesToMillis(persistenceManager.getPomodoroDefWork());
        tvTimerDisplay.setText(TimeCalculator.formatMillisToMMSS(savedWorkMillis));
        tvCycleStatus.setText(R.string.pomodoro_status_ready);
        tvCycleCount.setText("");

        // 2. Reset visibility & input states
        groupSettings.setVisibility(View.GONE);
        groupTimer.setVisibility(View.VISIBLE);
        etWorkTime.setEnabled(true);
        etBreakTime.setEnabled(true);
        etCycles.setEnabled(true);

        // 3. Unlock UI elements
        btnStart.setVisibility(View.VISIBLE);
        btnSettings.setVisibility(View.VISIBLE);
    }

    /**
     * Starts the current phase (Work or Break) and initializes the CountDownTimer.
     */
    private void startPhase() {
        if (isWorkPhase) {
            timeLeftInMillis = workTimeInMillis;
            tvCycleStatus.setText(R.string.pomodoro_label_work);
            tvCycleCount.setText(activity.getString(R.string.pomodoro_cycle_work, currentCycle, totalCycles));
        } else {
            timeLeftInMillis = breakTimeInMillis;
            tvCycleStatus.setText(R.string.pomodoro_label_break);
            tvCycleCount.setText(activity.getString(R.string.pomodoro_cycle_break, currentCycle, totalCycles));
        }

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                handlePhaseCompletion();
            }
        }.start();

        isTimerRunning = true;
    }

    /**
     * Handles the logic when a timer hits 00:00.
     */
    private void handlePhaseCompletion() {
        // 1. Add completed work minutes and save
        if (isWorkPhase) {
            int workedMinutes = TimeCalculator.millisToMinutes(workTimeInMillis);
            if (workedMinutes > 0) {
                persistenceManager.addTrackedTime(workedMinutes);
                totalWorkedMinutesSession += workedMinutes;
                if (onProgressUpdated != null) {
                    onProgressUpdated.run();
                }
            }
        }

        // 2. Switch phase or finish
        if (isWorkPhase) {
            isWorkPhase = false;
            startPhase();
        } else {
            if (currentCycle < totalCycles) {
                currentCycle++;
                isWorkPhase = true;
                startPhase();
            } else {
                Toast.makeText(activity, R.string.msg_pomodoro_session_complete, Toast.LENGTH_LONG).show();
                stopAndSave();
            }
        }
    }

    /**
     * Formats the remaining milliseconds into MM:SS.
     */
    private void updateTimerText() {
        String formattedTime = TimeCalculator.formatMillisToMMSS(timeLeftInMillis);
        tvTimerDisplay.setText(formattedTime);
    }

    /**
     * Cancels the timer, calculates partial minutes, saves the progress, and resets the UI.
     */
    private void stopAndSave() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        // 1. Saves minutes if stopped
        if (isWorkPhase && isTimerRunning) {
            long timeWorkedSoFar = workTimeInMillis - timeLeftInMillis;
            int partialMinutes = TimeCalculator.millisToMinutes(timeWorkedSoFar);

            if (partialMinutes > 0) {
                persistenceManager.addTrackedTime(partialMinutes);
                totalWorkedMinutesSession += partialMinutes;
                if (onProgressUpdated != null) {
                    onProgressUpdated.run();
                }
            }
        }

        // 2. Show summary toast
        if (totalWorkedMinutesSession > 0) {
            String successMsg = activity.getString(R.string.msg_pomodoro_tracked, totalWorkedMinutesSession);
            Toast.makeText(activity, successMsg, Toast.LENGTH_SHORT).show();
        }

        resetUI();
    }


    // ===========================================================
    // 4. LIFECYCLE MANAGEMENT (Background Timer)
    // ===========================================================

    /**
     * Called when the app goes into the background.
     * Saves the expected end time to resume later.
     */
    public void onPause() {
        if (isTimerRunning) {
            long endTime = System.currentTimeMillis() + timeLeftInMillis;
            persistenceManager.savePomodoroState(true, endTime, isWorkPhase, currentCycle,
                    totalCycles, workTimeInMillis, breakTimeInMillis, totalWorkedMinutesSession);
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
        } else {
            persistenceManager.savePomodoroState(false, 0, true, 1, 1, 0, 0, 0);
        }
    }

    /**
     * Called when the app returns to the foreground.
     * Calculates time passed and resumes the timer.
     */
    public void onResume() {
        if (persistenceManager.isPomodoroRunning()) {
            // 1. Get current time and expected end time
            long endTime = persistenceManager.getPomodoroEndTime();
            long now = System.currentTimeMillis();

            // 2. Restore state
            isWorkPhase = persistenceManager.isPomodoroWorkPhase();
            currentCycle = persistenceManager.getPomodoroCurrentCycle();
            totalCycles = persistenceManager.getPomodoroTotalCycles();
            workTimeInMillis = persistenceManager.getPomodoroWorkTimeMs();
            breakTimeInMillis = persistenceManager.getPomodoroBreakTimeMs();
            totalWorkedMinutesSession = persistenceManager.getPomodoroWorkedMinutes();

            // 3. Lock UI and makes sure that correct view is active
            groupSettings.setVisibility(View.GONE);
            groupTimer.setVisibility(View.VISIBLE);
            etWorkTime.setEnabled(false);
            etBreakTime.setEnabled(false);
            etCycles.setEnabled(false);
            btnStart.setVisibility(View.INVISIBLE);
            btnSettings.setVisibility(View.INVISIBLE);


            if (now < endTime) {
                // Resume running timer
                timeLeftInMillis = endTime - now;

                if (isWorkPhase) {
                    tvCycleStatus.setText(R.string.pomodoro_label_work);
                    tvCycleCount.setText(activity.getString(R.string.pomodoro_cycle_work, currentCycle, totalCycles));
                } else {
                    tvCycleStatus.setText(R.string.pomodoro_label_break);
                    tvCycleCount.setText(activity.getString(R.string.pomodoro_cycle_break, currentCycle, totalCycles));
                }

                countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        timeLeftInMillis = millisUntilFinished;
                        updateTimerText();
                    }

                    @Override
                    public void onFinish() {
                        handlePhaseCompletion();
                    }
                }.start();
                isTimerRunning = true;
            } else {
                // Timer finished in background
                timeLeftInMillis = 0;
                isTimerRunning = true;
                handlePhaseCompletion();
            }
        }
    }
}