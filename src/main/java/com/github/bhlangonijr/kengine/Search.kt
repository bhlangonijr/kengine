package com.github.bhlangonijr.kengine

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.move.MoveList
import java.util.concurrent.Executors

class Search(val board: Board, val engine: SearchEngine) {

    private val executor = Executors.newSingleThreadExecutor()
    private var state: SearchState? = null
    var threads: Int = 1

    fun reset() {

        board.loadFromFen(board.context.startFEN)
    }

    fun setupPosition(fen: String, moves: String) {

        if (moves.isNotBlank()) {
            val moveList = MoveList(fen)
            moveList.loadFromText(moves)
            board.loadFromFen(moveList.getFen(moveList.size))
        } else {
            board.loadFromFen(fen)
        }
    }

    fun setupPosition(moves: String) {

        if (moves.isNotBlank()) {
            val moveList = MoveList()
            moveList.loadFromText(moves)
            board.loadFromFen(moveList.getFen(moveList.size))
        } else {
            reset()
        }
    }

    @Synchronized
    fun start(params: SearchParams): Boolean {

        val search = this
        if (state == null) {
            val state = SearchState(params, board)
            executor.submit {
                engine.rooSearch(state)
                search.stop()
            }
            this.state = state
        } else {
            println("info string search in progress...")
        }
        return true
    }

    @Synchronized
    fun stop(): Boolean {

        state?.stopped = true
        state = null
        return true
    }
}