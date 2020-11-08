package com.github.bhlangonijr.kengine

import com.github.bhlangonijr.chesslib.move.Move

interface SearchEngine {

    fun rooSearch(state: SearchState): Move

}