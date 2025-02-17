package com.example.myapplication

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
class PlayersAdapter(private var playersList: List<Player>) :
    RecyclerView.Adapter<PlayersAdapter.PlayerViewHolder>() {

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerName: TextView = itemView.findViewById(R.id.playerName)
        val playerScore: TextView = itemView.findViewById(R.id.playerScore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player, parent, false)
        return PlayerViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = playersList[position]
        holder.playerName.text = player.name
        holder.playerScore.text = "Score: ${player.score}"
    }

    override fun getItemCount(): Int {
        return playersList.size
    }
    fun updateList(newList: ArrayList<Player>) {
        playersList = newList
        notifyDataSetChanged() // Refresh RecyclerView
    }
}