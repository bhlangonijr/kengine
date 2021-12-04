package com.github.bhlangonijr.kengine

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.kengine.montecarlo.MonteCarloSearch
import com.github.bhlangonijr.kengine.uci.Uci
import kotlin.system.exitProcess

const val VERSION = "1.0.2"
const val NAME = "kengine"
const val AUTHOR = "bhlangonijr"

class Main

@ExperimentalStdlibApi
fun main() {

    val search = Search(Board(), MonteCarloSearch())
    val uci = Uci(search)
    while (uci.exec(readLine()!!)) {
    }
    exitProcess(0)
}
