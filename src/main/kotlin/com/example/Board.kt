package com.example

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import java.awt.Dimension
import java.io.Serializable
import kotlin.random.Random

class Board(width: Int, height: Int) : Serializable {
    // Static values for set attributes
    companion object CompCoords {
        // Component type - Color pairs
        val component_colors: Map<Components, Colors> =
            mapOf(
                Pair(Components.line, Colors.turquoise),
                Pair(Components.L, Colors.orange),
                Pair(Components.L_rev, Colors.blue),
                Pair(Components.square, Colors.yellow),
                Pair(Components.cross, Colors.purple),
                Pair(Components.Z, Colors.red),
                Pair(Components.Z_rev, Colors.green)
            )

        // Matrix used color - JavaFX color pairs
        val color_map: Map<Colors, Color> =
            mapOf(
                Pair(Colors.turquoise, Color.TURQUOISE),
                Pair(Colors.orange, Color.ORANGE),
                Pair(Colors.blue, Color.BLUE),
                Pair(Colors.yellow, Color.YELLOW),
                Pair(Colors.purple, Color.MAGENTA),
                Pair(Colors.red, Color.RED),
                Pair(Colors.green, Color.GREEN)
            )

        // Array containing all components
        val all_components = arrayOf(
            Components.line,
            Components.L,
            Components.L_rev,
            Components.square,
            Components.cross,
            Components.Z,
            Components.Z_rev
        )
    }

    // Values for initializing and drawing the board
    private val startSquareX = 0
    private val startSquareY = 30
    private val board_height = height
    private val board_width = width

    // Position to spawn new components in the matrix
    private var spawn_pos: Coordinate = Coordinate(0, 0)

    // Creating matrix filled with empty color
    private var board: Array<Array<Colors>> =
        Array(width){
            Array(height){
                Colors.empty
            }
    }

    // Misc values
    var score = 0
    private var squareW = 0
    private var squareH = 0

    // Current and next component
    private var current : Component? = null
    var next = all_components[Random.nextInt(7)]

    init{
        // Calculating spawn position
        val spawn_x = if (width % 2 == 0) width / 2 else width / 2 - 1
        spawn_pos = Coordinate(spawn_x, 1)
    }

    // Update board matrix
    fun updateBoard(): BoardEvents {
        // If current is null: the component was either placed or never initialized
        if (current == null) {
            // If spawn location is obstructed, end game; else it spawns the component
            if (!spawnComponent(next)) {
                println("Game ended")

                // Return ended status
                return BoardEvents.ended
            }

            // Choose next component
            next = all_components[Random.nextInt(7)]

            // Update matrix with newly rendered component
            renderCurrentComponent()

            // Return spawned status
            return BoardEvents.spawned
        } else if (checkCollision(current)) {
            // Current component was placed
            current = null
            removeFullRows()

            // Return placed status
            return BoardEvents.placed
        } else {
            // Reset board matrix
            eraseCurrentComponent()

            // Moves component down by 1
            // Can't be null
            current!!.move_down(1)

            // Update board matrix
            renderCurrentComponent()
        }
        // Return running state
        return BoardEvents.running
    }

    // Checks if current component collided with another one, or
    // reached the bottom border
    private fun checkCollision(comp: Component?): Boolean {
        if (comp == null) return false
        if (comp.isAtBottomBorder(board_height)) return true
        for (a in comp.coords) {
            val temp = a.Add(Coordinate(0, 1))
            if (!comp.isPartOf(temp) && getFieldColor(temp) !== Colors.empty) return true
        }
        return false
    }

    // Update the score based on the amount of rows removed
    private fun calculateScore(rowsDeleted: Int) {
        score += when (rowsDeleted) {
            1 -> 200
            2 -> 500
            3 -> 1000
            4 -> 2000 // Tetris
            else -> 0
        }
    }

    // Get the color of the field at the given coordinate
    private fun getFieldColor(a: Coordinate): Colors {
        if (a.x < 0 || a.y < 0 || a.x > board_width - 1 || a.y > board_height - 1) throw IndexOutOfBoundsException(
            "A koordinata a tabla hatarain kivulre esik."
        )
        return board[a.x][a.y]
    }

    // Spawn new component
    // Returns true on success, false if new component cannot be spawned
    private fun spawnComponent(a: Components): Boolean {
        if (current != null) return false
        val temp = Component(a, spawn_pos)
        for (c in temp.coords) {
            if (getFieldColor(c) !== Colors.empty) return false
        }
        current = temp
        return true
    }

    // Resets the current component's position to the spawn position
    fun resetCurrentComponentPosition() {
        if (current == null) return
        val temp = current!!.type
        eraseCurrentComponent()
        current = null
        spawnComponent(temp)
        renderCurrentComponent()
    }

    // Erases the current component from the board matrix
    private fun eraseCurrentComponent() {
        if (current == null) return
        val temp = current!!.coords
        for (a in temp) {
            board[a.x][a.y] = Colors.empty
        }
    }

    // Draws current component on the board matrix
    private fun renderCurrentComponent() {
        if (current == null) return
        val temp = current!!.coords
        for (a in temp) {
            board[a.x][a.y] = component_colors[current!!.type]!!
        }
    }

    // Checks if a place determined by the b : Component is empty or not
    private fun checkIfNewPlaceIsEmpty(b: Component): Boolean {
        for (a in b.coords) {
            if (getFieldColor(a) !== Colors.empty) return false
        }
        return true
    }

