import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GridData
{
    private BoardState[][] board;
    private GraphicsContext gc;
    private int snakeLength;
    private int[] bodyLocX;
    private int[] bodyLocY;
    private int locX;
    private int locY;
    private int prevLocX;
    private int prevLocY;
    private int foodLocX;
    private int foodLocY;
    public static final int DIMENSION = 30;

    public GridData(int width, int height, int defaultLength, GraphicsContext gc)
    {
        if (defaultLength < 1 || width < 2 || height < 2)
            throw new IllegalArgumentException();

        bodyLocX = new int[width*height];
        bodyLocY = new int[width*height];

        for (int i = 0; i < bodyLocX.length; i++)
        {
            bodyLocX[i] = -1;
            bodyLocY[i] = -1;
        }

        snakeLength = defaultLength;
        board = new BoardState[width][height];
        this.gc = gc;

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
        return snakeLength+1;
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
                gc.strokeRect(row*DIMENSION,column*DIMENSION,30,30);
                switch (board[row][column])
                {
                    case EMPTY:
                        gc.setFill(Color.WHITE);
                        gc.fillRect(row*DIMENSION,column*DIMENSION,30,30);
                        break;

                    case FOOD:
                        gc.setFill(Color.BLUE);
                        gc.fillRect(row*DIMENSION,column*DIMENSION,30,30);
                        break;

                    case BODY:
                        gc.setFill(Color.BLACK);
                        gc.fillRect(row*DIMENSION,column*DIMENSION,30,30);
                        break;

                    case HEAD:
                        gc.setFill(Color.RED);
                        gc.fillRect(row*DIMENSION,column*DIMENSION,30,30);
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
        foodLocX = (int) (Math.random()*board.length/2);
        foodLocY = (int) (Math.random()*board[0].length/2);

        /*
        If the spawn location of the food and the head are the same, change the location of the food
         */
        while (board[foodLocX][foodLocY] == BoardState.HEAD || board[foodLocX][foodLocY] == BoardState.BODY)
        {
            foodLocX = (int) (Math.random()*board.length/2);
            foodLocY = (int) (Math.random()*board[0].length/2);
        }
    }

}
