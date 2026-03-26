package de.iu.zanshintracker.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.iu.zanshintracker.R;
import de.iu.zanshintracker.data.PersistenceManager;
import de.iu.zanshintracker.model.ProgressEntry;


/**
 * This class handles the history screen.
 * It shows the user's progress history and allows deleting entries.
 */
public class HistoryActivity extends AppCompatActivity {

    // ===========================================================
    // 1. UI COMPONENTS
    // ===========================================================
    private RecyclerView rvHistoryList;
    private Button btnHistoryBack;

    // ===========================================================
    // 2. DATA
    // ===========================================================
    private PersistenceManager persistenceManager;
    private ProgressEntryAdapter adapter;
    private List<ProgressEntry> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Initialize PersistenceManager
        persistenceManager = new PersistenceManager(this);

        // 2. Setup full screen view
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);

        // 3. Handle system bar padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.llHistoryMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 4. Initialize UI components
        initializeViews();

        // 5. Load data and setup the list
        setupRecyclerView();

        // 6. Set up Click Listeners
        btnHistoryBack.setOnClickListener(v -> finish());
    }

    /**
     * Finds all UI elements from the XML layout and links them to Java variables.
     */
    private void initializeViews() {
        rvHistoryList = findViewById(R.id.rvHistoryList);
        btnHistoryBack = findViewById(R.id.btnHistoryBack);
    }

    /**
     * Loads the history data and connects it to the RecyclerView using the adapter.
     */
    private void setupRecyclerView() {
        // 1. Get the current list of entries from storage
        historyList = persistenceManager.getHistory();

        // 2. Create the adapter and define what happens when the delete icon is clicked
        adapter = new ProgressEntryAdapter(historyList, position -> deleteEntry(position));

        // 3. Configure the RecyclerView
        rvHistoryList.setLayoutManager(new LinearLayoutManager(this));
        rvHistoryList.setAdapter(adapter);
    }

    /**
     * Deletes an entry from storage, removes it from the list, and updates the UI.
     *
     * @param position The index of the clicked item in the list.
     */
    private void deleteEntry(int position) {
        // 1. Delete from persistent storage
        persistenceManager.deleteProgressEntry(position);

        // 2. Remove from the local data list
        historyList.remove(position);

        // 3. Tell the adapter to visually remove the item
        adapter.notifyItemRemoved(position);

        // 4. Update the remaining items
        adapter.notifyItemRangeChanged(position, historyList.size());

        // 5. Feedback for deletion
        Toast.makeText(this, R.string.msg_history_entry_deleted, Toast.LENGTH_SHORT).show();
    }
}