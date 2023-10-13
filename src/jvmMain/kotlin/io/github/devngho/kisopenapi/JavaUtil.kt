package io.github.devngho.kisopenapi

import io.github.devngho.kisopenapi.layer.Updatable
import io.github.devngho.kisopenapi.requests.Data
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.NoDataRequest
import io.github.devngho.kisopenapi.requests.Response
import kotlinx.coroutines.future.future
import kotlinx.coroutines.runBlocking


object JavaUtil {
    @JvmStatic
    fun <T : Response> callWithoutData(req: NoDataRequest<T>) = runBlocking { future { req.call() } }

    @JvmStatic
    fun <T : Data, U : Response> callWithData(req: DataRequest<T, U>, data: T) =
        runBlocking { future { req.call(data) } }

    @JvmStatic
    fun updateBy(updatable: Updatable, res: Class<out Response>) =
        runBlocking { future { updatable.updateBy(res.kotlin) } }
}