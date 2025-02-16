package io.github.devngho.kisopenapi.requests.util

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

fun isATSAvailable(): Boolean {
    val now = Clock.System.now()
    val atsAvailableDay = LocalDateTime(2025, 3, 4, 0, 0).toInstant(TimeZone.of("Asia/Seoul"))

    return now >= atsAvailableDay
}