package com.github.bhlangonijr.kengine.eval

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Constants.startStandardFENPosition
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.kengine.SearchParams
import com.github.bhlangonijr.kengine.SearchState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MaterialEvalTest {

    @Test
    fun testEvalMaterial() {

        val eval = MaterialEval()
        val board = Board()

        board.loadFromFen("r1b1kb1r/ppp2ppp/8/4n3/4n3/PPP1P3/6PP/RNBK1BNR w kq - 0 19")
        assertEquals(0, eval.scoreMaterial(board))

        board.doMove(Move(Square.G2, Square.G3))
        assertEquals(0, eval.scoreMaterial(board))

        board.doMove(Move(Square.E4, Square.F2))
        assertEquals(0, eval.scoreMaterial(board))

        board.doMove(Move(Square.D1, Square.E1))
        assertEquals(0, eval.scoreMaterial(board))

        board.doMove(Move(Square.F2, Square.H1))
        assertEquals(-ROOK_VALUE, eval.scoreMaterial(board))

    }

    @Test
    fun testPieceSquareEval() {

        val eval = MaterialEval()
        val board = Board()

        board.loadFromFen("5k2/8/8/8/8/8/8/5K2 w - - 0 1")

        assertEquals(0, eval.scorePieceSquare(board, Side.WHITE))

        board.loadFromFen(startStandardFENPosition)

        assertEquals(0, eval.scorePieceSquare(board, Side.WHITE))

        board.loadFromFen("6k1/8/8/8/8/8/8/6K1 w - - 0 1")

        assertEquals(0, eval.scorePieceSquare(board, Side.WHITE))

        board.loadFromFen("3k4/8/8/8/8/8/8/6K1 w - - 0 1")

        assertEquals(29, eval.scorePieceSquare(board, Side.WHITE))

        board.loadFromFen("3k4/6R1/8/8/8/8/8/6K1 w - - 0 1")

        assertEquals(39, eval.scorePieceSquare(board, Side.WHITE))

        board.loadFromFen("3k4/8/8/8/4n3/8/8/3K4 w - - 0 1")

        assertEquals(20, eval.scorePieceSquare(board, Side.BLACK))

        board.loadFromFen("rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b KQkq - 0 1")

        assertEquals(40, eval.scorePieceSquare(board, Side.WHITE))

        board.loadFromFen("rnbqkb1r/pppppppp/7n/8/3P4/8/PPP1PPPP/RNBQKBNR w KQkq - 0 1")

        assertEquals(-30, eval.scorePieceSquare(board, Side.BLACK))

        board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")

        assertEquals(0, eval.scorePieceSquare(board, Side.BLACK))

        board.loadFromFen("8/8/8/3pp3/8/8/3PP3/8 w - - 0 1")

        assertEquals(-80, eval.scorePieceSquare(board, Side.WHITE))

    }

    @Test
    fun testEval() {

        val eval = MaterialEval()
        val board = Board()
        val state = SearchState(SearchParams(), board)
        board.loadFromFen("rnbqkbnr/ppp1pppp/8/3p4/3P4/8/PPP1PPPP/RNBQKBNR w KQkq - 0 1")

        assertTrue(eval.evaluate(state, board) == 0L)

        board.loadFromFen("rnbqkb1r/pppppppp/7n/8/3P4/8/PPP1PPPP/RNBQKBNR w KQkq - 0 1")
        assertTrue(eval.evaluate(state, board) > 0L)

        board.loadFromFen("r1bqkbr1/pppppppp/2n2n2/8/2PPP3/2N5/PP3PPP/R1BQKBNR b KQq e3 0 4")
        assertTrue(eval.evaluate(state, board) < 0L)

        board.loadFromFen("r1bqkbr1/pppppppp/2n5/8/2PPn3/2N5/PP3PPP/R1BQKBNR w KQq e3 0 4")
        assertTrue(eval.evaluate(state, board) < 0L)

        board.loadFromFen("r1bqkbr1/pppppppp/2n5/8/2PPN3/8/PP3PPP/R1BQKBNR b KQq e3 0 4")
        assertTrue(eval.evaluate(state, board) < 0L)

        board.loadFromFen("r1bqkbr1/pppppppp/8/8/2PnN3/8/PP3PPP/R1BQKBNR w KQq e3 0 4")
        assertTrue(eval.evaluate(state, board) > 0L)

        board.loadFromFen("r1bqkbr1/pppppppp/8/8/2PQN3/8/PP3PPP/R1B1KBNR b KQq e3 0 4")
        assertTrue(eval.evaluate(state, board) < -KNIGHT_VALUE)

    }
}



