package com.github.bhlangonijr.kengine

class SearchParams(var whiteTime: Long = 60000000,
                   var blackTime: Long = 60000000,
                   var whiteIncrement: Long = 0,
                   var blackIncrement: Long = 0,
                   var moveTime: Long = 60000,
                   var depth: Int = 100,
                   var movesToGo: Int = 1,
                   var nodes: Long = 50000000,
                   var searchMoves: String = "",
                   var infinite: Boolean = false,
                   var ponder: Boolean = false,
                   var threads: Int = 1) {

    val initialTime = System.currentTimeMillis()

    override fun toString(): String {

        return "SearchParams(whiteTime=$whiteTime, " +
                "blackTime=$blackTime, " +
                "whiteIncrement=$whiteIncrement, " +
                "blackIncrement=$blackIncrement, " +
                "moveTime=$moveTime, " +
                "depth=$depth, " +
                "movesToGo=$movesToGo, " +
                "nodes=$nodes, " +
                "searchMoves=$searchMoves, " +
                "infinite=$infinite, " +
                "ponder=$ponder, " +
                "threads=$threads)"
    }
}