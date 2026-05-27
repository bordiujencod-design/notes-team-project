package com.example.notesteamproject

import android.app.Activity
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.DateFormat
import java.util.Date
import java.util.UUID

class MainActivity : Activity() {
    private val notes = mutableListOf<Note>()
    private lateinit var notesContainer: LinearLayout
    private lateinit var titleInput: EditText
    private lateinit var bodyInput: EditText
    private lateinit var sortSpinner: Spinner
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = getSharedPreferences(STORAGE_NAME, MODE_PRIVATE)
        loadNotes()
        setContentView(R.layout.activity_main)
        bindViews()
        renderNotes()
    }

    private fun bindViews() {
        titleInput = findViewById(R.id.titleInput)
        bodyInput = findViewById(R.id.bodyInput)
        sortSpinner = findViewById(R.id.sortSpinner)
        notesContainer = findViewById(R.id.notesContainer)

        val addButton = findViewById<Button>(R.id.addButton)
        addButton.setOnClickListener { addNote() }
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                renderNotes()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                renderNotes()
            }
        }
    }

    private fun addNote() {
        val title = titleInput.text.toString().trim()
        val body = bodyInput.text.toString().trim()

        if (title.isEmpty() || body.isEmpty()) {
            Toast.makeText(this, "Completeaza titlul si continutul.", Toast.LENGTH_SHORT).show()
            return
        }

        notes.add(Note(UUID.randomUUID().toString(), title, body, System.currentTimeMillis()))
        saveNotes()
        titleInput.setText("")
        bodyInput.setText("")
        renderNotes()
    }

    private fun renderNotes() {
        notesContainer.removeAllViews()
        val sorted = sortedNotes()

        if (sorted.isEmpty()) {
            val empty = TextView(this)
            empty.text = "Nu exista notite inca."
            empty.gravity = Gravity.CENTER
            empty.setTextColor(Color.rgb(102, 112, 133))
            empty.setPadding(dp(16), dp(24), dp(16), dp(24))
            notesContainer.addView(empty)
            return
        }

        val dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)

        for (note in sorted) {
            val card = LinearLayout(this)
            card.orientation = LinearLayout.VERTICAL
            card.setPadding(dp(14), dp(12), dp(14), dp(12))
            card.setBackgroundResource(R.drawable.note_card_background)

            val cardParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            cardParams.setMargins(0, 0, 0, dp(12))

            val noteTitle = TextView(this)
            noteTitle.text = note.title
            noteTitle.textSize = 18f
            noteTitle.setTypeface(null, 1)
            noteTitle.setTextColor(Color.rgb(31, 41, 55))
            card.addView(noteTitle)

            val date = TextView(this)
            date.text = dateFormat.format(Date(note.createdAt))
            date.setTextColor(Color.rgb(102, 112, 133))
            date.setPadding(0, dp(4), 0, dp(8))
            card.addView(date)

            val body = TextView(this)
            body.text = note.body
            body.textSize = 15f
            body.setTextColor(Color.rgb(52, 64, 84))
            card.addView(body)

            val deleteButton = Button(this)
            deleteButton.text = "Sterge"
            deleteButton.isAllCaps = false
            deleteButton.setOnClickListener {
                notes.remove(note)
                saveNotes()
                renderNotes()
            }
            card.addView(deleteButton)

            notesContainer.addView(card, cardParams)
        }
    }

    private fun sortedNotes(): List<Note> {
        val sorted = notes.toMutableList()

        when (sortSpinner.selectedItemPosition) {
            1 -> sorted.sortBy { it.createdAt }
            2 -> sorted.sortBy { it.title.lowercase() }
            else -> sorted.sortByDescending { it.createdAt }
        }

        return sorted
    }

    private fun loadNotes() {
        notes.clear()
        val rawNotes = preferences.getString(NOTES_KEY, "[]") ?: "[]"

        try {
            val array = JSONArray(rawNotes)
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                notes.add(
                    Note(
                        item.getString("id"),
                        item.getString("title"),
                        item.getString("body"),
                        item.getLong("createdAt")
                    )
                )
            }
        } catch (error: JSONException) {
            preferences.edit().remove(NOTES_KEY).apply()
        }
    }

    private fun saveNotes() {
        val array = JSONArray()

        for (note in notes) {
            val item = JSONObject()
            try {
                item.put("id", note.id)
                item.put("title", note.title)
                item.put("body", note.body)
                item.put("createdAt", note.createdAt)
                array.put(item)
            } catch (ignored: JSONException) {
                // JSONObject accepts these simple values.
            }
        }

        preferences.edit().putString(NOTES_KEY, array.toString()).apply()
    }

    private fun dp(value: Int): Int {
        return Math.round(value * resources.displayMetrics.density)
    }

    private data class Note(
        val id: String,
        val title: String,
        val body: String,
        val createdAt: Long
    )

    private companion object {
        const val STORAGE_NAME = "notes_storage"
        const val NOTES_KEY = "notes"
    }
}
