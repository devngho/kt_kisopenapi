package io.github.devngho.kisopenapi.layer

import io.github.devngho.kisopenapi.requests.Response
import kotlin.reflect.KClass

interface Update {
    suspend fun updateBy(res: KClass<out Response>)
    fun updateBy(res: Response)
}