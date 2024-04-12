# -------------------------------
#      !/usr/bin/python3
#
#           agent.py
#           --------
# Programmed AI agent to play the
# game of Nine-Board Tic-Tac-Toe.
# -------------------------------

import numpy as np

EMPTY_CELL = "."
AGENT_MARK = "x"
OPPONENT_MARK = "o"

matrix = np.full((3, 3), EMPTY_CELL, dtype=str)

def get_empty_cells(matrix):
    return [i for i, cell in enumerate(matrix.flatten()) if cell == EMPTY_CELL]

def check_if_winner_found(matrix, mark):
    # Check rows and columns for wins
    if any(all(matrix[i, j] == mark for j in range(3)) for i in range(3)) or \
       any(all(matrix[j, i] == mark for j in range(3)) for i in range(3)):
        return True

    # Check diagonals for wins
    if all(matrix[i, i] == mark for i in range(3)) or \
       all(matrix[i, 2 - i] == mark for i in range(3)):
        return True

    return False


class Move:
    def __init__(self, index, score) -> None:
        self.index = index
        self.score = score

def minimax(matrix, mark):
    matrix = matrix.reshape(3, 3)
    available_cells = get_empty_cells(matrix)

    # Opposing player has won the game.
    if check_if_winner_found(matrix, OPPONENT_MARK): return -1
    # Our agent has won the game.
    elif check_if_winner_found(matrix, AGENT_MARK): return 1
    # No options can be found, therefore a tie.
    elif len(available_cells) == 0: return 0

    matrix = matrix.flatten()
    # Create a store of all the available moves from this point using recursion.
    checked_moves = []
    for i in range(len(available_cells)):
        move = Move(available_cells[i], None)

        # Attempts the move
        matrix[available_cells[i]] = mark
        if mark == AGENT_MARK:
            move.score = minimax(matrix, OPPONENT_MARK)
        else:
            move.score = minimax(matrix, AGENT_MARK)
        # Undos the move
        matrix[available_cells[i]] = EMPTY_CELL

        checked_moves.append(move)
    return


def main():
    # Temporary example matrix for testing.
    matrix = np.array([
        [AGENT_MARK, EMPTY_CELL, OPPONENT_MARK],
        [AGENT_MARK, EMPTY_CELL, AGENT_MARK], 
        [OPPONENT_MARK, OPPONENT_MARK, EMPTY_CELL]
    ])

    best_play = minimax(matrix, AGENT_MARK)
    
if __name__ == "__main__":
    main()
