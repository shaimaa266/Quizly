package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
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
    companion object {
        val playersList = mutableListOf<players>()
        var uniquePlayerCount = 0
    }

    private lateinit var nameText: TextView
    private lateinit var questionText: TextView
    private lateinit var answerText: Spinner
    private lateinit var startButton: Button
    private lateinit var nextButton: Button
    private lateinit var questionCount: TextView
    private  lateinit var image: ImageView

    private var score = 0
    private var index = 0
    private var questions = arrayOf(
        questions("Egypt", "Cairo"),
        questions("France", "Paris"),
        questions("USA", "Washington"),
        questions("Morocco", "Rabat"),
       questions("South Korea", "Seoul"),
      /*  questions("Germany", "Berlin"),
        questions("Japan", "Tokyo"),
        questions("Brazil", "Brasília"),
        questions("India", "New Delhi"),
        questions("Russia", "Moscow"),
        questions("Italy", "Rome"),
        questions("Spain", "Madrid"),
        questions("Pakistan", "Islamabad"),
        questions("Greece", "Athens"),
        questions("Turkey", "Ankara")*/
    )

    private var items = mutableListOf(
        "please select",
        "Cairo", "Baghdad", "Beijing", "Washington", "Toronto",
        "Paris", "Damascus", "London", "Rabat", "Islamabad",
        "Ryad", "Tokyo", "Berlin", "Seoul", "Ottawa", "Bern",
        "Cape Town","Brasília", "New Delhi", "Moscow", "Rome", "Madrid"
    )

    private val itemsMaster = items.toMutableList()
    private lateinit var countryAdapter: ArrayAdapter<String>
    private var player: MediaPlayer? = null

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
        image=findViewById(R.id.brainImage)
        image.setImageResource(R.drawable.thinking)
        countryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        answerText.adapter = countryAdapter

        val name = intent.getStringExtra("name")
        nameText.text = "$name is playing.."
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
        nextButton.isEnabled = true
        answerText.isEnabled = true
        answerText.setSelection(0)
    }
    override fun onBackPressed() {
        super.onBackPressed()

        setResult(RESULT_CANCELED)
    }


    @SuppressLint("SetTextI18n")

    private fun next(view: View) {
        val answer = answerText.selectedItem.toString()
        if (answerText.selectedItemPosition == 0) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show()
            return
        }
        if (answer.equals(questions[index].capital, ignoreCase = true)) {
            score++
        }
        index++
        if (index < questions.size) {
            questionText.text = "What is the capital of ${questions[index].country}?"
            questionCount.text = "Question ${index + 1} of ${questions.size}"
        } else {
            // Game finished
            saveWinnerToPreferences()

            val name = intent.getStringExtra("name") ?: "Unknown"

            // Start WinnerActivity and pass name and score
            val intent = Intent(this, WinnerActivity::class.java)
            intent.putExtra("name", name)
            intent.putExtra("score", score)
            startActivity(intent)
        }
        items.subList(1, items.size).shuffle()
        countryAdapter.notifyDataSetChanged()
        answerText.setSelection(0)
    }


    private fun saveWinnerToPreferences() {
        val sharedPreferences = getSharedPreferences("GameWinners", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val winnersJson = sharedPreferences.getString("winners", "[]")
        val winnersArray = JSONArray(winnersJson)

        val name = intent.getStringExtra("name") ?: "Unknown"

        val newWinner = JSONObject().apply {
            put("name", name)
            put("score", score)
        }
        winnersArray.put(newWinner)

        editor.putString("winners", winnersArray.toString())
        editor.apply()

        Toast.makeText(this, "Saved $name with score $score to winners!", Toast.LENGTH_SHORT).show()
    }


    private fun resetItems() {

        items.clear()
        items.addAll(itemsMaster)
        items.subList(1, items.size).shuffle()
        countryAdapter.notifyDataSetChanged()
    }
}

