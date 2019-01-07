import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple Snake game written using the application of JavaFX
 *
 * @author Luan Ta
 * @version 1.2.1 Beta
 */
public class SerpentApp extends Application
{
    private GridData board; // The Model of this program. Holds the states of the game.
    private long stepTiming; // The timing between each execution. Intended to restrict/delay the input of another key
    private int direction; // Based on the input of the user, determine the direction of the Snake
    private static final int STARTING_DIRECTION = 0; // Starting from the Left. See (*) below
    public static final int DELAY = 100; // In milliseconds
    public static final int paneHeight = 720;
    public static final int paneWidth = 1250;
    public static final int boardWidth = 20;
    public static final int boardHeight = 20;
    public static final int defaultLength = 1;


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

        GridPane root = new GridPane();

        root.setHgap(30);
        root.setVgap(30);

        root.setPadding(new Insets(10, 10, 10, 10));

        root.setStyle("-fx-background-color: #000000;"); // Set the background color for the window

        int height = boardHeight*GridData.DIMENSION*defaultLength;
        int width = boardWidth*GridData.DIMENSION*defaultLength;

        if (height >= paneHeight || width >= paneWidth)
            throw new IllegalStateException("Check constants!!");

        Canvas canvas = new Canvas(width,height);

        board = new GridData(20,20, 1, canvas.getGraphicsContext2D());

        root.add(canvas,1,1);

        // The reference to the program's display
        Scene main = new Scene(root, paneWidth, paneHeight, Color.BLACK);

        primaryStage.setResizable(false); // Disable resizing

        Label score = new Label();
        score.setText("Current Snake Length: "+ board.getSnakeLength());
        score.setTextFill(Color.RED);

        Label gameState = new Label();
        gameState.setText("Game is in Session!");
        gameState.setTextFill(Color.RED);

        root.add(score,0,2);
        root.add(gameState,0,0);

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
            if (stepTiming < System.currentTimeMillis() - DELAY/10)
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

        primaryStage.setOnCloseRequest(event ->
        {
            Platform.exit();
            timer.cancel();
        }); // Close the window and cancel the timer


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
                    Platform.runLater(() ->{
                                score.setText("Current Snake Length: "+ board.getSnakeLength());
                                /*
                                If there is no more node left in board, indicate that the player has won
                                 */
                                if (board.getScoreLeft() == 0)
                                {
                                    Alert winAlert = new Alert(Alert.AlertType.CONFIRMATION);
                                    winAlert.setTitle("You have won");
                                    winAlert.setContentText("Would you like to restart?");
                                    Optional<ButtonType> winAlertButton = winAlert.showAndWait();

                                    if (winAlertButton.get() == ButtonType.OK)
                                    {
                                        // TODO: restart insert here
                                    }
                                    else
                                    {
                                        Platform.exit();
                                    }

                                }
                            }); // Update the score
                }
                /*
                If the Snake hits itself, GameOverException will be thrown
                If it hits a wall, ArrayIndexOutOfBoundsException will be thrown
                 */
                catch (GameOverException e)
                {
                    timer.cancel();

                    Platform.runLater(() ->
                    {
                        /*
                        Display an Alert as well as changing a Label
                         */
                        gameState.setText("Game is Over!");
                        Alert overAlert = new Alert(Alert.AlertType.CONFIRMATION);

                        if (e instanceof SelfCollisionException)
                            overAlert.setHeaderText("You've hit yourself");
                        else
                            overAlert.setHeaderText("You've hit a Wall");

                        overAlert.setTitle("Game Over!");

                        overAlert.setContentText("Restart the Game?");
                        Optional<ButtonType> result = overAlert.showAndWait();

                        if (result.get() != ButtonType.OK)
                            Platform.exit();

                        else
                        {
                            // TODO: Find a way to restart the TimerTask

                            timer = new Timer();
                            // timer.schedule(this,DELAY,DELAY);
                        }

                    });
                }
            }
        };

        timer.schedule(task,DELAY,DELAY);
    }
}

