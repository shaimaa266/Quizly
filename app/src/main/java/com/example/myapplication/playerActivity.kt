package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
    private val players = mutableMapOf<String, Player>()
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

        startButton.setOnClickListener {
            play(it)
        }

        winButton.setOnClickListener {
            val name = playerName.text.toString()
            if (name.isNotEmpty() && lastPlayerScore > 0) {
                showWinners(name, lastPlayerScore)
                winButton.isEnabled = true
            } else {
                Toast.makeText(this, "No completed game to show winners.", Toast.LENGTH_SHORT).show()
            }
        }


        playerName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkPlayButtonState()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        checkPlayButtonState()

        mainActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val name = playerName.text.toString()
            if (result.resultCode == RESULT_OK && name.isNotEmpty()) {
                val score = result.data?.getIntExtra("score", 0) ?: 0
                lastPlayerScore = score
                val player = players[name] ?: Player(name)
                player.score = score
                player.backPressedCount = 0
                player.isRestricted = false
                players[name] = player
                Toast.makeText(this, "Game completed successfully! Score: $score", Toast.LENGTH_SHORT).show()
            } else if (name.isNotEmpty()) {
                val player = players[name] ?: Player(name)
                player.backPressedCount++
                players[name] = player
                checkPlayButtonState()
            }
        }


        if (intent.getBooleanExtra("resetPlayButton", false)) {
            players.clear()
        }
    }

    override fun onBackPressed() {
        val name = playerName.text.toString()
        if (name.isNotEmpty()) {
            val player = players[name] ?: Player(name)
            player.backPressedCount++
            players[name] = player
            checkPlayButtonState()
        }
        super.onBackPressed()
    }

    private fun checkPlayButtonState() {
        val name = playerName.text.toString().trim()
        if (name.isNotEmpty()) {
            val player = players[name] ?: Player(name)
            val attempts = player.backPressedCount

            startButton.isEnabled = attempts < 3

            if (attempts >= 3) {
                player.isRestricted = true
                startButton.isEnabled = false
                Toast.makeText(
                    this,
                    "Player $name has pressed back too many times. They cannot play again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            startButton.isEnabled = false
            Toast.makeText(this, "Please enter a valid player name.", Toast.LENGTH_SHORT).show()
        }
    }


    fun play(view: View) {
        val name = playerName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            return
        }

        val player = players.getOrPut(name) { Player(name) }

        if (player.isRestricted) {
            val otherPlayersPlayed = players.values.count { it.score > 0 }
            if (otherPlayersPlayed < 2) {
                Toast.makeText(
                    this,
                    "Player $name is restricted. Two different players must play first.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            player.isRestricted = false
            winButton.isEnabled=true
        }


        if (player.backPressedCount >= 3) {
            player.isRestricted = true
            winButton.isEnabled=false
            Toast.makeText(
                this,
                "Player $name is restricted after 3 back presses.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("player", player)
        mainActivityLauncher.launch(intent)
    }

    private fun showWinners(name: String, score: Int) {
        val sharedPreferences = getSharedPreferences("GameWinners", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val winnersJson = sharedPreferences.getString("winners", "[]")
        var winnersArray = JSONArray(winnersJson)

        val highestScore = if (winnersArray.length() > 0) {
            var maxScore = 0
            for (i in 0 until winnersArray.length()) {
                val winner = winnersArray.getJSONObject(i)
                maxScore = maxOf(maxScore, winner.getInt("score"))
            }
            maxScore
        } else {
            -1
        }

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

        editor.putString("winners", winnersArray.toString())
        editor.apply()

        val intent = Intent(this, WinnerActivity::class.java)
        startActivity(intent)
    }
}


