package com.github.bhlangonijr.kengine.montecarlo

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.kengine.SearchParams
import com.github.bhlangonijr.kengine.SearchState
import org.junit.Assert.assertEquals
import org.junit.Test


class MonteCarloSearchTest {

    @Test
    fun `Search mate in 1 with rook`() {

        val board = Board()
        board.loadFromFen("k7/8/1K6/2R5/8/8/8/8 w - - 0 1")
        val params = SearchParams(nodes = 5000)
        val state = SearchState(params, board)

        val bestMove = MonteCarloSearch().rooSearch(state)

        assertEquals(Move(Square.C5, Square.C8), bestMove)
    }

    @Test
    fun `Search mate in 1 with bishops`() {

        val board = Board()
        board.loadFromFen("k7/8/1K1B4/8/2B5/8/8/8 w - - 0 1")
        val params = SearchParams(nodes = 5000)
        val state = SearchState(params, board)

        val bestMove = MonteCarloSearch().rooSearch(state)

        assertEquals(Move(Square.C4, Square.D5), bestMove)
    }

    @Test
    fun `Search mate in 1 with rook and knight`() {

        val board = Board()
        board.loadFromFen("3k4/8/3NK3/8/8/8/8/2R5 w - - 0 1")
        val params = SearchParams(nodes = 10000)
        val state = SearchState(params, board)

        val bestMove = MonteCarloSearch().rooSearch(state)

        assertEquals(Move(Square.C1, Square.C8), bestMove)
    }

    @Test
    fun `Search mate in 1 with promoted queen`() {

        val board = Board()
        board.loadFromFen("3k4/5P2/3K4/8/8/8/8/2R5 w - - 0 1")
        val params = SearchParams(nodes = 10000)
        val state = SearchState(params, board)

        val bestMove = MonteCarloSearch().rooSearch(state)

        assertEquals(Move(Square.F7, Square.F8, Piece.WHITE_QUEEN), bestMove)
    }


}



