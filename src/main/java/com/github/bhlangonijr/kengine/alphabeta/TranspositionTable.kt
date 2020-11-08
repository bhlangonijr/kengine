package com.github.bhlangonijr.kengine.alphabeta

import com.github.bhlangonijr.chesslib.move.Move
import java.util.*

@ExperimentalStdlibApi
class TranspositionTable constructor(private val size: Int = 128, var generation: Int = 0) {

    enum class NodeType {
        EXACT, LOWERBOUND, UPPERBOUND
    }

    private val hashSize = 1.shl(size.countTrailingZeroBits() + 20) / (4 + 8 + 4 + 4 + 8 + 8 + 8)
    private val data = arrayOfNulls<Entry>(hashSize)
    private val mask = hashSize.takeHighestOneBit() - 1

    data class Entry constructor(val key: Int, val value: Long, val depth: Int, val generation: Int,
                                 val move: Move, val nodeType: NodeType)

    fun put(key: Int, value: Long, depth: Int, move: Move, nodeType: NodeType): Boolean {

        val entry = get(key)
        if (entry == null ||
                generation > entry.generation ||
                key != entry.key ||
                (generation == entry.generation && depth > entry.depth && entry.nodeType != NodeType.EXACT)) {
            data[key.and(mask)] = Entry(key, value, depth, generation, move, nodeType)
            return true
        }
        return false
    }

    fun get(key: Int): Entry? {

        val entry = data[key.and(mask)]
        return if (entry?.key == key) entry else null
    }

    fun clear() {

        Arrays.fill(data, null)
    }
}