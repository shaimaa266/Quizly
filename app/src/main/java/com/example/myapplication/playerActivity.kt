package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONArray
import org.json.JSONObject

class PlayerActivity : AppCompatActivity() {
    private val playerAttempts = mutableMapOf<String, Int>()
    private lateinit var startButton: Button
    private lateinit var playerName: EditText
    private lateinit var image: ImageView
    private lateinit var winButton: Button
    private lateinit var mainActivityLauncher: ActivityResultLauncher<Intent>
    private var lastPlayerScore: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        startButton = findViewById(R.id.button)
        playerName = findViewById(R.id.editTextt)
        image = findViewById(R.id.playerImage)
        image.setImageResource(R.drawable.playing)
        winButton = findViewById(R.id.winnerButton)


        mainActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val name = playerName.text.toString()
            if (result.resultCode == RESULT_OK && name.isNotEmpty()) {
                val score = result.data?.getIntExtra("score", 0) ?: 0
                lastPlayerScore = score
                playerAttempts[name] = 0 // Reset attempts for successful players
                Toast.makeText(this, "Game completed successfully! Score: $score", Toast.LENGTH_SHORT).show()
            } else if (name.isNotEmpty()) {
                playerAttempts[name] = (playerAttempts[name] ?: 0) + 1
                checkPlayButtonState()
            }
        }

        startButton.setOnClickListener {
            play()
        }

        winButton.setOnClickListener {
            val name = playerName.text.toString()
            if (name.isNotEmpty()) {
                showWinners(name, lastPlayerScore)
            } else {
                Toast.makeText(this, "Please enter a player name first.", Toast.LENGTH_SHORT).show()
            }
        }

        if (intent.getBooleanExtra("resetPlayButton", false)) {
            playerAttempts.clear()
        }

        checkPlayButtonState()
    }

    private fun play() {
        val name = playerName.text.toString()
        if (name.isNotEmpty()) {
            val attempts = playerAttempts[name] ?: 0
            if (attempts < 3) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("name", name)
                mainActivityLauncher.launch(intent)
            } else {
                Toast.makeText(
                    this,
                    "Player $name has exceeded the maximum number of trials.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("CommitPrefEdits")
    private fun showWinners(name: String, score: Int) {
        val sharedPreferences = getSharedPreferences("GameWinners", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val winnersJson = sharedPreferences.getString("winners", "[]")
        var winnersArray = JSONArray(winnersJson)

        if (winnersArray.length() > 0) {
            val highestScore = winnersArray.getJSONObject(0).getInt("score")
            if (score >= highestScore) {
                if (score > highestScore) {
                    winnersArray = JSONArray()
                }
                val newWinner = JSONObject().apply {
                    put("name", name)
                    put("score", score)
                }
                winnersArray.put(newWinner)
                }
                val newWinner = JSONObject().apply {
                    put("name", name)
                    put("score", score)
                }
                winnersArray.put(newWinner)

        } else {
            val newWinner = JSONObject().apply {
                put("name", name)
                put("score", score)
            }
            winnersArray.put(newWinner)
        }

        editor.putString("winners", winnersArray.toString())
        editor.apply()

        val intent = Intent(this, WinnerActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        checkPlayButtonState()
    }

    private fun checkPlayButtonState() {
        val name = playerName.text.toString().trim()
        val attempts = playerAttempts[name] ?: 0
        startButton.isEnabled = attempts < 3
        if (name.isNotEmpty() && attempts >= 3) {
            Toast.makeText(
                this,
                "Player $name has exceeded the maximum number of trials. Please try a different player.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}


