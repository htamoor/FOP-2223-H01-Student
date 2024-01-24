package h01;

import fopbot.Direction;
import fopbot.Robot;
import fopbot.RobotFamily;
import fopbot.World;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.tudalgo.algoutils.student.Student;
import static org.tudalgo.algoutils.student.Student.crash;
import static org.tudalgo.algoutils.student.io.PropertyUtils.getIntProperty;

/**
 * {@link Checkers} is a simplified version of Checkers, implemented in FOPBot.
 */
public class Checkers {

    /**
     * The number of rows in the game board.
     */
    public static final int NUMBER_OF_ROWS = getIntProperty("checkers.properties", "NUMBER_OF_ROWS");

    /**
     * The number of columns in the game board.
     */
    public static final int NUMBER_OF_COLUMNS = getIntProperty("checkers.properties", "NUMBER_OF_COLUMNS");

    /**
     * The minimum initial number of coins for a black stone.
     */
    public static final int MIN_NUMBER_OF_COINS = getIntProperty("checkers.properties", "MIN_NUMBER_OF_COINS");

    /**
     * The maximum initial number of coins for a black stone.
     */
    public static final int MAX_NUMBER_OF_COINS = getIntProperty("checkers.properties", "MAX_NUMBER_OF_COINS");

    /**
     * The current state of the game.
     * At the start of the game, the state of the game is set to {@link GameState#RUNNING}.
     * After the game has finished, the state of the game is set to {@link GameState#BLACK_WIN} or {@link GameState#WHITE_WIN}.
     */
    private GameState gameState = GameState.RUNNING;


    /**
     * The robot of the white team.
     */
    private Robot whiteStone;

    /**
     * The robots of the black team.
     */
    private Robot blackStone0, blackStone1, blackStone2, blackStone3, blackStone4;

    /**
     * Runs the initialization of the game.
     * The initialization of the game consists of the initialization of the world and all stones.
     */
    public void initGame() {
        Student.setCrashEnabled(false);
        // initialize the world
        World.setSize(NUMBER_OF_COLUMNS, NUMBER_OF_ROWS);
        // initialize all stones
        initWhiteStone();
        initBlackStones();
    }

    /**
     * Runs the game. After the game has finished, the winner of the game will be printed to the console.
     */
    public void runGame() {
        World.setVisible(true);
        while (isRunning()) {
            doBlackTeamActions();
            doWhiteTeamActions();
            updateGameState();
        }
        System.out.printf("Final State: %s%n", gameState);
    }

    /**
     * Returns {@code true} if the game is running, {@code false} otherwise.
     *
     * @return if the game is running
     */
    public boolean isRunning() {
        return gameState == GameState.RUNNING;
    }

    /**
     * Runs the initialization of the white stone.
     */
    public void initWhiteStone() {
        whiteStone = createStone(RobotFamily.SQUARE_WHITE);
    }

    private Robot createStone(RobotFamily family) {
        int x, y;
        do {
            x = getRandom().nextInt(NUMBER_OF_COLUMNS);
            y = getRandom().nextInt(NUMBER_OF_ROWS);
        } while ((x + y) % 2 == 0 || (family == RobotFamily.SQUARE_BLACK && x == whiteStone.getX() && y == whiteStone.getY()));
        Direction direction = Direction.values()[getRandom().nextInt(4)];
        int coins = family == RobotFamily.SQUARE_BLACK ? getRandom().nextInt(MIN_NUMBER_OF_COINS, MAX_NUMBER_OF_COINS + 1) : 0;
        return new Robot(x, y, direction, coins, family);
    }

    private boolean isPositionInWorld(int x, int y) {
        return 0 <= x && x < NUMBER_OF_COLUMNS && 0 <= y && y < NUMBER_OF_ROWS;
    }


    /**
     * Runs the initialization of all black stones.
     */
    public void initBlackStones() {
        blackStone0 = createStone(RobotFamily.SQUARE_BLACK);
        blackStone1 = createStone(RobotFamily.SQUARE_BLACK);
        blackStone2 = createStone(RobotFamily.SQUARE_BLACK);
        blackStone3 = createStone(RobotFamily.SQUARE_BLACK);
        blackStone4 = createStone(RobotFamily.SQUARE_BLACK);
    }

