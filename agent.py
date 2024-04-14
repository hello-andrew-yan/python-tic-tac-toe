#!/usr/bin/python3
#  agent.py
#  Nine-Board Tic-Tac-Toe Agent starter code
#  COMP3411/9814 Artificial Intelligence
#  CSE, UNSW

import socket
import sys
import numpy as np

# a board cell can hold:
#   0 - Empty
#   1 - We played here
#   2 - Opponent played here

# the boards are of size 10 because index 0 isn't used
boards = np.zeros((10, 10), dtype="int8")
s = [".","X","O"]
curr = 0 # this is the current board to play in

# print a row
def print_board_row(board, a, b, c, i, j, k):
    print(" "+s[board[a][i]]+" "+s[board[a][j]]+" "+s[board[a][k]]+" | " \
             +s[board[b][i]]+" "+s[board[b][j]]+" "+s[board[b][k]]+" | " \
             +s[board[c][i]]+" "+s[board[c][j]]+" "+s[board[c][k]])

# Print the entire board
def print_board(board):
    print_board_row(board, 1,2,3,1,2,3)
    print_board_row(board, 1,2,3,4,5,6)
    print_board_row(board, 1,2,3,7,8,9)
    print(" ------+-------+------")
    print_board_row(board, 4,5,6,1,2,3)
    print_board_row(board, 4,5,6,4,5,6)
    print_board_row(board, 4,5,6,7,8,9)
    print(" ------+-------+------")
    print_board_row(board, 7,8,9,1,2,3)
    print_board_row(board, 7,8,9,4,5,6)
    print_board_row(board, 7,8,9,7,8,9)
    print()

##########################################################################################

EMPTY = 0
AGENT_MARK = 1
OPPONENT_MARK = 2

MIN_EVALUATION = -1000000
MAX_EVALUATION = 1000000

##########################################################################################

# choose a move to play
def play():

    n = find_best_move()
    print("Choosing", str(n))

    # # just play a random move for now
    # n = np.random.randint(1,9)
    # while boards[curr][n] != 0:
    #     n = np.random.randint(1,9)

    # print("playing", n)
    place(curr, n, AGENT_MARK)
    return n

##########################################################################################

def find_best_move():

    """
    Iterates through the valid moves in of the current board selected
    and performs a minimax search, going through each potential move
    followed by the corresponding board switch.
    """

    # 0. Initalisation of search.

    global curr
    best_evaluation = MIN_EVALUATION
    best_score = None

    alpha = MIN_EVALUATION
    beta = MAX_EVALUATION

    copy = boards.copy()

    # Retrieves potential moves in the current board.
    for potential_move in range(1, 10):
        if copy[curr][potential_move] == EMPTY:

            # 1. Commit the move in that cell.

            copy[curr][potential_move] = AGENT_MARK

            # 2. Retrieves evaluation from corresponding board.
            evaluation = minimax(copy[potential_move], 1, potential_move, alpha, beta, AGENT_MARK)
            print("INDEX: " + str(potential_move) + ", EVALUATION: " + str(evaluation))
            # 3. Undo the move in that cell.
            copy[curr][potential_move] = EMPTY

            # 4. Updates best move found so far if applicable.
            if evaluation > best_evaluation:
                best_evaluation = evaluation
                best_move = potential_move     
                alpha = max(alpha, evaluation)
    # 5. Return best move to be executed.
    return best_move


def game_over(board):
    return check_if_winner_found(board, AGENT_MARK) or check_if_winner_found(board, OPPONENT_MARK)

def count_potential_winning_combinations(board, player):
    count = 0

    # Check rows
    for start in range(1, 8, 3):
        if board[start] == board[start + 1] == player and board[start + 2] == EMPTY:
            count += 1
    
    # Check columns
    for col in range(1, 4):
        if board[col] == board[col + 3] == player and board[col + 6] == EMPTY:
            count += 1
    
    # Check diagonals
    if board[1] == board[5] == player and board[9] == EMPTY:
        count += 1
    if board[3] == board[5] == player and board[7] == EMPTY:
        count += 1
    
    return count


