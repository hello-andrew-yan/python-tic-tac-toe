////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//																													  //
//  									[			Agent.java				]										  //
// 																													  //
//  										Nine-Board Tic-Tac-Toe Agent											  //
//  									COMP3411/9814 Artificial Intelligence										  //
// 																													  //
//  									[ 			 CSE, UNSW				]										  //
//																													  //
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class Robot {

    static final int[][] boards = new int[10][10];
    static int previous_board = 0;

    static final int EMPTY_CELL = 0;
    static final int AGENT_MARK = 1;
    static final int PLAYER_MARK = 2;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                      UNSW UTILITY FUNCTION | IGNORE                                                //
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java Agent -p (port)");
            return;
        }

        final String host = "localhost";
        final int portNumber = Integer.parseInt(args[1]);

        Socket socket = new Socket(host, portNumber);
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        String line;

        while (true) {
            line = br.readLine();
            int move = parse(line);

            if (move == -1) {
                socket.close();
                return;
            } else if (move == 0) {
                // No action, could output a debug message if we wanted
            } else {
                out.println(move);
            }
        }
    }

    /**
     * From the server, init() tells us that a new game is about to begin.
     * <p>
     * start(x) or start(o) tell us whether we will be playing first (x)
     * or second (o); we might be able to ignore start if we internally
     * use 'X' for *our* moves and 'O' for *opponent* moves.
     *
     * @param line Command line string
     * @return Number state depending on the command.
     */
    public static int parse(String line) {
        if (line.contains("init")) {
            // No action, could output a debug message if we wanted
        } else if (line.contains("start")) {
            // No action, could output a debug message if we wanted
        } else if (line.contains("second_move")) {
            int argsStart = line.indexOf("(");
            int argsEnd = line.indexOf(")");

            String list = line.substring(argsStart + 1, argsEnd);
            String[] numbers = list.split(",");

            // Place the first move (randomly generated for opponent)
            place(Integer.parseInt(numbers[0]), Integer.parseInt(numbers[1]), PLAYER_MARK);

            // Choose and return the second move
            return play();
        } else if (line.contains("third_move")) {
            int argsStart = line.indexOf("(");
            int argsEnd = line.indexOf(")");

            String list = line.substring(argsStart + 1, argsEnd);
            String[] numbers = list.split(",");

            // Place the first move (randomly generated for us) and second move (chosen by
            // opponent)
            place(Integer.parseInt(numbers[0]), Integer.parseInt(numbers[1]), AGENT_MARK);
            place(Integer.parseInt(numbers[1]), Integer.parseInt(numbers[2]), PLAYER_MARK);

            // Choose and return the third move
            return play();
        } else if (line.contains("next_move")) {
            int argsStart = line.indexOf("(");
            int argsEnd = line.indexOf(")");

            String list = line.substring(argsStart + 1, argsEnd);

            // Place the previous move (chosen by opponent)
            place(previous_board, Integer.parseInt(list), PLAYER_MARK);

            // Choose and return the next move
            return play();
        } else if (line.contains("last_move")) {
            // No action, could output a debug message if we wanted
        } else if (line.contains("win")) {
            // No action, could output a debug message if we wanted
        } else if (line.contains("loss")) {
            // No action, could output a debug message if we wanted
        } else if (line.contains("end")) {
            return -1;
        }
        return 0;
    }

    public static void place(int board, int index, int mark) {
        previous_board = index;
        boards[board][index] = mark;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                          I M P L E M E N T A T I O N                                               //
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static int plays = 0;
    static int activeDepth = 5;

    public static int play() {
        plays++;
        if (plays % 3 == 0 && activeDepth < MAXIMUM_DEPTH) {
            activeDepth++;
        }
        int bestMove = findBestMove();
        place(previous_board, bestMove, AGENT_MARK);

        return bestMove;
    }

    //////////////////////////////////////////////////////////
    //                   FIND BEST MOVE                     //
    //////////////////////////////////////////////////////////
    /**
     * Finds the best moves out of all the cells available
     * in the current board the game has assigned to the agent.
     * <p>
     * This function runs in O(b^m) time. Where b = 9 in a game
     * of 9-Board Tic-Tac-Toe and m = MAXIMUM_DEPTH.
     *
     * @return The index of the best move to
     *         go in this particular scenario
     */
    public static int findBestMove() {
        int bestScore = Integer.MIN_VALUE;
        int bestMove = 0;

        // Important to create a copy as we wil be testing a variety of local moves
        int[][] copiedBoards = copyBoards(boards);

        // Iterations must be indexed at 1 - 9.
        for (int i = 1; i < 10; i++) {
            // Skips cells that already have values set
            if (copiedBoards[previous_board][i] != EMPTY_CELL) continue;

            // The first layer of the Negamax Algorithm is called here, committing agent moves before checking player moves.
            copiedBoards[previous_board][i] = AGENT_MARK;

            // DEBUG
            // System.out.printf("Depth = \u001B[32m0\u001B[0m\n");
            // printBoard(copiedBoards[previous_board]);

            int score = negamax(copiedBoards, i, STARTING_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, PLAYER_MARK);
            copiedBoards[previous_board][i] = EMPTY_CELL;

            if (score > bestScore) {
                bestScore =  score;
                bestMove = i;
            }
        }

        return bestMove;
    }

    //////////////////////////////////////////////////////////
    //                 NEGAMAX ALGORITHM                    //
    //////////////////////////////////////////////////////////

    static final int STARTING_DEPTH = 0;
    static final int MAXIMUM_DEPTH = 15;

    public static int negamax(int[][] boards, int next_board, int depth, int alpha, int beta, int mark) {

        int[][] copiedBoards = copyBoards(boards);

        // Early detections of wins scaled by the depth.
        if (isWinning(copiedBoards[next_board], mark)) return -(1000000 * (MAXIMUM_DEPTH + 1 - depth));

        // Early detection of ties.
        if (isTied(copiedBoards[next_board])) return 1000;

        if (depth == activeDepth) {
            // Returns the score of the entire board back to higher depths.
            int score = 0;
            for (int i = 1; i < 10; i++) {
                /*
                 * 	  "Negamax can be implemented without the color parameter. In this case, the heuristic
                 * evaluation function must return values from the point of view of the node's current player"
                 *
                 * 						https://en.wikipedia.org/wiki/Negamax
                 *
                 * This is why "mark" is being used as a parameter in the evaluateBoard(...) function.
                 */
                score += evaluateBoard(copiedBoards[i], mark);
            }
            return -score;
        }

        int bestScore = Integer.MIN_VALUE;
        // Iterations must be indexed at 1 - 9.
        for (int i = 1; i < 10; i++) {
            // Skips cells that already have values set
            if (copiedBoards[next_board][i] != EMPTY_CELL) continue;

            copiedBoards[next_board][i] = mark;

            // DEBUG
            // System.out.printf("Depth = \u001B[32m%s\u001B[0m\n", depth + 1);
            // printBoard(copiedBoards[next_board]);

            int score = -negamax(copiedBoards, i, depth + 1, -beta, -alpha, mark == AGENT_MARK ? PLAYER_MARK : AGENT_MARK);


            // DEBUG
            // System.out.printf(" v\n");
            // // printBoard(copiedBoards[i]);
            // printBoards(copiedBoards);
            // System.out.printf("  = " + (-score >= 0 ? "\u001B[32m" : "\u001B[31m") + "%s FOR AGENT\u001B[0m\n\n", -score);


            copiedBoards[next_board][i] = EMPTY_CELL;

            bestScore = Math.max(score, bestScore);

            // Alpa-Beta Pruning (Comment out during functionality testing)
            alpha = Math.max(alpha, score);
            if (alpha >= beta) {
                break;
            }
        }
        return bestScore;
    }

    //////////////////////////////////////////////////////////
    //                EVAULATION FUNCTION                   //
    //////////////////////////////////////////////////////////

    // Evaluates how optimal the board is for the AGENT.

    /**
     * Heuristic is based on how many advantageous positions
     * the agent has on the board. These include having two in
     * a rows and having an advantageous position on the board.
     *
     * The reason why three in a rows are not considered is that
     * the negamax algorithm already has a terminal condition to
     * detect that already.
     */
    public static int evaluateBoard(int[] board, int mark) {
        int score = 0;
        for (int[] combos : WINNING_COMBINATIONS) score += calculatePositionalScore(board, combos, mark);
        return score;
    }

    public static int calculatePositionalScore(int[] board, int[] sequence, int mark) {
        // Validation checking for correct Tic-Tac-Toe sequences.
        if (sequence.length < 3) return 0;

        int opponent = mark == AGENT_MARK ? PLAYER_MARK : AGENT_MARK;

        int currentPlacements = 0;
        int opponentPlacements = 0;
        int positionalBonus = 1;

        for (int i = 0; i < 3; i++) {
            if (board[sequence[i]] == mark) currentPlacements++;
            if (board[sequence[i]] == opponent) opponentPlacements++;

            // Positional advantage.
            if (board[sequence[i]] != EMPTY_CELL) {
                positionalBonus *= POSITIONAL_SCORES[sequence[i]];
            }
        }

        // Opponent wins.
        if (opponentPlacements == 3) return -1000 * positionalBonus;

        // Current wins.
        if (currentPlacements == 3) return 1000 * positionalBonus;

        // Opponent has a two in a row, opponent may also have a positional advantage.
        if (currentPlacements == 0 && opponentPlacements == 2) {
            return -30 * positionalBonus;
        }

        // Current has a two in a row, current may also have a positional advantage.
        if (currentPlacements == 2 && opponentPlacements == 0) {
            return 30 * positionalBonus;
        }

        // Opponent holds one position, opponent may also have a positional advantage.
        if (currentPlacements == 0 && opponentPlacements == 1) {
            return -1 * positionalBonus;
        }

        // Current holds one position, current may also have a positional advantage.
        if (currentPlacements == 1 && opponentPlacements == 0) {
            return 1 * positionalBonus;
        }

        // Neutral position.
        return 0;
    }



    //////////////////////////////////////////////////////////
    //                  UTILITY FUNCTIONS                   //
    //////////////////////////////////////////////////////////

    // Center square and corner squares are more valuable.
    static final int[] POSITIONAL_SCORES = {-1,
            2, 1, 2,
            1, 5, 1,
            2, 1, 2
    };

    static final int[][] WINNING_COMBINATIONS = {
            {1, 2, 3}, {4, 5, 6}, {7, 8, 9},  // Horizontal
            {1, 4, 7}, {2, 5, 8}, {3, 6, 9},  // Vertical
            {1, 5, 9}, {3, 5, 7}              // Diagonal
    };

    public static boolean isWinning(int[] board, int mark) {
        // Iterate over the winning combinations
        for (int[] combo : WINNING_COMBINATIONS) {
            // Check if the values at the positions in the combo are the same and not empty.
            if (board[combo[0]] == mark && board[combo[1]] == mark && board[combo[2]] == mark) {
                return true;
            }
        }
        return false;
    }

    public static boolean isTied(int[] board) {
        for (int i = 1; i < 10; i++) if (board[i] == EMPTY_CELL) return false;
        return true;
    }

    public static int countDoubles(int[] board, int mark) {
        int count = 0;
        // Iterate over the winning combinations
        for (int[] combo : WINNING_COMBINATIONS) {
            // (_ X X) CHECKS
            if (board[combo[0]] == EMPTY_CELL && board[combo[1]] == mark && board[combo[2]] == mark) count++;

            // (X _ X) CHECKS
            if (board[combo[0]] == mark && board[combo[1]] == EMPTY_CELL && board[combo[2]] == mark) count++;

            // (X X _) CHECKS
            if (board[combo[0]] == mark && board[combo[1]] == mark && board[combo[2]] == EMPTY_CELL) count++;
        }
        return count;
    }


    // Might be able to merge functionality with countWinningOpportunities(...) and avoid duplicates.
    public static int countSingles(int[] board, int mark) {
        int count = 0;
        // Iterate over the winning combinations
        for (int[] combo : WINNING_COMBINATIONS) {
            // (X _ _) CHECKS
            if (board[combo[0]] == mark && board[combo[1]] == EMPTY_CELL && board[combo[2]] == EMPTY_CELL) count++;

            // (_ X _) CHECKS
            if (board[combo[0]] == EMPTY_CELL && board[combo[1]] == mark && board[combo[2]] == EMPTY_CELL) count++;

            // (_ _ X) CHECKS
            if (board[combo[0]] == EMPTY_CELL && board[combo[1]] == EMPTY_CELL && board[combo[2]] == mark) count++;
        }
        return count;
    }

    public static int[][] copyBoards(int[][] original) {
        return Arrays.stream(original).map(int[]::clone).toArray(int[][]::new);
    }

    //////////////////////////////////////////////////////////
    //                  DEBUG FUNCTIONS                     //
    //////////////////////////////////////////////////////////

    // Prints a singlular board with pretty colours ^-^

    static String[] prints = {".", "\u001B[32mO\u001B[0m", "\u001B[31mX\u001B[0m"};

    public static void printBoard(int[] board) {
        for (int i = 1; i < 10; i++) {
            System.out.print(prints[board[i]] + " ");
            if (i % 3 == 0) System.out.println();
        }
    }

    public static void printBoards(int[][] boards) {
        for (int i = 1; i < 10; i++) {
            printBoard(boards[i]);
            System.out.print("------\n");
        }
    }
}
