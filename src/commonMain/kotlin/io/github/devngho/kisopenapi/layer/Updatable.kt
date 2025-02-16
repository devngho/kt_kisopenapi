package io.github.devngho.kisopenapi.layer

import io.github.devngho.kisopenapi.requests.Response
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

/**
 * 업데이트 가능한 객체입니다.
 * @see StockDomesticImpl
 */
interface Updatable {
    /**
     * 주어진 [type]에 따라 적절한 요청을 호출해 값을 업데이트합니다. 여러 인자가 주어지면 동시에 업데이트합니다.
     */
    suspend fun update(vararg type: KClass<out Response>)

    /**
     * 주어진 [res]로 값을 업데이트합니다.
     */
    fun updateBy(res: Response)

    companion object {
        /**
         * 주어진 [T] 타입에 따라 적절한 요청을 호출해 값을 업데이트합니다.
         */
        @JvmName("updateT1")
        suspend inline fun <reified T : Response> Updatable.update() = update(T::class)

        /**
         * 주어진 [T] 타입에 따라 적절한 요청을 호출해 값을 업데이트합니다.
         */
        @JvmName("updateT2")
        suspend inline fun <reified T : Response, reified U : Response> Updatable.update() = update(T::class, U::class)

        /**
         * 주어진 [T] 타입에 따라 적절한 요청을 호출해 값을 업데이트합니다.
         */
        @JvmName("updateT3")
        @Suppress("unused")
        suspend inline fun <reified T : Response, reified U : Response, reified V : Response> Updatable.update() =
            update(T::class, U::class, V::class)

        /**
         * 주어진 [T] 타입에 따라 적절한 요청을 호출해 값을 업데이트합니다.
         */
        @JvmName("updateT4")
        @Suppress("unused")
        suspend inline fun <reified T : Response, reified U : Response, reified V : Response, reified W : Response> Updatable.update() =
            update(T::class, U::class, V::class, W::class)
    }
}