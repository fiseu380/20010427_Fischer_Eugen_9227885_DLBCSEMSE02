package de.iu.zanshintracker.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.iu.zanshintracker.R;
import de.iu.zanshintracker.model.ProgressEntry;

/**
 * Adapter for the RecyclerView to display the progress history.
 * Manages the list of ProgressEntry items and binds them to the UI.
 */
public class ProgressEntryAdapter extends RecyclerView.Adapter<ProgressEntryAdapter.ProgressEntryViewHolder> {

    // ===========================================================
    // 1. INTERFACES
    // ===========================================================

    // ===========================================================
    // 2. DATA
    // ===========================================================
    private final List<ProgressEntry> entries;
    private final OnItemDeleteListener deleteListener;
    private final SimpleDateFormat dateFormat;

    /**
     * Constructor for the ProgressEntryAdapter.
     *
     * @param entries        The list of progress entries to display.
     * @param deleteListener The listener for delete actions.
     */
    public ProgressEntryAdapter(List<ProgressEntry> entries, OnItemDeleteListener deleteListener) {
        // 1. Initialize variables
        this.entries = entries;
        this.deleteListener = deleteListener;

        // 2. Setup date format
        this.dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
    }

    // ===========================================================
    // 3. CONSTRUCTOR
    // ===========================================================

    @NonNull
    @Override
    public ProgressEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 1. Load the XML layout for a single list item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_entry, parent, false);

        // 2. Return the new ViewHolder
        return new ProgressEntryViewHolder(view, deleteListener);
    }

    // ===========================================================
    // 3. METHODS
    // ===========================================================

    @Override
    public void onBindViewHolder(@NonNull ProgressEntryViewHolder holder, int position) {
        // 1. Get the current entry
        ProgressEntry entry = entries.get(position);

        // 2. Format the date
        String dateString = dateFormat.format(new Date(entry.getTimestamp()));

        // 3. Update UI elements
        holder.tvDate.setText(dateString);
        holder.tvAmount.setText("+" + entry.getAmount());
    }

    @Override
    public int getItemCount() {
        // 1. Return the total number of entries
        return entries.size();
    }

    /**
     * Interface to handle delete button clicks in the Activity.
     */
    public interface OnItemDeleteListener {
        void onDeleteClick(int position);
    }

    // ===========================================================
    // 4. VIEWHOLDER CLASS
    // ===========================================================

    /**
     * ViewHolder class that holds the UI elements for a single list item.
     */
    public static class ProgressEntryViewHolder extends RecyclerView.ViewHolder {

        // ===========================================================
        // 1. UI COMPONENTS
        // ===========================================================
        private TextView tvDate;
        private TextView tvAmount;
        private ImageButton btnDelete;

        public ProgressEntryViewHolder(@NonNull View itemView, OnItemDeleteListener listener) {
            super(itemView);

            // 1. Initialize UI components
            initializeViews(itemView);

            // 2. Set up Click Listeners
            btnDelete.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(getAdapterPosition());
                }
            });
        }

        /**
         * Finds all UI elements from the XML layout and links them to Java variables.
         */
        private void initializeViews(View itemView) {
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
            tvAmount = itemView.findViewById(R.id.tvHistoryAmount);
            btnDelete = itemView.findViewById(R.id.btnHistoryDelete);
        }
    }
}