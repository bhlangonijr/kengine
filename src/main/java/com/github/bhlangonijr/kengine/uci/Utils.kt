package com.github.bhlangonijr.kengine.uci

fun mergeTokens(tokens: List<String>, startToken: String, separator: String): String {

    val start = tokens.indexOf(startToken)
    return if (start == -1) {
        ""
    } else {
        mergeTokens(tokens, start + 1, tokens.size - 1, separator)
    }
}

fun mergeTokens(tokens: List<String>, startToken: String, endToken: String, separator: String): String {

    val start = tokens.indexOf(startToken)
    val end = tokens.indexOf(endToken) - 1
    return if (start == -1) {
        ""
    } else {
        mergeTokens(tokens, start + 1, end, separator)
    }
}

fun mergeTokens(tokens: List<String>, start: Int, end: Int, separator: String): String {

    val str = StringBuilder()
    for (i in start..end) {
        str.append("${tokens[i]}$separator")
    }
    return str.toString()
}

fun getString(tokens: List<String>, startToken: String, defaultValue: String): String {

    val start = tokens.indexOf(startToken)
    if (start == -1 || tokens.size - 1 < start + 1) {
        return defaultValue
    }
    return mergeTokens(tokens, start + 1, start + 1, "")
}

fun getLong(tokens: List<String>, startToken: String, defaultValue: String): Long {

    return getString(tokens, startToken, defaultValue).toLong()
}

fun getInt(tokens: List<String>, startToken: String, defaultValue: String): Int {

    return getString(tokens, startToken, defaultValue).toInt()
}

fun getBoolean(tokens: List<String>, startToken: String, defaultValue: String): Boolean {

    return getString(tokens, startToken, defaultValue).toBoolean()
}