    // Removes full rows, then updates score based on rows removed
    private fun removeFullRows() {
        eraseCurrentComponent()
        var counter = 0
        for (i in 0 until board_height) {
            var good = true
            for (j in 0 until board_width) {
                if (board[j][i] === Colors.empty) {
                    good = false
                    break
                }
            }
            if (good) {
                counter++
                deleteRow(i)
            }
        }
        renderCurrentComponent()
        calculateScore(counter)
    }

    // Checks if a place determined by the b : Component is in the matrix, or out of bounds
    private fun checkIfNewPlaceIsInBounds(b: Component): Boolean {
        for (a in b.coords) {
            if (a.x < 0 || a.x > board_width - 1 || a.y < 0 || a.y > board_height - 1) return false
        }
        return true
    }

    // Deletes the row k in the board matrix, then moves every placed block
    // down by one
    private fun deleteRow(k: Int) {
        for (i in 0 until board_width) {
            board[i][k] = Colors.empty
        }
        for (i in k downTo 1) {
            for (j in 0 until board_width) {
                board[j][i] = board[j][i - 1]
            }
        }
        for (i in 0 until board_width) {
            board[i][0] = Colors.empty
        }
    }

    // If the move can be completed, moves the current component right 1 cell
    fun control_move_right() {
        if (current == null) return
        for (b in current!!.coords) {
            if (b.isInSameRowOrColumn(Coordinate(board_width - 1, -1))) return
        }
        eraseCurrentComponent()
        if (checkIfNewPlaceIsEmpty(current!!.shadow_move(Coordinate(1, 0)))) current!!.move_right(1)
        renderCurrentComponent()
    }

    // If the move can be completed, moves the current component left 1 cell
    fun control_move_left() {
        if (current == null) return
        for (b in current!!.coords) {
            if (b.isInSameRowOrColumn(Coordinate(0, -1))) return
        }
        eraseCurrentComponent()
        if (checkIfNewPlaceIsEmpty(current!!.shadow_move(Coordinate(-1, 0)))) current!!.move_left(1)
        renderCurrentComponent()
    }

    // If the move can be completed, rotates the current component counterclockwise 90 degrees
    fun control_rotate_left() {
        if (current == null || current!!.type === Components.square) return
        eraseCurrentComponent()
        val temp = current!!.shadow_rotate_left()
        if (checkIfNewPlaceIsInBounds(temp) && checkIfNewPlaceIsEmpty(temp)) current!!.rotate_left()
        renderCurrentComponent()
        //repaint()
    }

    // If the move can be completed, rotates the current component clockwise 90 degrees
    fun control_rotate_right() {
        if (current == null || current!!.type === Components.square) return
        eraseCurrentComponent()
        val temp = current!!.shadow_rotate_right()
        if (checkIfNewPlaceIsInBounds(temp) && checkIfNewPlaceIsEmpty(temp)) current!!.rotate_right()
        renderCurrentComponent()
        //repaint()
    }

    // Sends the current component to the bottom of the board matrix instantly
    fun control_move_place() {
        if (current == null) return
        if (checkCollision(current)) return
        var needed = 0
        for (i in 1 until board_height) {
            val temp = current!!.shadow_move(Coordinate(0, i))
            if (checkCollision(temp)) {
                needed = i
                break
            }
        }
        if (needed == 0) return
        eraseCurrentComponent()
        current!!.move_down(needed)
        renderCurrentComponent()
    }

    // Generate placement outline
    // Returns the number of down moves needed to get to the bottom
    private fun generate_placement_outline(): Int {
        if (current == null) return 0
        if (checkCollision(current)) return 0
        var needed = 0
        for (i in 1 until board_height) {
            val temp = current!!.shadow_move(Coordinate(0, i))
            if (checkCollision(temp)) {
                needed = i
                break
            }
        }
        return needed
    }

    // Draws board onto the provided gc
    fun drawBoard(gc: GraphicsContext) {
        // Initial values
        var x = startSquareX
        var y = startSquareY

        // Calculate square size
        val size = Dimension(410, 611)
        squareH = size.getHeight().toInt() / board[0].count()
        squareW = squareH

        // Set drawing color
        gc.apply{
            fill = Color.BLACK
            stroke = Color.BLACK
        }

        // Draw board matrix
        for (a in board) {
            for (b in a) {
                gc.strokeRect(x.toDouble(), y.toDouble(), squareW.toDouble(), squareH.toDouble())

                if (b !== Colors.empty) {
                    gc.apply{
                        fill = color_map[b]
                        fillRect(x + 1.0, y + 1.0, squareW - 1.0, squareH - 1.0)
                        fill = Color.WHITE
                    }
                }
                else{
                    gc.apply{
                        fill = Color.GRAY
                        fillRect(x + 1.0, y + 1.0, squareW - 1.0, squareH - 1.0)
                        fill = Color.WHITE
                    }
                }
                y += squareW
            }
            y = startSquareY
            x += squareH
        }

        // Drawing placement outline
        if (current != null) {
            val h = generate_placement_outline()
            if (h != 0) {
                val temp = current!!.shadow_move(Coordinate(0, h))

                // Get color and desaturate it
                val fillcolor = color_map[component_colors[temp.type]]!!.desaturate()
                for (cord in temp.coords) {
                    gc.apply{
                        fill = fillcolor
                        fillRect(startSquareX + 1.0 + cord.x * squareW, startSquareY + 1.0 + cord.y * squareH, squareW - 1.0, squareH - 1.0)
                        fill = Color.WHITE
                    }
                }
            }
        }
    }
}