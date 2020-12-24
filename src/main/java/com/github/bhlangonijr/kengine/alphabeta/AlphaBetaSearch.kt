package com.github.bhlangonijr.kengine.alphabeta

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.PieceType
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.move.MoveGenerator
import com.github.bhlangonijr.kengine.SearchEngine
import com.github.bhlangonijr.kengine.SearchState
import com.github.bhlangonijr.kengine.eval.*
import kotlin.math.max
import kotlin.math.min


const val MAX_DEPTH = 100

@ExperimentalStdlibApi
class AlphaBetaSearch constructor(private var evaluator: Evaluator = MaterialEval(),
                                  private var transpositionTable: TranspositionTable = TranspositionTable()) : SearchEngine {

    private val emptyMove = Move(Square.NONE, Square.NONE)

    override fun rooSearch(state: SearchState): Move {

        val fen = state.board.fen
        state.moveScore.clear()
        var bestMove = emptyMove
        var alpha = -MAX_VALUE
        var beta = MAX_VALUE
        var score = -MAX_VALUE
        for (i in 1..min(MAX_DEPTH, state.params.depth)) {

            var aspirationWindow = 16
            if (i > 3) {
                alpha = max(score - aspirationWindow, -MAX_VALUE)
                beta = min(score + aspirationWindow, MAX_VALUE)
            }
            while (true) {
                score = search(state.board, alpha, beta, i, 0, state)
                if (state.shouldStop() && bestMove != emptyMove) break
                bestMove = state.pv[0]
                val nodes = state.nodes.get()
                val time = System.currentTimeMillis() - state.params.initialTime
                val nps = nodes / (max(time / 1000, 1))
                val uciDetails = "time $time nodes $nodes nps $nps pv ${state.pvLine()}"
                if (score <= alpha) {
                    alpha = max(score - aspirationWindow, -MAX_VALUE)
                    beta = (alpha + beta) / 2
                    println("info depth $i score cp $score upperbound $uciDetails")
                } else if (score >= beta) {
                    beta = min(score + aspirationWindow, MAX_VALUE)
                    println("info depth $i score cp $score lowerbound $uciDetails")
                } else {
                    println("info depth $i score cp $score $uciDetails")
                    break
                }
                aspirationWindow += aspirationWindow / 4
            }
        }
        println("bestmove $bestMove")
        if (state.board.fen != fen) {
            println("info string board state error: initial fen [$fen], final fen[${state.board.fen}]")
        }
        println("info string total time ${System.currentTimeMillis() - state.params.initialTime}")
        return bestMove
    }

    private fun search(board: Board, alpha: Long, beta: Long, depth: Int, ply: Int, state: SearchState, doNullMove: Boolean = true): Long {

        if (depth <= 0 || ply >= MAX_DEPTH) {
            return quiesce(board, alpha, beta, ply, state)
        }
        state.nodes.incrementAndGet()
        if (state.shouldStop()) {
            return 0L
        }
        if (board.isRepetition) {
            return 0L
        }

        var bestScore = -Long.MAX_VALUE
        var newAlpha = max(alpha, -Long.MAX_VALUE + ply)
        val newBeta = min(beta, Long.MAX_VALUE - (ply + 1))
        var moveCounter = 0

        if (newAlpha >= newBeta) {
            return newAlpha
        }

        val entry = transpositionTable.get(board.incrementalHashKey, ply)
        if (entry != null && entry.depth >= depth && ply > 0) {
            when (entry.nodeType) {
                TranspositionTable.NodeType.EXACT -> {
                    return entry.value
                }
                TranspositionTable.NodeType.LOWERBOUND -> {
                    if (entry.value > newBeta) {
                        return entry.value
                    }
                }
                TranspositionTable.NodeType.UPPERBOUND -> {
                    if (entry.value <= newAlpha) {
                        return entry.value
                    }
                }
            }
        }

        val isKingAttacked = board.isKingAttacked

        if (doNullMove && depth > 3 && newBeta <= evaluator.evaluate(state, board) &&
                !isKingAttacked && isNullMoveAllowed(board)) {

            board.doNullMove()
            val score = -search(board, -newBeta, -newBeta + 1, depth - 3, ply + 1, state, false)
            board.undoMove()
            if (score >= newBeta) {
                transpositionTable.put(board.incrementalHashKey, score, depth,
                        TranspositionTable.NodeType.LOWERBOUND, ply)
                return score
            }
        }

        val moves = generateMoves(state, ply, false)
        for (move in moves) {
            if (!board.doMove(move)) {
                continue
            }
            moveCounter++
            var score: Long

            if (moveCounter == 0) {
                val newDepth = if (isKingAttacked) depth + 1 else depth - 1
                score = -search(board, -newBeta, -newAlpha, newDepth, ply + 1, state)
            } else {
                val newDepth = if (isKingAttacked) depth else depth - 1
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
                transpositionTable.put(board.incrementalHashKey, score, depth,
                        TranspositionTable.NodeType.LOWERBOUND, ply)
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

        if (moveCounter == 0) {
            return if (isKingAttacked) -MATE_VALUE + ply else 0L
        }

        val nodeType = when {
            bestScore > alpha -> TranspositionTable.NodeType.EXACT
            else -> TranspositionTable.NodeType.UPPERBOUND
        }
        transpositionTable.put(board.incrementalHashKey, bestScore, depth, nodeType, ply)

        return bestScore
    }

    fun quiesce(board: Board, alpha: Long, beta: Long, ply: Int, state: SearchState): Long {

        if (state.shouldStop() || ply >= MAX_DEPTH) {
            return 0
        }
        state.nodes.incrementAndGet()
        if (board.isRepetition) {
            return 0
        }
        var moveCounter = 0
        val eval = evaluator.evaluate(state, board)
        var bestScore = max(eval, alpha)

        if (bestScore >= beta) {
            return beta
        }

        val moves = generateMoves(state, ply, true)
        for (move in moves) {
            if (move.promotion != Piece.NONE && move.promotion.pieceType != PieceType.QUEEN) {
                continue
            }
            if (!board.doMove(move)) {
                continue
            }
            moveCounter++
            val score = -quiesce(board, -beta, -bestScore, ply + 1, state)
            board.undoMove()
            if (score >= beta) {
                return score
            }
            if (score > bestScore) {
                bestScore = score
                state.updatePv(move, ply)
            }
        }

        return bestScore
    }

    private fun generateMoves(state: SearchState, ply: Int, quiesce: Boolean): List<Move> {

        return when {
            quiesce -> orderMoves(state, state.board.pseudoLegalCaptures())
            ply == 0 -> orderRootMoves(state, state.board.pseudoLegalMoves())
            else -> orderMoves(state, state.board.pseudoLegalMoves())
        }
    }

    private fun orderMoves(state: SearchState, moves: List<Move>): List<Move> {

        if (state.moveScore.size == 0) return moves
        return moves.sortedWith { o1, o2 ->
            (moveScore(o1, state) - moveScore(o2, state)).toInt()
        }.reversed()
    }

    private fun orderRootMoves(state: SearchState, moves: List<Move>): List<Move> {

        if (state.moveScore.size == 0) return moves
        return moves.sortedWith { o1, o2 ->
            (rootMoveScore(o1, state) - rootMoveScore(o2, state)).toInt()
        }.reversed()
    }

    private fun rootMoveScore(move: Move, state: SearchState) = state.moveScore[move.toString()] ?: -Long.MAX_VALUE

    //mvv-lva
    private fun moveScore(move: Move, state: SearchState): Long {

        val attackedPiece = state.board.getPiece(move.to)
        val attackingPiece = state.board.getPiece(move.from)

        return when {
            attackedPiece != Piece.NONE -> evaluator.pieceStaticValue(attackedPiece)
            move.promotion != Piece.NONE -> evaluator.pieceStaticValue(move.promotion)
            else -> evaluator.pieceSquareStaticValue(attackingPiece, move.to)
        }
    }

    private fun isNullMoveAllowed(board: Board): Boolean =
            board.getBitboard(Piece.make(board.sideToMove, PieceType.KING)) or
                    board.getBitboard(Piece.make(board.sideToMove, PieceType.PAWN)) != board.getBitboard(board.sideToMove)


}