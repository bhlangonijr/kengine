package com.github.bhlangonijr.kengine.alphabeta

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.move.MoveGenerator.generatePseudoLegalCaptures
import com.github.bhlangonijr.chesslib.move.MoveGenerator.generatePseudoLegalMoves
import com.github.bhlangonijr.chesslib.move.MoveList
import com.github.bhlangonijr.kengine.SearchEngine
import com.github.bhlangonijr.kengine.SearchState
import com.github.bhlangonijr.kengine.eval.Evaluator
import com.github.bhlangonijr.kengine.eval.MATE_VALUE
import com.github.bhlangonijr.kengine.eval.MAX_VALUE
import com.github.bhlangonijr.kengine.eval.MaterialEval
import kotlin.math.max
import kotlin.math.min


const val MAX_DEPTH = 100

@ExperimentalStdlibApi
class AlphaBetaSearch constructor(private var evaluator: Evaluator = MaterialEval(),
                                  private var transpositionTable: TranspositionTable = TranspositionTable()) : SearchEngine {

    private val emptyMove = Move(Square.NONE, Square.NONE)

    override fun rooSearch(state: SearchState): Move {

        val fen = state.board.fen
        transpositionTable.generation++
        state.moveScore.clear()
        var bestMove = emptyMove
        for (i in 1..min(MAX_DEPTH, state.params.depth)) {
            val score = search(state.board, -MAX_VALUE, MAX_VALUE, i, 0, state)
            if (state.shouldStop() && bestMove != emptyMove) break
            bestMove = state.pv[0]
            val nodes = state.nodes.get()
            val time = System.currentTimeMillis() - state.params.initialTime
            val nps = nodes / (max(time / 1000, 1))
            println("info depth $i score cp $score time $time nodes $nodes nps $nps pv ${state.pvLine()}")
        }
        println("bestmove $bestMove")
        if (state.board.fen != fen) {
            println("info string board state error: initial fen [$fen], final fen[${state.board.fen}]")
        }
        println("info string total time ${System.currentTimeMillis() - state.params.initialTime}")
        return bestMove
    }

    private fun search(board: Board, alpha: Long, beta: Long, depth: Int, ply: Int, state: SearchState): Long {

        if (depth <= 0 || ply >= MAX_DEPTH) {
            return quiesce(board, alpha, beta, depth, ply, state)
        }
        state.nodes.incrementAndGet()
        if (state.shouldStop()) {
            return 0L
        }
        if (board.isRepetition || board.isInsufficientMaterial) {
            return 0L
        }

        var bestScore = -Long.MAX_VALUE
        var newAlpha = alpha
        var newBeta = beta
        var bestMove = emptyMove
        var hashMove = emptyMove

        val entry = transpositionTable.get(board.hashCode())
        if (entry != null && entry.depth >= depth && ply > 0) {
            hashMove = entry.move
            when (entry.nodeType) {
                TranspositionTable.NodeType.EXACT -> {
                    return entry.value
                }
                TranspositionTable.NodeType.LOWERBOUND -> {
                    newAlpha = max(alpha, entry.value)
                }
                TranspositionTable.NodeType.UPPERBOUND -> {
                    newBeta = min(beta, entry.value)
                }
            }
        }

        val isKingAttacked = board.isKingAttacked

        if (depth > 1 && beta <= evaluator.evaluate(state, board) &&
                !isKingAttacked && isNullMoveAllowed(board)) {

            board.doNullMove()
            val score = -search(board, -newBeta, -newBeta + 1, depth - 3, ply + 1, state)
            board.undoMove()
            if (score >= newBeta) {
                transpositionTable.put(board.hashCode(), score, depth, bestMove,
                        TranspositionTable.NodeType.LOWERBOUND)
                return score
            }
        }

        val moves = generateMoves(state, ply, hashMove, false)
        for (move in moves) {

            if (!board.doMove(move)) {
                continue
            }
            val newDepth = if (isKingAttacked) depth else depth - 1

            var score: Long

            if (bestScore == -Long.MAX_VALUE || move == hashMove) {
                score = -search(board, -newBeta, -newAlpha, newDepth, ply + 1, state)
            } else {
                score = -search(board, -newAlpha - 1, -newAlpha, newDepth, ply + 1, state)
                if (score in (newAlpha + 1) until newBeta) {
                    score = -search(board, -newBeta, -newAlpha, newDepth, ply + 1, state)
                }
            }

            board.undoMove()
            if (ply == 0) {
                state.moveScore[move.toString()] = score
            }
            if (score >= newBeta) {
                transpositionTable.put(board.hashCode(), score, depth, move,
                        TranspositionTable.NodeType.LOWERBOUND)
                return score
            }
            if (score > bestScore) {
                bestScore = score
                if (score > newAlpha) {
                    bestMove = move
                    newAlpha = score
                    state.updatePv(move, ply)
                }
            }
        }

        val nodeType = when {
            bestScore > alpha -> TranspositionTable.NodeType.EXACT
            else -> TranspositionTable.NodeType.UPPERBOUND
        }
        transpositionTable.put(board.hashCode(), bestScore, depth, bestMove, nodeType)

        if (bestScore == -Long.MAX_VALUE) {
            return if (isKingAttacked) -MATE_VALUE + ply else 0L
        }
        return bestScore
    }

    fun quiesce(board: Board, alpha: Long, beta: Long, depth: Int, ply: Int, state: SearchState): Long {

        state.nodes.incrementAndGet()

        if (state.shouldStop()) {
            return 0
        }
        if (board.isRepetition || board.isInsufficientMaterial) {
            return 0L
        }
        var newAlpha = alpha

        var bestScore = evaluator.evaluate(state, board)
        if (bestScore >= beta) {
            return beta
        }
        if (alpha < bestScore) {
            newAlpha = bestScore
        }

        val moves = generateMoves(state, ply, emptyMove, true)
        for (move in moves) {
            if (!board.doMove(move)) {
                continue
            }
            val score = -quiesce(board, -beta, -newAlpha, depth - 1, ply + 1, state)
            board.undoMove()
            if (score >= beta) {
                return score
            }
            if (score > bestScore) {
                bestScore = score
                if (score > newAlpha) {
                    newAlpha = score
                    state.updatePv(move, ply)
                }
            }
        }

        if (bestScore == -Long.MAX_VALUE && board.isKingAttacked) {
            return -MATE_VALUE + ply
        }
        return bestScore
    }

    private fun generateMoves(state: SearchState, ply: Int, hashMove: Move, quiesce: Boolean): List<Move> {

        return when {
            quiesce -> orderMoves(state, hashMove, generatePseudoLegalCaptures(state.board))
            ply == 0 -> orderRootMoves(state, generatePseudoLegalMoves(state.board))
            else -> orderMoves(state, hashMove, generatePseudoLegalMoves(state.board))
        }
    }

    private fun orderMoves(state: SearchState, hashMove: Move, moves: MoveList): List<Move> {

        if (state.moveScore.size == 0) return moves
        return moves.sortedWith(Comparator { o1, o2 ->
            (moveScore(o1, hashMove, state) - moveScore(o2, hashMove, state)).toInt()
        }).reversed()
    }

    private fun orderRootMoves(state: SearchState, moves: MoveList): List<Move> {

        if (state.moveScore.size == 0) return moves
        return moves.sortedWith(Comparator { o1, o2 ->
            (rootMoveScore(o1, state) - rootMoveScore(o2, state)).toInt()
        }).reversed()
    }

    private fun rootMoveScore(move: Move, state: SearchState) = state.moveScore[move.toString()] ?: -Long.MAX_VALUE

    //mvv-lva
    private fun moveScore(move: Move, hashMove: Move, state: SearchState): Long {

        val attackedPiece = state.board.getPiece(move.to)
        val attackingPiece = state.board.getPiece(move.from)

        return when {
            move == hashMove -> MATE_VALUE
            attackedPiece != Piece.NONE -> (evaluator.pieceStaticValue(attackedPiece) -
                    evaluator.pieceStaticValue(attackingPiece)) + 10000
            move.promotion != null -> evaluator.pieceStaticValue(move.promotion) * 10
            else -> (evaluator.pieceSquareStaticValue(attackingPiece, move.to) -
                    evaluator.pieceSquareStaticValue(attackingPiece, move.from))
        }
    }

    private fun isNullMoveAllowed(board: Board): Boolean =
            board.getBitboard(Side.WHITE).xor(board.getBitboard(Piece.WHITE_PAWN)
                    .or(board.getBitboard(Piece.WHITE_KING))) == 0L ||
                    board.getBitboard(Side.BLACK).xor(board.getBitboard(Piece.BLACK_PAWN)
                            .or(board.getBitboard(Piece.BLACK_KING))) == 0L

}