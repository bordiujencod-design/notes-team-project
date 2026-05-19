package com.example.notesteamproject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainActivity extends Activity {
    private static final String STORAGE_NAME = "notes_storage";
    private static final String NOTES_KEY = "notes";

    private final List<Note> notes = new ArrayList<>();
    private LinearLayout notesContainer;
    private EditText titleInput;
    private EditText bodyInput;
    private Spinner sortSpinner;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(STORAGE_NAME, MODE_PRIVATE);
        loadNotes();
        setContentView(R.layout.activity_main);
        bindViews();
        renderNotes();
    }

    private void bindViews() {
        titleInput = findViewById(R.id.titleInput);
        bodyInput = findViewById(R.id.bodyInput);
        sortSpinner = findViewById(R.id.sortSpinner);
        notesContainer = findViewById(R.id.notesContainer);

        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(view -> addNote());
        sortSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                renderNotes();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                renderNotes();
            }
        });
    }

    private void addNote() {
        String title = titleInput.getText().toString().trim();
        String body = bodyInput.getText().toString().trim();

        if (title.isEmpty() || body.isEmpty()) {
            Toast.makeText(this, "Completeaza titlul si continutul.", Toast.LENGTH_SHORT).show();
            return;
        }

        notes.add(new Note(UUID.randomUUID().toString(), title, body, System.currentTimeMillis()));
        saveNotes();
        titleInput.setText("");
        bodyInput.setText("");
        renderNotes();
    }

    private void renderNotes() {
        if (notesContainer == null) {
            return;
        }

        notesContainer.removeAllViews();
        List<Note> sorted = sortedNotes();

        if (sorted.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Nu exista notite inca.");
            empty.setGravity(Gravity.CENTER);
            empty.setTextColor(Color.rgb(102, 112, 133));
            empty.setPadding(dp(16), dp(24), dp(16), dp(24));
            notesContainer.addView(empty);
            return;
        }

        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);

        for (Note note : sorted) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(14), dp(12), dp(14), dp(12));
            card.setBackgroundResource(R.drawable.note_card_background);

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, dp(12));

            TextView noteTitle = new TextView(this);
            noteTitle.setText(note.title);
            noteTitle.setTextSize(18);
            noteTitle.setTypeface(null, 1);
            noteTitle.setTextColor(Color.rgb(31, 41, 55));
            card.addView(noteTitle);

            TextView date = new TextView(this);
            date.setText(dateFormat.format(new Date(note.createdAt)));
            date.setTextColor(Color.rgb(102, 112, 133));
            date.setPadding(0, dp(4), 0, dp(8));
            card.addView(date);

            TextView body = new TextView(this);
            body.setText(note.body);
            body.setTextSize(15);
            body.setTextColor(Color.rgb(52, 64, 84));
            card.addView(body);

            Button deleteButton = new Button(this);
            deleteButton.setText("Sterge");
            deleteButton.setAllCaps(false);
            deleteButton.setOnClickListener(view -> {
                notes.remove(note);
                saveNotes();
                renderNotes();
            });
            card.addView(deleteButton);

            notesContainer.addView(card, cardParams);
        }
    }

    private List<Note> sortedNotes() {
        List<Note> sorted = new ArrayList<>(notes);
        int sortType = sortSpinner == null ? 0 : sortSpinner.getSelectedItemPosition();

        if (sortType == 1) {
            Collections.sort(sorted, Comparator.comparingLong(note -> note.createdAt));
        } else if (sortType == 2) {
            Collections.sort(sorted, (a, b) -> a.title.compareToIgnoreCase(b.title));
        } else {
            Collections.sort(sorted, (a, b) -> Long.compare(b.createdAt, a.createdAt));
        }

        return sorted;
    }

    private void loadNotes() {
        notes.clear();
        String rawNotes = preferences.getString(NOTES_KEY, "[]");

        try {
            JSONArray array = new JSONArray(rawNotes);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                notes.add(new Note(
                        item.getString("id"),
                        item.getString("title"),
                        item.getString("body"),
                        item.getLong("createdAt")
                ));
            }
        } catch (JSONException error) {
            preferences.edit().remove(NOTES_KEY).apply();
        }
    }

    private void saveNotes() {
        JSONArray array = new JSONArray();

        for (Note note : notes) {
            JSONObject item = new JSONObject();
            try {
                item.put("id", note.id);
                item.put("title", note.title);
                item.put("body", note.body);
                item.put("createdAt", note.createdAt);
                array.put(item);
            } catch (JSONException ignored) {
                // JSONObject should accept these simple values.
            }
        }

        preferences.edit().putString(NOTES_KEY, array.toString()).apply();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static class Note {
        final String id;
        final String title;
        final String body;
        final long createdAt;

        Note(String id, String title, String body, long createdAt) {
            this.id = id;
            this.title = title;
            this.body = body;
            this.createdAt = createdAt;
        }
    }
}