def minimax(board, depth, curr, alpha, beta, mark):

    # Depth limit imposed, check conditions of game
    # Opposing player has won the game.
    if check_if_winner_found(board, OPPONENT_MARK):
        print("-> LOST DETECTED:", -1)
        return -1
    # Our agent has won the game.
    elif check_if_winner_found(board, AGENT_MARK):
        print("-> WIN DETECTED", 1)
        return 1
    # No options can be found until depth cutoff.
    elif np.count_nonzero(board == EMPTY) == 0:
        print("-> DRAW DETECTED")
        return 0  # Draw
    elif depth == 0:
        score = 0
        # Example: add points for each potential winning combination for the AI player
        score += count_potential_winning_combinations(board, AGENT_MARK)
        # Example: subtract points for each potential winning combination for the opponent
        score -= count_potential_winning_combinations(board, OPPONENT_MARK)
        print("-> COMBINATION SCORE: ", str(score))
        return score

    copy = boards.copy()

    # Simulated agent's turn.
    if mark == OPPONENT_MARK:
        max_evaluation = MIN_EVALUATION
        for potential_move in range(1, 10):
            if copy[curr][potential_move] == EMPTY:

                copy[curr][potential_move] = OPPONENT_MARK
                evaluation = minimax(copy[potential_move], depth - 1, potential_move, alpha, beta, AGENT_MARK)
                copy[curr][potential_move] = EMPTY
                
                # Check evaluations.
                max_evaluation = max(max_evaluation, evaluation)
                alpha = max(alpha, evaluation)

                # Pruning.
                if beta <= alpha:
                    break
        return max_evaluation
    # Simulated player's turn.
    else:
        min_evaluation = MAX_EVALUATION
        for potential_move in range(1, 10):
            if copy[curr][potential_move] == EMPTY:

                copy[curr][potential_move] = AGENT_MARK
                evaluation = minimax(copy[potential_move], depth -1, potential_move, alpha, beta, OPPONENT_MARK)
                copy[curr][potential_move] = EMPTY

                # Check evaluations.
                min_evaluation = min(min_evaluation, evaluation)
                beta = min(beta, evaluation)

                # Pruning
                if beta <= alpha:
                    break
        return min_evaluation

def check_if_winner_found(board, mark):
    # Define the winning combinations
    winning_combinations = [
        (1, 2, 3), (4, 5, 6), (7, 8, 9),  # Horizontal
        (1, 4, 7), (2, 5, 8), (3, 6, 9),  # Vertical
        (1, 5, 9), (3, 5, 7)              # Diagonal
    ]
    
    # Iterate over the winning combinations
    for combo in winning_combinations:
        a, b, c = combo
        # Check if the values at the positions in the combo are the same and not empty
        if board[a-1] == board[b-1] == board[c-1] == mark:
            return True  # A winner is found
    
    return False  # No winner found


##########################################################################################

# place a move in the global boards
def place( board, num, player ):
    global curr
    curr = num
    boards[board][num] = player

# read what the server sent us and
# parse only the strings that are necessary
def parse(string):
    if "(" in string:
        command, args = string.split("(")
        args = args.split(")")[0]
        args = args.split(",")
    else:
        command, args = string, []

    # init tells us that a new game is about to begin.
    # start(x) or start(o) tell us whether we will be playing first (x)
    # or second (o); we might be able to ignore start if we internally
    # use 'X' for *our* moves and 'O' for *opponent* moves.

    # second_move(K,L) means that the (randomly generated)
    # first move was into square L of sub-board K,
    # and we are expected to return the second move.
    if command == "second_move":
        # place the first move (randomly generated for opponent)
        place(int(args[0]), int(args[1]), 2)
        return play()  # choose and return the second move

    # third_move(K,L,M) means that the first and second move were
    # in square L of sub-board K, and square M of sub-board L,
    # and we are expected to return the third move.
    elif command == "third_move":
        # place the first move (randomly generated for us)
        place(int(args[0]), int(args[1]), 1)
        # place the second move (chosen by opponent)
        place(curr, int(args[2]), 2)
        return play() # choose and return the third move

    # nex_move(M) means that the previous move was into
    # square M of the designated sub-board,
    # and we are expected to return the next move.
    elif command == "next_move":
        # place the previous move (chosen by opponent)
        place(curr, int(args[0]), 2)
        return play() # choose and return our next move

    elif command == "win":
        print("Yay!! We win!! :)")
        return -1

    elif command == "loss":
        print("We lost :(")
        return -1

    return 0

# connect to socket
def main():
    sys.setrecursionlimit(9000000)
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    port = int(sys.argv[2]) # Usage: ./agent.py -p (port)

    s.connect(('localhost', port))
    while True:
        text = s.recv(1024).decode()
        if not text:
            continue
        for line in text.split("\n"):
            response = parse(line)
            if response == -1:
                s.close()
                return
            elif response > 0:
                s.sendall((str(response) + "\n").encode())

if __name__ == "__main__":
    main()
