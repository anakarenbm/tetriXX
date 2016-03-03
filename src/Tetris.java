
import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Random;
import javax.swing.JFrame;

/**
 * The {@code Tetris} class is responsible for handling much of the game logic
 * and reading user input.
 *
 * @authors Sergio Diaz A01192313 Ana Karen Beltran A01192508
 *
 */
public class Tetris extends JFrame implements Serializable {

    /**
     * The Serial Version UID.
     */
    private static final long serialVersionUID = -4722429764792514382L;

    /**
     * The number of milliseconds per frame.
     */
    private static final long FRAME_TIME = 1000L / 50L;

    /**
     * The number of pieces that exist.
     */
    private static final int TYPE_COUNT = TileType.values().length;

    /**
     * The BoardPanel instance.
     */
    private BoardPanel board;

    /**
     * The SidePanel instance.
     */
    private SidePanel side;

    /**
     * Whether or not the game is paused.
     */
    private boolean isPaused;

    /**
     * Whether or not we've played a game yet. This is set to true initially and
     * then set to false when the game starts.
     */
    private boolean isNewGame;

    /**
     * Whether or not the game is over.
     */
    private boolean isGameOver;

    /**
     * The current level we're on.
     */
    private int level;

    /**
     * The current score.
     */
    private int score;

    /**
     * The random number generator. This is used to spit out pieces randomly.
     */
    private Random random;

    /**
     * The clock that handles the update logic.
     */
    transient private Clock logicTimer;

    /**
     * The current type of tile.
     */
    private TileType currentType;

    /**
     * The next type of tile.
     */
    private TileType nextType;

    /**
     * The current column of our tile.
     */
    private int currentCol;

    /**
     * Name of the file where the game's progress is saved.
     */
    private String nombreArchivo;    //Nombre del archivo.

    /**
     * The current row of our tile.
     */
    private int currentRow;

    /**
     * The current rotation of our tile.
     */
    private int currentRotation;

    /**
     * Ensures that a certain amount of time passes after a piece is spawned
     * before we can drop it.
     */
    private int dropCooldown;

    /**
     * The speed of the game.
     */
    private float gameSpeed;

    /**
     * Background Music.
     */
    transient private SoundClip souBackgroundB;

    /**
     * clockwise turn Music.
     */
    transient private SoundClip souTurnCW;

    /**
     * Counter clockwise turn Music.
     */
    transient private SoundClip souTurnCCW;

    /**
     * Counter clockwise turn Music.
     */
    private SoundClip souClick;

    /**
     * Counter clockwise turn Music.
     */
    private int iCounterAddedPiece;

    /**
     * Counter clockwise turn Music.
     */
    private SoundClip souLevelUp;

