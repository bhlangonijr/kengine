package com.github.bhlangonijr.kengine.alphabeta

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Rank
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.kengine.SearchParams
import com.github.bhlangonijr.kengine.SearchState
import com.github.bhlangonijr.kengine.eval.MAX_VALUE
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

@ExperimentalStdlibApi
class AlphaBetaSearchTest {

    @Test
    fun `Search best move avoiding lose rook`() {

        val board = Board()
        board.loadFromFen("r1b1kb1r/ppp2ppp/8/4n3/4n3/PPP1P3/6PP/RNBK1BNR w kq - 0 19")

        val params = SearchParams(depth = 4)
        val state = SearchState(params, board)

        val bestMove = AlphaBetaSearch().rooSearch(state)
        assertTrue(Move(Square.D1, Square.E1) == bestMove || Move(Square.D1, Square.E2) == bestMove)
    }

    @Test
    fun `Search best move wining quality`() {

        val board = Board()
        board.loadFromFen("r1b1kb1r/ppp2ppp/8/4n3/4n3/PPP1P1P1/7P/RNBK1BNR b kq - 0 20")

        val params = SearchParams(depth = 4)
        val state = SearchState(params, board)

        val bestMove = AlphaBetaSearch().rooSearch(state)
        assertEquals(Move(Square.E4, Square.F2), bestMove)
    }

    @Test
    fun `Search best move force knight trade`() {

        val board = Board()
        board.loadFromFen("r1b1kb1r/ppp2ppp/8/4n3/8/PPP1P1P1/5n1P/RNBK1BNR w kq - 1 21")

        val params = SearchParams(depth = 4)
        val state = SearchState(params, board)

        val bestMove = AlphaBetaSearch().rooSearch(state)
        assertEquals(Move(Square.D1, Square.E2), bestMove)
    }

    @Test
    fun `Search best move capturing the rook`() {

        val board = Board()
        board.loadFromFen("r1b1kb1r/ppp2ppp/8/4n3/8/PPP1P1P1/5n1P/RNB1KBNR b kq - 2 22")

        val params = SearchParams(depth = 4)
        val state = SearchState(params, board)

        val bestMove = AlphaBetaSearch().rooSearch(state)
        assertEquals(Move(Square.F2, Square.H1), bestMove)
    }

    @Test
    fun `Search best move opening`() {

        val board = Board()
        board.loadFromFen("rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b KQkq - 0 1")

        val params = SearchParams(depth = 4)
        val state = SearchState(params, board)

        val tt = TranspositionTable()
        val search = AlphaBetaSearch(transpositionTable = tt)
        val bestMove = search.rooSearch(state)
        assertEquals(Move(Square.G8, Square.F6), bestMove)
    }

    @Test
    fun `Search best move knight`() {

        val board = Board()
        board.loadFromFen("r1bqkbr1/pppppppp/2n2n2/8/2PPP3/2N5/PP3PPP/R1BQKBNR b KQq e3 0 4")

        val params = SearchParams(depth = 4)
        val state = SearchState(params, board)

        val search = AlphaBetaSearch()
        val bestMove = search.rooSearch(state)
        assertNotEquals(Move(Square.F6, Square.E4), bestMove)

    }

    @Test
    fun `Search issue bestmove`() {

        val board = Board()
        board.loadFromFen("r4rk1/1pp1nppp/p1q5/2bp4/5P2/1B3QPP/PP1B4/1K1RR3 b - - 7 21")

        val params = SearchParams(blackTime = 180804, whiteTime = 184380)
        val state = SearchState(params, board)

        val search = AlphaBetaSearch()
        val bestMove = search.rooSearch(state)
        assertNotEquals(Move(Square.NONE, Square.NONE), bestMove)

    }

    @Test
    fun `Search issue bug loosing pawn`() {

        val board = Board()
        board.loadFromFen("r1bqk1nr/ppp2ppp/2n1p3/3pP3/1b1P3P/2P5/PP3PP1/RNBQKBNR b KQkq - 0 5")

        val params = SearchParams(blackTime = 271076, whiteTime = 278894)
        val state = SearchState(params, board)

        val search = AlphaBetaSearch()
        val bestMove = search.rooSearch(state)
        assertNotEquals(Move(Square.NONE, Square.NONE), bestMove)

    }

    @Test
    fun `Search mate KQ vs K`() {

        val board = Board()
        board.loadFromFen("5k2/7Q/8/8/8/8/8/1K6 w - - 22 12 - ")

        val params = SearchParams(depth = 7)
        val state = SearchState(params, board)

        val search = AlphaBetaSearch()
        val bestMove = search.rooSearch(state)
        val whiteKingSquare = board.getPieceLocation(Piece.WHITE_KING)
        //get white king closer to black king
        assertTrue(bestMove.from == whiteKingSquare[0] && bestMove.to.rank == Rank.RANK_2)

    }

    @Test
    fun `Quiesce search find exchanges`() {

        val board = Board()
        board.loadFromFen("r1bqkbr1/pppppppp/2n2n2/8/2PPP3/2N5/PP3PPP/R1BQKBNR b KQq e3 0 4")

        val params = SearchParams(depth = 4)
        val state = SearchState(params, board)

        val search = AlphaBetaSearch()
        val score = search.quiesce(board, -MAX_VALUE, MAX_VALUE, 0, 1, state)
        println("score = $score")

    }

    @Test
    fun `Quiesce search find exchanges 2`() {

        val board = Board()
        board.loadFromFen("r1bqkbr1/pppppppp/2n5/8/2PPn3/2N5/PP3PPP/R1BQKBNR w KQq e3 0 4")

        val params = SearchParams(depth = 4)
        val state = SearchState(params, board)

        val search = AlphaBetaSearch()
        val score = search.quiesce(board, -MAX_VALUE, MAX_VALUE, 0, 1, state)
        println("score = $score")

    }

}