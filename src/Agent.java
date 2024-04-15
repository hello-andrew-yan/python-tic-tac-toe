/*********************************************************
 *  Agent.java
 *  Nine-Board Tic-Tac-Toe Agent
 *  COMP3411/9814 Artificial Intelligence
 *  CSE, UNSW
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.Arrays;

public class Agent {

    static int previousBoard = 0;

    // TODO - Delete this later.
    static Random rand = new Random();

    //////////////////////////////////////////////////////////
    //             UNSW UTILITY FUNCTION | IGNORE           //
    //////////////////////////////////////////////////////////

    public static void main(String args[]) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java Agent -p (port)");
            return;
        }

        final String host = "localhost";
        final int portNumber = Integer.parseInt(args[1]);

        Socket socket = new Socket(host, portNumber);
        BufferedReader br = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        String line;

        while (true) {
            line = br.readLine();
            int move = parse(line);

            if (move == -1) {
                socket.close();
                return;
            } else if (move == 0) {
                // TODO - Currently does nothing?
            } else {
                out.println(move);
            }
        }
    }


    //////////////////////////////////////////////////////////
    //             UNSW UTILITY FUNCTION | IGNORE           //
    //////////////////////////////////////////////////////////
    /*
     * From the server, init() tells us that a new game is about to begin.
     * start(x) or start(o) tell us whether we will be playing first (x)
     * or second (o); we might be able to ignore start if we internally
     * use 'X' for *our* moves and 'O' for *opponent* moves.
     */
    public static int parse(String line) {
        if( line.contains("init")) {
            // No action
        } else if( line.contains("start")) {
            // No action
        } else if( line.contains("second_move")) {
            int argsStart = line.indexOf("(");
            int argsEnd = line.indexOf(")");

            String list = line.substring(argsStart+1, argsEnd);
            String[] numbers = list.split(",");

            // Place the first move (randomly generated for opponent)
            place( Integer.parseInt(numbers[0]),
                    Integer.parseInt(numbers[1]), 2 );

            // Choose and return the second move
            return play();
        } else if( line.contains("third_move")) {
            int argsStart = line.indexOf("(");
            int argsEnd = line.indexOf(")");

            String list = line.substring(argsStart+1, argsEnd);
            String[] numbers = list.split(",");

            // Place the first move (randomly generated for us)
            place(Integer.parseInt(numbers[0]),
                    Integer.parseInt(numbers[1]), 1);

            // Place the second move (chosen by opponent)
            place(Integer.parseInt(numbers[1]),
                    Integer.parseInt(numbers[2]), 2);

            // Choose and return the third move
            return play();
        }  else if (line.contains("next_move")) {
            int argsStart = line.indexOf("(");
            int argsEnd = line.indexOf(")");

            String list = line.substring(argsStart + 1, argsEnd);

            // Place the previous move (chosen by opponent)
            place(previousBoard, Integer.parseInt(list), 2);

            // Choose and return the next move
            return play();
        } else if (line.contains("last_move")) {
            // No action
        } else if (line.contains("win")) {
            // No action
        } else if (line.contains("loss")) {
            // No action
        } else if(line.contains("end")) {
            return -1;
        }
        return 0;
    }

    public static void place(int board, int index, int mark) {
        previousBoard = index;
        global_boards[board][index] = mark;
    }

    //////////////////////////////////////////////////////////
    //                   IMPLEMENTATION                     //
    //////////////////////////////////////////////////////////

    public static int play() {
        int bestMove = findBestMove();
        System.out.printf("\u001B[32mReturned optimal move %s\u001B[0m\n", bestMove);

        place(previousBoard, bestMove, AGENT_MARK);
        return bestMove;
    }

    //////////////////////////////////////////////////////////
    //              FIND BEST MOVE ALGORITHM                //
    //////////////////////////////////////////////////////////

    static int[][] global_boards = new int[10][10];

    static final int EMPTY_CELL = 0;
    static final int AGENT_MARK = 1;
    static final int PLAYER_MARK = 3;

    static final int MINIMUM_SCORE = -1000000;
    static final int MAXIMUM_SCORE = 1000000;

    static final int MAXIMUM_DEPTH = 2;

    public static int findBestMove() {
        int bestScore = MINIMUM_SCORE;
        // Placeholder variable initalisation.
        int bestMove = 0;

        int[][] copy = copyBoard(global_boards);

        for (int i = 1; i < 10; i++) {
            // Skips any cells that contain committed moves.
            if (copy[previousBoard][i] != EMPTY_CELL) continue;

            // DEBUG STATEMENT
            System.out.printf(
                    "Calculating optimality score for " +
                            "global_boards[\u001B[33m%s\u001B[0m][\u001B[32m%s\u001B[0m]\n",
                    previousBoard, i
            );

            // Initialisation of minimax algorithm.
            copy[previousBoard][i] = AGENT_MARK;

            int score = minimax(copy, i, 0, AGENT_MARK);

            System.out.println();
            System.out.printf("Calculated score of \u001B[32m%s\u001B[0m\n", score);

            copy[previousBoard][i] = EMPTY_CELL;

            if (score > bestScore) {
                bestScore = score;
                bestMove = i;
            }
        }

        return bestMove;
    }

    //////////////////////////////////////////////////////////
    //                  UTILITY FUNCTION                    //
    //////////////////////////////////////////////////////////

    public static int[][] copyBoard(int[][] original) {
        int[][] newArray = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            newArray[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return newArray;
    }

    //////////////////////////////////////////////////////////
    //            DRAFT - FOR EVALUATION FUNCTION           //
    //////////////////////////////////////////////////////////

    public static boolean checkWinner(int[] board, int mark) {
        // Define the winning combinations.
        int[][] winningCombinations = {
                {1, 2, 3}, {4, 5, 6}, {7, 8, 9},  // Horizontal
                {1, 4, 7}, {2, 5, 8}, {3, 6, 9},  // Vertical
                {1, 5, 9}, {3, 5, 7}              // Diagonal
        };

        // Iterate over the winning combinations
        for (int[] combo : winningCombinations) {
            // Check if the values at the positions in the combo are the same and not empty.
            if (board[combo[0]] == mark && board[combo[1]] == mark && board[combo[2]] == mark) {
                return true;
            }
        }
        return false;
    }

    //////////////////////////////////////////////////////////
    //            DRAFT - FOR EVALUATION FUNCTION           //
    //////////////////////////////////////////////////////////

    public static boolean checkTie(int[] board) {
        for (int i = 1; i < 10; i++) {
            if (board[i] == EMPTY_CELL) return false;
        }
        return true;
    }

    public static int countPotentialWins(int[] board, int mark) {
        int count = 0;

        // Check rows for potential wins
        for (int start = 1; start <= 7; start += 3) {
            if (board[start] == board[start + 1] && board[start] == mark && board[start + 2] == EMPTY_CELL) {
                count++;
            }
        }
        // Check columns for potential wins
        for (int col = 1; col <= 3; col++) {
            if (board[col] == board[col + 3] && board[col] == mark && board[col + 6] == EMPTY_CELL) {
                count++;
            }
        }
        // Check diagonals for potential wins
        if (board[1] == board[5] && board[1] == mark && board[9] == EMPTY_CELL) {
            count++;
        }
        if (board[3] == board[5] && board[3] == mark && board[7] == EMPTY_CELL) {
            count++;
        }
        if (board[1] == board[9] && board[1] == mark && board[5] == EMPTY_CELL) {
            count++;
        }
        if (board[3] == board[7] && board[3] == mark && board[5] == EMPTY_CELL) {
            count++;
        }
        return count;
    }

    //////////////////////////////////////////////////////////
    //                 MINIMAX ALGORITHM                    //
    //////////////////////////////////////////////////////////

    public static int minimax(int[][] boards, int previousBoard, int depth, int mark) {
        int[][] copy = copyBoard(boards);

        // HELPER INDENTATION FOR DEBUG STATEMENT
        String tab = "";
        for (int i = 0; i < depth; i++) tab += "\t";

        //////////////////////////////////////////////////////////
        //            DRAFT - FOR EVALUATION FUNCTION           //
        //////////////////////////////////////////////////////////
        if (checkWinner(copy[previousBoard], AGENT_MARK)) {
            System.out.printf("\t\t=> \u001B[32mAGENT\u001B[0m\n");
            return 1 + depth;
        }
        if (checkWinner(copy[previousBoard], PLAYER_MARK)) {
            System.out.printf("\t\t=> \u001B[32mPLAYER\u001B[0m\n");
            return -1 - depth;
        }
        if (checkTie(copy[previousBoard])) {
            return depth;
        }
        if (depth == MAXIMUM_DEPTH) {
            //int agentWins = countPotentialWins(copy[previousBoard], AGENT_MARK);
            //int playerWins = countPotentialWins(copy[previousBoard], PLAYER_MARK);
            int count = countPotentialWins(copy[previousBoard], mark);
            if (count > 0) {
                System.out.printf("%sPotential wins count: %s\n", tab, countPotentialWins(copy[previousBoard], mark));
            }
            return count * 100 * (mark == AGENT_MARK ? 1 : -1);
        }

        if (mark == AGENT_MARK) {
            int bestScore = MINIMUM_SCORE;
            for (int i = 1; i < 10; i++) {
                if (copy[previousBoard][i] != EMPTY_CELL) continue;

                // DEBUG STATEMENT
                System.out.printf(
                        "%s\u001B[32mDEPTH: %s\u001B[0m -> AGENT MOVE " +
                                "local_boards[\u001B[33m%s\u001B[0m][\u001B[32m%s\u001B[0m]\n", tab, depth,
                        previousBoard, i
                );

                copy[previousBoard][i] = AGENT_MARK;
                int score = minimax(copy, i, depth + 1, PLAYER_MARK);
                copy[previousBoard][i] = EMPTY_CELL;
                bestScore = Math.max(score, bestScore);
            }
            return bestScore;
        } else {
            int bestScore = MAXIMUM_SCORE;
            for (int i = 1; i < 10; i++) {
                if (copy[previousBoard][i] != EMPTY_CELL) continue;

                // DEBUG STATEMENT
                System.out.printf(
                        "%s\u001B[32mDEPTH: %s\u001B[0m -> PLAYER MOVE " +
                                "local_boards[\u001B[33m%s\u001B[0m][\u001B[32m%s\u001B[0m]\n", tab, depth,
                        previousBoard, i
                );

                copy[previousBoard][i] = PLAYER_MARK;
                int score = minimax(copy, i, depth + 1, AGENT_MARK);
                copy[previousBoard][i] = EMPTY_CELL;
                bestScore = Math.min(score, bestScore);
            }
            return bestScore;
        }
    }
}