    /**
     * Creates a new Tetris instance. Sets up the window's properties, and adds
     * a controller listener.
     */
    private Tetris() {
        /*
		 * Set the basic properties of the window.
         */
        super("Tetris");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        /*
		 * Initialize the BoardPanel and SidePanel instances.
         */
        this.board = new BoardPanel(this);
        this.side = new SidePanel(this);

        /*
		 * Add the BoardPanel and SidePanel instances to the window.
         */
        add(board, BorderLayout.CENTER);
        add(side, BorderLayout.EAST);

        /*
		* Initialize Sound Clips 
         */
        souBackgroundB = new SoundClip("audio/BackgroundB.wav");
        souTurnCCW = new SoundClip("audio/TurnCCW.wav");
        souTurnCW = new SoundClip("audio/TurnCW.wav");
        souClick = new SoundClip("audio/click.wav");
        souLevelUp = new SoundClip("audio/LevelUp.wav");

        /*
	 * Initialize File Name
         */
        nombreArchivo = "LoadFile.dat";//nombre del archivo

        /*
	 * Initialize counter
         */
        iCounterAddedPiece = 0;

        /*
		 * Adds a custom anonymous KeyListener to the frame.
         */
        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {

                switch (e.getKeyCode()) {

                    /*
                    * Drop - When pressed, we check to see that the game is not
                    * paused and that there is no drop cooldown, then set the
                    * logic timer to run at a speed of 25 cycles per second.
                     */
                    case KeyEvent.VK_DOWN:
                        if (!isPaused && dropCooldown == 0) {
                            logicTimer.setCyclesPerSecond(25.0f);
                        }
                        break;

                    /*
                     * Move Left - When pressed, we check to see that the game 
                     * is not paused and that the position to the left of the 
                     * current position is valid. If so, we decrement 
                     * the current column by 1.
                     */
                    case KeyEvent.VK_LEFT:
                        if (!isPaused && board.isValidAndEmpty(currentType,
                                currentCol - 1, currentRow, currentRotation)) {
                            currentCol--;
                        }
                        break;

                    /**
                     * Move Right. When pressed, we check to see that the game
                     * is not paused and that the position to the right of the
                     * current position is valid. If so, we increment the
                     * current column by 1.
                     */
                    case KeyEvent.VK_RIGHT:
                        if (!isPaused && board.isValidAndEmpty(currentType,
                                currentCol + 1, currentRow, currentRotation)) {
                            currentCol++;
                        }
                        break;

                    /*
                     * Rotate Anticlockwise - When pressed, check to see that 
                     * the game is not paused and then attempt to rotate the 
                     * piece anticlockwise. Because of the size and
                     * complexity of the rotation code, as well as it's 
                     * similarity to clockwise rotation, the code for rotating 
                     * the piece is handled in another method.
                     */
                    case KeyEvent.VK_Z:
                        if (!isPaused) {
                            rotatePiece((currentRotation == 0) ? 3
                                    : currentRotation - 1);
                            souTurnCCW.play();
                        }
                        break;

                    /*
                     * Rotate Clockwise - When pressed, check to see that the 
                     * game is not paused and then attempt to rotate the piece 
                     * clockwise. Because of the size and complexity of the
                     * rotation code, as well as it's similarity to 
                     * anticlockwise rotation, the code for rotating the piece
                     * is handled in another method.
                     */
                    case KeyEvent.VK_X:
                        if (!isPaused) {
                            rotatePiece((currentRotation == 3) ? 0
                                    : currentRotation + 1);
                            souTurnCW.play();
                        }
                        break;

                    /*
                     * Pause Game - When pressed, check to see that we're 
                     * currently playing a game. If so, toggle the pause 
                     *variable and update the logic timer to reflect this
                     * change, otherwise the game will execute a huge number of 
                     * updates and essentially cause an instant game over when 
                     * we unpause if we stay paused for more than a
                     * minute or so.
                     */
                    case KeyEvent.VK_P:
                        if (!isGameOver && !isNewGame) {
                            isPaused = !isPaused;
                            logicTimer.setPaused(isPaused);

                            if (isPaused) {
                                souBackgroundB.stop();
                            } else {

                                souBackgroundB.play();
                            }
                        }
                        break;

                    /*
                     * Start Game - When pressed, check to see that we're in 
                     *either a game over or new
                     * game state. If so, reset the game.
                     */
                    case KeyEvent.VK_ENTER:
                        if (isGameOver || isNewGame) {
                            resetGame();
                        }
                        break;

                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

                switch (e.getKeyCode()) {

                    /*
                     * Drop - When released, we set the speed of the logic timer
                     * back to whatever the current game speed is and clear out
                     * any cycles that might still be elapsed.
                     */
                    case KeyEvent.VK_DOWN:
                        logicTimer.setCyclesPerSecond(gameSpeed);
                        logicTimer.reset();
                        break;
                    /*
                     * Save Game - When pressed, saves the current
                     *       process of the game.
                     */
                    case KeyEvent.VK_G:
                        try {
                            grabaArchivo();

                        } catch (IOException ex) {
                            System.out.println("Error en " + ex.toString());
                        }
                        break;

                    /*
                    * Load Game - When pressed, starts the game 
                        based on what is saved.
                     */
                    case KeyEvent.VK_C:
                        try {
                            leeArchivo();
                        } catch (IOException ex) {
                            System.out.println("Error en " + ex.toString());
                        }
                        break;

                }

            }

        });

