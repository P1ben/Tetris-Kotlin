package com.example

import java.io.Serializable

data class Coordinate(var x: Int = 0, var y: Int = 0) : Serializable{
    // Copy constructor
    constructor(b: Coordinate) : this(b.x, b.y)

    // Add a coordinate to this
    fun Add(b: Coordinate) : Coordinate{
        return Coordinate(this.x + b.x, this.y + b.y)
    }

    // Checks if 2 coordinates are in the same row or column
    fun isInSameRowOrColumn(b : Coordinate): Boolean{
        if(x == b.x || y == b.y) return true
        return false
    }
}