    /**
     * Runs the action of the black team.
     */
    public void doBlackTeamActions() {
        Robot selectedRobot = null;
        while (selectedRobot == null || !selectedRobot.hasAnyCoins() || selectedRobot.isTurnedOff()) {
            int selectedNumber = ThreadLocalRandom.current().nextInt(5);
            selectedRobot = getBlackStone(selectedNumber);
        }
        selectedRobot.putCoin();
        int n;
        for (n = 0; n < 4; n++) {
            int nextX = selectedRobot.getX() + nextDeltaX(4 + n - selectedRobot.getDirection().ordinal());
            int nextY = selectedRobot.getY() + nextDeltaY(4 + n - selectedRobot.getDirection().ordinal());
            if (!isWhiteStonePosition(nextX, nextY) && isPositionInWorld(nextX, nextY)) {
                break;
            }
        }
        if (n == 0) {
            selectedRobot.move();
            selectedRobot.turnLeft();
            selectedRobot.turnLeft();
            selectedRobot.turnLeft();
            selectedRobot.move();
        } else if (n == 1) {
            selectedRobot.move();
            selectedRobot.turnLeft();
            selectedRobot.move();
        } else if (n == 2) {
            selectedRobot.turnLeft();
            selectedRobot.move();
            selectedRobot.turnLeft();
            selectedRobot.move();
        } else if (n == 3) {
            selectedRobot.turnLeft();
            selectedRobot.turnLeft();
            selectedRobot.move();
            selectedRobot.turnLeft();
            selectedRobot.move();
        }
    }

    private int nextDeltaX(int n) {
        n = n % 4;
        return n == 1 || n == 2 ? -1 : 1;
    }

    private int nextDeltaY(int n) {
        n = n % 4;
        return n == 2 || n == 3 ? -1 : 1;
    }




    /**
     * Runs the action of the white team.
     */
    public void doWhiteTeamActions() {
        int x = whiteStone.getX();
        int y = whiteStone.getY();
        Robot blackStone = null;
        int xD = 0;
        int yD = 0;
        for (int i = 0; i < 4 && blackStone == null; i++) {
            xD = i == 0 || i == 1 ? 1 : -1;
            yD = i == 0 || i == 3 ? 1 : -1;
            int xN = x + xD;
            int yN = y + yD;
            while (xN + xD >= 0 && xN + xD < NUMBER_OF_COLUMNS && yN + yD >= 0 && yN + yD < NUMBER_OF_ROWS) {
                blackStone = getBlackStone(xN, yN);
                if (blackStone != null) {
                    if (getBlackStone(xN + xD, yN + yD) != null) {
                        blackStone = null;
                    }
                    break;
                }
                xN += xD;
                yN += yD;
            }
        }
        if (blackStone != null) {
            whiteStone.setX(blackStone.getX() + xD);
            whiteStone.setY(blackStone.getY() + yD);
            blackStone.turnOff();
        }

    }

    /**
     * Checks if a team has won the game and, if so, updates the game state to {@link GameState#BLACK_WIN} or {@link GameState#WHITE_WIN}.
     */
    public void updateGameState() {
        boolean whiteCanWin = true, blackCanWin = true;
        for (int i = 0; i < 5 && (whiteCanWin || blackCanWin); i++) {
            Robot blackRobot = getBlackStone(i);
            if (blackRobot.isTurnedOn()) {
                whiteCanWin = false;
            }
            if (blackRobot.isTurnedOn() && blackRobot.hasAnyCoins()) {
                blackCanWin = false;
            }
            if (i == 4) {
                if (whiteCanWin) {
                    gameState = GameState.WHITE_WIN;
                } else if (blackCanWin) {
                    gameState = GameState.BLACK_WIN;
                }
            }
        }
    }

    /**
     * Returns an instance of {@link Random}.
     *
     * @return an instance of {@link Random}
     */
    private Random getRandom() {
        return ThreadLocalRandom.current();
    }

    private boolean isWhiteStonePosition(int x, int y) {
        return whiteStone.getX() == x && whiteStone.getY() == y;
    }

    private Robot getBlackStone(int number) {
        return switch (number) {
            case 0 -> blackStone0;
            case 1 -> blackStone1;
            case 2 -> blackStone2;
            case 3 -> blackStone3;
            case 4 -> blackStone4;
            default -> null;
        };
    }

    private Robot getBlackStone(int x, int y) {
        for (int i = 0; i < 5; i++) {
            Robot blackStone = getBlackStone(i);
            if (blackStone.isTurnedOn() && blackStone.getX() == x && blackStone.getY() == y) {
                return blackStone;
            }
        }
        return null;
    }


}


