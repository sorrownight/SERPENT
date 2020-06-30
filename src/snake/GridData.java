package snake;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Allows the construction of a (width)x(height) grid that spawns a Snake of (defaultLength).
 * The Grid must be updated each time a change to its states occurs by calling the updateBoard() method.
 *
 * States of the game:
 *          In Session: From start
 *          User input: Call determineMove() to change the direction of the Snake
 *          Game Over: When the Snake hits itself or when it hits a Wall (an exception will be thrown for each case)
 *
 * Expectation when the game ends: no more calls to the updateBoard() method should be made
 * Expectation when wanting to restart the game: a call to the restart() method should be made
 *
 * Features:
 *          Possible queries to the length of the Snake (not counting the head) and to the location of the head can be
 *          made.
 *          The Food and Snake's spawning locations are randomized. The Food's spawning location is not restricted
 *          except that it cannot spawn ON the Snake. The Snake's spawning location is restricted to the center tiles of
 *          the Grid.
 *
 * Restrictions:
 *          This class does not allow direct mutations to its states. All changes will occur within the Model
 *          as to accredit the MVC pattern.
 *          The object MUST be constructed, at minimum, a grid bigger than the the square of the defaultLength
 *
 * State expectations:
 *          Two arrays will be allocated for the location of each tile of the Snake. It is expected that unused memories
 *          be set (by default) to -1.
 *
 * @author Luan Ta
 * @version 1.3.0 Beta
 */
public class GridData
{
    private int width;
    private int height;
    private GraphicsContext gc;
    private int snakeLength; // The length of the Snake (not counting the Head)
    private int[] bodyLocX; // The location of each tile of the the Snake's body
    private int[] bodyLocY;
    private int locX; // Head location
    private int locY;
    private int prevLocX; // The previous location of the Head after one execution cycle
    private int prevLocY;
    private int foodLocX; // The location of the Food
    private int foodLocY;
    private int scoreLeft;
    private boolean inSession;
    static final int DIMENSION = 30; // The default Dimension of each Graphic object
    private static final boolean GOD_MODE = false; // Will disable ALL Collision logic if set to 'true'

    /**
     * Construct a Grid of the dimension width x height and spawn a Snake with the length of defaultLength
     * @param width the width of the Grid
     * @param height the height of the Grid
     * @param defaultLength the starting length of the Snake
     * @param gc the Graphics object to which this object will be displayed upon
     * @throws IllegalArgumentException if the defaultLength is less than 1 or if the width/height are too small
     *                                  in regards to the defaultLength
     */
    public GridData(int width, int height, int defaultLength, GraphicsContext gc)
    {
        /*
        The game requires AT LEAST a grid 2 units bigger than the default length of the Snake
         */
        if (defaultLength < 1)
            throw new IllegalArgumentException();
        
        this.width = width;
        this.height = height;
        this.inSession = true;


        /*
        The maximum length for the Snake's body is the number of tiles of the Grid
         */
        bodyLocX = new int[width*height];
        bodyLocY = new int[width*height];
        scoreLeft = width*height - 1 - defaultLength;

        /*
        Initialize the arrays
         */
        for (int i = 0; i < bodyLocX.length; i++)
        {
            bodyLocX[i] = -1;
            bodyLocY[i] = -1;
        }

        /*
        Initialize the instance variables
         */
        snakeLength = defaultLength;
        this.gc = gc;

        /*
        Algorithm for the randomized spawning location of the Snake
         */
        locX =(int) (Math.random()*width/2)+width/4;
        locY =(int) (Math.random()*height/2)+height/4;

        prevLocX = -1;
        prevLocY = -1;

        emptyBoard();

        newFood();

        updateBoard();
    }

    public int getScoreLeft()
    {
        return scoreLeft;
    }

    public int getSnakeLength()
    {
        return snakeLength;
    }

