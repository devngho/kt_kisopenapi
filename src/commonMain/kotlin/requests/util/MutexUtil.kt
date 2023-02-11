package io.github.devngho.kisopenapi.requests.util

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KProperty

class KMutex<T>(var value: T) {
    private val mutex = Mutex()

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T { return value }
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) { runBlocking { mutex.withLock { this@KMutex.value = value } } }
    suspend fun setIfNull(block: () -> T) {
        mutex.withLock { if (value == null) value = block() }
    }
}

fun <T> mutex(value: T): KMutex<T> = KMutex(value)