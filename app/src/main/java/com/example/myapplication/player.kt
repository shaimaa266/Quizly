package com.example.myapplication
import java.io.Serializable
data class Player(
    val name: String,
    var backPressedCount: Int = 0,
    var isRestricted: Boolean = false,
    var score: Int=0
):Serializable

