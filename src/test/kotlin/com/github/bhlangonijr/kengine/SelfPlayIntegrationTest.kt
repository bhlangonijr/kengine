package com.github.bhlangonijr.kengine

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Constants.startStandardFENPosition
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.move.MoveList
import com.github.bhlangonijr.kengine.alphabeta.AlphaBetaSearch
import com.github.bhlangonijr.kengine.montecarlo.MonteCarloSearch
import org.junit.Test


@ExperimentalStdlibApi
class SelfPlayIntegrationTest {

    @Test
    fun `Match Abts and Mcts engines`() {

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

    @Test
    fun `Search mate KQ vs K`() {

        val board = Board()
        board.loadFromFen("8/8/3k4/8/4K3/5Q2/8/8 w - - 0 1")

        val abts = AlphaBetaSearch()
        val moves = MoveList(board.fen)
        val state = SearchState(SearchParams(depth = 7), board)
        while (!board.isDraw && !board.isMated) {
            println("Search: ${board.fen} - \n$board")
            val move = abts.rooSearch(state)
            if (move != Move(Square.NONE, Square.NONE) && board.doMove(move)) {
                moves += move
                println("Played: $move = ${board.fen}")
            }
        }
        printResult(moves, board)
    }

    @Test
    fun `Search mate KQ vs K 2`() {

        val board = Board()
        board.loadFromFen("3k4/8/8/1KQ5/8/8/8/8 w - - 0 1")

        val abts = AlphaBetaSearch()
        val moves = MoveList(board.fen)
        var move = Move(Square.NONE, Square.NONE)
        try {
            while (!board.isDraw && !board.isMated) {
                println("Search: ${board.fen} - \n$board")
                val state = SearchState(SearchParams(depth = 7), board)
                move = abts.rooSearch(state)
                if (move != Move(Square.NONE, Square.NONE) && board.doMove(move)) {
                    moves += move
                    println("Played: $move = ${board.fen}")
                }
            }
            printResult(moves, board)
        } catch (e: Exception) {
            e.printStackTrace()
            println(board.fen)
            println(move)
        }
    }

    @Test
    fun `Match Mcts engine with different parameters`() {

        val board = Board()
        board.loadFromFen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq -")

        val mcts1 = MonteCarloSearch(1.4)
        val mcts2 = MonteCarloSearch()

        val moves = MoveList(board.fen)
        while (!board.isDraw && !board.isMated) {
            val move = play(board, mcts1, mcts2)
            if (move != Move(Square.NONE, Square.NONE) && board.doMove(move)) {
                moves += move
                println("Played: $move = ${board.fen}")
            }
        }

        printResult(moves, board)
    }
}