#!/bin/bash

# Play agent against specified program
# Example:
# ./playt.sh ./agent lookt 12345
# ./playt.sh "python3 agent.py" ./lookt 12345
# ./playt.sh "java Agent" ./lookt 12345

if [ "$#" -ne 3 ]; then
  echo "Usage: $0 <player1> <player2> <port>" >&2
  exit 1
fi

./servt -p $3 & sleep 0.5
$1 -p $3 & sleep 0.5
$2 -p $3
