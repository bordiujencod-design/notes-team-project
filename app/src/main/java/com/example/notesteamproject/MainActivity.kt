package com.example.notesteamproject

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.DateFormat
import java.util.Date
import java.util.UUID

class MainActivity : Activity() {
    private val notes = mutableListOf<Note>()
    private val prefs by lazy { getSharedPreferences("notes_storage", MODE_PRIVATE) }
    private lateinit var titleInput: EditText
    private lateinit var bodyInput: EditText
    private lateinit var sortSpinner: Spinner
    private lateinit var notesContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        titleInput = findViewById(R.id.titleInput)
        bodyInput = findViewById(R.id.bodyInput)
        sortSpinner = findViewById(R.id.sortSpinner)
        notesContainer = findViewById(R.id.notesContainer)
        findViewById<Button>(R.id.addButton).setOnClickListener { addNote() }
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) = renderNotes()
            override fun onNothingSelected(p: AdapterView<*>?) = renderNotes()
        }
        loadNotes()
        renderNotes()
    }

    private fun addNote() {
        val title = titleInput.text.toString().trim()
        val body = bodyInput.text.toString().trim()
        if (title.isEmpty() || body.isEmpty()) {
            Toast.makeText(this, "Completeaza titlul si continutul.", Toast.LENGTH_SHORT).show()
            return
        }
        notes.add(Note(UUID.randomUUID().toString(), title, body, System.currentTimeMillis()))
        titleInput.setText("")
        bodyInput.setText("")
        saveNotes()
        renderNotes()
    }

    private fun renderNotes() {
        notesContainer.removeAllViews()
        val list = when (sortSpinner.selectedItemPosition) {
            1 -> notes.sortedBy { it.createdAt }
            2 -> notes.sortedBy { it.title.lowercase() }
            else -> notes.sortedByDescending { it.createdAt }
        }
        if (list.isEmpty()) return notesContainer.addView(text("Nu exista notite inca.", 15f, Gravity.CENTER))
        val formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
        list.forEach { note ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(14), dp(12), dp(14), dp(12))
                setBackgroundResource(R.drawable.note_card_background)
            }
            card.addView(text(note.title, 18f).apply { setTypeface(null, 1) })
            card.addView(text(formatter.format(Date(note.createdAt)), 13f).apply { setTextColor(Color.rgb(102, 112, 133)) })
            card.addView(text(note.body, 15f))
            card.addView(Button(this).apply {
                text = "Sterge"
                isAllCaps = false
                setOnClickListener { notes.remove(note); saveNotes(); renderNotes() }
            })
            notesContainer.addView(card, LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, 0, 0, dp(12)) })
        }
    }

    private fun text(value: String, size: Float, gravityValue: Int = Gravity.NO_GRAVITY) =
        TextView(this).apply { text = value; textSize = size; gravity = gravityValue; setTextColor(Color.rgb(31, 41, 55)) }

    private fun loadNotes() {
        notes.clear()
        val array = JSONArray(prefs.getString("notes", "[]") ?: "[]")
        for (i in 0 until array.length()) array.getJSONObject(i).run {
            notes.add(Note(getString("id"), getString("title"), getString("body"), getLong("createdAt")))
        }
    }

    private fun saveNotes() {
        val array = JSONArray()
        notes.forEach { array.put(JSONObject().put("id", it.id).put("title", it.title).put("body", it.body).put("createdAt", it.createdAt)) }
        prefs.edit().putString("notes", array.toString()).apply()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density + 0.5f).toInt()
    private data class Note(val id: String, val title: String, val body: String, val createdAt: Long)
}
