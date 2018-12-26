import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Allows the construction of a (width)x(height) grid that spawns a Snake of (defaultLength).
 * The Grid must be updated each time a change to its states occurs by calling the updateBoard() method.
 *
 * States of the game:
 *          In Session: From start
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
 *          The object MUST be constructed, at minimum, a 2 x 2 grid.
 *
 * State expectations:
 *          Two arrays will be allocated for the location of each tile of the Snake. It is expected that unused memories
 *          be set (by default) to -1.
 *
 * @author Luan Ta
 * @version 1.0.0 Beta
 */
public class GridData
{
    private BoardState[][] board; // The 2D Grid of the game
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
    public static final int DIMENSION = 30; // The default Dimension of each Graphic object

    /**
     * Construct a Grid of the dimension width x height and spawn a Snake with the length of defaultLength
     * @param width the width of the Grid
     * @param height the height of the Grid
     * @param defaultLength the starting length of the Snake
     * @param gc the Graphics object to which this object will be displayed upon
     * @throws IllegalArgumentException if the defaultLength is less than 1 or if the width/height is less than 2
     */
    public GridData(int width, int height, int defaultLength, GraphicsContext gc)
    {
        /*
        The game requires AT LEAST a 2 x 2 grid to function
         */
        if (defaultLength < 1 || width < 2 || height < 2)
            throw new IllegalArgumentException();

        /*
        The maximum length for the Snake's body is the number of tiles of the Grid
         */
        bodyLocX = new int[width*height];
        bodyLocY = new int[width*height];

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
        board = new BoardState[width][height];
        this.gc = gc;

        /*
        Algorithm for the randomized spawning location of the Snake
         */
        locX =(int) (Math.random()*width/2)+width/4;
        locY =(int) (Math.random()*height/2)+height/4;

        prevLocX = -1;
        prevLocY = -1;

        newFood();

        updateBoard();
    }
    public void changeBoardState(int row, int col, BoardState newState)
    {
        if (row < 0 || col < 0 || newState == null)
            throw new IllegalArgumentException();

        board[row][col] = newState;
    }

    public int getLocX()
    {
        return locX;
    }

    public int getLocY()
    {
        return locY;
    }

    public int getSnakeLength()
    {
        return snakeLength;
    }

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
        }
    }

    public void updateBoard()
    {
        boolean growing = false;
        emptyBoard();

        gc.setStroke(Color.BLACK);

        bodyLocX[0] = prevLocX;
        bodyLocY[0] = prevLocY;

        if (foodLocX == locX && foodLocY == locY)
        {
            growing = true;

            for (int i = snakeLength + 1; i > 0; i--)
            {
                bodyLocX[i] = bodyLocX[i-1];
                bodyLocY[i] = bodyLocY[i-1];
            }

            snakeLength++;
        }
        else
        {
            for (int i = snakeLength; i > 0; i--)
            {
                bodyLocX[i] = bodyLocX[i-1];
                bodyLocY[i] = bodyLocY[i-1];
            }
        }

        for (int i = 0; i < bodyLocX.length; i++)
        {
            if (bodyLocX[i] != -1 && bodyLocY[i] != -1)
                board[bodyLocX[i]][bodyLocY[i]] = BoardState.BODY;
        }

        if (board[locX][locY] == BoardState.BODY)
            throw new GameOverException();

        /*
        Set the State for each block
         */
        board[locX][locY] = BoardState.HEAD;
        board[foodLocX][foodLocY] = BoardState.FOOD;
        prevLocX = locX;
        prevLocY = locY;

        if (growing)
        {
            board[foodLocX][foodLocY] = BoardState.HEAD;
            newFood();
        }

        for (int row = 0; row < board.length; row++)
        {
            for (int column = 0; column < board[row].length; column++)
            {
                gc.strokeRect(row*DIMENSION,column*DIMENSION,DIMENSION,DIMENSION);
                switch (board[row][column])
                {
                    case EMPTY:
                        gc.setFill(Color.WHITE);
                        gc.fillRect(row*DIMENSION,column*DIMENSION,DIMENSION,DIMENSION);
                        break;

                    case FOOD:
                        gc.setFill(Color.BLUE);
                        gc.fillRect(row*DIMENSION,column*DIMENSION,DIMENSION,DIMENSION);
                        break;

                    case BODY:
                        gc.setFill(Color.BLACK);
                        gc.fillRect(row*DIMENSION,column*DIMENSION,DIMENSION,DIMENSION);
                        break;

                    case HEAD:
                        gc.setFill(Color.RED);
                        gc.fillRect(row*DIMENSION,column*DIMENSION,DIMENSION,DIMENSION);
                        break;
                }
            }
        }
    }

    public void restart()
    {
        emptyBoard();
        newFood();
        locX =(int) (Math.random()*board.length/2)+board.length/4;
        locY =(int) (Math.random()*board[0].length/2)+board[0].length/4;
    }

    private void emptyBoard()
    {
        for (int row = 0; row < board.length; row++)
        {
            for (int column = 0; column < board[0].length; column++)
            {
                board[row][column] = BoardState.EMPTY;
            }
        }
    }

    private void newFood()
    {
        foodLocX = (int) (Math.random()*board.length);
        foodLocY = (int) (Math.random()*board[0].length);

        /*
        If the spawn location of the food and the head are the same, change the location of the food
         */
        while (board[foodLocX][foodLocY] == BoardState.HEAD || board[foodLocX][foodLocY] == BoardState.BODY)
        {
            foodLocX = (int) (Math.random()*board.length);
            foodLocY = (int) (Math.random()*board[0].length);
        }
    }

}
