simple kotlin chess engine
==========================

Simple Kotlin Chess Engine, or just kengine, is a minimalistic chess engine written in [kotlin](https://kotlinlang.org/).
This project aims at demonstrating how to build a chess engine using a [chess library](https://github.com/bhlangonijr/chesslib) 
and also at providing a basic project for people interested in quickly bootstrapping a chess playing agent for 
easily adding and test his/her own ideas. 

# Building
[![](https://jitpack.io/v/bhlangonijr/kengine.svg)](https://jitpack.io/#bhlangonijr/kengine)

## From source

```shell script
$ git clone git@github.com:bhlangonijr/kengine.git
$ cd kengine/
$ ./gradlew clean build
```

# Usage

kengine can be used in a chess GUI that supports the [UCI protocol](https://www.chessprogramming.org/UCI) by implementing
the basic commands that communicates with it. Refer to [UCI](http://wbec-ridderkerk.nl/html/UCIProtocol.html) documentation to have a better understanding on how it works. 

## Using the command-line interface
```shell script
# Look for a kengine.sh or kengine.bat in the root folder and execute it
$ ./kengine.sh

```

Type uci and hit enter to display engine info.
```shell script
$ uci 
id name kengine 1.0.1
id author bhlangonijr
option name Hash type spin default 128 min 1 max 16384
option name Threads type spin default 1 min 1 max 128
option name SearchAlgorithm type combo default MonteCarlo var AlphaBeta var MonteCarlo
uciok

```
`uciok` means the program is ready to accept commands.

#### Example 1: Increasing the hash table size to 256mb

```shell script
$ setoption name Hash value 256

```

#### Example 2: Calculating the best move for the initial standard position (limiting search to depth 5)

Type `go depth 5` and hit enter: 
```shell script
$ go depth 5
info depth 1 score cp 50 time 19 nodes 24 nps 24 pv b1c3
info depth 2 score cp 0 time 48 nodes 131 nps 131 pv g1f3 g8f6
info depth 3 score cp 50 time 69 nodes 787 nps 787 pv g1f3 g8f6 b1c3
info depth 4 score cp 0 time 128 nodes 4437 nps 4437 pv g1f3 g8f6 b1c3 b8c6 g5h7
info depth 5 score cp 40 time 195 nodes 22791 nps 22791 pv g1f3 g8f6 b1c3 b8c6 e2e4
bestmove g1f3
info string total time 195
```
 
#### Installing the engine in a chess GUI

For playing with kengine using your preferred chess GUI it only takes providing the startup script `kengine.sh` or `kengine.bat`
in your chess GUI UCI engine setup options. 

#### Self-play using integration tests

kengine comes with two different bare bones Search algorithms which can be set via options: Alphabeta and Monte Carlo.
Integration tests in the project test folder can match one search against the other: 


```kotlin
    @Test
    fun `match Abts and Mcts engines`() {

        val board = Board()
        board.loadFromFen(startStandardFENPosition)

        val abts = AlphaBetaSearch()
        val mcts = MonteCarloSearch()

        val moves = MoveList(board.fen)
        while (!board.isDraw && !board.isMated) {
            val move = play(board, abts, mcts)
            if (move != Move(Square.NONE, Square.NONE) && board.doMove(move)) {
                moves += move
                println("Played: $move = ${board.fen}")
            }
        }
        printResult(moves, board)
    }
``` 

# Features

  - UCI protocol support;
  - Basic time control;
  - Alphabeta search algorithm with Null move pruning, PVS (principal variation search), transposition table and quiescence search;
  - Basic Monte Carlo search with UCT;