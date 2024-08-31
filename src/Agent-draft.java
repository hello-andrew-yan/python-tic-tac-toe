//////////////////////////////////////////////////////////
//														//
//  				  Agent.java						//
//														//
//  		  Nine-Board Tic-Tac-Toe Agent				//
//  	 COMP3411/9814 Artificial Intelligence			//
// 														//
//  	 			  CSE, UNSW							//
//														//
//////////////////////////////////////////////////////////


/*
 * Briefly describe how your program works, including any
 * algorithms and data structures employed, and explain
 *   any design decisions you made along the way.
 *
 *
 *
 */


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class Machine {

    static final int[][] boards = new int[10][10];
    static int previousboard = 0;

    //////////////////////////////////////////////////////////
    // 					 UNSW FUNCTION 						//
    //////////////////////////////////////////////////////////

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

    ///////////////////////////////////////////////////////////////////////
    //								PARSE								 //
    ///////////////////////////////////////////////////////////////////////
    /**
     * From the server, init() tells us that a new game is about to begin.
     * <p>
     * start(x) or start(o) tell us whether we will be playing first (x)
     * or second (o); we might be able to ignore start if we internally
     * use 'X' for *our* moves and 'O' for *opponent* moves.
     *
     * @param line 	Command line string to execute
     * @return 		Number state depending on the command
     */
    ///////////////////////////////////////////////////////////////////////

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
            place(previousboard, Integer.parseInt(list), PLAYER_MARK);

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
        previousboard = index;
        boards[board][index] = mark;
    }

    //////////////////////////////////////////////////////////
    // 					IMPLEMENTATION 						//
    //////////////////////////////////////////////////////////

    public static int play() {
        int bestMove = findBestMove();
        place(previousboard, bestMove, AGENT_MARK);
        return bestMove;
    }

    //////////////////////////////////////////////////////////
    // 					FIND BEST MOVE 						//
    //////////////////////////////////////////////////////////
    /**
     * Finds the best moves out of all the cells available in
     * the current board the game has assigned to the agent.
     * <p>
     * This function runs in O(b^m) time. Where b = 9 in a
     * game of 9-Board Tic-Tac-Toe and m = STARTING_DEPTH.
     *
     * @return	The index of the best move to
     * 			go in this particular scenario
     */
    //////////////////////////////////////////////////////////

    static final int EMPTY_CELL = 0;
    static final int AGENT_MARK = 1;
    static final int PLAYER_MARK = 2;

    static final int STARTING_DEPTH = 8;

    public static int findBestMove() {

        int bestScore = Integer.MIN_VALUE;
        int bestMove = 0;

        int[][] copiedBoards = copyBoards(boards);
        for (int i = 1; i < 10; i++) {
            if (copiedBoards[previousboard][i] != EMPTY_CELL) continue;

            // Same format will be used in the minimax algorithm.
            // copiedBoards[previousboard][i] = AGENT_MARK;
            int score = minimax(copiedBoards, previousboard, STARTING_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
            // copiedBoards[previousboard][i] = EMPTY_CELL;

            if (score > bestScore) {
                bestScore = score;
                bestMove = i;
            }
        }
        return bestMove;
    }

    //////////////////////////////////////////////////////////
    // 					MINIMAX ALGORITHM					//
    //////////////////////////////////////////////////////////

    public static int minimax(int [][] boards, int nextBoard, int depth, int alpha, int beta, boolean isMax) {
        int[][] copiedBoards = copyBoards(boards);

        if (isWinning(copiedBoards[nextBoard], AGENT_MARK)) return 10000 * (depth + 1);

        if (isWinning(copiedBoards[nextBoard], PLAYER_MARK)) return -10000 * (depth + 1);

        // Experimental return for ties
        if (isTied(copiedBoards[nextBoard])) return 1000;

        if (depth == 0) {
            int score = 0;
            // Returns the score of the entire board.
            for (int i = 1; i < 10; i++) score += evaluateBoard(copiedBoards[i]);
            return score;
        }

        if (isMax) {
            int score = Integer.MIN_VALUE;
            for (int i = 1; i < 10; i++) {
                if (copiedBoards[nextBoard][i] != EMPTY_CELL) continue;

                copiedBoards[nextBoard][i] = AGENT_MARK;
                score = Math.max(score, minimax(copiedBoards, i, depth - 1, alpha, beta, false));
                copiedBoards[nextBoard][i] = EMPTY_CELL;

                alpha = Math.max(alpha, score);
                if (beta <= alpha) break;
            }
            return score;
        } else {
            int score = Integer.MAX_VALUE;
            for (int i = 1; i < 10; i++) {
                if (copiedBoards[nextBoard][i] != EMPTY_CELL) continue;

                copiedBoards[nextBoard][i] = AGENT_MARK;
                score = Math.min(score, minimax(copiedBoards, i, depth - 1, alpha, beta, true));
                copiedBoards[nextBoard][i] = EMPTY_CELL;

                beta = Math.min(beta, score);
                if (beta <= alpha) break;
            }
            return score;
        }
    }

    //////////////////////////////////////////////////////////
    //                EVAULATION FUNCTION                   //
    //////////////////////////////////////////////////////////
    /**
     * Heuristic is based on how many advantageous positions
     * the agent has on the board. These include having two in
     * a rows and having an advantageous position on the board.
     *
     * The reason why three in a rows are not considered is that
     * the negamax algorithm already has a terminal condition to
     * detect that already.
     */
    //////////////////////////////////////////////////////////

    public static int evaluateBoard(int[] board) {
        int score = 0;
        for (int[] combos : WINNING_COMBINATIONS) score += calculatePositionalScore(board, combos);
        return score;
    }

    public static int calculatePositionalScore(int[] board, int[] sequence) {
        // Validation checking for correct Tic-Tac-Toe sequences.
        if (sequence.length < 3) return 0;

        int agentPlacements = 0;
        int playerPlacements = 0;
        int positionalBonus = 1;

        for (int i = 0; i < 3; i++) {
            if (board[sequence[i]] == AGENT_MARK) agentPlacements++;
            if (board[sequence[i]] == PLAYER_MARK) playerPlacements++;

            // Positional advantage.
            if (board[sequence[i]] != EMPTY_CELL) {
                positionalBonus *= POSITIONAL_SCORES[sequence[i]];
            }
        }

        // Player has a two in a row, player may also have a positional advantage.
        if (agentPlacements == 0 && playerPlacements == 2) return -30 * positionalBonus;

        // Agent has a two in a row, current may also have a positional advantage.
        if (agentPlacements == 2 && playerPlacements == 0) return 30 * positionalBonus;

        // Player blocked agent.
        if (agentPlacements == 2 && playerPlacements == 1) return -15 * positionalBonus;

        // Agent blocked player.
        if (agentPlacements == 1 && playerPlacements == 2) return 15 * positionalBonus;

        // Player holds one position, player may also have a positional advantage.
        if (agentPlacements == 0 && playerPlacements == 1) return -1 * positionalBonus;

        // Agent holds one position, current may also have a positional advantage.
        if (agentPlacements == 1 && playerPlacements == 0) return 1 * positionalBonus;

        // Neutral position.
        return 0;
    }

    //////////////////////////////////////////////////////////
    // 					UTILITY FUNCTIONS					//
    //////////////////////////////////////////////////////////

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

    public static int[][] copyBoards(int[][] original) {
        return Arrays.stream(original).map(int[]::clone).toArray(int[][]::new);
    }
}
