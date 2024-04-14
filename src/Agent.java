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

public class Agent {

    static int previousMove = 0;
    
    // Zero index not used
    static int[][] boards = new int[10][10];

    static final int EMPTY = 0;
    static final int AGENT_MARK = 1;
    static final int PLAYER_MARK = 2;

     // TODO - Delete this later.
    static Random rand = new Random();

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
            place(previousMove, Integer.parseInt(list), 2);

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

    public static void place(int board, int num, int player) {
        previousMove = num;
        boards[board][num] = player;
    }

//////////////////////////////////////////////////////////
//                   IMPLEMENTATION                     //
//////////////////////////////////////////////////////////

    public static int play() {
        int n = rand.nextInt(9) + 1;
        while (boards[previousMove][n] != 0 ) {
            n = rand.nextInt(9) + 1;
        }
        place(previousMove, n, 1);
        return n;
    }
}
