package com.github.bhlangonijr.kengine

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.move.MoveList

fun printResult(moves: MoveList, board: Board) {

    if (board.isDraw) {
        println("result = 1/2 - 1/2")
    } else if (board.isMated && board.sideToMove == Side.BLACK) {
        println("result = 1 - 0")
    } else {
        println("result = 0 - 1")
    }
    println("move list: ${moves.toSan()}")
    println("final fen: ${board.fen}")
}

fun play(board: Board,
         player1: SearchEngine,
         player2: SearchEngine,
         time: Long = 180000,
         increment: Long = 0): Move {

    val params1 = SearchParams(whiteTime = time, whiteIncrement = increment,
            blackTime = time, blackIncrement = increment)
    val params2 = SearchParams(whiteTime = time, whiteIncrement = increment,
            blackTime = time, blackIncrement = increment)

    return if (board.sideToMove == Side.WHITE) {
        player1.rooSearch(SearchState(params1, board))
    } else {
        player2.rooSearch(SearchState(params2, board))
    }
}

