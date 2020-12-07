package com.github.bhlangonijr.kengine.alphabeta

import com.github.bhlangonijr.kengine.eval.MATE_VALUE
import java.util.*

@ExperimentalStdlibApi
class TranspositionTable constructor(size: Int = 128) {

    enum class NodeType {
        EXACT, LOWERBOUND, UPPERBOUND
    }

    private val hashSize = 1.shl(size.countTrailingZeroBits() + 16)
    private val data = LongArray(hashSize)
    private val keys = LongArray(hashSize)
    private val mask: Long = hashSize.takeHighestOneBit() - 1L

    data class Entry constructor(val key: Long,
                                 val value: Long,
                                 val depth: Int,
                                 val nodeType: NodeType)

    fun put(key: Long, value: Long, depth: Int, nodeType: NodeType, ply: Int): Boolean {

        val entry = get(key, ply)
        if (entry == null || depth > entry.depth || nodeType == NodeType.EXACT) {
            val newValue = when {
                value >= MATE_VALUE -> value + ply
                value <= -MATE_VALUE -> value - ply
                else -> value
            }
            val d = buildData(newValue, depth, nodeType)
            keys[key.and(mask).toInt()] = key xor d
            data[key.and(mask).toInt()] = d
            return true
        }
        return false
    }

    fun get(key: Long, ply: Int): Entry? {

        val k = keys[key.and(mask).toInt()]
        val d = data[key.and(mask).toInt()]
        val entry = buildEntry(k, d, ply)

        return if ((k xor d) == key) entry else null
    }

    fun clear() {

        Arrays.fill(keys, 0L)
        Arrays.fill(data, 0L)
    }

    private fun buildData(value: Long, depth: Int, nodeType: NodeType) =
            value.and(0xFFFFFFFFL).shl(32) or
                    depth.toLong().and(0xFFFFL).shl(16) or
                    nodeType.ordinal.toLong().and(0xFFFFL)

    private val nodeValues = NodeType.values()

    private fun buildEntry(key: Long, data: Long, ply: Int): Entry {

        val value = data.ushr(32).and(0xFFFFFFFFL).toInt().toLong()
        val depth = data.ushr(16).and(0xFFFFL).toShort().toInt()
        val nodeType = nodeValues[data.and(0xFFFFL).toInt()]
        val newValue = when {
            value > MATE_VALUE -> value - ply
            value < -MATE_VALUE -> value + ply
            else -> value
        }
        return Entry(key, newValue, depth, nodeType)
    }


}