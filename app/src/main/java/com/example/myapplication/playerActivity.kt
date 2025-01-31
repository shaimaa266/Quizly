package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
                winButton.isEnabled = true
                showWinners(it)


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

            // Enable the button if the player is not restricted and has not pressed back 3 times
            startButton.isEnabled = attempts < 3 && !player.isRestricted

            if (attempts >= 3) {
                player.isRestricted = true
                startButton.isEnabled = false
                Toast.makeText(
                    this,
                    "Player $name has pressed back too many times. They cannot play again.",
                    Toast.LENGTH_LONG
                ).show()
                Log.d("PlayerActivity", "Player: $name, Restricted: ${player.isRestricted}, Back Presses: ${player.backPressedCount}")
            }
        } else {
            startButton.isEnabled = false
            Toast.makeText(this, "Please enter a valid player name.", Toast.LENGTH_SHORT).show()
        }

    }

    private val playedPlayers = mutableSetOf<String>()
    fun play(view: View) {
        val name = playerName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            return
        }

        val player = players.getOrPut(name) { Player(name) }

        // Check if the player is restricted
        if (player.isRestricted) {
            // Check if two unique players have completed their games
            if (playedPlayers.size >= 2) {
                // Reset the restricted player's state
                player.isRestricted = false
                player.backPressedCount = 0
                playedPlayers.clear() // Reset the played players set
                winButton.isEnabled = true
                Toast.makeText(this, "Player $name is now allowed to play again!", Toast.LENGTH_SHORT).show()
                checkPlayButtonState() // Update the button state
            } else {
                Toast.makeText(
                    this,
                    "Player $name is restricted. Two different players must complete their game first.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        }

        // Check if the player has pressed back 3 times
        if (player.backPressedCount >= 3) {
            player.isRestricted = true
            winButton.isEnabled = false
            Toast.makeText(
                this,
                "Player $name is restricted after 3 back presses.",
                Toast.LENGTH_LONG
            ).show()
            checkPlayButtonState() // Update the button state
            return
        }

        // Launch the game activity
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("player", player)
        mainActivityLauncher.launch(intent)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val name = playerName.text.toString().trim()
        if (resultCode == RESULT_OK && name.isNotEmpty()) {
            playedPlayers.add(name) // Add the player to the set of completed players
            Toast.makeText(this, "Player $name has completed the game.", Toast.LENGTH_SHORT).show()

            // Check if two unique players have completed their games
            if (playedPlayers.size >= 2) {
                // Enable the winButton and update the UI
                winButton.isEnabled = true
                checkPlayButtonState() // Update the button state for restricted players
            }
        }
        Log.d("PlayerActivity", "Played Players: $playedPlayers")
    }


    private fun showWinners(view: View) {
        val intent = Intent(this, WinnerActivity::class.java)
        startActivity(intent)
    }
}


