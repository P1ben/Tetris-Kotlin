package com.example

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.Group
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.input.KeyCode
import javafx.scene.paint.Color
import java.io.*
import java.util.*

class GameWindow : Group() {
    // Misc
    var score : Int = 0
    val initialSpeed : Int = 800
    val maxSpeed : Int = 100
    var currentSpeed : Int = initialSpeed
    var key_listener_enabled : Boolean = true

    // Board
    var board : Board = Board(10, 20)

    // Timer
    var timer : Timer = Timer(true)

    // Control Buttons
    val pause_btn = Button("pause")
    val resume_btn = Button("resume")
    val save_btn = Button("save")
    val load_btn = Button("load")
    val back_btn = Button("back")

    // Other scene objects
    val score_label = Label("Score: ")
    val canvas = Canvas(410.0, 651.0)
    var gc = canvas.graphicsContext2D
    val next_pan = NextComponentPanel(Components.square)

    // Active keys set
    val currentlyActiveKeys : MutableSet<KeyCode> = mutableSetOf<KeyCode>()

    // Timer tasks
    lateinit var update_task: TimerTask
    lateinit var key_listener_task: TimerTask

    init{
        // Creating timer tasks
        recreateTimerTask()
        recreateKeyListenerTask()

        // Scheduling timer tasks
        timer.scheduleAtFixedRate(update_task, 0, currentSpeed.toLong())
        timer.scheduleAtFixedRate(key_listener_task, 0, 100)

        // Pause Button init
        pause_btn.apply{
            layoutX = 340.0
            layoutY = 180.0
            onAction = EventHandler {
                    _ -> run {
                // Stop timer
                timer.cancel()
                timer.purge()

                pause_btn.isVisible = false
                load_btn.isVisible = true
                save_btn.isVisible = true
                back_btn.isVisible = true
                resume_btn.isVisible = true
                key_listener_enabled = false
            }
            }
        }

        // Resume Button init
        resume_btn.apply{
            layoutX = 340.0
            layoutY = 180.0
            isVisible = false
            onAction = EventHandler {
                    _ -> run {
                restartTimer()
                pause_btn.isVisible = true

                load_btn.isVisible = false
                save_btn.isVisible = false
                back_btn.isVisible = false
                resume_btn.isVisible = false
                key_listener_enabled = true
            }
            }
        }

        // Save Button init
        save_btn.apply{
            layoutX = 340.0
            layoutY = 210.0
            isVisible = false
            onAction = EventHandler {
                    _ -> run {
                save_game()
            }
            }
        }

        // Load Button Init
        load_btn.apply{
            layoutX = 340.0
            layoutY = 240.0
            isVisible = false
            onAction = EventHandler {
                    _ -> run {
                load_game()
                board.resetCurrentComponentPosition()
                score = board.score
                if(score <= 100000) currentSpeed = initialSpeed - ((initialSpeed - maxSpeed) / 10) * (score / 10000)
                else currentSpeed = maxSpeed

                // Queueing board draw on UI thread
                Platform.runLater{
                    board.drawBoard(gc)
                    next_pan.draw(gc)
                }
            }
            }
        }

        // Back button init
        back_btn.apply{
            layoutX = 340.0
            layoutY = 270.0
            isVisible = false
            onAction = EventHandler {
                    _ -> run {
                back_to_menu()
            }
            }
        }

        // Score label init
        score_label.apply{
            layoutY = 5.0
            layoutX = 40.0
        }

        // Adding objects to scene
        this.children.apply{
            add(canvas)
            add(pause_btn)
            add(resume_btn)
            add(save_btn)
            add(load_btn)
            add(back_btn)
            add(score_label)
        }

        // Key Press event handlers
        this.onKeyPressed = EventHandler { event ->
            currentlyActiveKeys.add(event.code)
        }

        this.onKeyReleased = EventHandler { event ->
            currentlyActiveKeys.remove(event.code)
        }

        // Clearing canvas and drawing current score
        gc.apply{
            fill = Color.WHITE
            gc.fillRect(90.0, 0.0, 60.0, 30.0)
            gc.fill = Color.BLACK
            gc.fillText(board.score.toString(), 100.0, 18.0)
        }

    }

    // Creates a new Timer Task
    private fun recreateTimerTask(){
        update_task = object : TimerTask() {
            override fun run() {
                // Update board
                when(board.updateBoard()){
                    BoardEvents.spawned -> {
                        next_pan.switch_component(board.next)
                    }
                    BoardEvents.placed -> {
                        // Redraw score
                        gc.apply{
                            fill = Color.WHITE
                            fillRect(90.0, 0.0, 60.0, 30.0)
                            fill = Color.BLACK
                            fillText(board.score.toString(), 100.0, 18.0)
                        }
                        restartTimer()
                    }
                    BoardEvents.ended -> {
                        back_to_menu()
                    }
                    else -> {}
                }

                // Queueing board draw on UI thread
                Platform.runLater{
                    board.drawBoard(gc)
                    next_pan.draw(gc)
                }
            }
        }
    }

    // Recreats the key listener task
    fun recreateKeyListenerTask(){
        key_listener_task = object : TimerTask() {
            override fun run() {
                key_press_update()
            }
        }
    }

    // Clears Timer, then sends signal to end game
    fun back_to_menu(){
        timer.cancel()
        timer.purge()
        Game.game_back_interrupt = true
    }

    // Restarts Timer
    fun restartTimer() {
        // Destroy timer
        timer.cancel()
        timer.purge()

        // Create new timer and timer tasks
        timer = Timer(true)
        recreateTimerTask()
        recreateKeyListenerTask()

        // Reschedule everything
        timer.scheduleAtFixedRate(update_task, 30.toLong(), currentSpeed.toLong())
        timer.scheduleAtFixedRate(key_listener_task, 100.toLong(), 100)
    }

    // Save the current state of the game to a file
    private fun save_game() {
        try {
            val fileOut = FileOutputStream("game_save.ser")
            val out = ObjectOutputStream(fileOut)
            out.writeObject(board)
            out.close()
            fileOut.close()
            println("Game state saved successfully")
        } catch (e: IOException) {
            println("Couldn't save game")
        }
    }

    // Load saved game state
    private fun load_game() {
        try {
            val fileIn = FileInputStream("game_save.ser")
            val input = ObjectInputStream(fileIn)
            board = input.readObject() as Board
            input.close()
            fileIn.close()
            next_pan.switch_component(board.next)
        } catch (e: IOException) {
            println("Couldn't load game, probably no save exists")
            return
        }
    }

    // Handle key press
    fun key_press_update(){
        if (currentlyActiveKeys.contains(KeyCode.W)) {
            board.control_rotate_left()
        }
        if (currentlyActiveKeys.contains(KeyCode.A)) {
            board.control_move_left()
        }
        if (currentlyActiveKeys.contains(KeyCode.S)) {
            board.control_rotate_right()
        }
        if (currentlyActiveKeys.contains(KeyCode.D)) {
            board.control_move_right()
        }
        if (currentlyActiveKeys.contains(KeyCode.E)){
            board.control_move_place()
            restartTimer()
        }

        // Queueing board draw on UI thread
        if(currentlyActiveKeys.isNotEmpty())
            Platform.runLater{
                board.drawBoard(gc)
            }
    }
}