package com.example

import javafx.scene.paint.Color
import javafx.scene.canvas.GraphicsContext
import java.io.Serializable


class Leaderboard : Serializable {
    // Class to score name-score pairs
    data class NameScore(val name : String, val score : Int) : Serializable

    // List to store scores in
    private val lista = mutableListOf<NameScore>()

    // Adding new object to list
    fun add(name: String, score: Int) {
        // Add new score
        lista.add(NameScore(name, score))

        // Sort list by score descending
        lista.sortWith { lhs, rhs -> if (lhs.score > rhs.score) -1 else if (lhs.score < rhs.score) 1 else 0 }

        // Limit list length at 10
        while (lista.size > 10) {
            lista.removeAt(10)
        }
    }

    // Draw leaderboard on provided graphicsContext
    fun draw(gc: GraphicsContext){
        // Start coords
        val startX = 40.0
        val startY = 50.0
        val offsetX = 150.0
        val offsetY = 25.0

        // Currently drawn placement
        var currentPlacement = 1

        // Drawing all list elements
        for (a in lista){
            gc.apply{
                fill = Color.BLACK
                fillText(currentPlacement.toString(), startX, startY + currentPlacement * offsetY)
                fillText(a.name, startX + offsetX, startY + currentPlacement * offsetY)
                fillText(a.score.toString(), startX + 2 * offsetX, startY + currentPlacement * offsetY)
                fill = Color.WHITE
            }
            currentPlacement++
        }
    }
}