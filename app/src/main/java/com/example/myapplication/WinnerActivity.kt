package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray

class WinnerActivity : AppCompatActivity() {

    private lateinit var adapter: PlayersAdapter
private lateinit var searchTxt:EditText
lateinit var searchButton: Button
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_winner)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val winnersList = loadWinners()
        searchTxt=findViewById(R.id.searchNameText)
        searchButton=findViewById(R.id.searchButton)
        adapter = PlayersAdapter(winnersList)
        recyclerView.adapter = adapter
    }
    private fun loadWinners(): ArrayList<Player> {
        val sharedPreferences = getSharedPreferences("GameWinners", Context.MODE_PRIVATE)
        val winnersJson = sharedPreferences.getString("winners", "[]")
        val winnersArray = JSONArray(winnersJson)

        val winnersMap = mutableMapOf<String, Player>()
        for (i in 0 until winnersArray.length()) {
            val winner = winnersArray.getJSONObject(i)
            val name = winner.getString("name")
            val score = winner.getInt("score")
            if (!winnersMap.containsKey(name) || winnersMap[name]!!.score < score) {
                winnersMap[name] = Player(name, score = score)
            }
        }


        return ArrayList(winnersMap.values.sortedByDescending { it.score })
    }

    fun searchPlayer(view: View) {
        val name = searchTxt.text.toString().trim()
        val allWinners = loadWinners()

        val filteredWinners = if (name.isNotEmpty()) {
            allWinners.filter { it.name.contains(name, ignoreCase = true) }
        } else {
            allWinners // Show all winners if the search is empty
        }

        adapter.updateList(ArrayList(filteredWinners))
    }


}



