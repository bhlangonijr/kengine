package com.github.bhlangonijr.kengine

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.move.MoveList
import com.github.bhlangonijr.kengine.alphabeta.MAX_DEPTH
import java.util.concurrent.atomic.AtomicLong

class SearchState(val params: SearchParams, val board: Board) {


    private val noMove = Move(Square.NONE, Square.NONE)

    @Volatile
    var stopped = false
    var pvPly = 0
    val timeout = params.initialTime + timeToThink(params)
    val nodes = AtomicLong()
    val moveScore = HashMap<String, Long>()

    val pv = Array(MAX_DEPTH) { noMove }

    fun shouldStop(): Boolean {

        if (stopped || nodes.get() >= params.nodes) {
            return true
        }
        return System.currentTimeMillis() >= timeout
    }

    fun updatePv(move: Move, ply: Int) {

        if (pvPly > ply) pv.fill(noMove, ply, pvPly)
        pvPly = ply
        pv[ply] = move
    }

    fun pvLine(): MoveList {

        val moves = MoveList(board.fen)
        moves += pv.takeWhile { !(it.from == Square.NONE || it.to == Square.NONE) }
        return moves
    }

    private fun timeToThink(params: SearchParams): Long {

        return when (board.sideToMove) {
            Side.WHITE -> params.whiteTime / 40L + params.whiteIncrement
            else -> params.blackTime / 40L + params.blackIncrement
        }
    }
}