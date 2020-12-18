package ru.skillbranch.skillarticles.extensions

import java.text.SimpleDateFormat
import java.util.*

const val SECOND = 1000L
const val MINUTE = 60 * SECOND
const val HOUR = 60 * MINUTE
const val DAY = 24 * HOUR

fun Date.format(pattern: String = "HH:mm:ss dd.MM.yy"): String {
    val dateFormat = SimpleDateFormat(pattern, Locale("ru"))
    return dateFormat.format(this)
}

fun Date.add(value: Int, units: TimeUnits = TimeUnits.SECOND): Date {
    var time = this.time
    time += when (units) {
        TimeUnits.SECOND -> value * SECOND
        TimeUnits.MINUTE -> value * MINUTE
        TimeUnits.HOUR -> value * HOUR
        TimeUnits.DAY -> value * DAY
    }
    this.time = time
    return this
}

fun Date.shortFormat(): String? {
    val pattern = if (this.isSameDay(Date())) "HH:mm" else "dd.MM.yy"
    val dateFormat = SimpleDateFormat(pattern, Locale("ru"))
    return dateFormat.format(this)
}

fun Date.isSameDay(date: Date): Boolean {
    val day1 = this.time / DAY
    val day2 = date.time / DAY
    return day1 == day2
}

interface Number {
    fun plural(i: Long): String?
}

enum class TimeUnits : Number {
    SECOND {
        @Override
        override fun plural(i: Long): String? {

            return if (i <= 20) {
                when (i) {
                    0L -> "только что"
                    1L -> "1 секунду"
                    in 2..4 -> "$i секунды"
                    in 5..20 -> "$i секунд"
                    else -> "Ошибка"
                }

            } else {
                when (i % 10) {
                    0L -> "$i секунд"
                    1L -> "$i секунду"
                    in 2..4 -> "$i секунды"
                    in 5..9 -> "$i секунд"
                    else -> "Ошибка"
                }
            }
        }
    },
    MINUTE {
        @Override
        override fun plural(i: Long): String {

            return if (i <= 20) {
                when (i) {
                    0L -> "только что"
                    1L -> "1 минуту"
                    in 2..4 -> "$i минуты"
                    in 5..20 -> "$i минут"
                    else -> "Ошибка"
                }

            } else {
                when (i % 10) {
                    0L -> "$i минут"
                    1L -> "$i минуту"
                    in 2..4 -> "$i минуты"
                    in 5..9 -> "$i минут"
                    else -> "Ошибка"
                }
            }
        }
    },
    HOUR {
        override fun plural(i: Long): String {

            return if (i <= 20) {
                when (i) {
                    0L -> "только что"
                    1L -> "1 час"
                    in 2..4 -> "$i часа"
                    in 5..20 -> "$i часов"
                    else -> "Ошибка"
                }

            } else {
                when (i % 10) {
                    0L -> "$i часов"
                    1L -> "$i час"
                    in 2..4 -> "$i часа"
                    in 5..9 -> "$i часов"
                    else -> "Ошибка"
                }
            }
        }
    },
    DAY {
        override fun plural(i: Long): String {

            return if (i <= 20) {
                when (i) {
                    0L -> "только что"
                    1L -> "1 день"
                    in 2..4 -> "$i дня"
                    in 5..20 -> "$i дней"
                    else -> "Ошибка"
                }

            } else {
                when (i % 10) {
                    0L -> "$i дней"
                    1L -> "$i день"
                    in 2..4 -> "$i дня"
                    in 5..9 -> "$i дней"
                    else -> "Ошибка"
                }
            }
        }
    }
}

fun Date.humanizeDiff(date: Date = Date()): String {
    var diff = date.time - this.time
    if (diff > 0) {
        return when (diff) {
            in 0..SECOND -> "только что"
            in SECOND..MINUTE -> "несколько секунд назад"
            in 45 * SECOND..75 * SECOND -> "минуту назад"
            in 75 * SECOND..45 * MINUTE -> "${TimeUnits.MINUTE.plural(diff / MINUTE)} назад"
            in 45 * MINUTE..75 * MINUTE -> "час назад"
            in 75 * MINUTE..22 * HOUR -> "${TimeUnits.HOUR.plural(diff / HOUR)} назад"
            in 22 * HOUR..26 * HOUR -> "день назад"
            in 26 * HOUR..360 * DAY -> "${TimeUnits.DAY.plural(diff / DAY)} назад"
            else -> "более года назад"
        }
    } else {
        diff = -diff
        return when (diff) {
            in 0..SECOND -> "только что"
            in SECOND..MINUTE -> "через несколько секунд"
            in 45 * SECOND..75 * SECOND -> "через минуту"
            in 75 * SECOND..45 * MINUTE -> "через ${TimeUnits.MINUTE.plural(diff / MINUTE)}"
            in 45 * MINUTE..75 * MINUTE -> "через час"
            in 75 * MINUTE..22 * HOUR -> "через ${TimeUnits.HOUR.plural(diff / HOUR)}"
            in 22 * HOUR..26 * HOUR -> "через день"
            in 26 * HOUR..360 * DAY -> "через ${TimeUnits.DAY.plural(diff / DAY)}"
            else -> "более чем через год"
        }
    }
}