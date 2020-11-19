package com.github.bhlangonijr.kengine.montecarlo

import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.move.Move
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.ln
import kotlin.math.sqrt

class Node(val move: Move,
           val side: Side) {

    val hits = AtomicLong(0)
    val wins = AtomicLong(0)
    val losses = AtomicLong(0)
    var children: List<Node> = emptyList()
    var terminal = AtomicBoolean(false)
    var result = AtomicLong(0)

    fun pickBest(): Node {

        var selected = children[0]
        for (node in children) {
            if (node.wins.get() / (node.hits.get() + 1.0) > selected.wins.get() / (selected.hits.get() + 1.0)) {
                selected = node
            }
        }
        return selected
    }

    fun expand(moves: List<Move>, side: Side): List<Node>? {

        if (children.isEmpty()) {
            synchronized(this as Any) {
                if (children.isEmpty()) {
                    children = moves.map { Node(it, side) }
                }
            }
        }

        return children
    }

    // UCT
    fun select(temperature: Double): Node {

        var selected: Node = children[0]
        var best = Double.NEGATIVE_INFINITY
        for (node in children) {
            val winRate = node.wins.get() / (node.hits.get() + temperature)
            val exploration = sqrt(ln(hits.get().toDouble()) / node.hits.get())
            val score = winRate + temperature * exploration
            if (score > best) {
                selected = node
                best = score
            }
        }
        return selected
    }

    fun updateStats(score: Long) {

        hits.incrementAndGet()
        if (score > 0.0) wins.incrementAndGet()
        if (score < 0.0) losses.incrementAndGet()
    }

    fun terminate(result: Long) {

        this.terminal.set(true)
        this.result.set(result)
    }

    fun isLeaf() = children.isEmpty()

    override fun toString(): String {
        return "Node(move=$move, side=$side, hits=$hits, wins=$wins, losses=$losses)"
    }
}