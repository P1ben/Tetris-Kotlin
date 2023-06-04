package com.example

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import java.awt.Dimension

class NextComponentPanel(var contained : Components) {
    // 5 * 5 matrix filled with empty color
    var field : Array<Array<Colors>> = Array(5){
        _ -> Array(5){
            _ -> Colors.empty
        }
    }

    init {
        // Update matrix with default component
        switch_component(contained)
    }

    // Switch component in matrix
    fun switch_component(a: Components) {
        // Update contained
        contained = a

        // Reset matrix to default
        field = Array(5){
            _ -> Array(5){
                _ -> Colors.empty
            }
        }

        // Get coordinates for the parts of given shape
        // Can't be null, as every shape is contained in the map
        val pos: Array<Coordinate> = Component.component_dims[a]!!

        // Update matrix with the part position values and given color
        // Can't be null, as every Component-Color pair is defined in the matrix
        for (b in pos) {
            field[2 + b.x][2 + b.y] = Board.component_colors[a]!!
        }
    }

    // Draw Panel on the given graphicsContext
    fun draw(gc : GraphicsContext){
        // Pos
        var x = 320
        var y = 30

        // Dimensions
        val size = Dimension(410 - x, 410 - x)
        val squareH = size.getHeight().toInt() / field[0].count()
        val squareW = squareH

        // Set draw color
        gc.apply{
            fill = Color.BLACK
            stroke = Color.BLACK
        }

        // Draw matrix onto gc
        for (a in field) {
            for (b in a) {
                gc.strokeRect(x.toDouble(), y.toDouble(), squareW.toDouble(), squareH.toDouble())
                if (b !== Colors.empty) {
                    gc.apply{
                        fill = Board.color_map[b]
                        fillRect(x + 1.0, y + 1.0, squareW - 1.0, squareH - 1.0)
                        fill = Color.GRAY
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
            y = 30
            x += squareH
        }
    }
}