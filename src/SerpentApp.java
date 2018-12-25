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

public class SerpentApp extends Application
{
    private Scene main; // The reference to the program's display
    private GridPane root;
    private GridData board;
    private long stepTiming;
    private int direction;
    private static final int STARTING_DIRECTION = 0; // LEFT
    public static final int DELAY = 300; // In milliseconds
    private Timer timer;
    private boolean inSession;

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        stepTiming = System.currentTimeMillis();
        direction = STARTING_DIRECTION;

        inSession = true;

        root = new GridPane();

        Label gameState = new Label();
        gameState.setText("Game is in Session!");

        Canvas canvas = new Canvas(600,600);

        board = new GridData(20,20, 1, canvas.getGraphicsContext2D());

        root.add(canvas,0,0);

        root.add(gameState,0,1);

        main = new Scene(root,1250,1000); // Set a fixed size for the Application
        primaryStage.setResizable(false);

        main.setOnKeyPressed(event ->
        {
            /*
            If the time of the last execution of a step is larger than the current time minus a delay constant,
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
                if (direction > 3)
                    direction = 0;

                else if (direction < 0)
                    direction = 3;

                stepTiming = System.currentTimeMillis();
            }
        });

        primaryStage.setTitle("SERPENT");
        primaryStage.setScene(main);
        primaryStage.setResizable(false);
        primaryStage.show();

        timer = new Timer();

        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                if (!inSession)
                    timer.cancel();

                board.determineMove(direction);
                try
                {
                    board.updateBoard();
                }
                catch (GameOverException | ArrayIndexOutOfBoundsException e)
                {
                    inSession = false;
                    Platform.runLater(() ->
                    {
                        gameState.setText("Game is Over!");
                        Alert overAlert = new Alert(Alert.AlertType.CONFIRMATION);

                        if (e instanceof  GameOverException)
                            overAlert.setHeaderText("You've hit yourself");
                        else
                            overAlert.setHeaderText("You've hit a Wall");

                        overAlert.setTitle("Game Over!");

                        overAlert.setContentText("Restart the Game?");
                        Optional<ButtonType> result = overAlert.showAndWait();
                        if (result.get() == ButtonType.OK)
                        {
                            restartTimer();


                            gameState.setText("Game is in Session");
                        }
                        else
                        {

                        }
                    });
                }
            }
        };

        timer.schedule(task,DELAY,DELAY);
    }

    private void restartTimer()
    {
        timer = new Timer();
    }
}
