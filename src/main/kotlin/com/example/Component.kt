package com.example

import java.io.Serializable

class Component(t: Components, spwn_coords: Coordinate): Serializable {
    companion object CompCoords {
        // relative coords of each component
        val component_dims: Map<Components, Array<Coordinate>> =
            mapOf(
                Pair(Components.line, arrayOf(Coordinate(0, 0), Coordinate(-2,  0), Coordinate(-1,  0), Coordinate(1,  0))),
                Pair(Components.L, arrayOf(Coordinate(0, 0), Coordinate(-1,  0), Coordinate(1,  0), Coordinate(1,  -1))),
                Pair(Components.L_rev, arrayOf(Coordinate(0, 0), Coordinate(-1,  -1), Coordinate(-1,  0), Coordinate(1,  0))),
                Pair(Components.square, arrayOf(Coordinate(0, 0), Coordinate(-1,  -1), Coordinate(-1,  0), Coordinate(0,  -1))),
                Pair(Components.cross, arrayOf(Coordinate(0, 0), Coordinate(-1,  0), Coordinate(0,  -1), Coordinate(1,  0))),
                Pair(Components.Z, arrayOf(Coordinate(0, 0), Coordinate(-1,  -1), Coordinate(0,  -1), Coordinate(1,  0))),
                Pair(Components.Z_rev, arrayOf(Coordinate(0, 0), Coordinate(-1,  0), Coordinate(0,  -1), Coordinate(1,  -1)))
            )
    }

    // Type of the component
    val type : Components = t

    val coords : Array<Coordinate> = Array(4){
            index -> (spwn_coords.Add(component_dims[t]!![index]))
    }

    // Copy
    constructor(b: Component) : this(b.type, b.coords[0]){
        for (i in 0..3) {
            coords[i] = Coordinate(b.coords[i])
        }
    }

    // Moves the component down by n
    fun move_down(n: Int) {
        for (i in 0..3) {
            coords[i] = coords[i].Add(Coordinate(0, n))
        }
    }

    // Returns a new component moved by b
    fun shadow_move(b: Coordinate): Component {
        val temp = Component(this)
        for (i in 0..3) {
            temp.coords[i] = temp.coords[i].Add(b)
        }
        return temp
    }

    // Rotates the component counterclockwise 90 degrees
    fun rotate_left() {
        val abs = coords[0]
        for (i in 0..3) {
            coords[i] = coords[i].Add(
                Coordinate(
                    -abs.x,
                    -abs.y
                )
            )
            coords[i] = Coordinate(coords[i].y, -coords[i].x)
            coords[i] = coords[i].Add(abs)
        }
    }

    // Returns a new component rotated counterclockwise 90 degrees
    fun shadow_rotate_left(): Component {
        val temp = Component(this)
        val abs = temp.coords[0]
        for (i in 0..3) {
            temp.coords[i] = temp.coords[i].Add(
                Coordinate(
                    -abs.x,
                    -abs.y
                )
            )
            temp.coords[i] = Coordinate(temp.coords[i].y, -temp.coords[i].x)
            temp.coords[i] = temp.coords[i].Add(abs)
        }
        return temp
    }

    // Rotates the component clockwise 90 degrees
    fun rotate_right() {
        val abs = coords[0]
        for (i in 0..3) {
            coords[i] = coords[i].Add(
                Coordinate(
                    -abs.x,
                    -abs.y
                )
            )
            coords[i] = Coordinate(-coords[i].y, coords[i].x)
            coords[i] = coords[i].Add(abs)
        }
    }

    // Returns a new component rotated clockwise 90 degrees
    fun shadow_rotate_right(): Component {
        val temp = Component(this)
        val abs = temp.coords[0]
        for (i in 0..3) {
            temp.coords[i] = temp.coords[i].Add(
                Coordinate(
                    -abs.x,
                    -abs.y
                )
            )
            temp.coords[i] = Coordinate(-temp.coords[i].y, temp.coords[i].x)
            temp.coords[i] = temp.coords[i].Add(abs)
        }
        return temp
    }

    // Moves the component right by n
    fun move_right(n: Int) {
        for (i in 0..3) {
            coords[i] = coords[i].Add(Coordinate(n, 0))
        }
    }

    // Moves the component left by n
    fun move_left(n: Int) {
        for (i in 0..3) {
            coords[i] = coords[i].Add(Coordinate(-n, 0))
        }
    }

    // Checks if component is at bottom border
    fun isAtBottomBorder(board_height: Int): Boolean {
        for (a in coords) {
            if (a.y == board_height - 1) return true
        }
        return false
    }

    // Checks if a coordinate is part of this component
    fun isPartOf(b: Coordinate): Boolean {
        for (a in coords) {
            if (a == b) return true
        }
        return false
    }
}