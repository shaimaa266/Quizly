package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var nameText: TextView
    private lateinit var questionText: TextView
    private lateinit var answerText: Spinner
    private lateinit var startButton: Button
    private lateinit var nextButton: Button
    private lateinit var questionCount: TextView
    private lateinit var image: ImageView

    private var score = 0
    private var index = 0
    private var questions = arrayOf(
        Questions("Egypt", "Cairo"),
        Questions("France", "Paris"),
        Questions("USA", "Washington"),
        Questions("Morocco", "Rabat"),
        Questions("South Korea", "Seoul"),
    )

    private var items = mutableListOf(
        "please select",
        "Cairo", "Baghdad", "Beijing", "Washington",
        "Paris", "Damascus", "London", "Rabat", "Islamabad",
        "Ryad", "Tokyo", "Seoul",
        "Brasília", "New Delhi", "Moscow", "Rome", "Madrid"
    )

    private val itemsMaster = items.toMutableList()
    private lateinit var countryAdapter: ArrayAdapter<String>
    private var player: MediaPlayer? = null
    private lateinit var currentPlayer: Player

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        nameText = findViewById(R.id.textName)
        questionText = findViewById(R.id.questionView)
        answerText = findViewById(R.id.spinner)
        startButton = findViewById(R.id.startButton)
        nextButton = findViewById(R.id.nextButton)
        questionCount = findViewById(R.id.numText)
        image = findViewById(R.id.brainImage)
        image.setImageResource(R.drawable.thinking)

        answerText.visibility = View.GONE
        nextButton.isEnabled = false
        countryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        answerText.adapter = countryAdapter


        currentPlayer = intent.getSerializableExtra("player") as? Player ?: Player("Unknown",-0,false,-1)


        nameText.text = "${currentPlayer.name} is playing.."

        startButton.setOnClickListener { start(it) }
        nextButton.setOnClickListener { next(it) }
    }

    override fun onDestroy() {
        player?.release()
        super.onDestroy()
    }

    @SuppressLint("SetTextI18n")
    private fun start(view: View) {
        player?.stop()
        resetItems()
        score = 0
        index = 0
        questions.shuffle()
        questionText.text = "What is the capital of ${questions[index].country}?"
        questionCount.text = "Question ${index + 1} of ${questions.size}"
        answerText.visibility = View.VISIBLE
        nextButton.isEnabled = true
        answerText.isEnabled = true
        answerText.setSelection(0)
    }
    @SuppressLint("SetTextI18n")
    private fun next(view: View) {
        val answer = answerText.selectedItem.toString()
        if (answerText.selectedItemPosition == 0) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show()
            return
        }
        if (answer.equals(questions[index].capital, ignoreCase = true)) {
            score++ // Increment the score if the answer is correct
            items.remove(answer)
        }
        index++
        if (index < questions.size) {
            questionText.text = "What is the capital of ${questions[index].country}?"
            questionCount.text = "Question ${index + 1} of ${questions.size}"
        } else {
            saveWinnerToPreferences() // Save the winner's data

            // Pass the correct score back to PlayerActivity
            val resultIntent = Intent()
            resultIntent.putExtra("score", score) // Correctly pass the final score
            setResult(RESULT_OK, resultIntent)
            finish() // Close MainActivity and return to PlayerActivity
        }
        items.subList(1, items.size).shuffle()
        countryAdapter.notifyDataSetChanged()
        answerText.setSelection(0)
    }


    private var hasSavedScore = false

    private fun saveWinnerToPreferences() {
        if (score == 0 || hasSavedScore) return// Prevent saving twice in the same session

        val sharedPreferences = getSharedPreferences("GameWinners", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val winnersJson = sharedPreferences.getString("winners", "[]")
        val winnersArray = JSONArray(winnersJson)

        var existingWinnerIndex = -1
        for (i in 0 until winnersArray.length()) {
            val winner = winnersArray.getJSONObject(i)
            if (winner.getString("name") == currentPlayer.name) {
                existingWinnerIndex = i
                break
            }
        }

        if (existingWinnerIndex != -1) {

            val existingWinner = winnersArray.getJSONObject(existingWinnerIndex)
            val existingScore = existingWinner.getInt("score")
            if (score > existingScore) {
                existingWinner.put("score", score) // Update the score
                winnersArray.put(existingWinnerIndex, existingWinner)
            }
        } else {
            // Add a new winner if they don't exist
            val newWinner = JSONObject().apply {
                put("name", currentPlayer.name)
                put("score", score)
            }
            winnersArray.put(newWinner)
        }

        editor.putString("winners", winnersArray.toString())
        editor.apply()

        hasSavedScore = true // Mark the score as saved
        Toast.makeText(this, "Saved ${currentPlayer.name} with score $score!", Toast.LENGTH_SHORT).show()
    }





    override fun onBackPressed() {
        super.onBackPressed()

        currentPlayer.backPressedCount++

        if (currentPlayer.backPressedCount >= 3) {
            currentPlayer.isRestricted = true
            Toast.makeText(
                this,
                "Player ${currentPlayer.name} is restricted after 3 back presses.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun resetItems() {
        items.clear()
        items.addAll(itemsMaster)
        items.subList(1, items.size).shuffle()
        countryAdapter.notifyDataSetChanged()
    }
}

