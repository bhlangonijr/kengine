package com.github.bhlangonijr.kengine.uci

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.kengine.*
import com.github.bhlangonijr.kengine.alphabeta.AlphaBetaSearch
import com.github.bhlangonijr.kengine.alphabeta.TranspositionTable
import com.github.bhlangonijr.kengine.montecarlo.MonteCarloSearch

const val ALGO_MCTS_OPTION = "MonteCarlo"
const val ALGO_ABTS_OPTION = "AlphaBeta"

@ExperimentalStdlibApi
class Uci constructor(private var search: Search) {

    fun exec(cmd: String): Boolean {

        val tokens = cmd.split(" ")
        val command = tokens[0]
        return when (command) {
            "uci" -> handleUci()
            "ucinewgame" -> handleUciNewGame()
            "isready" -> handleIsReady()
            "position" -> handlePosition(tokens)
            "go" -> handleGo(tokens)
            "stop" -> handleStop()
            "quit" -> handleQuit()
            "setoption" -> handleSetoption(tokens)
            else -> handleUnknownCommand(cmd)
        }
    }

    private fun handleUci(): Boolean {

        println("id name $NAME $VERSION")
        println("id author $AUTHOR")
        println("option name Hash type spin default 128 min 1 max 16384")
        println("option name Threads type spin default 1 min 1 max 128")
        println("option name SearchAlgorithm type combo default $ALGO_MCTS_OPTION " +
                "var $ALGO_ABTS_OPTION var $ALGO_MCTS_OPTION")
        println("uciok")
        return true
    }

    private fun handleUciNewGame(): Boolean {

        search.stop()
        search.reset()
        return true
    }

    private fun handleIsReady(): Boolean {

        //do stuff
        println("readyok")
        return true
    }

    private fun handlePosition(tokens: List<String>): Boolean {

        val positionType = tokens[1]
        when (positionType) {
            "fen" -> {
                val moves = mergeTokens(tokens, "moves", " ").trim()
                val part = getString(tokens, "fen", "")
                val state = if (moves.isNotBlank())
                    mergeTokens(tokens, part, "moves", " ")
                else
                    mergeTokens(tokens, part, " ")
                val fen = "$part $state".trim()
                search.setupPosition(fen, moves)
            }
            "startpos" -> {
                val moves = mergeTokens(tokens, "moves", " ").trim()
                search.setupPosition(moves)
            }
            else -> println("info string ignoring malformed uci command")

        }
        return true
    }

    private fun handleGo(tokens: List<String>): Boolean {

        val params = SearchParams(
                whiteTime = getLong(tokens, "wtime", "6000000"),
                blackTime = getLong(tokens, "btime", "6000000"),
                whiteIncrement = getLong(tokens, "winc", "0"),
                blackIncrement = getLong(tokens, "binc", "0"),
                moveTime = getLong(tokens, "movetime", "0"),
                movesToGo = getInt(tokens, "movestogo", "0"),
                depth = getInt(tokens, "depth", "100"),
                nodes = getLong(tokens, "nodes", "5000000000"),
                infinite = getBoolean(tokens, "infinite", "true"),
                ponder = getBoolean(tokens, "ponder", "false"),
                searchMoves = getString(tokens, "searchmoves", ""),
                threads = search.threads
        )

        return search.start(params)
    }

    @ExperimentalStdlibApi
    private fun handleSetoption(tokens: List<String>): Boolean {

        val option = tokens[2]
        val value = tokens[4]

        when (option) {
            "Hash" -> search = Search(Board(), AlphaBetaSearch(transpositionTable = TranspositionTable(value.toInt())))
            "Threads" -> search.threads = value.toInt()
            "SearchAlgorithm" -> {
                search = if (value == ALGO_ABTS_OPTION) {
                    Search(Board(), AlphaBetaSearch())
                } else {
                    Search(Board(), MonteCarloSearch())
                }
            }
            else -> println("info string ignoring unsupported uci option")
        }

        return true
    }

    private fun handleStop(): Boolean = search.stop()

    private fun handleQuit(): Boolean {

        println("bye")
        return false
    }

    private fun handleUnknownCommand(cmd: String): Boolean {

        println("info string unknown command: $cmd")
        return true
    }

}