        /**
         * Here we resize the frame to hold the BoardPanel and SidePanel
         * instances center the window on the screen, and show it to the user.
         */
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Starts the game running. Initializes everything and enters the game loop.
     */
    private void startGame() {
        /**
         * Initialize our random number generator, logic timer, and new game
         * variables.
         */

        this.random = new Random();
        this.isNewGame = true;
        this.gameSpeed = 1.0f;

        /**
         * Setup the timer to keep the game from running before the user presses
         * enter to start it.
         */
        this.logicTimer = new Clock(gameSpeed);
        logicTimer.setPaused(true);

        while (true) {
            //Get the time that the frame started.
            long start = System.nanoTime();

            if (iCounterAddedPiece <= 50) {

                if (iCounterAddedPiece == 50) {

                    iCounterAddedPiece = 0;
                    board.isAdded(false);
                }

            }

            iCounterAddedPiece++;

            //Update the logic timer.
            logicTimer.update();

            /*
            * If a cycle has elapsed on the timer, we can update the game and
             * move our current piece down.
             */
            if (logicTimer.hasElapsedCycle()) {
                updateGame();
            }

            //Decrement the drop cool down if necessary.
            if (dropCooldown > 0) {
                dropCooldown--;
            }

            //Display the window to the user.
            renderGame();

            /*
             * Sleep to cap the framerate.
             */
            long delta = (System.nanoTime() - start) / 1000000L;
            if (delta < FRAME_TIME) {
                try {
                    Thread.sleep(FRAME_TIME - delta);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Updates the game and handles the bulk of it's logic.
     */
    private void updateGame() {
        /**
         * Check to see if the piece's position can move down to the next row.
         */
        if (board.isValidAndEmpty(currentType, currentCol, currentRow + 1,
                currentRotation)) {
            //Increment the current row if it's safe to do so.
            currentRow++;
            //AQUI CAMBIAR EL COLOR 

        } else {
            /**
             * We've either reached the bottom of the board, or landed on
             * another piece, so we need to add the piece to the board.
             */
            board.addPiece(currentType, currentCol, currentRow,
                    currentRotation);

            //added piece image
            board.isAdded(true);

            /**
             * Check to see if adding the new piece resulted in any cleared
             * lines. If so,increase the player's score. (Up to 4 lines can be
             * cleared in a single go; [1 = 100pts, 2 = 200pts, 3 = 400pts, 4 =
             * 800pts]).
             */
            int cleared = board.checkLines();
            if (cleared > 0) {
                score += 50 << cleared;

                souLevelUp.play();
            }

            /**
             * Increase the speed slightly for the next piece and update the
             * game's timer to reflect the increase.
             */
            gameSpeed += 0.035f;
            logicTimer.setCyclesPerSecond(gameSpeed);
            logicTimer.reset();

            /**
             * Set the drop cooldown so the next piece doesn't automatically
             * come flying in from the heavens immediately after this piece hits
             * if we've not reacted yet. (~0.5 second buffer).
             */
            dropCooldown = 25;

            /**
             * Update the difficulty level. This has no effect on the game, and
             * is only used in the "Level" string in the SidePanel.
             */
            level = (int) (gameSpeed * 1.70f);

            /*
            * Spawn a new piece to control.
             */
            spawnPiece();

        }
    }

    /**
     * Forces the BoardPanel and SidePanel to repaint.
     */
    private void renderGame() {
        board.repaint();
        side.repaint();
    }

    /**
     * Resets the game variables to their default values at the start of a new
     * game.
     */
    private void resetGame() {
        this.level = 1;
        this.score = 0;
        this.gameSpeed = 1.0f;
        this.nextType = TileType.values()[random.nextInt(TYPE_COUNT)];
        this.isNewGame = false;
        this.isGameOver = false;
        board.clear();
        logicTimer.reset();
        logicTimer.setCyclesPerSecond(gameSpeed);
        spawnPiece();
        souBackgroundB.play();
        souBackgroundB.setLooping(true);
    }

    /**
     * Spawns a new piece and resets our piece's variables to their default
     * values.
     */
    private void spawnPiece() {
        
        /**
         * Poll the last piece and reset our position and rotation to their
         * default variables, then pick the next piece to use.
         */
        this.currentType = nextType;
        this.currentCol = currentType.getSpawnColumn();
        this.currentRow = currentType.getSpawnRow();
        this.currentRotation = 0;
        this.nextType = TileType.values()[random.nextInt(TYPE_COUNT)];
        souClick.play();

        /**
         * If the spawn point is invalid, we need to pause the game and flag
         * that we've lost because it means that the pieces on the board have
         * gotten too high.
         */
        if (!board.isValidAndEmpty(currentType, currentCol, currentRow,
                currentRotation)) {
            this.isGameOver = true;
            souBackgroundB.stop();
            logicTimer.setPaused(true);

        }
    }

    /**
     * Attempts to set the rotation of the current piece to newRotation.
     *
     * @param newRotation The rotation of the new peice.
     */
    private void rotatePiece(int newRotation) {
        /**
         * Sometimes pieces will need to be moved when rotated to avoid clipping
         * out of the board (the I piece is a good example of this). Here we
         * store temporary row and column in case we need to move the tile as
         * well.
         */
        int newColumn = currentCol;
        int newRow = currentRow;

        /**
         * Get the insets for each of the sides. These are used to determine how
         * many empty rows or columns there are on a given side.
         */
        int left = currentType.getLeftInset(newRotation);
        int right = currentType.getRightInset(newRotation);
        int top = currentType.getTopInset(newRotation);
        int bottom = currentType.getBottomInset(newRotation);

        /**
         * If the current piece is too far to the left or right, move the piece
         * away from the edges so that the piece doesn't clip out of the map and
         * automatically become invalid.
         */
        if (currentCol < -left) {
            newColumn -= currentCol - left;
        } else if (currentCol + currentType.getDimension() - right
                >= BoardPanel.COL_COUNT) {
            newColumn -= (currentCol + currentType.getDimension() - right)
                    - BoardPanel.COL_COUNT + 1;
        }

        /**
         * If the current piece is too far to the top or bottom, move the piece
         * away from the edges so that the piece doesn't clip out of the map and
         * automatically become invalid.
         */
        if (currentRow < -top) {
            newRow -= currentRow - top;
        } else if (currentRow + currentType.getDimension() - bottom
                >= BoardPanel.ROW_COUNT) {
            newRow -= (currentRow + currentType.getDimension() - bottom)
                    - BoardPanel.ROW_COUNT + 1;
        }

        /**
         * Check to see if the new position is acceptable. If it is, update the
         * rotation and position of the piece.
         */
        if (board.isValidAndEmpty(currentType, newColumn, newRow, newRotation)) 
                {
            currentRotation = newRotation;
            currentRow = newRow;
            currentCol = newColumn;
        }
    }

    /**
     * Checks to see whether or not the game is paused.
     *
     * @return Whether or not the game is paused.
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * Checks to see whether or not the game is over.
     *
     * @return Whether or not the game is over.
     */
    public boolean isGameOver() {
        return isGameOver;
    }

    /**
     * Checks to see whether or not we're on a new game.
     *
     * @return Whether or not this is a new game.
     */
    public boolean isNewGame() {
        return isNewGame;
    }

    /**
     * Gets the current score.
     *
     * @return The score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Gets the current level.
     *
     * @return The level.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Gets the current type of piece we're using.
     *
     * @return The piece type.
     */
    public TileType getPieceType() {
        return currentType;
    }

    /**
     * Gets the next type of piece we're using.
     *
     * @return The next piece.
     */
    public TileType getNextPieceType() {
        return nextType;
    }

    /**
     * Gets the column of the current piece.
     *
     * @return The column.
     */
    public int getPieceCol() {
        return currentCol;
    }

    /**
     * Gets the row of the current piece.
     *
     * @return The row.
     */
    public int getPieceRow() {
        return currentRow;
    }

    /**
     * Gets the rotation of the current piece.
     *
     * @return The rotation.
     */
    public int getPieceRotation() {
        return currentRotation;
    }

    /**
     * Metodo que lee a informacion de un archivo y lo agrega a un vector.
     *
     * @throws IOException
     */
    public void leeArchivo() throws IOException {
        RandomAccessFile finArchivo = new RandomAccessFile(nombreArchivo, "rw");

        this.level = finArchivo.readInt();
        this.score = finArchivo.readInt();
        this.currentCol = finArchivo.readInt();
        this.currentRow = finArchivo.readInt();
        this.currentRotation = finArchivo.readInt();
        this.currentType = TileType.values()[finArchivo.readInt()];
        this.nextType = TileType.values()[finArchivo.readInt()];
        this.gameSpeed = finArchivo.readFloat();
        this.isGameOver = finArchivo.readBoolean();
        this.isNewGame = finArchivo.readBoolean();

        logicTimer.reset();
        logicTimer.setCyclesPerSecond(gameSpeed);

        int i = finArchivo.readInt();
        int j = finArchivo.readInt();
        int matBoard[][] = new int[i][j];

        for (int iR = 0; iR < i; iR++) {
            for (int iC = 0; iC < j; iC++) {
                matBoard[iR][iC] = finArchivo.readInt();
            }
        }
        board.clear();
        board.setMatrix(matBoard);
        finArchivo.close();

    }

    /**
     * Metodo que agrega la informacion del vector al archivo.
     *
     * @throws IOException
     */
    public void grabaArchivo() throws IOException {

        RandomAccessFile fpwArchivo = new RandomAccessFile(nombreArchivo, "rw");

        fpwArchivo.writeInt(level);
        fpwArchivo.writeInt(score);
        fpwArchivo.writeInt(currentCol);
        fpwArchivo.writeInt(currentRow);
        fpwArchivo.writeInt(currentRotation);
        fpwArchivo.writeInt(currentType.getType());
        fpwArchivo.writeInt(nextType.getType());
        fpwArchivo.writeFloat(gameSpeed);
        fpwArchivo.writeBoolean(isGameOver);
        fpwArchivo.writeBoolean(isNewGame);

        int matStatus[][] = board.getMatrix();

        fpwArchivo.writeInt(matStatus.length);
        fpwArchivo.writeInt(matStatus[0].length);
        for (int iR = 0; iR < matStatus.length; iR++) {
            for (int iC = 0; iC < matStatus[0].length; iC++) {
                fpwArchivo.writeInt(matStatus[iR][iC]);
            }
        }

        fpwArchivo.close();
    }

    /**
     * Entry-point of the game. Responsible for creating and starting a new game
     * instance.
     *
     * @param args Unused.
     */
    public static void main(String[] args) {
        Tetris tetris = new Tetris();
        tetris.startGame();

    }

}
