package com.github.bhlangonijr.kengine.eval

import com.github.bhlangonijr.chesslib.*
import com.github.bhlangonijr.kengine.SearchState
import kotlin.math.min

const val PAWN_VALUE = 100L
const val BISHOP_VALUE = 320L
const val KNIGHT_VALUE = 330L
const val ROOK_VALUE = 500L
const val QUEEN_VALUE = 900L
const val MAX_VALUE = 40000L
const val MATE_VALUE = 39000L

val PAWN_PST = longArrayOf(
        0, 0, 0, 0, 0, 0, 0, 0,
        50, 50, 50, 50, 50, 50, 50, 50,
        10, 10, 20, 30, 30, 20, 10, 10,
        5, 5, 10, 25, 25, 10, 5, 5,
        0, 0, 0, 20, 20, 0, 0, 0,
        5, -5, -10, 0, 0, -10, -5, 5,
        5, 10, 10, -20, -20, 10, 10, 5,
        0, 0, 0, 0, 0, 0, 0, 0)

val KNIGHT_PST = longArrayOf(
        -50, -40, -30, -30, -30, -30, -40, -50,
        -40, -20, 0, 0, 0, 0, -20, -40,
        -30, 0, 10, 15, 15, 10, 0, -30,
        -30, 5, 15, 20, 20, 15, 5, -30,
        -30, 0, 15, 20, 20, 15, 0, -30,
        -30, 5, 10, 15, 15, 10, 5, -30,
        -40, -20, 0, 5, 5, 0, -20, -40,
        -50, -40, -30, -30, -30, -30, -40, -50
)

val BISHOP_PST = longArrayOf(
        -20, -10, -10, -10, -10, -10, -10, -20,
        -10, 0, 0, 0, 0, 0, 0, -10,
        -10, 0, 5, 10, 10, 5, 0, -10,
        -10, 5, 5, 10, 10, 5, 5, -10,
        -10, 0, 10, 10, 10, 10, 0, -10,
        -10, 10, 10, 10, 10, 10, 10, -10,
        -10, 5, 0, 0, 0, 0, 5, -10,
        -20, -10, -10, -10, -10, -10, -10, -20
)

val ROOK_PST = longArrayOf(
        0, 0, 0, 0, 0, 0, 0, 0,
        5, 10, 10, 10, 10, 10, 10, 5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        0, 0, 0, 5, 5, 0, 0, 0
)

val QUEEN_PST = longArrayOf(
        -20, -10, -10, -5, -5, -10, -10, -20,
        -10, 0, 0, 0, 0, 0, 0, -10,
        -10, 0, 5, 5, 5, 5, 0, -10,
        -5, 0, 5, 5, 5, 5, 0, -5,
        0, 0, 5, 5, 5, 5, 0, -5,
        -10, 5, 5, 5, 5, 5, 0, -10,
        -10, 0, 5, 0, 0, 0, 0, -10,
        -20, -10, -10, -5, -5, -10, -10, -20
)

val KING_OPENING_PST = longArrayOf(
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -20, -30, -30, -40, -40, -30, -30, -20,
        -10, -20, -20, -20, -20, -20, -20, -10,
        20, 20, 0, 0, 0, 0, 20, 20,
        20, 30, 10, 0, 0, 10, 30, 20
)

val KING_END_PST = longArrayOf(
        -50, -40, -30, -20, -20, -30, -40, -50,
        -30, -20, -10, 0, 0, -10, -20, -30,
        -30, -10, 20, 30, 30, 20, -10, -30,
        -30, -10, 30, 40, 40, 30, -10, -30,
        -30, -10, 30, 40, 40, 30, -10, -30,
        -30, -10, 20, 30, 30, 20, -10, -30,
        -30, -30, 0, 0, 0, 0, -30, -30,
        -50, -30, -30, -30, -30, -30, -30, -50
)

class MaterialEval : Evaluator {

    override fun evaluate(state: SearchState, board: Board): Long {

        return scoreMaterial(board) + scorePieceSquare(board)
    }

    override fun pieceStaticValue(piece: Piece): Long {

        return when (piece.pieceType) {
            PieceType.PAWN -> PAWN_VALUE
            PieceType.BISHOP -> BISHOP_VALUE
            PieceType.KNIGHT -> KNIGHT_VALUE
            PieceType.ROOK -> ROOK_VALUE
            PieceType.QUEEN -> QUEEN_VALUE
            PieceType.KING -> MATE_VALUE
            else -> 0L
        }
    }

    override fun pieceSquareStaticValue(piece: Piece, square: Square): Long {

        return when (piece.pieceType) {
            PieceType.PAWN -> PAWN_PST[getIndex(piece.pieceSide, square)]
            PieceType.KNIGHT -> KNIGHT_PST[getIndex(piece.pieceSide, square)]
            PieceType.BISHOP -> BISHOP_PST[getIndex(piece.pieceSide, square)]
            PieceType.ROOK -> ROOK_PST[getIndex(piece.pieceSide, square)]
            PieceType.QUEEN -> QUEEN_PST[getIndex(piece.pieceSide, square)]
            PieceType.KING -> KING_END_PST[getIndex(piece.pieceSide, square)]
            else -> 0L
        }
    }

    fun scoreMaterial(board: Board) = scoreMaterial(board, board.sideToMove)

    fun scoreMaterial(board: Board, player: Side): Long {

        return countMaterial(board, player) - countMaterial(board, player.flip())
    }

    fun scorePieceSquare(board: Board) = scorePieceSquare(board, board.sideToMove)

    fun scorePieceSquare(board: Board, player: Side): Long {

        return calculatePieceSquare(board, player) - calculatePieceSquare(board, player.flip())
    }

    private fun calculatePieceSquare(board: Board, side: Side): Long {

        val maxMoves = 40
        val phase = min(maxMoves, board.moveCounter)
        var sum = 0L

        var pieces = board.getBitboard(side)
                .and(board.getBitboard(Piece.make(side, PieceType.KING)).inv())
        while (pieces != 0L) {
            val index = Bitboard.bitScanForward(pieces)
            pieces = Bitboard.extractLsb(pieces)
            val sq = Square.squareAt(index)
            sum += pieceSquareStaticValue(board.getPiece(sq), sq)
        }

        board.getPieceLocation(Piece.make(side, PieceType.KING)).forEach {
            sum += (maxMoves - phase) * KING_OPENING_PST[getIndex(side, it)] / maxMoves +
                    phase * KING_END_PST[getIndex(side, it)] / maxMoves
        }

        return sum
    }

    private fun getIndex(side: Side, sq: Square) =
            if (side == Side.BLACK) sq.ordinal else 63 - sq.ordinal

    private fun countMaterial(board: Board, side: Side) =
            bitCount(board.getBitboard(Piece.make(side, PieceType.PAWN))) * PAWN_VALUE +
                    bitCount(board.getBitboard(Piece.make(side, PieceType.BISHOP))) * BISHOP_VALUE +
                    bitCount(board.getBitboard(Piece.make(side, PieceType.KNIGHT))) * KNIGHT_VALUE +
                    bitCount(board.getBitboard(Piece.make(side, PieceType.ROOK))) * ROOK_VALUE +
                    bitCount(board.getBitboard(Piece.make(side, PieceType.QUEEN))) * QUEEN_VALUE

    private fun bitCount(bb: Long) = java.lang.Long.bitCount(bb).toLong()
}