package ru.skillbranch.skillarticles.extensions

import java.text.SimpleDateFormat
import java.util.*

fun String.indexesOf(pattern: String): List<Pair<Int, Int>> {
    val dateFormat = SimpleDateFormat(pattern, Locale("ru"))
    return dateFormat.format(this)
}