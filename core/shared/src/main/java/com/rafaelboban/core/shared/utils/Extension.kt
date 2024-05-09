package com.rafaelboban.core.shared.utils

fun <T> List<List<T>>.replaceLastSublist(new: List<T>): List<List<T>> {
    return if (isEmpty()) {
        listOf(new)
    } else {
        dropLast(1) + listOf(new)
    }
}

val Int.F: Float
    get() = toFloat()

val Double.F: Float
    get() = toFloat()
