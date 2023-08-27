package io.github.devngho.kisopenapi

import io.github.devngho.kisopenapi.layer.Updatable
import io.github.devngho.kisopenapi.requests.Data
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.NoDataRequest
import io.github.devngho.kisopenapi.requests.Response
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future


object JavaUtil {
    @JvmStatic
    @OptIn(DelicateCoroutinesApi::class)
    @JvmName("callWithoutData")
    fun <T : Response> NoDataRequest<T>.call() = GlobalScope.future { call() }

    @JvmStatic
    @OptIn(DelicateCoroutinesApi::class)
    @JvmName("callWithData")
    fun <T : Data, U : Response> DataRequest<T, U>.call(data: T) = GlobalScope.future { call(data) }

    @OptIn(DelicateCoroutinesApi::class)
    @JvmStatic
    @JvmName("updateByClass")
    fun Updatable.updateBy(res: Class<out Response>) =
        GlobalScope.future { this@updateBy.updateBy(res.kotlin) }
}