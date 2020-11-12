package com.github.bhlangonijr.kengine.montecarlo

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.move.MoveGenerator
import com.github.bhlangonijr.chesslib.move.MoveList
import com.github.bhlangonijr.kengine.SearchEngine
import com.github.bhlangonijr.kengine.SearchState
import com.github.bhlangonijr.kengine.eval.Evaluator
import com.github.bhlangonijr.kengine.eval.MAX_VALUE
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.Collectors
import kotlin.math.max

const val DEFAULT_TEMPERATURE = 1.5

class MonteCarloSearch(private var temperature: Double = DEFAULT_TEMPERATURE, private val evaluator: Evaluator? = null) : SearchEngine {

    private val random = Random()

    override fun rooSearch(state: SearchState): Move {

        val executor = Array(state.params.threads) { Executors.newFixedThreadPool(state.params.threads) }
        val node = Node(Move(Square.NONE, Square.NONE), state.board.sideToMove)
        val boards = Array(state.params.threads) { state.board.clone() }
        val simulations = AtomicLong(0)

        if (state.params.threads > 1) {
            for (i in 1 until state.params.threads) {
                executor[i].submit {
                    while (!state.shouldStop()) {
                        val score = searchMove(node, state, boards[i], 0)
                        node.updateStats(score)
                        simulations.incrementAndGet()
                    }
                }
            }
        }

        var timestamp = System.currentTimeMillis()
        var bestScore = -MAX_VALUE
        while (!state.shouldStop()) {
            val score = searchMove(node, state, boards[0], 0)
            node.updateStats(score)
            simulations.incrementAndGet()
            if ((System.currentTimeMillis() - timestamp) > 3000L) {
                val nodes = state.nodes.get()
                val time = System.currentTimeMillis() - state.params.initialTime
                if (score > bestScore) {
                    bestScore = score
                }
                val nps = nodes / (max(time / 1000L, 1L))
                println("info depth 1 score cp $score time $time nps $nps nodes $nodes pv ${node.pickBest().move}")
                println("info string total nodes ${state.nodes.get()}")
                timestamp = System.currentTimeMillis()
            }
        }

        node.children.forEach { println(it) }
        println("bestmove ${node.pickBest().move}")
        println("info string total time ${System.currentTimeMillis() - state.params.initialTime}")
        println("info string total nodes [${state.nodes.get()}], simulations[${simulations.get()}]")
        return node.pickBest().move
    }

    private fun searchMove(node: Node, state: SearchState, board: Board, ply: Int): Long {

        state.nodes.incrementAndGet()
        if (node.terminal.get()) {
            return node.result.get()
        }

        val moves = MoveGenerator.generateLegalMoves(board)
        val isKingAttacked = board.isKingAttacked
        return when {
            moves.size == 0 && isKingAttacked -> {
                node.terminate(-1)
                -1L
            }
            moves.size == 0 && !isKingAttacked -> {
                node.terminate(0)
                0L
            }
            node.isLeaf() -> {
                if (ply == 0 && state.params.searchMoves.isNotBlank()) {
                    val searchMoves = arrayListOf<Move>()
                    searchMoves.addAll(moves
                            .stream()
                            .filter { state.params.searchMoves.contains(it.toString()) }
                            .collect(Collectors.toList()))
                    node.expand(searchMoves, board.sideToMove)
                } else {
                    node.expand(moves, board.sideToMove)
                }
                val childNode = node.select(temperature)
                board.doMove(childNode.move)
                val score = if (evaluator == null)
                    -playOut(state, board, ply + 1, childNode.move)
                else
                    -evaluator.evaluate(state, board)
                childNode.updateStats(score)
                board.undoMove()
                score
            }
            else -> {
                val childNode = node.select(temperature)
                board.doMove(childNode.move)
                val score = -searchMove(childNode, state, board, ply + 1)
                childNode.updateStats(score)
                board.undoMove()
                score
            }
        }
    }

    // play out a sequence of random moves until the end of the game
    private fun playOut(state: SearchState, board: Board, ply: Int, lastMove: Move): Long {

        state.nodes.incrementAndGet()
        var m: Move? = null
        return try {

            val moves = board.legalMoves()
            val isKingAttacked = board.isKingAttacked
            when {
                moves.size == 0 && isKingAttacked -> -1L
                moves.size == 0 && !isKingAttacked -> 0L
                board.isRepetition || board.isInsufficientMaterial -> 0L
                else -> {
                    val move = moves[random.nextInt(moves.size)]
                    val kq = board.getKingSquare(board.sideToMove.flip())
                    if (kq == move.to) {
                        println("FEN: ${board.fen}")
                        println("move: $move")
                        println("last lastMove: $lastMove")
                    }
                    m = move
                    board.doMove(move)
                    val playOutScore = -playOut(state, board, ply + 1, move)
                    board.undoMove()
                    return playOutScore
                }
            }
        } catch (e: Exception) {
            println("Error: ${e.message} - $m")
            println("FEN error pos: ${board.fen}")
            println(board)
            println("error: ${e.message}")
            e.printStackTrace()
            0L
        }
    }
}