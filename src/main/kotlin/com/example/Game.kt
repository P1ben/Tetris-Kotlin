package com.example

import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.Stage
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.system.exitProcess

class Game : Application() {

    companion object {
        private const val WIDTH = 410
        private const val HEIGHT = 651
        var game_back_interrupt = false
    }

    private var graphicsContext: GraphicsContext
    private lateinit var mainStg : Stage

    var leaderboard = Leaderboard()
    var lastScore = 0

    lateinit var gw : GameWindow

    // Menu
    val menu_content : Group = Group()
    val menu = Scene(menu_content)

    // Menu components
    var menu_btn_start : Button = Button("start")
    var menu_btn_leaderboard : Button = Button("leaderboard")
    var menu_btn_back : Button = Button("back")
    var menu_btn_exit : Button = Button("exit")

    // Name input scene
    val name_input_content : Group = Group()
    val name_input = Scene(name_input_content)

    // Name input components
    var name_field : TextField = TextField("Name")
    var name_submit_btn = Button("submit")
    var name_submit_score_label = Label("Score: ")
    var name_submit_canvas = Canvas(WIDTH.toDouble(), HEIGHT.toDouble())

    init {
        // Menu canvas/graphicsContext init
        val canvas = Canvas(WIDTH.toDouble(), HEIGHT.toDouble())
        menu_content.children.add(canvas)
        graphicsContext = canvas.graphicsContext2D

        // Trying to read existing leaderboard
        load_leaderboard()

        // Menu Start Button init
        menu_btn_start.apply{
            isVisible = true
            layoutX = 190.0
            layoutY = 320.0
            onAction = EventHandler {
                    _ -> run {
                    gw = GameWindow()
                    mainStg.scene = Scene(gw)
            }
            }
        }

        // Menu Leaderboard Button init
        menu_btn_leaderboard.apply{
            isVisible = true
            layoutX = 170.0
            layoutY = 350.0
            onAction = EventHandler {
                    _ -> run {
                this.isVisible = false
                menu_btn_start.isVisible = false
                menu_btn_exit.isVisible = false
                menu_btn_back.isVisible = true
                graphicsContext.fill = Color.DARKGRAY
                graphicsContext.fillRect(0.0, 0.0, WIDTH.toDouble(), HEIGHT.toDouble())
                load_leaderboard()
                leaderboard.draw(graphicsContext)
            }
            }
        }

        // Menu Back Button init
        menu_btn_back.apply{
            isVisible = false
            layoutX = 190.0
            layoutY = 500.0
            onAction = EventHandler {
                    _ -> run {
                menu_btn_start.isVisible = true
                menu_btn_leaderboard.isVisible = true
                menu_btn_exit.isVisible = true
                menu_btn_back.isVisible = false
                graphicsContext.fill = Color.DARKGRAY
                graphicsContext.fillRect(0.0, 0.0, WIDTH.toDouble(), HEIGHT.toDouble())
                render_menu_text()
            }
            }
        }

        // Menu exit button init
        menu_btn_exit.apply{
            isVisible = true
            layoutX = 193.0
            layoutY = 380.0
            onAction = EventHandler {
                    _ -> run {
                exitProcess(0)
            }
            }
        }

        // Name Input Field init
        name_field.apply{
            isVisible = true
            layoutX = 100.0
            layoutY = 300.0
        }

        // Name Input Field Submit Button init
        name_submit_btn.apply{
            layoutX = 260.0
            layoutY = 300.0
            onAction = EventHandler { _ ->
                run {
                    if (!name_field.text.equals("")) {
                        leaderboard.add(name_field.text, lastScore)
                        save_leaderboard()
                    }
                    mainStg.scene = menu
                }
            }
        }

        // Submit Score Label init
        name_submit_score_label.apply{
            layoutX = 100.0
            layoutY = 250.0
        }

        // Adding objects to menu
        menu_content.apply{
            children.add(menu_btn_start)
            children.add(menu_btn_back)
            children.add(menu_btn_leaderboard)
            children.add(menu_btn_exit)
        }

        // Adding objects to Name Input screen
        name_input_content.apply{
            children.add(name_submit_canvas)
            children.add(name_field)
            children.add(name_submit_btn)
            children.add(name_submit_score_label)
        }
    }

    // Drawing "TETRIS" Title in menu
    fun render_menu_text(){
        graphicsContext.apply{
            fill = Color.GRAY
            font = Font.font("Comic Sans MS", 100.0)
            fillText("TETRIS", 12.0, 150.0)
            font = Font.font("Arial", 12.0)
        }
    }

    // JavaFX init
    override fun start(mainStage: Stage) {
        // Window init
        mainStage.title = "Tetris"
        mainStg = mainStage
        mainStage.scene = menu

        // Main loop for signal handling
        object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                tick()
            }
        }.start()

        // Clearing menu canvas, then rendering menu text
        graphicsContext.fill = Color.DARKGRAY
        graphicsContext.fillRect(0.0, 0.0, WIDTH.toDouble(), HEIGHT.toDouble())
        render_menu_text()
        mainStage.show()
    }

    private fun tick() {
        // Waits for end signal from game
        if(game_back_interrupt){
            game_back_interrupt = false
            lastScore = gw.board.score
            println("Score: $lastScore")

            // If the score is not 0, name input is required
            if (lastScore == 0){
                mainStg.scene = menu
            }
            else {
                mainStg.scene  = name_input
                name_submit_canvas.graphicsContext2D.apply{
                    fill = Color.WHITE
                    fillRect(100.0, 250.0, 400.0, 50.0)
                    fill = Color.BLACK
                    fillText(lastScore.toString(), 150.0, 263.0)
                    fill = Color.WHITE
                }
            }
        }
    }

    // Trying to save leaderboard
    private fun save_leaderboard() {
        try {
            val fileOut = FileOutputStream("leaderboard.ser")
            val out = ObjectOutputStream(fileOut)
            out.writeObject(leaderboard)
            out.close()
            fileOut.close()
            println("Score saved")
        }
        catch(e:Exception){
            println("Couldn't save leaderboard")
        }
    }

    // Trying to load an existing leaderboard
    fun load_leaderboard(){
        try {
            val fileIn = FileInputStream("leaderboard.ser")
            val input = ObjectInputStream(fileIn)
            leaderboard = input.readObject() as Leaderboard
            input.close()
            fileIn.close()
            println("Leaderboard loaded succesfully")
        }
        catch(e: Exception){
            println("Couldn't load leaderboard")
        }
    }
}
