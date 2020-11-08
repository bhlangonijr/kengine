package com.github.bhlangonijr.kengine

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.kengine.montecarlo.MonteCarloSearch
import org.junit.Assert.assertEquals
import org.junit.Test

class MonteCarloSearchIntegrationTest {

    @Test
    fun testSearchWithNodes() {

        val board = Board()
        board.loadFromFen("r2qkb1r/pp2nppp/3p4/2pNN1B1/2BnP3/3P4/PPP2PPP/R2bK2R w KQkq - 1 0")
        val params = SearchParams(nodes = 4000000)
        val state = SearchState(params, board)

        val bestMove = MonteCarloSearch().rooSearch(state)

        assertEquals(Move(Square.D5, Square.F6), bestMove)
    }

    @Test
    fun testSearchWithNodes2() {

        val board = Board()
        board.loadFromFen("r2qk2r/pb4pp/1n2Pb2/2B2Q2/p1p5/2P5/2B2PPP/RN2R1K1 w - - 1 0")
        val params = SearchParams(nodes = 4000000)
        val state = SearchState(params, board)

        val bestMove = MonteCarloSearch().rooSearch(state)
        assertEquals(Move(Square.F5, Square.G6), bestMove)
    }

    @Test
    fun testSearchWithNodes3() {

        val board = Board()
        board.loadFromFen("5rkr/pp2Rp2/1b1p1Pb1/3P2Q1/2n3P1/2p5/P4P2/4R1K1 w - - 1 0")
        val params = SearchParams(nodes = 4000000)
        val state = SearchState(params, board)

        val bestMove = MonteCarloSearch().rooSearch(state)
        assertEquals(Move(Square.G5, Square.G6), bestMove)
    }

    @Test
    fun testSearchWithNodes4() {

        val board = Board()
        board.loadFromFen("2k5/7R/4K3/8/8/8/8/8 w - - 1 0")
        val params = SearchParams(nodes = 4000000)
        val state = SearchState(params, board)

        val bestMove = MonteCarloSearch().rooSearch(state)
        assertEquals(Move(Square.E6, Square.D6), bestMove)
    }
}



