import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple Snake game written using the application of JavaFX
 *
 * @author Luan Ta
 * @version 1.0.0 Beta
 */
public class SerpentApp extends Application
{
    private Scene main; // The reference to the program's display
    private GridPane root;
    private GridData board; // The Model of this program. Holds the states of the game.
    private long stepTiming; // The timing between each execution. Intended to restrict/delay the input of another key
    private int direction; // Based on the input of the user, determine the direction of the Snake
    private static final int STARTING_DIRECTION = 0; // Starting from the Left. See (*) below
    public static final int DELAY = 300; // In milliseconds
    private Timer timer;

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        /*
        Initialize instance variables and set up the layout
         */
        stepTiming = System.currentTimeMillis();
        direction = STARTING_DIRECTION;

        root = new GridPane();

        Label gameState = new Label();

        gameState.setText("Game is in Session!");

        Canvas canvas = new Canvas(600,600);

        board = new GridData(20,20, 1, canvas.getGraphicsContext2D());

        root.add(canvas,0,0);

        root.add(gameState,0,1);

        main = new Scene(root,1250,1000); // Set a fixed size for the Application
        primaryStage.setResizable(false);

        Label score = new Label();
        score.setText("Current Snake Length: "+ board.getSnakeLength());

        root.add(score,2,0);

        /*
        Create a Controller unit for the layout that listens to 2 Key inputs:
        Left: to change the direction to the Left of the current direction
        Right: to change the direction to the Right of the current direction
         */
        main.setOnKeyPressed(event ->
        {
            /*
            If the time of the last execution of a step is smaller than the current time minus a delay constant,
            DO NOT execute this key command.
             */
            if (stepTiming < System.currentTimeMillis() - DELAY - 50)
            {
                switch (event.getCode())
                {
                    case LEFT:
                        direction--;
                        break;

                    case RIGHT:
                        direction++;
                        break;
                }
                /*
                If the direction has gone one full circle clockwise or counter-clockwise, re-initiate the direction
                as appropriate
                 */
                if (direction > 3)
                    direction = 0;

                else if (direction < 0)
                    direction = 3;

                stepTiming = System.currentTimeMillis(); // Update the timing of this execution
            }
        });

        primaryStage.setTitle("SERPENT");
        primaryStage.setScene(main);
        primaryStage.setResizable(false);
        primaryStage.show();

        timer = new Timer();

        /*
        Create a concurrent thread that execute and update the game Grid as well as ending such as necessary.
         */
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {

                board.determineMove(direction); // Using the user's input, determine the next state of the Snake
                try
                {
                    board.updateBoard(); // Update the board and its states
                    Platform.runLater(() ->
                            score.setText("Current Snake Length: "+ board.getSnakeLength())); // Update the score
                }
                /*
                If the Snake hits itself, GameOverException will be thrown
                If it hits a wall, ArrayIndexOutOfBoundsException will be thrown
                 */
                catch (GameOverException | ArrayIndexOutOfBoundsException e)
                {
                    timer.cancel();

                    Platform.runLater(() ->
                    {
                        /*
                        Display an Alert as well as changing a Label
                         */
                        gameState.setText("Game is Over!");
                        Alert overAlert = new Alert(Alert.AlertType.CONFIRMATION);

                        if (e instanceof  GameOverException)
                            overAlert.setHeaderText("You've hit yourself");
                        else
                            overAlert.setHeaderText("You've hit a Wall");

                        overAlert.setTitle("Game Over!");

                        overAlert.setContentText("Restart the Game?");
                        Optional<ButtonType> result = overAlert.showAndWait();

                        if (result.get() != ButtonType.OK)
                        {
                            Platform.exit();
                        }
                        else
                        {
                            // TODO: Find a way to restart the TimerTask

                            timer = new Timer();
                            timer.schedule(this,DELAY,DELAY);
                        }

                    });
                }
            }
        };

        timer.schedule(task,DELAY,DELAY);
    }
}

