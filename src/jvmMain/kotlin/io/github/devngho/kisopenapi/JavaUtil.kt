package io.github.devngho.kisopenapi

import io.github.devngho.kisopenapi.layer.Updatable
import io.github.devngho.kisopenapi.requests.Data
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.NoDataRequest
import io.github.devngho.kisopenapi.requests.Response
import kotlinx.coroutines.future.future
import kotlinx.coroutines.runBlocking

/**
 * Kotlin coroutines으로 만들어진 API를 Java CompletableFuture로 사용할 수 있도록 하는 유틸리티입니다.
 */
object JavaUtil {
    /**
     * [io.github.devngho.kisopenapi.requests.NoDataRequest]를 호출하고, [java.util.concurrent.CompletableFuture]로 반환합니다.
     * @see io.github.devngho.kisopenapi.requests.NoDataRequest
     * @see java.util.concurrent.CompletableFuture
     */
    @JvmStatic
    fun <T : Response> callWithoutData(req: NoDataRequest<T>) = runBlocking { future { req.call() } }

    /**
     * [io.github.devngho.kisopenapi.requests.DataRequest]를 호출하고, [java.util.concurrent.CompletableFuture]로 반환합니다.
     * @see io.github.devngho.kisopenapi.requests.DataRequest
     * @see java.util.concurrent.CompletableFuture
     */
    @JvmStatic
    fun <T : Data, U : Response> callWithData(req: DataRequest<T, U>, data: T) =
        runBlocking { future { req.call(data) } }

    /**
     * [io.github.devngho.kisopenapi.layer.Updatable]을 업데이트하고, [java.util.concurrent.CompletableFuture]로 반환합니다.
     */
    @JvmStatic
    fun updateBy(updatable: Updatable, res: Class<out Response>) =
        runBlocking { future { updatable.updateBy(res.kotlin) } }
}