    /**
     * Determine where the Snake would move next the next time updateBoard() is called
     * @param direction
     *          0: Left
     *          1: Up
     *          2: Right
     *          3: Down
     * @throws IllegalArgumentException if the parameter is not one of the options above
     */
    public void determineMove(int direction)
    {
         /*
         0: Left
         1: Up
         2: Right
         3: Down
          */

        switch (direction)
        {
            case 0:
                locX--;
                break;
            case 2:
                locX++;
                break;
            case 1:
                locY--;
                break;
            case 3:
                locY++;
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Update the current states of the Object. This method is expected to be executed in
     * each time a state is expected to be changed.
     */
    public void updateBoard()
    {

        /*
        For each execution of the method, move the head one Node toward the current direction
        and move each Node of the Snake's body to the previous location of the Node in front
        of it.
         */

        bodyLocX[0] = prevLocX; // update the body location to the previously saved location
        bodyLocY[0] = prevLocY;

        /*
        If the location of the Snake matches the location of a food Node, the Snake has consumed
        the food. Update the Snake's length accordingly.
         */
        if (foodLocX == locX && foodLocY == locY)
        {
            for (int i = snakeLength + 1; i > 0; i--)
            {
                bodyLocX[i] = bodyLocX[i-1];
                bodyLocY[i] = bodyLocY[i-1];
            }

            /*
            If the Snake has consumed a food Node, spawn a new food and update the Head's location
            to the old food's location
             */

            scoreLeft--;
            draw(foodLocX,foodLocY,Color.RED);
            newFood();

            snakeLength++;
        }

        else
        {
            for (int i = snakeLength; i > 0; i--)
            {
                if (i == snakeLength)
                    draw(bodyLocX[snakeLength],bodyLocY[snakeLength],Color.WHITE);

                bodyLocX[i] = bodyLocX[i-1];
                bodyLocY[i] = bodyLocY[i-1];
            }
        }

        /*
        Draw the body and if the head's location matches a body location, a collision between
        the head and the body happens thus ending the game.
         */
        for (int i = 0; i < snakeLength; i++)
        {
            if (bodyLocX[i] != -1 && bodyLocY[i] != -1)
                draw(bodyLocX[i],bodyLocY[i],Color.GREEN);

            if (bodyLocX[i] == locX && bodyLocY[i] == locY && !GOD_MODE)
                inSession = false;
        }

        /*
        Draw the head and update the previous locations
         */
        draw(locX,locY,Color.RED); // Draw the Head
        prevLocX = locX;
        prevLocY = locY;

        /*
        If the head collides with a Wall, end the game
         */
        if ((locX >= width || locY >= height || locX < 0 || locY < 0) && !GOD_MODE)
            inSession = false;

        draw(foodLocX,foodLocY,Color.BLUE); // So that the food wouldn't get overridden

    }

    public boolean isInSession()
    {
        return inSession;
    }

    /**
     * Restart the board and spawn a new Snake
     */
    public void restart()
    {
        bodyLocX = new int[width*height]; // Empty the array
        bodyLocY = new int[width*height];
        emptyBoard(); // Empty the board
        newFood(); // Create a new food
        locX =(int) (Math.random()*width/2)+width/4; // Spawn a new head
        locY =(int) (Math.random()*height/2)+height/4;
        inSession = true;
        prevLocX = -1;
        prevLocY = -1;
        snakeLength = 1;

    }

    private void emptyBoard()
    {
        for (int row = 0; row < width; row++)
        {
            for (int column = 0; column < height; column++)
                draw(row,column,Color.WHITE);
        }
    }

    private void newFood()
    {
        foodLocX = (int) (Math.random()*width);
        foodLocY = (int) (Math.random()*height);

        /*
        If the spawn location of the food and the head are the same, change the location of the food
         */
        for (int i = 0; i < snakeLength; i++)
        {
            if ((foodLocX == bodyLocX[i] && foodLocY == bodyLocY[i]) ||
                    (foodLocX == locX && foodLocY == locY))
            {
                System.out.println("Food: " + foodLocX + " " + foodLocY);
                newFood();
            }
        }


    }

    private void draw(int x, int y, Color c)
    {
        gc.setFill(c);
        gc.fillRect(x*DIMENSION,y*DIMENSION,DIMENSION,DIMENSION);
    }
}
